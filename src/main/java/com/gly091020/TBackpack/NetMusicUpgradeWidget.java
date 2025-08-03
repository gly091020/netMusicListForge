package com.gly091020.TBackpack;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.packet.BackpackPlayMusicPacket;
import com.tiviacz.travelersbackpack.client.screens.BackpackScreen;
import com.tiviacz.travelersbackpack.client.screens.widgets.UpgradeWidgetBase;
import com.tiviacz.travelersbackpack.client.screens.widgets.WidgetElement;
import com.tiviacz.travelersbackpack.inventory.upgrades.Point;
import net.minecraft.client.gui.GuiGraphics;

public class NetMusicUpgradeWidget extends UpgradeWidgetBase<NetMusicUpgrade> {
    private final WidgetElement playButton = new WidgetElement(new Point(24, 22), new Point(18, 18));
    private final WidgetElement stopButton = new WidgetElement(new Point(42, 22), new Point(18, 18));
    public NetMusicUpgradeWidget(BackpackScreen screen, NetMusicUpgrade upgrade, Point pos) {
        super(screen, upgrade, pos, new Point(137, 103), "gui.net_music_upgrade.tip");
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (this.isTabOpened()) {
            if (this.isMouseOverPlayButton(mouseX, mouseY)) {
                guiGraphics.blit(BackpackScreen.ICONS, this.pos.x() + this.playButton.pos().x(), this.pos.y() + this.playButton.pos().y(), 24, 18, this.playButton.size().x(), this.playButton.size().y());
            }

            if (this.isMouseOverStopButton(mouseX, mouseY)) {
                guiGraphics.blit(BackpackScreen.ICONS, this.pos.x() + this.stopButton.pos().x(), this.pos.y() + this.stopButton.pos().y(), 24, 18, this.stopButton.size().x(), this.stopButton.size().y());
            }
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.upgrade.getUpgradeManager().getWrapper().getScreenID() == 1 && this.isMouseOverPlayButton(pMouseX, pMouseY) && this.isBackpackOwner() && this.isTabOpened()) {
            var info = ItemMusicCD.getSongInfo(this.upgrade.handler.getStackInSlot(0));
            if(info != null) {
                NetMusicList.CHANNEL.sendToServer(new BackpackPlayMusicPacket(
                        this.screen.getScreenPlayer().getId(), info.songUrl, info.songTime, info.songName,
                        this.screen.getWrapper().getBackpackSlotIndex(), this.dataHolderSlot
                        ));
                this.screen.playUIClickSound();
                return true;
            }
        } else if (this.upgrade.getUpgradeManager().getWrapper().getScreenID() == 1 && this.isMouseOverStopButton(pMouseX, pMouseY) && this.isBackpackOwner() && this.isTabOpened()) {
            // todo 暂不实现停止播放

            this.screen.playUIClickSound();
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public boolean isMouseOverPlayButton(double mouseX, double mouseY) {
        return this.isWithinBounds(mouseX, mouseY, this.playButton);
    }

    public boolean isMouseOverStopButton(double mouseX, double mouseY) {
        return this.isWithinBounds(mouseX, mouseY, this.stopButton);
    }
}
