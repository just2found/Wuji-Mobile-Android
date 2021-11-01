package net.sdvn.nascommon.model.oneos.api.file;


import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.FileManageAction;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.log.Logger;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OneSpace OS File Manage API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/21.
 */
public class OneOSFileManageAPI extends BaseAPI {
    private static final String TAG = OneOSFileManageAPI.class.getSimpleName();

    private FileManageAction action;
    private OnFileManageListener listener;

    public OneOSFileManageAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.FILE_API, "manage");
    }


    private void doManageFiles(Map<String, Object> params) {
        setParams(params);
        httpRequest.setParseResult(false);
        httpRequest.post(oneOsRequest, new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {
                if (listener != null) {
                    listener.onStart(url, action);
                }
            }

            @Override
            public void onSuccess(String url, String result) {
                if (listener != null) {
                    listener.onSuccess(url, action, result);
                }
            }

            @Override
            public void onFailure(String url, int httpCode, int errorNo, String strMsg) {
                if (listener != null) {
                    listener.onFailure(url, action, errorNo, strMsg);
                }
            }
        });
    }


    public void attr(@NonNull String path) {

        this.action = FileManageAction.ATTRIBUTES;
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "attributes");
        params.put("path", path);
        doManageFiles(params);
    }

    public void delete(@NonNull List<OneOSFile> delList, boolean isDelShift) {
        del(genPathArray(delList), isDelShift);
    }

    public void del(@NonNull List<String> delList, boolean isDelShift) {
        this.action = isDelShift ? FileManageAction.DELETE_SHIFT : FileManageAction.DELETE;
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", isDelShift ? "deleteshift" : "delete");
        params.put("path", (delList));
        doManageFiles(params);

    }

    public void chmod(@NonNull OneOSFile file, String group, String other) {
        this.action = FileManageAction.CHMOD;
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "chmod");
        params.put("path", file.getPath());
        params.put("group", group);
        params.put("other", other);
        doManageFiles(params);
    }

    public void move(@NonNull List<OneOSFile> moveList, String toDir) {
        moveTo(genPathArray(moveList), toDir);
    }

    public void moveTo(@NonNull List<String> moveList, String toDir) {
        this.action = FileManageAction.MOVE;
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "move");
        params.put("path", moveList);
        params.put("todir", toDir);
        doManageFiles(params);

    }

    public void copy(@NonNull List<OneOSFile> copyList, String toDir) {
        copyTo(genPathArray(copyList), toDir);
    }

    public void copyTo(@NonNull List<String> copyList, String toDir) {
        this.action = FileManageAction.COPY;
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "copy");
        params.put("path", copyList);
        params.put("todir", toDir);
        if (loginSession.getOneOSInfo() != null && loginSession.getOneOSInfo().getVerno() > 51004) {
            params.put("show_progress", 1);
        }
        doManageFiles(params);
    }

    public void rename(@NonNull OneOSFile file, String newName) {
        renameTo(file.getPath(), newName);

    }

    public void renameTo(@NonNull String path, String newName) {
        this.action = FileManageAction.RENAME;
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "rename");
        params.put("path", path);
        params.put("newname", newName);
        doManageFiles(params);

    }

    public void readTxt(@NonNull OneOSFile file) {
        this.action = FileManageAction.READTXT;
        String path = file.getPath();
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "readtxt");
        params.put("path", path);
        doManageFiles(params);

    }


    public void mkdir(@NonNull String path, String newName) {

        this.action = FileManageAction.MKDIR;
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "mkdir");
        params.put("path", path + newName);
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, path + newName);
        doManageFiles(params);

    }

    public void crypt(@NonNull OneOSFile file, String pwd, boolean isEncrypt) {
        this.action = isEncrypt ? FileManageAction.ENCRYPT : FileManageAction.DECRYPT;
        String path = file.getPath();
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", isEncrypt ? "encrypt" : "decrypt");
        params.put("path", path);
        params.put("password", pwd);

        doManageFiles(params);
    }

    //解压
    public void extract(@NonNull OneOSFile file, String toDir) {
        this.action = FileManageAction.EXTRACT;
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "extract file to: " + toDir);
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "extract");
        params.put("path", file.getPath());
        params.put("todir", toDir);

        doManageFiles(params);
    }

    //    {"method":"recycle","session":"xxx","params":{"cmd":"clean","path":"xxx"}}
    public void cleanRecycle() {
        this.action = FileManageAction.CLEAN_RECYCLE;
        Map<String, Object> params = new HashMap<>();
        if (loginSession.getOneOSInfo() != null && loginSession.getOneOSInfo().getVerno() > 51004) {
            setMethod("recycle");
            params.put("cmd", "clean");
            params.put("area", 0);
        } else {
            params.put("cmd", "cleanrecycle");
            params.put("path", "private");
        }
        doManageFiles(params);
    }

    //{"method":"recycle","session":"xxx","params":{"cmd":"restore","path":["xxx","xxxx"]}}
    public void restoreRecycle(List<String> strings) {
        this.action = FileManageAction.RESTORE_RECYCLE;
        Map<String, Object> params = new HashMap<>();
        setMethod("recycle");
        params.put("cmd", "restore");
        params.put("path", strings);
        doManageFiles(params);

    }

    public void share(@NonNull ArrayList<OneOSFile> file, ArrayList<String> user) {
        this.action = FileManageAction.SHARE;
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "share");
        params.put("touser", user);
        params.put("path", genPathArray(file));
        doManageFiles(params);
    }


    @NonNull
    private ArrayList<String> genPathArray(List<OneOSFile> fileList) {
        ArrayList<String> pathList = new ArrayList<>(fileList.size());
        if (EmptyUtils.isEmpty(fileList)) {
            return pathList;
        }
        for (OneOSFile file : fileList) {
            pathList.add(file.getPath());
        }

        return pathList;
    }

    private static String pathArray(List<OneOSFile> fileList, String uid) {
        List<String> pathList = new ArrayList<>();
        for (OneOSFile file : fileList) {
            Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "pathArray path=" + file.getPath());
            if (file.getPath().indexOf("public") != -1) {
                pathList.add("storage/" + file.getPath());
            } else {
                pathList.add(uid + file.getPath());
            }
        }
        JSONArray jsonArray = new JSONArray(pathList);
        Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "JSON Array: " + jsonArray.toString());
        return jsonArray.toString();
    }

    public void setOnFileManageListener(OnFileManageListener listener) {
        this.listener = listener;
    }

    public interface OnFileManageListener {
        void onStart(String url, FileManageAction action);

        void onSuccess(String url, FileManageAction action, String response);

        void onFailure(String url, FileManageAction action, int errorNo, String errorMsg);
    }
}
