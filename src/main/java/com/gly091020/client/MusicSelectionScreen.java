package com.gly091020.client;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.packet.DeleteMusicDataPacket;
import com.gly091020.packet.MusicListDataPacket;
import com.gly091020.NetMusicList;
import com.gly091020.PlayMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.gly091020.NetMusicList.CHANNEL;

public class MusicSelectionScreen extends Screen {
    private final List<String> musicList;
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(NetMusicList.ModID,
            "textures/gui/bg.png");
    private final int backgroundWidth = 256;
    private final int backgroundHeight = 230;
    private int left, top;
    private PlayModeButton playModeButton;
    private MusicListWidget listWidget;
    private Integer index;
    private final PlayMode mode;
    private Button deleteButton;

    public MusicSelectionScreen(List<String> musicList, PlayMode mode, Integer index) {
        super(Component.translatable("gui.net_music_list.title"));
        this.musicList = musicList;
        this.mode = mode;
        this.index = index;
    }

    @Override
    protected void init() {
        super.init();

        // 计算UI位置（居中）
        this.left = (this.width - this.backgroundWidth) / 2;
        this.top = (this.height - this.backgroundHeight) / 2;

        // 创建音乐列表组件（非全屏）
        listWidget = new MusicListWidget();

        // 添加所有音乐条目
        for (String music : musicList) {
            listWidget.addMusicEntry(music);
        }
        listWidget.addMusicEntry(Component.translatable("gui.net_music_list.add").getString());

        listWidget.setSelected(listWidget.children().get(index));
        this.addRenderableWidget(listWidget);

        // 关闭按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.net_music_list.close"), button -> {
            sendPackage();
            this.onClose();
                })
                .pos(left + backgroundWidth / 2 - 50, top + backgroundHeight - 24)
                .size(100, 20)
                .build());
        playModeButton = new PlayModeButton(left + 10, top + backgroundHeight - 90, button -> {
            playModeButton.playMode = playModeButton.playMode.getNext();
            playModeButton.setTooltip(Tooltip.create(playModeButton.playMode.getName()));
            sendPackage();
        }, mode);
        this.addRenderableWidget(playModeButton);
        deleteButton = Button.builder(Component.translatable("gui.net_music_list.delete"),
                        button -> deleteMusic())
                .pos(left + backgroundWidth - 90, top + backgroundHeight - 90)
                .size(80, 22).build();
        deleteButton.active = canDelete();
        this.addRenderableWidget(deleteButton);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        // 渲染半透明背景
        this.renderBackground(context);
        var fontHeight = font.lineHeight;

        // 渲染背景
        context.blit(BACKGROUND_TEXTURE, left, top, 0, 0, backgroundWidth, backgroundHeight);

        // 渲染标题
        context.drawCenteredString(
                font,
                this.title,
                left + backgroundWidth / 2,
                top + 6,
                0x404040
        );

        context.drawString(
                font,
                Component.translatable("gui.net_music_list.play_list"),
                left + 10,
                top + 6 + fontHeight + 6,
                0x000000, false
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
        if (p_96552_ == GLFW.GLFW_KEY_DELETE) {
            deleteMusic();
            return true;
        }
        if(p_96552_ == GLFW.GLFW_KEY_ESCAPE && super.keyPressed(p_96552_, p_96553_, p_96554_)){
            sendPackage();
            return true;
        }
        return super.keyPressed(p_96552_, p_96553_, p_96554_);
    }

    public void deleteMusic(){
        if (this.listWidget.getSelectedIndex() != musicList.size()) {
            var o = this.listWidget.getSelectedIndex();
            var o1 = o;
            this.musicList.remove(o);
            if (o == this.musicList.size()) {
                o--;
            }
            if (o < 0) {
                o = 0;
            }
            index = o;
            this.clearWidgets();
            this.init();
            listWidget.setSelectedIndex(o);
            this.index = listWidget.getSelectedIndex();
            CHANNEL.sendToServer(new DeleteMusicDataPacket(o1));
            deleteButton.active = canDelete();
            sendPackage();
        }
    }

    public boolean canDelete(){
        return this.listWidget.getSelectedIndex() != musicList.size();
    }

    private class MusicListEntry extends ObjectSelectionList.Entry<MusicListEntry> {
        private final String musicName;

        public MusicListEntry(String musicName) {
            this.musicName = musicName;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(musicName);
        }

        @Override
        public void render(@NotNull GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            // 渲染背景
            if (hovered) {
                context.fill(x, y, x + entryWidth - 3, y + entryHeight, 0x80FFFFFF);
            }

            // 渲染文本
            context.drawString(
                    font,
                    Component.literal(musicName),
                    x + 5,
                    y + (entryHeight - 10) / 2,
                    0xFFFFFF
            );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            MusicSelectionScreen.this.index = MusicSelectionScreen.this.listWidget.getSelectedIndex();
            sendPackage();
            deleteButton.active = canDelete();
            return true;
        }
    }

    public void sendPackage(){
        this.index = listWidget.getSelectedIndex();
        CHANNEL.sendToServer(new MusicListDataPacket(index, this.playModeButton.playMode));
    }

    private class MusicListWidget extends ObjectSelectionList<MusicListEntry> {
        public MusicListWidget() {
            super(Minecraft.getInstance(), backgroundWidth - 10,
                    backgroundHeight - 50, MusicSelectionScreen.this.top + 24 + font.lineHeight,
                    backgroundHeight + MusicSelectionScreen.this.top - 100,
                    font.lineHeight + 1);
            this.x0 = MusicSelectionScreen.this.left + 5;
            this.x1 = backgroundWidth + MusicSelectionScreen.this.left - 5;
            this.setRenderTopAndBottom(false);
        }

        @Override
        protected int getScrollbarPosition() {
            return this.x1 + this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 16;
        }

        public void addMusicEntry(String musicName) {
            this.addEntry(new MusicListEntry(musicName));
        }

        @Override
        public void setRenderSelection(boolean renderSelection) {
            super.setRenderSelection(renderSelection);
        }

        @Override
        protected void renderBackground(GuiGraphics context) {
            context.fill(left, top, left + width, top + height, 0x000000);
        }

        public int getSelectedIndex(){
            return this.children().indexOf(this.getSelected());
        }

        public void setSelectedIndex(int index){
            this.setSelected(this.children().get(index));
        }
    }

    public static void open(List<ItemMusicCD.SongInfo> musicList, PlayMode mode, Integer index) {
        var l = new ArrayList<String>();
        for(ItemMusicCD.SongInfo info: musicList){
            if(info.artists.isEmpty()){
                l.add(info.songName);
            }else {
                var a = new StringBuilder();
                for(String artist: info.artists){
                    a.append(artist);
                    a.append("、");
                }
                l.add(String.format("%s —— %s", a, info.songName));
            }
        }
        if(index < 0 || index > musicList.size()){
            NetMusicList.LOGGER.error("错误的索引：{}", index);
            return;
        }
        Minecraft.getInstance().setScreen(new MusicSelectionScreen(l, mode, index));
    }

    public static class PlayModeButton extends Button{
        public PlayMode playMode;
        protected PlayModeButton(int x, int y, OnPress onPress, PlayMode playMode) {
            super(x, y, 22, 22, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            this.playMode = playMode;
            setTooltip(Tooltip.create(this.playMode.getName()));
        }

        @Override
        protected void renderWidget(GuiGraphics context, int p_282682_, int p_281714_, float p_282542_) {
            context.blit(BACKGROUND_TEXTURE, this.getX(), this.getY(),
                    this.isHovered() ? 22 : 0, 230, this.width, this.height);
            var x = 0;
            switch (this.playMode){
                case SEQUENTIAL -> x = 44;
                case RANDOM -> x = 66;
                case LOOP -> x = 88;
            }
            context.blit(BACKGROUND_TEXTURE, this.getX(), this.getY(),
                    x, 230, this.width, this.height);
        }
    }
}