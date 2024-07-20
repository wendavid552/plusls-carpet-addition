package com.plusls.carpet.util.rule.emptyShulkerBoxStack;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

//#if MC > 12004
//$$ import net.minecraft.core.component.DataComponents;
//$$ import net.minecraft.world.item.component.ItemContainerContents;
//#else
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import top.hendrixshen.magiclib.api.compat.minecraft.nbt.TagCompat;
//#endif

public class ShulkerBoxItemUtil {
    public static final int SHULKERBOX_MAX_STACK_AMOUNT = 64;

    public static boolean isEmptyShulkerBoxItem(@NotNull ItemStack itemStack) {
        //#if MC > 12004
        //$$ ItemContainerContents countContainer = itemStack.getComponents().get(DataComponents.CONTAINER);
        //$$ return countContainer == null || !countContainer.nonEmptyItems().iterator().hasNext();
        //#else

        if (!(itemStack.getItem() instanceof BlockItem) ||
                !(((BlockItem) itemStack.getItem()).getBlock() instanceof ShulkerBoxBlock)) {
            return false;
        }

        CompoundTag nbt = itemStack.getTag();

        if (nbt != null && nbt.contains("BlockEntityTag", TagCompat.TAG_COMPOUND)) {
            CompoundTag tag = nbt.getCompound("BlockEntityTag");

            if (tag.contains("Items", 9)) {
                ListTag tagList = tag.getList("Items", TagCompat.TAG_COMPOUND);
                return !tagList.isEmpty();
            }
        }

        return true;
        //#endif
    }

    public static int getMaxCount(ItemStack itemStack) {
        if (PluslsCarpetAdditionSettings.emptyShulkerBoxStack && ShulkerBoxItemUtil.isEmptyShulkerBoxItem(itemStack)) {
            return ShulkerBoxItemUtil.SHULKERBOX_MAX_STACK_AMOUNT;
        } else {
            return itemStack.getMaxStackSize();
        }
    }

    public static boolean isStackable(ItemStack itemStack) {
        return getMaxCount(itemStack) > 1 && (!itemStack.isDamageableItem() || !itemStack.isDamaged());
    }
}