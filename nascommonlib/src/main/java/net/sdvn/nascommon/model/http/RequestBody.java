package net.sdvn.nascommon.model.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.utils.GsonUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by eli100 on 2017/1/18.
 */


public class RequestBody {
    @Nullable
    private String method = null;

    @Nullable
    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    @Nullable
    private String session = null;
    @Nullable
    private Map<String, Object> params = null;

    public RequestBody(String method, String session, Map<String, Object> params) {
        this.method = method;
        this.session = session;
        this.params = params;
    }

    public RequestBody(String method, Map<String, Object> params) {
        this.method = method;
        this.params = params;
    }

    public RequestBody(String method, String session) {
        this.method = method;
        this.session = session;
    }

    public RequestBody() {
    }

    public static class Builder {
        @Nullable
        private String method = null;
        @Nullable
        private String session = null;
        @Nullable
        private Map<String, Object> params = null;

        @NonNull
        public Builder method(String method) {
            this.method = method;
            return this;
        }

        @NonNull
        public Builder addSession(String session) {
            this.session = session;
            return this;
        }

        @NonNull
        public Builder setParams(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        @NonNull
        public Builder addParams(String key, Object value) {
            if (this.params == null)
                params = new ConcurrentHashMap<>();
            params.put(key, value);
            return this;
        }

        @Nullable
        public RequestBody build() {
            return new RequestBody(this.method, this.session, this.params);
        }
    }

    public String json() {
        try {
            Map<String, Object> map = new ConcurrentHashMap<>();
            map.put("method", method);
            if (session != null)
                map.put("session", session);
            if (params != null)
                map.put("params", params);
            return GsonUtils.encodeJSON(map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public String jsonString() {
        return json();
    }

    @Nullable
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Nullable
    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}