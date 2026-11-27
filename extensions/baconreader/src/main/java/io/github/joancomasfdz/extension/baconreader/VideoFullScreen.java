// SPDX-FileCopyrightText: 2026 JoanComasFdz
// SPDX-License-Identifier: GPL-3.0-only

package io.github.joancomasfdz.extension.baconreader;

import static app.morphe.extension.shared.StringRef.str;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;

import app.morphe.extension.shared.Logger;
import app.morphe.extension.shared.ResourceType;
import app.morphe.extension.shared.ResourceUtils;
import io.github.joancomasfdz.extension.baconreader.errors.ErrorDialog;

/**
 * A video shown over the whole screen, from the player of a comment's video or of a post's: the round buttons over a
 * playing video, and the full screen they open. One shows at a time. The players are ExoPlayer's, bundled in
 * BaconReader, reached by reflection.
 */
public final class VideoFullScreen {
    private static final int BUTTON_SIZE_DP = 36;
    private static final long ORIENTATION_RELEASE_DELAY_MS = 1000L;
    /**
     * Player.STATE_ENDED: the video played to its end.
     */
    private static final int STATE_ENDED = 4;

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /**
     * The full screen showing a video, if any, and the player and view it came from.
     */
    private static Dialog fullScreen;
    private static Object shownPlayer;
    private static View shownInline;

    /**
     * Whether the video still plays in the view it came from, when the full screen closes.
     */
    public interface Owner {
        boolean owns(Object player);
    }

    /**
     * Whether the video is wider than high, asked when the full screen opens.
     */
    public interface Wide {
        boolean isWide();
    }

    private VideoFullScreen() {
    }

    /**
     * The full screen button over a playing video.
     */
    public static ImageView fullScreenButton(View inline, Object player, Wide wide, Owner owner) {
        ImageView full = button(inline.getContext());
        full.setImageDrawable(new ButtonDrawable(ButtonDrawable.FULL_SCREEN));
        full.setContentDescription(str("revanced_embed_comment_images_video_full_screen"));
        full.setOnClickListener(v -> open(inline, player, wide.isWide(), owner));
        return full;
    }

