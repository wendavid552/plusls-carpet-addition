package com.plusls.carpet.mixin.rule.useDyeOnShulkerBox;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.player.PlayerCompat;

//#if MC > 12004
//$$ import com.plusls.carpet.mixin.accessor.AccessorBaseContainerBlockEntity;
//$$ import net.minecraft.core.component.DataComponents;
//#endif

//#if MC < 11900
import org.spongepowered.asm.mixin.Intrinsic;
//#endif

//#if MC < 11800
import net.minecraft.nbt.CompoundTag;
//#endif

@Mixin(PotionItem.class)
public abstract class MixinPotionItem extends Item {
    public MixinPotionItem(Properties settings) {
        super(settings);
    }

    //#if MC < 11900
    @Override
    @Intrinsic
    public @NotNull InteractionResult useOn(UseOnContext useOnContext) {
        return super.useOn(useOnContext);
    }
    //#endif

    //#if MC < 11900
    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    //#endif
    @Inject(
            //#if MC > 11802
            //$$ method = "useOn",
            //#else
            method = {"useOn" ,"method_7884"},
            remap = false,
            //#endif
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void preUseOn(@NotNull UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = useOnContext.getItemInHand();
        Player player = useOnContext.getPlayer();

        if (!PluslsCarpetAdditionSettings.useDyeOnShulkerBox ||
                player == null ||
                itemStack.getItem() != Items.POTION ||
                //#if MC > 12004
                //$$ !itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)
                //#else
                PotionUtils.getPotion(itemStack) != Potions.WATER
            //#endif
        ) {
            return;
        }

        Level level = useOnContext.getLevel();
        BlockPos pos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();

        if (block instanceof ShulkerBoxBlock &&
                ((ShulkerBoxBlock) block).getColor() != null) {
            if (!level.isClientSide()) {
                ShulkerBoxBlockEntity blockEntity = (ShulkerBoxBlockEntity) level.getBlockEntity(pos);
                BlockState newBlockState = Blocks.SHULKER_BOX.defaultBlockState().
                        setValue(ShulkerBoxBlock.FACING, blockState.getValue(ShulkerBoxBlock.FACING));

                if (level.setBlockAndUpdate(pos, newBlockState)) {
                    ShulkerBoxBlockEntity newBlockEntity = (ShulkerBoxBlockEntity) level.getBlockEntity(pos);
                    assert blockEntity != null;
                    assert newBlockEntity != null;
                    newBlockEntity.loadFromTag(
                            //#if MC > 11701
                            //$$ blockEntity.saveWithoutMetadata(
                            //#if MC > 12004
                            //$$         level.registryAccess()
                            //#endif
                            //$$ )
                            //#else
                            new CompoundTag()
                            //#endif
                            //#if MC > 12004
                            //$$ , level.registryAccess()
                            //#endif
                    );
                    //#if MC > 12004
                    //$$ ((AccessorBaseContainerBlockEntity) newBlockEntity).pca$setName(blockEntity.getCustomName());
                    //#else
                    newBlockEntity.setCustomName(blockEntity.getCustomName());
                    //#endif
                    newBlockEntity.setChanged();

                    if (!player.isCreative()) {
                        useOnContext.getItemInHand().shrink(1);
                        PlayerCompat playerCompat = PlayerCompat.of(useOnContext.getPlayer());
                        playerCompat.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
                    }
                }
            }

            cir.setReturnValue(
                    //#if MC > 11802
                    //$$ InteractionResult.sidedSuccess(level.isClientSide)
                    //#else
                    level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS
                    //#endif
            );
        }
    }
}
