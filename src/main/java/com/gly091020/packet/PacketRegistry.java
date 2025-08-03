package com.gly091020.packet;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.gly091020.sounds.BackpackNetMusicSound;
import com.gly091020.sounds.PlayerNetMusicSound;
import com.gly091020.item.NetMusicListItem;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import com.tiviacz.travelersbackpack.util.NbtHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.gly091020.NetMusicList.*;

public class PacketRegistry {
    public static void registry(){
        CHANNEL.registerMessage(
                0,
                MusicListDataPacket.class,
                MusicListDataPacket::encode,
                MusicListDataPacket::decode,
                PacketRegistry::handleServerMusicListDataPacket
        );
        CHANNEL.registerMessage(
                1,
                DeleteMusicDataPacket.class,
                DeleteMusicDataPacket::encode,
                DeleteMusicDataPacket::decode,
                PacketRegistry::handleServerDeleteMusicDataPacket
        );
        CHANNEL.registerMessage(
                2,
                MoveMusicDataPacket.class,
                MoveMusicDataPacket::encode,
                MoveMusicDataPacket::decode,
                PacketRegistry::handleServerMoveMusicDataPacket
        );
        CHANNEL.registerMessage(
                3,
                PlayerPlayMusicPacket.class,
                PlayerPlayMusicPacket::encode,
                PlayerPlayMusicPacket::decode,
                PacketRegistry::handleClientPlayerPlayPacket
        );
        CHANNEL.registerMessage(4,
                BackpackPlayMusicPacket.class,
                BackpackPlayMusicPacket::encode,
                BackpackPlayMusicPacket::decode,
                PacketRegistry::handleClientBackpackPlayPacket);
    }

    private static void handleServerMusicListDataPacket(MusicListDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (stack.is(MUSIC_LIST_ITEM.get())) {
                    NetMusicListItem.setSongIndex(stack, packet.index());
                    NetMusicListItem.setPlayMode(stack, packet.playMode());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleServerDeleteMusicDataPacket(DeleteMusicDataPacket packet, Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if(player != null){
                ItemStack stack = player.getMainHandItem();
                if (stack.is(MUSIC_LIST_ITEM.get())) {
                    NetMusicListItem.deleteSong(stack, packet.index());
                }
            }
        });
    }

    private static void handleServerMoveMusicDataPacket(MoveMusicDataPacket packet, Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if(player != null){
                ItemStack stack = player.getMainHandItem();
                if (stack.is(MUSIC_LIST_ITEM.get())) {
                    NetMusicListItem.moveSong(stack, packet.fromIndex(), packet.toIndex());
                }
            }
        });
    }

    private static void handleClientPlayerPlayPacket(PlayerPlayMusicPacket packet, Supplier<NetworkEvent.Context> ctx){
        var c = ctx.get();
        if(c.getDirection().getReceptionSide().isClient()) {
            c.enqueueWork(() -> CompletableFuture.runAsync(() -> {
                if (Minecraft.getInstance().level != null) {
                    var p = Minecraft.getInstance().level.getEntity(packet.playerID());
                    if(p instanceof Player player){
                        MusicPlayManager.play(packet.url(), packet.songName(), url ->
                                new PlayerNetMusicSound(player, url, packet.timeSecond(), packet.slot()));
                    }
                }
            }, Util.backgroundExecutor()));
        }else{
            CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        }
        c.setPacketHandled(true);
    }

    private static void handleClientBackpackPlayPacket(BackpackPlayMusicPacket packet, Supplier<NetworkEvent.Context> ctx){
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
