package com.gly091020.block;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.gly091020.NetMusicList;
import com.gly091020.packet.PlayEnderMusicPlayerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.gly091020.NetMusicList.CHANNEL;

public class EnderMusicPlayerEntity extends BlockEntity {
    private final TileEntityMusicPlayer originalPlayer;
    private static final String PlayerListKey = "players";
    private final List<UUID> players = new ArrayList<>();
    public EnderMusicPlayerEntity(BlockPos blockPos, BlockState blockState) {
        super(NetMusicList.ENDER_MUSIC_PLAYER_TYPE.get(), blockPos, blockState);
        originalPlayer = new TileEntityMusicPlayer(blockPos, blockState);
    }

    public ItemStackHandler getPlayerInv() {
        return originalPlayer.getPlayerInv();
    }

    public boolean isPlay() {
        return originalPlayer.isPlay();
    }

    public void setPlay(boolean play) {
        originalPlayer.setPlay(play);
        setChanged();
    }

    public void setPlayToClient(ItemMusicCD.SongInfo info){
        originalPlayer.setCurrentTime(info.songTime * 20 + 64);
        originalPlayer.setPlay(true);
        if (this.level != null && !this.level.isClientSide) {
            MusicToClientMessage msg = new PlayEnderMusicPlayerPacket(this.worldPosition, info.songUrl, info.songTime, info.songName);
            sendToNearby(this.level, this.worldPosition, msg);
        }
    }

    public void sendToNearby(Level world, BlockPos pos, Object toSend) {
        if (world instanceof ServerLevel ws) {
            ws.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).stream().filter((p) -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < (double)9216.0F).forEach((p) ->
            {
                if(!this.players.isEmpty() && !this.players.contains(p.getUUID()))return;
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), toSend);
            });
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag compound) {
        super.saveAdditional(compound);
        var playersTag = new ListTag();
        players.forEach(uuid -> playersTag.add(StringTag.valueOf(uuid.toString())));

        compound.put(PlayerListKey, playersTag);
        originalPlayer.saveAdditional(compound);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        players.clear();
        if(nbt.contains(PlayerListKey, Tag.TAG_LIST)){
            for (Tag tag: nbt.getList(PlayerListKey, Tag.TAG_STRING)){
                if(tag instanceof StringTag stringTag){
                    try{
                        players.add(UUID.fromString(stringTag.getAsString()));
                    }catch (IllegalArgumentException ignored){}
                }
            }
        }
        originalPlayer.load(nbt);
    }

    public void addPlayer(UUID uuid){
        players.add(uuid);
        markDirty();
    }

    public boolean hasPlayer(UUID uuid){
        return players.contains(uuid);
    }

    public void removePlayer(UUID uuid){
        if(hasPlayer(uuid)){players.remove(uuid);markDirty();}
    }

    public void markDirty(){
        originalPlayer.markDirty();
        setChanged();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        var playersTag = new ListTag();
        players.forEach(uuid -> playersTag.add(StringTag.valueOf(uuid.toString())));
        var tag = new CompoundTag();
        tag.put(PlayerListKey, playersTag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        players.clear();
        if(tag.contains(PlayerListKey, Tag.TAG_LIST)){
            for (Tag tag1: tag.getList(PlayerListKey, Tag.TAG_STRING)){
                if(tag1 instanceof StringTag stringTag){
                    try{
                        players.add(UUID.fromString(stringTag.getAsString()));
                    }catch (IllegalArgumentException ignored){}
                }
            }
        }
        super.handleUpdateTag(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
