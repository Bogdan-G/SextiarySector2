package shift.sextiarysector.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import shift.sextiarysector.api.equipment.EquipmentType;

public class InventoryQuiver implements IInventory {

    private final IInventory inventoryPlayer;
    private final ItemStack currentItem;
    private final ItemStack[] items;

    public InventoryQuiver(InventoryPlayer inventory) {
        inventoryPlayer = inventory;
        currentItem = ((InventoryPlayer) inventoryPlayer).getCurrentItem();

        //InventorySize
        items = new ItemStack[27];
    }

    public InventoryQuiver(InventoryPlayerNext inventory) {
        inventoryPlayer = inventory;
        currentItem = inventoryPlayer.getStackInSlot(EquipmentType.Bag.getSlots()[0]);

        //InventorySize
        items = new ItemStack[27];
    }

    @Override
    public int getSizeInventory() {
        return items.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return items[slot];
    }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {
        if (this.items[p_70298_1_] != null) {
            ItemStack itemstack;

            if (this.items[p_70298_1_].stackSize <= p_70298_2_) {
                itemstack = this.items[p_70298_1_];
                this.items[p_70298_1_] = null;
                this.markDirty();
                return itemstack;
            } else {
                itemstack = this.items[p_70298_1_].splitStack(p_70298_2_);

                if (this.items[p_70298_1_].stackSize == 0) {
                    this.items[p_70298_1_] = null;
                }

                this.markDirty();
                return itemstack;
            }
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_) {
        if (this.items[p_70304_1_] != null) {
            ItemStack itemstack = this.items[p_70304_1_];
            this.items[p_70304_1_] = null;
            return itemstack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {
        this.items[p_70299_1_] = p_70299_2_;

        if (p_70299_2_ != null && p_70299_2_.stackSize > this.getInventoryStackLimit()) {
            p_70299_2_.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
    }

    @Override
    public String getInventoryName() {
        return "InventoryItem";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
        return true;
    }

    /*
        Containerが開かれたタイミングでItemStackの持っているNBTからアイテムを読み込んでいる
     */
    @Override
    public void openInventory() {
        if (!currentItem.hasTagCompound()) {
            currentItem.setTagCompound(new NBTTagCompound());
            currentItem.getTagCompound().setTag("Items", new NBTTagList());
        }
        NBTTagList tags = (NBTTagList) currentItem.getTagCompound().getTag("Items");

        for (int i = 0; i < tags.tagCount(); i++) {
            NBTTagCompound tagCompound = tags.getCompoundTagAt(i);
            int slot = tagCompound.getByte("Slot");
            if (slot >= 0 && slot < items.length) {
                items[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
    }

    /*
        Containerを閉じるときに保存
     */
    @Override
    public void closeInventory() {
        NBTTagList tagList = new NBTTagList();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setByte("Slot", (byte) i);
                items[i].writeToNBT(compound);
                tagList.appendTag(compound);
            }
        }
        ItemStack result = new ItemStack(currentItem.getItem(), 1);
        result.setTagCompound(new NBTTagCompound());
        result.getTagCompound().setTag("Items", tagList);

        //ItemStackをセットする。NBTは右クリック等のタイミングでしか保存されないため再セットで保存している。
        if (inventoryPlayer instanceof InventoryPlayer) {
            ((InventoryPlayer) inventoryPlayer).mainInventory[((InventoryPlayer) inventoryPlayer).currentItem] = result;
        } else if (inventoryPlayer instanceof InventoryPlayerNext) {
            inventoryPlayer.setInventorySlotContents(EquipmentType.Bag.getSlots()[0], result);
        }

    }

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return true;
    }

    private int getIndex(Item p_146029_1_) {

        for (int i = 0; i < this.items.length; ++i) {
            if (this.items[i] != null && this.items[i].getItem() == p_146029_1_) {
                return i;
            }
        }

        return -1;
    }

    public boolean consumeInventoryItem(Item p_146026_1_) {
        int i = this.getIndex(p_146026_1_);

        if (i < 0) {
            return false;
        } else {
            if (--this.items[i].stackSize <= 0) {
                this.items[i] = null;
            }

            return true;
        }
    }

    public boolean hasItem(Item p_146028_1_) {
        int i = this.getIndex(p_146028_1_);
        return i >= 0;
    }

}
