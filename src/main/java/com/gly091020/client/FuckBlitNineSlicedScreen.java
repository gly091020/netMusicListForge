package com.gly091020.client;

import com.gly091020.NetMusicList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FuckBlitNineSlicedScreen extends Screen {
    // 常量定义最大值
    private static final int MAX_VALUE = 500;

    // 纹理资源位置
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "textures/gui/button.png");

    // 所有参数变量，使用原始名称
    // 这些名称还是错的
    private int p_283509_ = 50;  // x 位置
    private int p_283259_ = 50;  // y 位置
    private int p_283273_ = 100; // 宽度
    private int p_282043_ = 100; // 高度
    private int p_281430_ = 0;   // 左侧不重复宽度（与右侧不重复宽度不相同会导致渲染错误）
    private int p_281412_ = 0;   // 边框宽
    private int p_282566_ = 100; // 右侧不重复宽度（与左侧不重复宽度不相同会导致渲染错误）
    private int p_281971_ = 100; // 边框高
    private int p_282879_ = 10;  // 9切区域宽
    private int p_281529_ = 10;  // 9切区域高
    private int p_281924_ = 10;  // v 坐标
    private int p_281407_ = 10;  // u 坐标

    // 滑动条数组
    private ParameterSlider[] sliders = new ParameterSlider[12];

    public FuckBlitNineSlicedScreen() {
        // fuck mojang
        // fuck mojang
        // fuck mojang
        // fuck mojang
        // fuck mojang
        // fuck mojang
        // fuck mojang
        // fuck mojang
        // fuck mojang
        // fuck mojang
        // fuck mojang
        // fuck mojang
        // fuck mojang
        // 一共是13个参数，所以fuck13次
        super(Component.literal("TM的 BlitNineSliced 到底怎么用啊啊啊啊啊啊！"));
    }

    @Override
    protected void init() {
        super.init();

        // 参数名称数组
        String[] paramNames = {
                "p_283509_", "p_283259_", "p_283273_", "p_282043_",
                "p_281430_", "p_281412_", "p_282566_", "p_281971_",
                "p_282879_", "p_281529_", "p_281924_", "p_281407_"
        };

        // 创建12个滑动条
        for (int i = 0; i < 12; i++) {
            final int paramIndex = i;
            sliders[i] = new ParameterSlider(
                    this.width / 2 - 155,  // x 位置
                    30 + i * 25,           // y 位置
                    310,                   // 宽度
                    20,                    // 高度
                    Component.literal(paramNames[i]),
                    getParameterValue(i) / (double) MAX_VALUE,
                    paramIndex
            );
            this.addRenderableWidget(sliders[i]);
        }

        // 添加关闭按钮
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE,
                        button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 30, 200, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染背景
        this.renderBackground(guiGraphics);

        // 渲染标题
        guiGraphics.drawString(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 10, 0xFFFFFF);

        // 使用当前参数渲染九宫格纹理
        try{
            guiGraphics.blitNineSliced(
                    TEXTURE,
                    p_283509_, p_283259_, p_283273_, p_282043_,
                    p_281430_, p_281412_, p_282566_, p_281971_,
                    p_282879_, p_281529_, p_281924_, p_281407_
            );
        } catch (Exception e) {
            guiGraphics.drawString(this.font, "渲染时出现问题！", this.width / 2 - this.font.width(this.title) / 2, 10 + font.lineHeight, 0xFFFF0000);
        }

        // 渲染当前参数值信息
        int infoY = 30 + 12 * 25 + 10;
        guiGraphics.drawString(this.font,
                String.format("位置: (%d, %d)  大小: %dx%d", p_283509_, p_283259_, p_283273_, p_282043_),
                10, infoY, 0xFFFFFF);
        guiGraphics.drawString(this.font,
                String.format("UV: (%d, %d)  UV大小: %dx%d", p_281430_, p_281412_, p_282566_, p_281971_),
                10, infoY + 12, 0xFFFFFF);
        guiGraphics.drawString(this.font,
                String.format("切片: 左%d 上%d 右%d 下%d", p_282879_, p_281529_, p_281924_, p_281407_),
                10, infoY + 24, 0xFFFFFF);

        // 渲染滑动条和其他组件
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        // 返回上一个屏幕
        this.minecraft.setScreen(null);
    }

    /**
     * 根据索引获取参数值
     */
    private int getParameterValue(int index) {
        return switch (index) {
            case 0 -> p_283509_;
            case 1 -> p_283259_;
            case 2 -> p_283273_;
            case 3 -> p_282043_;
            case 4 -> p_281430_;
            case 5 -> p_281412_;
            case 6 -> p_282566_;
            case 7 -> p_281971_;
            case 8 -> p_282879_;
            case 9 -> p_281529_;
            case 10 -> p_281924_;
            case 11 -> p_281407_;
            default -> 0;
        };
    }

    /**
     * 根据索引设置参数值
     */
    private void setParameterValue(int index, int value) {
        switch (index) {
            case 0 -> p_283509_ = value;
            case 1 -> p_283259_ = value;
            case 2 -> p_283273_ = value;
            case 3 -> p_282043_ = value;
            case 4 -> p_281430_ = value;
            case 5 -> p_281412_ = value;
            case 6 -> p_282566_ = value;
            case 7 -> p_281971_ = value;
            case 8 -> p_282879_ = value;
            case 9 -> p_281529_ = value;
            case 10 -> p_281924_ = value;
            case 11 -> p_281407_ = value;
        }
    }

    /**
     * 自定义滑动条类
     */
    private class ParameterSlider extends AbstractSliderButton {
        private final int paramIndex;
        private final String paramName;

        public ParameterSlider(int x, int y, int width, int height, Component message, double value, int paramIndex) {
            super(x, y, width, height, message, value);
            this.paramIndex = paramIndex;
            this.paramName = message.getString();
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int value = (int) (this.value * MAX_VALUE);
            this.setMessage(Component.literal(paramName + ": " + value));
        }

        @Override
        protected void applyValue() {
            int newValue = (int) (this.value * MAX_VALUE);
            setParameterValue(paramIndex, newValue);
        }
    }
}