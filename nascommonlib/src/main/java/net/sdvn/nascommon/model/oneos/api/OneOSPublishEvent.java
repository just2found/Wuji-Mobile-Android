package net.sdvn.nascommon.model.oneos.api;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.oneos.user.LoginSession;

/*
请求方式: GET/POST HTTP/1.1 Content-Type: application/json
请求 URL: /oneapi/event/pub
请求参数:
{ "method":"", "params": { "session":"xxx", "msg": "xxx "} }
名称  类型  必须  描述
msg string  是   需要推送的消息
请求示例:
     curl -H 'content-type:application/json' -X POST -d '{"method":"","params":{"msg":"hello"}}' "http://ip/oneapi/event/pub"
HTTP Code:HTTP 200
成功:{"result":true,"data":{}}
失败: {"result":false, "error":{"code":xx,"msg":"xxxx"}}

*/
public class OneOSPublishEvent extends BaseAPI {

    public OneOSPublishEvent(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.EVENT_PUB);
    }

    public void publish(String msg) {

    }
}
