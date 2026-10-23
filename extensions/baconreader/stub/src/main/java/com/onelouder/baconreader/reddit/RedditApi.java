package com.onelouder.baconreader.reddit;

import android.content.Context;

import com.onelouder.baconreader.connectivity.Tasks;

import java.util.List;

public class RedditApi {
    /** GET api/link_flair?r=subreddit: the post flairs users can choose in the subreddit. */
    public static void getLinkFlairs(Context context, String subreddit, Tasks.OnCompleteListener<List<LinkFlair>> listener) {
    }
}
