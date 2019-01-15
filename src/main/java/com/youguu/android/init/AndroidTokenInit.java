package com.youguu.android.init;

import com.youguu.android.cache.ITokenLoadService;
import com.youguu.push.pojo.AndroidToken;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by xyj on 2017/1/19.
 *  华为token启动加载
 */
@Service("huaWeiTokenInit")
public class AndroidTokenInit implements InitializingBean {

    @Resource
    private ITokenLoadService tokenLoadService;

    @Override
    public void afterPropertiesSet() throws Exception {
        //这里只去加载需要提供广播服务的服务商的所有token,暂只有华为，有需要再添加加载即可
        tokenLoadService.incToken(AndroidToken.PUSH_HUAWEI);
    }
}
