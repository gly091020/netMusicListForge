package com.gly091020.packet;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public record PlayerPlayMusicPacket(int playerID, String url, int timeSecond, String songName, int slot, ItemMusicCD.SongInfo info) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerID);
        buf.writeUtf(url);
        buf.writeInt(timeSecond);
        buf.writeUtf(songName);
        buf.writeInt(slot);
        var tag = new CompoundTag();
        ItemMusicCD.SongInfo.serializeNBT(info, tag);
        buf.writeNbt(tag);
    }

    public static PlayerPlayMusicPacket decode(FriendlyByteBuf buf) {
        return new PlayerPlayMusicPacket(
                buf.readInt(), buf.readUtf(), buf.readInt(), buf.readUtf(), buf.readInt(),
                ItemMusicCD.SongInfo.deserializeNBT(Objects.requireNonNull(buf.readNbt()))
        );
    }
}
