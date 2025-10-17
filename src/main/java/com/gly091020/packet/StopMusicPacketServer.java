package com.gly091020.packet;

import net.minecraft.network.FriendlyByteBuf;

public record StopMusicPacketServer(int playerID, String url) {
    // 玩家间的背包是不完全同步的！

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerID);
        buf.writeUtf(url);
    }
    public static StopMusicPacketServer decode(FriendlyByteBuf buf) {
        return new StopMusicPacketServer(buf.readInt(), buf.readUtf());
    }
}
