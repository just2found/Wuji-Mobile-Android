package net.sdvn.nascommon.iface;


import net.sdvn.nascommon.utils.log.Logger;

import io.reactivex.functions.Consumer;

public class ConsumerThrowable implements Consumer<Throwable> {
    private Runnable mRunnable;
    private String mTag;
    private Object[] mMessages;

    public ConsumerThrowable(String tag, Object... messages) {
        mTag = tag;
        mMessages = messages;
    }

    public ConsumerThrowable(Runnable runnable, String tag, Object... messages) {
        mRunnable = runnable;
        mTag = tag;
        mMessages = messages;
    }

    @Override
    public void accept(Throwable throwable) throws Exception {
        Logger.LOGE(mTag, throwable, mMessages);
        if (mRunnable != null) {
            mRunnable.run();
        }
    }
}
