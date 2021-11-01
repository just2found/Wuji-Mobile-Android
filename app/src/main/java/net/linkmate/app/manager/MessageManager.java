package net.linkmate.app.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import net.linkmate.app.BuildConfig;
import net.linkmate.app.base.MyApplication;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.protocal.SampleConnectStatusListener;
import net.sdvn.common.data.remote.MsgRemoteDataSource;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.GsonBaseProtocolV2;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.protocol.DataPages;
import net.sdvn.common.internet.protocol.entity.MsgCommon;
import net.sdvn.common.internet.protocol.entity.SdvnMessage;
import net.sdvn.common.repo.EnMbPointMsgRepo;
import net.sdvn.common.repo.SdvnMsgRepo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import libs.source.common.utils.RateLimiter;
import timber.log.Timber;


public class MessageManager extends SampleConnectStatusListener {
    public static final int QUICK_REFRESH_TIME = 10 * 1000;
    private int delayTime = 0;
    private int quickTime = 0;
    private boolean isQuickGetMessage = false;
    private boolean isEstablished;
    private List<SdvnMessage> messageslist;
    private final Handler threadHandler;
    private int messagesCount;
    //keep  don't change
    public final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final DevStatusReceiver mDevStatusReceiver;
    private RateLimiter<Object> mLimiter = new RateLimiter<>(QUICK_REFRESH_TIME, TimeUnit.MILLISECONDS);
    private boolean isScreenOn = true;
    //    private GetNewsHttpLoader refreshLoader;
//    private GetNewsHttpLoader loopLoader;
//    private GetENMbpointRatioHttpLoader mGetENMbpointRatioHttpLoader;
    private String mUserId;
    private long unreadTime;
    private HttpLoader messagesHttpLoader;
    private String mTicket;

    public static MessageManager getInstance() {
        return SingletonHolder.instance;
    }


    private static class SingletonHolder {
        private static MessageManager instance = new MessageManager();
    }

    private MessageManager() {
        messageslist = new ArrayList<>();
        HandlerThread mHandlerThread = new HandlerThread("Thread MessageManager");
        mHandlerThread.start();
        threadHandler = new Handler(mHandlerThread.getLooper());
        CMAPI.getInstance().addConnectionStatusListener(this);
        mDevStatusReceiver = new DevStatusReceiver();
    }

    private class DevStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
//                Timber.d("ACTION : %s", action);
                switch (action) {
                    case Intent.ACTION_SCREEN_OFF:
//                    case Intent.ACTION_BATTERY_LOW:
                        isScreenOn = false;
                        if (threadHandler != null) {
                            threadHandler.removeCallbacks(loopGetMessageRunnable);
                            Timber.d("msg stop loop ...");
                        }
                        break;
                    case Intent.ACTION_SCREEN_ON:
//                    case Intent.ACTION_USER_PRESENT:
//                    case Intent.ACTION_BATTERY_CHANGED:
//                    case Intent.ACTION_BATTERY_OKAY:
                        if (threadHandler != null && isEstablished && !isScreenOn) {
                            threadHandler.postDelayed(loopGetMessageRunnable, QUICK_REFRESH_TIME);
                            Timber.d("msg start loop -- %s", action);
                        }
                        isScreenOn = true;
                        break;
                }
            }
        }
    }

    @Override
    public void onConnected() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        LocalBroadcastManager.getInstance(MyApplication.getContext())
                .registerReceiver(mDevStatusReceiver, filter);
        mUserId = CMAPI.getInstance().getBaseInfo().getUserId();
        mTicket = CMAPI.getInstance().getBaseInfo().getTicket();
        unreadTime = EnMbPointMsgRepo.getLastItemTime(mUserId);
        isRefreshing = false;
        setEstablished(true);
