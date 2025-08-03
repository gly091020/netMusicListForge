package com.gly091020.TBackpack;

import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tiviacz.travelersbackpack.inventory.upgrades.UpgradeBase;
import com.tiviacz.travelersbackpack.items.upgrades.UpgradeItem;
import com.tiviacz.travelersbackpack.util.NbtHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;

public class NetMusicUpgradeItem extends UpgradeItem {
    public NetMusicUpgradeItem() {
        super(new Properties().stacksTo(1), "item.net_music_upgrade.name");
    }

    @Override
    public Class<? extends UpgradeBase<?>> getUpgradeClass() {
        return NetMusicUpgrade.class;
    }

    @Override
    public TriFunction<UpgradeManager, Integer, ItemStack, Optional<? extends UpgradeBase<?>>> getUpgrade() {
        return (upgrade, slot, stack) -> Optional.of(new NetMusicUpgrade(upgrade, slot,
                NbtHelper.getOrDefault(stack, "Inventory",
                        NonNullList.withSize(1, ItemStack.EMPTY))));
    }

    @Override
    public boolean isTickingUpgrade() {
        return true;
    }
}
