package net.sdvn.nascommon.model.oneos;

public interface MultiSelectStateChangeListener {
    void update(int totalCount, int selectedCount);
}
