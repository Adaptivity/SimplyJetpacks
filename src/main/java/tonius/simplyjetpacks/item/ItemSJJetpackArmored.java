package tonius.simplyjetpacks.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

public class ItemSJJetpackArmored extends ItemSJJetpack {

    protected int armorDisplay;
    protected double armorAbsorption;

    public ItemSJJetpackArmored(int id, EnumArmorMaterial material, String name, int maxEnergy, int maxInput, int jetpackTier, int tickEnergy, double maxSpeed, double acceleration, double forwardThrust, double hoverModeIdleSpeed, double hoverModeActiveSpeed, int armorDisplay, double armorAbsorption) {
        super(id, material, name, maxEnergy, maxInput, jetpackTier, tickEnergy, maxSpeed, acceleration, forwardThrust, hoverModeIdleSpeed, hoverModeActiveSpeed);
        this.armorDisplay = armorDisplay;
        this.armorAbsorption = armorAbsorption;
    }

    @Override
    public boolean isArmored() {
        return true;
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
        return new ArmorProperties(0, this.armorAbsorption, 50);
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
        return this.armorDisplay;
    }

}
