package com.plusls.carpet.util.rule.powerfulBoneMeal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.hendrixshen.magiclib.api.compat.minecraft.world.level.state.BlockStateCompat;

public class Grow {
    static public boolean grow(ItemStack itemStack, Level world, BlockPos pos, Block block) {
        if (block instanceof SugarCaneBlock) {
            return growSugarCaneBlock(itemStack, world, pos);
        } else if (block instanceof ChorusFlowerBlock) {
            // TODO
            return false;
        } else {
            return false;
        }
    }

    static private boolean growSugarCaneBlock(ItemStack itemStack, Level level, BlockPos pos) {
        BlockPos downPos = pos.below();
        BlockPos upPos = pos.above();
        int height = 1;

        // 计算上层空气坐标
        while (!level.isEmptyBlock(upPos)) {
            if (BlockStateCompat.of(level.getBlockState(upPos)).is(Blocks.SUGAR_CANE)) {
                upPos = upPos.above();
                height++;
            } else {
                return false;
            }
        }

        // 计算底部坐标
        while (BlockStateCompat.of(level.getBlockState(downPos)).is(Blocks.SUGAR_CANE)) {
            downPos = downPos.below();
            height++;
        }

        // 甘蔗最多长 3 格
        if (height < 3) {
            BlockPos sugarCanePos = upPos.below();
            BlockState blockState = level.getBlockState(sugarCanePos);

            int age = blockState.getValue(SugarCaneBlock.AGE);
            if (age == 15) {
                level.setBlockAndUpdate(upPos, Blocks.SUGAR_CANE.defaultBlockState());
                level.setBlock(sugarCanePos, blockState.setValue(SugarCaneBlock.AGE, 0), 4);
            } else {
                age = Math.min(15, age + level.random.nextInt(16));
                level.setBlock(sugarCanePos, blockState.setValue(SugarCaneBlock.AGE, age), 4);
            }
            itemStack.shrink(1);
            return true;
        } else {
            return false;
        }
    }
}