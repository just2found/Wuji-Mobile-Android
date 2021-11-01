package net.sdvn.nascommon.model.oneos.transfer;

public interface TransmissionRunnable extends Runnable {
    void start();

    void pause();

    void restart();

    void cancel();
}
