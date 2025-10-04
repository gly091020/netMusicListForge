package com.gly091020.sounds;

import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.gly091020.NetMusicList;
import com.gly091020.block.EnderMusicPlayerEntity;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class EnderPlayerNetMusicSound extends AbstractTickableSoundInstance {
    final BlockPos pos;
    final URL url;
    final int countTick;
    int tick = 0;

    public EnderPlayerNetMusicSound(BlockPos pos, URL songUrl, int second) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.pos = pos;
        this.url = songUrl;
        this.countTick = second * 20;
        this.volume = 4.0F;
        this.x = (float)pos.getX() + 0.5F;
        this.y = (float)pos.getY() + 0.5F;
        this.z = (float)pos.getZ() + 0.5F;
    }

    @Override
    public void tick() {
        Level world = Minecraft.getInstance().level;
        if(world == null){stop();return;}
        tick++;
        if (this.tick > this.countTick + 50) {
            this.stop();
        } else if (world.getGameTime() % 8L == 0L) {
            for(int i = 0; i < 2; ++i) {
                world.addParticle(ParticleTypes.NOTE, this.x - (double)0.5F + world.random.nextDouble(), this.y + world.random.nextDouble() + (double)1.0F, this.z - (double)0.5F + world.random.nextDouble(), world.random.nextGaussian(), world.random.nextGaussian(), (double)world.random.nextInt(3));
            }
        }

        BlockEntity te = world.getBlockEntity(this.pos);
        if (te instanceof EnderMusicPlayerEntity musicPlay) {
            if (!musicPlay.isPlay()) {
                this.stop();
            }
        } else {
            this.stop();
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

    @Override
    public @NotNull Attenuation getAttenuation() {
        return Attenuation.NONE;
    }
}
