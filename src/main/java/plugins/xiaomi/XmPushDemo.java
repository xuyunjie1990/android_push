package plugins.xiaomi;

/**
 * Created by xyj on 2017/1/9.
 */
public class XmPushDemo {

    public static void main(String [] args)
    {
        XmPush push = new XmPush("Y5wKdZhoYoAonB6kc0Rbeg==","","优顾炒股");
        push.createMessage("{\"counterId\":642,\"title\":\"系统消息\",\"forword\":\"youguu://system_message_list\",\"description\":\"七千万\",\"notice\":0,\"openTitle\":\"氢气球\",\"type\":12}");
        //push.createMessage("我是内容啊4");
        System.out.println(push.push_all_send());
    }

}