//        loadMessage();
        delayTime = 0;
        if (threadHandler != null) {
            threadHandler.postDelayed(loopGetMessageRunnable, QUICK_REFRESH_TIME);
            Timber.d("msg start loop onConnected ...");
        }
    }

    @Override
    public void onDisconnected(int disconnectedReason) {
        try {
            if (messagesHttpLoader != null)
                messagesHttpLoader.cancel();
            messagesHttpLoader = null;
        } catch (Exception ignore) {
        }
//        try {
//            if (refreshLoader != null)
//                refreshLoader.cancel();
//            refreshLoader = null;
//        } catch (Exception ignore) {
//        }
//        try {
//            if (mGetENMbpointRatioHttpLoader != null) {
//                mGetENMbpointRatioHttpLoader.cancel();
//            }
//            mGetENMbpointRatioHttpLoader = null;
//        } catch (Exception ignore) {
//
//        }
        setEstablished(false);
        if (threadHandler != null)
            threadHandler.removeCallbacks(loopGetMessageRunnable);
        if (messageslist != null) {
            messageslist.clear();
        }
        LocalBroadcastManager.getInstance(MyApplication.getContext())
                .unregisterReceiver(mDevStatusReceiver);
//        clearMessagesCount();
    }

    public void setEstablished(boolean established) {
        isEstablished = established;
    }

    private Runnable loopGetMessageRunnable = new Runnable() {
        @Override
        public void run() {
            threadHandler.postDelayed(this, QUICK_REFRESH_TIME);
            if (isEstablished && ((delayTime % (CMAPI.getInstance().isApplicationInForeground() ? 12 : 360) == 0)
                    || isQuickGetMessage)) {
                //慢速模式下，12次延时处理一次; 快速模式则每次延时都处理
                if (isQuickGetMessage) {
                    quickTime++;
                }
                if (quickTime >= 10) {
                    //快速模式10次延迟后转为慢速
                    isQuickGetMessage = false;
                    delayTime = 1;
                }

                try {
                    Timber.d("msg loop exec ...");
//                    loadSysMsg();
//                    refreshEnMsg(false);
                    loadNewMsg();
                } catch (Exception ignore) {
                    Timber.d(ignore);
                }
            }
            delayTime++;
        }
    };
    private MsgRemoteDataSource mMsgRemoteDataSource = new MsgRemoteDataSource();

    public long getTenDaysAgo() {
        if (BuildConfig.DEBUG) {
            Timber.d("currentTime:%s", Calendar.getInstance().getTimeInMillis());
        }
        Calendar dueDate = Calendar.getInstance();
        // 设置在大约 05:00:00 AM 执行
        dueDate.set(Calendar.HOUR_OF_DAY, 0);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);
        dueDate.add(Calendar.DATE, -10);
        long timeInMillis = dueDate.getTimeInMillis();
        if (BuildConfig.DEBUG) {
            Timber.d("10dayAgoTime:%s", timeInMillis);
        }
        return timeInMillis;
    }

    public void loadNewMsg() {
        if (mLimiter.shouldFetch(mMsgRemoteDataSource)) {
            int pageSize = 15;
            final String userId = mUserId;
            long lastTime = SdvnMsgRepo.getLastTime(mUserId);
            if (lastTime == 0) {
                lastTime = getTenDaysAgo();
            }
            final String ticket = mTicket;
            long finalUnreadTime = lastTime;
            ResultListener<GsonBaseProtocolV2<DataPages<MsgCommon>>> listener = new ResultListener<GsonBaseProtocolV2<DataPages<MsgCommon>>>() {
                private List<MsgCommon> list = new CopyOnWriteArrayList<>();
                int count = 0;

                @Override
                public void success(@Nullable Object tag, GsonBaseProtocolV2<DataPages<MsgCommon>> data) {
                    if (data.isSuccessful()) {
                        if (data.data != null) {
                            int totalPage = data.data.getTotalPage();
                            int page = data.data.getPage();
                            if (data.data.getData() != null) {
                                list.addAll(data.data.getData());
                            }
                            if (finalUnreadTime > 0 && page < totalPage && count++ < 10) {
                                messagesHttpLoader = mMsgRemoteDataSource.getMessages(finalUnreadTime,
                                        ++page, pageSize, ticket, this);

                            } else {
                                SdvnMsgRepo.saveCommonData(list, userId, finalUnreadTime);
                            }
                        }

                    }
                }

                @Override
                public void error(@Nullable Object tag, GsonBaseProtocol baseProtocol) {

                }
            };
            messagesHttpLoader = mMsgRemoteDataSource.getMessages(lastTime,
                    0, pageSize, ticket, listener);
        }
    }

