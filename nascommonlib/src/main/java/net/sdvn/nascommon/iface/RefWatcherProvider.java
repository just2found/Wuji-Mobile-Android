package net.sdvn.nascommon.iface;

import com.squareup.leakcanary.RefWatcher;

public interface RefWatcherProvider {
    RefWatcher getRefWatcher();
}
