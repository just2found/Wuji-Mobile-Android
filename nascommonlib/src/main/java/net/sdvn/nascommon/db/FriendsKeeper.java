package net.sdvn.nascommon.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import net.sdvn.nascommon.db.objecbox.FriendItem;
import net.sdvn.nascommon.db.objecbox.FriendItem_;
import net.sdvn.nascommon.utils.log.Logger;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

/**
 * Created by yun on 18/06/06.
 */

public class FriendsKeeper {
    private static final String TAG = FriendsKeeper.class.getSimpleName();


    public static List<FriendItem> getFriends() {
        Box<FriendItem> dao = DBHelper.getBoxStore().boxFor(FriendItem.class);
        QueryBuilder<FriendItem> queryBuilder = dao.query();
        queryBuilder.equal(FriendItem_.userId, DBHelper.getAccount());
        return queryBuilder.build().find();
    }

    @Nullable
    public static FriendItem getFriend(@NonNull String username) {
        Box<FriendItem> dao = DBHelper.getBoxStore().boxFor(FriendItem.class);
        QueryBuilder<FriendItem> queryBuilder = dao.query();
        queryBuilder.equal(FriendItem_.userId, DBHelper.getAccount());
        queryBuilder.equal(FriendItem_.username, (username));
        return queryBuilder.build().findFirst();
    }

    /**
     * Insert a user into Database if it does not exist or replace it.
     *
     * @param info
     * @return friend ID or -1
     */
    public static long addFriendOrUpdate(@Nullable FriendItem info) {
        if (info != null) {
            Logger.p(Logger.Level.ERROR, true, TAG, "Insert New Friend: " + info);
            Box<FriendItem> dao = DBHelper.getBoxStore().boxFor(FriendItem.class);
            QueryBuilder<FriendItem> query = dao.query();
            QueryBuilder<FriendItem> queryBuilder = query.equal(FriendItem_.userId, DBHelper.getAccount())
                    .equal(FriendItem_.username, info.getUsername());
            FriendItem item = queryBuilder.build().findFirst();
            if (item != null)
                item.updateSelf(info);
            else item = info;
            return dao.put(item);
        }
        return -1;
    }

    /**
     * Update user information
     *
     * @param friend
     * @return
     */
    public static boolean update(@Nullable FriendItem friend) {
        if (null == friend) {
            return false;
        }
        addFriendOrUpdate(friend);
        return true;
    }

    public static boolean delete(@Nullable FriendItem friend) {
        if (friend == null) return false;
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "delete Friend: " + new Gson().toJson(friend));
        Box<FriendItem> dao = DBHelper.getBoxStore().boxFor(FriendItem.class);
        friend.setUserId(DBHelper.getAccount());
        dao.remove(friend);
        return true;
    }
}
