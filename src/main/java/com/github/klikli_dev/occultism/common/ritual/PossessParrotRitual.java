/*
 * MIT License
 *
 * Copyright 2020 klikli-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.klikli_dev.occultism.common.ritual;

import com.github.klikli_dev.occultism.common.tile.GoldenSacrificialBowlTileEntity;
import com.github.klikli_dev.occultism.registry.OccultismItems;
import com.github.klikli_dev.occultism.registry.OccultismRituals;
import com.github.klikli_dev.occultism.util.ItemNBTUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class PossessParrotRitual extends SummonSpiritRitual {
    //region Fields
    public static final ResourceLocation chickenTag = new ResourceLocation("forge", "chicken");
    //endregion Fields

    //region Initialization
    public PossessParrotRitual() {
        super(null,
                OccultismRituals.POSSESS_FOLIOT_PENTACLE.get(),
                Ingredient.fromItems(OccultismItems.BOOK_OF_BINDING_BOUND_FOLIOT.get()),
                "possess_parrot", 30);
        this.sacrificePredicate =
                (entity) -> EntityTypeTags.getCollection().getOrCreate(chickenTag).contains(entity.getType());
    }
    //endregion Initialization

    //region Overrides

    @Override
    public void finish(World world, BlockPos goldenBowlPosition, GoldenSacrificialBowlTileEntity tileEntity,
                       PlayerEntity castingPlayer, ItemStack activationItem) {
        super.finish(world, goldenBowlPosition, tileEntity, castingPlayer, activationItem);

        String entityName = ItemNBTUtil.getBoundSpiritName(activationItem);
        activationItem.shrink(1); //remove original activation item.

        ((ServerWorld) world).spawnParticle(ParticleTypes.LARGE_SMOKE, goldenBowlPosition.getX() + 0.5,
                goldenBowlPosition.getY() + 0.5, goldenBowlPosition.getZ() + 0.5, 1, 0, 0, 0, 0);

        //1/3 are a parrot, 2/3 are chickens.
        AnimalEntity parrot = world.rand.nextInt(3) == 0 ? EntityType.PARROT.create(world) : EntityType.CHICKEN.create(world);
        parrot.onInitialSpawn(world, world.getDifficultyForLocation(goldenBowlPosition), SpawnReason.MOB_SUMMONED,
                null,
                null);
        parrot
                .setPositionAndRotation(goldenBowlPosition.getX(), goldenBowlPosition.getY(), goldenBowlPosition.getZ(),
                        world.rand.nextInt(360), 0);
        parrot.setCustomName(new StringTextComponent(entityName));

        //notify players nearby and spawn
        this.spawnEntity(parrot, world);
    }
    //endregion Overrides
}