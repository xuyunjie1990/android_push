package plugins.huawei;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youguu.android.service.ImessageSendService;
import com.youguu.android.util.Constants;
import com.youguu.android.util.ProductConfig;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import nsp.NSPClient;
import nsp.OAuth2Client;
import nsp.support.common.AccessToken;
import nsp.support.common.NSPException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xyj on 2017/1/5.
 */
public class Hpush {

    private Log log = LogFactory.getLog(ImessageSendService.class);

    private String token;

    private  long lasttime  = 0 ;

    /**
     *  token过期时间，秒级
     */
    private int expires = 0;

    private Map<String,NSPClient> clientMap  = new HashMap<>();

    private static Hpush hpush;

    private String getToken(String appId,String appKey,String propPath,String propPwd)
    {
        if(tokenIsNoPastDue())
        {
            return token;
        }
        try {
            OAuth2Client oauth2Client = new OAuth2Client();
            oauth2Client.initKeyStoreStream(Hpush.class.getResource(propPath).openStream(),propPwd);
            AccessToken access_token = oauth2Client.getAccessToken("client_credentials", appId, appKey);
            token = access_token.getAccess_token();
            expires = access_token.getExpires_in();
            lasttime = System.currentTimeMillis();
            return token;
        } catch (Exception e) {
            log.error("获取华为token错误", e);
            return "";
        }
    }

    /**
     *  判断token是否过期
     * @return
     */
    private boolean tokenIsNoPastDue()
    {
        if(System.currentTimeMillis() - lasttime < expires*1000 && token!=null)
        {
            return true;
        }
        return false;
    }

    /**
     *  获取连接
     * @param product
     * @return
     */
    private NSPClient getClient(String product)
    {
        try {
            if(clientMap.get(product) == null)
            {
                synchronized(Hpush.class)
                {
                    if(clientMap.get(product) == null)
                    {
                        product = String.format(Constants.HUAWEI,product);
                        ProductConfig config = ProductConfig.getProductConfig(product);
                        NSPClient client = new NSPClient(getToken(config.getAccessId(),config.getSecretKey(),config.getPropPath(),config.getPropPwd()));
                        client.initHttpConnections(config.getConnNum(), config.getConnMaxNum());
                        client.initKeyStoreStream(Hpush.class.getResource(config.getPropPath()).openStream(), config.getPropPwd());
                        clientMap.put(product,client);
                        return client;
                    }
                }
            }
            return clientMap.get(product);
        } catch (Exception e) {
            log.error("获取华为client错误", e);
            return null;
        }

    }

    public static Hpush getCacheHpush()
    {
        if(hpush != null)
        {
            return hpush;
        }
        hpush = new Hpush();
        return hpush;
    }

    /**
     *  单推选择
     * @param token
     * @param project
     * @param message
     * @return
     */
    public int single_send_check(String token,String project,String message)
    {
        return checkPushType(token, project, message);
    }

    /**
     *  选择推送方式
     * @param message
     * @return
     */
    private int checkPushType(String token,String project,String message)
    {
        //处理消息结构
        JSONObject msg = JSONObject.parseObject(message);
        //如果type为3则需要透传给客户端，否则直接走通知栏
        int type = msg.getIntValue("type");
        if(type == 3)
        {
            return single_send_tc(token, project, message);
        }
        else
        {
            return single_send(token, project, message);
        }
    }

    /**
     *  通知栏单发消息
     * @param token
     * @param project
     * @throws NSPException
     */
    public int single_send(String token,String project,String message)
    {
        try {
            String toMessage = getMessage(project,message);
            //消息是否需要缓存，必选
            //0：不缓存
            //1：缓存
            //  缺省值为0
            int cacheMode = 1;

            //标识消息类型（缓存机制），必选
            //由调用端赋值，取值范围（1~100）。当TMID+msgType的值一样时，仅缓存最新的一条消息
            int msgType = 1;

            // 可选
            // 0: 当前用户
            // 1: 主要用户
            // -1: 默认用户
            //
            String userType = "1";

            //构造请求
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("deviceToken", token);
            hashMap.put("android",toMessage);
            hashMap.put("cacheMode", cacheMode);
            hashMap.put("msgType", msgType);
            hashMap.put("userType", userType);

            //设置http超时时间
            getClient(project).setTimeout(10000, 15000);
            //接口调用  openpush.message.single_send 是透传  openpush.message.psSingleSend 是通知栏
            PushRet resp = getClient(project).call("openpush.message.psSingleSend", hashMap, PushRet.class);
            if(resp.getResultcode() == 0)
            {
                return Constants.GUANGBO_STATUS_YES;
            }

            log.error("单发接口消息失败，message="+resp.getMessage());
            return Constants.GUANGBO_STATUS_NO;
        } catch (NSPException e) {
            log.error("single_send错误",e);
            return Constants.GUANGBO_STATUS_NO;
        }
    }

    /**
     *  透传单发信息
     * @param token
     * @param project
     * @param message
     * @return
     */
    public int single_send_tc(String token,String project,String message)
    {
        try {
            //必选
            //0：高优先级
            //1：普通优先级
            //缺省值为1
            int priority = 0;

            //消息是否需要缓存，必选
            //0：不缓存
            //1：缓存
            //  缺省值为0
            int cacheMode = 1;

            //标识消息类型（缓存机制），必选
            //由调用端赋值，取值范围（1~100）。当TMID+msgType的值一样时，仅缓存最新的一条消息
            int msgType = 1;

            //构造请求
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("deviceToken", token);
            hashMap.put("message", message);
            hashMap.put("priority", priority);
            hashMap.put("cacheMode", cacheMode);
            hashMap.put("msgType", msgType);

            //设置http超时时间
            getClient(project).setTimeout(10000, 15000);
            //接口调用  openpush.message.single_send 是透传   openpush.message.psSingleSend 是通知栏
            PushRet resp = getClient(project).call("openpush.message.single_send", hashMap, PushRet.class);
            if(resp.getResultcode() == 0)
            {
                return Constants.GUANGBO_STATUS_YES;
            }

            log.error("单发接口消息失败，message="+resp.getMessage());
            return Constants.GUANGBO_STATUS_NO;
        } catch (NSPException e) {
            log.error("single_send错误",e);
            return Constants.GUANGBO_STATUS_NO;
        }
    }

