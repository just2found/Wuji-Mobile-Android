package net.sdvn.nascommon.model.oneos.vo;

import com.google.gson.annotations.SerializedName;

import net.sdvn.nascommon.model.oneos.OneOSFile;

import java.util.List;

public class FileListModel {
    @SerializedName("files")
    public List<OneOSFile> files;
    @SerializedName("total")
    public int total;
    @SerializedName("page")
    public int page;
    @SerializedName("pages")
    public int pages;


    public boolean hasMorePage() {
        return pages - 1 > page;
    }

    public int nextPage() {
        return page + 1;
    }
}
