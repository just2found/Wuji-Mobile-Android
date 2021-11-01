package net.sdvn.nascommon.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class RxViewModel extends ViewModel {
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

    @Override
    protected void onCleared() {
        dispose();
        super.onCleared();
    }

}
