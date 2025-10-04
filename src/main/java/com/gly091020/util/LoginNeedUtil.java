package com.gly091020.util;

import com.gly091020.NetMusicLoginNeed.NetMusicLoginNeedUtil;

public class LoginNeedUtil {
    public static String getUrl(String url){
        return NetMusicLoginNeedUtil.pasteVIPUrl(url);
    }
}
