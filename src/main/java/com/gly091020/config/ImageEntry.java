package com.gly091020.config;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ImageEntry extends AbstractConfigListEntry<Object> {
    private final ResourceLocation image;
    private int entryWidth = 100;
    public ImageEntry(ResourceLocation image){
        super(Component.empty(), false);
        this.image = image;
    }
    @Override
    public boolean isRequiresRestart() {
        return false;
    }

    @Override
    public void setRequiresRestart(boolean b) {

    }

    @Override
    public Component getFieldName() {
        return Component.empty();
    }

    @Override
    public Optional<Object> getDefaultValue() {
        return Optional.empty();
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        this.entryWidth = entryWidth;
        graphics.blit(image, x, y, 0, 0, entryWidth, entryHeight, entryWidth, entryHeight);
    }

    @Override
    public int getItemHeight() {
        return entryWidth / 10;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return List.of();
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return List.of();
    }
}
