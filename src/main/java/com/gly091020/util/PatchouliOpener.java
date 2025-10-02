package com.gly091020.util;

import com.gly091020.NetMusicList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import vazkii.patchouli.api.PatchouliAPI;

public class PatchouliOpener {
    private static final ResourceLocation BOOK = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "net_music_list_manual");
    public static void open(ServerPlayer player){
        PatchouliAPI.get().openBookGUI(player, BOOK);
    }
}
