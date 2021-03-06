package tonius.simplyjetpacks.item;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import tonius.simplyjetpacks.KeyboardTracker;
import tonius.simplyjetpacks.PacketHandler;
import tonius.simplyjetpacks.SimplyJetpacks;
import tonius.simplyjetpacks.util.LangUtils;
import tonius.simplyjetpacks.util.StackUtils;
import tonius.simplyjetpacks.util.StringUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSJJetpack extends ItemSJArmorEnergy {

    protected int tickEnergy;
    protected int tickEnergyHover;
    protected double maxSpeed;
    protected double acceleration;
    protected double forwardThrust;
    protected double hoverModeIdleSpeed;
    protected double hoverModeActiveSpeed;

    public ItemSJJetpack(int id, EnumArmorMaterial material, String name, int maxEnergy, int maxInput, int tickEnergy, double maxSpeed, double acceleration, double forwardThrust, double hoverModeIdleSpeed, double hoverModeActiveSpeed) {
        super(id, material, 2, 1, name, maxEnergy, maxInput, 0);
        this.tickEnergy = tickEnergy;
        this.tickEnergyHover = (int) (tickEnergy / 1.5);
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.forwardThrust = forwardThrust;
        this.hoverModeIdleSpeed = hoverModeIdleSpeed;
        this.hoverModeActiveSpeed = hoverModeActiveSpeed;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getItemDisplayName(ItemStack itemStack) {
        if (itemStack.getItem() == SimplyJetpacks.jetpackTier3) {
            return StringUtils.YELLOW + super.getItemDisplayName(itemStack);
        } else if (itemStack.getItem() == SimplyJetpacks.jetpackTier4) {
            return StringUtils.BRIGHT_BLUE + super.getItemDisplayName(itemStack);
        }
        return super.getItemDisplayName(itemStack);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
        if (StringUtils.isShiftKeyDown()) {
            list.add(StringUtils.getChargeText(this.getEnergyStored(itemStack), this.getMaxEnergyStored(itemStack)));
            list.add(StringUtils.getStateText(this.isOn(itemStack)));
            list.add(StringUtils.getHoverModeText(this.isHoverModeActive(itemStack)));
            int currentTickEnergy = this.isHoverModeActive(itemStack) ? this.tickEnergyHover : this.tickEnergy;
            list.add(StringUtils.getEnergyUsageText(currentTickEnergy));
        } else if (StringUtils.isControlKeyDown()) {
            list.add(StringUtils.LIGHT_BLUE + LangUtils.translate("tooltip.jetpack.description.1"));
            list.add(StringUtils.LIGHT_BLUE + LangUtils.translate("tooltip.jetpack.description.2"));
        } else {
            list.add(StringUtils.getShiftText());
            list.add(StringUtils.getCtrlText());
        }
    }

    @Override
    public void getSubItems(int id, CreativeTabs creativeTabs, List list) {
        list.add(new ItemStack(id, 1, 31));
        list.add(this.getChargedItem(this));
    }

    @Override
    public void onArmorTickUpdate(World world, EntityPlayer player, ItemStack itemStack) {
        this.useJetpack(player, itemStack, false);
    }

    public void useJetpack(EntityLivingBase user, ItemStack jetpack, boolean force) {
        if (isOn(jetpack)) {
            boolean hoverMode = this.isHoverModeActive(jetpack);
            double hoverSpeed = user.isSneaking() ? this.hoverModeActiveSpeed : this.hoverModeIdleSpeed;
            boolean jumpKeyDown = true;
            if (!force && user instanceof EntityPlayer && !KeyboardTracker.isJumpKeyDown((EntityPlayer) user)) {
                jumpKeyDown = false;
            }

            if (jumpKeyDown || (hoverMode && !user.onGround)) {
                int usedPower = hoverMode ? this.tickEnergyHover : this.tickEnergy;
                this.subtractEnergy(jetpack, usedPower, false);

                if (this.getEnergyStored(jetpack) > 0) {
                    if (jumpKeyDown) {
                        if (!hoverMode) {
                            user.motionY = Math.min(user.motionY + this.acceleration, this.maxSpeed);
                        } else {
                            user.motionY = Math.min(user.motionY + this.acceleration, this.hoverModeActiveSpeed);
                        }
                    } else {
                        user.motionY = Math.max(user.motionY, -hoverSpeed);
                    }

                    user.fallDistance = 0.0F;
                    if (user instanceof EntityPlayerMP) {
                        ((EntityPlayerMP) user).playerNetServerHandler.ticksForFloatKick = 0;
                    }
                    this.sendJetpackPacket(user, hoverMode);
                }
            }
        }
    }

    public boolean isHoverModeActive(ItemStack itemStack) {
        return StackUtils.getNBT(itemStack).getBoolean("HoverModeActive");
    }

    public void toggleHoverMode(ItemStack itemStack, EntityPlayer player) {
        if (this.isHoverModeActive(itemStack)) {
            player.addChatMessage(LangUtils.translate("chat.jetpack.hoverMode") + " " + StringUtils.LIGHT_RED + LangUtils.translate("chat.disabled"));
            itemStack.stackTagCompound.setBoolean("HoverModeActive", false);
        } else {
            player.addChatMessage(LangUtils.translate("chat.jetpack.hoverMode") + " " + StringUtils.BRIGHT_GREEN + LangUtils.translate("chat.enabled"));
            itemStack.stackTagCompound.setBoolean("HoverModeActive", true);
        }
    }

    public void sendJetpackPacket(EntityLivingBase user, boolean hoverMode) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        try {
            data.writeInt(PacketHandler.JETPACK_TICK);
            data.writeInt(user.entityId);
            data.writeBoolean(hoverMode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "SmpJet";
        packet.data = bytes.toByteArray();
        packet.length = bytes.size();
        PacketDispatcher.sendPacketToAllAround(user.posX, user.posY, user.posZ, 128, user.worldObj.provider.dimensionId, packet);
    }

    @Override
    public String getActivateMsg() {
        return LangUtils.translate("chat.jetpack.engine") + " " + StringUtils.BRIGHT_GREEN + LangUtils.translate("chat.enabled");
    }

    @Override
    public String getDeactivateMsg() {
        return LangUtils.translate("chat.jetpack.engine") + " " + StringUtils.LIGHT_RED + LangUtils.translate("chat.disabled");
    }

    @Override
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        return 0;
    }

}
