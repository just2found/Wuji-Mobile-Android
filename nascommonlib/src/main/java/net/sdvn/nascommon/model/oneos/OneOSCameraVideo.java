package net.sdvn.nascommon.model.oneos;

import androidx.annotation.Keep;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/1/8.
 */
@Keep
public class OneOSCameraVideo implements Serializable {
    private static final long serialVersionUID = -476325903555490994L;
    private String time;
    private String videoPath;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

//    @Override
//    public String toString() {
//        return "OneOSCameraVideo:{time:\"" + time +  "\", videoPath:\"" + videoPath + "\"}";
//    }
}