    /**
     * Shows the video of the player over the whole screen, turned to landscape when it's wide. Back or the button
     * closes it, and the video plays on in the inline view while its owner still has it.
     */
    private static void open(View inline, Object player, boolean wide, Owner owner) {
        try {
            final Activity activity = activityOf(inline.getContext());
            if (player == null || activity == null) return;
            close();

            FrameLayout root = new FrameLayout(activity);
            root.setBackgroundColor(Color.BLACK);
            final View playerView = newPlayerView(activity);
            playerView.setId(resourceId("baconreader_patches_video_full_screen"));
            root.addView(playerView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ImageView exit = button(activity);
            exit.setImageDrawable(new ButtonDrawable(ButtonDrawable.EXIT_FULL_SCREEN));
            exit.setContentDescription(str("revanced_embed_comment_images_video_exit_full_screen"));
            LinearLayout buttons = buttonRow(activity, exit);
            root.addView(buttons, buttonRowParams(activity));

            final Dialog dialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(root);
            exit.setOnClickListener(v -> dialog.dismiss());

            final int orientation = activity.getRequestedOrientation();
            final int before = activity.getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE
                    ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            if (wide) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
            dialog.setOnDismissListener(d -> {
                if (fullScreen == dialog) {
                    fullScreen = null;
                    shownPlayer = null;
                    shownInline = null;
                }
                if (wide) {
                    // Given back right away, the app's own orientation follows the sensor, which keeps landscape
                    // while the phone lies flat. The screen turns back first.
                    activity.setRequestedOrientation(before);
                    MAIN.postDelayed(() -> {
                        if (fullScreen == null) activity.setRequestedOrientation(orientation);
                    }, ORIENTATION_RELEASE_DELAY_MS);
                }
                if (owner.owns(player)) {
                    try {
                        switchTargetView(player, playerView, inline);
                    } catch (Exception ex) {
                        ErrorDialog.printException(() -> "Could not show the video in its place again", ex);
                    }
                }
            });

            // A new view shows no frame of a video that has ended: it starts over, paused. The player still wants to
            // play once it has ended, so it's paused first.
            if ((Integer) player.getClass().getMethod("getPlaybackState").invoke(player) == STATE_ENDED) {
                player.getClass().getMethod("setPlayWhenReady", boolean.class).invoke(player, false);
                player.getClass().getMethod("seekTo", int.class, long.class).invoke(player, 0, 0L);
            }
            switchTargetView(player, inline, playerView);
            followControls(playerView, buttons);
            dialog.show();
            // The theme hides the status bar. Hiding the navigation bar too would show Android's "Viewing full
            // screen" message over the video, the first time.
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
            fullScreen = dialog;
            shownPlayer = player;
            shownInline = inline;
        } catch (Exception ex) {
            ErrorDialog.printException(() -> "Could not show the video in full screen", ex);
        }
    }

    /**
     * Closes the full screen when it shows the player, or the video of the inline view.
     */
    public static void closeShowing(Object player, View inline) {
        if (fullScreen == null) return;
        if ((player != null && player == shownPlayer) || (inline != null && inline == shownInline)) close();
    }

    private static void close() {
        Dialog dialog = fullScreen;
        if (dialog == null) return;
        fullScreen = null;
        shownPlayer = null;
        shownInline = null;
        try {
            dialog.dismiss();
        } catch (Exception ex) {
            Logger.printInfo(() -> "Could not close the full screen", ex);
        }
    }

    private static View newPlayerView(Context context) throws Exception {
        Constructor<?> playerViewConstructor = context.getClassLoader()
                .loadClass("com.google.android.exoplayer2.ui.PlayerView").getConstructor(Context.class);
        View playerView = (View) playerViewConstructor.newInstance(context);
        playerView.getClass().getMethod("setShowBuffering", int.class).invoke(playerView, 1); // When playing.
        playerView.getClass().getMethod("setShutterBackgroundColor", int.class).invoke(playerView, Color.TRANSPARENT);
        return playerView;
    }

    /**
     * PlayerView.switchTargetView: moves the player's video from one view to the other.
     */
    private static void switchTargetView(Object player, View from, View to) throws Exception {
        ClassLoader loader = from.getContext().getClassLoader();
        Class<?> playerClass = loader.loadClass("com.google.android.exoplayer2.Player");
        Class<?> playerViewClass = loader.loadClass("com.google.android.exoplayer2.ui.PlayerView");
        playerViewClass.getMethod("switchTargetView", playerClass, playerViewClass, playerViewClass)
                .invoke(null, player, from, to);
    }

    private static Activity activityOf(Context context) {
        while (context != null) {
            if (context instanceof Activity) return (Activity) context;
            context = context instanceof ContextWrapper ? ((ContextWrapper) context).getBaseContext() : null;
        }
        return null;
    }

    /**
     * The id a patch adds to the app's resources, for the e2e tests to find a view by.
     */
    private static int resourceId(String name) {
        int id = ResourceUtils.getIdentifier(ResourceType.ID, name);
        return id == 0 ? View.NO_ID : id;
    }

    /**
     * Shows the buttons while the player's controls show, and hides them with the controls.
     */
    public static void followControls(View playerView, final View buttons) {
        try {
            ClassLoader loader = playerView.getContext().getClassLoader();
            boolean visible = (Boolean) playerView.getClass().getMethod("isControllerVisible").invoke(playerView);
            buttons.setVisibility(visible ? View.VISIBLE : View.GONE);
            Class<?> listenerClass =
                    loader.loadClass("com.google.android.exoplayer2.ui.PlayerControlView$VisibilityListener");
            Object listener = Proxy.newProxyInstance(loader, new Class<?>[]{listenerClass}, (proxy, method, args) -> {
                if ("onVisibilityChange".equals(method.getName())) {
                    buttons.setVisibility((Integer) args[0]);
                    return null;
                }
                if ("equals".equals(method.getName())) return proxy == args[0];
                if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                if ("toString".equals(method.getName())) return "VisibilityListener";
                return null;
            });
            playerView.getClass().getMethod("setControllerVisibilityListener", listenerClass)
                    .invoke(playerView, listener);
        } catch (Exception ex) {
            buttons.setVisibility(View.VISIBLE);
            Logger.printInfo(() -> "Could not follow the video's controls", ex);
        }
    }

    public static ImageView button(Context context) {
        ImageView button = new ImageView(context);
        button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return button;
    }

    public static LinearLayout buttonRow(Context context, ImageView... buttons) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        int size = dp(context, BUTTON_SIZE_DP);
        for (int i = 0; i < buttons.length; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            if (i > 0) params.leftMargin = dp(context, 8);
            row.addView(buttons[i], params);
        }
        return row;
    }

