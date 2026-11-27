// SPDX-FileCopyrightText: 2026 JoanComasFdz
// SPDX-License-Identifier: GPL-3.0-only

package io.github.joancomasfdz.extension.baconreader.postvideo;

import android.content.Context;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;

import io.github.joancomasfdz.extension.baconreader.VideoFullScreen;
import io.github.joancomasfdz.extension.baconreader.errors.ErrorDialog;
import io.github.joancomasfdz.extension.baconreader.settings.Settings;

/**
 * The video of a post's detail view gets the full screen button of a comment's video, over its top right corner while
 * its controls show. PostMediaHandler is also used by the full-screen photo viewer, already full screen, and the
 * browser, which keep their player as it is.
 */
@SuppressWarnings("unused")
public final class PostVideoFullScreenPatch {
    /** The listener DetailLinkFragment gives its PostMediaHandler. PhotoSlide and BrowserActivity give their own. */
    private static final String DETAIL_LISTENER = "com.onelouder.baconreader.adapters.DetailLinkFragment$6";

    /** The buttons over each post's player view, and the player it shows. */
    private static final Map<View, View> ROWS = new WeakHashMap<>();
    private static final Map<View, Object> PLAYERS = new WeakHashMap<>();

    private PostVideoFullScreenPatch() {
    }

    /**
     * Injected at the start of PostMediaHandler.onCreateVideo(SimpleExoPlayer), which binds the player to the post's
     * player view: adds the full screen button over the post's video.
     */
    public static void addButton(Object handler, Object player) {
        try {
            if (handler == null || player == null) return;
            if (!Settings.POST_VIDEO_FULL_SCREEN_BUTTON.get()) return;
            Object listener = get(handler, "listener");
            if (listener == null || !DETAIL_LISTENER.equals(listener.getClass().getName())) return;
            final View videoView = (View) get(handler, "videoView");
            if (videoView == null) return;
            // The page of the media's ViewFlipper that holds the player and its mute button.
            ViewParent parent = videoView.getParent();
            if (!(parent instanceof FrameLayout)) return;
            FrameLayout page = (FrameLayout) parent;

            View old = ROWS.remove(videoView);
            if (old != null) page.removeView(old);
            PLAYERS.put(videoView, player);
            Context context = videoView.getContext();
            ImageView full = VideoFullScreen.fullScreenButton(videoView, player,
                    () -> isWide(videoView), p -> PLAYERS.get(videoView) == p);
            LinearLayout row = VideoFullScreen.buttonRow(context, full);
            page.addView(row, VideoFullScreen.buttonRowParams(context));
            ROWS.put(videoView, row);
            VideoFullScreen.followControls(videoView, row);
        } catch (Exception ex) {
            ErrorDialog.printException(() -> "Could not add the full screen button", ex);
        }
    }

    /**
     * Injected at the start of PostMediaHandler.destroyVideo(boolean), before it releases the post's player: closes
     * its full screen.
     */
    public static void release(Object handler) {
        try {
            if (handler == null) return;
            View videoView = (View) get(handler, "videoView");
            if (videoView == null) return;
            VideoFullScreen.closeShowing(null, videoView);
            PLAYERS.remove(videoView);
        } catch (Exception ex) {
            ErrorDialog.printException(() -> "Could not close the full screen", ex);
        }
    }

    /**
     * Whether the video is wider than high: the player fits its surface to the video's shape.
     */
    private static boolean isWide(View videoView) {
        try {
            View surface = (View) videoView.getClass().getMethod("getVideoSurfaceView").invoke(videoView);
            return surface != null && surface.getWidth() > surface.getHeight();
        } catch (Exception ex) {
            return false;
        }
    }

    private static Object get(Object target, String name) throws Exception {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name);
    }
}
