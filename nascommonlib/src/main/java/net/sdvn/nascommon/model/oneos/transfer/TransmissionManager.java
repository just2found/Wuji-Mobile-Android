package net.sdvn.nascommon.model.oneos.transfer;

import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.db.TransferHistoryKeeper;
import net.sdvn.nascommon.db.objecbox.TransferHistory;
import net.sdvn.nascommon.db.objecbox.TransferHistory_;
import net.sdvn.nascommon.model.oneos.transfer.thread.Priority;
import net.sdvn.nascommon.utils.log.Logger;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.objectbox.query.Query;
import libs.source.common.utils.RateLimiter;
import timber.log.Timber;

public abstract class TransmissionManager<T extends TransferElement> extends TransferManager<T> {

    protected int mPriority = Priority.DEFAULT;
    private static final String LOG_TAG = TransmissionManager.class.getSimpleName();

    public TransmissionManager(boolean isDownload) {
        super(isDownload);
    }

    @Nullable
    protected OnTransferFileListener<TransferElement> mOnTransferFileListener;

    public void setOnTransferFileListener(@Nullable OnTransferFileListener<TransferElement> onTransferFileListener) {
        mOnTransferFileListener = onTransferFileListener;
    }

    protected RateLimiter<Object> mLimiter = new RateLimiter<>(3, TimeUnit.SECONDS);

