package com.plusls.carpet.mixin.rule.pcaSyncProtocol.block;

import com.plusls.carpet.PluslsCarpetAdditionReference;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.impl.network.PcaSyncProtocol;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC > 11605
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.world.level.block.state.BlockState;
//#endif

@Mixin(BrewingStandBlockEntity.class)
public abstract class MixinBrewingStandBlockEntity extends BaseContainerBlockEntity {
    private MixinBrewingStandBlockEntity(
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

    @Override
    @Intrinsic
    public void setChanged() {
        super.setChanged();
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference", "target"})
    @Inject(
            method = "setChanged()V",
            at = @At(
                    value = "RETURN"
            )
    )
    private void postSetChanged(CallbackInfo ci) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol && PcaSyncProtocol.syncBlockEntityToClient(this)) {
            PluslsCarpetAdditionReference.getLogger().debug("update BrewingStandBlockEntity: {}", this.worldPosition);
        }
    }
}