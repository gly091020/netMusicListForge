package com.gly091020.packet;

import com.gly091020.item.MusicPlayerContainer;
import com.gly091020.item.NetMusicListItem;
import com.gly091020.item.NetMusicPlayerItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

import static com.gly091020.NetMusicList.*;

public class ServerHandler {
    public static void handleServerMusicListDataPacket(MusicListDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().getReceptionSide().isClient()){return;}
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
        if(ctx.get().getDirection().getReceptionSide().isClient()){return;}
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
        if(ctx.get().getDirection().getReceptionSide().isClient()){return;}
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
        if(ctx.get().getDirection().getReceptionSide().isClient()){return;}
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void handleServerUpdateMusicPacket(UpdatePlayerMusicPacket packet, Supplier<NetworkEvent.Context> ctx){
        var player = ctx.get().getSender();
        if(player == null){return;}
        var stack = player.getInventory().getItem(packet.slot());
        if(stack.is(MUSIC_PLAYER_ITEM.get())){
            var container = new MusicPlayerContainer(stack);
            var stack1 = container.getItem(0);
            NetMusicListItem.setSongIndex(stack1, packet.index());
            container.setItem(0, stack1);
            NetMusicPlayerItem.playSound(stack, player, packet.slot());
        }
    }

    public static void handlePlayerUpdateTickPacket(UpdateMusicTickCTSPacket packet, Supplier<NetworkEvent.Context> ctx){
        var player = ctx.get().getSender();
        if(player == null){return;}
        var stack = player.getInventory().getItem(packet.slot());
        if(stack.is(MUSIC_PLAYER_ITEM.get())){
            stack.getOrCreateTag().putInt("tick", packet.tick());
        }
    }
}
