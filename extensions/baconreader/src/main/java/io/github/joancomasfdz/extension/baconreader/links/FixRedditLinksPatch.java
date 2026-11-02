// SPDX-FileCopyrightText: 2026 JoanComasFdz
// SPDX-License-Identifier: GPL-3.0-only

package io.github.joancomasfdz.extension.baconreader.links;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.onelouder.baconreader.BrowserActivity;
import com.onelouder.baconreader.DetailActivity;
import com.onelouder.baconreader.LinkDispatcher;
import com.onelouder.baconreader.LinkProcessor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.morphe.extension.shared.Logger;
import app.morphe.extension.shared.Utils;

/**
 * Opens the Reddit links LinkProcessor doesn't recognize in the app: share links, resolved through their redirect,
 * posts without their subreddit, galleries, videos, found through Reddit's api/info, and posts on a user's profile.
 */
@SuppressWarnings("unused")
public final class FixRedditLinksPatch {
    /** Share links resolve on www.reddit.com whatever host the link names. */
    private static final String REDDIT = "https://www.reddit.com";

    private static final String VIDEO_HOST = "v.redd.it";

    private static final String EXTRA_SHARE_LINK = "io.github.joancomasfdz.extension.baconreader.links.SHARE_LINK";
    private static final String EXTRA_VIDEO_LINK = "io.github.joancomasfdz.extension.baconreader.links.VIDEO_LINK";

    private static final int TIMEOUT_MS = 10_000;

    /**
     * The ID of a post's own gallery or Reddit video, the last segment of its address, which the app is opening in its
     * media viewer: {@link #intent} leaves it to the app instead of opening the post again. The next {@link #intent}
     * call takes it, so it never applies to a later link. LinkProcessor rewrites some Reddit addresses, so the ID is
     * compared, not the address.
     */
    private static final ThreadLocal<String> appMedia = new ThreadLocal<>();

    private FixRedditLinksPatch() {
    }

    /**
     * Injected at the start of LinkProcessor.getIntent. Returns the intent for the Reddit links the app doesn't
     * recognize, or null to let the app decide.
     */
    public static Intent intent(Object processor) {
        String keep = appMedia.get();
        appMedia.remove();
        String url = null;
        try {
            if ((boolean) field(processor, "forceBrowser")) return null;
            Context context = (Context) field(processor, "context");
            url = (String) field(processor, "url");
            if (context == null || url == null) return null;
            Uri uri = Uri.parse(url);
            if (keep != null && keep.equals(uri.getLastPathSegment())) return null;
            // v.redd.it/<video>: the app looks up its post through www.reddit.com/video/<video>, which Reddit refuses
            // (403).
            if (VIDEO_HOST.equals(uri.getHost()) && uri.getPathSegments() != null
                    && uri.getPathSegments().size() == 1) {
                return new Intent(context, LinkDispatcher.class).setData(uri).putExtra(EXTRA_VIDEO_LINK, true);
            }
            if (!LinkProcessor.isReddit(url)) return null;
            List<String> segments = uri.getPathSegments();
            if (segments == null || segments.isEmpty()) return null;
            String first = segments.get(0);
            boolean isUser = "u".equals(first) || "user".equals(first);
            int size = segments.size();
            // /r/<sub>/s/<code> and /u/<user>/s/<code>: Reddit's share links, which only say what they open through
            // a redirect.
            if (("r".equals(first) || isUser) && size == 4 && "s".equals(segments.get(2))) {
                return new Intent(context, LinkDispatcher.class).setData(uri).putExtra(EXTRA_SHARE_LINK, true);
            }
            // /comments/<post>[/<title>[/<comment>]]: a post without its subreddit.
            if ("comments".equals(first) && size >= 2) {
                return detail(context, uri, segments.get(1), size >= 4 ? segments.get(3) : null);
            }
            // /gallery/<post>
            if ("gallery".equals(first) && size == 2) return detail(context, uri, segments.get(1), null);
            // /user/<user>/comments/<post>[/<title>[/<comment>]]: a post on a user's profile, which the app opens
            // as the profile.
            if (isUser && size >= 4 && "comments".equals(segments.get(2))) {
                return detail(context, uri, segments.get(3), size >= 6 ? segments.get(5) : null);
            }
            return null;
        } catch (Exception ex) {
            String link = url;
            Logger.printInfo(() -> "Could not read the link " + link, ex);
            return null;
        }
    }

