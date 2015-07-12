package com.volokh.danylo.videolist.player;

public enum PlayerMessageState {
    IDLE,
    INITIALIZED,
    PREPARING,
    PREPARED,
    STARTING,
    STARTED,
    PAUSING,
    PAUSED,
    STOPPING,
    STOPPED,
    RELEASING,
    RELEASED,
    RESETTING,
    RESET,
    CLEARING_PLAYER_INSTANCE,
    PLAYER_INSTANCE_CLEARED,
    CREATING_PLAYER_INSTANCE,
    PLAYER_INSTANCE_CREATED,
    SETTING_VIDEO_SOURCE,
    VIDEO_SOURCE_SET,
    PLAYBACK_COMPLETED,
    END,
    ERROR,
}
