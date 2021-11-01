package net.sdvn.nascommon.db;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import net.sdvn.nascommon.db.objecbox.TransferHistory;
import net.sdvn.nascommon.db.objecbox.TransferHistory_;
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
public class TransferHistoryKeeper {

    public static int getTransferType(boolean isDownload) {
        if (isDownload) {
            return TransferHistory.TYPE_DOWNLOAD;
        }
        return TransferHistory.TYPE_UPLOAD;
    }

    /**
     * List All Transfer History
     *
     * @return transfer list
     */
    public static List<TransferHistory> all(boolean isDownload, boolean isComplete) {
//        Log.e("TransferHistoryKeeper", ">>>>Query TransferHistory, UID=" + uid + ", Download=" + isDownload);
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        queryBuilder.equal(TransferHistory_.type, (getTransferType(isDownload)));
        queryBuilder.equal(TransferHistory_.isComplete, (isComplete));
        if (isComplete)
            queryBuilder.orderDesc(TransferHistory_.time);
        else
            queryBuilder.order(TransferHistory_.time);
        return queryBuilder.build().find();
    }

    @NotNull
    public static QueryBuilder<TransferHistory> getTransferHistoryQueryBuilder() {
        Box<TransferHistory> dao = getTransferHistoryBox();
        QueryBuilder<TransferHistory> queryBuilder = dao.query();
        queryBuilder.equal(TransferHistory_.userId, DBHelper.getAccount());
        return queryBuilder;
    }

    private static Box<TransferHistory> getTransferHistoryBox() {
        return DBHelper.getBoxStore().boxFor(TransferHistory.class);
    }

    /**
     * List All Transfer History
     *
     * @return transfer list
     */
    public static List<TransferHistory> all(String devId, boolean isDownload, boolean isComplete) {
//        Log.e("TransferHistoryKeeper", ">>>>Query TransferHistory, UID=" + uid + ", Download=" + isDownload);
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        queryBuilder.equal(TransferHistory_.userId, DBHelper.getAccount());
        if (!TextUtils.isEmpty(devId))
            queryBuilder.equal(TransferHistory_.srcDevId, (devId));
        queryBuilder.equal(TransferHistory_.type, (getTransferType(isDownload)));
        queryBuilder.equal(TransferHistory_.isComplete, (isComplete));
        queryBuilder.orderDesc(TransferHistory_.id);

        return queryBuilder.build().find();
    }

    public static List<TransferHistory> allToManager(String devId, boolean isDownload, Long limit) {
//        Log.e("TransferHistoryKeeper", ">>>>Query TransferHistory, UID=" + uid + ", Download=" + isDownload);
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        queryBuilder.equal(TransferHistory_.userId, DBHelper.getAccount());
        if (!TextUtils.isEmpty(devId))
            queryBuilder.notEqual(TransferHistory_.srcDevId, (devId));
        queryBuilder.equal(TransferHistory_.type, (getTransferType(isDownload)));
        queryBuilder.equal(TransferHistory_.isComplete, (false));
        queryBuilder.equal(TransferHistory_.state, TransferState.START.name()).or().equal(TransferHistory_.state, TransferState.WAIT.name());
        queryBuilder.order(TransferHistory_.id);
        return queryBuilder.build().find(0, limit);
    }


    public static List<TransferHistory> all(String devId, boolean isDownload, boolean isComplete, Long offset, Long limit) {
//        Log.e("TransferHistoryKeeper", ">>>>Query TransferHistory, UID=" + uid + ", Download=" + isDownload);
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        queryBuilder.equal(TransferHistory_.userId, DBHelper.getAccount());
        if (!TextUtils.isEmpty(devId))
            queryBuilder.equal(TransferHistory_.srcDevId, (devId));
        queryBuilder.equal(TransferHistory_.type, (getTransferType(isDownload)));
        queryBuilder.equal(TransferHistory_.isComplete, (isComplete));
        if (isComplete) {
            queryBuilder.orderDesc(TransferHistory_.id);
        } else {
            queryBuilder.order(TransferHistory_.id);
        }
        return queryBuilder.build().find(offset, limit);
    }

