package com.gly091020.packet;

import net.minecraft.network.FriendlyByteBuf;

public record PlayerPlayMusicPacket(int playerID, String url, int timeSecond, String songName, int slot) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerID);
        buf.writeUtf(url);
        buf.writeInt(timeSecond);
        buf.writeUtf(songName);
        buf.writeInt(slot);
    }

    public static PlayerPlayMusicPacket decode(FriendlyByteBuf buf) {
        return new PlayerPlayMusicPacket(
                buf.readInt(), buf.readUtf(), buf.readInt(), buf.readUtf(), buf.readInt()
        );
    }
}
