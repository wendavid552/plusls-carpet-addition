package com.plusls.carpet.util.rule.gravestone;

import com.plusls.carpet.PluslsCarpetAdditionReference;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.player.PlayerCompat;
import top.hendrixshen.magiclib.api.compat.minecraft.world.item.ItemStackCompat;
import top.hendrixshen.magiclib.api.compat.minecraft.world.level.LevelCompat;
import top.hendrixshen.magiclib.impl.compat.minecraft.world.level.dimension.DimensionWrapper;

import java.util.Objects;

//#if MC > 12004
//$$ import net.minecraft.world.item.component.ResolvableProfile;
//#endif

//#if MC > 11502
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
//#else
//$$ import com.google.common.collect.Lists;
//$$ import net.minecraft.world.level.dimension.DimensionType;
//$$
//$$ import java.util.List;
//#endif

public class GravestoneUtil {
    public static final int NETHER_BEDROCK_MAX_Y = 127;
    public static final int SEARCH_RANGE = 5;
    public static final int PLAYER_INVENTORY_SIZE = 41;

    //#if MC > 11502
    public static void init() {
        ServerPlayerEvents.ALLOW_DEATH.register((player, damageSource, damageAmount) -> {
            GravestoneUtil.deathHandle(player);
            return true;
        });
    }
    //#endif

