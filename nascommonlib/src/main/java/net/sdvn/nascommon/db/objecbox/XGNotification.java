package net.sdvn.nascommon.db.objecbox;

import androidx.annotation.Keep;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
@Keep
@Entity
public class XGNotification {
    /* db.execSQL("CREATE TABLE notification (id integer primary key autoincrement,msg_id varchar(64)," +
             "title varchar(128),activity varchar(256),notificationActionType varchar(512)" +
             ",content text,update_time varchar(16))");*/
    @Id
    private Long id;
    private long msg_id;
    private String title;
    private String content;
    private String activity;
    private int notificationActionType;
    private String update_time;

    public XGNotification(Long id, long msg_id, String title, String content, String activity,
                          int notificationActionType, String update_time) {
        this.id = id;
        this.msg_id = msg_id;
        this.title = title;
        this.content = content;
        this.activity = activity;
        this.notificationActionType = notificationActionType;
        this.update_time = update_time;
    }

    public XGNotification() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getMsg_id() {
        return this.msg_id;
    }

    public void setMsg_id(long msg_id) {
        this.msg_id = msg_id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getActivity() {
        return this.activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public int getNotificationActionType() {
        return this.notificationActionType;
    }

    public void setNotificationActionType(int notificationActionType) {
        this.notificationActionType = notificationActionType;
    }

    public String getUpdate_time() {
        return this.update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }


}