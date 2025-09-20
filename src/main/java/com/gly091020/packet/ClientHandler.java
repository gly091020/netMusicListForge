package com.gly091020.packet;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.hud.MusicInfoHud;
import com.gly091020.item.NetMusicPlayerItem;
import com.gly091020.sounds.BackpackNetMusicSound;
import com.gly091020.sounds.PlayerNetMusicSound;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import com.tiviacz.travelersbackpack.util.NbtHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.gly091020.NetMusicList.CHANNEL;

public class ClientHandler {
    public static void handleClientPlayerPlayPacket(PlayerPlayMusicPacket packet, Supplier<NetworkEvent.Context> ctx){
        var c = ctx.get();
        if(c.getDirection().getReceptionSide().isClient()) {
            c.enqueueWork(() -> CompletableFuture.runAsync(() -> {
                if (Minecraft.getInstance().level != null) {
                    var p = Minecraft.getInstance().level.getEntity(packet.playerID());
                    if(p instanceof Player player){
                        MusicPlayManager.play(packet.url(), packet.songName(), url ->
                                new PlayerNetMusicSound(player, url, packet.timeSecond(), packet.slot()));
                        if(player == Minecraft.getInstance().player){
                            var stack = player.getInventory().getItem(packet.slot());
                            if(stack.is(NetMusicList.MUSIC_PLAYER_ITEM.get())){
                                var stack1 = NetMusicPlayerItem.getContainer(stack).getItem(0);
                                if(stack1.getItem() instanceof ItemMusicCD){
                                    MusicInfoHud.setInfo(packet.info(), stack, packet.slot());
                                }
                            }
                        }
                    }
                }
            }, Util.backgroundExecutor()));
        }else{
            CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        }
        c.setPacketHandled(true);
    }

    public static void handleClientBackpackPlayPacket(BackpackPlayMusicPacket packet, Supplier<NetworkEvent.Context> ctx){
        var c = ctx.get();
        if(c.getDirection().getReceptionSide().isClient()) {
            c.enqueueWork(() -> CompletableFuture.runAsync(() -> {
                if (Minecraft.getInstance().level != null) {
                    var p = Minecraft.getInstance().level.getEntity(packet.playerID());
                    if(p instanceof Player player){
                        MusicPlayManager.play(packet.url(), packet.songName(), url ->
                                new BackpackNetMusicSound(player, url, packet.timeSecond(), packet.slot(),
                                        packet.upgradeSlot()));
                    }
                }
            }, Util.backgroundExecutor()));
        }else{
            var p = c.getSender();
            if (p == null) {return;}
            var s = p.getInventory().getItem(packet.slot());
            if(s.getItem() instanceof TravelersBackpackItem){
                var l = NbtHelper.getOrDefault(s, "Upgrades", NonNullList.withSize(10, ItemStack.EMPTY));
                s.getOrCreateTag().putInt("tick", packet.timeSecond() * 20);
                NbtHelper.set(s, "Upgrades", new ItemStackHandler(l));
            }
            CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        }
        c.setPacketHandled(true);
    }
}
