package net.sdvn.nascommon.iface;


import net.sdvn.nascommon.widget.FileManagePanel;

import java.util.List;

public interface MangerBarInterface<T, F> {
    /**
     * Show/Hide Bottom Operate Bar
     *
     * @param isShown Whether show
     */
    void showManageBar(boolean isShown);

    /**
     * Update Bottom Operate Bar`
     *
     * @param fileType     OneOS/Local file type
     * @param selectedList Selected file list
     * @param mListener    On file operate listener
     */
    void updateManageBar(T fileType, List<F> selectedList, Boolean isMore, FileManagePanel.OnFileManageListener<F> mListener);
}
