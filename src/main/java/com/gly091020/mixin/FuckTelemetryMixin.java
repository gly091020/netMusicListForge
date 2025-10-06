package com.gly091020.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Advancement.class)
public class FuckTelemetryMixin {
    @Mutable
    @Shadow
    @Final
    private boolean sendsTelemetryEvent;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void fuckTelemetry(ResourceLocation p_286878_, Advancement p_286496_, DisplayInfo p_286499_, AdvancementRewards p_286389_, Map<?, ?> p_286635_, String[][] p_286882_, boolean p_286478_, CallbackInfo ci){
        // TM的数据包整那么难用还默认收集数据，收你M呢
        sendsTelemetryEvent = false;
    }
}
