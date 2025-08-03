package com.gly091020.packet;

import net.minecraft.network.FriendlyByteBuf;

public record BackpackPlayMusicPacket(int playerID, String url, int timeSecond, String songName, int slot, int upgradeSlot) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerID);
        buf.writeUtf(url);
        buf.writeInt(timeSecond);
        buf.writeUtf(songName);
        buf.writeInt(slot);
        buf.writeInt(upgradeSlot);
    }

    public static BackpackPlayMusicPacket decode(FriendlyByteBuf buf) {
        return new BackpackPlayMusicPacket(
                buf.readInt(), buf.readUtf(), buf.readInt(), buf.readUtf(), buf.readInt(), buf.readInt()
        );
    }
}
