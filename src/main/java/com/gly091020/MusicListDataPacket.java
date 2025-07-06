package com.gly091020;

import net.minecraft.network.FriendlyByteBuf;

public record MusicListDataPacket(int index, PlayMode playMode) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(index);
        buf.writeInt(playMode.ordinal());
    }

    public static MusicListDataPacket decode(FriendlyByteBuf buf) {
        return new MusicListDataPacket(
                buf.readInt(),
                PlayMode.values()[buf.readInt()]
        );
    }
}
