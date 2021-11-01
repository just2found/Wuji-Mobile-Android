package net.sdvn.nascommon.db.objecbox;

import androidx.annotation.Keep;

import java.util.Objects;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Keep
@Entity
public class NasServiceItem {
    @Id
    private long id;
    private String userId;
    private String devId;
    private int serviceId;
    private String serviceName;
    private int serviceType;
    private boolean serviceStatus = true;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getServiceType() {
        return serviceType;
    }

    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public boolean isServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(boolean serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NasServiceItem that = (NasServiceItem) o;
        return id == that.id &&
                serviceId == that.serviceId &&
                serviceType == that.serviceType &&
                serviceStatus == that.serviceStatus &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(devId, that.devId) &&
                Objects.equals(serviceName, that.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, devId, serviceId, serviceName, serviceType, serviceStatus);
    }
}
