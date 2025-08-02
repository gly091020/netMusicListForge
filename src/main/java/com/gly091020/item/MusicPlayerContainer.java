package com.gly091020.item;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.gly091020.NetMusicList;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MusicPlayerContainer implements Container {
    private final ItemStack stack;
    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);
    public MusicPlayerContainer(ItemStack stack){
        this.stack = stack;
        ContainerHelper.loadAllItems(stack.getOrCreateTagElement("Item"), itemStacks);
    }

    @Override
    public void setChanged() {
        var t = new CompoundTag();
        ContainerHelper.saveAllItems(t, itemStacks);
        stack.getOrCreateTag().put("Item", t);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    public boolean canAddItem(ItemStack stack) {
        return (stack.is(NetMusicList.MUSIC_LIST_ITEM.get()) ||
                stack.is(InitItems.MUSIC_CD.get()))
                && canAddItem(stack);
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return this.canAddItem(stack);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return itemStacks.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public @NotNull ItemStack getItem(int i) {
        return itemStacks.get(i);
    }

    @Override
    public @NotNull ItemStack removeItem(int i, int i1) {
        var r = ContainerHelper.removeItem(itemStacks, i, i1);
        setChanged();
        return r;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int i) {
        var r = ContainerHelper.takeItem(itemStacks, i);
        setChanged();
        return r;
    }

    @Override
    public void setItem(int i, @NotNull ItemStack itemStack) {
        itemStacks.set(i, itemStack);
        setChanged();
    }

    @Override
    public void clearContent() {
        itemStacks.clear();
        setChanged();
    }
}
