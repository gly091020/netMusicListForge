package com.gly091020.util;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.ExtraMusicList;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.client.PauseSoundManager;
import com.gly091020.config.NetMusicListConfig;
import com.gly091020.hud.MusicInfoHud;
import com.gly091020.mixin.TickableSoundGetterMixins;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.NativeImage;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gly091020.NetMusicList.CONFIG;

public class NetMusicListUtil {
    public static final Gson GSON = new Gson();
    public static final UUID _5112151111121 = UUID.fromString("91bd580f-5f17-4e30-872f-2e480dd9a220");
    public static final UUID N44 = UUID.fromString("5a33e9b0-35bc-44ed-9b4e-03e3e180a3d2");
    // 自己的石山还得让别人来修……
    public static final UUID WANG_REN_ZE_9788 = UUID.fromString("21b900df-8ea3-47e4-81cb-ed1146714b14");
    public static boolean globalStopMusic = false;
    @OnlyIn(Dist.CLIENT)
    public static void playSound(SoundEvent event){
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, 1));
    }

    @OnlyIn(Dist.CLIENT)
    public static long getIdFromInfo(ItemMusicCD.SongInfo info) throws IllegalAccessException {
        var s = info.songUrl;
        return getIdFromUrl(s);
    }

    public static long getIdFromUrl(String url) throws IllegalAccessException {
        String[] parts = url.split("[?&]id=");  // 为 什 么 要 用 这 种 代 码
        String idPart;
        if (parts.length > 1) {
            idPart = parts[1].split("&")[0];
        } else {
            throw new IllegalAccessException("解析失败");
        }
        return Long.parseLong(idPart.replace(".mp3", ""));
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("all")
    public static URL getIconUrl(String json) throws Exception{
        var data = (Map<String, Object>)GSON.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
        var song = (Map<String, Object>)((List<Object>)data.get("songs")).get(0);
        var album = song.get("album");
        // 大力出奇迹.png
        return new URL((String) ((Map<String, Object>)album).get("picUrl"));
    }

    @OnlyIn(Dist.CLIENT)
    public static AbstractTexture getTextureFromURL(URL imageUrl) throws IOException {
        try (InputStream stream = imageUrl.openConnection().getInputStream()) {
            BufferedImage bufferedImage = ImageIO.read(stream);
            if (bufferedImage == null) {
                throw new IOException("无法读取图片 - 不支持的格式或损坏的文件");
            }
            NativeImage nativeImage = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), false);
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    int argb = bufferedImage.getRGB(x, y);
                    int a = (argb >> 24) & 0xFF;
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    int rgba = (a << 24) | (b << 16) | (g << 8) | r;
                    nativeImage.setPixelRGBA(x, y, rgba | 0xFF000000);
                }
            }
            return new DynamicTexture(nativeImage);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static AbstractTexture getTextureFromPath(Path imagePath) throws IOException {
        try (InputStream stream = Files.newInputStream(imagePath)) {
            BufferedImage bufferedImage = ImageIO.read(stream);
            if (bufferedImage == null) {
                throw new IOException("无法读取图片 - 不支持的格式或损坏的文件");
            }
            NativeImage nativeImage = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), false);
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    int argb = bufferedImage.getRGB(x, y);
                    int a = (argb >> 24) & 0xFF;
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    int rgba = (a << 24) | (b << 16) | (g << 8) | r;
                    nativeImage.setPixelRGBA(x, y, rgba | 0xFF000000);
                }
            }
            return new DynamicTexture(nativeImage);
        }
    }

    public static class Lyric {
        @SerializedName("lyric")
        private final Map<Float, String> lyric;

        @SerializedName("transform_lyric")
        @Nullable
        private final Map<Float, String> transformLyric;

        public Lyric(Map<Float, String> lyric, @Nullable Map<Float, String> transformLyric) {
            this.lyric = lyric;
            this.transformLyric = transformLyric;
        }

        public Lyric() {
            this.lyric = new TreeMap<>();
            this.transformLyric = null;
        }

        public Pair<String, String> getLyric(float second) {
            var keyList = lyric.keySet().stream().toList();
            var valueList = lyric.values().stream().toList();
            var text = "";
            var lyricTime = 0f;
            String transformText = null;
            for (int i = 0; i < lyric.size(); i++) {
                if (i == lyric.size() - 1) {
                    text = valueList.get(i);
                    lyricTime = keyList.get(i);
                    break;
                }
                if (keyList.get(i) <= second && keyList.get(i + 1) > second) {
                    text = valueList.get(i);
                    lyricTime = keyList.get(i);
                    break;
                }
            }
            if (transformLyric != null) {
                transformText = transformLyric.getOrDefault(lyricTime, null);
            }
            return new Pair<>(text, transformText);
        }

        public Map<Float, String> getLyric() {
            return lyric;
        }

        @Nullable
        public Map<Float, String> getTransformLyric() {
            return transformLyric;
        }

        public String toJson() {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            return gson.toJson(this);
        }

        public static Lyric fromJson(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json, Lyric.class);
        }
    }

    public static String secondsToMinutesSeconds(int totalSeconds) {
        if (totalSeconds < 0) {
            return "00:00";
        }

        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    @SuppressWarnings("all")
    public static Lyric getLyric(String json){
        var data = (Map<String, Object>)GSON.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
        var lrc = (String)((Map<String, Object>)data.get("lrc")).get("lyric");
        var transformlLrc = "";
        if(data.containsKey("tlyric")){
            transformlLrc = (String)((Map<String, Object>)data.get("tlyric")).get("lyric");
        }
        if(lrc.isEmpty()){
            return null;
        }
        Map<Float, String> lyricMap;
        Map<Float, String> transformLyricMap = null;
        if(!transformlLrc.isEmpty()){
            transformLyricMap = new LinkedHashMap<>();
            for(String part: transformlLrc.split("\n")){
                var p = getLyricPair(part);
                if(p != null){transformLyricMap.put(p.getA(), p.getB());}
            }
        }
        lyricMap = new LinkedHashMap<>();
        for(String part: lrc.split("\n")){
            var p = getLyricPair(part);
            if(p != null){lyricMap.put(p.getA(), p.getB());}
        }
        return new Lyric(lyricMap, transformLyricMap);
    }

    private static Pair<Float, String> getLyricPair(String input){
        Pattern pattern = Pattern.compile("^\\[(\\d+):(\\d+)[.:](\\d+)](.*)$");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            int minutes = Integer.parseInt(matcher.group(1));
            int seconds = Integer.parseInt(matcher.group(2));
            int milliseconds = Integer.parseInt(matcher.group(3));
            String text = matcher.group(4);

            // 计算总秒数（带小数）
            float totalSeconds = minutes * 60 + seconds + milliseconds / 1000f;
            return new Pair<>(totalSeconds, text.trim());
        }
        return null;
    }

    public static boolean isGLY(){
        return Objects.equals(Minecraft.getInstance().getUser().getProfileId(), _5112151111121);
    }

    public static boolean isN44(){
        return Objects.equals(Minecraft.getInstance().getUser().getProfileId(), N44);
    }

    public static List<TickableSoundInstance> getTickableSounds(){
        return ((TickableSoundGetterMixins.SoundEngineMixin)((TickableSoundGetterMixins.SoundManagerMixin) Minecraft.getInstance().getSoundManager()).getSoundEngine()).getTickableSoundInstances();
    }

    public static boolean isWangRenZe9788(){
        return Objects.equals(Minecraft.getInstance().getUser().getProfileId(), WANG_REN_ZE_9788);
    }

    public static void reloadConfig(){
        var holder = AutoConfig.getConfigHolder(NetMusicListConfig.class);
        if(CONFIG.maxImportList < 100){
            CONFIG.maxImportList = 300;
        }
        holder.setConfig(CONFIG);
        holder.save();
        if(FMLEnvironment.dist.isClient()){
            MusicInfoHud.setPos(CONFIG.x, CONFIG.y);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isPaused(){
        return ((PauseSoundManager)Minecraft.getInstance().getSoundManager()).isPaused();
    }

    public static boolean hasLoginNeed(){
        return ModList.get().isLoaded("net_music_login_need");
    }

    public static List<ItemMusicCD.SongInfo> getMusicList(long id) throws Exception {
        // 从网络音乐机里拿的代码
        var SONGS = new ArrayList<ItemMusicCD.SongInfo>();
        NetEaseMusicList pojo = GSON.fromJson(NetMusic.NET_EASE_WEB_API.list(id), NetEaseMusicList.class);
        int count = pojo.getPlayList().getTracks().size();
        int size = Math.min(pojo.getPlayList().getTrackIds().size(), CONFIG.maxImportList);
        if (count < size) {
            long[] ids = new long[size - count];

            for(int i = count; i < size; ++i) {
                ids[i - count] = pojo.getPlayList().getTrackIds().get(i).getId();
            }

            String extraTrackInfo = NetMusic.NET_EASE_WEB_API.songs(ids);
            ExtraMusicList extra = GSON.fromJson(extraTrackInfo, ExtraMusicList.class);
            pojo.getPlayList().getTracks().addAll(extra.getTracks());
        }
        for(NetEaseMusicList.Track track : pojo.getPlayList().getTracks()) {
            SONGS.add(new ItemMusicCD.SongInfo(track));
        }
        return SONGS;
    }

    public static URL resolveRedirect(URL originalUrl, int maxRedirects, Map<String, String> headers) throws IOException {
        URL currentUrl = originalUrl;
        HttpURLConnection connection;

        while (maxRedirects-- > 0) {
            connection = (HttpURLConnection) currentUrl.openConnection();
            connection.setInstanceFollowRedirects(false); // 手动处理重定向

            // 设置请求头
            if (headers != null) {
                headers.forEach(connection::setRequestProperty);
            }

            int responseCode = connection.getResponseCode();

            // 如果是 200，直接返回当前 URL
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return currentUrl;
            }

            // 处理 302/301/307/308 等重定向
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                    responseCode == 307 || responseCode == 308) {

                String location = connection.getHeaderField("Location");
                connection.disconnect(); // 关闭当前连接

                if (location == null) {
                    throw new IOException("Redirect with no Location header: " + responseCode);
                }

                // 处理相对路径的 Location
                currentUrl = new URL(currentUrl, location);
            } else {
                throw new IOException("Unexpected HTTP status: " + responseCode);
            }
        }

        throw new IOException("Too many redirects (max: " + maxRedirects + ")");
    }
}
