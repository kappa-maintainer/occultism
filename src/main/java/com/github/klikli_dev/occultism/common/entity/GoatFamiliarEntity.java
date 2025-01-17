/*
 * MIT License
 *
 * Copyright 2021 vemerion
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

package com.github.klikli_dev.occultism.common.entity;

import com.github.klikli_dev.occultism.common.advancement.FamiliarTrigger;
import com.github.klikli_dev.occultism.registry.OccultismAdvancements;
import com.github.klikli_dev.occultism.registry.OccultismEntities;
import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.FollowMobGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.Category;
import net.minecraftforge.common.Tags;

public class GoatFamiliarEntity extends ResizableFamiliarEntity {

    private int shakeHeadTimer;

    public GoatFamiliarEntity(EntityType<? extends GoatFamiliarEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public GoatFamiliarEntity(World worldIn, boolean hasRing, boolean hasBeard, byte size, LivingEntity owner) {
        this(OccultismEntities.GOAT_FAMILIAR.get(), worldIn);
        this.setRing(hasRing);
        this.setBeard(hasBeard);
        this.setSize(size);
        this.setFamiliarOwner(owner);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new SitGoal(this));
        this.goalSelector.addGoal(2, new LookAtGoal(this, PlayerEntity.class, 8));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1, 3, 1));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new FollowMobGoal(this, 1, 3, 7));
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (super.hurt(pSource, pAmount)) {
            if (pSource.getEntity() != null) {
                ringBell(this);
            }
            return true;
        }
        return false;
    }

    public static void ringBell(FamiliarEntity entity) {
        LivingEntity owner = entity.getFamiliarOwner();
        if (owner == null || !entity.hasBlacksmithUpgrade())
            return;

        entity.playSound(SoundEvents.BELL_BLOCK, 1, 1);

        for (MobEntity e : entity.level.getEntitiesOfClass(MobEntity.class, entity.getBoundingBox().inflate(30),
                e -> e.isAlive() && e.getClassification(false) == EntityClassification.MONSTER))
            e.setTarget(owner);
    }

    @Override
    public Iterable<EffectInstance> getFamiliarEffects() {
        return ImmutableList.of();
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        if (!compound.contains("variants"))
            this.setRing(compound.getBoolean("hasRing"));
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide)
            this.shakeHeadTimer--;
    }

    @Override
    public boolean canBlacksmithUpgrade() {
        return !this.hasBlacksmithUpgrade();
    }

    public boolean hasRing() {
        return this.hasVariant(0);
    }

    private void setRing(boolean b) {
        this.setVariant(0, b);
    }

    public boolean hasBeard() {
        return this.hasVariant(1);
    }

    private void setBeard(boolean b) {
        this.setVariant(1, b);
    }

    public boolean isBlack() {
        return this.hasVariant(2);
    }

    private void setBlack(boolean b) {
        this.setVariant(2, b);
    }

    public boolean hasRedEyes() {
        return this.hasVariant(3);
    }

    private void setRedEyes(boolean b) {
        this.setVariant(3, b);
    }

    public boolean hasEvilHorns() {
        return this.hasVariant(4);
    }

    private void setEvilHorns(boolean b) {
        this.setVariant(4, b);
    }

    @Override
    protected ActionResultType mobInteract(PlayerEntity playerIn, Hand hand) {
        ItemStack stack = playerIn.getItemInHand(hand);
        Item item = stack.getItem();
        boolean isInForest = isInForest(playerIn) || isInForest(this);
        if (isTransformItem(item) && playerIn == getFamiliarOwner()) {
            if (isInForest) {
                if (!level.isClientSide)
                    stack.shrink(1);
                if (item.is(Tags.Items.DYES_BLACK))
                    this.setBlack(true);
                else if (item == Items.ENDER_EYE)
                    this.setRedEyes(true);
                else if (item == Items.FLINT)
                    this.setEvilHorns(true);
                if (shouldTransform()) {
                    OccultismAdvancements.FAMILIAR.trigger(playerIn, FamiliarTrigger.Type.SHUB_NIGGURATH_SUMMON);
                    transform();
                }
                return ActionResultType.sidedSuccess(level.isClientSide);
            } else {
                shakeHeadTimer = 20;
                return ActionResultType.CONSUME;
            }
        }
        return super.mobInteract(playerIn, hand);
    }

    private void transform() {
        if (level.isClientSide) {
            float scale = getScale();
            for (int i = 0; i < 30; i++)
                level.addParticle(ParticleTypes.SMOKE, getRandomX(scale), getRandomY() * scale, getRandomZ(scale), 0, 0,
                        0);
        } else {
            ShubNiggurathFamiliarEntity shubNiggurath = new ShubNiggurathFamiliarEntity(level, this);
            level.addFreshEntity(shubNiggurath);
            this.remove();
        }
    }

    boolean isTransformItem(Item item) {
        return (item.is(Tags.Items.DYES_BLACK) && !this.isBlack()) || (item == Items.FLINT && !this.hasEvilHorns())
                || (item == Items.ENDER_EYE && !this.hasRedEyes());
    }

    boolean shouldTransform() {
        return this.isBlack() && this.hasRedEyes() && this.hasEvilHorns();
    }

    private boolean isInForest(Entity entity) {
        return this.level.getBiome(entity.blockPosition()).getBiomeCategory() == Category.FOREST;
    }

    public float getNeckYRot(float pPartialTick) {
        if (shakeHeadTimer <= 0)
            return 0;
        return MathHelper.sin((shakeHeadTimer - pPartialTick) / 20 * (float) Math.PI * 5) * (float) Math.toRadians(30);
    }
}
