package com.gly091020.util;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class FileDownloadThread extends Thread {
    private final String downloadUrl;
    private final long resourceId;
    private final String threadId;
    private final Path downloadDir;
    private final String fileType;

    private volatile float progress = 0.0f;
    private volatile long totalBytes = 0;
    private volatile long downloadedBytes = 0;
    private volatile boolean completed = false;
    private volatile boolean failed = false;
    private volatile String errorMessage = "";

    public FileDownloadThread(String downloadUrl, long resourceId, String fileType, String id) {
        this.downloadUrl = downloadUrl;
        this.resourceId = resourceId;
        this.threadId = id;
        this.downloadDir = CacheManager.PATH; // 固定目录
        this.fileType = fileType;
        this.setName("DownloadThread-" + threadId);
    }

    @Override
    public void run() {
        try {
            // 创建下载目录
            Files.createDirectories(downloadDir);

            // 生成文件名
            String fileName = threadId + ".tmp"; // 临时文件名
            Path filePath = downloadDir.resolve(fileName);

            URL url = new URL(downloadUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            // 获取文件大小
            totalBytes = connection.getContentLengthLong();

            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalRead = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    if (isInterrupted()) {
                        // 清理临时文件
                        Files.deleteIfExists(filePath);
                        return;
                    }

                    outputStream.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    downloadedBytes = totalRead;

                    // 更新进度
                    if (totalBytes > 0) {
                        progress = (float) totalRead / totalBytes;
                    }
                }
            }

            // 下载完成，重命名文件
            String finalFileName = threadId + fileType;
            Path finalPath = downloadDir.resolve(finalFileName);
            Files.move(filePath, finalPath);

            completed = true;

        } catch (Exception e) {
            failed = true;
            errorMessage = e.getMessage();
        }
    }

    // 获取进度信息
    public float getProgress() {
        return progress;
    }

    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isFailed() {
        return failed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getThreadId() {
        return threadId;
    }

    public long getResourceId() {
        return resourceId;
    }

    public String getFileType() {
        return fileType;
    }
}