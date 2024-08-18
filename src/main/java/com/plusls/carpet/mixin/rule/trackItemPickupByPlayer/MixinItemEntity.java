package com.plusls.carpet.mixin.rule.trackItemPickupByPlayer;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.api.compat.minecraft.network.chat.ComponentCompat;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.EntityCompat;
import top.hendrixshen.magiclib.util.minecraft.MessageUtil;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {
    @Unique
    private boolean pca$pickup = false;
    @Unique
    private int pca$trackItemPickupByPlayerCooldown = 0;

    public MixinItemEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow
    public abstract void tick();

    @Shadow
    public abstract void setItem(ItemStack itemStack);

    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void prevTick(CallbackInfo ci) {
        if (!EntityCompat.of(this).getLevel().isClientSide() && PluslsCarpetAdditionSettings.trackItemPickupByPlayer && pca$pickup) {
            ci.cancel();
        }
    }

    @Inject(
            method = "playerTouch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void checkPickup(Player player, CallbackInfo ci) {
        if (!PluslsCarpetAdditionSettings.trackItemPickupByPlayer) {
            return;
        }

        EntityCompat entityCompat = EntityCompat.of(this);

        if (!entityCompat.getLevel().isClientSide()) {
            return;
        }

        if (pca$trackItemPickupByPlayerCooldown == 0) {
            MessageUtil.sendServerMessage(ComponentCompat.translatable("pca.message.pickup",
                    player.getName().getString(),
                    entityCompat.getX(), entityCompat.getY(), entityCompat.getZ(),
                    this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z()));
        }

        pca$trackItemPickupByPlayerCooldown = (pca$trackItemPickupByPlayerCooldown + 1) % 100;
        pca$pickup = true;
        this.setItem(new ItemStack(Items.BARRIER));
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setDeltaMovement(0, 0, 0);
        ci.cancel();
    }
}
