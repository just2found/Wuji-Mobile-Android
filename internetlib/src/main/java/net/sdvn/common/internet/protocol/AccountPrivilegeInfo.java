package net.sdvn.common.internet.protocol;


import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;

import java.util.List;

@Keep
public class AccountPrivilegeInfo extends GsonBaseProtocol {

    //一周毫秒数
    private static final long WEEK_TIME = 1000 * 60 * 60 * 24 * 7;

    public DataBean data;

    @Keep
    public static class DataBean {

        public ServiceBean service;
        public List<VnodesBean> vnodes;
        public List<DevicesBean> devices;
    }

    @Keep
    public static class ServiceBean implements AdapterBean {

        public long expired;
        public long current;
        public int Status;

        @Override
        public String getName() {
            return "";
        }

        @Override
        public long getExpired() {
            return expired;
        }

        @Override
        public void setStatus(int status) {
        }

        @Override
        public int getStatus() {
            return Status;
        }

        @Override
        public long getFlowUsable() {
            return 0;
        }

        @Override
        public long getFlowUsed() {
            return 0;
        }

        @Override
        public String getID() {
            return "";
        }

        @Override
        public String getSN() {
            return "";
        }

        @Override
        public String getUnits() {
            return "";
        }
    }

    @Keep
    public static class VnodesBean implements AdapterBean {

        public String id;
        public String name;
        public long expire;
        public int status;
        public boolean scanconfirm;
        public int vnodetype;
        public List<VnodegroupBean> vnodegroup;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getExpired() {
            return expire;
        }

        @Override
        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public int getStatus() {
            return status;
//            for (VnodegroupBean bean : vnodegroup) {
//                if (bean.paid)
//                    return bean.status;
//            }
//            return 0;
        }

        @Override
        public long getFlowUsable() {
            for (VnodegroupBean bean : vnodegroup) {
                if (bean.paid)
                    return bean.flow_usable;
            }
            return 0;
        }

        @Override
        public long getFlowUsed() {
            for (VnodegroupBean bean : vnodegroup) {
                if (bean.paid)
                    return bean.flow_used;
            }
            return 0;
        }

        @Override
        public String getID() {
            return id;
        }

        @Override
        public String getSN() {
            return "";
        }

        @Override
        public String getUnits() {
            for (VnodegroupBean bean : vnodegroup) {
                if (bean.paid)
                    return bean.units;
            }
            return "";
        }

        @Keep
        public static class VnodegroupBean {

            public String groupid;
            public String groupname;
            public int priority;
            public int status;
            public boolean paid;
            public long flow_usable;
            public long flow_used;
            public String units;
            public List<String> nodes;
        }
    }

    @Keep
    public static class DevicesBean implements AdapterBean {

        public String name;
        public String sn;
        public long expired;
        public int status;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getExpired() {
            return expired;
        }

        @Override
        public void setStatus(int status) {
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public long getFlowUsable() {
            return 0;
        }

        @Override
        public long getFlowUsed() {
            return 0;
        }

        @Override
        public String getID() {
            return "";
        }

        @Override
        public String getSN() {
            return sn;
        }

        @Override
        public String getUnits() {
            return "";
        }
    }

    public interface AdapterBean {
        String getName();

        long getExpired();

        void setStatus(int status);

        int getStatus();

        long getFlowUsable();

        long getFlowUsed();

        String getID();

        String getSN();

        String getUnits();
    }
}
