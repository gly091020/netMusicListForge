package com.gly091020.mixin;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.item.NetMusicListItem;
import com.gly091020.util.PlayMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.gly091020.NetMusicList.CONFIG;

@Mixin(value = CDBurnerMenu.class, remap = false)
public class CanBurnerCDListMixin {
    @Shadow
    private final ItemStackHandler input = new ItemStackHandler() {
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(InitItems.MUSIC_CD.get()) || stack.is(NetMusicList.MUSIC_LIST_ITEM.get());
        }
    };

    @Shadow
    @Final
    private ItemStackHandler output;

    @Inject(method = "setSongInfo", at = @At("HEAD"), cancellable = true)
    public void onSetInfo(ItemMusicCD.SongInfo setSongInfo, CallbackInfo ci){
        // 列表刻录暴力适配
        if(input.getStackInSlot(0).is(NetMusicList.MUSIC_LIST_ITEM.get())){
            if(!input.getStackInSlot(0).getOrCreateTag().contains("index")){
                NetMusicListItem.setSongIndex(input.getStackInSlot(0), 0);
                NetMusicListItem.setPlayMode(input.getStackInSlot(0), PlayMode.LOOP);
            }
        }
        if (input.getStackInSlot(0).isEmpty() && output.getStackInSlot(0).is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            input.setStackInSlot(0, output.getStackInSlot(0));
            output.setStackInSlot(0, Items.AIR.getDefaultInstance());
        }
        if(NetMusicListItem.getSongInfoList(input.getStackInSlot(0)).size() >= CONFIG.maxImportList){
            ci.cancel();
        }
    }
}
