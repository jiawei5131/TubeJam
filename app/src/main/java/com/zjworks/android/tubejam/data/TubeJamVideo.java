package com.zjworks.android.tubejam.data;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import com.zjworks.android.tubejam.R;

/**
 * Created by nemay5131 on 2017-11-27.
 *
 * Data model for parcelable YouTube Video
 */

public class TubeJamVideo implements Serializable {
    public static final String TUBEJAM_VIDEO_OBJECT_KEY = "TubeJam_Video_Object_Key";

    /** YouTube video ID. */
    private String	id;
    /** Video title. */
    private String	title;
    /** Channel ID. */
    private String channelId;
    /** Channel name. */
    private String	channelName;
    /** The total number of 'likes'. */
    private String	likeCount;
    /** The total number of 'dislikes'. */
    private String	dislikeCount;
    /** The percentage of people that thumbs-up this video (format:  "<percentage>%"). */
    private String	thumbsUpPercentageStr;
    private int		thumbsUpPercentage;
    /** Video duration string (e.g. "5:15"). */
    private String	duration;
    /** Total views count.  This can be <b>null</b> if the video does not allow the user to
     * like/dislike it. */
    private String	viewsCount;
    /** The date/time of when this video was published. */
    private DateTime publishDate;
    /** Thumbnail URL string. */
    private String	thumbnailUrl;
    /** The language of this video.  (This tends to be ISO 639-1).  */
    private String	language;
    /** The description of the video (set by the YouTuber/Owner). */
    private String	description;
    /** Set to true if the video is a current live stream. */
    private boolean isLiveStream;

    public TubeJamVideo(Video video) {
        this.id = video.getId();

        if (video.getSnippet() != null) {
            this.title       = video.getSnippet().getTitle();
            this.channelId   = video.getSnippet().getChannelId();
            this.channelName = video.getSnippet().getChannelTitle();
            publishDate = video.getSnippet().getPublishedAt();

            if (video.getSnippet().getThumbnails() != null) {
                Thumbnail thumbnail = video.getSnippet().getThumbnails().getHigh();
                if (thumbnail != null)
                    this.thumbnailUrl = thumbnail.getUrl();
            }

            this.language = video.getSnippet().getDefaultAudioLanguage() != null ? video.getSnippet().getDefaultAudioLanguage()
                    : (video.getSnippet().getDefaultLanguage() != null ? video.getSnippet().getDefaultLanguage() : null);

            this.description = video.getSnippet().getDescription();
        }

        if (video.getStatistics() != null) {
            BigInteger likeCount = video.getStatistics().getLikeCount(),
                    dislikeCount = video.getStatistics().getDislikeCount();

            setThumbsUpPercentage(likeCount, dislikeCount);

            this.viewsCount = String.format(Locale.getDefault(), "%,d views",
                    video.getStatistics().getViewCount());

            if (likeCount != null)
                this.likeCount = String.format(Locale.getDefault(), "%,d",
                        video.getStatistics().getLikeCount());

            if (dislikeCount != null)
                this.dislikeCount = String.format(Locale.getDefault(), "%,d",
                        video.getStatistics().getDislikeCount());
        }
    }


    /**
     * Sets the {@link #thumbsUpPercentageStr}, i.e. the percentage of people that thumbs-up this video
     * (format:  "<percentage>%").
     *
     * @param likedCountInt		Total number of "likes".
     * @param dislikedCountInt	Total number of "dislikes".
     */
    private void setThumbsUpPercentage(BigInteger likedCountInt, BigInteger dislikedCountInt) {
        String	fullPercentageStr = null;
        int		percentageInt = -1;

        // some videos do not allow users to like/dislike them:  hence likedCountInt / dislikedCountInt
        // might be null in those cases
        if (likedCountInt != null  &&  dislikedCountInt != null) {
            BigDecimal likedCount   = new BigDecimal(likedCountInt),
                    dislikedCount   = new BigDecimal(dislikedCountInt),
                    totalVoteCount  = likedCount.add(dislikedCount),    // liked and disliked counts
                    likedPercentage;

            if (totalVoteCount.compareTo(BigDecimal.ZERO) != 0) {
                likedPercentage = (likedCount.divide(totalVoteCount, MathContext.DECIMAL128)).multiply(new BigDecimal(100));

                // round the liked percentage to 0 decimal places and convert it to string
                String percentageStr = likedPercentage.setScale(0, RoundingMode.HALF_UP).toString();
                fullPercentageStr = percentageStr + "%";
                percentageInt = Integer.parseInt(percentageStr);
            }
        }

        this.thumbsUpPercentageStr = fullPercentageStr;
        this.thumbsUpPercentage = percentageInt;
    }

    /**
     * Gets the {@link #publishDate} as a pretty string.
     */
    public String getPublishDatePretty() {
        return (publishDate != null)
                ? publishDate.toString()
                : "???";
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    /**
     * @return True if the video allows the users to like/dislike it.
     */
    public boolean isThumbsUpPercentageSet() {
        return (thumbsUpPercentageStr != null);
    }

    /**
     * @return The thumbs up percentage (as an integer).  Can return <b>-1</b> if the video does not
     * allow the users to like/dislike it.  Refer to {@link #isThumbsUpPercentageSet}.
     */
    public int getThumbsUpPercentage() {
        return thumbsUpPercentage;
    }

    /**
     * @return The thumbs up percentage (format:  "«percentage»%").  Can return <b>null</b> if the
     * video does not allow the users to like/dislike it.  Refer to {@link #isThumbsUpPercentageSet}.
     */
    public String getThumbsUpPercentageStr() {
        return thumbsUpPercentageStr;
    }

    /**
     * @return The total number of 'likes'.  Can return <b>null</b> if the video does not allow the
     * users to like/dislike it.  Refer to {@link #isThumbsUpPercentageSet}.
     */
    public String getLikeCount() {
        return likeCount;
    }

    /**
     * @return The total number of 'dislikes'.  Can return <b>null</b> if the video does not allow the
     * users to like/dislike it.  Refer to {@link #isThumbsUpPercentageSet}.
     */
    public String getDislikeCount() {
        return dislikeCount;
    }

    public String getDuration() {
        return duration;
    }

    public String getViewsCount() {
        return viewsCount;
    }

    public DateTime getPublishDate() {
        return publishDate;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getVideoUrl() {
        return String.format("https://youtu.be/%s", id);
    }

    public String getLanguage() {
        return language;
    }

    public String getDescription() {
        return description;
    }

    public boolean isLiveStream() {
        return isLiveStream;
    }


    /**
     * Bookmark this video
     * @param context
     */
    public void bookmarkVideo(Context context) {
    }

    public void unbookmarkVideo(Context context) {
    }


    /**
     * Like this video
     * @param context
     */
    public void likeVideo(Context context) {

    }

    public void unlikeVIdeo(Context context) {

    }


    /**
     * Dislike this video
     * @param context
     */
    public void dislikeVideo(Context context) {

    }

    public void undislikeVideo(Context context) {

    }

    /**
     * Share this video's URL as plain text
     * @param context
     */
    public void shareVideo(Context context) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, getVideoUrl());
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_via)));
    }


    /**
     * Copy the URL of this video to the clipboard
     * @param context
     */
    public void copyUrl(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Video URL", getVideoUrl());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, R.string.url_copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }
}
