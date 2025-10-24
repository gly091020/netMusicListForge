package com.gly091020.hud;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.item.NetMusicListItem;
import com.gly091020.item.NetMusicPlayerItem;
import com.gly091020.util.NetMusicListKeyMapping;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@OnlyIn(Dist.CLIENT)
public class MusicListLayer{
    public static boolean isRender = false;
    public static int index = -1;
    public static int count = 0;

    public static void render(@NotNull GuiGraphics guiGraphics) {
        if(!isRender){
            index = -1;
            return;
        }
        if(Minecraft.getInstance().options.hideGui){return;}
        var p = Minecraft.getInstance().player;
        if(p == null){return;}
        var i = p.getMainHandItem();
        if(!i.is(NetMusicList.MUSIC_PLAYER_ITEM.get())){
            isRender = false;
            return;
        }
        var disc = NetMusicPlayerItem.getContainer(i).getItem(0);
        if(!disc.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            isRender = false;
            return;
        }
        var m = NetMusicListItem.getSongIndex(disc);
        if(index == -1){
            index = m;
        }
        var font = Minecraft.getInstance().font;
        var width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        var height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        var songList = NetMusicListItem.getSongInfoList(disc);
        var pose = guiGraphics.pose();
        var length = NetMusicList.CONFIG.selectHudCount;
        pose.pushPose();

        var scale = NetMusicList.CONFIG.selectHudSize;

        count = songList.size();
        if(m == songList.size()){
            isRender = false;
            return;
        }

        RenderSystem.enableBlend();

        List<Integer> indexList = getIndexList(index, length, count, false);

        // 计算总高度
        int margin = 2;
        float selectedSize = 1.3f;
        float totalHeight = (indexList.size() - 1) * (font.lineHeight + margin) + font.lineHeight * selectedSize;

        pose.translate(width - 5, height / 2f, 0);
        pose.scale(scale, scale, 1);
        pose.translate(0, -totalHeight / 2, 0);

        float y = 0;
        // 计算衰减系数
        float b = 0.45f;
        float v = (float) (1 / Math.pow((Math.E * Math.E * b), 1 / (1 - Math.ceil(length / 2f))));
        for (Integer listIndex : indexList) {
            if (listIndex != null) {
                ItemMusicCD.SongInfo songInfo = songList.get(listIndex);
                String name = getMusicText(songInfo);
                MutableComponent text = Component.literal(name);
                if (songInfo.vip) {
                    text.append(Component.literal(" [VIP]").withStyle(ChatFormatting.RED));
                }
                int textWidth = font.width(text);
                pose.pushPose();
                if (listIndex == index) {
                    pose.translate(-(textWidth * selectedSize + 4), y, 0);
                    pose.scale(selectedSize, selectedSize, 1);
                    drawString(guiGraphics, font, text, 0, 0, 0xFFFFFFFF);
                    y += font.lineHeight * selectedSize + margin;
                } else {
                    float alpha = (float) (b * Math.pow(v, - Math.abs(listIndex - index) + 1));
                    pose.translate(-(textWidth + 4), y, 0);
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
                    drawString(guiGraphics, font, text, 0, 0, 0xFFFFFF);
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                    y += font.lineHeight + margin;
                }
                pose.popPose();
            } else {
                y += font.lineHeight + margin;
            }
        }

        RenderSystem.disableBlend();
        pose.popPose();
    }

    private static void drawString(GuiGraphics guiGraphics, Font font, MutableComponent text, int x, int y, int color) {
        if (NetMusicList.CONFIG.glowingText) {
            final int glowColor = 0xFF000000;
            Matrix4f matrix = guiGraphics.pose().last().pose();
            MultiBufferSource bufferSource = guiGraphics.bufferSource();
            font.drawInBatch8xOutline(text.getVisualOrderText(), x, y, color, glowColor, matrix, bufferSource, 0xF000F0);
        }
        guiGraphics.drawString(font, text, x, y, color, !NetMusicList.CONFIG.glowingText);
    }

    private static String getMusicText(ItemMusicCD.SongInfo info){
        if(!NetMusicList.CONFIG.selectHudShowArtist){
            return NetMusicListKeyMapping.TOGGLE_MUSIC_TRANSFORM.isDown() ? getTransName(info) : info.songName;
        }
        var artists = new StringBuilder();
        if(info.artists != null && !info.artists.isEmpty()) {
            artists.append("——");
            String join = String.join(", ", info.artists);
            artists.append(join);
        }
        return (NetMusicListKeyMapping.TOGGLE_MUSIC_TRANSFORM.isDown() ? getTransName(info) : info.songName) + artists;
    }

    private static String getTransName(ItemMusicCD.SongInfo info){
        if(info.transName.isEmpty())return info.songName;
        return info.transName;
    }

    private static List<Integer> getIndexList(int current, int length, int total, boolean loop) {
        if (current < 0 || current > total - 1) {
            return IntStream.range(0, length).mapToObj(i -> (Integer) null).toList();
        }
        List<Integer> indexList = new ArrayList<>();
        int half = length / 2;
        for (int i = -half; i <= half; i++) {
            int idx = current + i;
            if (loop) {
                idx = (idx + total) % total;
            } else {
                if (idx < 0 || idx >= total) {
                    indexList.add(null);
                    continue;
                }
            }
            indexList.add(idx);
        }
        return indexList;
    }
}
