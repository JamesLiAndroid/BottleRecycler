package com.incomrecycle.prms.rvm.gui.entity;

import android.media.MediaPlayer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MediaEntity implements Serializable {
    private static int index = 0;
    private static List<HashMap<String, String>> listVideo = null;
    private static MediaPlayer mplayer = null;
    private static int position = 0;
    private static final long serialVersionUID = -4554710332752861155L;

    public static int getIndex() {
        return index;
    }

    public static void setIndex(int index) {
        index = index;
    }

    public static int getPosition() {
        return position;
    }

    public static void setPosition(int position) {
        position = position;
    }

    public static List<HashMap<String, String>> getListVideo() {
        if (listVideo == null) {
            listVideo = new ArrayList();
        }
        return listVideo;
    }

    public static void setListVideo(List<HashMap<String, String>> listVideo) {
        listVideo = listVideo;
    }

    public static MediaPlayer getMplayer() {
        if (mplayer == null) {
            mplayer = new MediaPlayer();
        }
        return mplayer;
    }

    public static void setMplayer(MediaPlayer mplayer) {
        mplayer = mplayer;
    }
}
