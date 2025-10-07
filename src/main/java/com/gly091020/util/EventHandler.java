package com.gly091020.util;

import com.gly091020.NetMusicList;
import com.gly091020.item.NetMusicPlayerItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NetMusicList.ModID)
public class EventHandler {
    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event){
        handlePlayerEvent(event);
    }

    @SubscribeEvent
    public static void onSpawn(PlayerEvent.PlayerRespawnEvent event){
        handlePlayerEvent(event);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event){
        handlePlayerEvent(event);
    }

    private static void handlePlayerEvent(PlayerEvent event){
        if(hasPlayer(event.getEntity())){
            playerPlayMusic(event.getEntity());
        }
    }

    private static boolean hasPlayer(Player player){
        return player.getInventory().countItem(NetMusicList.MUSIC_PLAYER_ITEM.get()) > 0;
    }

    private static void playerPlayMusic(Player player){
        for(ItemStack stack: player.getInventory().items){
            if(stack.is(NetMusicList.MUSIC_PLAYER_ITEM.get())){
                NetMusicPlayerItem.playSound(stack, player,
                        player.getInventory().findSlotMatchingItem(stack));
                NetMusicPlayerItem.sendPacket(stack, player,
                        player.getInventory().findSlotMatchingItem(stack));
            }
        }
    }
}
