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

package com.github.klikli_dev.occultism.common.world.cave;

import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SphericalCaveFeature extends Feature<NoFeatureConfig> {

    //region Fields
    protected ICaveDecorator caveDecorator;
    protected int radius;
    protected int maxRandomRadiusOffset;
    protected int additionalSpheres;
    protected int maxRandomAdditionalSpheres;
    //endregion Fields

    //region Initialization

    /**
     * @param caveDecorator         the decorator for the generated cave.
     * @param radius                the radius of the cave.
     * @param maxRandomRadiusOffset the maximum random offset for the radius
     */
    public SphericalCaveFeature(ICaveDecorator caveDecorator, int radius, int maxRandomRadiusOffset) {
        this(caveDecorator, radius, maxRandomRadiusOffset, 3, 2);
    }

    /**
     * @param caveDecorator              the decorator for the generated cave.
     * @param radius                     the radius of the cave.
     * @param maxRandomRadiusOffset      the maximum random offset for the radius
     * @param additionalSpheres          the amount of additional spheres to add on to the main sphere.
     * @param maxRandomAdditionalSpheres the maximum amount of random additional sphere to add to the main sphere.
     */
    public SphericalCaveFeature(ICaveDecorator caveDecorator, int radius, int maxRandomRadiusOffset,
                                int additionalSpheres, int maxRandomAdditionalSpheres) {
        super(dynamic -> IFeatureConfig.NO_FEATURE_CONFIG);
        this.caveDecorator = caveDecorator;
        this.radius = radius;
        this.maxRandomRadiusOffset = maxRandomRadiusOffset;
        this.additionalSpheres = additionalSpheres;
        this.maxRandomAdditionalSpheres = maxRandomAdditionalSpheres;
    }
    //endregion Initialization

    //region Overrides

    @Override
    public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand,
                         BlockPos pos, NoFeatureConfig config) {
        List<Sphere> spheres = new ArrayList<>();
        int radiusBase = this.radius + rand.nextInt(this.maxRandomRadiusOffset);
        int radius = (int) (radiusBase * 0.2F) + rand.nextInt(8);
        spheres.add(this.generateSphere(world, rand, pos, radius));
        for (int i = 0; i < this.additionalSpheres + rand.nextInt(this.maxRandomAdditionalSpheres); i++) {
            Direction direction = Direction.Plane.HORIZONTAL.random(rand);
            spheres.add(this.generateSphere(world, rand, pos.offset(direction, radius - 2),
                    2 * (int) (radius / 3F) + rand.nextInt(8)));
        }
        for (Sphere sphere : spheres) {
            this.hollowOutSphere(world, rand, sphere.center, sphere.radius - 2);
            this.decorateSphere(world, generator, rand, sphere.center, sphere.radius + 2);
        }
        spheres.clear();
        return true;
    }

    //endregion Overrides

    //region Methods
    protected Sphere generateSphere(IWorld world, Random rand, BlockPos position, int radius) {
        return new Sphere(position, radius);
    }

    protected void hollowOutSphere(IWorld world, Random rand, BlockPos center, int radius) {
        int j = radius;
        int k = radius / 2;
        int l = radius;
        float f = (float) (j + k + l) * 0.333F + 0.5F;
        BlockPos.getAllInBox(center.add(-j, -k, -l), center.add(j, k, l)).forEach(blockPos -> {
            if (blockPos.distanceSq(center) <= (double) (f * f * MathHelper.clamp(rand.nextFloat(), 0.75F, 1.0F))) {
                if (!(world.getBlockState(blockPos).hasTileEntity())) {
                    world.setBlockState(blockPos, Blocks.CAVE_AIR.getDefaultState(), 2);
                }
            }
        });
    }

    protected void decorateSphere(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand,
                                  BlockPos pos, int radius) {
        int j = radius;
        //int k = radius / 2;
        int k = radius / 2;
        int l = radius;
        CaveDecoratordata data = new CaveDecoratordata();
        float f = (float) (j + k + l) * 0.333F + 0.5F;
        BlockPos.getAllInBox(pos.add(-j, -k, -l), pos.add(j, k, l)).forEach(blockPos -> {
            if (blockPos.distanceSq(pos) <= (double) (f * f)) {
                this.caveDecorator.fill(world, generator, rand, blockPos, data);
            }
        });

        this.caveDecorator.finalPass(world, generator, rand, data);
    }
    //endregion Methods

    public class Sphere {
        //region Fields
        public BlockPos center;
        public int radius;
        //endregion Fields

        //region Initialization
        public Sphere(BlockPos center, int radius) {
            this.center = center;
            this.radius = radius;
        }
        //endregion Initialization
    }
}