package net.linkmate.app.view.adapter;

import android.os.Bundle;
import android.util.SparseArray;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MainVPAdapter extends FragmentPagerAdapter {
    private final SparseArray<Fragment> mFragments;

    public MainVPAdapter(FragmentManager fm, SparseArray<Fragment> fragments) {
        super(fm);
        this.mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments == null ? 0 : mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Bundle arguments = mFragments.get(position).getArguments();
        String title = arguments != null ? arguments.getString("title") : "";
        return title != null ? title : "";
    }
}