    /**
     * The buttons' place over a video: its top right corner.
     */
    public static FrameLayout.LayoutParams buttonRowParams(Context context) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.END);
        int margin = dp(context, 6);
        params.setMargins(margin, margin, margin, margin);
        return params;
    }

    private static int dp(Context context, int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics()));
    }

    /**
     * A button's icon over a video: a white sign on a dark circle, like the play button. Shrink is "<", enlarge ">",
     * full screen four outward corners and exit full screen four inward ones.
     */
    public static final class ButtonDrawable extends Drawable {
        public static final int SHRINK = 0;
        public static final int ENLARGE = 1;
        public static final int FULL_SCREEN = 2;
        public static final int EXIT_FULL_SCREEN = 3;

        private final Paint circle = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint sign = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private int kind;

        public ButtonDrawable(int kind) {
            this.kind = kind;
            circle.setColor(0x99000000);
            sign.setColor(0xFFFFFFFF);
            sign.setStyle(Paint.Style.STROKE);
            sign.setStrokeCap(Paint.Cap.ROUND);
            sign.setStrokeJoin(Paint.Join.ROUND);
        }

        public void setKind(int kind) {
            if (this.kind == kind) return;
            this.kind = kind;
            invalidateSelf();
        }

        @Override
        public void draw(Canvas canvas) {
            Rect b = getBounds();
            float cx = b.exactCenterX();
            float cy = b.exactCenterY();
            float r = Math.min(b.width(), b.height()) / 2f;
            canvas.drawCircle(cx, cy, r, circle);
            sign.setStrokeWidth(r * 0.14f);
            float t = r * 0.4f;
            path.reset();
            if (kind == SHRINK) {
                chevron(cx + t * 0.4f, cy, -t);
            } else if (kind == ENLARGE) {
                chevron(cx - t * 0.4f, cy, t);
            } else {
                float c = t * 0.5f;
                boolean outward = kind == FULL_SCREEN;
                for (int dx = -1; dx <= 1; dx += 2) {
                    for (int dy = -1; dy <= 1; dy += 2) {
                        float x = cx + dx * t;
                        float y = cy + dy * t;
                        // Outward corners point away from the center; inward ones toward it.
                        float ix = outward ? -dx * c : dx * c;
                        float iy = outward ? -dy * c : dy * c;
                        float px = outward ? x : x - dx * c;
                        float py = outward ? y : y - dy * c;
                        path.moveTo(px + ix, py);
                        path.lineTo(px, py);
                        path.lineTo(px, py + iy);
                    }
                }
            }
            canvas.drawPath(path, sign);
        }

        /**
         * A chevron with its tip at (x + depth, y).
         */
        private void chevron(float x, float y, float depth) {
            path.moveTo(x, y - Math.abs(depth));
            path.lineTo(x + depth, y);
            path.lineTo(x, y + Math.abs(depth));
        }

        @Override
        public void setAlpha(int alpha) {
            circle.setAlpha(alpha);
            sign.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            sign.setColorFilter(colorFilter);
        }

        @Override
        @SuppressWarnings("deprecation")
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