    public static Long allCount(String devId, boolean isDownload, boolean isComplete) {
//        Log.e("TransferHistoryKeeper", ">>>>Query TransferHistory, UID=" + uid + ", Download=" + isDownload);
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        queryBuilder.equal(TransferHistory_.userId, DBHelper.getAccount());
        if (!TextUtils.isEmpty(devId))
            queryBuilder.equal(TransferHistory_.srcDevId, (devId));
        queryBuilder.equal(TransferHistory_.type, (getTransferType(isDownload)));
        queryBuilder.equal(TransferHistory_.isComplete, (isComplete));


        return queryBuilder.build().count();
    }


    @Nullable
    public static TransferHistory query(int transferType, @NonNull String devId, @NonNull String srcPath,
                                        @NonNull String srcName, @NonNull String toPath) {
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        queryBuilder.equal(TransferHistory_.srcDevId, (devId));
        queryBuilder.equal(TransferHistory_.type, (transferType));
        queryBuilder.equal(TransferHistory_.srcPath, (srcPath));
        queryBuilder.equal(TransferHistory_.toPath, (toPath));
        queryBuilder.equal(TransferHistory_.name, (srcName));
        return queryBuilder.build().findFirst();
    }

    //只是更新状态，开始暂停
    public static TransferHistory queryById(long id) {
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        queryBuilder.equal(TransferHistory_.id, id);
        return queryBuilder.build().findFirst();
    }


    //只是更新状态，开始暂停
    public static boolean updateStateById(long id, @NonNull TransferState state) {
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        queryBuilder.equal(TransferHistory_.id, id);
        TransferHistory transferHistory = queryBuilder.build().findFirst();
        if (transferHistory != null) {
            transferHistory.setState(state);
            return getTransferHistoryBox().put(transferHistory) == id;
        }
        return false;
    }


    //只是更新状态，开始暂停   非完成的
    public static boolean updateStateByDevId(@Nullable String devId, @NonNull TransferState state, boolean isDownload) {
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        if (!TextUtils.isEmpty(devId)) {
            queryBuilder.equal(TransferHistory_.srcDevId, devId);
        }
        queryBuilder.equal(TransferHistory_.isComplete, false);
        queryBuilder.equal(TransferHistory_.type, getTransferType(isDownload));
        queryBuilder.notEqual(TransferHistory_.state, state.name());
        Query<TransferHistory> query = queryBuilder.build();
        List<TransferHistory> list = null;
        Box<TransferHistory> historyBox = getTransferHistoryBox();
        long offset = 0;
        long limit = 800;
        long countF = 0;
        L.i("count：" + query.count() + " " + Thread.currentThread(), "updateStateByDevId", "TransferHistoryKeeper", "nwq", "2021/3/18");
        while ((list = query.find(offset, limit)).size() > 0) {
            for (TransferHistory history : list) {
                history.setState(state);
            }
            historyBox.put(list);
            countF += list.size();
            L.i("count :  " + countF, "updateStateByDevId", "TransferHistoryKeeper", "nwq", "2021/3/18");
            list = null;
            System.gc();
        }
        return false;
    }


    //根据状态删除
    public static boolean deleteByState(@Nullable String devId, @NonNull TransferState state, boolean isDownload) {
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        if (!TextUtils.isEmpty(devId)) {
            queryBuilder.equal(TransferHistory_.srcDevId, devId);
        }
        queryBuilder.equal(TransferHistory_.type, getTransferType(isDownload));
        queryBuilder.equal(TransferHistory_.state, state.name());
        Query<TransferHistory> query = queryBuilder.build();
        Boolean flag = false;
        if (query.remove() > 0) {
            flag = true;
        }
        query.publish();
        return flag;
    }


    //清除所有正在进行的任务
    public static boolean deleteByRun(@Nullable String devId, boolean isDownload) {
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        if (!TextUtils.isEmpty(devId)) {
            queryBuilder.equal(TransferHistory_.srcDevId, devId);
        }
        queryBuilder.equal(TransferHistory_.type, getTransferType(isDownload));
        queryBuilder.equal(TransferHistory_.isComplete, (false));
        Query<TransferHistory> query = queryBuilder.build();
        Boolean flag = false;
        if (query.remove() > 0) {
            flag = true;
        }
        query.publish();
        return flag;
    }

    public static long insert(@Nullable TransferHistory history) {
        if (null == history) {
            return -1;
        }
        history.setUserId(DBHelper.getAccount());
        Box<TransferHistory> dao = getTransferHistoryBox();
        return dao.put(history);
    }

