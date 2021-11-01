package net.sdvn.nascommon.db;

import androidx.annotation.Nullable;

import net.sdvn.nascommon.db.objecbox.BackupInfo;
import net.sdvn.nascommon.db.objecbox.BackupInfo_;
import net.sdvn.nascommon.model.oneos.backup.info.BackupInfoType;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;


/**
 * Created by gaoyun@eli-tech.com on 2016/02/25.
 */
public class BackupInfoKeeper {
    private static final int TYPE_BACKUP_CONTACTS = 1;
    private static final int TYPE_RECOVERY_CONTACTS = 2;
    private static final int TYPE_BACKUP_SMS = 3;
    private static final int TYPE_RECOVERY_SMS = 4;

    private static int getBackupType(BackupInfoType type) {
        if (type == BackupInfoType.BACKUP_CONTACTS) {
            return TYPE_BACKUP_CONTACTS;
        } else if (type == BackupInfoType.RECOVERY_CONTACTS) {
            return TYPE_RECOVERY_CONTACTS;
        } else if (type == BackupInfoType.BACKUP_SMS) {
            return TYPE_BACKUP_SMS;
        } else {
            return TYPE_RECOVERY_SMS;
        }
    }

    @Nullable
    public static BackupInfo getBackupHistory(String devUUID, BackupInfoType type) {
        Box<BackupInfo> dao = DBHelper.getBoxStore().boxFor(BackupInfo.class);
        QueryBuilder<BackupInfo> queryBuilder = dao.query();
        queryBuilder.equal(BackupInfo_.userId, DBHelper.getAccount());
        queryBuilder.equal(BackupInfo_.devUUID, (devUUID));
        queryBuilder.equal(BackupInfo_.type, (getBackupType(type)));

        return queryBuilder.build().findFirst();
    }

    public static boolean insertOrReplace(@Nullable BackupInfo info) {
        if (info != null) {
            info.setUserId(DBHelper.getAccount());
            Box<BackupInfo> dao = DBHelper.getBoxStore().boxFor(BackupInfo.class);
            return dao.put(info) > 0;
        }

        return false;
    }

    public static boolean update(String devUUID, BackupInfoType type, long time) {
        Box<BackupInfo> dao = DBHelper.getBoxStore().boxFor(BackupInfo.class);
        QueryBuilder<BackupInfo> queryBuilder = dao.query();
        queryBuilder.equal(BackupInfo_.userId, DBHelper.getAccount());
        queryBuilder.equal(BackupInfo_.devUUID, (devUUID));
        queryBuilder.equal(BackupInfo_.type, (getBackupType(type)));

        BackupInfo history = queryBuilder.build().findFirst();
        if (history != null) {
            history.setCount(history.getCount() + 1);
            history.setTime(time);
            dao.put(history);
        } else {
//            null, devUUID, getBackupType(type), time, 1L
            history = new BackupInfo();
            history.setCount(1L);
            history.setUserId(DBHelper.getAccount());
            history.setDevUUID(devUUID);
            history.setType(getBackupType(type));
            history.setTime(time);
            dao.put(history);
        }

        return true;
    }
}
