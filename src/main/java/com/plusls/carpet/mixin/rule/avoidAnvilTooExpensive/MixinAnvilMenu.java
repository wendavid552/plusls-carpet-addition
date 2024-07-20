package com.plusls.carpet.mixin.rule.avoidAnvilTooExpensive;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

//#if MC > 12004
//$$ import net.minecraft.core.component.DataComponents;
//#endif

//#if MC > 11502
import net.minecraft.world.inventory.ItemCombinerMenu;
//#else
//$$ import net.minecraft.world.Container;
//$$ import org.spongepowered.asm.mixin.Final;
//#endif

@Mixin(AnvilMenu.class)
public abstract class MixinAnvilMenu
        //#if MC > 11502
        extends ItemCombinerMenu
        //#endif
{
    private MixinAnvilMenu(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    @Shadow
    private String itemName;

    //#if MC < 11600
    //$$ @Shadow
    //$$ @Final
    //$$ private Container repairSlots;
    //#endif

    @ModifyVariable(
            method = "createResult",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/inventory/DataSlot;get()I",
                            ordinal = 1
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
                    ordinal = 0
            ),
            ordinal = 1
    )
    private ItemStack setItemStack(ItemStack itemStack) {
        //#if MC > 11502
        ItemStack itemStackA = this.inputSlots.getItem(0);
        ItemStack itemStackB = this.inputSlots.getItem(1);
        //#else
        //$$ ItemStack itemStackA = this.repairSlots.getItem(0);
        //$$ ItemStack itemStackB = this.repairSlots.getItem(1);
        //#endif

        if (PluslsCarpetAdditionSettings.avoidAnvilTooExpensive && itemStack.isEmpty() && !itemStackA.isEmpty() &&
                (!itemStackB.isEmpty() ||
                        (StringUtils.isBlank(this.itemName) &&
                                //#if MC > 12004
                                //$$ itemStackA.has(DataComponents.CUSTOM_NAME)
                                //#else
                                itemStackA.hasCustomHoverName()
                                //#endif
                        ) ||
                        (!StringUtils.isBlank(this.itemName) && !this.itemName.equals(itemStackA.getHoverName().getString())))
        ) {
            return itemStackA.copy();
        } else {
            return itemStack;
        }
    }
}
