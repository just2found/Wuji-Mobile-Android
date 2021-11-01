package net.sdvn.nascommon.db;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import net.sdvn.nascommon.db.objecbox.SafeBoxTransferHistory;
import net.sdvn.nascommon.db.objecbox.SafeBoxTransferHistory_;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.log.Logger;

import org.jetbrains.annotations.NotNull;
import org.view.libwidget.log.L;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.android.ObjectBoxLiveData;
import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;


/**
 * Created by gaoyun@eli-tech.com on 2016/02/26.
 */
public class SafeBoxTransferHistoryKeeper {

    public static int getTransferType(boolean isDownload) {
        if (isDownload) {
            return SafeBoxTransferHistory.TYPE_DOWNLOAD;
        }
        return SafeBoxTransferHistory.TYPE_UPLOAD;
    }

    /**
     * List All Transfer History
     *
     * @return transfer list
     */
    public static List<SafeBoxTransferHistory> all(boolean isDownload, boolean isComplete) {
//        Log.e("SafeBoxTransferHistoryKeeper", ">>>>Query SafeBoxTransferHistory, UID=" + uid + ", Download=" + isDownload);
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
        queryBuilder.equal(SafeBoxTransferHistory_.type, (getTransferType(isDownload)));
        queryBuilder.equal(SafeBoxTransferHistory_.isComplete, (isComplete));
        if (isComplete)
            queryBuilder.orderDesc(SafeBoxTransferHistory_.time);
        else
            queryBuilder.order(SafeBoxTransferHistory_.time);
        return queryBuilder.build().find();
    }

    @NotNull
    public static QueryBuilder<SafeBoxTransferHistory> getSafeBoxTransferHistoryQueryBuilder() {
        Box<SafeBoxTransferHistory> dao = getSafeBoxTransferHistoryBox();
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = dao.query();
        queryBuilder.equal(SafeBoxTransferHistory_.userId, DBHelper.getAccount());
        return queryBuilder;
    }

    private static Box<SafeBoxTransferHistory> getSafeBoxTransferHistoryBox() {
        return DBHelper.getBoxStore().boxFor(SafeBoxTransferHistory.class);
    }

    /**
     * List All Transfer History
     *
     * @return transfer list
     */
    public static List<SafeBoxTransferHistory> all(String devId, boolean isDownload, boolean isComplete) {
//        Log.e("SafeBoxTransferHistoryKeeper", ">>>>Query SafeBoxTransferHistory, UID=" + uid + ", Download=" + isDownload);
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
        queryBuilder.equal(SafeBoxTransferHistory_.userId, DBHelper.getAccount());
        if (!TextUtils.isEmpty(devId))
            queryBuilder.equal(SafeBoxTransferHistory_.srcDevId, (devId));
        queryBuilder.equal(SafeBoxTransferHistory_.type, (getTransferType(isDownload)));
        queryBuilder.equal(SafeBoxTransferHistory_.isComplete, (isComplete));
        queryBuilder.orderDesc(SafeBoxTransferHistory_.id);

        return queryBuilder.build().find();
    }

    public static List<SafeBoxTransferHistory> allToManager(String devId, boolean isDownload, Long limit) {
//        Log.e("SafeBoxTransferHistoryKeeper", ">>>>Query SafeBoxTransferHistory, UID=" + uid + ", Download=" + isDownload);
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
        queryBuilder.equal(SafeBoxTransferHistory_.userId, DBHelper.getAccount());
        if (!TextUtils.isEmpty(devId))
            queryBuilder.notEqual(SafeBoxTransferHistory_.srcDevId, (devId));
        queryBuilder.equal(SafeBoxTransferHistory_.type, (getTransferType(isDownload)));
        queryBuilder.equal(SafeBoxTransferHistory_.isComplete, (false));
        queryBuilder.equal(SafeBoxTransferHistory_.state, TransferState.START.name()).or().equal(SafeBoxTransferHistory_.state, TransferState.WAIT.name());
        queryBuilder.order(SafeBoxTransferHistory_.id);
        return queryBuilder.build().find(0, limit);
    }


    public static List<SafeBoxTransferHistory> all(String devId, boolean isDownload, boolean isComplete, Long offset, Long limit) {
//        Log.e("SafeBoxTransferHistoryKeeper", ">>>>Query SafeBoxTransferHistory, UID=" + uid + ", Download=" + isDownload);
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
        queryBuilder.equal(SafeBoxTransferHistory_.userId, DBHelper.getAccount());
        if (!TextUtils.isEmpty(devId))
            queryBuilder.equal(SafeBoxTransferHistory_.srcDevId, (devId));
        queryBuilder.equal(SafeBoxTransferHistory_.type, (getTransferType(isDownload)));
        queryBuilder.equal(SafeBoxTransferHistory_.isComplete, (isComplete));
        if (isComplete) {
            queryBuilder.orderDesc(SafeBoxTransferHistory_.id);
        } else {
            queryBuilder.order(SafeBoxTransferHistory_.id);
        }
        return queryBuilder.build().find(offset, limit);
    }

