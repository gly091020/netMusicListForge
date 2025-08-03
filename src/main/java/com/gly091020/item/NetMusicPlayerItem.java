// 回来吧我的IAM MUSIC PLAYER
package com.gly091020.item;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.packet.PlayerPlayMusicPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NetMusicPlayerItem extends Item{
    public NetMusicPlayerItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack stack1,
                                            @NotNull Slot slot, @NotNull ClickAction action,
                                            @NotNull Player player, @NotNull SlotAccess access) {
        if(action == ClickAction.SECONDARY && !getContainer(stack).isEmpty()){
            access.set(getContainer(stack).removeItem(0, 1));
            return true;
        }
        if(action == ClickAction.PRIMARY && ItemMusicCD.getSongInfo(stack1) != null){
            getContainer(stack).setItem(0, stack1);
            access.set(ItemStack.EMPTY);
            playSound(stack, player, slot.getSlotIndex());
            return true;
        }
        return super.overrideOtherStackedOnMe(stack, stack1, slot, action, player, access);
    }

    private static void playSound(ItemStack stack, Player player, int slot){
        var i = getContainer(stack).getItem(0);
        if(!i.is(InitItems.MUSIC_CD.get()) && !i.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            return;
        }
        var info = ItemMusicCD.getSongInfo(i);
        stack.getOrCreateTag().putInt("tick", info.songTime * 20);
        if(!player.level().isClientSide){return;}
        NetMusicList.CHANNEL.sendToServer(new PlayerPlayMusicPacket(player.getId(), info.songUrl, info.songTime, info.songName, slot));
    }

    public static MusicPlayerContainer getContainer(ItemStack stack){
        return new MusicPlayerContainer(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        Component t;
        var c = getContainer(stack);
        if(c.isEmpty()){
            t = Component.translatable("item.net_music_player.empty").withStyle(ChatFormatting.RED);
        }else{
            t = c.getItem(0).getHoverName();
        }
        components.add(Component.translatable("item.net_music_player.tip", t));
        var i = getContainer(stack).getItem(0);
        i.getItem().appendHoverText(i, level, components, flag);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.translatable("item.net_music_player.name");
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack stack, @NotNull Slot slot,
                                          @NotNull ClickAction action, @NotNull Player player) {
        var i = getContainer(stack).getItem(0);
        if(ItemMusicCD.getSongInfo(i) != null){
            playSound(stack, player, slot.getSlotIndex());
        }
        return false;
    }

    public static void nextMusic(ItemStack stack, Player player, int slot){
        var i = getContainer(stack).getItem(0);
        if(i.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            NetMusicListItem.nextMusic(i);
        }
        playSound(stack, player, slot);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean b) {
        super.inventoryTick(stack, level, entity, slot, b);
        var t = stack.getOrCreateTag().getInt("tick") - 1;
        if(entity instanceof Player player && 0 < t && t < 16 && t % 5 == 0){
            stack.getOrCreateTag().putInt("tick", -1);
            nextMusic(stack, player, slot);
            return;
        }
        stack.getOrCreateTag().putInt("tick", t);
    }
}
