package powercrystals.minefactoryreloaded.processing;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;
import powercrystals.core.util.Util;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.MineFactoryReloadedCore;
import powercrystals.minefactoryreloaded.core.MFRUtil;
import powercrystals.minefactoryreloaded.core.TileEntityFactoryInventory;

public class TileEntityBioReactor extends TileEntityFactoryInventory implements ITankContainer, ISidedInventory
{
	private LiquidTank _tank;
	private int _burnTime;
	private static final int _burnTimeMax = 8000;
	private static final int _bioFuelPerTick = 10;
	private static final int _burnTimeDecreasePerTick = 10;
	
	// start at 80, +20 for each slot after the first
	private static final int[] _outputValues = { 0, 80, 180, 300, 440, 600, 780, 980, 1200, 1440 };
	
	public TileEntityBioReactor()
	{
		_tank = new LiquidTank(LiquidContainerRegistry.BUCKET_VOLUME * 4);
	}
	
	public int getBurnTime()
	{
		return _burnTime;
	}
	
	public void setBurnTime(int burnTime)
	{
		_burnTime = burnTime;
	}
	
	public int getBurnTimeMax()
	{
		return _burnTimeMax;
	}
	
	public int getOutputValue()
	{
		int occupiedSlots = 0;
		for(int i = 9; i < 18; i++)
		{
			if(_inventory[i] != null)
			{
				occupiedSlots++;
			}
		}
		
		return _outputValues[occupiedSlots];
	}
	
	public int getOutputValueMax()
	{
		return _outputValues[9];
	}
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		
		if(!worldObj.isRemote)
		{
			for(int i = 0; i < 9; i++)
			{
				if(_inventory[i] != null && MFRRegistry.getPlantables().containsKey(_inventory[i].itemID))
				{
					int targetSlot = findMatchingSlot(_inventory[i]);
					if(targetSlot < 0)
					{
						continue;
					}
					
					if(_inventory[targetSlot] == null)
					{
						_inventory[targetSlot] = _inventory[i];
						_inventory[i] = null;
					}
					else
					{
						MFRUtil.mergeStacks(_inventory[targetSlot], _inventory[i]);
						if(_inventory[i].stackSize <= 0)
						{
							_inventory[i] = null;
						}
					}
				}
			}
			
			if(Util.isRedstonePowered(this))
			{
				return;
			}
			
			int newBurn = getOutputValue();
			if(_burnTimeMax - _burnTime >= newBurn)
			{
				_burnTime += newBurn;
				for(int i = 9; i < 18; i++)
				{
					if(_inventory[i] != null)
					{
						decrStackSize(i, 1);
					}
				}
			}
			
			if(_burnTime > 0 && (_tank.getLiquid() == null || _tank.getLiquid().amount < _tank.getCapacity() - _bioFuelPerTick))
			{
				_burnTime -= _burnTimeDecreasePerTick;
				_tank.fill(new LiquidStack(MineFactoryReloadedCore.bioFuelItem.itemID, _bioFuelPerTick), true);
			}
		}
	}
	
	private int findMatchingSlot(ItemStack s)
	{
		for(int i = 9; i < 18; i++)
		{
			if(_inventory[i] != null && _inventory[i].itemID == s.itemID && _inventory[i].getItemDamage() == s.getItemDamage())
			{
				return i;
			}
		}
		return findEmptySlot();
	}
	
	private int findEmptySlot()
	{
		for(int i = 9; i < 18; i++)
		{
			if(_inventory[i] == null)
			{
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public String getGuiBackground()
	{
		return "bioreactor.png";
	}
	
	@Override
	public ILiquidTank getTank()
	{
		return _tank;
	}
	
	@Override
	protected boolean shouldPumpLiquid()
	{
		return true;
	}
	
	@Override
	public int getSizeInventory()
	{
		return 18;
	}

	@Override
	public String getInvName()
	{
		return "Bio Reactor";
	}

	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		return 0;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 9;
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill)
	{
		return 0;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill)
	{
		return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction)
	{
		return new ILiquidTank[] { _tank };
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type)
	{
		return _tank;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("burnTime", _burnTime);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		_burnTime = nbttagcompound.getInteger("burnTime");
	}
}
