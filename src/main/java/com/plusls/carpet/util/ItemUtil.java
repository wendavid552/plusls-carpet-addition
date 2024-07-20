package com.plusls.carpet.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//#if MC > 12004
//$$ import net.minecraft.core.HolderLookup;
//#endif

public class ItemUtil {
    //#if MC > 11502
    @Nullable
    public static ItemStack upGradeToNetheriteLike(@NotNull ItemStack stack) {
        Item newItem;

        if (Items.DIAMOND_SWORD.equals(stack.getItem())) {
            newItem = Items.NETHERITE_SWORD;
        } else if (Items.DIAMOND_SHOVEL.equals(stack.getItem())) {
            newItem = Items.NETHERITE_SHOVEL;
        } else if (Items.DIAMOND_PICKAXE.equals(stack.getItem())) {
            newItem = Items.NETHERITE_PICKAXE;
        } else if (Items.DIAMOND_AXE.equals(stack.getItem())) {
            newItem = Items.NETHERITE_AXE;
        } else if (Items.DIAMOND_HOE.equals(stack.getItem())) {
            newItem = Items.NETHERITE_HOE;
        } else if (Items.DIAMOND_HELMET.equals(stack.getItem())) {
            newItem = Items.NETHERITE_HELMET;
        } else if (Items.DIAMOND_CHESTPLATE.equals(stack.getItem())) {
            newItem = Items.NETHERITE_CHESTPLATE;
        } else if (Items.DIAMOND_LEGGINGS.equals(stack.getItem())) {
            newItem = Items.NETHERITE_LEGGINGS;
        } else if (Items.DIAMOND_BOOTS.equals(stack.getItem())) {
            newItem = Items.NETHERITE_BOOTS;
        } else {
            newItem = null;
        }

        if (newItem == null) {
            return null;
        }

        ItemStack ret = new ItemStack(newItem);

        //#if MC > 12004
        //$$ ret.applyComponents(stack.getComponents());
        //$$ ret.setDamageValue(ret.getMaxDamage() - 1);
        //#else
        CompoundTag compoundTag = stack.getTag();

        if (compoundTag != null) {
            ret.setTag(compoundTag.copy());
            ret.setDamageValue(ret.getMaxDamage() - 1);
        }
        //#endif

        return ret;
    }
    //#endif
}
