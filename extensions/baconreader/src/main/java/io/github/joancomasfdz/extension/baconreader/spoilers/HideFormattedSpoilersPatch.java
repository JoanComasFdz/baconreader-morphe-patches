// SPDX-FileCopyrightText: 2026 JoanComasFdz
// SPDX-License-Identifier: GPL-3.0-only

package io.github.joancomasfdz.extension.baconreader.spoilers;

import static app.morphe.extension.shared.StringRef.str;

import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.morphe.extension.shared.Logger;

/**
 * Hide formatted spoilers.
 *
 * A hidden spoiler is only drawn as a box: accessibility would still read its text. While one is
 * hidden, the view's description is its text with each hidden spoiler read as "spoiler".
 */
@SuppressWarnings("unused")
public final class HideFormattedSpoilersPatch {
    private static final String SPOILER = "com.onelouder.baconreader.parser.SpoilerClickableSpan";

    private HideFormattedSpoilersPatch() {
    }

    /**
     * Injected right after the app sets the text of a comment (CommentHolder.updateView) or a post
     * (DetailLinkFragment.updateSelftext), and after a tap shows or hides a spoiler
     * (SpoilerClickableSpan.onClick).
     */
    public static void describe(View view) {
        if (!(view instanceof TextView)) return;
        TextView textView = (TextView) view;
        try {
            CharSequence current = textView.getText();
            if (!(current instanceof Spanned)) {
                textView.setContentDescription(null);
                return;
            }
            final Spanned text = (Spanned) current;
            List<ClickableSpan> hidden = new ArrayList<>();
            for (ClickableSpan span : text.getSpans(0, text.length(), ClickableSpan.class)) {
                if (span.getClass().getName().equals(SPOILER) && isHidden(span)) hidden.add(span);
            }
            if (hidden.isEmpty()) {
                textView.setContentDescription(null);
                return;
            }
            Collections.sort(hidden, new Comparator<ClickableSpan>() {
                @Override
                public int compare(ClickableSpan a, ClickableSpan b) {
                    return Integer.compare(text.getSpanStart(a), text.getSpanStart(b));
                }
            });
            StringBuilder described = new StringBuilder();
            int at = 0;
            for (ClickableSpan spoiler : hidden) {
                int start = text.getSpanStart(spoiler);
                if (start < at) continue;
                described.append(text, at, start).append(str("revanced_spoiler_hidden"));
                at = text.getSpanEnd(spoiler);
            }
            described.append(text, at, text.length());
            textView.setContentDescription(described.toString());
        } catch (Exception ex) {
            Logger.printInfo(() -> "Could not describe the hidden spoilers", ex);
        }
    }

    /**
     * The span's "hideSpoiler" is false while the spoiler is hidden, and flips with each tap. It is
     * private in BaconReader.
     */
    private static boolean isHidden(Object span) throws ReflectiveOperationException {
        Field field = span.getClass().getDeclaredField("hideSpoiler");
        field.setAccessible(true);
        return !field.getBoolean(span);
    }
}
