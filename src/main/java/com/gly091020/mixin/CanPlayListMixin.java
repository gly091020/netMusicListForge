package com.gly091020.mixin;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.NetMusicListItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemMusicCD.class)
public abstract class CanPlayListMixin {
    @Inject(method = "setSongInfo", at = @At("HEAD"), cancellable = true, remap = false)
    private static void setInfo(ItemMusicCD.SongInfo info, ItemStack stack, CallbackInfoReturnable<ItemStack> cir){
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            cir.setReturnValue(NetMusicListItem.setSongInfo(info, stack));
        }
    }

    @Inject(method = "getSongInfo", at = @At("HEAD"), cancellable = true, remap = false)
    private static void getInfo(ItemStack stack, CallbackInfoReturnable<ItemMusicCD.SongInfo> cir){
        if(stack.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            cir.setReturnValue(NetMusicListItem.getSongInfo(stack));
        }
    }
}
