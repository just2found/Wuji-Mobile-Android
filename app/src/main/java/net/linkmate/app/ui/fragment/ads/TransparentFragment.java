package net.linkmate.app.ui.fragment.ads;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import net.linkmate.app.R;
import net.linkmate.app.ui.fragment.BaseFragment;

/**
 *  
 * <p>
 * Created by admin on 2020/7/31,10:44
 */
public abstract class TransparentFragment extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.StyledIndicators);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        return localInflater.inflate(getLayoutId(), container, false);
    }
}
