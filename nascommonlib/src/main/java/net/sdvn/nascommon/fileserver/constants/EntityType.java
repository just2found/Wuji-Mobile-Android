package net.sdvn.nascommon.fileserver.constants;

public interface EntityType {
    int SHARE_FILE_V1 = 1 << 5;
    int SHARE_FILE_V2 = 1 << 4;
    int SHARE_FILE_V2_RECEIVE = SHARE_FILE_V2 | 1;
    int SHARE_FILE_V2_SEND = SHARE_FILE_V2 | 1 << 1;
    int SHARE_FILE_V2_COPY = SHARE_FILE_V2_SEND | SHARE_FILE_V2_RECEIVE;

    int SHARE_FILE_V1_RECEIVE = SHARE_FILE_V1 | 1;
    int SHARE_FILE_V1_SEND = SHARE_FILE_V1 | 1 << 1;
    int SHARE_FILE_V1_COPY = SHARE_FILE_V1_SEND | SHARE_FILE_V1_RECEIVE;
    int BIT_AND = 4;

    enum DownloadID {
        ALL_ID, SHARE_DOWNLOAD_ID_PATHS, SHARE_DOWNLOAD_ID_ERR_FILES, SHARE_DOWNLOAD_ID_COMPLETED
    }

    static void main(final String[] args) {
        System.out.println("SHARE_FILE_V2_RECEIVE : " + SHARE_FILE_V2_RECEIVE);
        System.out.println("SHARE_FILE_V2_SEND : " + SHARE_FILE_V2_SEND);
        System.out.println("SHARE_FILE_V2_COPY : " + SHARE_FILE_V2_COPY);
        System.out.println("SHARE_FILE_V2_SEND&SHARE_FILE_V2_COPY : " + (SHARE_FILE_V2_SEND & SHARE_FILE_V2_COPY));
        System.out.println("SHARE_FILE_V2_RECEIVE&SHARE_FILE_V2_COPY : " + (SHARE_FILE_V2_RECEIVE & SHARE_FILE_V2_COPY));
        System.out.println("SHARE_FILE_V2_RECEIVE&SHARE_FILE_V2_SEND : " + (SHARE_FILE_V2_RECEIVE & SHARE_FILE_V2_SEND));

        System.out.println("SHARE_FILE_V1_RECEIVE : " + SHARE_FILE_V1_RECEIVE);
        System.out.println("SHARE_FILE_V1_SEND : " + SHARE_FILE_V1_SEND);
        System.out.println("SHARE_FILE_V1_COPY : " + SHARE_FILE_V1_COPY);


        System.out.println("key : " + (1766616666 >>> 24));


    }
}
