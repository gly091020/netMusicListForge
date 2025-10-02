package com.gly091020.client;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.util.NetMusicListUtil;
import com.gly091020.util.PlayMode;
import com.gly091020.packet.DeleteMusicDataPacket;
import com.gly091020.packet.MoveMusicDataPacket;
import com.gly091020.packet.MusicListDataPacket;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MusicSelectionScreen extends Screen {
    private final List<String> musicList;
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "textures/gui/bg.png");
    private static final ResourceLocation GLY091020 = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID,
            "textures/gui/gly091020.png");
    private static ResourceLocation PLAYER_HAND = null;
    private static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "textures/gui/button.png");
    private final int backgroundWidth = 321;
    private final int backgroundHeight = 161;
    private int left, top;
    private PlayModeButton playModeButton;
    private MusicListWidget listWidget;
    private Integer index;
    private final PlayMode mode;
    private Button deleteButton;
    private Button upButton;
    private Button downButton;

    private float lastScroll = 0;
    private float CDRotation = 0;
    private float nowSpeed = 0;

    private float pointerRotation = 0;

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

        playModeButton = new PlayModeButton(left + 4 + 3, top + 133, button -> {
            playModeButton.playMode = playModeButton.playMode.getNext();
            playModeButton.setTooltip(Tooltip.create(playModeButton.playMode.getName()));
            sendPackage();
        }, mode);
        this.addRenderableWidget(playModeButton);
        deleteButton = new Button(Button.builder(Component.translatable("gui.net_music_list.delete"),
                        button -> deleteMusic())
                .pos(left + 4 + 66 + 3, top + 133)
                .size(50, 22)){
            @Override
            protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blitNineSliced(
                        BUTTON_TEXTURE,
                        this.getX(), this.getY(),
                        this.getWidth(), this.getHeight(),
                        25, 25,
                        3, 3, 3, 3
                );
                this.renderString(guiGraphics, font, 0xFFFFFFFF);
            }
        };

        upButton = new MoveButton(left + 4 + 22 + 3,
                top + 133, button -> moveMusic(true), true);
        downButton = new MoveButton(left + 4 + 44 + 3,
                top + 133, button -> moveMusic(false), false);

        deleteButton.active = canDelete();
        upButton.active = canMove(true);
        downButton.active = canMove(false);

        this.addRenderableWidget(deleteButton);
        this.addRenderableWidget(upButton);
        this.addRenderableWidget(downButton);

        lastScroll = (float) listWidget.getScrollAmount();
        nowSpeed = 0;
        if(musicList.size() == listWidget.getSelectedIndex()){
            pointerRotation = 45;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
        guiGraphics.blit(BACKGROUND_TEXTURE, left, top, 0, 0, backgroundWidth, backgroundHeight, 512, 256);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public void renderCD(@NotNull GuiGraphics guiGraphics, float delta){
        guiGraphics.pose().pushPose();
        float scrollSpeed = (float) Math.abs(listWidget.getScrollAmount() - lastScroll);
        if(scrollSpeed <= 0.3){
            scrollSpeed = 0;
        }
        if(Math.abs(scrollSpeed - nowSpeed) > 0.01){
            if (scrollSpeed > nowSpeed) {
                nowSpeed += 0.1f;
            } else {
                nowSpeed -= 0.1f;
            }
        }else{
            nowSpeed = scrollSpeed;
        }
        nowSpeed = clamp(nowSpeed, 0, 0.5f);
        CDRotation += nowSpeed * delta * 10;
        if(Math.abs(lastScroll - listWidget.getScrollAmount()) <= 3){
            lastScroll = (float) listWidget.getScrollAmount();
        }else {
            if (lastScroll < listWidget.getScrollAmount()) {
                lastScroll += (float) (Math.abs(lastScroll - listWidget.getScrollAmount()) / 10);
            } else {
                lastScroll -= (float) (Math.abs(lastScroll - listWidget.getScrollAmount()) / 10);
            }
        }

        var x = left + 15;
        var y = top + 15;
        var size = 100;
        guiGraphics.pose().translate(x + (float) size / 2, y + (float) size / 2, 0);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(CDRotation));
        guiGraphics.pose().translate(-x - (float) size / 2, -y - (float) size / 2, 0);
        if(NetMusicListUtil.isGLY()){
            guiGraphics.blit(GLY091020, x, y, size, size, 0, 0, 256, 256, 256, 256);
        }
        else if(NetMusicListUtil.isN44()) {
            if(PLAYER_HAND == null){
                PLAYER_HAND = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(Minecraft.getInstance().getUser().getGameProfile());
            }
            guiGraphics.blit(PLAYER_HAND, x, y, size, size, 8, 8, 8, 8, 64, 64);
        }else{
            guiGraphics.blit(BACKGROUND_TEXTURE, x, y, size, size, 322, 0, 128, 128, 512, 256);
        }
        guiGraphics.pose().popPose();
    }

    public void renderPointer(@NotNull GuiGraphics guiGraphics, float delta){
        if(musicList.size() == listWidget.getSelectedIndex()){
            if(pointerRotation < 45) {
                pointerRotation += 10f * delta;
                if(pointerRotation > 44){
                    pointerRotation = 45;
                }
            }
        }else{
            if(pointerRotation > 0) {
                pointerRotation -= 10f * delta;
                if(pointerRotation < 1){
                    pointerRotation = 0;
                }
            }
        }
        var x = left + 50;
        var y = top + 3;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + 55, y + 3, 0);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(pointerRotation));
        guiGraphics.pose().translate(-x - 55, -y - 3, 0);
        guiGraphics.blit(BACKGROUND_TEXTURE, x, y, 64, 58, 0, 256 - 69, 69, 66, 512, 256);
        guiGraphics.pose().popPose();
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        renderCD(context, delta);
        renderPointer(context, delta);
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
            sendToServer(new DeleteMusicDataPacket(o1));
            updateButton();
            sendPackage();
        }
    }

    public void moveMusic(boolean isUp){
        if (this.listWidget.getSelectedIndex() != musicList.size()) {
            var i1 = listWidget.getSelectedIndex() - (isUp ? 1 : -1);
            sendToServer(new MoveMusicDataPacket(listWidget.getSelectedIndex(), i1));
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

    public static void sendToServer(Object payload){
        NetMusicList.CHANNEL.sendToServer(payload);
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
                context.fill(x, y, x + entryWidth - 4, y + entryHeight, 0x80FFFFFF);
            }

            // 渲染文本
            context.drawString(
                    font,
                    font.plainSubstrByWidth(musicName, entryWidth - 10),
                    x + 5,
                    y + (entryHeight - 10) / 2 + 1,
                    0xFFFFFF
            );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            super.mouseClicked(mouseX, mouseY, button);
            MusicSelectionScreen.this.index = listWidget.children().indexOf(this);
            listWidget.setSelectedIndex(index);
            sendPackage();
            updateButton();
            return true;
        }
    }

    public void sendPackage(){
        this.index = listWidget.getSelectedIndex();
        sendToServer(new MusicListDataPacket(index, this.playModeButton.playMode));
    }

    private class MusicListWidget extends ObjectSelectionList<MusicListEntry> {
        public MusicListWidget() {
            super(Minecraft.getInstance(), 197,
                    153, 12, 12, font.lineHeight + 1);
            x0 = left + 122;
            x1 = backgroundWidth + MusicSelectionScreen.this.left - 5;
            this.setRenderHeader(false, 0);
            this.setRenderTopAndBottom(false);
        }

        @Override
        protected int getScrollbarPosition() {
            return y0 + this.width - 5;
        }

        @Override
        public int getRowWidth() {
            return this.width - 16;
        }

        public void addMusicEntry(String musicName) {
            this.addEntry(new MusicListEntry(musicName));
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int x, int y, float d) {
            super.render(guiGraphics, x, y, d);
            if (this.getMaxScroll() > 0) {
                int l = this.getScrollbarPosition();
                int i1 = (int) ((float) (this.height * this.height) / (float) this.getMaxPosition());
                i1 = Mth.clamp(i1, 32, this.height - 8);
                int k = (int) this.getScrollAmount() * (this.height - i1) / this.getMaxScroll() + y0;
                if (k < this.y0) {
                    k = this.y0;
                }
                guiGraphics.blitNineSliced(
                        ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "textures/gui/bar.png"),
                        l - 1, k, 5, i1, 5, 18,
                        1, 4, 1, 13
                );
            }
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
    }

    public static void open(List<ItemMusicCD.SongInfo> musicList, PlayMode mode, Integer index) {
        var l = new ArrayList<String>();
        for(ItemMusicCD.SongInfo info: musicList){
            if(info.artists.isEmpty()){
                l.add(info.songName);
            } else {
                var a = new StringBuilder();
                for(String artist: info.artists){
                    a.append(artist);
                    a.append("、");
                }
                var t = "";
                if(info.readOnly){
                    t = Component.translatable("gui.net_music_list.read_only").getString();
                }else if(info.vip){
                    t = Component.translatable("gui.net_music_list.vip").getString();
                }
                var AT = a.toString();
                l.add(String.format("%s%s —— %s", info.songName, t, AT.substring(0, AT.length() - 1)));
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
        protected void renderWidget(@NotNull GuiGraphics context, int p_282682_, int p_281714_, float p_282542_) {
            super.renderWidget(context, p_282682_, p_281714_, p_282542_);
            var x = 0;
            switch (this.playMode){
                case SEQUENTIAL -> x = 1;
                case RANDOM -> x = 26;
                case LOOP -> x = 51;
            }
            context.blitNineSliced(BUTTON_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 25, 25,
                    3, 3, 3, 3);
            context.blit(BACKGROUND_TEXTURE, this.getX(), this.getY(),
                    x, 162, this.width, this.height, 512, 256);
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
//            super.renderWidget(context, p_282682_, p_281714_, p_282542_);
            // 我这辈子都不知道这个函数是干嘛的
            context.blitNineSliced(BUTTON_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 4, 25, 25, 0, 0);
            context.blit(BACKGROUND_TEXTURE, this.getX(), this.getY(),
                    isUp ? 76 : 101, 162, this.width, this.height, 512, 256);
        }
    }
}