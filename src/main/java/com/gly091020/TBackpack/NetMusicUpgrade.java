package com.gly091020.TBackpack;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.item.NetMusicListItem;
import com.gly091020.packet.BackpackPlayMusicPacket;
import com.tiviacz.travelersbackpack.client.screens.BackpackScreen;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tiviacz.travelersbackpack.inventory.menu.BackpackBaseMenu;
import com.tiviacz.travelersbackpack.inventory.menu.slot.UpgradeSlotItemHandler;
import com.tiviacz.travelersbackpack.inventory.upgrades.ITickableUpgrade;
import com.tiviacz.travelersbackpack.inventory.upgrades.Point;
import com.tiviacz.travelersbackpack.inventory.upgrades.UpgradeBase;
import com.tiviacz.travelersbackpack.util.NbtHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NetMusicUpgrade extends UpgradeBase<NetMusicUpgrade> implements ITickableUpgrade {
    public ItemStackHandler handler;
    public NetMusicUpgrade(UpgradeManager manager, int dataHolderSlot, NonNullList<ItemStack> disks) {
        super(manager, dataHolderSlot, new Point(66, 46));
        this.handler = new ItemStackHandler(disks){
            @Override
            public void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                var s = manager.getUpgradesHandler().getStackInSlot(dataHolderSlot).copy();
                if(!s.isEmpty()){
                    NbtHelper.update(s, "Inventory", 1, slot, handler.getStackInSlot(slot));
                    getUpgradeManager().getUpgradesHandler().setStackInSlot(dataHolderSlot, s);
                }
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return stack.is(InitItems.MUSIC_CD.get()) ||
                        stack.is(NetMusicList.MUSIC_LIST_ITEM.get());
            }
        };
    }

    @Override
    public NetMusicUpgradeWidget createWidget(BackpackScreen backpackScreen, int x, int y) {
        return new NetMusicUpgradeWidget(backpackScreen, this,
                new Point(backpackScreen.getGuiLeft() + x, backpackScreen.getGuiTop() + y));
    }

    @Override
    public List<? extends Slot> getUpgradeSlots(BackpackBaseMenu backpackBaseMenu, BackpackWrapper backpackWrapper, int x, int y) {
        return List.of(new UpgradeSlotItemHandler<NetMusicUpgrade>(this, handler, 0, x + 7, y + 23){
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return super.mayPlace(stack) || handler.isItemValid(0, stack);
            }
        });
    }

    public static NonNullList<ItemStack> getContainer(ItemStack stack){
        return NbtHelper.getOrDefault(stack, "Inventory",
                NonNullList.withSize(1, ItemStack.EMPTY));
    }

    public void nextMusic(ItemStack stack, Player player, int slot){
        var i = getContainer(stack).get(0);
        if(i.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            NetMusicListItem.nextMusic(i);
        }
        playSound(stack, player, slot);
    }

    private void playSound(ItemStack stack, Player player, int slot){
        var i = getContainer(stack).get(0);
        if(!i.is(InitItems.MUSIC_CD.get()) && !i.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            return;
        }
        var info = ItemMusicCD.getSongInfo(i);
        stack.getOrCreateTag().putInt("tick", info.songTime * 20);
        if(!player.level().isClientSide){return;}
        NetMusicList.CHANNEL.sendToServer(new BackpackPlayMusicPacket(player.getId(), info.songUrl, info.songTime, info.songName, slot, this.dataHolderSlot));
    }


    @Override
    public void tick(@Nullable Player player, Level level, BlockPos blockPos, int i) {
        // 模组作者小时候用电脑要把电脑放到背上才能用
        // 我可能要选择精妙背包了
        // todo:由于一些问题，暂未实现播放列表切歌
        var s = this.getUpgradeManager().getUpgradesHandler().getStackInSlot(dataHolderSlot).copy();
        if(s.isEmpty()){return;}
        var t = NbtHelper.getOrDefault(s, "tick", -1) - 1;
        if(0 < t && t < 16 && t % 5 == 0){
            NbtHelper.set(s, "tick", -1);
            nextMusic(getContainer(s).get(0), player, this.getUpgradeManager().wrapper.getBackpackSlotIndex());
            return;
        }
        NbtHelper.set(s, "tick", t);
    }

    @Override
    public int getTickRate() {
        return 1;
    }

    @Override
    public void onUpgradeRemoved(ItemStack removedStack) {
        NbtHelper.set(removedStack, "tick", -1);
    }
}
