package com.gly091020.mixin;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.gly091020.NetMusicList;
import com.gly091020.item.NetMusicListItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityMusicPlayer.class)
public abstract class PlayModeMixin {
    @Inject(method = "tick", at = @At("RETURN"), remap = false)
    private static void nextMusic(Level level, BlockPos blockPos, BlockState blockState, TileEntityMusicPlayer te, CallbackInfo ci){
        if(!te.isPlay() && te.getPlayerInv().getStackInSlot(0).is(NetMusicList.MUSIC_LIST_ITEM.get())){
            ItemStack stackInSlot = te.getPlayerInv().getStackInSlot(0);
            if (stackInSlot.isEmpty()) {
                return;
            }

            te.setPlay(true);
            te.markDirty();
            NetMusicListItem.nextMusic(stackInSlot);
            ItemMusicCD.SongInfo songInfo = NetMusicListItem.getSongInfo(stackInSlot);
            if (songInfo != null) {
                te.setPlayToClient(songInfo);
            }
        }
    }
}