    public static boolean deleteComplete(String devId) {
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        queryBuilder.equal(TransferHistory_.srcDevId, (devId));
        queryBuilder.equal(TransferHistory_.isComplete, (true));
        queryBuilder.build().remove();
        return true;
    }

    public static boolean deleteComplete(@Nullable String devId, boolean isDownload) {
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
//        queryBuilder.where(TransferHistoryDao.Properties.Uid.eq(uid));
        if (!TextUtils.isEmpty(devId))
            queryBuilder.equal(TransferHistory_.srcDevId, (devId));
        queryBuilder.equal(TransferHistory_.isComplete, (true));
        queryBuilder.equal(TransferHistory_.type, (getTransferType(isDownload)));
        final Query<TransferHistory> query = queryBuilder.build();
        Logger.LOGD("deleteComplete", "count : ", query.count(), " :: ", System.currentTimeMillis());
        final long remove = query.remove();
        Logger.LOGD("deleteComplete", "remove : ", remove, " :: ", System.currentTimeMillis());
        query.publish();
        return true;
    }

    public static boolean delete(@Nullable TransferHistory history) {
        if (null == history) {
            return false;
        }
        Box<TransferHistory> dao = getTransferHistoryBox();
        return dao.remove(history.getId());
    }

    public static boolean delete(@Nullable long... ids) {
        Box<TransferHistory> dao = getTransferHistoryBox();
        dao.remove(ids);
        return true;
    }

    public static boolean update(@Nullable TransferHistory history) {
        if (null == history) {
            return false;
        }
        history.setUserId(DBHelper.getAccount());
        Box<TransferHistory> dao = getTransferHistoryBox();
        dao.put(history);
        return true;
    }

    public static boolean clear() {
        Box<TransferHistory> dao = getTransferHistoryBox();
        dao.query().equal(TransferHistory_.userId, DBHelper.getAccount())
                .build()
                .remove();
        return true;
    }

    public static boolean delete(long id) {
        Box<TransferHistory> dao = getTransferHistoryBox();
        return dao.remove(id);

    }

    /**
     * @deprecated 存在性能问题
     */
    @NonNull
    public static LiveData<List<TransferHistory>> getLiveDataIncomplete(@Nullable String devId) {
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        if (EmptyUtils.isNotEmpty(devId))
            queryBuilder.equal(TransferHistory_.srcDevId, devId);
        queryBuilder.equal(TransferHistory_.isComplete, false);
        return new ObjectBoxLiveData<>(queryBuilder.build());
    }

    @NonNull
    public static long getIncompleteCount(@Nullable String devId) {
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        if (EmptyUtils.isNotEmpty(devId))
            queryBuilder.equal(TransferHistory_.srcDevId, devId);
        queryBuilder.equal(TransferHistory_.isComplete, false);
        return queryBuilder.build().count();
    }

    public static void delete(List<Long> list) {
        Box<TransferHistory> dao = getTransferHistoryBox();
        dao.removeByIds(list);
    }

    public static void update(List<TransferHistory> histories) {
        Box<TransferHistory> dao = getTransferHistoryBox();
        String userId = DBHelper.getAccount();
        for (TransferHistory history : histories) {
            history.setUserId(userId);
        }
        dao.put(histories);
    }

    public static void deleteByDevId(String devId) {
        QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
        if (!TextUtils.isEmpty(devId)) {
            queryBuilder.equal(TransferHistory_.srcDevId, devId);
        }
        queryBuilder.build().remove();
    }

    public static class QueryTransferHistory {
        private boolean mIsDownload;
        private Boolean mIsComplete;

        public QueryTransferHistory(boolean isDownload, Boolean isComplete) {
            mIsDownload = isDownload;
            mIsComplete = isComplete;
        }

        public Query<TransferHistory> invoke() {
            QueryBuilder<TransferHistory> queryBuilder = getTransferHistoryQueryBuilder();
            queryBuilder.equal(TransferHistory_.type, (getTransferType(mIsDownload)));
            if (mIsComplete != null)
                queryBuilder.equal(TransferHistory_.isComplete, (mIsComplete));
            queryBuilder.order(TransferHistory_.isComplete)
                    .order(TransferHistory_.status)
                    .order(TransferHistory_.id);

            return queryBuilder.build();
        }
    }

}
