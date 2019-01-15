package plugins.huawei;

import nsp.support.common.NSPException;

/**
 * 华为推送测试
 * Created by xyj on 2017/1/6.
 */
public class HpushDemo {

    public static void main(String[] args)throws NSPException
    {
        try
        {
            Hpush hp = Hpush.getCacheHpush();
            //调用push单发接口
            System.out.println(hp.single_send("0862629039064954200000160500CN01","001","{\"counterId\":643,\"title\":\"系统消息2\",\"forword\":\"youguu://system_message_list\",\"description\":\"请问完全齐威王齐威王全额问问去问问去问问去\",\"notice\":0,\"openTitle\":\"氢气球\",\"type\":12}"));
//            System.out.println(hp.single_send_tc("0866023020131392200000160500CN01", "001", "哈哈，3单条测试3"));

            //调用群发push消息接口
//            List<String> list = new ArrayList<>();
//            list.add("0866023020131392200000160500CN01");
//            hp.batch_send(list,"001","哈哈，群条测试5");

            //调用广播接口
//            hp.notification_send("001","2l4ai12345al2");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
