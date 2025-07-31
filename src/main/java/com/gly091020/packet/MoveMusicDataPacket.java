package com.gly091020.packet;

import net.minecraft.network.FriendlyByteBuf;

public record MoveMusicDataPacket(int fromIndex, int toIndex) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(fromIndex);
        buf.writeInt(toIndex);
    }

    public static MoveMusicDataPacket decode(FriendlyByteBuf buf) {
        return new MoveMusicDataPacket(buf.readInt(), buf.readInt());
    }
}
