package com.pahimar.ee3.block;

import com.pahimar.ee3.block.base.BlockEnumEE;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class BlockAlchemicalFuel extends BlockEnumEE<BlockAlchemicalFuel.FuelType> {

    private static final String NAME = "alchemical_fuel_block";
    private static final PropertyEnum<FuelType> TYPE = PropertyEnum.create("type", FuelType.class);

    public BlockAlchemicalFuel() {
        super(NAME, FuelType.class);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TYPE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE, fromMeta(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TYPE).getMeta();
    }

    @Override
    protected PropertyEnum<FuelType> getPropertyEnum() {
        return TYPE;
    }

    public enum FuelType implements BlockEnumEE.IEnumMeta {
        ALCHEMICAL_COAL,
        MOBIUS_FUEL,
        AETERNALIS_FUEL;

        public final int meta;

        FuelType() {
            meta = ordinal();
        }

        @Override
        public int getMeta() {
            return meta;
        }
    }
}
