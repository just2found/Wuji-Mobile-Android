package net.sdvn.nascommon.iface;


import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.OneOSFileType;

import java.util.ArrayList;

public interface OnFileListListener {
    void onStart(String url);

    void onSuccess(String url, OneOSFileType type, String path, int total, int pages, int page, ArrayList<OneOSFile> files);

    void onFailure(String url, OneOSFileType type, String path, int errorNo, String errorMsg);
}