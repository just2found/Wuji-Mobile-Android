package net.sdvn.nascommon.rx;

import androidx.annotation.NonNull;

import io.reactivex.disposables.Disposable;

public interface RxInterface {
    void addDisposable(@NonNull Disposable disposable);

    void dispose();

    void clear();
}