    private static Object field(Object processor, String name) throws ReflectiveOperationException {
        Field field = processor.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(processor);
    }

    private static Intent detail(Context context, Uri uri, String post, String comment) {
        if (post.isEmpty()) return null;
        Intent intent = new Intent(context, DetailActivity.class).putExtra(DetailActivity.EXTRA_LINKID, post);
        int parents = 0;
        try {
            String parentsParameter = uri.getQueryParameter("context");
            if (parentsParameter != null) parents = Integer.parseInt(parentsParameter);
        } catch (NumberFormatException ignored) {
        }
        intent.putExtra(DetailActivity.EXTRA_PARENTS, parents);
        if (comment != null && !comment.isEmpty()) intent.putExtra(DetailActivity.EXTRA_COMMENTID, comment);
        return intent;
    }

    /**
     * Injected at the start of LinkProcessor.getBrowserIntent(Context, Link) and of the post header's
     * DetailLinkHelper.onOpen(Link), which open a post's media from its thumbnail. A gallery or Reddit video the post carries opens in the app's media viewer, as before
     * {@link #intent} turned its link into the post itself.
     */
    public static void keepAppMedia(Object link) {
        String url = link != null && showsOwnMedia(link) ? (String) fieldOrNull(link, "url") : null;
        appMedia.set(url != null ? Uri.parse(url).getLastPathSegment() : null);
    }

    private static boolean showsOwnMedia(Object link) {
        try {
            Class<?> linkClass = Class.forName("com.onelouder.baconreader.reddit.Link");
            Method gallery = LinkProcessor.class.getMethod("isValidRedditGallery", linkClass);
            return (boolean) gallery.invoke(null, link)
                    || (boolean) linkClass.getMethod("isRedditVideo").invoke(link);
        } catch (Exception ex) {
            Logger.printInfo(() -> "Could not check the media of the post", ex);
            return false;
        }
    }

    private static Object fieldOrNull(Object object, String name) {
        try {
            return field(object, name);
        } catch (ReflectiveOperationException ex) {
            Logger.printInfo(() -> "Could not read " + name, ex);
            return null;
        }
    }

    /**
     * Injected at the start of ClickableLinkSpan.onClick, the link next to a post's title, which starts only the
     * intents that open the browser. Opens the links {@link #intent} knows and returns true; returns false to let the
     * span go on, and the post's own gallery or video opens in the app's media viewer ({@link #keepAppMedia}).
     */
    public static boolean open(View view, Object link) {
        if (view == null || link == null) return false;
        Context context = view.getContext();
        String url = (String) fieldOrNull(link, "url");
        if (context == null || url == null) return false;
        keepAppMedia(link);
        if (appMedia.get() != null) return false;
        Intent next = intent(new LinkProcessor(context, url));
        if (next == null) return false;
        try {
            context.startActivity(next);
        } catch (Exception ex) {
            Logger.printInfo(() -> "Could not open the link " + url, ex);
        }
        return true;
    }

    /**
     * Injected at the start of LinkDispatcher.processing. Opens a share link where it leads, once its redirect is
     * known, or a video's post, once it is found, and returns true; returns false for every other intent.
     */
    public static boolean dispatch(Activity activity, Intent intent) {
        if (activity == null || intent == null) return false;
        Uri link = intent.getData();
        if (link == null) return false;
        if (intent.getBooleanExtra(EXTRA_SHARE_LINK, false)) {
            new Thread(() -> {
                String target = resolve(link);
                activity.runOnUiThread(() -> open(activity, link, target));
            }).start();
            return true;
        }
        if (intent.getBooleanExtra(EXTRA_VIDEO_LINK, false)) {
            new Thread(() -> {
                String post = videoPost(link);
                activity.runOnUiThread(() -> openVideo(activity, link, post));
            }).start();
            return true;
        }
        return false;
    }

    private static void openVideo(Activity activity, Uri link, String post) {
        try {
            Intent next = post != null
                    ? new Intent(activity, DetailActivity.class).putExtra(DetailActivity.EXTRA_LINKID, post)
                    : new Intent(activity, BrowserActivity.class).setData(link);
            activity.startActivity(next);
        } catch (Exception ex) {
            Logger.printInfo(() -> "Could not open the video " + link, ex);
        }
        activity.finish();
    }

