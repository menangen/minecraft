package net.minecraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.text.DecimalFormat;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.BlockEntityTag;
import net.minecraft.util.datafix.walkers.EntityTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public final class ItemStack
{
    public static final ItemStack field_190927_a = new ItemStack((Item)null);
    public static final DecimalFormat DECIMALFORMAT = new DecimalFormat("#.##");

    /** Size of the stack. */
    private int stackSize;

    /**
     * Number of animation frames to go when receiving an item (by walking into it, for example).
     */
    private int animationsToGo;
    private final Item item;

    /**
     * A NBTTagMap containing data about an ItemStack. Can only be used for non stackable items
     */
    private NBTTagCompound stackTagCompound;
    private boolean field_190928_g;
    private int itemDamage;

    /** Item frame this stack is on, or null if not on an item frame. */
    private EntityItemFrame itemFrame;
    private Block canDestroyCacheBlock;
    private boolean canDestroyCacheResult;
    private Block canPlaceOnCacheBlock;
    private boolean canPlaceOnCacheResult;

    public ItemStack(Block blockIn)
    {
        this((Block)blockIn, 1);
    }

    public ItemStack(Block blockIn, int amount)
    {
        this((Block)blockIn, amount, 0);
    }

    public ItemStack(Block blockIn, int amount, int meta)
    {
        this(Item.getItemFromBlock(blockIn), amount, meta);
    }

    public ItemStack(Item itemIn)
    {
        this((Item)itemIn, 1);
    }

    public ItemStack(Item itemIn, int amount)
    {
        this((Item)itemIn, amount, 0);
    }

    public ItemStack(Item itemIn, int amount, int meta)
    {
        this.item = itemIn;
        this.itemDamage = meta;
        this.stackSize = amount;

        if (this.itemDamage < 0)
        {
            this.itemDamage = 0;
        }

        this.func_190923_F();
    }

    private void func_190923_F()
    {
        this.field_190928_g = this.func_190926_b();
    }

    public ItemStack(NBTTagCompound p_i47263_1_)
    {
        this.item = Item.getByNameOrId(p_i47263_1_.getString("id"));
        this.stackSize = p_i47263_1_.getByte("Count");
        this.itemDamage = Math.max(0, p_i47263_1_.getShort("Damage"));

        if (p_i47263_1_.hasKey("tag", 10))
        {
            this.stackTagCompound = p_i47263_1_.getCompoundTag("tag");

            if (this.item != null)
            {
                this.item.updateItemStackNBT(p_i47263_1_);
            }
        }

        this.func_190923_F();
    }

    public boolean func_190926_b()
    {
        return this == field_190927_a ? true : (this.item != null && this.item != Item.getItemFromBlock(Blocks.AIR) ? (this.stackSize <= 0 ? true : this.itemDamage < -32768 || this.itemDamage > 65535) : true);
    }

    public static void registerFixes(DataFixer fixer)
    {
        fixer.registerWalker(FixTypes.ITEM_INSTANCE, new BlockEntityTag());
        fixer.registerWalker(FixTypes.ITEM_INSTANCE, new EntityTag());
    }

    /**
     * Splits off a stack of the given amount of this stack and reduces this stack by the amount.
     */
    public ItemStack splitStack(int amount)
    {
        int i = Math.min(amount, this.stackSize);
        ItemStack itemstack = this.copy();
        itemstack.func_190920_e(i);
        this.func_190918_g(i);
        return itemstack;
    }

    /**
     * Returns the object corresponding to the stack.
     */
    public Item getItem()
    {
        return this.field_190928_g ? Item.getItemFromBlock(Blocks.AIR) : this.item;
    }

    /**
     * Called when the player uses this ItemStack on a Block (right-click). Places blocks, etc. (Legacy name:
     * tryPlaceItemIntoWorld)
     */
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        EnumActionResult enumactionresult = this.getItem().onItemUse(playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);

        if (enumactionresult == EnumActionResult.SUCCESS)
        {
            playerIn.addStat(StatList.getObjectUseStats(this.item));
        }

        return enumactionresult;
    }

    public float getStrVsBlock(IBlockState blockIn)
    {
        return this.getItem().getStrVsBlock(this, blockIn);
    }

    public ActionResult<ItemStack> useItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        return this.getItem().onItemRightClick(worldIn, playerIn, hand);
    }

    /**
     * Called when the item in use count reach 0, e.g. item food eaten. Return the new ItemStack. Args : world, entity
     */
    public ItemStack onItemUseFinish(World worldIn, EntityLivingBase entityLiving)
    {
        return this.getItem().onItemUseFinish(this, worldIn, entityLiving);
    }

    /**
     * Write the stack fields to a NBT object. Return the new NBT object.
     */
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        ResourceLocation resourcelocation = (ResourceLocation)Item.REGISTRY.getNameForObject(this.item);
        nbt.setString("id", resourcelocation == null ? "minecraft:air" : resourcelocation.toString());
        nbt.setByte("Count", (byte)this.stackSize);
        nbt.setShort("Damage", (short)this.itemDamage);

        if (this.stackTagCompound != null)
        {
            nbt.setTag("tag", this.stackTagCompound);
        }

        return nbt;
    }

    /**
     * Returns maximum size of the stack.
     */
    public int getMaxStackSize()
    {
        return this.getItem().getItemStackLimit();
    }

    /**
     * Returns true if the ItemStack can hold 2 or more units of the item.
     */
    public boolean isStackable()
    {
        return this.getMaxStackSize() > 1 && (!this.isItemStackDamageable() || !this.isItemDamaged());
    }

    /**
     * true if this itemStack is damageable
     */
    public boolean isItemStackDamageable()
    {
        return this.field_190928_g ? false : (this.item.getMaxDamage() <= 0 ? false : !this.hasTagCompound() || !this.getTagCompound().getBoolean("Unbreakable"));
    }

    public boolean getHasSubtypes()
    {
        return this.getItem().getHasSubtypes();
    }

    /**
     * returns true when a damageable item is damaged
     */
    public boolean isItemDamaged()
    {
        return this.isItemStackDamageable() && this.itemDamage > 0;
    }

    public int getItemDamage()
    {
        return this.itemDamage;
    }

    public int getMetadata()
    {
        return this.itemDamage;
    }

    public void setItemDamage(int meta)
    {
        this.itemDamage = meta;

        if (this.itemDamage < 0)
        {
            this.itemDamage = 0;
        }
    }

    /**
     * Returns the max damage an item in the stack can take.
     */
    public int getMaxDamage()
    {
        return this.getItem().getMaxDamage();
    }

    /**
     * Attempts to damage the ItemStack with par1 amount of damage, If the ItemStack has the Unbreaking enchantment
     * there is a chance for each point of damage to be negated. Returns true if it takes more damage than
     * getMaxDamage(). Returns false otherwise or if the ItemStack can't be damaged or if all points of damage are
     * negated.
     */
    public boolean attemptDamageItem(int amount, Random rand)
    {
        if (!this.isItemStackDamageable())
        {
            return false;
        }
        else
        {
            if (amount > 0)
            {
                int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, this);
                int j = 0;

                for (int k = 0; i > 0 && k < amount; ++k)
                {
                    if (EnchantmentDurability.negateDamage(this, i, rand))
                    {
                        ++j;
                    }
                }

                amount -= j;

                if (amount <= 0)
                {
                    return false;
                }
            }

            this.itemDamage += amount;
            return this.itemDamage > this.getMaxDamage();
        }
    }

    /**
     * Damages the item in the ItemStack
     */
    public void damageItem(int amount, EntityLivingBase entityIn)
    {
        if (!(entityIn instanceof EntityPlayer) || !((EntityPlayer)entityIn).capabilities.isCreativeMode)
        {
            if (this.isItemStackDamageable())
            {
                if (this.attemptDamageItem(amount, entityIn.getRNG()))
                {
                    entityIn.renderBrokenItemStack(this);
                    this.func_190918_g(1);

                    if (entityIn instanceof EntityPlayer)
                    {
                        EntityPlayer entityplayer = (EntityPlayer)entityIn;
                        entityplayer.addStat(StatList.getObjectBreakStats(this.item));
                    }

                    this.itemDamage = 0;
                }
            }
        }
    }

    /**
     * Calls the delegated method to the Item to damage the incoming Entity, and if necessary, triggers a stats
     * increase.
     */
    public void hitEntity(EntityLivingBase entityIn, EntityPlayer playerIn)
    {
        boolean flag = this.item.hitEntity(this, entityIn, playerIn);

        if (flag)
        {
            playerIn.addStat(StatList.getObjectUseStats(this.item));
        }
    }

    /**
     * Called when a Block is destroyed using this ItemStack
     */
    public void onBlockDestroyed(World worldIn, IBlockState blockIn, BlockPos pos, EntityPlayer playerIn)
    {
        boolean flag = this.getItem().onBlockDestroyed(this, worldIn, blockIn, pos, playerIn);

        if (flag)
        {
            playerIn.addStat(StatList.getObjectUseStats(this.item));
        }
    }

    /**
     * Check whether the given Block can be harvested using this ItemStack.
     */
    public boolean canHarvestBlock(IBlockState blockIn)
    {
        return this.getItem().canHarvestBlock(blockIn);
    }

    public boolean interactWithEntity(EntityPlayer playerIn, EntityLivingBase entityIn, EnumHand hand)
    {
        return this.getItem().itemInteractionForEntity(this, playerIn, entityIn, hand);
    }

    /**
     * Returns a new stack with the same properties.
     */
    public ItemStack copy()
    {
        ItemStack itemstack = new ItemStack(this.item, this.stackSize, this.itemDamage);

        if (this.stackTagCompound != null)
        {
            itemstack.stackTagCompound = this.stackTagCompound.copy();
        }

        return itemstack;
    }

    public static boolean areItemStackTagsEqual(ItemStack stackA, ItemStack stackB)
    {
        return stackA.func_190926_b() && stackB.func_190926_b() ? true : (!stackA.func_190926_b() && !stackB.func_190926_b() ? (stackA.stackTagCompound == null && stackB.stackTagCompound != null ? false : stackA.stackTagCompound == null || stackA.stackTagCompound.equals(stackB.stackTagCompound)) : false);
    }

    /**
     * compares ItemStack argument1 with ItemStack argument2; returns true if both ItemStacks are equal
     */
    public static boolean areItemStacksEqual(ItemStack stackA, ItemStack stackB)
    {
        return stackA.func_190926_b() && stackB.func_190926_b() ? true : (!stackA.func_190926_b() && !stackB.func_190926_b() ? stackA.isItemStackEqual(stackB) : false);
    }

    /**
     * compares ItemStack argument to the instance ItemStack; returns true if both ItemStacks are equal
     */
    private boolean isItemStackEqual(ItemStack other)
    {
        return this.stackSize != other.stackSize ? false : (this.getItem() != other.getItem() ? false : (this.itemDamage != other.itemDamage ? false : (this.stackTagCompound == null && other.stackTagCompound != null ? false : this.stackTagCompound == null || this.stackTagCompound.equals(other.stackTagCompound))));
    }

    /**
     * Compares Item and damage value of the two stacks
     */
    public static boolean areItemsEqual(ItemStack stackA, ItemStack stackB)
    {
        return stackA == stackB ? true : (!stackA.func_190926_b() && !stackB.func_190926_b() ? stackA.isItemEqual(stackB) : false);
    }

    public static boolean areItemsEqualIgnoreDurability(ItemStack stackA, ItemStack stackB)
    {
        return stackA == stackB ? true : (!stackA.func_190926_b() && !stackB.func_190926_b() ? stackA.isItemEqualIgnoreDurability(stackB) : false);
    }

    /**
     * compares ItemStack argument to the instance ItemStack; returns true if the Items contained in both ItemStacks are
     * equal
     */
    public boolean isItemEqual(ItemStack other)
    {
        return !other.func_190926_b() && this.item == other.item && this.itemDamage == other.itemDamage;
    }

    public boolean isItemEqualIgnoreDurability(ItemStack stack)
    {
        return !this.isItemStackDamageable() ? this.isItemEqual(stack) : !stack.func_190926_b() && this.item == stack.item;
    }

    public String getUnlocalizedName()
    {
        return this.getItem().getUnlocalizedName(this);
    }

    public String toString()
    {
        return this.stackSize + "x" + this.getItem().getUnlocalizedName() + "@" + this.itemDamage;
    }

    /**
     * Called each tick as long the ItemStack in on player inventory. Used to progress the pickup animation and update
     * maps.
     */
    public void updateAnimation(World worldIn, Entity entityIn, int inventorySlot, boolean isCurrentItem)
    {
        if (this.animationsToGo > 0)
        {
            --this.animationsToGo;
        }

        if (this.item != null)
        {
            this.item.onUpdate(this, worldIn, entityIn, inventorySlot, isCurrentItem);
        }
    }

    public void onCrafting(World worldIn, EntityPlayer playerIn, int amount)
    {
        playerIn.addStat(StatList.getCraftStats(this.item), amount);
        this.getItem().onCreated(this, worldIn, playerIn);
    }

    public int getMaxItemUseDuration()
    {
        return this.getItem().getMaxItemUseDuration(this);
    }

    public EnumAction getItemUseAction()
    {
        return this.getItem().getItemUseAction(this);
    }

    /**
     * Called when the player releases the use item button.
     */
    public void onPlayerStoppedUsing(World worldIn, EntityLivingBase entityLiving, int timeLeft)
    {
        this.getItem().onPlayerStoppedUsing(this, worldIn, entityLiving, timeLeft);
    }

    /**
     * Returns true if the ItemStack has an NBTTagCompound. Currently used to store enchantments.
     */
    public boolean hasTagCompound()
    {
        return !this.field_190928_g && this.stackTagCompound != null;
    }

    @Nullable

    /**
     * Returns the NBTTagCompound of the ItemStack.
     */
    public NBTTagCompound getTagCompound()
    {
        return this.stackTagCompound;
    }

    public NBTTagCompound func_190925_c(String p_190925_1_)
    {
        if (this.stackTagCompound != null && this.stackTagCompound.hasKey(p_190925_1_, 10))
        {
            return this.stackTagCompound.getCompoundTag(p_190925_1_);
        }
        else
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            this.setTagInfo(p_190925_1_, nbttagcompound);
            return nbttagcompound;
        }
    }

    @Nullable

    /**
     * Get an NBTTagCompound from this stack's NBT data.
     */
    public NBTTagCompound getSubCompound(String key)
    {
        return this.stackTagCompound != null && this.stackTagCompound.hasKey(key, 10) ? this.stackTagCompound.getCompoundTag(key) : null;
    }

    public void func_190919_e(String p_190919_1_)
    {
        if (this.stackTagCompound != null && this.stackTagCompound.hasKey(p_190919_1_, 10))
        {
            this.stackTagCompound.removeTag(p_190919_1_);
        }
    }

    @Nullable
    public NBTTagList getEnchantmentTagList()
    {
        return this.stackTagCompound == null ? null : this.stackTagCompound.getTagList("ench", 10);
    }

    /**
     * Assigns a NBTTagCompound to the ItemStack, minecraft validates that only non-stackable items can have it.
     */
    public void setTagCompound(@Nullable NBTTagCompound nbt)
    {
        this.stackTagCompound = nbt;
    }

    /**
     * returns the display name of the itemstack
     */
    public String getDisplayName()
    {
        NBTTagCompound nbttagcompound = this.getSubCompound("display");

        if (nbttagcompound != null)
        {
            if (nbttagcompound.hasKey("Name", 8))
            {
                return nbttagcompound.getString("Name");
            }

            if (nbttagcompound.hasKey("LocName", 8))
            {
                return I18n.translateToLocal(nbttagcompound.getString("LocName"));
            }
        }

        return this.getItem().getItemStackDisplayName(this);
    }

    public ItemStack func_190924_f(String p_190924_1_)
    {
        this.func_190925_c("display").setString("LocName", p_190924_1_);
        return this;
    }

    public ItemStack setStackDisplayName(String displayName)
    {
        this.func_190925_c("display").setString("Name", displayName);
        return this;
    }

    /**
     * Clear any custom name set for this ItemStack
     */
    public void clearCustomName()
    {
        NBTTagCompound nbttagcompound = this.getSubCompound("display");

        if (nbttagcompound != null)
        {
            nbttagcompound.removeTag("Name");

            if (nbttagcompound.hasNoTags())
            {
                this.func_190919_e("display");
            }
        }

        if (this.stackTagCompound != null && this.stackTagCompound.hasNoTags())
        {
            this.stackTagCompound = null;
        }
    }

    /**
     * Returns true if the itemstack has a display name
     */
    public boolean hasDisplayName()
    {
        NBTTagCompound nbttagcompound = this.getSubCompound("display");
        return nbttagcompound != null && nbttagcompound.hasKey("Name", 8);
    }

    public EnumRarity getRarity()
    {
        return this.getItem().getRarity(this);
    }

    /**
     * True if it is a tool and has no enchantments to begin with
     */
    public boolean isItemEnchantable()
    {
        return !this.getItem().isItemTool(this) ? false : !this.isItemEnchanted();
    }

    /**
     * Adds an enchantment with a desired level on the ItemStack.
     */
    public void addEnchantment(Enchantment ench, int level)
    {
        if (this.stackTagCompound == null)
        {
            this.setTagCompound(new NBTTagCompound());
        }

        if (!this.stackTagCompound.hasKey("ench", 9))
        {
            this.stackTagCompound.setTag("ench", new NBTTagList());
        }

        NBTTagList nbttaglist = this.stackTagCompound.getTagList("ench", 10);
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setShort("id", (short)Enchantment.getEnchantmentID(ench));
        nbttagcompound.setShort("lvl", (short)((byte)level));
        nbttaglist.appendTag(nbttagcompound);
    }

    /**
     * True if the item has enchantment data
     */
    public boolean isItemEnchanted()
    {
        return this.stackTagCompound != null && this.stackTagCompound.hasKey("ench", 9) ? !this.stackTagCompound.getTagList("ench", 10).hasNoTags() : false;
    }

    public void setTagInfo(String key, NBTBase value)
    {
        if (this.stackTagCompound == null)
        {
            this.setTagCompound(new NBTTagCompound());
        }

        this.stackTagCompound.setTag(key, value);
    }

    public boolean canEditBlocks()
    {
        return this.getItem().canItemEditBlocks();
    }

    /**
     * Return whether this stack is on an item frame.
     */
    public boolean isOnItemFrame()
    {
        return this.itemFrame != null;
    }

    /**
     * Set the item frame this stack is on.
     */
    public void setItemFrame(EntityItemFrame frame)
    {
        this.itemFrame = frame;
    }

    @Nullable

    /**
     * Return the item frame this stack is on. Returns null if not on an item frame.
     */
    public EntityItemFrame getItemFrame()
    {
        return this.field_190928_g ? null : this.itemFrame;
    }

    /**
     * Get this stack's repair cost, or 0 if no repair cost is defined.
     */
    public int getRepairCost()
    {
        return this.hasTagCompound() && this.stackTagCompound.hasKey("RepairCost", 3) ? this.stackTagCompound.getInteger("RepairCost") : 0;
    }

    /**
     * Set this stack's repair cost.
     */
    public void setRepairCost(int cost)
    {
        if (!this.hasTagCompound())
        {
            this.stackTagCompound = new NBTTagCompound();
        }

        this.stackTagCompound.setInteger("RepairCost", cost);
    }

    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot)
    {
        Multimap<String, AttributeModifier> multimap;

        if (this.hasTagCompound() && this.stackTagCompound.hasKey("AttributeModifiers", 9))
        {
            multimap = HashMultimap.<String, AttributeModifier>create();
            NBTTagList nbttaglist = this.stackTagCompound.getTagList("AttributeModifiers", 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                AttributeModifier attributemodifier = SharedMonsterAttributes.readAttributeModifierFromNBT(nbttagcompound);

                if (attributemodifier != null && (!nbttagcompound.hasKey("Slot", 8) || nbttagcompound.getString("Slot").equals(equipmentSlot.getName())) && attributemodifier.getID().getLeastSignificantBits() != 0L && attributemodifier.getID().getMostSignificantBits() != 0L)
                {
                    multimap.put(nbttagcompound.getString("AttributeName"), attributemodifier);
                }
            }
        }
        else
        {
            multimap = this.getItem().getItemAttributeModifiers(equipmentSlot);
        }

        return multimap;
    }

    public void addAttributeModifier(String attributeName, AttributeModifier modifier, @Nullable EntityEquipmentSlot equipmentSlot)
    {
        if (this.stackTagCompound == null)
        {
            this.stackTagCompound = new NBTTagCompound();
        }

        if (!this.stackTagCompound.hasKey("AttributeModifiers", 9))
        {
            this.stackTagCompound.setTag("AttributeModifiers", new NBTTagList());
        }

        NBTTagList nbttaglist = this.stackTagCompound.getTagList("AttributeModifiers", 10);
        NBTTagCompound nbttagcompound = SharedMonsterAttributes.writeAttributeModifierToNBT(modifier);
        nbttagcompound.setString("AttributeName", attributeName);

        if (equipmentSlot != null)
        {
            nbttagcompound.setString("Slot", equipmentSlot.getName());
        }

        nbttaglist.appendTag(nbttagcompound);
    }

    /**
     * Get a ChatComponent for this Item's display name that shows this Item on hover
     */
    public ITextComponent getTextComponent()
    {
        TextComponentString textcomponentstring = new TextComponentString(this.getDisplayName());

        if (this.hasDisplayName())
        {
            textcomponentstring.getStyle().setItalic(Boolean.valueOf(true));
        }

        ITextComponent itextcomponent = (new TextComponentString("[")).appendSibling(textcomponentstring).appendText("]");

        if (!this.field_190928_g)
        {
            NBTTagCompound nbttagcompound = this.writeToNBT(new NBTTagCompound());
            itextcomponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new TextComponentString(nbttagcompound.toString())));
            itextcomponent.getStyle().setColor(this.getRarity().rarityColor);
        }

        return itextcomponent;
    }

    public boolean canDestroy(Block blockIn)
    {
        if (blockIn == this.canDestroyCacheBlock)
        {
            return this.canDestroyCacheResult;
        }
        else
        {
            this.canDestroyCacheBlock = blockIn;

            if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanDestroy", 9))
            {
                NBTTagList nbttaglist = this.stackTagCompound.getTagList("CanDestroy", 8);

                for (int i = 0; i < nbttaglist.tagCount(); ++i)
                {
                    Block block = Block.getBlockFromName(nbttaglist.getStringTagAt(i));

                    if (block == blockIn)
                    {
                        this.canDestroyCacheResult = true;
                        return true;
                    }
                }
            }

            this.canDestroyCacheResult = false;
            return false;
        }
    }

    public boolean canPlaceOn(Block blockIn)
    {
        if (blockIn == this.canPlaceOnCacheBlock)
        {
            return this.canPlaceOnCacheResult;
        }
        else
        {
            this.canPlaceOnCacheBlock = blockIn;

            if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanPlaceOn", 9))
            {
                NBTTagList nbttaglist = this.stackTagCompound.getTagList("CanPlaceOn", 8);

                for (int i = 0; i < nbttaglist.tagCount(); ++i)
                {
                    Block block = Block.getBlockFromName(nbttaglist.getStringTagAt(i));

                    if (block == blockIn)
                    {
                        this.canPlaceOnCacheResult = true;
                        return true;
                    }
                }
            }

            this.canPlaceOnCacheResult = false;
            return false;
        }
    }

    public void func_190915_d(int p_190915_1_)
    {
        this.animationsToGo = p_190915_1_;
    }

    public int func_190916_E()
    {
        return this.field_190928_g ? 0 : this.stackSize;
    }

    public void func_190920_e(int p_190920_1_)
    {
        this.stackSize = p_190920_1_;
        this.func_190923_F();
    }

    public void func_190917_f(int p_190917_1_)
    {
        this.func_190920_e(this.stackSize + p_190917_1_);
    }

    public void func_190918_g(int p_190918_1_)
    {
        this.func_190917_f(-p_190918_1_);
    }
}
