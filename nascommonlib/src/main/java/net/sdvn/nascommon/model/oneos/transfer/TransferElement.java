package net.sdvn.nascommon.model.oneos.transfer;

import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.utils.log.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import io.objectbox.annotation.BaseEntity;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.NameInDb;
import io.objectbox.annotation.Transient;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/18.
 */
@Keep
@BaseEntity
public abstract class TransferElement {
    //    public static final int NONE = 0;         //无状态
//    public static final int WAIT = 1;      //等待
//    public static final int START_CHANNEL = 2;      //下载中
//    public static final int PAUSE = 3;        //暂停
//    public static final int FAILED = 4;        //错误
//    public static final int COMPLETE = 5;       //完成
    @Transient
    public static final long REFRESH_TIME = 250;
    @Transient
    public static final long REFRESH_BYTES_SIZE = 1024 * 512;
    @Transient
    private static final String TAG = TransferElement.class.getSimpleName();
    @Nullable
    protected Long groupId;

    @Id
    protected long id;
    /**
     * Transmission url
     */
    @Transient
    protected transient String url;

    @Transient
    protected transient Uri thumbUri;
    /**
     * Transmission source file path
     */
    protected String srcPath;
    /**
     * Transmission file target path
     */
    protected String toPath;
    /**
     * Source file size
     */
    protected long size;
    /**
     * Seek offset
     */
    @Transient
    protected long offset;
    /**
     * Transmitted length
     */
    protected long length;
    /**
     * Transmission end time
     */
    @Transient
    protected long time;
    /**
     * Transmission state
     */
    @Convert(converter = TransferState.TransferStateConverter.class, dbType = String.class)
    protected TransferState state = TransferState.NONE;
    /**
     * Transmission exception
     */
    @Transient
    protected TransferException exception = TransferException.NONE;
    @Transient
    private transient long lastRefreshTime;
    @Transient
    private transient long mSpeed;
    private transient int priority;
    @NameInDb("toId")
    protected String devId;

    private String tag;

    private int feature;

    @Nullable
    protected String tmpName;
    protected boolean check;


    public TransferElement(long id) {
        this.id = id;
    }

    /**
     * Whether is download file
     *
     * @return true or false
     */
    public abstract boolean isDownload();

    /**
     * Get transmission source file path
     */
    public abstract String getSrcPath();

    /**
     * Get transmission source file name
     */
    public abstract String getSrcName();

    /**
     * Get transmission source file size
     */
    public abstract long getSize();

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferElement that = (TransferElement) o;
        return getSize() == that.getSize() &&
                Objects.equals(getSrcPath(), that.getSrcPath()) &&
                Objects.equals(getToPath(), that.getToPath()) &&
                Objects.equals(getDevId(), that.getDevId());
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        hashCode ^= (int) (getSize() ^ (getSize() >>> 32));
        hashCode ^= (TextUtils.isEmpty(getToPath()) ? 0 : getToPath().hashCode());
        hashCode ^= (TextUtils.isEmpty(getSrcPath()) ? 0 : getSrcPath().hashCode());
        hashCode ^= (TextUtils.isEmpty(getDevId()) ? 0 : getDevId().hashCode());
        return hashCode;
    }

    @NonNull
    private transient List<Long> speedBuffer       //网速做平滑的缓存，避免抖动过快
            = new ArrayList<>();

    /**
     * 平滑网速，避免抖动过大
     */
    private long bufferSpeed(long speed) {
        speedBuffer.add(speed);
        if (speedBuffer.size() > 10) {
            speedBuffer.remove(0);
        }
        long sum = 0;
        for (float speedTemp : speedBuffer) {
            sum += speedTemp;
        }
        return sum / speedBuffer.size();
    }

    private transient long mSpeedSampleStart, mSpeedSampleBytes, mLastUpdateBytes, mLastUpdateTime;
    public static final int MIN_PROGRESS_STEP = 65536, MIN_PROGRESS_TIME = 2000;

    private void update(final long currentBytes) {
        final long now = SystemClock.elapsedRealtime();
        final long sampleDelta = now - mSpeedSampleStart;
        if (sampleDelta > 500) {
            final long sampleSpeed = ((currentBytes - mSpeedSampleBytes) * 1000)
                    / sampleDelta;

            if (mSpeed == 0) {
                mSpeed = sampleSpeed;
            } else {
                mSpeed = ((mSpeed * 3) + sampleSpeed) / 4;
            }

            // Only notify once we have a full sample window
            if (mSpeedSampleStart != 0) {
                notifyChange();
            }
            mSpeedSampleStart = now;
            mSpeedSampleBytes = currentBytes;
        }
        final long bytesDelta = currentBytes - mLastUpdateBytes;
        final long timeDelta = now - mLastUpdateTime;
        // 65536 2000
        if (bytesDelta > MIN_PROGRESS_STEP && timeDelta > MIN_PROGRESS_TIME) {
            mLastUpdateBytes = currentBytes;
            mLastUpdateTime = now;
        }
    }

    // ===============getter and setter method======================
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getToPath() {
        return toPath;
    }

    public void setToPath(String toPath) {
        this.toPath = toPath;
    }

    /**
     * Transmitted length
     */
    public long getLength() {
        return length;
    }