    /**
     * The ID of the post a v.redd.it link was posted in, or null. Reddit's api/info finds the posts with that link:
     * the post itself, and the crossposts of it, which have no video of their own.
     */
    private static String videoPost(Uri link) {
        try {
            // Built from the link's host: the e2e tests move a constant "https://v.redd.it/" to their fake Reddit.
            String video = "https://" + link.getHost() + "/" + link.getPathSegments().get(0);
            Map<String, String> params = new HashMap<>();
            params.put("url", video);
            params.put("raw_json", "1");
            JSONArray children = new JSONObject(apiGet("api/info", params))
                    .getJSONObject("data").getJSONArray("children");
            String crosspostOf = null;
            for (int index = 0; index < children.length(); index++) {
                JSONObject post = children.getJSONObject(index).optJSONObject("data");
                if (post == null) continue;
                String parent = post.optString("crosspost_parent");
                if (parent.isEmpty() || post.isNull("crosspost_parent")) {
                    String id = post.optString("id");
                    return id.isEmpty() ? null : id;
                }
                if (crosspostOf == null) crosspostOf = parent.startsWith("t3_") ? parent.substring(3) : parent;
            }
            if (crosspostOf == null) Logger.printInfo(() -> "Reddit knows no post with the video " + link);
            return crosspostOf;
        } catch (Exception ex) {
            Logger.printInfo(() -> "Could not find the post of the video " + link, ex);
            return null;
        }
    }

    /**
     * A GET to Reddit's API through the app's own request code, RedditApi.httpRequest. With a login, it calls
     * oauth.reddit.com with the session's token, refreshed when needed, and the app's user agent. Reddit blocks other
     * requests from apps.
     */
    private static String apiGet(String path, Map<String, String> params) throws Exception {
        Object token = Class.forName("com.onelouder.baconreader.data.SessionManager")
                .getMethod("getToken").invoke(null);
        Class<?> requestMethod = Class.forName("com.onelouder.baconreader.RequestMethod");
        Class<?> api = Class.forName("com.onelouder.baconreader.reddit.RedditApi");
        Method httpRequest = api.getDeclaredMethod("httpRequest", Context.class, requestMethod, String.class,
                Map.class, Class.forName("com.onelouder.baconreader.data.SessionToken"));
        httpRequest.setAccessible(true);
        // Without a login the app calls www.reddit.com, which needs the .json suffix.
        if (token == null) path += ".json";
        @SuppressWarnings({"unchecked", "rawtypes"})
        Object get = Enum.valueOf((Class<Enum>) requestMethod, "GET");
        return (String) httpRequest.invoke(null, Utils.getContext(), get, path, params, token);
    }

    private static void open(Activity activity, Uri link, String target) {
        try {
            Intent next = target != null ? new LinkProcessor(activity, target).getIntent() : null;
            if (next == null) next = new Intent(activity, BrowserActivity.class).setData(link);
            activity.startActivity(next);
        } catch (Exception ex) {
            Logger.printInfo(() -> "Could not open the share link " + link, ex);
        }
        activity.finish();
    }

    /** The link the share link redirects to, on the share link's host, or null. */
    private static String resolve(Uri link) {
        HttpURLConnection connection = null;
        try {
            String query = link.getEncodedQuery();
            URL request = new URL(REDDIT + link.getEncodedPath() + (query != null ? "?" + query : ""));
            connection = (HttpURLConnection) request.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            int status = connection.getResponseCode();
            String location = connection.getHeaderField("Location");
            if (status < 300 || status > 399 || location == null || location.isEmpty()) {
                Logger.printInfo(() -> "The share link " + link + " answered " + status + " without a redirect");
                return null;
            }
            // Rebuilt on the link's own host, so the app recognizes it as a Reddit link.
            Uri target = Uri.parse(new URL(request, location).toString());
            return new Uri.Builder()
                    .scheme(link.getScheme())
                    .encodedAuthority(link.getEncodedAuthority())
                    .encodedPath(target.getEncodedPath())
                    .encodedQuery(target.getEncodedQuery())
                    .build()
                    .toString();
        } catch (Exception ex) {
            Logger.printInfo(() -> "Could not resolve the share link " + link, ex);
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