    public static Long allCount(String devId, boolean isDownload, boolean isComplete) {
//        Log.e("SafeBoxTransferHistoryKeeper", ">>>>Query SafeBoxTransferHistory, UID=" + uid + ", Download=" + isDownload);
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
        queryBuilder.equal(SafeBoxTransferHistory_.userId, DBHelper.getAccount());
        if (!TextUtils.isEmpty(devId))
            queryBuilder.equal(SafeBoxTransferHistory_.srcDevId, (devId));
        queryBuilder.equal(SafeBoxTransferHistory_.type, (getTransferType(isDownload)));
        queryBuilder.equal(SafeBoxTransferHistory_.isComplete, (isComplete));


        return queryBuilder.build().count();
    }


    @Nullable
    public static SafeBoxTransferHistory query(int transferType, @NonNull String devId, @NonNull String srcPath,
                                               @NonNull String srcName, @NonNull String toPath) {
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
        queryBuilder.equal(SafeBoxTransferHistory_.srcDevId, (devId));
        queryBuilder.equal(SafeBoxTransferHistory_.type, (transferType));
        queryBuilder.equal(SafeBoxTransferHistory_.srcPath, (srcPath));
        queryBuilder.equal(SafeBoxTransferHistory_.toPath, (toPath));
        queryBuilder.equal(SafeBoxTransferHistory_.name, (srcName));
        return queryBuilder.build().findFirst();
    }

    //只是更新状态，开始暂停
    public static SafeBoxTransferHistory queryById(long id) {
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
        queryBuilder.equal(SafeBoxTransferHistory_.id, id);
        return queryBuilder.build().findFirst();
    }


    //只是更新状态，开始暂停
    public static boolean updateStateById(long id, @NonNull TransferState state) {
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
        queryBuilder.equal(SafeBoxTransferHistory_.id, id);
        SafeBoxTransferHistory SafeBoxtransferHistory = queryBuilder.build().findFirst();
        if (SafeBoxtransferHistory != null) {
            SafeBoxtransferHistory.setState(state);
            return getSafeBoxTransferHistoryBox().put(SafeBoxtransferHistory) == id;
        }
        return false;
    }


    //只是更新状态，开始暂停   非完成的
    public static boolean updateStateByDevId(@Nullable String devId, @NonNull TransferState state, boolean isDownload) {
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
        if (!TextUtils.isEmpty(devId)) {
            queryBuilder.equal(SafeBoxTransferHistory_.srcDevId, devId);
        }
        queryBuilder.equal(SafeBoxTransferHistory_.isComplete, false);
        queryBuilder.equal(SafeBoxTransferHistory_.type, getTransferType(isDownload));
        queryBuilder.notEqual(SafeBoxTransferHistory_.state, state.name());
        Query<SafeBoxTransferHistory> query = queryBuilder.build();
        List<SafeBoxTransferHistory> list = null;
        Box<SafeBoxTransferHistory> historyBox = getSafeBoxTransferHistoryBox();
        long offset = 0;
        long limit = 800;
        long countF = 0;
        L.i("count：" + query.count() + " " + Thread.currentThread(), "updateStateByDevId", "SafeBoxTransferHistoryKeeper", "nwq", "2021/3/18");
        while ((list = query.find(offset, limit)).size() > 0) {
            for (SafeBoxTransferHistory history : list) {
                history.setState(state);
            }
            historyBox.put(list);
            countF += list.size();
            System.gc();
        }
        return false;
    }


    //根据状态删除
    public static boolean deleteByState(@Nullable String devId, @NonNull TransferState state, boolean isDownload) {
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
        if (!TextUtils.isEmpty(devId)) {
            queryBuilder.equal(SafeBoxTransferHistory_.srcDevId, devId);
        }
        queryBuilder.equal(SafeBoxTransferHistory_.type, getTransferType(isDownload));
        queryBuilder.equal(SafeBoxTransferHistory_.state, state.name());
        Query<SafeBoxTransferHistory> query = queryBuilder.build();
        Boolean flag = false;
        if (query.remove() > 0) {
            flag = true;
        }
        query.publish();
        return flag;
    }


    //清除所有正在进行的任务
    public static boolean deleteByRun(@Nullable String devId, boolean isDownload) {
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
        if (!TextUtils.isEmpty(devId)) {
            queryBuilder.equal(SafeBoxTransferHistory_.srcDevId, devId);
        }
        queryBuilder.equal(SafeBoxTransferHistory_.type, getTransferType(isDownload));
        queryBuilder.equal(SafeBoxTransferHistory_.isComplete, (false));
        Query<SafeBoxTransferHistory> query = queryBuilder.build();
        Boolean flag = false;
        if (query.remove() > 0) {
            flag = true;
        }
        query.publish();
        return flag;
    }

