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

package com.github.klikli_dev.occultism.common.block.otherworld;

import com.github.klikli_dev.occultism.Occultism;
import com.github.klikli_dev.occultism.registry.OccultismEffects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.util.Random;

public interface IOtherworldBlock {
    //region Fields
    IProperty<Boolean> UNCOVERED = BooleanProperty.create("uncovered");
    //endregion Fields

    //region Getter / Setter
    Block getUncoveredBlock();

    Block getCoveredBlock();
    //endregion Getter / Setter

    //region Methods

    default BlockState getHarvestState(PlayerEntity player, BlockState state) {
        return player.isPotionActive(OccultismEffects.THIRD_EYE.get()) ? state.with(UNCOVERED, true) : state;
    }

    default ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(state.get(UNCOVERED) ? this.getUncoveredBlock() :this.getCoveredBlock(), 1);
    }

    //endregion Methods
}