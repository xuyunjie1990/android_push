package plugins.xiaomi;

import com.alibaba.fastjson.JSONObject;
import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Result;
import com.xiaomi.xmpush.server.Sender;

import java.util.List;
import java.util.Random;

/**
 * 小米推送
 * Created by xyj on 2017/1/6.
 */
public class XmPush {

    static {
        //使用push服务时，需要先加载下这行代码，安卓不区分测试正式环境
        Constants.useOfficial();
    }

    /**
     *  小米发送对象
     */
    private Sender sender;

    private Message message;

    private String packageName;

    private String productName;

    public XmPush(){}

    public XmPush(String appSecret,String packageName,String productName)
    {
        this.sender = new Sender(appSecret);
        this.packageName = packageName;
        this.productName = productName;
    }

    public void createMessage(String content)
    {
        try {
            //处理消息结构
            JSONObject msg = JSONObject.parseObject(content);
            //获取下拉展示的内容
            String des = msg.getString("description");
            //因为某些情况下，比如@某人，关注是没有description的，而小米要求这个字段不为空，所以当为空时给个默认值
            if("".equals(des))
            {
                des = "优顾炒股";
            }

            String title = msg.getString("title");
            if(title == null || "".equals(title))
            {
                title = productName;
            }

            //判断是否需要透传
            boolean isTc = false;
            //如果type为3则需要透传给客户端，否则直接走通知栏
            int type = msg.getIntValue("type");
            if(type == 3)
            {
                isTc =  true;
            }
            else
            {
                isTc =  false;
            }

            //TODO 没确定payload是做什么用的，从表象上来，都是用来装载消息内容的
            //默认情况下，通知栏只显示一条推送消息。如果通知栏要显示多条推送消息，需要针对不同的消息设置不同的notify_id
            //TODO restrictedPackageNames设置空默认为发送所有包名渠道this.packageName.split(",")
            int notifyId = new Random().nextInt(100000);
            message = new Message.Builder()
                    .title(title)
                    .description(des)
                    .payload(content)
                    .restrictedPackageNames(null)
                    .passThrough(isTc ? 1:0) // 1表示透传消息，0表示通知栏消息
                    .notifyId(notifyId)
                    .notifyType(1)     // 使用默认提示音提示
                    //.extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT,Constants.NOTIFY_LAUNCHER_ACTIVITY)//客户端点击通知栏的行为：打开当前app内的任意一个Activity。
                    .build();
            //TODO timeToLive(int milliseconds)可选项参数，如果用户离线，设置消息在服务器保存的时间，单位：ms。服务器默认最长保留两周。
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  单推
     * @param regIds
     * @return
     */
    public int single_send(String regIds)
    {
        try {
            Result result = sender.send(message,regIds,3);
            return result.getMessageId() != null ? com.youguu.android.util.Constants.BATCH_YES: com.youguu.android.util.Constants.BATCH_NO;
        } catch (Exception e) {
            e.printStackTrace();
            return com.youguu.android.util.Constants.BATCH_NO;
        }
    }

    /**
     *  批量推送
     * @param regIds
     * @return
     */
    public int batch_send(List<String> regIds)
    {
        try {
            Result result = sender.send(message,regIds,3);
            return result.getMessageId() != null ? com.youguu.android.util.Constants.GUANGBO_STATUS_YES: com.youguu.android.util.Constants.GUANGBO_STATUS_NO;
        } catch (Exception e) {
            e.printStackTrace();
            return com.youguu.android.util.Constants.GUANGBO_STATUS_NO;
        }
    }

    /**
     *  广播
     * @return
     */
    public int push_all_send()
    {
        try {
            Result result = sender.broadcastAll(message,3);
            return result.getMessageId() != null ? com.youguu.android.util.Constants.GUANGBO_STATUS_YES: com.youguu.android.util.Constants.GUANGBO_STATUS_NO;
        } catch (Exception e) {
            e.printStackTrace();
            return com.youguu.android.util.Constants.GUANGBO_STATUS_NO;
        }
    }

}