//    private transient long tempSize;

    /**
     * Transmitted length
     */
    public void setLength(long length) {
        if (length > this.length) {
//            tempSize += length - this.length;
//        if (isDownload()) {
            long currentTime = System.currentTimeMillis();
            boolean isNotify = (currentTime - this.lastRefreshTime) >= REFRESH_TIME;
            if (isNotify || length == size) {
//                if (this.lastRefreshTime > 0) {
//                    long diffTime = currentTime - this.lastRefreshTime;
//                    if (diffTime == 0) diffTime = 1;
//                    this.mSpeed = bufferSpeed(tempSize * 1000 / diffTime);
//                }
//                tempSize = 0;
                this.lastRefreshTime = currentTime;
                notifyChange();
            }
//            Logger.p(Logger.Level.WARN, Logger.Logd.UPLOAD, TAG, "diffTime= " + (currentTime - this.lastRefreshTime));
//        }
            update(length);
        }
        this.length = length;
    }//        } else {
//            if (tempSize >= OneOSUploadFileAPI.HTTP_BUFFER_SIZE * 16) {
//                notifyChang();
//                tempSize = 0;
//            }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public TransferState getState() {
        return state;
    }

    public void setState(TransferState state) {
        if (this.state != state) {
            this.state = state;
            notifyChange();
        }
    }

    public TransferException getException() {
        return exception;
    }

    public void setException(TransferException exception) {
        this.exception = exception;
        notifyChange();
    }


    public Uri getThumbUri() {
        return thumbUri;
    }

    public void setThumbUri(Uri thumbUrl) {
        this.thumbUri = thumbUrl;
    }

    public long getSpeed() {
        return mSpeed;
    }

    public void setSpeed(long speed) {
        this.mSpeed = speed;
    }

    private void notifyChange() {
//        Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "Transfer notifyChang");
        if (mObservers != null) {
            for (WeakReference<TransferStateObserver> observer : mObservers) {
                if (observer != null && observer.get() != null) {
                    observer.get().onChanged(getTag());
                }
            }
            Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "Transfer notifyChang Observers size : " + mObservers.size());
        }
    }

    private transient Set<WeakReference<TransferStateObserver>> mObservers;

    public void addTransferStateObserver(@NonNull TransferStateObserver observer) {
        if (mObservers == null) {
            mObservers = new CopyOnWriteArraySet<>();
        }
        this.mObservers.add(new WeakReference<>(observer));
        Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "$observer: " + observer);

    }

    public void removeTransferStateObserver(@NonNull TransferStateObserver observer) {
        if (mObservers != null) {
            for (WeakReference<TransferStateObserver> reference : mObservers) {
                if (reference.get() == observer) {
                    mObservers.remove(reference);
                    Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "remove observer: " + observer);
                    break;
                }
            }
        }

    }

    public String getDevId() {
        return this.devId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public abstract String getTag();

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getFeature() {
        return feature;
    }

    public void setFeature(int feature) {
        this.feature = feature;
    }

    public int orFeature(int fea) {
        return feature = feature | fea;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public boolean isCheck() {
        return check;
    }

    public interface TransferStateObserver {
        void onChanged(Object tag);
    }

    // ===============getter and setter method======================
    public void setTmpName(String tmpName) {
        this.tmpName = tmpName;
    }

    public String getTmpName() {
        return tmpName;
    }

    @Nullable
    public Long getGroupId(){
        return groupId;
    }

    public void setGroup(@Nullable Long groupId) {
        this.groupId = groupId;
    }
}
