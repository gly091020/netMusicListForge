package com.gly091020.client;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.PlayMode;
import com.gly091020.packet.DeleteMusicDataPacket;
import com.gly091020.packet.MoveMusicDataPacket;
import com.gly091020.packet.MusicListDataPacket;
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

@Deprecated
public class OldMusicSelectionScreen extends Screen {
    private final List<String> musicList;
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "textures/gui/old_bg.png");
    private final int backgroundWidth = 256;
    private final int backgroundHeight = 230;
    private int left, top;
    private PlayModeButton playModeButton;
    private MusicListWidget listWidget;
    private Integer index;
    private final PlayMode mode;
    private Button deleteButton;
    private Button upButton;
    private Button downButton;

    public OldMusicSelectionScreen(List<String> musicList, PlayMode mode, Integer index) {
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
                .pos(left + backgroundWidth - 90 - 23, top + backgroundHeight - 90)
                .size(80, 22).build();

        upButton = new MoveButton(left + backgroundWidth - 27,
                top + backgroundHeight - 90, button -> moveMusic(true), true);
        downButton = new MoveButton(left + backgroundWidth - 27,
                top + backgroundHeight - 90 + 22, button -> moveMusic(false), false);

        deleteButton.active = canDelete();
        upButton.active = canMove(true);
        downButton.active = canMove(false);

        this.addRenderableWidget(deleteButton);
        this.addRenderableWidget(upButton);
        this.addRenderableWidget(downButton);
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
        if (p_96552_ == GLFW.GLFW_KEY_DELETE && canDelete()) {
            deleteMusic();
            return true;
        }
        if(p_96552_ == GLFW.GLFW_KEY_UP && canMove(true)){
            moveMusic(true);
            return true;
        }
        if(p_96552_ == GLFW.GLFW_KEY_DOWN && canMove(false)){
            moveMusic(false);
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
            updateButton();
            sendPackage();
        }
    }

    public void moveMusic(boolean isUp){
        if (this.listWidget.getSelectedIndex() != musicList.size()) {
            var i1 = listWidget.getSelectedIndex() - (isUp ? 1 : -1);
            CHANNEL.sendToServer(new MoveMusicDataPacket(listWidget.getSelectedIndex(), i1));
            var l = listWidget.getSelected();
            var l1 = musicList.get(listWidget.getSelectedIndex());
            musicList.set(listWidget.getSelectedIndex(), musicList.get(i1));
            musicList.set(i1, l1);
            listWidget.setEntry(listWidget.getSelectedIndex(), listWidget.children().get(i1));
            listWidget.setEntry(i1, l);
            this.index = i1;
            listWidget.setSelectedIndex(i1);
            updateButton();
            sendPackage();
        }
    }

    public void updateButton(){
        deleteButton.active = canDelete();
        upButton.active = canMove(true);
        downButton.active = canMove(false);
    }

    public boolean canDelete(){
        return this.index != musicList.size();
    }

    public boolean canMove(boolean isUp){
        if(isUp){
            if(!canDelete()){return false;}
            return this.index > 0;
        }else{
            if(!canDelete()){return false;}
            return this.index < musicList.size() - 1;
        }
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
            super.mouseClicked(mouseX, mouseY, button);
            OldMusicSelectionScreen.this.index = listWidget.children().indexOf(this);
            listWidget.setSelectedIndex(index);
            sendPackage();
            updateButton();
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
                    backgroundHeight - 50, OldMusicSelectionScreen.this.top + 24 + font.lineHeight,
                    backgroundHeight + OldMusicSelectionScreen.this.top - 100,
                    font.lineHeight + 1);
            this.x0 = OldMusicSelectionScreen.this.left + 5;
            this.x1 = backgroundWidth + OldMusicSelectionScreen.this.left - 5;
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

        public void setEntry(int index, MusicListEntry entry){
            var l = children();
            l.set(index, entry);
        }

        @Override
        public boolean mouseScrolled(double p_93416_, double p_93417_, double p_93418_) {
            return super.mouseScrolled(p_93416_, p_93417_, p_93418_ * (NetMusicList.TOGGLE_MUSIC_SPEED_UP.isDown() ? 5 : 1));
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
        Minecraft.getInstance().setScreen(new OldMusicSelectionScreen(l, mode, index));
    }

    public static class PlayModeButton extends Button{
        public PlayMode playMode;
        protected PlayModeButton(int x, int y, OnPress onPress, PlayMode playMode) {
            super(x, y, 22, 22, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            this.playMode = playMode;
            setTooltip(Tooltip.create(this.playMode.getName()));
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics context, int p_282682_, int p_281714_, float p_282542_) {
            super.renderWidget(context, p_282682_, p_281714_, p_282542_);
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

    public static class MoveButton extends Button{
        boolean isUp;
        protected MoveButton(int x, int y, OnPress onPress, boolean isUP) {
            super(x, y, 22, 22, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            isUp = isUP;
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics context, int p_282682_, int p_281714_, float p_282542_) {
            super.renderWidget(context, p_282682_, p_281714_, p_282542_);
            context.blit(BACKGROUND_TEXTURE, this.getX(), this.getY(),
                    isUp ? 110 : 132, 230, this.width, this.height);
        }
    }
}