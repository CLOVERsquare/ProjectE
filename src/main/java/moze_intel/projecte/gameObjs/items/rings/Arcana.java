package moze_intel.projecte.gameObjs.items.rings;

import java.util.List;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import cpw.mods.fml.common.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import moze_intel.projecte.api.IExtraFunction;
import moze_intel.projecte.api.IFireProtectionItem;
import moze_intel.projecte.api.IFlightItem;
import moze_intel.projecte.api.IModeChanger;
import moze_intel.projecte.api.IProjectileShooter;
import moze_intel.projecte.gameObjs.entity.EntityFireProjectile;
import moze_intel.projecte.gameObjs.entity.EntityLightningProjectile;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.handlers.PlayerChecks;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class Arcana extends ItemPE implements IBauble, IModeChanger, IFlightItem, IFireProtectionItem, IExtraFunction, IProjectileShooter
{
	private static final String[] MODES = new String[]{"Zero", "Ignition", "Harvest", "SWRG"};

	private IIcon[] icons = new IIcon[4];
	private IIcon[] iconsOn = new IIcon[4];
	
	public Arcana()
	{
		super();
		setUnlocalizedName("arcana_ring");
		setMaxStackSize(1);
		setNoRepair();
		setContainerItem(this);
	}
	
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	{
		return false;
	}
	
	public byte getMode(ItemStack stack)
	{
		return (byte)stack.getItemDamage();
	}
	
	public void changeMode(EntityPlayer player, ItemStack stack)
	{
		stack.setItemDamage((stack.getItemDamage() + 1) % 4);
	}
	
	private void tick(ItemStack stack, World world, EntityPlayerMP player)
	{
		if(!player.capabilities.isCreativeMode)
		{
			if(!player.capabilities.allowFlying)
			{
				//System.out.println("Enabling flight");
				PlayerHelper.enableFlight(player);
				PlayerChecks.addPlayerFlyChecks(player);
			}
			
			if(!player.isImmuneToFire())
			{
				//System.out.println("Immunising against fire");
				PlayerHelper.setPlayerFireImmunity(player, true);
				PlayerChecks.addPlayerFireChecks(player);
			}
		}
		
		if(stack.getTagCompound().getBoolean("Active"))
		{
			switch(stack.getItemDamage())
			{
				case 0:
					WorldHelper.freezeNearbyRandomly(world, player);
					break;
				case 1:
					WorldHelper.igniteNearby(world, player);
					break;
				case 2:
					WorldHelper.growNearbyRandomly(true, world, player);
					break;
				case 3:
					WorldHelper.repelEntitiesInAABBFromPoint(world, player.boundingBox.expand(5, 5, 5), player.posX, player.posY, player.posZ, true);
					break;
			}
		}
	}
	
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held)
	{
		if(stack.stackTagCompound == null) stack.setTagCompound(new NBTTagCompound());
		
		if(world.isRemote || slot > 8 || !(entity instanceof EntityPlayerMP)) return;
		
		tick(stack, world, (EntityPlayerMP)entity);
	}
	
	@Optional.Method(modid = "Baubles")
	public BaubleType getBaubleType(ItemStack stack)
	{
		return BaubleType.RING;
	}

	@Optional.Method(modid = "Baubles")
	public void onWornTick(ItemStack stack, EntityLivingBase entity)
	{
		if(stack.stackTagCompound == null) stack.setTagCompound(new NBTTagCompound());
		
		if(entity.worldObj.isRemote || !(entity instanceof EntityPlayerMP)) return;
		
		tick(stack, entity.worldObj, (EntityPlayerMP)entity);
	}

	@Optional.Method(modid = "Baubles")
	public void onEquipped(ItemStack stack, EntityLivingBase player)
	{
		
	}

	@Optional.Method(modid = "Baubles")
	public void onUnequipped(ItemStack stack, EntityLivingBase player)
	{
		
	}

	@Optional.Method(modid = "Baubles")
	public boolean canEquip(ItemStack stack, EntityLivingBase player)
	{
		return true;
	}

	@Optional.Method(modid = "Baubles")
	public boolean canUnequip(ItemStack stack, EntityLivingBase player)
	{
		return true;
	}
	
	public IIcon getIcon(ItemStack stack, int pass)
	{
		return getIconIndex(stack);
	}
	
	public IIcon getIconIndex(ItemStack stack)
	{
		boolean active = (stack.hasTagCompound() ? stack.getTagCompound().getBoolean("Active") : false);
		return (active ? iconsOn : icons)[MathHelper.clamp_int(stack.getItemDamage(), 0, 3)];
	}
	
	public void registerIcons(IIconRegister register)
	{
		for(int i = 0; i < 4; i++)
		{
			icons[i] = register.registerIcon(this.getTexture("rings", "arcana_" + i));
		}
		
		for(int i = 0; i < 4; i++)
		{
			iconsOn[i] = register.registerIcon(this.getTexture("rings", "arcana_" + i + "_on"));
		}
		
		itemIcon = icons[0];
	}
	
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b)
	{
		if(stack.hasTagCompound())
		{
			if(!stack.stackTagCompound.getBoolean("Active"))
			{
				list.add(EnumChatFormatting.RED + "Inactive!");
			}
			else
			{
				list.add("Mode: " + EnumChatFormatting.AQUA + MODES[stack.getItemDamage()]);
			}
		}
	}
	
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if(!world.isRemote)
		{
			NBTTagCompound compound = stack.getTagCompound();
			
			compound.setBoolean("Active", !compound.getBoolean("Active"));
		}
		
		return stack;
	}
	
	public void doExtraFunction(ItemStack stack, EntityPlayer player) // GIANT FIRE ROW OF DEATH
	{
		World world = player.worldObj;
		
		if(world.isRemote) return;
		
		switch(stack.getItemDamage())
		{
			case 1: // ignition
				switch(MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5) & 3)
				{
					case 0: // south, -z
					case 2: // north, +z
						for(int x = (int) (player.posX - 30); x <= player.posX + 30; x++)
							for(int y = (int) (player.posY - 5); y <= player.posY + 5; y++)
								for(int z = (int) (player.posZ - 3); z <= player.posZ + 3; z++)
									if(world.getBlock(x, y, z) == Blocks.air)
										world.setBlock(x, y, z, Blocks.fire);
						break;
					case 1: // west, -x
					case 3: // east, +x
						for(int x = (int) (player.posX - 3); x <= player.posX + 3; x++)
							for(int y = (int) (player.posY - 5); y <= player.posY + 5; y++)
								for(int z = (int) (player.posZ - 30); z <= player.posZ + 30; z++)
									if(world.getBlock(x, y, z) == Blocks.air)
										world.setBlock(x, y, z, Blocks.fire);
						break;
				}
				break;
		}
	}
	
	public boolean shootProjectile(EntityPlayer player, ItemStack stack)
	{
		World world = player.worldObj;
		
		if(world.isRemote) return false;
		
		switch(stack.getItemDamage())
		{
			case 0: // zero
				EntitySnowball snowball = new EntitySnowball(world, player);
				world.spawnEntityInWorld(snowball);
				break;
			case 1: // ignition
				EntityFireProjectile fire = new EntityFireProjectile(world, player);
				world.spawnEntityInWorld(fire);
				break;
			case 3: // swrg
				EntityLightningProjectile lightning = new EntityLightningProjectile(world, player, stack);
				world.spawnEntityInWorld(lightning);
				break;
		}
		
		return true;
	}
}
