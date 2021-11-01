package net.sdvn.nascommon.receiver;

public interface ResultCallback<E> {
    void onFailure(Throwable e, E e2);
}
