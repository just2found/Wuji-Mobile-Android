package net.sdvn.nascommon;

import net.sdvn.nascommon.widget.FileManagePanel;
import net.sdvn.nascommon.widget.FileSelectPanel;
import net.sdvn.nascommon.widget.SearchPanel;

import java.util.List;

/**
 * Navigation Base Abstract Class
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public abstract class BaseNavFileFragment<T, F> extends BaseFragment {

    /**
     * Show/Hide Top Select Bar
     *
     * @param isShown Whether show
     */
    public void showSelectBar(boolean isShown) {
    }

    /**
     * Update Top Select Bar
     *
     * @param totalCount    Total select count
     * @param selectedCount Selected count
     * @param mListener     On file select listener
     */
    public void updateSelectBar(int totalCount, int selectedCount, FileSelectPanel.OnFileSelectListener mListener) {
    }

    /**
     * Show/Hide Bottom Operate Bar
     *
     * @param isShown Whether show
     */
    public void showManageBar(boolean isShown) {
    }

    /**
     * Update Bottom Operate Bar`
     *
     * @param fileType     OneOS/Local file type
     * @param selectedList Selected file list
     * @param mListener    On file operate listener
     */
//    public abstract void updateManageBar(T fileType, ArrayList<F> selectedList, FileManagePanel.OnFileManageListener mListener);
    public void updateManageBar(T fileType, List<F> selectedList, Boolean isMore, FileManagePanel.OnFileManageListener<F> mListener) {
    }

    /**
     * Add search file listener
     *
     * @param listener
     */
    public void addSearchListener(SearchPanel.OnSearchActionListener listener) {
    }

}
