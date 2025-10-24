package com.gly091020.mixin;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.gly091020.util.CacheManager;
import com.gly091020.util.NetMusicListUtil;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URL;
import java.util.function.Function;

@Mixin(value = MusicPlayManager.class, remap = false)
public abstract class PlayMusicCacheMixin {
    @Shadow
    private static void playMusic(String url, String songName, Function<URL, SoundInstance> sound) {
    }

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private static void onPlayMusic(String url, String songName, Function<URL, SoundInstance> sound, CallbackInfo ci){
        try{
            var id = NetMusicListUtil.getIdFromUrl(url);
            if(CacheManager.hasCache(id)){
                var songUrl = CacheManager.getSongCache(id);
                if(songUrl != null){
                    playMusic(songUrl, songName, sound);
                    ci.cancel();
                }
            }
        } catch (IllegalAccessException ignored) {
        }
    }
}
