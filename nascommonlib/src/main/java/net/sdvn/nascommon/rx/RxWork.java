package net.sdvn.nascommon.rx;

import androidx.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class RxWork implements RxInterface{

    private CompositeDisposable compositeDisposable;

    public void addDisposable(@NonNull Disposable disposable) {
        if (compositeDisposable == null || compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    public void dispose() {
        if (compositeDisposable != null) compositeDisposable.dispose();
    }

    public void clear() {
        if (compositeDisposable != null) compositeDisposable.clear();
    }

}
