package com.gly091020.hud;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.util.NetMusicListUtil;
import com.gly091020.item.NetMusicPlayerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

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
        if(!NetMusicList.CONFIG.musicHUD)return;
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
                getTextureFromLocal(info);
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

    private static void getTextureFromLocal(ItemMusicCD.SongInfo info){
        var musicPath = convertFileURLToPath(info.songUrl);
        if(musicPath != null){
            var imagePath = getPicturePath(musicPath);
            if(imagePath == null)return;
            var resourceLocation = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
                    String.format("icon_%s", UUID.randomUUID().toString().toLowerCase()));
            try {
                Minecraft.getInstance().getTextureManager().register(resourceLocation,
                        NetMusicListUtil.getTextureFromPath(imagePath));
                icon = resourceLocation;
            } catch (IOException ignored) {
            }
        }
    }

    private static Path getPicturePath(Path musicPath){
        var p1 = musicPath.getParent().resolve(musicPath.getFileName().toFile().getName() + ".png");
        if(p1.toFile().isFile()){return p1;}
        p1 = musicPath.getParent().resolve(musicPath.getFileName().toFile().getName() + ".jpg");
        if(p1.toFile().isFile()){return p1;}
        p1 = musicPath.getParent().resolve(musicPath.getFileName().toFile().getName() + ".jpeg");
        if(p1.toFile().isFile()){return p1;}
        return null;
    }

    public static Path convertFileURLToPath(String urlString) {
        try {
            URL url = new URL(urlString);
            if (!"file".equals(url.getProtocol())) {
                return null;
            }

            // 使用 URI 转换更安全，可以正确处理特殊字符
            URI uri = url.toURI();
            return Paths.get(uri);

        } catch (Exception e) {
            return null;
        }
    }
}
