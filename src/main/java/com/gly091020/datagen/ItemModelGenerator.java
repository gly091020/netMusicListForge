package com.gly091020.datagen;

import com.gly091020.NetMusicList;
import com.gly091020.block.EnderMusicPlayer;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, NetMusicList.ModID, existingFileHelper);
    }
    @Override
    protected void registerModels() {
        basicItem(NetMusicList.MUSIC_LIST_ITEM.get());
        basicItem(NetMusicList.MUSIC_PLAYER_ITEM.get());
        blockItem(NetMusicList.ENDER_MUSIC_PLAYER);
    }

    public void blockItem(RegistryObject<EnderMusicPlayer> block) {
        if (block.getId() != null) {
            withExistingParent(block.getId().getPath(),
                    modLoc("block/" + block.getId().getPath()));
        }
    }
}
