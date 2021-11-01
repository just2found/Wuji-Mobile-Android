package net.sdvn.common.internet;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Description:
 *
 * @author admin
 * CreateDate: 2021/4/8
 */
public class TestRxjava {
    @Test
    public void testDoOnSubscribe() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        Observable.create(emitter -> {
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {

                }
                emitter.onNext(" emitter.onNext  " + i);
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .doOnSubscribe(disposable -> {
                    System.out.println("doOnSubscribe : " + disposable.toString());
//                    compositeDisposable.add(disposable);
                })
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        System.out.println("onSubscribe : " + disposable.toString());
                        compositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(@NonNull Object o) {
                        System.out.println(o);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println("onError" + e);
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");
                    }
                });

        try {
            Thread.sleep(500);
            compositeDisposable.dispose();
            System.out.println("end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
