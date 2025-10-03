package com.gly091020.packet;

import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class PlayEnderMusicPlayerPacket extends MusicToClientMessage {
    public final BlockPos pos;
    public final String url;
    public final int timeSecond;
    public final String songName;
    public PlayEnderMusicPlayerPacket(BlockPos pos, String url, int timeSecond, String songName) {
        super(pos, url, timeSecond, songName);
        this.pos = pos;
        this.url = url;
        this.timeSecond = timeSecond;
        this.songName = songName;
    }

    public static PlayEnderMusicPlayerPacket decode(FriendlyByteBuf buf) {
        return new PlayEnderMusicPlayerPacket(BlockPos.of(buf.readLong()), buf.readUtf(), buf.readInt(), buf.readUtf());
    }

    public static void encode(PlayEnderMusicPlayerPacket message, FriendlyByteBuf buf) {
        buf.writeLong(message.pos.asLong());
        buf.writeUtf(message.url);
        buf.writeInt(message.timeSecond);
        buf.writeUtf(message.songName);
    }
}
