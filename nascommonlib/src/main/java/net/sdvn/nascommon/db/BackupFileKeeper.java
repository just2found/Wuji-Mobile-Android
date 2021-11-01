package net.sdvn.nascommon.db;

import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.objecbox.BackupFile;
import net.sdvn.nascommon.db.objecbox.BackupFile_;
import net.sdvn.nascommon.model.oneos.backup.BackupType;
import net.sdvn.nascommon.utils.EmptyUtils;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;


/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class BackupFileKeeper {

    /**
     * List Backup Info by mac and username
     *
     * @param devUUID dev ID
     * @return
     */
    public static List<BackupFile> all(String devUUID, int type) {
        Box<BackupFile> dao = DBHelper.getBoxStore().boxFor(BackupFile.class);
        QueryBuilder<BackupFile> queryBuilder = dao.query();
        queryBuilder.equal(BackupFile_.userId, DBHelper.getAccount())
                .and()
                .equal(BackupFile_.devUUID, devUUID)
                .and()
                .equal(BackupFile_.type, (type));
        return queryBuilder.build().find();
    }

    public static List<BackupFile> all(int type) {
        Box<BackupFile> dao = DBHelper.getBoxStore().boxFor(BackupFile.class);
        QueryBuilder<BackupFile> queryBuilder = dao.query();
        queryBuilder.equal(BackupFile_.userId, DBHelper.getAccount())
                .and()
                .equal(BackupFile_.type, (type));

        return queryBuilder.build().find();
    }

    /**
     * Get Backup Info from database by user, mac and path
     *
     * @param devUUID dev ID
     * @param path    backup path
     * @return {@link BackupFile} or {@code null}
     */
    @Nullable
    public static BackupFile getBackupInfo(String devUUID, String path, int type) {
        Box<BackupFile> dao = DBHelper.getBoxStore().boxFor(BackupFile.class);
        QueryBuilder<BackupFile> queryBuilder = dao.query();
        queryBuilder.equal(BackupFile_.userId, DBHelper.getAccount())
                .and()
                .equal(BackupFile_.devUUID, devUUID)
                .and()
                .equal(BackupFile_.path, (path));

        return queryBuilder.build().findFirst();
    }

    /**
     * Insert a user into Database if it does not exist or replace it.
     *
     * @param info
     * @return insertBackupAlbum result
     */
    public static boolean insertBackupAlbum(@Nullable BackupFile info) {
        if (info != null) {
            info.setUserId(DBHelper.getAccount());
            Box<BackupFile> dao = DBHelper.getBoxStore().boxFor(BackupFile.class);
            return dao.put(info) > 0;
        }

        return false;
    }

    public static long insertBackupFile(@Nullable BackupFile info) {
        if (null == info) {
            return -1;
        }
        info.setUserId(DBHelper.getAccount());
        int maxCount = AppConstants.MAX_BACKUP_FILE_COUNT;
        Box<BackupFile> dao = DBHelper.getBoxStore().boxFor(BackupFile.class);
        QueryBuilder<BackupFile> queryBuilder = dao.query();
        queryBuilder.equal(BackupFile_.userId, info.getUserId());
        queryBuilder.equal(BackupFile_.devUUID, (info.getDevUUID()));
        queryBuilder.equal(BackupFile_.type, (BackupType.FILE));
        int count = queryBuilder.build().find().size();
        if (count >= maxCount) {
            return -1;
        }

        return dao.put(info);
    }

    @Nullable
    public static BackupFile deleteBackupFile(String devUUID, String path) {
        Box<BackupFile> dao = DBHelper.getBoxStore().boxFor(BackupFile.class);
        QueryBuilder<BackupFile> queryBuilder = dao.query();
        queryBuilder.equal(BackupFile_.userId, DBHelper.getAccount());
        queryBuilder.equal(BackupFile_.devUUID, (devUUID));
        queryBuilder.equal(BackupFile_.type, (BackupType.FILE));
        queryBuilder.equal(BackupFile_.path, (path));
        BackupFile backupFile = queryBuilder.build().findFirst();
        if (null != backupFile) {
            dao.remove(backupFile);
        }

        return backupFile;
    }

    /**
     * Reset Backup by mac and username
     *
     * @return
     */
    public static boolean resetBackupAlbum(String devUUID) {
        Box<BackupFile> dao = DBHelper.getBoxStore().boxFor(BackupFile.class);
        QueryBuilder<BackupFile> queryBuilder = dao.query();
        queryBuilder.equal(BackupFile_.userId, DBHelper.getAccount());
        queryBuilder.equal(BackupFile_.devUUID, (devUUID));
        queryBuilder.equal(BackupFile_.type, (BackupType.ALBUM));

        List<BackupFile> list = queryBuilder.build().find();
        if (null != list) {
            for (BackupFile info : list) {
                info.setTime(0L);
            }
            update(list);
        }

        return true;
    }

    private static boolean update(List<BackupFile> list) {
        if (EmptyUtils.isEmpty(list)) {
            return false;
        }
        for (BackupFile backupFile : list) {
            backupFile.setUserId(DBHelper.getAccount());
        }
        Box<BackupFile> dao = DBHelper.getBoxStore().boxFor(BackupFile.class);
        dao.put(list);
        return true;
    }

    /**
     * Delete a user from Database
     *
     * @param info
     * @return result
     */
    public static boolean delete(@Nullable BackupFile info) {
        if (info != null) {
            info.setUserId(DBHelper.getAccount());
            Box<BackupFile> dao = DBHelper.getBoxStore().boxFor(BackupFile.class);
            dao.remove(info);

            return true;
        }

        return false;
    }

    /**
     * Update user information
     *
     * @param file
     * @return
     */
    public static boolean update(@Nullable BackupFile file) {
        if (null == file) {
            return false;
        }
        file.setUserId(DBHelper.getAccount());
        Box<BackupFile> dao = DBHelper.getBoxStore().boxFor(BackupFile.class);
        dao.put(file);
        return true;
    }
}
