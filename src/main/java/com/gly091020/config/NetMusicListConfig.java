package com.gly091020.config;

import com.gly091020.NetMusicList;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = NetMusicList.ModID)
public class NetMusicListConfig implements ConfigData {
    public boolean musicHUD = true;
    public boolean notPauseSoundOnGamePause = false;
    public int x = 10;
    public int y = 10;
    public int maxImportList = 300;

    public float selectHudSize = 0.7f;
    public int selectHudCount = 5;
    public boolean selectHudShowArtist = true;
}
