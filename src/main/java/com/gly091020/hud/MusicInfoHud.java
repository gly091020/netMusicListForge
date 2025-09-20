package com.gly091020.hud;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.NetMusicListUtil;
import com.gly091020.item.NetMusicPlayerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class MusicInfoHud{
    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "textures/gui/default.png");

    private static ItemMusicCD.SongInfo info;
    private static ResourceLocation icon;
    private static long id;
    private static NetMusicListUtil.Lyric lyric;
    private static ItemStack stack;
    private static int slot;
    private static int left = 10;
    private static int top = 10;

    private static Thread thread;

    public static void render(@NotNull GuiGraphics guiGraphics) {
        if(Minecraft.getInstance().options.hideGui){return;}
        if(info == null){return;}

        var font = Minecraft.getInstance().font;
        guiGraphics.blit(Objects.requireNonNullElse(icon, DEFAULT_TEXTURE), left, top, 0, 0, 40, 40, 40, 40);
        var text = "";
        if(info.transName.isEmpty()){
            text = info.songName;
        }else{
            text = String.format("%s(%s)", info.songName, info.transName);
        }
        guiGraphics.drawString(font, text, left + 50, top, 0xFFFFFFFF);

        var count = info.songTime;
        if(stack != null){
            int tick;
            if (Minecraft.getInstance().player != null) {
                var stack1 = Minecraft.getInstance().player.getInventory().getItem(slot);
                if(!stack1.is(NetMusicList.MUSIC_PLAYER_ITEM.get()) || NetMusicPlayerItem.getContainer(stack1).isEmpty()){
                    clearInfo();
                    return;
                }
                tick = stack1.getOrCreateTag().getInt("tick");
            }else{
                clearInfo();
                return;
            }

            var tickWidth = 100;
            guiGraphics.fill(left + 50, top + font.lineHeight + 4, left + 50 + tickWidth, top + font.lineHeight + 6, 0xFFAAAAAA);
            guiGraphics.fill(left + 50, top + font.lineHeight + 4, (int) (left + 50 + tickWidth * clamp((count - tick / 20f) / count, 0, 1)), top + font.lineHeight + 6, 0xFFFFFFFF);
            guiGraphics.drawString(font, String.format("%s/%s", NetMusicListUtil.secondsToMinutesSeconds((int) (count - (tick / 20f))), NetMusicListUtil.secondsToMinutesSeconds(count)), left + 50 + tickWidth + 5, top + font.lineHeight + 1, 0xFFFFFFFF);

            if(lyric != null){
                var lyricPart = lyric.getLyric(clamp(count - tick / 20f, 0, Float.MAX_VALUE));
                guiGraphics.drawString(font, lyricPart.getA(), left + 50, top + (font.lineHeight * 2 + 1), 0xFFFFFFFF);
                if (lyricPart.getB() != null && !lyricPart.getB().isEmpty()) {
                    guiGraphics.drawString(font, lyricPart.getB(), left + 50, top + (font.lineHeight + 1) * 3, 0xFFFFFFFF);
                }
            }
        }
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static ItemMusicCD.SongInfo getInfo() {
        return info;
    }

    public static void setPos(int x, int y){
        left = x;
        top = y;
    }

    public static void setInfo(ItemMusicCD.SongInfo info, @NotNull ItemStack playerStack, int slot){
        MusicInfoHud.info = info;
        MusicInfoHud.slot = slot;
        if(thread != null){
            thread.interrupt();
            thread = null;
        }
        if(icon != null){
            Minecraft.getInstance().getTextureManager().release(icon);
            icon = null;
        }
        lyric = null;
        stack = playerStack;
        getData();
    }

    public static void clearInfo(){
        info = null;
    }

    public static void getData(){
        if(info != null){
            try {
                id = NetMusicListUtil.getIdFromInfo(info);
                thread = new Thread(() -> getDataByThread(id));
                thread.start();
            } catch (Exception e) {
                NetMusicList.LOGGER.error("解析出现错误", e);
            }
        }
    }

    private static void getDataByThread(long id){
        try {
            var icon_url = NetMusicListUtil.getIconUrl(NetMusic.NET_EASE_WEB_API.song(id));
            var resourceLocation = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
                    String.format("icon_%s", id));
            Minecraft.getInstance().getTextureManager().register(resourceLocation,
                    NetMusicListUtil.getTextureFromURL(icon_url));
            var l = NetMusicListUtil.getLyric(NetMusic.NET_EASE_WEB_API.lyric(id));
            if(Thread.currentThread().isInterrupted()){return;}
            icon = resourceLocation;
            lyric = l;
        } catch (Exception e) {
            NetMusicList.LOGGER.error("解析出现错误", e);
        }
    }
}
