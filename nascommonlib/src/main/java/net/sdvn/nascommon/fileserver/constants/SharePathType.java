package net.sdvn.nascommon.fileserver.constants;

public enum SharePathType {
    USER(0), VIRTUAL(1), PUBLIC(2), GLOBAL(3), SAFE_BOX(4), GROUP(6), EXTERNAL_STORAGE(7);

    private int mType;

    SharePathType(int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }
}