package com.plusls.carpet.mixin.rule.pcaSyncProtocol.entity;

import com.plusls.carpet.PluslsCarpetAdditionReference;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.impl.network.PcaSyncProtocol;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.EntityCompat;

@Mixin(AbstractVillager.class)
public abstract class MixinAbstractVillager extends AgableMob implements ContainerListener {
    @Final
    @Shadow
    private SimpleContainer inventory;

    protected MixinAbstractVillager(EntityType<? extends AgableMob> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void addInventoryListener(EntityType<? extends AbstractVillager> entityType, Level world, CallbackInfo info) {
        if (EntityCompat.of(this).getLevel().isClientSide()) {
            return;
        }

        this.inventory.addListener(this);
    }

    @Override
    @Intrinsic
    public void containerChanged(Container container) {
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference", "target"})
    @Inject(
            method = {"containerChanged(Lnet/minecraft/world/Container;)V", "method_5453"},
            at = @At("HEAD"),
            remap = false
    )
    public void postContainerChanged(Container inventory, CallbackInfo ci) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol && PcaSyncProtocol.syncEntityToClient(this)) {
            PluslsCarpetAdditionReference.getLogger().debug("update villager inventory: onInventoryChanged.");
        }
    }
}