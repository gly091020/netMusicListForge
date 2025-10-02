package com.gly091020.client;

import com.gly091020.NetMusicList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

public class NetMusicListManualRenderer extends BlockEntityWithoutLevelRenderer {
    // 用了很多来自IAM Music Player的渲染代码
    private static final Component DEV_TEXT = Component.literal("5112151111121");
    private static final Component MANUAL_TEXT = Component.translatable("manual.net_music_list.manual");
    private static final Component COVER_INFO1_TEXT = Component.translatable("manual.net_music_list.cover1");
    private static final Component COVER_INFO2_TEXT = Component.translatable("manual.net_music_list.cover2");
    private static final Component MOD_NAME_TEXT = Component.translatable("modmenu.nameTranslation.net_music_list");
    private final Component MOD_VERSION_TEXT;

    private static BakedModel model;
    private static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "manual_model"),
            "inventory");
    public NetMusicListManualRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        var info = ModList.get().getModFileById(NetMusicList.ModID).getMods().get(0);
        MOD_VERSION_TEXT = Component.literal(info.getVersion().toString());
    }

    private static final NetMusicListManualRenderer INSTANCE = new NetMusicListManualRenderer();

    public static NetMusicListManualRenderer getInstance() {
        return INSTANCE;
    }

    @Override
    @SuppressWarnings("all")
    public void renderByItem(@NotNull ItemStack itemStack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        var renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        var vertexConsumer = ItemRenderer.getFoilBufferDirect(multiBufferSource, Sheets.solidBlockSheet(), true, itemStack.hasFoil());
        if(model == null){
            model = Minecraft.getInstance().getModelManager().getModel(MODEL_LOCATION);
        }
        renderer.renderModel(poseStack.last(), vertexConsumer, null, model, 1, 1, 1, packedLight, packedOverlay);
        renderText(poseStack, multiBufferSource, DEV_TEXT, packedLight, 9f, 15.75f, 0.4f, false, 0);
        renderText(poseStack, multiBufferSource, MANUAL_TEXT, packedLight, 4.225f / 2f, 15.75f, 0.54f, true, 0xFFFFFFFF);
        renderText(poseStack, multiBufferSource, MOD_NAME_TEXT, packedLight, 9.85f, 14.5f, 0.6f, false, 0);
        renderText(poseStack, multiBufferSource, MOD_VERSION_TEXT, packedLight, 9.85f, 13.5f, 0.4f, false, 0);
        renderText(poseStack, multiBufferSource, COVER_INFO1_TEXT, packedLight, 10f / 2f, 12, 0.4f, true, 0xFFFFFFFF);
        renderText(poseStack, multiBufferSource, COVER_INFO2_TEXT, packedLight, 10f / 2f, 11.5f, 0.4f, true, 0XFFFFFFFF);
    }

    public void renderText(@NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, Component text, int light, float x, float y, float scale, boolean center, int color){
        var mc = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(0, 1.0E-3F, 0);
        poseStack.translate(1 / 16f * x, 0.5f / 16, 1 / 16f * y);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        int lineHeight = -mc.font.lineHeight + mc.font.lineHeight;
        if(center){
            poseStack.pushPose();
            poseStack.scale(0.010416667F * scale, -0.010416667F * scale, 0.010416667F * scale);
            mc.font.drawInBatch(text, ((float) -mc.font.width(text) / 2f), lineHeight, color, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.NORMAL, 0, light);
            poseStack.popPose();
        }else{
            poseStack.pushPose();
            poseStack.scale(0.010416667F * scale, -0.010416667F * scale, 0.010416667F * scale);
            mc.font.drawInBatch(text, 0, lineHeight, color, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.NORMAL, 0, light);
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
