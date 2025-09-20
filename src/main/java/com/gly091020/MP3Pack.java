package com.gly091020;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.locating.IModFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.function.Consumer;

public class MP3Pack implements RepositorySource {
    private final Pack pack;
    public MP3Pack(){
        Pack.ResourcesSupplier supplier = (name) -> getLegacyPack();
        pack = Pack.create("net_music_list_mp3_pack",
                Component.translatable("title.net_music_list.mp3"),
                false, supplier,
                new Pack.Info(Component.translatable("description.net_music_list.mp3"),
                        SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES),
                        FeatureFlagSet.of()), PackType.CLIENT_RESOURCES,
                Pack.Position.TOP, false, PackSource.BUILT_IN);
    }

    private net.minecraftforge.resource.PathPackResources getLegacyPack() {
        final IModFile file = ModList.get().getModFileById(NetMusicList.ModID).getFile();
        return new net.minecraftforge.resource.PathPackResources(file.getFileName(), true, file.getFilePath()) {
            protected @NotNull Path resolve(String @NotNull ... paths) {
                String[] newPaths = new String[paths.length + 1];
                newPaths[0] = "mp3";
                System.arraycopy(paths, 0, newPaths, 1, paths.length);
                return file.findResource(newPaths);
            }
        };
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        consumer.accept(pack);
    }
}
