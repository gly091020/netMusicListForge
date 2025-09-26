package com.gly091020.config;

import com.gly091020.NetMusicList;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = NetMusicList.ModID)
public class NetMusicListConfig implements ConfigData {
    public boolean musicHUD = true;
    public int x = 10;
    public int y = 10;

    public float selectHudSize = 0.7f;
    public int selectHudCount = 20;
    public boolean selectHudShowArtist = true;
}
