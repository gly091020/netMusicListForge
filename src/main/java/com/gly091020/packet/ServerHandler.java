package com.gly091020.packet;

import com.gly091020.item.NetMusicListItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

import static com.gly091020.NetMusicList.CHANNEL;
import static com.gly091020.NetMusicList.MUSIC_LIST_ITEM;

public class ServerHandler {
    public static void handleServerMusicListDataPacket(MusicListDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
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

    public static void handleServerDeleteMusicDataPacket(DeleteMusicDataPacket packet, Supplier<NetworkEvent.Context> ctx){
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

    public static void handleServerMoveMusicDataPacket(MoveMusicDataPacket packet, Supplier<NetworkEvent.Context> ctx){
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

    public static void handleServerPlayerPlayPacket(PlayerPlayMusicPacket packet, Supplier<NetworkEvent.Context> ctx){
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }
}
