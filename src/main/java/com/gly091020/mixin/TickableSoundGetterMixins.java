package com.gly091020.mixin;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;


public interface TickableSoundGetterMixins {
    @Mixin(SoundEngine.class)
    interface SoundEngineMixin{
        @Accessor("tickingSounds")
        @Final
        List<TickableSoundInstance> getTickableSoundInstances();

        @Accessor("channelAccess")
        @Final
        ChannelAccess getChannelAccess();
    }
    @Mixin(SoundManager.class)
    interface SoundManagerMixin{
        @Accessor
        @Final
        SoundEngine getSoundEngine();
    }
    @Mixin(Channel.class)
    interface ChannelMixin{
        @Invoker("getState")
        int invokeGetState();
    }
}
