package net.sdvn.nascommon.model.http;

import androidx.annotation.NonNull;

import static net.sdvn.nascommon.constant.OneOSAPIs.ONE_APIS_DEFAULT_PORT;
import static net.sdvn.nascommon.constant.OneOSAPIs.PREFIX_HTTP;

/**
 * @author gaoyun@eli-tech.com
 * @date 2018/03/20
 */
public class OneOsRequest {
    private String address; // 192.168.1.100:80
    private String action; // /oneapi/user
    private RequestBody params; // {"method":"login","params":{"username":"xxx","password":"xxx"}}
    private int port =ONE_APIS_DEFAULT_PORT;
    public OneOsRequest() {
    }

    public OneOsRequest(String address, String action) {
        this.address = address;
        this.action = action;
    }

    public String url() {
        return String.format("%s%s:%s%s", PREFIX_HTTP, address,port, action);
    }

    @NonNull
    public String params() {
        return null != params ? params.jsonString() : "{}";
    }


    @Override
    public String toString() {
        return String.format("## %s --> %s ##", url(), params());
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public RequestBody getParams() {
        return params;
    }

    public void setParams(RequestBody params) {
        this.params = params;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
