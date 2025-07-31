package com.gly091020.mixin;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.inventory.ComputerMenu;
import com.gly091020.NetMusicList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ComputerMenu.class)
public class CanBurnerCDListComputerMixin {
    @Shadow(remap = false)
    private final ItemStackHandler input = new ItemStackHandler() {
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(InitItems.MUSIC_CD.get()) || stack.is(NetMusicList.MUSIC_LIST_ITEM.get());
        }
    };
}
