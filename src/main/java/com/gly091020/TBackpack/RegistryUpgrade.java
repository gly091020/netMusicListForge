package com.gly091020.TBackpack;

import net.minecraftforge.registries.RegistryObject;

import static com.gly091020.NetMusicList.ITEMS;

@Deprecated
public class RegistryUpgrade {
    public static RegistryObject<NetMusicUpgradeItem> MUSIC_UPGRADE_ITEM;
    public static void registry(){
        MUSIC_UPGRADE_ITEM = ITEMS.register("music_upgrade", NetMusicUpgradeItem::new);
    }
}
