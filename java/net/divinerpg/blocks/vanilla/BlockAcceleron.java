package net.divinerpg.blocks.vanilla;

import net.divinerpg.blocks.base.BlockMod;
import net.divinerpg.blocks.base.tileentity.TileEntityModFurnace;
import net.divinerpg.libs.Reference;
import net.divinerpg.utils.material.EnumBlockType;
import net.divinerpg.utils.tabs.DivineRPGTabs;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAcceleron extends BlockMod{

	private IIcon top;
	private IIcon side;
	
	public BlockAcceleron() {
		super(EnumBlockType.ROCK, "acceleron", 3.0F);
		slipperiness = 4.0F;
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister icon) {
        side = icon.registerIcon(Reference.PREFIX + "acceleron" + "_side");
        top = icon.registerIcon(Reference.PREFIX + "acceleron" + "_top");
    }
	
    @Override
    public IIcon getIcon(int par1, int par2) {
        int var3 = par2 & 12;
        return var3 == 0 && (par1 == 1 || par1 == 0) ? this.top : (var3 == 4 && (par1 == 5 || par1 == 4) ? this.top  : (var3 == 8 && (par1 == 2 || par1 == 3) ? top  : this.side ));
    }
}