    /**
     * Cancel task and remove them from the manager. The task will be stopped if
     * it was running, and it will no longer be accessible through the manager. If there is
     * a temporary file, partial or complete, it is deleted.
     *
     * @param tag file full path at server, uniqueness
     * @return the id of task actually removed, if remove failed, return -1.
     */
    @Override
    public int cancel(String tag) {
        T element = findElement(tag);
        if (element != null) {
            TransmissionRunnable runnable = mTaskHashMap.get(element);
            if (runnable != null) {
                runnable.cancel();
            }
            mTaskHashMap.remove(element);
            executeBackgroundTask(new Runnable() {
                @Override
                public void run() {
                    TransferHistory query = TransferHistoryKeeper.query(TransferHistoryKeeper.getTransferType(isDownload)
                            , element.getDevId(), element.getSrcPath(), element.getSrcName(), element.getToPath());
                    TransferHistoryKeeper.delete(query);
                    getMainHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyTransferCount();
                        }
                    }, 20);
                }
            });
            return element.hashCode();
        }
        return -1;
    }

    @Override
    public void cancel(List<String> tags) {
        Logger.p(Logger.Level.DEBUG, IS_LOG, LOG_TAG, "Remove all download tasks");
        final HashMap<T, TransmissionRunnable> map = new HashMap<>(mTaskHashMap);
        executeBackgroundTask(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> mutableList = new ArrayList<>(tags);
                Iterator<Map.Entry<T, TransmissionRunnable>> entryIterator = map.entrySet().iterator();
                List<Long> list = new ArrayList<>();
                while (entryIterator.hasNext()) {
                    Map.Entry<T, TransmissionRunnable> entry = entryIterator.next();
                    TransmissionRunnable task = entry.getValue();
                    T element = entry.getKey();
                    Iterator<String> iterator = mutableList.iterator();
                    while (iterator.hasNext()) {
                        String tag = iterator.next();
                        if (Objects.equals(tag, element.getTag())) {
                            if (task != null) {
                                task.cancel();
                            }
                            long id = element.getId();
                            if (id > 0) {
                                list.add(id);
                            }
                            iterator.remove();
                            entryIterator.remove();
                        }
                    }
                }
                TransferHistoryKeeper.delete(list);
                getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mTaskHashMap.clear();
                        mTaskHashMap.putAll(map);
                        notifyTransferCount();
                    }
                }, 20);
            }
        });

    }

    /**
     * Cancel all task and remove them from the manager.
     *
     * @return true if succeed, false otherwise.
     * @see #cancel(String)
     */
    @Override
    public boolean cancel() {
        Logger.p(Logger.Level.DEBUG, IS_LOG, LOG_TAG, "Remove all download tasks");
        final HashMap<T, TransmissionRunnable> map = new HashMap<>(mTaskHashMap);
        executeBackgroundTask(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<T, TransmissionRunnable> entry : map.entrySet()) {
                    TransmissionRunnable task = entry.getValue();
                    T element = entry.getKey();
                    if (task != null)
                        task.cancel();
                    element.setState(TransferState.CANCELED);
                }
                Set<T> ts = map.keySet();
                long[] longs = new long[ts.size()];
                int count = 0;
                for (T t : ts) {
                    long id = t.getId();
                    if (id > 0) {
                        longs[count] = id;
                    }
                    count++;
                }
                TransferHistoryKeeper.delete(longs);
                getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mTaskHashMap.clear();
                        notifyTransferCount();
                    }
                }, 20);
            }
        });

        return true;
    }

    /**
     * Pause the task, set state to {@link TransferState PAUSE }
     *
     * @param tag
     * @return true if succeed, false otherwise.
     */
    @Override
    public boolean pause(String tag) {
        T element = findElement(tag);

        if (element == null) {
            return false;
        }
        TransmissionRunnable runnable = mTaskHashMap.get(element);
        if (runnable != null) {
            runnable.pause();
        }
        Logger.p(Logger.Level.DEBUG, IS_LOG, LOG_TAG, "Pause download: " + tag + "; " +
                "state: " + element.getState());
//        element.setState(TransferState.PAUSE);
        return true;
    }

    /**
     * Pause all tasks
     *
     * @return true if succeed, false otherwise.
     * @see #pause(String)
     */
    @Override
    public boolean pause() {
        long start = SystemClock.uptimeMillis();
//        executeBackgroundTask(new Runnable() {
//            @Override
//            public void run() {
        HashMap<T, TransmissionRunnable> map = new HashMap<>(mTaskHashMap);
        for (Map.Entry<T, TransmissionRunnable> entry : map.entrySet()) {
            TransmissionRunnable task = entry.getValue();
            T element = entry.getKey();
            if (element != null) {
//                if (element.getState() == TransferState.WAIT)
//                    element.setState(TransferState.PAUSE);
//                else if (element.getState() == TransferState.START_CHANNEL)
                if (task != null) {
                    task.pause();
                }
                element.setState(TransferState.PAUSE);
            }
        }
//            }
//        });
        Timber.d("pause consume %s", (SystemClock.uptimeMillis() - start));
        return true;
    }

    /**
     * Pause all tasks
     *
     * @return true if succeed, false otherwise.
     * @see #pause(String)
     */
    @Override
    public void pause(List<String> tags) {
        long start = SystemClock.uptimeMillis();
        HashMap<T, TransmissionRunnable> map = new HashMap<>(mTaskHashMap);
        ArrayList<String> mutableList = new ArrayList<>(tags);
        for (Map.Entry<T, TransmissionRunnable> entry : map.entrySet()) {
            TransmissionRunnable task = entry.getValue();
            T element = entry.getKey();
            Iterator<String> iterator = mutableList.iterator();
            while (iterator.hasNext()) {
                String tag = iterator.next();
                if (Objects.equals(tag, element.getTag())) {
                    if (task != null) {
                        task.pause();
                    }
                    element.setState(TransferState.PAUSE);
                    iterator.remove();
                }
            }
        }
        Timber.d("pause consume %s", (SystemClock.uptimeMillis() - start));
    }

    /**
     * Resume the task, reset state to {@link TransferState WAITING }
     *
     * @param tag file full path at server, uniqueness
     * @return true if succeed, false otherwise.
     */
    @Override
    public boolean resume(String tag) {
        Logger.p(Logger.Level.DEBUG, IS_LOG, LOG_TAG, "Continue download: " + tag);

        T element = findElement(tag);
        if (element == null) {
            return false;
        }
        TransmissionRunnable task = mTaskHashMap.get(element);
        if (task == null) {
            task = genTransmissionRunnable(element);
        }
        task.start();
        return true;
    }

    /**
     * Resume all tasks
     *
     * @return true if succeed, false otherwise.
     * @see #resume(String)
     */
    @Override
    public boolean resume() {
        Logger.p(Logger.Level.DEBUG, IS_LOG, LOG_TAG, "Continue activeUsers downloads");
        long start = SystemClock.uptimeMillis();
        HashMap<T, TransmissionRunnable> map = new HashMap<>(mTaskHashMap);
        List<T> list = new ArrayList<>();
        for (Map.Entry<T, TransmissionRunnable> entry : map.entrySet()) {
            T element = entry.getKey();
            element.setPriority((int) (Priority.DEFAULT - element.id));
            TransmissionRunnable runnable = entry.getValue();
            if (runnable == null) {
                runnable = genTransmissionRunnable(element);
                mTaskHashMap.put(element, runnable);
            }
            list.add(element);
        }
        Collections.sort(list, (o1, o2) -> Long.compare(o1.id, o2.id));
        for (T t : list) {
            final TransmissionRunnable transmissionRunnable = map.get(t);
            if (transmissionRunnable != null) {
                transmissionRunnable.start();
            }
        }
        Timber.d("resume consume %s", (SystemClock.uptimeMillis() - start));
        return true;
    }

    /**
     * Resume all tasks
     *
     * @return true if succeed, false otherwise.
     * @see #resume(String)
     */
    @Override
    public void resume(List<String> tags) {
        Logger.p(Logger.Level.DEBUG, IS_LOG, LOG_TAG, "Continue activeUsers downloads");
        long start = SystemClock.uptimeMillis();
        HashMap<T, TransmissionRunnable> map = new HashMap<>(mTaskHashMap);
        List<T> list = new ArrayList<>();
        ArrayList<String> mutableList = new ArrayList<>(tags);
        for (Map.Entry<T, TransmissionRunnable> entry : map.entrySet()) {
            T element = entry.getKey();
            Iterator<String> iterator = mutableList.iterator();
            while (iterator.hasNext()) {
                String tag = iterator.next();
                if (Objects.equals(tag, element.getTag())) {
                    element.setPriority((int) (Priority.DEFAULT - element.id));
                    TransmissionRunnable runnable = entry.getValue();
                    if (runnable == null) {
                        runnable = genTransmissionRunnable(element);
                        mTaskHashMap.put(element, runnable);
                    }
                    list.add(element);
                    iterator.remove();
                }
            }
        }
        Collections.sort(list, (o1, o2) -> Long.compare(o1.id, o2.id));
        for (T t : list) {
            final TransmissionRunnable transmissionRunnable = map.get(t);
            if (transmissionRunnable != null) {
                transmissionRunnable.start();
            }
        }
        Timber.d("resume consume %s", (SystemClock.uptimeMillis() - start));
    }

    @NonNull
    protected abstract TransmissionRunnable genTransmissionRunnable(T element);


    /**
     * Get transfer task list
     *
     * @return transfer list
     */
    @NonNull
    @Override
    public List<T> getTransferList() {
        ArrayList<T> destList = new ArrayList<>();
        HashMap<T, TransmissionRunnable> map = new HashMap<>(mTaskHashMap);
        for (Map.Entry<T, TransmissionRunnable> entry : map.entrySet()) {
            T element = entry.getKey();
            if (element != null) {
                destList.add(element);
            }
        }

        Collections.sort(destList, new Comparator<T>() {
            @Override
            public int compare(@Nullable T o1, @Nullable T o2) {
                if (o1 != null && o2 != null) {
                    return (int) (o2.getId() - o1.getId());
                }
                return 0;
            }
        });

        return destList;
    }


    /**
     * Find element from {@code transferList} by file path
     *
     * @param tag
     * @return Element or {@code null}
     */
    @Nullable
    @Override
    public T findElement(String tag) {
        HashMap<T, TransmissionRunnable> map = new HashMap<>(mTaskHashMap);
        for (Map.Entry<T, TransmissionRunnable> entry : map.entrySet()) {
            T element = entry.getKey();
            if (element != null)
                if (Objects.equals(element.getTag(), tag)) {
                    return element;
                }
        }
        Logger.p(Logger.Level.DEBUG, IS_LOG, LOG_TAG, "Can't find element: " + tag);

        return null;
    }

    @Override
    public int enqueue(List<T> elements) {
        if (elements.size() > 0) {
            T element0 = elements.get(0);
            ArrayList<T> arrayList = new ArrayList<>(elements);
            Query<TransferHistory> query = getQuery(element0);
            List<TransferHistory> histories = findIdsTransferHistories(arrayList, query);
            for (T element : arrayList) {
                TransferHistory transferHistory = getTransferHistory(element);
                histories.add(transferHistory);
            }
            TransferHistoryKeeper.update(histories);
            Timber.d(" enqueue(List<TransferElement> elements : %s", elements.size());
            findIdsTransferHistories(arrayList, query);
            for (T element : elements) {
                TransmissionRunnable runnable = mTaskHashMap.get(element);
                if (runnable == null) {
                    runnable = genTransmissionRunnable(element);
                    mTaskHashMap.put(element, runnable);
                }
                if (element.getPriority() == Priority.DEFAULT)
                    element.setPriority((int) (mPriority - element.getId()));
                runnable.start();
            }
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    notifyTransferCount();
                }
            });
        }
        return 0;
    }

    @NonNull
    public Query<TransferHistory> getQuery(T element0) {
        if (isDownload) {
            return TransferHistoryKeeper.getTransferHistoryQueryBuilder()
                    .equal(TransferHistory_.srcDevId, element0.getDevId())
                    .equal(TransferHistory_.type, TransferHistoryKeeper.getTransferType(isDownload))
                    .build();
        } else {
            return TransferHistoryKeeper.getTransferHistoryQueryBuilder()
                    .equal(TransferHistory_.srcDevId, element0.getDevId())
                    .equal(TransferHistory_.type, TransferHistoryKeeper.getTransferType(isDownload))
                    .equal(TransferHistory_.toPath, element0.getToPath())
                    .build();
        }
    }

    ;

    @NotNull
    protected abstract TransferHistory getTransferHistory(T element);

    @NotNull
    private List<TransferHistory> findIdsTransferHistories(ArrayList<T> arrayList, Query<TransferHistory> query) {
        long count = query.count();
        long offset = 0;
        long limit = 800;
        List<TransferHistory> histories = new ArrayList<>();
        out:
        while (offset < count) {
            List<TransferHistory> transferHistoriesPart = query.find(offset, limit);
            offset += limit;
            Iterator<T> iterator = arrayList.iterator();
            inner:
            for (TransferHistory transferHistory : transferHistoriesPart) {
                while (iterator.hasNext()) {
                    T next = iterator.next();
                    if (Objects.equals(transferHistory.getSrcPath(), next.getSrcPath())) {
                        if (!transferHistory.getIsComplete()) {
                            next.setLength(transferHistory.getLength());
                        } else {
                            next.setCheck(true);
                        }
                        next.id = transferHistory.getId();
                        histories.add(transferHistory);
                        iterator.remove();
                        continue inner;
                    }
                }
                if (arrayList.size() == 0) {
                    break out;
                }
            }
        }
        return histories;
    }


    /**
     * Destroy transfer manager
     */
    @Override
    public void onDestroy() {
        pause();
        notifyTransferCount();
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
        threadPool.removeAll();

    }
}
