package com.gly091020.datagen;

import com.gly091020.NetMusicList;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings("all")
public class AdvancementGenerator extends ForgeAdvancementProvider {
    public AdvancementGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new NetMusicListAdvancements()));
    }

    private static class NetMusicListAdvancements implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<Advancement> saver, ExistingFileHelper existingFileHelper) {
            Advancement.Builder.advancement()
                    .addCriterion("get_item",
                            InventoryChangeTrigger.TriggerInstance.hasItems(NetMusicList.MUSIC_LIST_ITEM.get()))
                    .rewards(AdvancementRewards.Builder.loot(ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "book")))
                    .save(saver, ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "give_book"), existingFileHelper);
        }
    }
}
