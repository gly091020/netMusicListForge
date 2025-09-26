package com.gly091020.packet;

import net.minecraft.network.FriendlyByteBuf;

public record UpdateMusicTickCTSPacket(int slot, int tick) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(slot);
        buf.writeInt(tick);
    }

    public static UpdateMusicTickCTSPacket decode(FriendlyByteBuf buf) {
        return new UpdateMusicTickCTSPacket(
                buf.readInt(), buf.readInt()
        );
    }
}
