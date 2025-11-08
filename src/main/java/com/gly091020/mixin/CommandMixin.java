package com.gly091020.mixin;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList;
import com.github.tartaricacid.netmusic.command.NetMusicCommand;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.item.NetMusicListItem;
import com.gly091020.util.CacheManager;
import com.gly091020.util.NetMusicListUtil;
import com.google.gson.Gson;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(NetMusicCommand.class)
public class CommandMixin {
    @Inject(method = "get", at = @At("RETURN"), remap = false)
    private static void addCommand(CallbackInfoReturnable<LiteralArgumentBuilder<CommandSourceStack>> cir){
        if(FMLEnvironment.dist == Dist.DEDICATED_SERVER){return;}
        cir.getReturnValue().then(Commands.literal("music_list_to_item").then(Commands.argument("id",
                LongArgumentType.longArg()).executes(CommandMixin::netMusicListNeoForge$toItem)));
        if(NetMusicList.CONFIG.enableCache){
            cir.getReturnValue().then(Commands.literal("cache_all_music").then(Commands.argument("id",
                    LongArgumentType.longArg()).executes(CommandMixin::netmusiclistforge$cacheAll)));
        }
    }

    @Unique
    private static final Gson netMusicListNeoForge$GSON = new Gson();

    @Unique
    private static int netmusiclistforge$cacheAll(CommandContext<CommandSourceStack> context){
        if(context.getSource().getPlayer() == null){
            return 0;
        }
        int count = 0;
        try{
            var id = LongArgumentType.getLong(context, "id");
            var songs = NetMusicListUtil.getMusicList(id);
            for(ItemMusicCD.SongInfo info: songs){
                try {
                    var songId = NetMusicListUtil.getIdFromInfo(info);
                    var uuid = UUID.randomUUID().toString();
                    if(!CacheManager.hasCache(songId)){
                        CacheManager.startSongDownload(songId, uuid);
                        CacheManager.startImgDownload(songId, uuid);
                        CacheManager.startLycDownload(songId, uuid);
                    }
                    count++;
                }catch (Exception ignored){}
            }
        } catch (Exception e) {
            throw new CommandRuntimeException(Component.literal(e.getMessage()));
        }
        int finalCount = count;
        context.getSource().sendSuccess(() -> Component.literal(String.format("缓存中(共%s项)", finalCount * 3)),
                false);
        return count;
    }

    @Unique
    private static int netMusicListNeoForge$toItem(CommandContext<CommandSourceStack> context){
        if(context.getSource().getPlayer() == null){
            return 0;
        }
        try {
            // todo:未完成
            var withoutVip = false;
            try{
                withoutVip = BoolArgumentType.getBool(context, "withoutVIP");
            }catch (IllegalArgumentException ignored){}

            var stack = new ItemStack(NetMusicList.MUSIC_LIST_ITEM.get(), 1);
            NetMusicListItem.setSongIndex(stack, 0);

            var id = LongArgumentType.getLong(context, "id");
            NetEaseMusicList pojo = netMusicListNeoForge$GSON.fromJson(NetMusic.NET_EASE_WEB_API.list(id), NetEaseMusicList.class);
            var SONGS = NetMusicListUtil.getMusicList(id);
            var name = pojo.getPlayList().getName();

            for(ItemMusicCD.SongInfo info: SONGS){
                if(withoutVip && info.vip){continue;}
                NetMusicListItem.setSongInfo(info, stack);
            }

            context.getSource().getPlayer().addItem(stack);
            context.getSource().sendSuccess(() -> Component.translatable("command.net_music_list.success", name),
                    false);
            return 1;
        } catch (Exception e) {
            NetMusic.LOGGER.error("出现错误：", e);
            context.getSource().sendFailure(Component.translatable("command.net_music_list.fail"));
        }

        return 1;
    }
}
