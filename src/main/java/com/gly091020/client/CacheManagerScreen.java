package com.gly091020.client;

import com.gly091020.util.CacheManager;
import com.gly091020.util.FileDownloadThread;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.util.List;

@SuppressWarnings("all")
public class CacheManagerScreen extends Screen {
    // 由于调试用途，所以是AI写的
    private static final DecimalFormat PROGRESS_FORMAT = new DecimalFormat("0.0%");
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0.00");

    private int refreshTicks = 0;
    private static final int REFRESH_INTERVAL = 5; // 每5tick刷新一次

    public CacheManagerScreen() {
        super(Component.literal("下载监控"));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 绘制半透明背景
        this.renderBackground(guiGraphics);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // 绘制标题
        guiGraphics.drawCenteredString(this.font, "下载任务监控", this.width / 2, 20, 0xFFFFFF);

        // 获取下载线程列表
        List<FileDownloadThread> downloads = CacheManager.getThreads();

        // 显示下载数量
        String countText = "活跃下载任务: " + downloads.size();
        guiGraphics.drawString(this.font, countText, 10, 50, 0xFFFFFF);

        // 绘制下载列表
        int startY = 80;
        int lineHeight = 30;

        if (downloads.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, "没有活跃的下载任务", this.width / 2, startY, 0x888888);
        } else {
            for (int i = 0; i < downloads.size(); i++) {
                FileDownloadThread thread = downloads.get(i);
                renderDownloadEntry(guiGraphics, thread, 10, startY + i * lineHeight, this.width - 20);
            }
        }

        poseStack.popPose();

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void renderDownloadEntry(GuiGraphics guiGraphics, FileDownloadThread thread, int x, int y, int width) {
        // 线程基本信息
        String threadInfo = String.format("ID: %s | 资源: %d | 类型: %s",
                thread.getThreadId(), thread.getResourceId(), thread.getFileType());
        guiGraphics.drawString(this.font, threadInfo, x, y, 0xFFFFFF);

        // 进度信息
        float progress = thread.getProgress();
        long downloaded = thread.getDownloadedBytes();
        long total = thread.getTotalBytes();

        String progressText;
        if (total > 0) {
            String downloadedStr = formatFileSize(downloaded);
            String totalStr = formatFileSize(total);
            progressText = String.format("%s / %s (%s)", downloadedStr, totalStr, PROGRESS_FORMAT.format(progress));
        } else {
            progressText = String.format("%s / 未知大小 (%s)", formatFileSize(downloaded), PROGRESS_FORMAT.format(progress));
        }

        guiGraphics.drawString(this.font, progressText, x, y + 10, 0xCCCCCC);

        // 进度条
        int progressBarWidth = width - 20;
        int progressBarHeight = 6;
        int progressBarX = x;
        int progressBarY = y + 20;

        // 进度条背景
        guiGraphics.fill(progressBarX, progressBarY,
                progressBarX + progressBarWidth, progressBarY + progressBarHeight,
                0xFF555555);

        // 进度条前景
        int progressWidth = (int) (progressBarWidth * progress);
        int color = getProgressBarColor(thread);
        guiGraphics.fill(progressBarX, progressBarY,
                progressBarX + progressWidth, progressBarY + progressBarHeight,
                color);

        // 状态文本
        String statusText = getStatusText(thread);
        int statusColor = getStatusColor(thread);
        guiGraphics.drawString(this.font, statusText, x + progressBarWidth + 5, progressBarY, statusColor);
    }

    private String getStatusText(FileDownloadThread thread) {
        if (thread.isCompleted()) {
            return "完成";
        } else if (thread.isFailed()) {
            return "失败: " + thread.getErrorMessage();
        } else if (thread.isInterrupted()) {
            return "已中断";
        } else {
            return "下载中...";
        }
    }

    private int getStatusColor(FileDownloadThread thread) {
        if (thread.isCompleted()) {
            return 0xFF00FF00; // 绿色
        } else if (thread.isFailed()) {
            return 0xFFFF0000; // 红色
        } else if (thread.isInterrupted()) {
            return 0xFFFFA500; // 橙色
        } else {
            return 0xFFFFFF00; // 黄色
        }
    }

    private int getProgressBarColor(FileDownloadThread thread) {
        if (thread.isCompleted()) {
            return 0xFF00FF00; // 绿色
        } else if (thread.isFailed()) {
            return 0xFFFF0000; // 红色
        } else if (thread.isInterrupted()) {
            return 0xFFFFA500; // 橙色
        } else {
            return 0xFF0000FF; // 蓝色
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return SIZE_FORMAT.format(bytes / 1024.0) + " KB";
        } else {
            return SIZE_FORMAT.format(bytes / (1024.0 * 1024.0)) + " MB";
        }
    }

    @Override
    public void tick() {
        super.tick();

        // 定期刷新显示
        refreshTicks++;
        if (refreshTicks >= REFRESH_INTERVAL) {
            refreshTicks = 0;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC键关闭界面
        if (keyCode == 256) { // ESC
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 游戏在后台继续运行
    }
}