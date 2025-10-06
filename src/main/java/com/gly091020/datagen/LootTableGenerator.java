package com.gly091020.datagen;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class LootTableGenerator extends LootTableProvider {
    public LootTableGenerator(PackOutput output) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(BookLootTable::new, LootContextParamSets.ADVANCEMENT_REWARD),
                new SubProviderEntry(BlockLootTable::new, LootContextParamSets.BLOCK)
        ));
    }

    @SuppressWarnings("all")
    public static class BookLootTable implements LootTableSubProvider{
        @Override
        public void generate(@NotNull BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
            consumer.accept(ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "book"),
                    new LootTable.Builder().withPool(
                            new LootPool.Builder()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(NetMusicList.MANUAL.get()))
                    )
            );

            consumer.accept(ResourceLocation.fromNamespaceAndPath(NetMusicList.ModID, "cat_dj"),
                    new LootTable.Builder().withPool(
                            new LootPool.Builder()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(InitItems.MUSIC_CD.get()).apply(SetNbtFunction.setTag(getCatDJTag())))
                    )
            );
        }

        private static CompoundTag getCatDJTag(){
            var nbt = new CompoundTag();
            var nbt1 = new CompoundTag();
            var songInfo = new ItemMusicCD.SongInfo();
            songInfo.songName = "自学编曲作品集";
            songInfo.songTime = 201;
            songInfo.readOnly = true;
            songInfo.artists = List.of("Frafrale");
            songInfo.songUrl = "https://music.163.com/song/media/outer/url?id=2752399163.mp3";
            songInfo.vip = false;
            ItemMusicCD.SongInfo.serializeNBT(songInfo, nbt1);
            nbt.put(ItemMusicCD.SONG_INFO_TAG, nbt1);
            return nbt;
        }
    }

    public static class BlockLootTable extends BlockLootSubProvider{

        protected BlockLootTable() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            dropSelf(NetMusicList.ENDER_MUSIC_PLAYER.get());
        }

        @Override
        protected @NotNull Iterable<Block> getKnownBlocks() {
            return List.of(NetMusicList.ENDER_MUSIC_PLAYER.get());
        }
    }
}