//    private void loadSysMsg() {
//        if (loopLoader == null) {
//            loopLoader = new GetNewsHttpLoader(SdvnMessageList.class);
//            loopLoader.setAsync(false);
//        }
//        if (mLimiter.shouldFetch(loopLoader) || isQuickGetMessage) {
//            long messageNewTimestamp = SdvnMsgRepo.getLastItemTime(userId);
//            loopLoader.setParams(MyApplication.getContext(), MySPUtils.getMessageNewDate(),
//                    messageNewTimestamp);
//            loopLoader.executor(new CommonResultListener<SdvnMessageList>() {
//                @Override
//                public void success(Object tag, SdvnMessageList data) {
//                    List<SdvnMessage> newslist = data.getNewslist();
//                    if (newslist != null) {
////                                    updateMessages(newslist);
//                        SdvnMsgRepo.saveData(newslist, messageNewTimestamp);
//                    }
////                            threadHandler.postDelayed(loopGetMessageRunnable, QUICK_REFRESH_TIME);
//                }
//
//                @Override
//                public void error(Object tag, GsonBaseProtocol baseProtocol) {
////                            threadHandler.postDelayed(loopGetMessageRunnable, QUICK_REFRESH_TIME);
//                }
//            });
//        }
//    }

    private void log(String tag, String msg) {
        if (BuildConfig.DEBUG)
            Timber.tag(tag).d(msg);
    }

    //进入快速刷新状态，用于某些需要后续关注消息的操作，在收到新消息后停止
    public void quickDelay() {
        isQuickGetMessage = true;
        quickTime = 0;
    }

    public void refreshEnMsg(boolean isOnce) {
        if (isOnce) {
            quickDelay();
        }
//        Runnable task = new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if (mGetENMbpointRatioHttpLoader == null) {
//                        mGetENMbpointRatioHttpLoader = new GetENMbpointRatioHttpLoader(GsonBaseProtocolV2.class);
//                        mGetENMbpointRatioHttpLoader.setAsync(false);
//                    }
//                    if (mLimiter.shouldFetch(mGetENMbpointRatioHttpLoader) || isOnce) {
//                        int pageSize = 15;
//                        Type type = new TypeToken<GsonBaseProtocolV2<DataPages<DataEnMbPointMsg>>>() {
//                        }.getType();
//                        if (unreadTime == 0) {
//                            unreadTime = getTenDaysAgo() / 1000;
//                        }
//                        long finalUnreadTime = unreadTime;
//                        String displayLanguage = Locale.getDefault().getLanguage();
//                        String ticket = CMAPI.getInstance().getBaseInfo().getTicket();
//                        mGetENMbpointRatioHttpLoader.setParams(displayLanguage, finalUnreadTime, 1, pageSize, ticket);
//                        CommonResultListener<GsonBaseProtocolV2<DataPages<DataEnMbPointMsg>>> resultListener =
//                                new CommonResultListener<GsonBaseProtocolV2<DataPages<DataEnMbPointMsg>>>() {
//                                    List<DataEnMbPointMsg> list = new ArrayList<>();
//                                    int count = 0;
//
//                                    @Override
//                                    public void success(Object tag, GsonBaseProtocolV2<DataPages<DataEnMbPointMsg>> data) {
//                                        if (data.isSuccessful()) {
//                                            if (data.data != null) {
//                                                int totalPage = data.data.getTotalPage();
//                                                int page = data.data.getPage();
//                                                if (data.data.getData() != null) {
//                                                    list.addAll(data.data.getData());
//                                                }
//                                                if (finalUnreadTime > 0 && page < totalPage && count++ < 10) {
//                                                    mGetENMbpointRatioHttpLoader.setParams(displayLanguage, finalUnreadTime, ++page, pageSize, ticket);
//                                                    mGetENMbpointRatioHttpLoader.executor(type, this);
//                                                } else {
//                                                    unreadTime = EnMbPointMsgRepo.saveData(list, userId, finalUnreadTime);
//                                                }
//                                            }
//
//                                        }
//                                    }
//
//                                };
//                        mGetENMbpointRatioHttpLoader.executor(type, resultListener);
//                    }
//                } catch (Exception e) {
//                    Timber.e(e);
//                }
//            }
//        };
//        if (Looper.myLooper() == Looper.getMainLooper()) {
//            if (threadHandler != null)
//                threadHandler.post(task);
//        } else {
//            task.run();
//        }
    }

    public void refreshMessage() {
//        refreshMessage(null, null);
    }

    private boolean isRefreshing;
    private int retryTime;

