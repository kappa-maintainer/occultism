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

package com.github.klikli_dev.occultism.common.block.storage;

import com.github.klikli_dev.occultism.common.tile.StorageControllerTileEntity;
import com.github.klikli_dev.occultism.registry.OccultismTiles;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class StorageControllerBlock extends Block {
    //region Initialization
    public StorageControllerBlock(Properties properties) {
        super(properties);
    }
    //endregion Initialization

    //region Overrides

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof StorageControllerTileEntity) {
                //We're not dropping anything here, should be handled correctly by loot table.
                worldIn.updateComparatorOutputLevel(pos, this);
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                             Hand handIn, BlockRayTraceResult p_225533_6_) {
        if (!world.isRemote) {
            //TODO: enable once gui is available
            TileEntity tileEntity = world.getTileEntity(pos);
            //            if (!(tileEntity instanceof TileEntityStorageController)) {
            //                return ActionResultType.FAIL;
            //            }
            //            player.openGui(Occultism.instance, GuiHandler.GuiID.STORAGE_CONTROLLER.ordinal(), world, pos.getX(),
            //                    pos.getY(), pos.getZ());
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        ItemStack itemstack = super.getItem(worldIn, pos, state);

        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof StorageControllerTileEntity) {
            CompoundNBT compound = tileentity.serializeNBT();
            if (!compound.isEmpty()) {
                itemstack.setTagInfo("BlockEntityTag", compound);
            }
        }
        return itemstack;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return OccultismTiles.STORAGE_CONTROLLER.get().create();
    }

    //endregion Overrides
}