    public static long insert(@Nullable SafeBoxTransferHistory history) {
        if (null == history) {
            return -1;
        }
        history.setUserId(DBHelper.getAccount());
        Box<SafeBoxTransferHistory> dao = getSafeBoxTransferHistoryBox();
        return dao.put(history);
    }

    public static boolean deleteComplete(String devId) {
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
        queryBuilder.equal(SafeBoxTransferHistory_.srcDevId, (devId));
        queryBuilder.equal(SafeBoxTransferHistory_.isComplete, (true));
        queryBuilder.build().remove();
        return true;
    }

    public static boolean deleteComplete(@Nullable String devId, boolean isDownload) {
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
//        queryBuilder.where(SafeBoxTransferHistoryDao.Properties.Uid.eq(uid));
        if (!TextUtils.isEmpty(devId))
            queryBuilder.equal(SafeBoxTransferHistory_.srcDevId, (devId));
        queryBuilder.equal(SafeBoxTransferHistory_.isComplete, (true));
        queryBuilder.equal(SafeBoxTransferHistory_.type, (getTransferType(isDownload)));
        final Query<SafeBoxTransferHistory> query = queryBuilder.build();
        Logger.LOGD("deleteComplete", "count : ", query.count(), " :: ", System.currentTimeMillis());
        final long remove = query.remove();
        Logger.LOGD("deleteComplete", "remove : ", remove, " :: ", System.currentTimeMillis());
        query.publish();
        return true;
    }

    public static boolean delete(@Nullable SafeBoxTransferHistory history) {
        if (null == history) {
            return false;
        }
        Box<SafeBoxTransferHistory> dao = getSafeBoxTransferHistoryBox();
        return dao.remove(history.getId());
    }

    public static boolean delete(@Nullable long... ids) {
        Box<SafeBoxTransferHistory> dao = getSafeBoxTransferHistoryBox();
        dao.remove(ids);
        return true;
    }

    public static boolean update(@Nullable SafeBoxTransferHistory history) {
        if (null == history) {
            return false;
        }
        history.setUserId(DBHelper.getAccount());
        Box<SafeBoxTransferHistory> dao = getSafeBoxTransferHistoryBox();
        dao.put(history);
        return true;
    }



    public static boolean clear(String devId) {
        Box<SafeBoxTransferHistory> dao = getSafeBoxTransferHistoryBox();
        dao.query().equal(SafeBoxTransferHistory_.userId, DBHelper.getAccount())
                .equal(SafeBoxTransferHistory_.srcDevId, devId)
                .build()
                .remove();
        return true;
    }

    public static boolean clear() {
        Box<SafeBoxTransferHistory> dao = getSafeBoxTransferHistoryBox();
        dao.query().equal(SafeBoxTransferHistory_.userId, DBHelper.getAccount())
                .build()
                .remove();
        return true;
    }

    public static boolean delete(long id) {
        Box<SafeBoxTransferHistory> dao = getSafeBoxTransferHistoryBox();
        return dao.remove(id);

    }

    @NonNull
    public static LiveData<List<SafeBoxTransferHistory>> getLiveDataIncomplete(@Nullable String devId) {
        QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
        if (EmptyUtils.isNotEmpty(devId))
            queryBuilder.equal(SafeBoxTransferHistory_.srcDevId, devId);
        queryBuilder.equal(SafeBoxTransferHistory_.isComplete, false);
        return new ObjectBoxLiveData<>(queryBuilder.build());
    }

    public static void delete(List<Long> list) {
        Box<SafeBoxTransferHistory> dao = getSafeBoxTransferHistoryBox();
        dao.removeByIds(list);
    }

    public static void update(List<SafeBoxTransferHistory> histories) {
        Box<SafeBoxTransferHistory> dao = getSafeBoxTransferHistoryBox();
        String userId = DBHelper.getAccount();
        for (SafeBoxTransferHistory history : histories) {
            history.setUserId(userId);
        }
        dao.put(histories);
    }

    public static class QuerySafeBoxTransferHistory {
        private boolean mIsDownload;
        private Boolean mIsComplete;

        public QuerySafeBoxTransferHistory(boolean isDownload, Boolean isComplete) {
            mIsDownload = isDownload;
            mIsComplete = isComplete;
        }

        public Query<SafeBoxTransferHistory> invoke() {
            QueryBuilder<SafeBoxTransferHistory> queryBuilder = getSafeBoxTransferHistoryQueryBuilder();
            queryBuilder.equal(SafeBoxTransferHistory_.type, (getTransferType(mIsDownload)));
            if (mIsComplete != null)
                queryBuilder.equal(SafeBoxTransferHistory_.isComplete, (mIsComplete));
            queryBuilder.order(SafeBoxTransferHistory_.isComplete)
                    .order(SafeBoxTransferHistory_.status)
                    .order(SafeBoxTransferHistory_.id);

            return queryBuilder.build();
        }
    }

}
