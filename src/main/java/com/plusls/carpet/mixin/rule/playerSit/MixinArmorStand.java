package com.plusls.carpet.mixin.rule.playerSit;

import com.plusls.carpet.util.rule.playerSit.SitEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.api.compat.minecraft.nbt.TagCompat;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.EntityCompat;

@Mixin(ArmorStand.class)
public abstract class MixinArmorStand extends LivingEntity implements SitEntity {
    @Unique
    private boolean pca$sitEntity = false;

    protected MixinArmorStand(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Shadow
    protected abstract void setMarker(boolean marker);

    @Override
    public boolean pca$isSitEntity() {
        return this.pca$sitEntity;
    }

    @Override
    public void pca$setSitEntity(boolean isSitEntity) {
        this.pca$sitEntity = isSitEntity;
        this.setMarker(isSitEntity);
        this.setInvisible(isSitEntity);
    }

    @Override
    @Intrinsic
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference", "target"})
    @Inject(
            method = "removePassenger(Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD")
    )
    private void preRemovePassenger(Entity passenger, CallbackInfo ci) {
        if (this.pca$isSitEntity()) {
            EntityCompat entityCompat = EntityCompat.of(this);
            this.setPos(entityCompat.getX(), entityCompat.getY() + 0.16, entityCompat.getZ());
            this.kill();
        }
    }

    @Inject(
            method = "addAdditionalSaveData",
            at = @At("RETURN")
    )
    private void postAddAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
        if (this.pca$sitEntity) {
            nbt.putBoolean("SitEntity", true);
        }
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("RETURN")
    )
    private void postReadAdditionalSaveData(@NotNull CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("SitEntity", TagCompat.TAG_BYTE)) {
            this.pca$sitEntity = nbt.getBoolean("SitEntity");
        }
    }
}
