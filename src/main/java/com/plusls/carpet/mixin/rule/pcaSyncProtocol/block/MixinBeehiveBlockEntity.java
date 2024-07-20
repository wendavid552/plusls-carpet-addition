package com.plusls.carpet.mixin.rule.pcaSyncProtocol.block;

import com.plusls.carpet.PluslsCarpetAdditionReference;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.impl.network.PcaSyncProtocol;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

//#if MC > 12004
//$$ import net.minecraft.core.HolderLookup;
//#endif

//#if MC > 11605
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.world.level.Level;
//$$ import java.util.Objects;
//#endif

//#if MC > 11502
import net.minecraft.world.level.block.state.BlockState;
//#endif

@Mixin(BeehiveBlockEntity.class)
public abstract class MixinBeehiveBlockEntity extends BlockEntity {
    private MixinBeehiveBlockEntity(
            BlockEntityType<?> blockEntityType
            //#if MC > 11605
            //$$ , BlockPos blockPos
            //$$ , BlockState blockState
            //#endif
    ) {
        super(
                blockEntityType
                //#if MC > 11605
                //$$ , blockPos
                //$$ , blockState
                //#endif
        );
    }

    @Inject(
            method = "tickOccupants",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Iterator;remove()V",
                    shift = At.Shift.AFTER
            )
    )
    private
        //#if MC > 11605
        //$$ static
        //#endif
    void postTickOccupants(
            //#if MC > 11605
            //$$ Level level,
            //$$ BlockPos blockPos,
            //$$ BlockState blockState,
            //$$ List<BeehiveBlockEntity.BeeData> bees,
            //$$ BlockPos flowerPos,
            //#endif
            CallbackInfo ci
    ) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol && PcaSyncProtocol.syncBlockEntityToClient(
                //#if MC > 11605
                //$$ Objects.requireNonNull(level.getBlockEntity(blockPos))
                //#else
                this
                //#endif
        )) {
            PluslsCarpetAdditionReference.getLogger().debug(
                    "update BeehiveBlockEntity: {}",
                    //#if MC > 11605
                    //$$ blockPos
                    //#else
                    this.worldPosition
                    //#endif
            );
        }
    }

    @Inject(
            method = "releaseAllOccupants",
            at = @At(
                    value = "RETURN"
            )
    )
    public void postReleaseAllOccupants(CallbackInfoReturnable<List<Entity>> cir) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol && PcaSyncProtocol.syncBlockEntityToClient(this) && cir.getReturnValue() != null) {
            PluslsCarpetAdditionReference.getLogger().debug("update BeehiveBlockEntity: {}", this.worldPosition);
        }
    }

    @Inject(
            //#if MC > 12004
            //$$ method = "loadAdditional",
            //#else
            method = "load",
            //#endif
            at = @At(
                    value = "RETURN"
            )
    )
    public void postLoad(
            //#if MC > 11502 && MC < 11700
            BlockState blockState,
            //#endif
            @NotNull CompoundTag compoundTag,
            //#if MC > 12004
            //$$ HolderLookup.Provider provider,
            //#endif
            CallbackInfo ci
    ) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol && PcaSyncProtocol.syncBlockEntityToClient(this)) {
            PluslsCarpetAdditionReference.getLogger().debug("update BeehiveBlockEntity: {}", this.worldPosition);
        }
    }

    @Inject(
            //#if MC > 12004
            //$$ method = "addOccupant",
            //#else
            method = "addOccupantWithPresetTicks",
            //#endif

            at = @At(
                    value = "INVOKE",
                    //#if MC > 11605
                    //$$ target = "Lnet/minecraft/world/entity/Entity;discard()V",
                    //#else
                    target = "Lnet/minecraft/world/entity/Entity;remove()V",
                    //#endif
                    ordinal = 0
            )
    )
    public void postAddOccupantWithPresetTicks(CallbackInfo ci) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol && PcaSyncProtocol.syncBlockEntityToClient(this)) {
            PluslsCarpetAdditionReference.getLogger().debug("update BeehiveBlockEntity: {}", this.worldPosition);
        }
    }
}