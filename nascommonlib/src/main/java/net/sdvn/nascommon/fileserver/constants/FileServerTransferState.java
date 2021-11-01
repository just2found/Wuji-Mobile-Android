package net.sdvn.nascommon.fileserver.constants;

public interface FileServerTransferState {
    int STATUS_INIT = 0;//创建
    int STATUS_RUNNING = 1; //运行中
    int STATUS_STOPPED = 2;//暂停中
    int STATUS_COMPLETE = 3;//完成
    int STATUS_ERROR = 4;//错误
    int STATUS_ERROR_BY_DISK_FULL = 5;//磁盘满，错误
}
