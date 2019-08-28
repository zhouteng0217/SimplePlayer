package com.zt.core.base;

import java.util.Map;

//定义播放器UI层操作逻辑接口
public abstract class BaseVideoController {

    private String url;
    private Map<String, String> headers;

    private BasePlayer basePlayer;

}
