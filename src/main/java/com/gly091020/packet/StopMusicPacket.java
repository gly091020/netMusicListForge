package com.gly091020.packet;

import net.minecraft.network.FriendlyByteBuf;

public record StopMusicPacket(int playerID, String url) {
    // 玩家间的背包是不完全同步的！
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerID);
        buf.writeUtf(url);
    }

    public static StopMusicPacket decode(FriendlyByteBuf buf) {
        return new StopMusicPacket(buf.readInt(), buf.readUtf());
    }
}
