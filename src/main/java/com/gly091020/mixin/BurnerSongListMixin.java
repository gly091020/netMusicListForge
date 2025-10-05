package com.gly091020.mixin;

import com.github.tartaricacid.netmusic.client.gui.CDBurnerMenuScreen;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import com.gly091020.util.NetMusicListUtil;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(value = CDBurnerMenuScreen.class, remap = false)
public class BurnerSongListMixin {
    @Shadow
    private EditBox textField;
    @Shadow
    private Component tips;
    @Shadow
    private Checkbox readOnlyButton;
    @Unique
    private static final Pattern LIST_ID_REG = Pattern.compile("^list/(\\d+)$");
    @Inject(method = "handleCraftButton", at = @At(value = "INVOKE", target = "Ljava/util/regex/Matcher;matches()Z"), cancellable = true)
    public void onCraft(CallbackInfo ci){
        var matcher = LIST_ID_REG.matcher(textField.getValue());
        if(matcher.find()){
            long listID = Long.parseLong(matcher.group(1));
            try{
                var songs = NetMusicListUtil.getMusicList(listID);
                if(!songs.isEmpty()){
                    for (ItemMusicCD.SongInfo info: songs){
                        info.readOnly = readOnlyButton.selected();
                        NetworkHandler.CHANNEL.sendToServer(new SetMusicIDMessage(info));
                    }
                }
            } catch (Exception e) {
                tips = Component.translatable("gui.netmusic.cd_burner.get_info_error");
            }
            ci.cancel();
        }
    }
}
