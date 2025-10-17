package com.gly091020.packet;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.hud.MusicInfoHud;
import com.gly091020.item.NetMusicPlayerItem;
import com.gly091020.sounds.EnderPlayerNetMusicSound;
import com.gly091020.sounds.PlayerNetMusicSound;
import com.gly091020.util.LoginNeedUtil;
import com.gly091020.util.NetMusicListUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.gly091020.NetMusicList.CHANNEL;

public class ClientHandler {
    public static void handleClientPlayerPlayPacket(PlayerPlayMusicPacket packet, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        if (c.getDirection().getReceptionSide().isClient()) {
            c.enqueueWork(() -> CompletableFuture.runAsync(() -> {
                if (Minecraft.getInstance().level != null) {
                    var p = Minecraft.getInstance().level.getEntity(packet.playerID());
                    if (p instanceof Player player) {
                        var finalUrl = packet.url();
                        if(NetMusicListUtil.hasLoginNeed()){
                            var url1 = LoginNeedUtil.getUrl(finalUrl);
                            if(url1 != null)finalUrl = url1;
                        }
                        MusicPlayManager.play(finalUrl, packet.songName(), url ->
                                new PlayerNetMusicSound(player, url, packet.timeSecond(), packet.slot()));

                        if (player == Minecraft.getInstance().player) {
                            var stack = player.getInventory().getItem(packet.slot());
                            if (stack.is(NetMusicList.MUSIC_PLAYER_ITEM.get())) {
                                var stack1 = NetMusicPlayerItem.getContainer(stack).getItem(0);
                                if (stack1.getItem() instanceof ItemMusicCD) {
                                    MusicInfoHud.setInfo(packet.info(), stack, packet.slot());
                                }
                            }
                        }
                    }
                }
            }, Util.backgroundExecutor()));
        } else {
            CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        }
        c.setPacketHandled(true);
    }

    public static void handleClientEnderPlayerPlayPacket(PlayEnderMusicPlayerPacket packet, Supplier<NetworkEvent.Context> ctx){
        var c = ctx.get();
        if (c.getDirection().getReceptionSide().isClient()) {
            c.enqueueWork(() -> CompletableFuture.runAsync(() -> {
                var finalUrl = packet.url;
                if(NetMusicListUtil.hasLoginNeed()){
                    var url1 = LoginNeedUtil.getUrl(finalUrl);
                    if(url1 != null)finalUrl = url1;
                }
                MusicPlayManager.play(finalUrl, packet.songName, url ->
                        new EnderPlayerNetMusicSound(packet.pos, url, packet.timeSecond));
            }));
            c.setPacketHandled(true);
        }
    }

    public static void handleStopMusicPacket(StopMusicPacket packet, Supplier<NetworkEvent.Context> ctx){
        var c = ctx.get();
        if (FMLEnvironment.dist.isClient()) {
            c.enqueueWork(() -> CompletableFuture.runAsync(() -> {
                var sounds = NetMusicListUtil.getTickableSounds();
                for(TickableSoundInstance soundInstance: sounds){
                    if(soundInstance instanceof PlayerNetMusicSound sound && sound.getPlayer().getId() == packet.playerID()){
                        sound.stopMusic();
                    }
                }
            }));
        }

        c.setPacketHandled(true);
    }
}