    public static void deathHandle(@NotNull ServerPlayer player) {
        PlayerCompat playerCompat = PlayerCompat.of(player);
        LevelCompat levelCompat = playerCompat.getLevel();
        Level level = levelCompat.get();

        if (PluslsCarpetAdditionSettings.gravestone && !level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack itemStack = player.getItemInHand(hand);

                if (ItemStackCompat.of(itemStack).is(Items.TOTEM_OF_UNDYING)) {
                    return;
                }
            }

            player.destroyVanishingCursedItems();
            //#if MC > 11502
            SimpleContainer inventory = new SimpleContainer(PLAYER_INVENTORY_SIZE);

            for (ItemStack itemStack : playerCompat.getInventory().items) {
                inventory.addItem(itemStack);
            }

            for (ItemStack itemStack : playerCompat.getInventory().armor) {
                inventory.addItem(itemStack);
            }

            for (ItemStack itemStack : playerCompat.getInventory().offhand) {
                inventory.addItem(itemStack);
            }
            //#else
            //$$ List<ItemStack> inventory = Lists.newArrayList();
            //$$ inventory.addAll(player.inventory.items);
            //$$ inventory.addAll(player.inventory.armor);
            //$$ inventory.addAll(player.inventory.offhand);
            //#endif
            int xp = player.totalExperience / 2;
            playerCompat.getInventory().clearContent();

            // only need clear experienceLevel
            player.experienceLevel = 0;
            BlockPos gravePos = findGravePos(player);
            Objects.requireNonNull(level.getServer()).tell(new TickTask(level.getServer().getTickCount(),
                    placeGraveRunnable(level,
                            gravePos,
                            new DeathInfo(System.currentTimeMillis(), xp, inventory),
                            player)));
        }
    }

    // find pos to place gravestone
    public static BlockPos findGravePos(@NotNull ServerPlayer player) {
        PlayerCompat playerCompat = PlayerCompat.of(player);

        //#if MC > 11502
        BlockPos.MutableBlockPos playerPos = playerCompat.getBlockPosition().mutable();
        //#else
        //$$ BlockPos.MutableBlockPos playerPos = new BlockPos.MutableBlockPos(playerCompat.getBlockPosition());
        //#endif
        playerPos.setY(GravestoneUtil.clampY(player, playerPos.getY()));

        if (GravestoneUtil.canPlaceGrave(player, playerPos)) {
            return playerPos;
        }

        BlockPos.MutableBlockPos gravePos = new BlockPos.MutableBlockPos();

        for (int x = playerPos.getX() + SEARCH_RANGE; x >= playerPos.getX() - SEARCH_RANGE; x--) {
            gravePos.setX(x);
            int minY = clampY(player, playerPos.getY() - SEARCH_RANGE);

            for (int y = clampY(player, playerPos.getY() + SEARCH_RANGE); y >= minY; y--) {
                gravePos.setY(y);

                for (int z = playerPos.getZ() + SEARCH_RANGE; z >= playerPos.getZ() - SEARCH_RANGE; z--) {
                    gravePos.setZ(z);

                    if (canPlaceGrave(player, gravePos)) {
                        return drop(player, gravePos);
                    }
                }
            }
        }

        // search up
        gravePos.set(playerPos);

        while (playerCompat.getLevel().get().getBlockState(gravePos).getBlock() == Blocks.BEDROCK) {
            gravePos.setY(gravePos.getY() + 1);
        }

        return gravePos;
    }

    // make sure to spawn graves on the suitable place
    public static int clampY(@NotNull ServerPlayer player, int y) {
        //don't spawn on nether ceiling, unless the player is already there.
        PlayerCompat playerCompat = PlayerCompat.of(player);
        LevelCompat levelCompat = playerCompat.getLevel();

        if (DimensionWrapper.of(levelCompat.get()).equals(DimensionWrapper.NETHER) &&
                y < NETHER_BEDROCK_MAX_Y) {
            //clamp to 1 -- don't spawn graves the layer right above the void, so players can actually recover their items.
            return Mth.clamp(y, levelCompat.getMinBuildHeight() + 1, NETHER_BEDROCK_MAX_Y - 1);
        } else {
            return Mth.clamp(y, levelCompat.getMinBuildHeight() + 1, levelCompat.get().getMaxBuildHeight() - 1);
        }
    }

    public static boolean canPlaceGrave(@NotNull ServerPlayer player, BlockPos pos) {
        LevelCompat levelCompat = PlayerCompat.of(player).getLevel();
        BlockState state = levelCompat.get().getBlockState(pos);

        if (pos.getY() <= levelCompat.getMinBuildHeight() + 1 ||
                pos.getY() >= levelCompat.get().getMaxBuildHeight() - 1) {
            return false;
        } else if (state.isAir()) {
            return true;
        } else { // block can replace
            return state.canBeReplaced(new DirectionalPlaceContext(levelCompat.get(), pos,
                    Direction.DOWN, ItemStack.EMPTY, Direction.UP));
        }
    }

    // players are blown up
    // reduce y pos
    public static BlockPos drop(@NotNull ServerPlayer player, BlockPos pos) {
        LevelCompat levelCompat = PlayerCompat.of(player).getLevel();
        BlockPos.MutableBlockPos searchPos = new BlockPos.MutableBlockPos().set(pos);
        int i = 0;

        for (int y = pos.getY() - 1; y > levelCompat.getMinBuildHeight() + 1 && i < 10; y--) {
            i++;
            searchPos.setY(clampY(player, y));

            if (!levelCompat.get().getBlockState(searchPos).isAir()) {
                searchPos.setY(clampY(player, y + 1));
                return searchPos;
            }
        }

        return pos;
    }

    @Contract(pure = true)
    public static @NotNull Runnable placeGraveRunnable(Level world, BlockPos pos, DeathInfo deathInfo, ServerPlayer player) {
        return () -> {
            BlockState graveBlock = Blocks.PLAYER_HEAD.defaultBlockState();

            // avoid setblockstate fail.
            while (!world.setBlockAndUpdate(pos, graveBlock)) {
                PluslsCarpetAdditionReference.getLogger().warn(String.format("set gravestone at %d %d %d fail, try again.",
                        pos.getX(), pos.getY(), pos.getZ()));
            }
            SkullBlockEntity graveEntity = (SkullBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos));
            graveEntity.setOwner(
                    //#if MC > 12004
                    //$$ new ResolvableProfile(player.getGameProfile())
                    //#else
                    player.getGameProfile()
                    //#endif
            );
            ((GravesStoneSkullBlockEntity) graveEntity).pca$setDeathInfo(deathInfo);
            graveEntity.setChanged();
        };
    }
}