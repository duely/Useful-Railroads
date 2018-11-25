package info.u_team.useful_railroads.block;

import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockCustomRailPowered extends BlockCustomRail {
	
	public static final PropertyBool POWERED = PropertyBool.create("powered");
	
	public BlockCustomRailPowered(String name) {
		super(name, true);
	}
	
	@Override
	public void onMinecartPass(World world, EntityMinecart cart, BlockPos pos) {
		if (!world.getBlockState(pos).getValue(POWERED)) {
			cart.motionX = 0;
			cart.motionY = 0;
			cart.motionZ = 0;
			return;
		}
		onMinecartPassPowered(world, cart, pos);
	}
	
	public abstract void onMinecartPassPowered(World world, EntityMinecart cart, BlockPos pos);
	
	@Override
	public int getMetaFromState(IBlockState state) {
		int shape = state.getValue(SHAPE).getMetadata();
		boolean powered = state.getValue(POWERED);
		int meta = shape;
		meta |= (powered ? 1 : 0) << 1;
		return meta;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		int shape = meta & 1;
		boolean powered = ((meta >> 1) & 1) == 1;
		return blockState.getBaseState().withProperty(SHAPE, EnumRailDirection.byMetadata(shape)).withProperty(POWERED, powered);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, SHAPE, POWERED);
	}
	
	// Just minecraft rail logic for power
	
	protected boolean findPoweredRailSignal(World world, BlockPos pos, IBlockState state, boolean positive_axis, int distance) {
		if (distance >= 8) {
			return false;
		} else {
			int i = pos.getX();
			int j = pos.getY();
			int k = pos.getZ();
			boolean flag = true;
			
			Block block = state.getBlock();
			
			EnumRailDirection direction;
			if (block instanceof BlockRailPowered) {
				direction = state.getValue(BlockRailPowered.SHAPE);
			} else {
				direction = state.getValue(SHAPE);
			}
			
			switch (direction) {
			case NORTH_SOUTH:
				
				if (positive_axis) {
					++k;
				} else {
					--k;
				}
				
				break;
			case EAST_WEST:
				
				if (positive_axis) {
					--i;
				} else {
					++i;
				}
				
				break;
			case ASCENDING_EAST:
				
				if (positive_axis) {
					--i;
				} else {
					++i;
					++j;
					flag = false;
				}
				
				direction = EnumRailDirection.EAST_WEST;
				break;
			case ASCENDING_WEST:
				
				if (positive_axis) {
					--i;
					++j;
					flag = false;
				} else {
					++i;
				}
				
				direction = EnumRailDirection.EAST_WEST;
				break;
			case ASCENDING_NORTH:
				
				if (positive_axis) {
					++k;
				} else {
					--k;
					++j;
					flag = false;
				}
				
				direction = EnumRailDirection.NORTH_SOUTH;
				break;
			case ASCENDING_SOUTH:
				
				if (positive_axis) {
					++k;
					++j;
					flag = false;
				} else {
					--k;
				}
				
				direction = EnumRailDirection.NORTH_SOUTH;
			default:
				break;
			}
			
			if (this.isSameRailWithPower(world, new BlockPos(i, j, k), positive_axis, distance, direction)) {
				return true;
			} else {
				return flag && this.isSameRailWithPower(world, new BlockPos(i, j - 1, k), positive_axis, distance, direction);
			}
		}
	}
	
	protected boolean isSameRailWithPower(World world, BlockPos pos, boolean p_176567_3_, int distance, EnumRailDirection direction) {
		IBlockState iblockstate = world.getBlockState(pos);
		Block block = iblockstate.getBlock();
		
		if (block instanceof BlockCustomRailPowered || block instanceof BlockRailPowered) {
			
			EnumRailDirection directionOther;
			if (block instanceof BlockRailPowered) {
				directionOther = iblockstate.getValue(BlockRailPowered.SHAPE);
			} else {
				directionOther = iblockstate.getValue(SHAPE);
			}
			
			if (direction != EnumRailDirection.EAST_WEST || directionOther != EnumRailDirection.NORTH_SOUTH && directionOther != EnumRailDirection.ASCENDING_NORTH && directionOther != EnumRailDirection.ASCENDING_SOUTH) {
				if (direction != EnumRailDirection.NORTH_SOUTH || directionOther != EnumRailDirection.EAST_WEST && directionOther != EnumRailDirection.ASCENDING_EAST && directionOther != EnumRailDirection.ASCENDING_WEST) {
					
					boolean powered;
					if (block instanceof BlockRailPowered) {
						powered = iblockstate.getValue(BlockRailPowered.POWERED);
					} else {
						powered = iblockstate.getValue(POWERED);
					}
					
					if (powered) {
						return world.isBlockPowered(pos) ? true : findPoweredRailSignal(world, pos, iblockstate, p_176567_3_, distance + 1);
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		return false;
	}
	
	protected void updateState(IBlockState state, World world, BlockPos pos, Block block) {
		
		boolean powered;
		if (block instanceof BlockRailPowered) {
			powered = state.getValue(BlockRailPowered.POWERED);
		} else {
			powered = state.getValue(BlockCustomRailPowered.POWERED);
		}
		
		boolean flag = powered;
		boolean flag1 = world.isBlockPowered(pos) || findPoweredRailSignal(world, pos, state, true, 0) || findPoweredRailSignal(world, pos, state, false, 0);
		
		if (flag1 != flag) {
			if (block instanceof BlockRailPowered) {
				world.setBlockState(pos, state.withProperty(BlockRailPowered.POWERED, flag1), 3);
			} else {
				world.setBlockState(pos, state.withProperty(BlockCustomRailPowered.POWERED, flag1), 3);
			}
			world.notifyNeighborsOfStateChange(pos.down(), this, false);
			
			if (block instanceof BlockRailPowered) {
				if (state.getValue(BlockRailPowered.SHAPE).isAscending()) {
					world.notifyNeighborsOfStateChange(pos.up(), block, false);
				}
			} else {
				if (state.getValue(BlockCustomRailPowered.SHAPE).isAscending()) {
					world.notifyNeighborsOfStateChange(pos.up(), block, false);
				}
			}
		}
	}
}
