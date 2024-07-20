package com.plusls.carpet.mixin.rule.renewableNetherite;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.ItemUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.EntityCompat;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {
    private MixinItemEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow
    public abstract ItemStack getItem();

    @Inject(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    //#if MC > 11605
                    //$$ target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V"
                    //#else
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;remove()V"
                    //#endif
            )
    )
    private void checkDiamondEquip(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        EntityCompat entityCompat = EntityCompat.of(this);

        if (!PluslsCarpetAdditionSettings.renewableNetheriteEquip || entityCompat.getLevel().get().isClientSide) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) entityCompat.getLevel().get();

        if (
                //#if MC > 11903
                //$$ source != serverLevel.damageSources().lava()
                //#else
                source != DamageSource.LAVA
                //#endif
                        && serverLevel.dimension() != Level.NETHER
        ) {
            return;
        }

        ItemStack stack = this.getItem();

        if (stack.isEmpty() || stack.getMaxDamage() - stack.getDamageValue() != 1) {
            return;
        }

        Item item = stack.getItem();

        if ((item instanceof ArmorItem && ((ArmorItem) item).getMaterial() == ArmorMaterials.DIAMOND) ||
                item instanceof TieredItem && ((TieredItem) item).getTier() == Tiers.DIAMOND) {
            ItemStack newItemStack = ItemUtil.upGradeToNetheriteLike(stack);

            if (newItemStack != null) {
                serverLevel.addFreshEntity(new ItemEntity(serverLevel, this.getX(), this.getY(), this.getZ(), newItemStack));
            }
        }
    }
}
