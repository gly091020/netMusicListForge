// 部分参考MaidNetMusicSound，以保持与女仆行为一致
package com.gly091020.sounds;

import com.github.tartaricacid.netmusic.client.audio.NetMusicAudioStream;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.init.InitSounds;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.TBackpack.RegistryUpgrade;
import com.gly091020.item.NetMusicListItem;
import com.gly091020.item.NetMusicPlayerItem;
import com.tiviacz.travelersbackpack.inventory.BackpackContainer;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import com.tiviacz.travelersbackpack.util.NbtHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class BackpackNetMusicSound extends AbstractTickableSoundInstance {
    final Player player;
    final URL url;
    final int countTick;
    int tick = 0;
    final int slot;
    final int upgradeSlot;

    public BackpackNetMusicSound(Player player, URL songUrl, int second, int slot, int upgradeSlot) {
        super(InitSounds.NET_MUSIC.get(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.url = songUrl;
        this.countTick = second * 20;
        this.volume = 4f;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.slot = slot;
        this.upgradeSlot = upgradeSlot;
    }

    @Override
    public void tick() {
        if(player.isRemoved()){
            stop();
        }
        var i = player.getInventory().getItem(slot);
        if(!(i.getItem() instanceof TravelersBackpackItem)){
            stop();
            return;
        }
        i = NbtHelper.getOrDefault(i, "Upgrades", NonNullList.withSize(1, ItemStack.EMPTY)).get(upgradeSlot);
        if(!i.is(RegistryUpgrade.MUSIC_UPGRADE_ITEM.get())){
            stop();
            return;
        }

        i = NbtHelper.getOrDefault(i, "Inventory", NonNullList.withSize(1, ItemStack.EMPTY)).get(0);

        if(i.is(InitItems.MUSIC_CD.get())){
            if(ItemMusicCD.getSongInfo(i) == null) {
                stop();
            }
        }else if(i.is(NetMusicList.MUSIC_LIST_ITEM.get())){
            var info = NetMusicListItem.getSongInfo(i);
            if(info == null) {
                stop();
            }
        }else{
            stop();
        }

        ClientLevel level = Minecraft.getInstance().level;
        if(level == null){
            stop();
        }else{
            ++this.tick;
            if (this.tick > this.countTick + 50){
                stop();
            }else{
                this.x = player.getX();
                this.y = player.getY();
                this.z = player.getZ();
            }
        }
    }

    @Override
    public @NotNull CompletableFuture<AudioStream> getStream(@NotNull SoundBufferLibrary soundBuffers, @NotNull Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new NetMusicAudioStream(url);
            } catch (UnsupportedAudioFileException | IOException e) {
                NetMusicList.LOGGER.error("出现错误：", e);
                return null;
            }
        }, Util.backgroundExecutor());
    }
}
