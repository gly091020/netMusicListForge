package com.gly091020.util;

import com.github.tartaricacid.netmusic.NetMusic;
import com.gly091020.NetMusicList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApiStatus.Experimental
public class CacheManager {
    private static Map<String, String> musicCache = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);
    public static final String DIR_NAME = "netMusicListCache";
    public static final String INDEX_FILE_NAME = "index.json";
    public static Path PATH = FMLPaths.CONFIGDIR.get().resolve(DIR_NAME);
    public static final List<FileDownloadThread> threads = new CopyOnWriteArrayList<>();

    public static void init(){
        if(NetMusicList.CONFIG.globalCache){
            PATH = Paths.get(System.getProperty("user.home")).resolve(DIR_NAME);
        }
        if(!NetMusicList.CONFIG.enableCache)return;
        try {
            if (PATH.toFile().isFile()) {
                Files.createDirectory(PATH);
            }
            Files.writeString(PATH.resolve(INDEX_FILE_NAME), "{}");
        } catch (IOException e) {
            NetMusicList.LOGGER.error("缓存无法创建：", e);
        }
    }

    @SuppressWarnings("all")
    public static void load(){
        if(!NetMusicList.CONFIG.enableCache)return;
        try{
            musicCache = (Map<String, String>)GSON.fromJson(Files.readString(PATH.resolve(INDEX_FILE_NAME)),
                    TypeToken.get(Object.class));
            checkCache();
        }catch (Exception e){
            NetMusicList.LOGGER.error("缓存读取失败：", e);
            init();
        }
    }

    public static void save(){
        if(!NetMusicList.CONFIG.enableCache)return;
        try{
            Files.writeString(PATH.resolve(INDEX_FILE_NAME), GSON.toJson(musicCache));
        } catch (Exception e) {
            NetMusicList.LOGGER.error("缓存写入失败：", e);
        }
    }

    public static int checkCache(boolean andClear){
        if(!NetMusicList.CONFIG.enableCache)return 0;
        var keys = new ArrayList<String>();
        musicCache.forEach((k, v) -> {
            var file = PATH.resolve(v + ".mp3");
            try {
                if(!file.toFile().isFile() || isHtmlOrErrorResponse(Files.readAllBytes(file)))keys.add(k);
            } catch (IOException e) {
                keys.add(k);
            }
        });
        keys.forEach(l -> {
            if(andClear)deleteCache(Long.parseLong(l));
            musicCache.remove(l);
        });
        if(!keys.isEmpty())save();
        return keys.size();
    }

    public static int checkCache(){
        return checkCache(false);
    }

    private static void startDownload(String downloadUrl, long resourceId, String fileType, String uuid){
        if(!NetMusicList.CONFIG.enableCache)return;
        var thread = new FileDownloadThread(downloadUrl, resourceId, fileType, uuid);
        EXECUTOR_SERVICE.submit(thread);
        threads.add(thread);
    }

    public static void startImgDownload(long resourceId, String uuid){
        if(!NetMusicList.CONFIG.enableCache)return;
        EXECUTOR_SERVICE.submit(() -> {
            try {
                startDownload(NetMusicListUtil.getIconUrl(NetMusic.NET_EASE_WEB_API.song(resourceId))
                        .toString(), resourceId, ".png", uuid);
            } catch (Exception e) {
                NetMusicList.LOGGER.error("出现错误：", e);
            }
        });
    }

    public static void startSongDownload(long resourceId, String uuid){
        if(!NetMusicList.CONFIG.enableCache)return;
        EXECUTOR_SERVICE.submit(() -> {
            try {
                startDownload(pasteUrl(resourceId), resourceId, ".mp3", uuid);
            } catch (Exception e) {
                NetMusicList.LOGGER.error("出现错误：", e);
            }
        });
    }

    public static void startLycDownload(long resourceId, String uuid){
        if(!NetMusicList.CONFIG.enableCache)return;
        EXECUTOR_SERVICE.submit(() -> {
            try {
                var lyc = NetMusicListUtil.getLyric(NetMusic.NET_EASE_WEB_API.lyric(resourceId));
                if(lyc == null)return;
                Files.createDirectories(PATH);
                Files.writeString(PATH.resolve(uuid + ".lyc.json"), lyc.toJson(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            } catch (Exception e) {
                NetMusicList.LOGGER.error("出现错误：", e);
            }
        });
    }

    @SuppressWarnings("all")
    public static String pasteUrl(long resourceId){
        if(NetMusicListUtil.hasLoginNeed()){
            var r = LoginNeedUtil.getUrl("?id=" + resourceId);
            if(r != null)return r;
        }
        try {
            return NetMusicListUtil.resolveRedirect(new URL(String.format("https://music.163.com/song/media/outer/url?id=%s.mp3", resourceId)), 3, Map.of()).toString();
        } catch (IOException e) {
            return String.format("https://music.163.com/song/media/outer/url?id=%s.mp3", resourceId);
        }
    }

    private static void addCache(long resourceId, String uuid){
        if(!NetMusicList.CONFIG.enableCache)return;
        musicCache.put(String.valueOf(resourceId), uuid);
        save();
    }

    public static boolean hasCache(long resourceId){
        if(!NetMusicList.CONFIG.enableCache)return false;
        return musicCache.containsKey(String.valueOf(resourceId));
    }

    public static Path getImageCache(long resourceId){
        if(!NetMusicList.CONFIG.enableCache)return null;
        if(!hasCache(resourceId))return null;
        var path = PATH.resolve(musicCache.get(String.valueOf(resourceId)) + ".png");
        if(path.toFile().isFile()){
            return path;
        }
        return null;
    }

    public static String getSongCache(long resourceId){
        if(!NetMusicList.CONFIG.enableCache)return null;
        if(!hasCache(resourceId))return null;
        var path = PATH.resolve(musicCache.get(String.valueOf(resourceId)) + ".mp3");
        if(path.toFile().isFile()){
            return path.toFile().toURI().toString();
        }
        return null;
    }

    public static NetMusicListUtil.Lyric getLycCache(long resourceId){
        if(!NetMusicList.CONFIG.enableCache)return null;
        if(!hasCache(resourceId))return null;
        var path = PATH.resolve(musicCache.get(String.valueOf(resourceId)) + ".lyc.json");
        if(path.toFile().isFile()){
            try {
                return NetMusicListUtil.Lyric.fromJson(Files.readString(path));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static void tick() {
        if(!NetMusicList.CONFIG.enableCache)return;
        List<FileDownloadThread> toRemove = new ArrayList<>();
        for (FileDownloadThread t : threads) {
            if (t.isCompleted()) {
                if(Objects.equals(t.getFileType(), ".mp3")) {
                    addCache(t.getResourceId(), t.getThreadId());
                }
                toRemove.add(t);
            } else if (t.isFailed()) {
                NetMusicList.LOGGER.error("缓存出现错误：{}", t.getErrorMessage());
                toRemove.add(t);
            }
        }
        threads.removeAll(toRemove);
    }

    public static float getDownloadProgress(long resourceId){
        if(!NetMusicList.CONFIG.enableCache)return 0;
        for(FileDownloadThread thread: threads){
            if(thread.getResourceId() == resourceId && Objects.equals(thread.getFileType(), ".mp3")){
                return thread.getProgress();
            }
        }
        return 0f;
    }

    public static List<FileDownloadThread> getThreads(){
        return new ArrayList<>(threads);
    }

    public static void deleteCache(long resourceId){
        if(!NetMusicList.CONFIG.enableCache)return;
        if(!hasCache(resourceId))return;
        List<Path> paths = new ArrayList<>();
        paths.add(PATH.resolve(musicCache.get(String.valueOf(resourceId)) + ".lyc.json"));
        paths.add(getImageCache(resourceId));
        paths.add(PATH.resolve(musicCache.get(String.valueOf(resourceId)) + ".mp3"));
        paths.forEach(path -> {
            if(path != null) {
                try {
                    Files.delete(path);
                } catch (IOException ignored) {}
            }
        });
        musicCache.remove(String.valueOf(resourceId));
        save();
    }

    private static final String[] HTML_STARTS = {
            "<!DOCTYPE html",
            "<html",
            "<?xml",
            "{",
            "[",
            "HTTP/",
            "Error",
            "404",
            "500"
    };
    /**
     * 快速检查文件是否是HTML或错误响应
     */
    public static boolean isHtmlOrErrorResponse(byte[] data) {
        if (data == null || data.length < 10) {
            return false;
        }

        // 将前1KB转换为字符串进行检查
        int checkLength = Math.min(data.length, 1024);
        String fileStart = new String(data, 0, checkLength, StandardCharsets.UTF_8).trim();

        // 检查常见的HTML/错误响应开头
        for (String htmlStart : HTML_STARTS) {
            if (fileStart.startsWith(htmlStart)) {
                return true;
            }
        }

        // 检查是否包含明显的HTML标签
        if (fileStart.contains("<head>") ||
                fileStart.contains("<body>") ||
                fileStart.contains("<title>") ||
                fileStart.contains("</html>") ||
                fileStart.contains("DOCTYPE") ||
                fileStart.contains("html>")) {
            return true;
        }

        // 检查JSON响应（常见于API错误）
        if ((fileStart.startsWith("{") && fileStart.contains("\"error\"")) ||
                (fileStart.startsWith("[") && fileStart.contains("\"error\""))) {
            return true;
        }

        return false;
    }
}