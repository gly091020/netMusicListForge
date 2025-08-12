package com.gly091020;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.gly091020.item.NetMusicListItem;
import com.gly091020.item.NetMusicPlayerItem;
import com.gly091020.packet.PacketRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(NetMusicList.ModID)
public class NetMusicList {
    public static final String ModID = "net_music_list";

    public static final Logger LOGGER = LoggerFactory.getLogger(ModID);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModID);

    public static final RegistryObject<NetMusicListItem> MUSIC_LIST_ITEM = ITEMS.register("music_list",
            NetMusicListItem::new);
    public static final RegistryObject<NetMusicPlayerItem> MUSIC_PLAYER_ITEM = ITEMS.register("music_player",
            NetMusicPlayerItem::new);

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ModID, "send_data"),
            // todo:别信它 ↑ MD没有问题硬是给我整个错误照着它的改完反倒是跑不起来
            // 【【重音テト/MV/中译版】气到原地爆炸！/イライラしている（BY：じん OFFICIAL YOUTUBE CHANNEL）-哔哩哔哩】 https://b23.tv/Bo8I5ri
            () -> "1.1",  // 协议版本
            "1.1"::equals,
            "1.1"::equals
    );

    public NetMusicList(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addItemsToCreativeTab);
        if(FMLEnvironment.dist == Dist.CLIENT){
            PacketRegistry.registryClient();
        }else{
            PacketRegistry.registryServer();
        }
    }

    public NetMusicList() {
        this(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void addItemsToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == InitItems.NET_MUSIC_TAB.get()) {
            event.getEntries().putAfter(
                    new ItemStack(InitItems.MUSIC_CD.get()),
                    new ItemStack(MUSIC_LIST_ITEM.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(MUSIC_LIST_ITEM.get()),
                    new ItemStack(MUSIC_PLAYER_ITEM.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
    }
}