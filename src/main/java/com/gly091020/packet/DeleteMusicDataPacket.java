package com.gly091020.packet;

import net.minecraft.network.FriendlyByteBuf;

public record DeleteMusicDataPacket(int index) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(index);
    }

    public static DeleteMusicDataPacket decode(FriendlyByteBuf buf) {
        return new DeleteMusicDataPacket(
                buf.readInt()
        );
    }
}
