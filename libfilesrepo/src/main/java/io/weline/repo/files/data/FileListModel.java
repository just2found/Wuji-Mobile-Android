package io.weline.repo.files.data;

import com.google.gson.annotations.SerializedName;

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
