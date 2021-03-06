package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class ItemBanner extends ItemBlock
{
    public ItemBanner()
    {
        super(Blocks.STANDING_BANNER);
        this.maxStackSize = 16;
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
    }

    /**
     * Called when a Block is right-clicked with this Item
     */
    public EnumActionResult onItemUse(EntityPlayer stack, World playerIn, BlockPos worldIn, EnumHand pos, EnumFacing hand, float facing, float hitX, float hitY)
    {
        IBlockState iblockstate = playerIn.getBlockState(worldIn);
        boolean flag = iblockstate.getBlock().isReplaceable(playerIn, worldIn);

        if (hand != EnumFacing.DOWN && (iblockstate.getMaterial().isSolid() || flag) && (!flag || hand == EnumFacing.UP))
        {
            worldIn = worldIn.offset(hand);
            ItemStack itemstack = stack.getHeldItem(pos);

            if (stack.canPlayerEdit(worldIn, hand, itemstack) && Blocks.STANDING_BANNER.canPlaceBlockAt(playerIn, worldIn))
            {
                if (playerIn.isRemote)
                {
                    return EnumActionResult.SUCCESS;
                }
                else
                {
                    worldIn = flag ? worldIn.down() : worldIn;

                    if (hand == EnumFacing.UP)
                    {
                        int i = MathHelper.floor((double)((stack.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
                        playerIn.setBlockState(worldIn, Blocks.STANDING_BANNER.getDefaultState().withProperty(BlockStandingSign.ROTATION, Integer.valueOf(i)), 3);
                    }
                    else
                    {
                        playerIn.setBlockState(worldIn, Blocks.WALL_BANNER.getDefaultState().withProperty(BlockWallSign.FACING, hand), 3);
                    }

                    TileEntity tileentity = playerIn.getTileEntity(worldIn);

                    if (tileentity instanceof TileEntityBanner)
                    {
                        ((TileEntityBanner)tileentity).setItemValues(itemstack, false);
                    }

                    itemstack.func_190918_g(1);
                    return EnumActionResult.SUCCESS;
                }
            }
            else
            {
                return EnumActionResult.FAIL;
            }
        }
        else
        {
            return EnumActionResult.FAIL;
        }
    }

    public String getItemStackDisplayName(ItemStack stack)
    {
        String s = "item.banner.";
        EnumDyeColor enumdyecolor = getBaseColor(stack);
        s = s + enumdyecolor.getUnlocalizedName() + ".name";
        return I18n.translateToLocal(s);
    }

    public static ItemStack func_190910_a(EnumDyeColor p_190910_0_, @Nullable NBTTagList p_190910_1_)
    {
        ItemStack itemstack = new ItemStack(Items.BANNER, 1, p_190910_0_.getDyeDamage());

        if (p_190910_1_ != null && !p_190910_1_.hasNoTags())
        {
            itemstack.func_190925_c("BlockEntityTag").setTag("Patterns", p_190910_1_.copy());
        }

        return itemstack;
    }

    public static EnumDyeColor getBaseColor(ItemStack stack)
    {
        return EnumDyeColor.byDyeDamage(stack.getMetadata() & 15);
    }
}
