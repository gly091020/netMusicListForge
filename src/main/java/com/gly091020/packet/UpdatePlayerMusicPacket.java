package com.gly091020.packet;

import net.minecraft.network.FriendlyByteBuf;

public record UpdatePlayerMusicPacket(int index, int slot) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(index);
        buf.writeInt(slot);
    }

    public static UpdatePlayerMusicPacket decode(FriendlyByteBuf buf) {
        return new UpdatePlayerMusicPacket(buf.readInt(), buf.readInt());
    }
}
