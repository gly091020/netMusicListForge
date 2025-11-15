package com.gly091020.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record PlayEnderMusicPlayerPacket(BlockPos pos, String url, int timeSecond, String songName) {

    public static PlayEnderMusicPlayerPacket decode(FriendlyByteBuf buf) {
        return new PlayEnderMusicPlayerPacket(BlockPos.of(buf.readLong()), buf.readUtf(), buf.readInt(), buf.readUtf());
    }

    public static void encode(PlayEnderMusicPlayerPacket message, FriendlyByteBuf buf) {
        buf.writeLong(message.pos.asLong());
        buf.writeUtf(message.url);
        buf.writeInt(message.timeSecond);
        buf.writeUtf(message.songName);
    }
}
