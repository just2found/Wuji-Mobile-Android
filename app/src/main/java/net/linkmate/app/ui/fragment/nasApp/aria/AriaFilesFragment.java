package net.linkmate.app.ui.fragment.nasApp.aria;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.linkmate.app.R;
import net.linkmate.app.ui.activity.nasApp.aria.AriaDetailsActivity;
import net.linkmate.app.view.adapter.AriaFileAdapter;
import net.sdvn.nascommon.model.oneos.aria.AriaFile;
import net.sdvn.nascommon.model.oneos.aria.AriaStatus;
import net.sdvn.nascommon.utils.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Aria2 Task File List Fragment
 *
 * @author shz
 * @since V1.6.21
 */
public class AriaFilesFragment extends Fragment implements AriaDetailsActivity.OnAriaTaskChangedListener {

    private static final String TAG = AriaFilesFragment.class.getSimpleName();

    private ListView mListView;
    @Nullable
    private AriaFileAdapter mAdapter;

    @NonNull
    private List<AriaFile> mTaskFileList = new ArrayList<AriaFile>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aria_files, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {
        mListView = view.findViewById(R.id.listview_files);
        mAdapter = new AriaFileAdapter(getActivity(), mTaskFileList);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onAriaChanged(@Nullable AriaStatus ariaStatus) {
        mTaskFileList.clear();
        if (null != ariaStatus) {
            List<AriaFile> list = ariaStatus.getFiles();
            if (null != list) {
                mTaskFileList.addAll(list);
            }
        }
        Logger.LOGD(TAG, "Aria File List: " + mTaskFileList.size());
        if (null != mAdapter) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
