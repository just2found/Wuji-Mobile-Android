package net.sdvn.nascommon;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

public abstract class LazyBaseFragment extends BaseFragment {

    public boolean bIsViewCreated; // 界面是否已创建完成
    public boolean bIsVisibleToUser; // 是否对用户可见
    public boolean bIsDataLoaded; // 数据是否已请求



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bIsViewCreated = false;
        bIsDataLoaded = false;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.bIsVisibleToUser = isVisibleToUser;
        tryLoadData();
    }

    /**
     * 保证在initData后触发
     */
    @Override
    public void onResume() {
        super.onResume();
        bIsViewCreated = true;
        tryLoadData();
    }

    /**
     * ViewPager场景下，判断父fragment是否可见
     */
    private boolean isParentVisible() {
        Fragment fragment = getParentFragment();
        return fragment == null || (fragment instanceof LazyBaseFragment
                && ((LazyBaseFragment) fragment).bIsVisibleToUser)
                || fragment.isResumed();
    }

    /**
     * ViewPager场景下，当前fragment可见时，如果其子fragment也可见，则让子fragment请求数据
     */
    private void dispatchParentVisibleState() {
        FragmentManager fragmentManager = getChildFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments.isEmpty()) {
            return;
        }
        for (Fragment child : fragments) {
            if (child instanceof LazyBaseFragment &&
                    ((LazyBaseFragment) child).bIsVisibleToUser) {
                ((LazyBaseFragment) child).tryLoadData();
            }
        }
    }

    public void tryLoadData() {
        if (bIsViewCreated && bIsVisibleToUser
                && isParentVisible() && !bIsDataLoaded) {
            loadData();
            bIsDataLoaded = true;
            //通知子Fragment请求数据
            dispatchParentVisibleState();
        }
    }

    /**
     * 加载数据
     */
    protected abstract void loadData();

}
