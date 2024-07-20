package com.plusls.carpet.mixin.rule.emptyShulkerBoxStack;

import com.plusls.carpet.util.rule.emptyShulkerBoxStack.ShulkerBoxItemUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//#if MC > 12004
//$$ import net.minecraft.world.inventory.Slot;
//#endif

@Mixin(AbstractContainerMenu.class)
public class MixinAbstractContainerMenu {
    @Redirect(
            method = "moveItemStackTo",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isStackable()Z",
                    ordinal = 0
            )
    )
    private boolean insertItemIsStackable(ItemStack itemStack) {
        return ShulkerBoxItemUtil.isStackable(itemStack);
    }

    @Redirect(
            method = "moveItemStackTo",
            at = @At(
                    value = "INVOKE",
                    //#if MC > 12004
                    //$$ target = "Lnet/minecraft/world/inventory/Slot;getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I"
                    //#else
                    target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"
                    //#endif
            )
    )
    private int insertItemGetMaxCount0(
            //#if MC > 12004
            //$$ Slot instance,
            //#endif
            ItemStack itemStack
    ) {
        return ShulkerBoxItemUtil.getMaxCount(itemStack);
    }

    @Redirect(
            //#if MC > 11502
            method = "doClick",
            //#else
            //$$ method = "clicked",
            //#endif
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I",
                    ordinal = -1
            )
    )
    private int removeStackGetMaxCount(ItemStack itemStack) {
        return ShulkerBoxItemUtil.getMaxCount(itemStack);
    }
}
