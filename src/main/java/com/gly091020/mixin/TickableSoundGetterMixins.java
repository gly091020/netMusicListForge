package com.gly091020.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;


public interface TickableSoundGetterMixins {
    @Mixin(SoundEngine.class)
    interface SoundEngineMixin{
        @Accessor("tickingSounds")
        @Final
        List<TickableSoundInstance> getTickableSoundInstances();
    }
    @Mixin(SoundManager.class)
    interface SoundManagerMixin{
        @Accessor
        @Final
        SoundEngine getSoundEngine();
    }
}
