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

package com.github.klikli_dev.occultism.client.render;

import com.github.klikli_dev.occultism.util.RenderUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SelectedBlockRenderer {

    //region Fields
    protected Set<SelectionInfo> selectedBlocks = new HashSet<>();
    //endregion Fields

    //region Methods

    /**
     * Highlights the given block position until the given system time (not game time)
     *
     * @param pos        the position to highlight.
     * @param expireTime the time when it expires. Absolute system time, not interval!
     */
    public void selectBlock(BlockPos pos, long expireTime) {
        this.selectBlock(pos, expireTime, new Color(1.0f, 1.0f, 1.0f, 0.8f));
    }

    /**
     * Highlights the given block position until the given system time (not game time)
     *
     * @param pos        the position to highlight.
     * @param expireTime the time when it expires. Absolute system time, not interval!
     * @param color      the color to render the block in.
     */
    public void selectBlock(BlockPos pos, long expireTime, Color color) {
        SelectionInfo info = new SelectionInfo(pos, expireTime, color);
        this.selectedBlocks.remove(info);
        this.selectedBlocks.add(info);
    }

    /**
     * Unselects the given block position.
     *
     * @param pos the position.
     */
    public void unselectBlock(BlockPos pos) {
        this.selectedBlocks.removeIf(info -> info.selectedBlock.equals(pos));
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        this.renderSelectedBlocks(event);
    }

    protected void renderSelectedBlocks(RenderWorldLastEvent event) {
        if (!this.selectedBlocks.isEmpty()) {
            long time = System.currentTimeMillis();

            MatrixStack matrixStack = event.getMatrixStack();
            IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            IVertexBuilder builder = buffer.getBuffer(OccultismRenderType.BLOCK_SELECTION);
            matrixStack.pushPose();
            Vector3d projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
            Matrix4f transform = matrixStack.last().pose();

            for (Iterator<SelectionInfo> it = this.selectedBlocks.iterator(); it.hasNext(); ) {
                SelectionInfo info = it.next();

                if (time > info.selectionExpireTime || info.selectedBlock == null) {
                    //remove expired or invalid selections
                    it.remove();
                    return;
                } else {
                    RenderUtil
                            .buildBlockOutline(builder, transform, info.selectedBlock.getX(), info.selectedBlock.getY(),
                                    info.selectedBlock.getZ(), info.color.getRed() / 255.0f,
                                    info.color.getGreen() / 255.0f, info.color.getBlue() / 255.0f,
                                    info.color.getAlpha() / 255.0f);
                }
            }

            matrixStack.popPose();
            RenderSystem.enableTexture();
            RenderSystem.disableDepthTest();
            buffer.endBatch(OccultismRenderType.BLOCK_SELECTION);
            RenderSystem.enableDepthTest();
        }
    }
    //endregion Methods

    public class SelectionInfo {
        //region Fields
        public BlockPos selectedBlock;
        public long selectionExpireTime;
        public Color color;
        //endregion Fields

        //region Initialization
        public SelectionInfo(BlockPos selectedBlock, long selectionExpireTime, Color color) {
            this.selectedBlock = selectedBlock;
            this.selectionExpireTime = selectionExpireTime;
            this.color = color;
        }
        //endregion Initialization

        //region Overrides
        @Override
        public int hashCode() {
            return this.selectedBlock.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            SelectionInfo other = (SelectionInfo) obj;
            if (other == null)
                return false;

            return other.selectedBlock.equals(this.selectedBlock);
        }
        //endregion Overrides
    }
}
