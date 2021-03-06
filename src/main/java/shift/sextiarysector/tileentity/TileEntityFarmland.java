package shift.sextiarysector.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import shift.sextiarysector.SSCrops;
import shift.sextiarysector.api.agriculture.IFertilizer;
import shift.sextiarysector.api.agriculture.TileFarmland;

public class TileEntityFarmland extends TileEntity implements TileFarmland {

    //水
    //protected FluidTank water = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
    protected int water;
    private final int MAX_WATER = 10;

    //肥料
    private IFertilizer fertilizer;

    @Override
    public void updateEntity() {

        if (!this.worldObj.isRemote) {
            this.updateServerEntity();
        }

    }

    public void updateServerEntity() {

        this.doSpreadingWater();

        if (this.getBlockMetadata() == 0 && this.hasWater()) {
            this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 4);
            this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }

        if (this.getBlockMetadata() == 1 && !this.hasWater()) {
            this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 4);
            this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }

    }

    public void doSpreadingWater() {

        if (getWorldObj().getWorldTime() % 40 != 0) return;
        if (this.water < 7) return;

        if (getWorldObj().rand.nextBoolean()) {

            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {

                    if (i == 0 && j == 0) continue;
                    if (this.water < 7) break;

                    TileEntity t = getWorldObj().getTileEntity(xCoord + i, yCoord, zCoord + j);

                    if (!(t instanceof TileFarmland)) continue;
                    TileFarmland f = (TileFarmland) t;
                    if (f.getWater() + 1 >= this.water) continue;
                    if (f.addWater(1) > 0) this.water--;

                }
            }

        } else {

            for (int i = 1; i > -2; i--) {
                for (int j = 1; j > -2; j--) {

                    if (i == 0 && j == 0) continue;
                    if (this.water < 7) break;

                    TileEntity t = getWorldObj().getTileEntity(xCoord + i, yCoord, zCoord + j);

                    if (!(t instanceof TileFarmland)) continue;
                    TileFarmland f = (TileFarmland) t;
                    if (f.getWater() + 1 >= this.water) continue;
                    if (f.addWater(1) > 0) this.water--;

                }
            }

        }

    }

    @Override
    public boolean canCropGrow(int water) {

        if (this.water < water) return false;

        return true;

    }

    @Override
    public void doGrow(int water) {

        this.water -= water;

    }

    @Override
    public int getWater() {
        return this.water;
    }

    @Override
    public int addWater(int amount) {

        int add = this.water;

        this.water = Math.min(water + amount, MAX_WATER);

        return this.water - add;

    }

    @Override
    public boolean hasWater() {
        return water > 5;
    }

    @Override
    public IFertilizer getFertilizer() {
        return fertilizer;
    }

    @Override
    public void setFertilizer(IFertilizer fertilizer) {
        this.fertilizer = fertilizer;
        this.getWorldObj().markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    //NBT
    @Override
    public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
        super.readFromNBT(par1nbtTagCompound);

        this.water = par1nbtTagCompound.getInteger("water");

        if (par1nbtTagCompound.hasKey("fertilizerName")) {

            this.fertilizer = SSCrops.fertilizerManager.getFertilizer(par1nbtTagCompound.getString("fertilizerName"));

        }

    }

    @Override
    public void writeToNBT(NBTTagCompound par1nbtTagCompound) {

        super.writeToNBT(par1nbtTagCompound);
        par1nbtTagCompound.setInteger("water", this.water);

        if (fertilizer != null) par1nbtTagCompound.setString("fertilizerName", fertilizer.getName());

    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public World getWorld() {
        return this.getWorld();
    }

    @Override
    public int getX() {
        return this.xCoord;
    }

    @Override
    public int getY() {
        return this.yCoord;
    }

    @Override
    public int getZ() {
        return this.zCoord;
    }

}
