package com.gly091020.mixin;

import com.gly091020.client.PauseSoundManager;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
@Implements(@Interface(iface = PauseSoundManager.class, prefix = "pause$"))
public abstract class SoundPausedMixin {
    @Unique
    private boolean netmusiclistforge$isPause = false;

    @Inject(method = "pause", at = @At("HEAD"))
    private void onPause(CallbackInfo ci){
        netmusiclistforge$isPause = true;
    }

    @Inject(method = "resume", at = @At("HEAD"))
    private void onResume(CallbackInfo ci){
        netmusiclistforge$isPause = false;
    }

    @Unique
    public boolean pause$isPaused(){
        return netmusiclistforge$isPause;
    }
}