//    public void refreshMessage(final CommonResultListener<SdvnMessageList> listener, final HttpLoader.HttpLoaderStateListener loaderStateListener) {
//        if (isRefreshing)
//            return;
//        if (retryTime > 0 && refreshLoader == null) {
//            return;
//        }
//        threadHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (isRefreshing)
//                    return;
//                isRefreshing = true;
//                if (refreshLoader == null)
//                    refreshLoader = new GetNewsHttpLoader(SdvnMessageList.class);
//                long messageNewTimestamp = SdvnMsgRepo.getLastItemTime(userId);
//                refreshLoader.setParams(MyApplication.getContext(), MySPUtils.getMessageNewDate(),
//                        messageNewTimestamp);
//                if (loaderStateListener != null) {
//                    refreshLoader.setHttpLoaderStateListener(loaderStateListener);
//                }
//                refreshLoader.executor(new CommonResultListener<SdvnMessageList>() {
//                    @Override
//                    public void success(Object tag, SdvnMessageList data) {
//                        retryTime = 0;
//                        List<SdvnMessage> newslist = data.getNewslist();
//                        if (newslist != null) {
////                            updateMessages(newslist);
//                            SdvnMsgRepo.saveData(newslist, messageNewTimestamp);
//                        }
//                        if (listener != null)
//                            listener.success(tag, data);
//                        isRefreshing = false;
//                        refreshLoader = null;
//                    }
//
//                    @Override
//                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
//                        if (listener != null)
//                            listener.error(tag, baseProtocol);
//                        isRefreshing = false;
//                        if (retryTime++ < 3) {
//                            refreshMessage(listener, loaderStateListener);
//                        } else {
//                            refreshLoader = null;
//                        }
//                    }
//                });
//            }
//        });
//    }
//
//    private synchronized void updateMessages(List<SdvnMessage> messages) {
//        log("updateMessages", "updateMessages");
//        int newCount = 0;
//        String newDate = "";
//        long lNewTimestamp = -1;
//        List<String> deleteIds = MySPUtils.getMessageDeleteIdsKey();
//        boolean isUpdate = false;
//        a:
//        for (int i = messages.size() - 1; i >= 0; i--) {
//            SdvnMessage message = messages.get(i);
//            if (deleteIds.contains(message.getNewsid())) {
//                continue;
//            }
//            for (SdvnMessage sdvnMessage : messageslist) {
//                if (sdvnMessage.getNewsid().equals(message.getNewsid())) {
//                    sdvnMessage.setStatus(message.getStatus());
//                    isUpdate = true;
//                    continue a;
//                }
//            }
//            newDate = message.getDate();
//            lNewTimestamp = lNewTimestamp > message.getTimestamp() ? lNewTimestamp : message.getTimestamp();
//            newCount++;
//            messageslist.add(0, message);
//            log("updateMessages", "messageslist add :" + newCount);
//        }
//        if (MySPUtils.getMessageNewTimestamp() != -1) {
//            messagesCount += newCount;
//            log("updateMessages", "messagesCount update :" + messagesCount);
//        }
//        if (newCount > 0) {
//            log("updateMessages", "sort notify");
//            isQuickGetMessage = false;
//            saveMessage();
//            MySPUtils.saveMessageNewCount(messagesCount);
//            saveDate(newDate, lNewTimestamp);
//            long currentTimeMillis = System.currentTimeMillis();
//            Collections.sort(messageslist, new Comparator<SdvnMessage>() {
//                @Override
//                public int compare(SdvnMessage o1, SdvnMessage o2) {
//                    try {
//                        long o1Timestamp, o2Timestamp;
//                        if (o1.getTimestamp() > 0)
//                            o1Timestamp = o1.getTimestamp();
//                        else
//                            o1Timestamp = sdf.parse(o1.getDate()).getTime() / 1000;
//                        if (o2.getTimestamp() > 0)
//                            o2Timestamp = o2.getTimestamp();
//                        else
//                            o2Timestamp = sdf.parse(o2.getDate()).getTime() / 1000;
//                        if (o1Timestamp == o2Timestamp) return 0;
//                        return o1Timestamp > o2Timestamp ? -1 : 1;
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                    return 0;
//                }
//            });
//            notifyMessagesListObserver(messageslist);
//        } else if (isUpdate) {
//            log("updateMessages", "notify");
//            saveMessage();
//            notifyMessagesListObserver(messageslist);
//        }
//    }
//
//    public synchronized void setMessagesStatus(String newsid, String status) {
//        for (SdvnMessage sdvnMessage : messageslist) {
//            if (sdvnMessage.getNewsid().equals(newsid)) {
//                sdvnMessage.setStatus(status);
//                saveMessage();
//                notifyMessagesListObserver(messageslist);
//                break;
//            }
//        }
//    }
//
//    public synchronized void removeMessages(List<String> ids) {
//        Iterator<SdvnMessage> iterator = messageslist.iterator();
//        a:
//        while (iterator.hasNext()) {
//            SdvnMessage next = iterator.next();
//            for (String id : ids) {
//                if (next.getNewsid().equals(id)) {
//                    iterator.remove();
//                    continue a;
//                }
//            }
//        }
//        saveMessage();
//        notifyMessagesListObserver(messageslist);
//    }
//
//    public synchronized void loadMessage() {
//        messagesCount = MySPUtils.getMessageNewCount();
//        messageslist.clear();
//        messageslist.addAll(MySPUtils.loadMessage());
//    }
//
//    private void saveMessage() {
////        if (messageslist.size() > 1000) {
////            MySPUtils.saveMessage(messageslist.subList(0, 1000));
////        } else {
////        MySPUtils.saveMessage(messageslist);
////        }
//    }
//
//    private void saveDate(String date, long timestamp) {
//        MySPUtils.saveMessageNewDate(date);
//        MySPUtils.saveMessageNewTimestamp(timestamp);
//    }
//
//    public void clearMessagesCount() {
//        messagesCount = 0;
//        MySPUtils.saveMessageNewCount(0);
//        notifyMessagesListObserver(messageslist);
//    }
//
//    public List<SdvnMessage> getMessageslist() {
//        return messageslist;
//    }
//
//    /**
//     * 消息观察者
//     */
//    public interface MessagesListObserver {
//        void onMessagesListChanged(int newCount, List<SdvnMessage> messages);
//    }
//
//    private final byte[] messagesListLock = new byte[0];
//    private WeakHashMap<MessagesListObserver, Integer> messagesListObservers = new WeakHashMap<>();
//
//    public synchronized void addMessagesListObserver(MessagesListObserver o) {
//        synchronized (messagesListLock) {
//            if (o == null)
//                throw new NullPointerException();
//            if (!messagesListObservers.containsKey(o)) {
//                messagesListObservers.put(o, 0);
//                o.onMessagesListChanged(messagesCount, messageslist);
//            }
//        }
//    }
//
//    public synchronized void deleteMessagesListObserver(MessagesListObserver o) {
//        synchronized (messagesListLock) {
//            messagesListObservers.remove(o);
//        }
//    }
//
//    private void notifyMessagesListObserver(List<SdvnMessage> beans) {
//        synchronized (messagesListLock) {
//            for (MessagesListObserver o : messagesListObservers.keySet()) {
//                if (o != null) {
//                    o.onMessagesListChanged(messagesCount, beans);
//                }
//            }
//        }
//    }
}

