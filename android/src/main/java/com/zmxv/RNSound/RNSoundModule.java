package com.zmxv.RNSound;


import android.net.Uri;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import net.protyposis.android.mediaplayer.FileSource;
import net.protyposis.android.mediaplayer.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RNSoundModule extends ReactContextBaseJavaModule {
    Map<Integer, MediaPlayer> playerPool = new HashMap<>();
    ReactApplicationContext context;
    final static Object NULL = null;

    public RNSoundModule(ReactApplicationContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public String getName() {
        return "RNSound";
    }

    @ReactMethod
    public void prepare(final String fileName, final Integer key, final Callback callback) {
        MediaPlayer player = null;
        try {
            player = createMediaPlayer(fileName);
        } catch (IOException e1) {
            WritableMap e = Arguments.createMap();
            e.putInt("code", -1);
            e.putString("message", e1.getMessage());
            callback.invoke(e);
            return;
        }
        if (player == null) {
            WritableMap e = Arguments.createMap();
            e.putInt("code", -1);
            e.putString("message", "resource not found");
            callback.invoke(e);
            return;
        }
        try {
            player.prepare();
        } catch (Exception e) {
        }
        this.playerPool.put(key, player);
        WritableMap props = Arguments.createMap();
        props.putDouble("duration", player.getDuration() * .001);
        callback.invoke(NULL, props);
    }

    protected MediaPlayer createMediaPlayer(final String fileName) throws IOException {

        File file = new File(fileName);
        if (file.exists()) {
            FileSource fileSource = new FileSource(file);
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(fileSource);
            return player;
        }
        return null;
    }

    @ReactMethod
    public void play(final Integer key, final Callback callback) {
        MediaPlayer player = this.playerPool.get(key);
        if (player == null) {
            callback.invoke(false);
            return;
        }
        if (player.isPlaying()) {
            return;
        }
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!mp.isLooping()) {
                    callback.invoke(true);
                }
            }
        });
        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                callback.invoke(false);
                return true;
            }
        });
        player.start();
    }

    @ReactMethod
    public void pause(final Integer key) {
        MediaPlayer player = this.playerPool.get(key);
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    @ReactMethod
    public void stop(final Integer key) {
        MediaPlayer player = this.playerPool.get(key);
        if (player != null && player.isPlaying()) {
            player.pause();
            player.seekTo(0);
        }
    }

    @ReactMethod
    public void release(final Integer key) {
        MediaPlayer player = this.playerPool.get(key);
        if (player != null) {
            player.release();
            this.playerPool.remove(key);
        }
    }

    @ReactMethod
    public void setVolume(final Integer key, final Float left, final Float right) {
        MediaPlayer player = this.playerPool.get(key);
        if (player != null) {
            player.setVolume(left, right);
        }
    }


    @ReactMethod
    public void setSpeed(final Integer key, final Float speed) {
        MediaPlayer player = this.playerPool.get(key);
        if (player != null) {
            player.setPlaybackSpeed(speed);
        }
    }



    @ReactMethod
    public void setLooping(final Integer key, final Boolean looping) {
        MediaPlayer player = this.playerPool.get(key);
        if (player != null) {
            player.setLooping(looping);
        }
    }

    @ReactMethod
    public void setCurrentTime(final Integer key, final Float sec) {
        MediaPlayer player = this.playerPool.get(key);
        if (player != null) {
            player.seekTo((int) Math.round(sec * 1000));
        }
    }

    @ReactMethod
    public void getCurrentTime(final Integer key, final Callback callback) {
        MediaPlayer player = this.playerPool.get(key);
        if (player == null) {
            callback.invoke(-1, false);
            return;
        }
        callback.invoke(player.getCurrentPosition() * .001, player.isPlaying());
    }

    @ReactMethod
    public void enable(final Boolean enabled) {
        // no op
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("IsAndroid", true);
        return constants;
    }
}
