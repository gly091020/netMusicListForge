package com.gly091020.item;

import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.util.PlayMode;
import com.gly091020.client.OldMusicSelectionScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NetMusicListItem extends ItemMusicCD {
    private static final String listKey = "NetMusicSongInfoList";
    public NetMusicListItem() {
        super();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    public static List<SongInfo> getSongInfoList(ItemStack stack) {
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            CompoundTag tag = stack.getOrCreateTag();
            if(tag.contains(listKey)){
                var l = new ArrayList<SongInfo>();
                for(Object compound: tag.getList(listKey, Tag.TAG_COMPOUND).toArray()){
                    var c1 = ((CompoundTag)compound);
                    l.add(SongInfo.deserializeNBT(c1));
                }
                return l;
            }
        }

        return new ArrayList<>();
    }

    public static void nextMusic(ItemStack stack){
        if(stack.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            switch (getPlayMode(stack)){
                case RANDOM -> {
                    var i = RandomSource.create().nextInt(0, getSongInfoList(stack).size() - 1);
                    setSongIndex(stack, i);
                }
                case SEQUENTIAL -> {
                    var i = getSongIndex(stack) + 1;
                    if(i >= getSongInfoList(stack).size()){
                        i = 0;
                    }
                    setSongIndex(stack, i);
                }
            }
        }
    }

    public static SongInfo getSongInfo(ItemStack stack) {
        var l = getSongInfoList(stack);
        if(l.isEmpty()){
            return null;
        }
        var i = getSongIndex(stack);
        if(i >= 0 && i >= l.size()){return null;}
        if(i < 0){return null;}
        return l.get(i);
    }

    public static Integer getSongIndex(ItemStack stack){
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            var l = stack.getOrCreateTag();
            if (!l.contains("index")) {
                var l1 = new CompoundTag();
                l1.putInt("index", 0);
                var l2 = getSongInfoList(stack);
                var nl1 = new ListTag();
                for (SongInfo songInfo : l2) {
                    var n1 = new CompoundTag();
                    SongInfo.serializeNBT(songInfo, n1);
                    nl1.add(n1);
                }
                l1.put(listKey, nl1);
                stack.setTag(l1);
                return 0;
            }else{
                return l.getInt("index");
            }
        }
        return -1;
    }

    public static void deleteSong(ItemStack stack, int index){
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            var n = stack.getOrCreateTag();
            if(n.contains(listKey)){
                var l = n.getList(listKey, Tag.TAG_COMPOUND);
                l.remove(index);
                n.put(listKey, l);
                n.putInt("index", 0);
                stack.setTag(n);
            }
        }
    }

    public static void moveSong(ItemStack stack, int from, int to){
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            var n = stack.getOrCreateTag();
            if (n.contains(listKey)) {
                var l = n.getList(listKey, Tag.TAG_COMPOUND);
                var j = l.get(from);
                l.set(from, l.get(to));
                l.set(to, j);
            }
        }
    }

    public static void setSongIndex(ItemStack stack, Integer index){
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            var n = stack.getOrCreateTag();
            n.putInt("index", index);
            stack.setTag(n);
        }
    }

    public static ItemStack setSongInfo(SongInfo info, ItemStack stack) {
        if (stack.is(NetMusicList.MUSIC_LIST_ITEM.get())) {
            var l = getSongInfoList(stack);
            CompoundTag oldCompound = new CompoundTag();
            {
                var l1 = new ListTag();
                for(SongInfo songInfo: l){
                    var n1 = new CompoundTag();
                    SongInfo.serializeNBT(songInfo, n1);
                    l1.add(n1);
                }
                oldCompound.put(listKey, l1);
            }

            CompoundTag tag = stack.getOrCreateTag();
            var l1 = getSongInfoList(stack);
            if(getSongIndex(stack) >= l1.size()){
                var nl = tag.getList(listKey, Tag.TAG_COMPOUND);
                var sn = new CompoundTag();
                SongInfo.serializeNBT(info, sn);
                nl.add(sn);
                tag.put(listKey, nl);
                setSongIndex(stack, l1.size() + 1);
                stack.setTag(tag);
                return stack;
            }
            var d = l1.get(getSongIndex(stack));
            if (d == null){
                return stack;
            }

            var nl = tag.getList(listKey, Tag.TAG_COMPOUND);
            var sn = new CompoundTag();
            SongInfo.serializeNBT(info, sn);
            nl.set(getSongIndex(stack), sn);
            tag.put(listKey, nl);
            stack.setTag(tag);
        }

        return stack;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> componentList, @NotNull TooltipFlag flag) {
        String name;
        String text;
        name = Component.translatable("tooltip.net_music_list.play_mode").getString();
        text = "§a▍ §7" + name + ": §6" + getPlayMode(stack).getName().getString();
        if(getSongInfoList(stack).isEmpty()){
            componentList.add(Component.translatable("tooltips.netmusic.cd.empty").withStyle(ChatFormatting.RED));
        }

        componentList.add(Component.literal(text));
        SongInfo info = getSongInfo(stack);
        Language language = Language.getInstance();
        if (info != null) {
            if(info.transName != null && !info.transName.isEmpty()){
                name = language.getOrDefault("tooltips.netmusic.cd.trans_name");
                text = "§a▍ §7" + name + ": §6" + info.transName;
                componentList.add(Component.literal(text));
            }

            if (info.artists != null && !info.artists.isEmpty()) {
                text = StringUtils.join(info.artists, " | ");
                name = language.getOrDefault("tooltips.netmusic.cd.artists");
                text = "§a▍ §7" + name + ": §3" + text;
                componentList.add(Component.literal(text));
            }

            name = language.getOrDefault("tooltips.netmusic.cd.time");
            text = "§a▍ §7" + name + ": §5" + this.getSongTime(info.songTime);
            componentList.add(Component.literal(text));
        }
    }

    private String getSongTime(int songTime) {
        int min = songTime / 60;
        int sec = songTime % 60;
        String minStr = min <= 9 ? "0" + min : "" + min;
        String secStr = sec <= 9 ? "0" + sec : "" + sec;
        String format = Language.getInstance().getOrDefault("tooltips.netmusic.cd.time.format");
        return String.format(format, minStr, secStr);
    }

    public static PlayMode getPlayMode(ItemStack stack){
        var n = stack.getOrCreateTag();
        if(!n.contains("play_mode")){setPlayMode(stack, PlayMode.LOOP);return PlayMode.LOOP;}
        return PlayMode.getMode(n.getInt("play_mode"));
    }

    public static void setPlayMode(ItemStack stack, PlayMode mode){
        var n = stack.getOrCreateTag();
        n.putInt("play_mode", mode.ordinal());
        stack.setTag(n);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if(context.getPlayer() == null){return InteractionResult.PASS;}
        var stack = context.getPlayer().getMainHandItem();
        if(!stack.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            return InteractionResult.PASS;
        }
        if(context.getLevel().getBlockState(context.getClickedPos()).is(InitBlocks.MUSIC_PLAYER.get())){
            if(getSongInfoList(stack).isEmpty()){return InteractionResult.PASS;}
            if(getSongIndex(stack) >= getSongInfoList(stack).size()){
                setSongIndex(stack, getSongInfoList(stack).size() - 1);
            }
            return InteractionResult.PASS;
        }
        if(context.getLevel().isClientSide){
            var l = getSongInfoList(stack);
            // 在我搞懂blitNineSliced的十几个参数是什么意思前我不会换新ui的
            OldMusicSelectionScreen.open(l, getPlayMode(stack), getSongIndex(stack));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if(!stack.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            return InteractionResultHolder.pass(stack);
        }
        if(level.isClientSide){
            var l = getSongInfoList(stack);
            // 在我搞懂blitNineSliced的十几个参数是什么意思前我不会换新ui的
            OldMusicSelectionScreen.open(l, getPlayMode(stack), getSongIndex(stack));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        if(Objects.equals(super.getName(stack), Component.translatable(getDescriptionId(stack)))){
            return Component.translatable("item.net_music_list.name", getSongInfoList(stack).size());
        }
        return Component.translatable("item.net_music_list.info", super.getName(stack));
    }
}
