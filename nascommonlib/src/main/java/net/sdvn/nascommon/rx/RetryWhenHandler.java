package net.sdvn.nascommon.rx;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public final class RetryWhenHandler implements Function<Observable<? extends Throwable>, Observable<Long>> {

    private static final int INITIAL = 1;
    private int maxConnectCount = 1;

    public RetryWhenHandler(int retryCount) {
        this.maxConnectCount += retryCount;
    }

    @Override
    public Observable<Long> apply(Observable<? extends Throwable> errorObservable) {
        return errorObservable.zipWith(Observable.range(INITIAL, maxConnectCount),
                new BiFunction<Throwable, Integer, ThrowableWrapper>() {
                    @Override
                    public ThrowableWrapper apply(Throwable throwable, Integer integer) throws Exception {
                        //①只在IOException的情况下记录本次请求在最大请求次数中的位置，否则视为最后一次请求，避免多余的请求重试。
                        if (throwable instanceof IOException)
                            return new ThrowableWrapper(throwable, integer);

                        return new ThrowableWrapper(throwable, maxConnectCount);
                    }

                }).concatMap(new Function<ThrowableWrapper, Observable<Long>>() {
            @Override
            public Observable<Long> apply(ThrowableWrapper throwableWrapper) {

                final int retryCount = throwableWrapper.getRetryCount();

                //②如果最后一次网络请求依然遭遇了异常，则将此异常继续向下传递，以便在最后的onError()函数中处理。
                if (maxConnectCount == retryCount) {
                    return Observable.error(throwableWrapper.getSourceThrowable());
                }

                //③
                return Observable.timer((long) Math.pow(3, retryCount), TimeUnit.SECONDS);
            }
        });
    }

    private static final class ThrowableWrapper {

        private Throwable sourceThrowable;
        private Integer retryCount;

        ThrowableWrapper(Throwable sourceThrowable, Integer retryCount) {
            this.sourceThrowable = sourceThrowable;
            this.retryCount = retryCount;
        }

        Throwable getSourceThrowable() {
            return sourceThrowable;
        }

        Integer getRetryCount() {
            return retryCount;
        }
    }
}