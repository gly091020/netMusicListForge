package com.gly091020.mixin;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound;
import com.github.tartaricacid.netmusic.network.message.MusicToClientMessage;
import com.gly091020.util.CacheManager;
import com.gly091020.util.NetMusicListUtil;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MusicToClientMessage.class, remap = false)
public class LyricCacheMixin {
    @Inject(method = "onHandle", at = @At("HEAD"), cancellable = true)
    private static void getLyric(MusicToClientMessage message, CallbackInfo ci){
        // 很烂的Mixin
        // TODO:为什么女仆的代码我不写呢？因为我不会
        var getter = ((UrlGetter)message);
        try {
            var id = NetMusicListUtil.getIdFromUrl(getter.getUrl());
            var lyric = CacheManager.getLycCache(id);
            if(lyric == null)return;
            MusicPlayManager.play(getter.getUrl(), getter.getSongName(), (url) -> new NetMusicSound(getter.getPos(), url, getter.getTimeSecond(), lyric.toLyricRecord()));
            ci.cancel();
        } catch (IllegalAccessException ignored) {

        }
    }

    @Mixin(value = MusicToClientMessage.class, remap = false)
    private interface UrlGetter{
        @Accessor
        String getUrl();
        @Accessor
        BlockPos getPos();
        @Accessor
        int getTimeSecond();
        @Accessor
        String getSongName();
    }
}