    /**
     * 群发消息
     * @param tokens
     * @param project
     * @param message
     * @throws NSPException
     */
    public int batch_send(List<String> tokens,String project,String message)
    {
        try {
            //目标用户列表，必选
            //最多填1000个，每个目标用户为32字节长度，由系统分配的合法TMID。手机上安装了push应用后，会到push服务器申请token，申请到的token会上报给应用服务器
            String[] deviceTokenList = tokens.toArray(new String[tokens.size()]);

            String toMessage = getMessage(project,message);

            //消息是否需要缓存，必选
            //0：不缓存
            //1：缓存
            // 缺省值为0
            Integer cacheMode = 1;

            //标识消息类型（缓存机制），必选
            //由调用端赋值，取值范围（1~100）。当TMID+msgType的值一样时，仅缓存最新的一条消息
            Integer msgType = 1;

            // 可选
            // 0: 当前用户
            // 1: 主要用户
            // -1: 默认用户
            //
            String userType = "1";

            //构造请求
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("deviceTokenList", deviceTokenList);
            hashMap.put("android", toMessage);
            hashMap.put("cacheMode", cacheMode);
            hashMap.put("msgType", msgType);
            hashMap.put("userType", userType);

            //设置http超时时间
            getClient(project).setTimeout(10000, 15000);
            //接口调用   openpush.message.batch_send 是透传   openpush.message.psBatchSend 是通知栏
            PushRet resp = getClient(project).call("openpush.message.psBatchSend", hashMap, PushRet.class);
            if(resp.getResultcode() == 0)
            {
                log.error("群发接口消息成功");
                return Constants.GUANGBO_STATUS_YES;
            }

            log.error("群发接口消息失败，message=" + resp.getMessage());
            return Constants.GUANGBO_STATUS_NO;
        } catch (Exception e) {
            log.error("huawei_batch_send错误", e);
            return Constants.GUANGBO_STATUS_NO;
        }
    }

    private String getTags(String project)
    {
        JSONObject obj = new JSONObject();
        JSONObject obj2 = new JSONObject();
        JSONArray ja = new JSONArray();
        JSONArray ja2 = new JSONArray();
        ja2.add("youguu");
        obj2.put("jhss",ja2);
        ja.add(obj2);
        obj.put("tags",ja);
       return obj.toJSONString();
    }

    /**
     *  获取推送的title
     * @param project
     * @return
     */
    private String getTitle(String project)
    {
        if(Constants.PRODUCT_YGLC.equals(project))
        {
            return "优顾理财";
        }
        else
        {
            return "优顾炒股";
        }
    }

    /**
     *  处理通知栏方式推送的消息结构体
     * @param project
     * @param message
     * @return
     */
    private String getMessage(String project,String message)
    {
        //处理消息结构
        JSONObject msg = JSONObject.parseObject(message);
        //获取下拉展示的内容
        String des = msg.getString("description");
        String title = msg.getString("title");
        if(title == null || "".equals(title))
        {
            title = getTitle(project);
        }

        JSONObject obj = new JSONObject();
        obj.put("notification_title",title);
        obj.put("notification_content",des);
        //TODO 这里用2测试下
        //1：直接打开应 2：通过自定义动作打开应用 3：打开URL
        //将消息体透传方式给客户端
//        JSONArray ja = new JSONArray();
//        ja.add(msg);
//        obj.put("doings",1);
//        obj.put("extras",ja);
        try {
            //因为客户端在股价预警业务上还需要用到，所以不删
            int type = msg.getIntValue("type");
            if(type != 22 && type != 21 && type != 23)
            {
                msg.remove("description");
            }
            msg.remove("title");
            String toUrl = URLEncoder.encode(msg.toJSONString(),"utf-8");
            obj.put("doings",2);
            obj.put("intent","youguu://com.jhss.youguu/push?msg="+toUrl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return obj.toJSONString();
    }

    /**
     * 通过tags走通知栏消息接口
     * @throws NSPException
     */
    public int notification_send(String project,String message)
            throws NSPException
    {
        try {
            //推送范围，必选
            //1：指定用户，必须指定tokens字段
            //2：所有人，无需指定tokens，tags，exclude_tags
            //3：一群人，必须指定tags或者exclude_tags字段
            Integer push_type = 3;

            String toMessage = getMessage(project,message);

            String tags = getTags(project);

            //构造请求
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("push_type", push_type);
            hashMap.put("android", toMessage);
            System.out.println(tags);
            hashMap.put("tags", tags);
            System.out.println(hashMap);

            //设置http超时时间
            getClient(project).setTimeout(10000, 15000);
            //接口调用
            //openpush.message.batch_send 是透传
            //openpush.message.psBatchSend 是通知栏
            String resp = getClient(project).call("openpush.openapi.notification_send", hashMap, String.class);
            System.out.println(resp);
//            if(resp.getResultcode() == 0)
//            {
//                log.error("群发接口消息成功");
//                return Constants.BATCH_YES;
//            }
//
//            log.error("群发接口消息失败，message=" + resp.getMessage());
            return Constants.BATCH_NO;
        } catch (Exception e) {
            log.error("huawei_batch_send错误", e);
            return Constants.BATCH_NO;
        }
    }

}
