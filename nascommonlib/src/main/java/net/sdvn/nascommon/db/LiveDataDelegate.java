package net.sdvn.nascommon.db;

import androidx.lifecycle.LiveData;

import java.util.List;

import io.objectbox.query.Query;
import io.objectbox.reactive.DataObserver;
import io.objectbox.reactive.DataSubscription;

public class LiveDataDelegate<T> extends LiveData<List<T>> {

    private final Query<T> query;
    private DataSubscription subscription;
    private final DataObserver<List<T>> listener = new DataObserver<List<T>>() {
        public void onData(List<T> data) {
            LiveDataDelegate.this.postValue(data);
        }
    };

    public LiveDataDelegate(Query<T> query) {
        this.query = query;
    }

    protected void onActive() {
        if (this.subscription == null) {
            this.subscription = this.query.subscribe().weak().observer(this.listener);
        }
    }

    protected void onInactive() {
        if (!this.hasObservers()) {
            this.subscription.cancel();
            this.subscription = null;
        }
    }

    @Override
    public void postValue(List<T> value) {
        super.postValue(value);
    }

    @Override
    public void setValue(List<T> value) {
        super.setValue(value);
    }

}
