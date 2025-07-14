package com.gly091020.mixin;

import com.github.tartaricacid.netmusic.compat.tlm.inventory.MusicPlayerBackpackContainer;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.gly091020.NetMusicList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MusicPlayerBackpackContainer.class)
public abstract class MusicPlayerBackpackMixin {
    @Redirect(method = "addBackpackInv", at = @At(value = "INVOKE", target = "Lcom/github/tartaricacid/netmusic/compat/tlm/inventory/MusicPlayerBackpackContainer;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;"))
    public Slot putSlot(MusicPlayerBackpackContainer instance, Slot inputSlot){
        // 这样mixin很差，但我找不到更好的方式
        return ((AbstractContainerMenuMixin) instance).invokeAddSlot(new SlotItemHandler(instance.getMaid().getMaidInv(), inputSlot.getSlotIndex(), inputSlot.x, inputSlot.y) {
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(InitItems.MUSIC_CD.get()) || stack.is(NetMusicList.MUSIC_LIST_ITEM.get());
            }

            @OnlyIn(Dist.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return inputSlot.getNoItemIcon();
            }
        });
    }

    @Redirect(method = "playMusic", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    public boolean canInsert(ItemStack instance, Item item){
        return instance.is(item) || instance.is(NetMusicList.MUSIC_LIST_ITEM.get());
    }
}
