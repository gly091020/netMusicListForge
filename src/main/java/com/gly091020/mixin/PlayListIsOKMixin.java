package com.gly091020.mixin;

import com.github.tartaricacid.netmusic.inventory.MusicPlayerInv;
import com.gly091020.NetMusicList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MusicPlayerInv.class)
public class PlayListIsOKMixin {
    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true, remap = false)
    public void isOK(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        if(stack.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            cir.setReturnValue(true);
        }
    }
}
