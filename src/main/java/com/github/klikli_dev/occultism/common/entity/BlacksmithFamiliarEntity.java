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

import com.github.klikli_dev.occultism.Occultism;
import com.github.klikli_dev.occultism.common.advancement.FamiliarTrigger;
import com.github.klikli_dev.occultism.config.value.CachedInt;
import com.github.klikli_dev.occultism.registry.OccultismAdvancements;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import java.util.EnumSet;

public class BlacksmithFamiliarEntity extends FamiliarEntity {

    private static final CachedInt UPGRADE_COST = Occultism.SERVER_CONFIG.spiritJobs.blacksmithFamiliarUpgradeCost;

    private static int getMaxIron() {
        return UPGRADE_COST.get() * 10;
    }

    private static final DataParameter<Byte> BARS = EntityDataManager.defineId(BlacksmithFamiliarEntity.class,
            DataSerializers.BYTE);

    private int ironCount;

    public BlacksmithFamiliarEntity(EntityType<? extends BlacksmithFamiliarEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    public ILivingEntityData finalizeSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason,
                                           ILivingEntityData spawnDataIn, CompoundNBT dataTag) {
        this.setEarring(this.getRandom().nextDouble() < 0.1);
        this.setMarioMoustache(this.getRandom().nextDouble() < 0.5);
        this.setSquareHair(this.getRandom().nextDouble() < 0.5);
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(1, new SitGoal(this));
        this.goalSelector.addGoal(2, new UpgradeGoal(this));
        this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 8));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1, 3, 1));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new FollowMobGoal(this, 1, 3, 7));
    }

    @Override
    protected ActionResultType mobInteract(PlayerEntity playerIn, Hand hand) {
        ItemStack stack = playerIn.getItemInHand(hand);
        Item item = stack.getItem();
        if (playerIn == this.getFamiliarOwner() && this.ironCount < getMaxIron()
                && (item.is(Tags.Items.INGOTS_IRON) || item.is((Tags.Items.STORAGE_BLOCKS_IRON)))) {
            if (!this.level.isClientSide) {
                stack.shrink(1);
                this.changeIronCount(item.is(Tags.Items.INGOTS_IRON) ? 1 : 9);
            }
            return ActionResultType.sidedSuccess(!this.level.isClientSide);
        }
        return super.mobInteract(playerIn, hand);
    }

    @Override
    public void setFamiliarOwner(LivingEntity owner) {
        if (this.hasEarring())
            OccultismAdvancements.FAMILIAR.trigger(owner, FamiliarTrigger.Type.RARE_VARIANT);
        super.setFamiliarOwner(owner);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BARS, (byte) 0);
    }

    public boolean hasEarring() {
        return this.hasVariant(0);
    }

    public boolean hasMarioMoustache() {
        return this.hasVariant(1);
    }

    public boolean hasSquareHair() {
        return this.hasVariant(2);
    }

    private void setEarring(boolean b) {
        this.setVariant(0, b);
    }

    private void setMarioMoustache(boolean b) {
        this.setVariant(1, b);
    }

    private void setSquareHair(boolean b) {
        this.setVariant(2, b);
    }

    private void setIronCount(int count) {
        this.ironCount = count;
        this.entityData.set(BARS, (byte) Math.min(10, (this.ironCount / UPGRADE_COST.get())));
    }

    private void changeIronCount(int delta) {
        this.setIronCount(this.ironCount + delta);
    }

    public byte getBars() {
        return this.entityData.get(BARS);
    }

    @Override
    public Iterable<EffectInstance> getFamiliarEffects() {
        return ImmutableList.of();
    }

    @Override
    protected void dropEquipment() {
        int blockCount = this.ironCount / 9;
        int barCount = this.ironCount % 9;
        this.spawnAtLocation(new ItemStack(Items.IRON_INGOT, barCount));
        this.spawnAtLocation(new ItemStack(Items.IRON_BLOCK, blockCount));
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("ironCount", this.ironCount);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        if (!compound.contains("variants")) {
            this.setEarring(compound.getBoolean("hasEarring"));
            this.setMarioMoustache(compound.getBoolean("hasMarioMoustache"));
            this.setSquareHair(compound.getBoolean("hasSquareHair"));
        }
        this.setIronCount(compound.getInt("ironCount"));
    }

    private static class UpgradeGoal extends Goal {

        private static final CachedInt MAX_COOLDOWN = Occultism.SERVER_CONFIG.spiritJobs.blacksmithFamiliarUpgradeCooldown;

        private final BlacksmithFamiliarEntity blacksmith;
        private IFamiliar target;
        private int cooldown = MAX_COOLDOWN.get();

        public UpgradeGoal(BlacksmithFamiliarEntity blacksmith) {
            this.blacksmith = blacksmith;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            this.target = this.findTarget();
            return this.blacksmith.ironCount >= UPGRADE_COST.get() && this.target != null && this.cooldown-- < 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null;
        }

        public void start() {
            this.blacksmith.getNavigation().moveTo(this.target.getFamiliarEntity(), 0.7);
        }

        public void stop() {
            this.blacksmith.getNavigation().stop();
            this.cooldown = MAX_COOLDOWN.get();
            this.target = null;
        }

        @Override
        public void tick() {
            if (this.target == null)
                return;

            if (!this.blacksmith.isPathFinding())
                this.blacksmith.getNavigation().moveTo(this.target.getFamiliarEntity(), 0.7);

            if (this.blacksmith.distanceToSqr(this.target.getFamiliarEntity()) < 3) {
                if (this.target.canBlacksmithUpgrade()) {
                    this.target.blacksmithUpgrade();
                    this.blacksmith.changeIronCount(-UPGRADE_COST.get());
                    OccultismAdvancements.FAMILIAR.trigger(this.blacksmith.getFamiliarOwner(),
                            FamiliarTrigger.Type.BLACKSMITH_UPGRADE);
                }
                this.target = null;
            }
        }

        private IFamiliar findTarget() {
            for (LivingEntity e : this.blacksmith.level.getEntitiesOfClass(LivingEntity.class,
                    this.blacksmith.getBoundingBox().inflate(4), e -> this.familiarPred(e))) {
                return (IFamiliar) e;
            }
            return null;
        }

        private boolean familiarPred(Entity e) {
            if (!(e instanceof IFamiliar))
                return false;
            IFamiliar familiar = (IFamiliar) e;
            LivingEntity owner = familiar.getFamiliarOwner();
            return familiar.canBlacksmithUpgrade() && owner != null && owner == this.blacksmith.getFamiliarOwner();
        }
    }
}
