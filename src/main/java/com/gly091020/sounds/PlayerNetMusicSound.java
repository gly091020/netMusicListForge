// 部分参考MaidNetMusicSound，以保持与女仆行为一致
package com.gly091020.sounds;

import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.item.NetMusicListItem;
import com.gly091020.item.NetMusicPlayerItem;
import com.gly091020.packet.StopMusicPacketServer;
import com.gly091020.packet.UpdateMusicTickCTSPacket;
import com.gly091020.util.NetMusicListUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PlayerNetMusicSound extends AbstractTickableSoundInstance {
    final Player player;
    final URL url;
    final int countTick;
    int tick = 0;
    final int slot;

    @Nullable
    String clientUrl;

    public PlayerNetMusicSound(Player player, URL songUrl, int second, int slot) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.url = songUrl;
        this.countTick = second * 20;
        this.volume = 4f;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.slot = slot;

        relative = isClientPlayer();
        attenuation = isClientPlayer() ? Attenuation.NONE : Attenuation.LINEAR;
    }

    @Override
    public void tick() {
        if(player.isRemoved()){
            stopMusic();
        }

        if(isClientPlayer()){
            var itemStack = player.getInventory().getItem(slot);
            if (!itemStack.is(NetMusicList.MUSIC_PLAYER_ITEM.get())) {
                stopMusic();
            }
            itemStack = NetMusicPlayerItem.getContainer(itemStack).getItem(0);

            if (itemStack.is(InitItems.MUSIC_CD.get())) {
                if (ItemMusicCD.getSongInfo(itemStack) == null) {
                    stopMusic();
                }
            } else if (itemStack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
                var info = NetMusicListItem.getSongInfo(itemStack);
                if (info == null) {
                    stopMusic();
                } else {
                    if (clientUrl == null) clientUrl = info.songUrl;
                    if (!Objects.equals(clientUrl, info.songUrl)) {
                        stopMusic();
                    }
                }
            } else {
                stopMusic();
            }
        }

        ClientLevel level = Minecraft.getInstance().level;
        if(level == null){
            stopMusic();
        }else{
            ++this.tick;
            if (this.tick > this.countTick + 50){
                stopMusic();
            }else{
                this.x = player.getX();
                this.y = player.getY();
                this.z = player.getZ();
                if (level.getGameTime() % 8L == 0L) {
                    for(int i = 0; i < 2; ++i) {
                        level.addParticle(ParticleTypes.NOTE, this.x - (double)0.5F + level.random.nextDouble(), this.y + (double)1.5F + level.random.nextDouble(), this.z - (double)0.5F + level.random.nextDouble(), level.random.nextGaussian(), level.random.nextGaussian(), level.random.nextInt(3));
                    }
                }
            }
        }

        if(tick % 20 == 0 && isClientPlayer()){
            NetMusicList.CHANNEL.sendToServer(new UpdateMusicTickCTSPacket(slot, countTick - tick));
        }

        if(isStopped() && isClientPlayer()){
            NetMusicList.CHANNEL.sendToServer(new UpdateMusicTickCTSPacket(slot, -1));
        }

        if(NetMusicListUtil.globalStopMusic){
            this.volume = 0;
        }else{
            this.volume = 4f;
        }
    }

    public void stopMusic(){
        if(!isStopped() && isClientPlayer()){
            NetMusicList.CHANNEL.sendToServer(new StopMusicPacketServer(player.getId(), url.toString()));
        }
        stop();
    }

    public void onlyTickUpdate(){
        tick++;
        if(tick % 20 == 0 && isClientPlayer()){
            NetMusicList.CHANNEL.sendToServer(new UpdateMusicTickCTSPacket(slot, countTick - tick));
        }

        if(isStopped() && isClientPlayer()){
            NetMusicList.CHANNEL.sendToServer(new UpdateMusicTickCTSPacket(slot, -1));
        }
    }

    @Override
    public @NotNull CompletableFuture<AudioStream> getStream(@NotNull SoundBufferLibrary soundBuffers, @NotNull Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new NetMusicAudioStream(url);
            } catch (UnsupportedAudioFileException | IOException e) {
                NetMusicList.LOGGER.error("出现错误：", e);
                return null;
            }
        }, Util.backgroundExecutor());
    }

    public boolean isClientPlayer(){
        if (Minecraft.getInstance().player != null) {
            return player.getUUID() == Minecraft.getInstance().player.getUUID();
        }
        return false;
    }

    public Player getPlayer() {
        return player;
    }

    public String getClientUrl() {
        return clientUrl;
    }
}
