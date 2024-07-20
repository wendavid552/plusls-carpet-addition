package com.plusls.carpet.util.rule.gravestone;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import top.hendrixshen.magiclib.api.compat.minecraft.nbt.TagCompat;

//#if MC > 12004
//$$ import net.minecraft.core.HolderLookup;
//#endif

//#if MC > 11502
import net.minecraft.world.SimpleContainer;
//#else
//$$ import com.google.common.collect.Lists;
//$$ import net.minecraft.world.item.ItemStack;
//$$ import net.minecraft.nbt.ListTag;
//$$
//$$ import java.util.List;
//#endif

public class DeathInfo {
    public final long deathTime;
    public final int xp;
    //#if MC > 11502
    public final SimpleContainer inventory;
    //#else
    //$$ public final List<ItemStack> inventory;
    //#endif

    public DeathInfo(
            long deathTime,
            int xp,
            //#if MC > 11502
            SimpleContainer inv
            //#else
            //$$ Lists<ItemStack> inv
            //#endif
    ) {
        this.deathTime = deathTime;
        this.xp = xp;
        this.inventory = inv;
    }

    public static @NotNull DeathInfo fromTag(
            @NotNull CompoundTag tag
            //#if MC > 12004
            //$$ , HolderLookup.Provider provider
            //#endif
    ) {
        long deathTime = tag.getLong("DeathTime");
        int xp = tag.getInt("XP");
        //#if MC > 11502
        SimpleContainer inventory = new SimpleContainer(GravestoneUtil.PLAYER_INVENTORY_SIZE);
        inventory.fromTag(
                tag.getList("Items", TagCompat.TAG_COMPOUND)
                //#if MC > 12004
                //$$ , provider
                //#endif
        );
        //#else
        //$$ List<ItemStack> inventory = DeathInfo.readTagList(tag.getList("Items", TagCompat.TAG_COMPOUND));
        //#endif
        return new DeathInfo(deathTime, xp, inventory);
    }

    public CompoundTag toTag(
            //#if MC > 12004
            //$$ HolderLookup.Provider provider
            //#endif
    ) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("DeathTime", this.deathTime);
        tag.putInt("XP", this.xp);
        tag.put(
                "Items",
                //#if MC > 11502
                this.inventory.createTag(
                        //#if MC > 12004
                        //$$ provider
                        //#endif
                )
                //#else
                //$$ DeathInfo.toTagList(this.inventory)
                //#endif
        );
        return tag;
    }

    //#if MC < 11600
    //$$ private static List<ItemStack> readTagList(@NotNull ListTag listTag) {
    //$$     List<ItemStack> ret = Lists.newArrayList();
    //$$
    //$$     for (int i = 0; i < listTag.size(); i++) {
    //$$         ItemStack itemStack = ItemStack.of(listTag.getCompound(i));
    //$$
    //$$         if (!itemStack.isEmpty()) {
    //$$             ret.add(itemStack);
    //$$         }
    //$$     }
    //$$
    //$$     return ret;
    //$$ }
    //$$
    //$$ public static @NotNull ListTag toTagList(@NotNull List<ItemStack> inventory) {
    //$$     ListTag ret = new ListTag();
    //$$
    //$$     for (ItemStack itemStack : inventory) {
    //$$         if (!itemStack.isEmpty()) {
    //$$             listTag.add(itemStack.save(new CompoundTag()));
    //$$         }
    //$$     }
    //$$
    //$$     return ret;
    //$$ }
    //#endif
}