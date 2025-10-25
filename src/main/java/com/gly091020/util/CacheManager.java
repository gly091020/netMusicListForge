package com.gly091020.util;

import com.github.tartaricacid.netmusic.NetMusic;
import com.gly091020.NetMusicList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApiStatus.Experimental
public class CacheManager {
    private static Map<String, String> musicCache = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);
    public static final String DIR_NAME = "netMusicListCache";
    public static final String INDEX_FILE_NAME = "index.json";
    public static final Path PATH = FMLPaths.CONFIGDIR.get().resolve(DIR_NAME);
    public static final List<FileDownloadThread> threads = new ArrayList<>();

    public static void init(){
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
            if(!checkCache()){
                save();
            }
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

    public static boolean checkCache(){
        if(!NetMusicList.CONFIG.enableCache)return true;
        var keys = new ArrayList<String>();
        musicCache.forEach((k, v) -> {
            if(!PATH.resolve(v + ".mp3").toFile().isFile())keys.add(k);
        });
        keys.forEach(l -> musicCache.remove(l));
        return keys.isEmpty();
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
                Files.writeString(PATH.resolve(uuid + ".lyc.json"), lyc.toJson());
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
        Iterator<FileDownloadThread> iterator = threads.iterator();
        while (iterator.hasNext()) {
            FileDownloadThread t = iterator.next();
            if (t.isCompleted() && Objects.equals(t.getFileType(), ".mp3")) {
                addCache(t.getResourceId(), t.getThreadId());
                iterator.remove();
            } else if (t.isFailed()) {
                NetMusicList.LOGGER.error("缓存出现错误：{}", t.getErrorMessage());
                iterator.remove();
            }
        }
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
}
