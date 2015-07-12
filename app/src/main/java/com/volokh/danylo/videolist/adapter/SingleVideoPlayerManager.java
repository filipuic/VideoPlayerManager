package com.volokh.danylo.videolist.adapter;

import com.volokh.danylo.videolist.Config;
import com.volokh.danylo.videolist.adapter.interfaces.SingleVideoPlayerManagerCallback;
import com.volokh.danylo.videolist.adapter.interfaces.VideoPlayerManager;
import com.volokh.danylo.videolist.player.ClearPlayerInstance;
import com.volokh.danylo.videolist.player.CreateNewPlayerInstance;
import com.volokh.danylo.videolist.player.SetDataSourceMessage;
import com.volokh.danylo.videolist.player.PlayerHandlerThread;
import com.volokh.danylo.videolist.player.PlayerMessageState;
import com.volokh.danylo.videolist.player.Prepare;
import com.volokh.danylo.videolist.player.Release;
import com.volokh.danylo.videolist.player.Reset;
import com.volokh.danylo.videolist.player.Start;
import com.volokh.danylo.videolist.player.Stop;
import com.volokh.danylo.videolist.ui.VideoPlayer;
import com.volokh.danylo.videolist.utils.Logger;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class SingleVideoPlayerManager implements VideoPlayerManager, SingleVideoPlayerManagerCallback {

    private static final String TAG = SingleVideoPlayerManager.class.getSimpleName();
    private static final boolean SHOW_LOGS = Config.SHOW_LOGS;

    private final PlayerHandlerThread mPlayerHandler = new PlayerHandlerThread(TAG);
    private final AtomicReference<VideoPlayer> mCurrentPlayer = new AtomicReference<>();
    private final AtomicReference<PlayerMessageState> mCurrentPlayerState = new AtomicReference<>();

    @Override
    public void playNewVideo(VideoPlayer videoPlayer, String videoUrl) {
        if(SHOW_LOGS) Logger.v(TAG, ">> playNewVideo, videoPlayer " + videoPlayer + ", videoUrl " + videoUrl);

        synchronized (mCurrentPlayerState){

            mPlayerHandler.pauseQueueProcessing(TAG);
            if(SHOW_LOGS) Logger.v(TAG, "playNewVideo, videoPlayer " + videoPlayer + ", videoUrl " + videoUrl + ", mCurrentPlayerState " + mCurrentPlayerState);

            mPlayerHandler.clearAllPendingMessages(TAG);

            switch (mCurrentPlayerState.get()){
                case IDLE:
                    break;
                case INITIALIZED:
                case PREPARING:
                case PREPARED:
                case STARTING:
                case STARTED:
                case PAUSING:
                case PAUSED:
                case PLAYBACK_COMPLETED:

                    mPlayerHandler.addMessages(Arrays.asList(
                            new Stop(videoPlayer, videoUrl),
                            new Reset(videoPlayer, videoUrl),
                            new Release(videoPlayer, videoUrl),
                            new ClearPlayerInstance(videoPlayer, videoUrl),
                            new CreateNewPlayerInstance(videoPlayer, videoUrl),
                            new SetDataSourceMessage(videoPlayer, videoUrl),
                            new Prepare(videoPlayer, videoUrl),
                            new Start(videoPlayer, videoUrl)
                            ));
                    break;
                case STOPPING:
                case STOPPED:
                    mPlayerHandler.addMessage(new Stop(videoPlayer, videoUrl));
                    //FALL-THROUGH

                case RESETTING:
                case RESET:
                    mPlayerHandler.addMessage(new Reset(videoPlayer, videoUrl));
                    //FALL-THROUGH

                case RELEASING:
                case RELEASED:
                    mPlayerHandler.addMessage(new ClearPlayerInstance(videoPlayer, videoUrl));
                    //FALL-THROUGH

                case CLEARING_PLAYER_INSTANCE:
                case PLAYER_INSTANCE_CLEARED:
                    mPlayerHandler.addMessage(new CreateNewPlayerInstance(videoPlayer, videoUrl));
                    //FALL-THROUGH

                case CREATING_PLAYER_INSTANCE:
                case PLAYER_INSTANCE_CREATED:
                    mPlayerHandler.addMessages(Arrays.asList(
                            new SetDataSourceMessage(videoPlayer, videoUrl),
                            new Prepare(videoPlayer, videoUrl),
                            new Start(videoPlayer, videoUrl)
                    ));
                    break;

                case SETTING_VIDEO_SOURCE:
                    throw new RuntimeException("unhandled " + mCurrentPlayerState);

                case VIDEO_SOURCE_SET:
                    throw new RuntimeException("unhandled " + mCurrentPlayerState);

                case END:
                    throw new RuntimeException("unhandled " + mCurrentPlayerState);

                case ERROR:
                    throw new RuntimeException("unhandled " + mCurrentPlayerState);

            }
            mPlayerHandler.resumeQueueProcessing(TAG);
        }

        if(SHOW_LOGS) Logger.v(TAG, "<< playNewVideo, videoPlayer " + videoPlayer + ", videoUrl " + videoUrl);
    }

    private boolean isStopping() {
        boolean result = false;
        switch (mCurrentPlayerState.get()){
            case IDLE:
                break;
            case INITIALIZED:
            case PREPARING:
            case PREPARED:
            case STARTING:
            case STARTED:
            case PAUSING:
            case PAUSED:
            case STOPPED:
            case PLAYBACK_COMPLETED:
            case END:
            case ERROR:
                result = false;
                break;
            case STOPPING:
                result = true;
                break;
        }
        return result;
    }

    private boolean isInPlayback() {
        boolean result = false;
        switch (mCurrentPlayerState.get()){
            case IDLE:
                break;
            case INITIALIZED:
            case PREPARING:
            case PREPARED:
            case STARTING:
            case STARTED:
                result = true;
                break;
            case PAUSING:
            case PAUSED:
            case STOPPING:
            case STOPPED:
            case PLAYBACK_COMPLETED:
            case END:
            case ERROR:
                result = false;
                break;
        }
        return result;
    }

    @Override
    public void setCurrentVideoPlayer(VideoPlayer currentVideoPlayer) {
        if(SHOW_LOGS) Logger.v(TAG, ">> setCurrentVideoPlayer, currentVideoPlayer " + currentVideoPlayer);

        synchronized (mCurrentPlayer){
            mCurrentPlayer.set(currentVideoPlayer);
        }
        if(SHOW_LOGS) Logger.v(TAG, "<< setCurrentVideoPlayer");
    }

    @Override
    public void setCurrentVideoPlayerState(VideoPlayer currentVideoPlayer, PlayerMessageState playerMessageState) {
        if(SHOW_LOGS) Logger.v(TAG, ">> setCurrentVideoPlayerState, currentVideoPlayer " + currentVideoPlayer + ", playerMessageState " + playerMessageState);

        synchronized (mCurrentPlayerState){
            mCurrentPlayerState.set(playerMessageState);
        }
        if(SHOW_LOGS) Logger.v(TAG, "<< setCurrentVideoPlayerState");
    }
}
