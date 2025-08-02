package com.gly091020.mixin;

import com.github.tartaricacid.netmusic.compat.tlm.backpack.data.MusicPlayerBackpackData;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidMusicToClientMessage;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.NetMusicList;
import com.gly091020.item.NetMusicListItem;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MusicPlayerBackpackData.class)
public abstract class PlayNextSongMixin {
    @Shadow(remap = false) private int selectSlotId;

    @Shadow(remap = false) protected abstract boolean playMusic(EntityMaid entityMaid, CombinedInvWrapper availableInv, int slotId);

    @Shadow(remap = false) private int playTick;

    @Inject(method = "playNextSong", at = @At("HEAD"), remap = false, cancellable = true)
    public void nextSong(EntityMaid entityMaid, CallbackInfo ci){
        CombinedInvWrapper availableInv = entityMaid.getAvailableInv(false);
        var i = availableInv.getStackInSlot(this.selectSlotId + 6);
        if(i.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            NetMusicListItem.nextMusic(i);
            if(this.playMusic(entityMaid, availableInv, this.selectSlotId + 6)){
                ci.cancel();
            }
        }
    }

    @Inject(method = "playMusic", at = @At("HEAD"), cancellable = true, remap = false)
    public void playThis(EntityMaid entityMaid, CombinedInvWrapper availableInv, int slotId, CallbackInfoReturnable<Boolean> cir){
        var i = availableInv.getStackInSlot(slotId);
        if(i.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            ItemMusicCD.SongInfo info = NetMusicListItem.getSongInfo(i);
            if (info != null) {
                this.playTick = info.songTime * 20 + 64;
                MaidMusicToClientMessage msg = new MaidMusicToClientMessage(entityMaid.getId(), info.songUrl, info.songTime, info.songName);
                NetworkHandler.sendToNearby(entityMaid.level(), entityMaid.blockPosition(), msg);
                cir.setReturnValue(true);
            }
        }
    }
}
