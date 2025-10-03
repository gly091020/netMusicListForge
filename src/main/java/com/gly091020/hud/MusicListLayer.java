package com.gly091020.hud;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.util.NetMusicListKeyMapping;
import com.gly091020.item.NetMusicListItem;
import com.gly091020.item.NetMusicPlayerItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

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
        var j = 0;
        var font = Minecraft.getInstance().font;
        var width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        var height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        var max_width = 0;
        var songList = NetMusicListItem.getSongInfoList(disc);
        var pose = guiGraphics.pose();
        var length = NetMusicList.CONFIG.selectHudCount;
        pose.pushPose();

        var scale = NetMusicList.CONFIG.selectHudSize;
        pose.translate(width * (1 - scale), height / 2f * (1 - scale), 0);
        pose.scale(scale, scale, 1);

        count = songList.size();
        if(m == songList.size()){
            isRender = false;
            return;
        }

        if(index - length / 2 < 0){
            for(int l = 0; l < length; l++){
                if(l >= songList.size()){continue;}
                var name = getMusicText(songList.get(l));
                if(font.width(name) > max_width){
                    max_width = font.width(name);
                }
            }
        } else if (index + length / 2 >= songList.size()) {
            for (int l = songList.size() - length; l < songList.size(); l++) {
                if(l < 0){continue;}
                var name = getMusicText(songList.get(l));
                if (font.width(name) > max_width) {
                    max_width = font.width(name);
                }
            }
        }else{
            for(int l = index - length / 2; l < index + length / 2; l++){
                if(l >= songList.size() || l < 0){continue;}
                var name = getMusicText(songList.get(l));
                if(font.width(name) > max_width){
                    max_width = font.width(name);
                }
            }
        }

        if(max_width <= 0){
            isRender = false;
            return;
        }

        var yPos = height / 2 - (length * (font.lineHeight + 2)) / 2;

        RenderSystem.enableBlend();
        guiGraphics.fill(width - max_width - 9, yPos - 4, width - 5, yPos + length * (font.lineHeight + 2), 0x80000000);

        if(index - length / 2 < 0){
            for(int l = 0; l < length; l++){
                if(l >= songList.size()){continue;}
                guiGraphics.drawString(font, getMusicText(songList.get(l)), width - max_width - 7, (yPos + j * (font.lineHeight + 2)), 0xFFFFFF);
                if(l == index){
                    guiGraphics.fill(width - max_width - 7, (yPos + j * (font.lineHeight + 2)) - 2,
                            width - 7, (yPos + (j + 1) * (font.lineHeight + 2)) - 2, 0x80FFFFFF);
                }
                if(songList.get(l).vip){
                    guiGraphics.fill(width - max_width - 7, (yPos + j * (font.lineHeight + 2)) - 2,
                            width - 7, (yPos + (j + 1) * (font.lineHeight + 2)) - 2, 0x50FF0000);
                }
                j++;
            }
        } else if (index + length / 2 >= songList.size()) {
            for(int l = songList.size() - length; l < songList.size(); l++){
                if(l < 0){continue;}
                guiGraphics.drawString(font, getMusicText(songList.get(l)), width - max_width - 7, (yPos + j * (font.lineHeight + 2)), 0xFFFFFF);
                if(l == index){
                    guiGraphics.fill(width - max_width - 7, (yPos + j * (font.lineHeight + 2)) - 2,
                            width - 7, (yPos + (j + 1) * (font.lineHeight + 2)) - 2, 0x80FFFFFF);
                }
                if(songList.get(l).vip){
                    guiGraphics.fill(width - max_width - 7, (yPos + j * (font.lineHeight + 2)) - 2,
                            width - 7, (yPos + (j + 1) * (font.lineHeight + 2)) - 2, 0x50FF0000);
                }
                j++;
            }
        }else{
            for(int l = index - length / 2; l < index + length / 2; l++){
                if(l >= songList.size() || l < 0){continue;}
                guiGraphics.drawString(font, getMusicText(songList.get(l)), width - max_width - 7, (yPos + j * (font.lineHeight + 2)), 0xFFFFFF);
                if(l == index){
                    guiGraphics.fill(width - max_width - 7, (yPos + j * (font.lineHeight + 2)) - 2,
                            width - 7, (yPos + (j + 1) * (font.lineHeight + 2)) - 2, 0x80FFFFFF);
                }
                if(songList.get(l).vip){
                    guiGraphics.fill(width - max_width - 7, (yPos + j * (font.lineHeight + 2)) - 2,
                            width - 7, (yPos + (j + 1) * (font.lineHeight + 2)) - 2, 0x50FF0000);
                }
                j++;
            }
        }

        RenderSystem.disableBlend();
        pose.popPose();
    }

    private static String getMusicText(ItemMusicCD.SongInfo info){
        if(!NetMusicList.CONFIG.selectHudShowArtist){
            return NetMusicListKeyMapping.TOGGLE_MUSIC_TRANSFORM.isDown() ? getTransName(info) : info.songName;
        }
        var artists = new StringBuilder();
        if(info.artists != null && !info.artists.isEmpty()) {
            artists.append("——");
            for(String a: info.artists){
                artists.append(a);
                artists.append(", ");
            }
        }
        return (NetMusicListKeyMapping.TOGGLE_MUSIC_TRANSFORM.isDown() ? getTransName(info) : info.songName) + ((artists.isEmpty())? "":artists.substring(0, artists.length() - 2));
    }

    private static String getTransName(ItemMusicCD.SongInfo info){
        if(info.transName.isEmpty())return info.songName;
        return info.transName;
    }
}
