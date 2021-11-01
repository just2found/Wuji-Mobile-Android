//package net.sdvn.common.internet;
//
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.text.TextUtils;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.google.gson.Gson;
//
//import net.sdvn.cmapi.CMAPI;
//import net.sdvn.cmapi.global.Constants;
//import net.sdvn.common.internet.core.GsonBaseProtocol;
//import net.sdvn.common.internet.listener.CommonResultListener;
//import net.sdvn.common.internet.loader.GetNewsHttpLoader;
//import net.sdvn.common.internet.protocol.SdvnMessageList;
//import net.sdvn.common.internet.protocol.entity.SdvnMessage;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Locale;
//import java.util.Objects;
//import java.util.WeakHashMap;
//
//
//public class SdvnMessageManager {
//    public static final int REFRESH_TIME = 2 * 60 * 1000;
//    private static final String TAG = SdvnMessageManager.class.getSimpleName();
//    private static SdvnMessageManager sInstance;
//    public final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//    @NonNull
//    private final Handler threadHandler;
//    private final byte[] messagesListLock = new byte[0];
//    private List<SdvnMessage> messages;
//    private int messagesCount;
//    private boolean isGoing;
//    private Comparator<SdvnMessage> mMessageComparator;
//    private GetNewsHttpLoader mGetNewsHttpLoader;
//    private long count;
//    @NonNull
//    private WeakHashMap<MessagesListObserver, Integer> messagesListObservers = new WeakHashMap<>();
//    @NonNull
//    private Runnable loopGetMessageRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (isGoing) {
//                if (CMAPI.getInstance().isApplicationInForeground() || count / 60 == 0) {
//                    getNews();
//                    Log.d(TAG, "GetMsg");
//                }
//                count++;
//                threadHandler.postDelayed(this, REFRESH_TIME);
//            } else
//                Log.d(TAG, "StopGet");
//        }
//    };
//
//
//    private SdvnMessageManager() {
//        messages = new ArrayList<>();
//        mMessageComparator = new Comparator<SdvnMessage>() {
//            @Override
//            public int compare(@NonNull SdvnMessage o1, @NonNull SdvnMessage o2) {
//                try {
//                    long o1Timestamp, o2Timestamp;
//                    if (o1.getTimestamp() > 0)
//                        o1Timestamp = o1.getTimestamp();
//                    else
//                        o1Timestamp = sdf.parse(o1.getDate()).getTime() / 1000;
//                    if (o2.getTimestamp() > 0)
//                        o2Timestamp = o2.getTimestamp();
//                    else
//                        o2Timestamp = sdf.parse(o2.getDate()).getTime() / 1000;
//                    if (o1Timestamp == o2Timestamp) return 0;
//                    return o1Timestamp > o2Timestamp ? -1 : 1;
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                return 0;
//            }
//        };
//
//        HandlerThread mHandlerThread = new HandlerThread("Thread MessageManager");
//        mHandlerThread.start();
//        threadHandler = new Handler(mHandlerThread.getLooper());
////        threadHandler.postDelayed(loopGetMessageRunnable, 1000);
//    }
//
//    public static SdvnMessageManager getInstance() {
//        if (sInstance == null) {
//            synchronized (SdvnMessageManager.class) {
//                if (sInstance == null) {
//                    sInstance = new SdvnMessageManager();
//                }
//            }
//        }
//        return sInstance;
//    }
//
//    public void startOrStopGetMsg(boolean start) {
//        if (isGoing == start) return;
//        this.isGoing = start;
//        if (start) {
//
//            Log.d(TAG, "startGetMsg");
//            threadHandler.postDelayed(loopGetMessageRunnable, 60 * 1000);
//            loadMessage();
//            String count = LocalMsgStore.getString(OkHttpClientIns.getContext(),
//                    LocalMsgStore.MESSAGE_COUNT_KEY, "");
//            if (!TextUtils.isEmpty(count)) {
//                messagesCount = Integer.parseInt(count);
//            }
//        } else {
//            Log.d(TAG, "StopGetMsg");
//            if (mGetNewsHttpLoader != null) {
//                mGetNewsHttpLoader.cancel();
//            }
//            threadHandler.removeCallbacks(loopGetMessageRunnable);
//            if (messages != null) {
//                messages.clear();
//            }
//        }
//    }
//
//    public void getNews() {
//        if (CMAPI.getInstance().getBaseInfo().getStatus() != Constants.CS_CONNECTED) return;
//        mGetNewsHttpLoader = new GetNewsHttpLoader(SdvnMessageList.class);
//        mGetNewsHttpLoader.setParams(OkHttpClientIns.getContext(), LocalMsgStore.getMessageNewDate(),
//                LocalMsgStore.getMessageTimestamp());
//        mGetNewsHttpLoader.executor(new CommonResultListener<SdvnMessageList>() {
//            @Override
//            public void success(Object tag, @NonNull SdvnMessageList data) {
//                updateMessages(data.getNewslist());
//            }
//
//            @Override
//            public void error(Object tag, GsonBaseProtocol baseProtocol) {
//                Log.e(TAG, "OkHttp返回数据:" + new Gson().toJson(baseProtocol));
//            }
//        });
//    }
//
//    private synchronized void updateMessages(@NonNull List<SdvnMessage> messages) {
//        int newCount = 0;
////        String newDate = "";
//        long newTimestamp = -1;
//
//        a:
//        for (SdvnMessage message : messages) {
//            if (newTimestamp < 0) {
//                if (message.getTimestamp() > 0)
//                    newTimestamp = message.getTimestamp();
//                else {
//                    try {
//                        newTimestamp = sdf.parse(message.getDate()).getTime() / 1000;
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else {
//                try {
//                    long timestamp = 0;
//                    if (message.getTimestamp() > 0)
//                        timestamp = message.getTimestamp();
//                    else {
//                        timestamp = sdf.parse(message.getDate()).getTime() / 1000;
//                    }
//
//                    newTimestamp = timestamp > newTimestamp ? timestamp : newTimestamp;
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//            for (SdvnMessage sdvnMessage : this.messages) {
//                if (Objects.equals(sdvnMessage.getNewsid(), message.getNewsid())) {
//                    sdvnMessage.setStatus(message.getStatus());
//                    continue a;
//                }
//            }
//            newCount++;
//            this.messages.add(message);
//        }
//        messagesCount += newCount;
////        if (!TextUtils.isEmpty(newDate))
////            saveDate(newDate);
////
//        if (newTimestamp != -1) {
//            LocalMsgStore.saveMessageTimestamp(newTimestamp);
//        }
//        Collections.sort(this.messages, mMessageComparator);
//        if (newCount > 0) {
//            saveMessage();
//            notifyMessagesListObserver(this.messages);
//        }
//        updateCountStore();
//
//    }
//
//    private void updateCountStore() {
//        LocalMsgStore.saveString(OkHttpClientIns.getContext(), LocalMsgStore.MESSAGE_COUNT_KEY, String.valueOf(messagesCount));
//    }
//
//    public synchronized void setMessagesStatus(String newsid, String status) {
//        for (SdvnMessage sdvnMessage : messages) {
//            if (Objects.equals(sdvnMessage.getNewsid(), newsid)) {
//                sdvnMessage.setStatus(status);
//                saveMessage();
//                notifyMessagesListObserver(messages);
//                break;
//            }
//        }
//    }
//
//    public synchronized void removeMessages(@NonNull List<String> ids) {
//        Iterator<SdvnMessage> iterator = messages.iterator();
//        a:
//        while (iterator.hasNext()) {
//            SdvnMessage next = iterator.next();
//            for (String id : ids) {
//                if (Objects.equals(next.getNewsid(), id)) {
//                    iterator.remove();
//                    continue a;
//                }
//            }
//        }
//        saveMessage();
//        notifyMessagesListObserver(messages);
//    }
//
//    public synchronized void loadMessage() {
//        messages.clear();
//        messages.addAll(LocalMsgStore.loadMessage());
//        Collections.sort(messages, mMessageComparator);
//    }
//
//    private void saveMessage() {
//        LocalMsgStore.saveMessage(messages);
//    }
//
//    private void saveDate(String date) {
//        LocalMsgStore.saveMessageNewDate(date);
//    }
//
//    public void clearMessagesCount() {
//        messagesCount = 0;
//        updateCountStore();
//        notifyMessagesListObserver(messages);
//    }
//
//    public List<SdvnMessage> getMessages() {
//        return messages;
//    }
//
//    public synchronized void addMessagesListObserver(@Nullable MessagesListObserver o) {
//        synchronized (messagesListLock) {
//            if (o == null)
//                throw new NullPointerException();
//            if (!messagesListObservers.containsKey(o)) {
//                messagesListObservers.put(o, 0);
//                o.onMessagesListChanged(messagesCount, this.messages);
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
//
//    /**
//     * 消息观察者
//     */
//    public interface MessagesListObserver {
//        void onMessagesListChanged(int newCount, List<SdvnMessage> messages);
//    }
//}
