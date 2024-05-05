package divinerpg.items.base;

import divinerpg.DivineRPG;
import divinerpg.capability.*;
import divinerpg.entities.projectile.*;
import divinerpg.enums.BulletType;
import divinerpg.items.arcana.ItemLaVekor;
import divinerpg.items.vanilla.*;
import divinerpg.items.vethea.*;
import divinerpg.registries.EntityRegistry;
import divinerpg.util.LocalizeUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.*;
import net.minecraft.stats.Stats;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.registries.ForgeRegistries;
import javax.annotation.Nullable;
import java.util.List;

public class ItemModRanged extends ItemMod {
    private final Item ammoSupplier;
    private final SoundSource soundCategory;
    protected BulletType bulletType;
    protected final String entityType;
    public SoundEvent sound;
    /**
     * Constructor for DivineRPG ranged weapons.
     *
     * @param entityType - entity type
     * @param bulletType - type of bullets. Has lower priority than entityType param
     * @param sound - what sound will be on bullet spawn
     * @param uses - max usage of item
     * @param delay - weapon's cooldown
     * @param ammo - ammo for weapon. If it returns null, no ammo required
     * @param arcanaConsuming - arcana consuming per shot. Pass 0 to not consume at all
     */
    public ItemModRanged(Rarity rarity, String entityType, BulletType bulletType, SoundEvent sound, int uses, int delay, Item ammo, int arcanaConsuming) {
        super(new Properties().durability(uses).rarity(rarity));
        ammoSupplier = ammo;
        arcanaConsumedUse = arcanaConsuming;
        this.bulletType = bulletType;
        cooldown = delay;
        this.entityType = entityType;
        this.sound = sound;
        soundCategory = SoundSource.PLAYERS;
    }
    public ItemModRanged(String entityType, BulletType bulletType, SoundEvent sound, int uses, int delay, Item ammo, int arcanaConsuming) {
        super(new Properties().durability(uses));
        ammoSupplier = ammo;
        arcanaConsumedUse = arcanaConsuming;
        this.bulletType = bulletType;
        cooldown = delay;
        this.entityType = entityType;
        this.sound = sound;
        soundCategory = SoundSource.PLAYERS;
    }
    //Throwables (no durability)
    public ItemModRanged(Rarity rarity, BulletType bulletType, int delay) {
        super(new Properties().rarity(rarity));
        ammoSupplier = null;
        arcanaConsumedUse = 0;
        this.bulletType = bulletType;
        cooldown = delay;
        entityType = null;
        sound = SoundEvents.ARROW_SHOOT;
        soundCategory = SoundSource.PLAYERS;
    }
    //No rarity, specified arcana usage
    public ItemModRanged(BulletType bulletType, SoundEvent sound, Item ammoSupplier, int uses, int delay, int arcanaConsuming) {this(null, bulletType, sound, uses, delay, ammoSupplier, arcanaConsuming);}
    //No rarity, specified ammo
    public ItemModRanged(BulletType bulletType, SoundEvent sound, Item ammoSupplier, int uses, int delay) {this(null, bulletType, sound, uses, delay, ammoSupplier, 0);}
    //No rarity, no ammo
    public ItemModRanged(BulletType bulletType, SoundEvent sound, int uses, int delay) {this(bulletType, sound, null, uses, delay);}
    @Override public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(canUseRangedWeapon(player, stack)) {
            InteractionResultHolder<ItemStack> ammo = tryFindAmmo(player);
            InteractionResultHolder<Arcana> checkArcana = tryCheckArcana(player);
            if(ammo.getResult() == InteractionResult.SUCCESS && checkArcana.getResult() == InteractionResult.SUCCESS) {
                doPreUsageEffects(world, player);
                if(!world.isClientSide) spawnEntity(world, player, stack, bulletType, entityType);
                Arcana arcana = checkArcana.getObject();
                if(arcana != null) arcana.consume(player, arcanaConsumedUse);
                ItemStack ammoStack = ammo.getObject();
                if(ammoStack != null) ammoStack.shrink(1);
                if(!player.isCreative()) stack.hurtAndBreak(1, player, (ctx) -> ctx.broadcastBreakEvent(player.getUsedItemHand()));
                player.getCooldowns().addCooldown(this, cooldown);
                player.awardStat(Stats.ITEM_USED.get(this));
                doPostUsageEffects(world, player);
                if(this instanceof ItemModThrowable || this instanceof ItemVetheanDisk) {
                    world.playSound(null, player.blockPosition(), sound != null ? sound : SoundEvents.ARROW_SHOOT, soundCategory != null ? soundCategory : SoundSource.PLAYERS, .5F, .4F / (player.getRandom().nextFloat() * .4F + .8F));
                    return InteractionResultHolder.success(stack);
                } world.playSound(null, player.blockPosition(), sound != null ? sound : SoundEvents.ARROW_SHOOT, soundCategory != null ? soundCategory : SoundSource.PLAYERS, 1, 1);
                return InteractionResultHolder.consume(stack);
            }
        } return InteractionResultHolder.pass(stack);
    }
    private boolean needsAmmo() {return ammoSupplier != null;}
    private boolean isAmmo(@Nullable ItemStack stack) {return stack != null && stack.getItem() == ammoSupplier;}
    private ItemStack findAmmo(Player player) {
        if(this.isAmmo(player.getItemInHand(InteractionHand.OFF_HAND))) return player.getItemInHand(InteractionHand.OFF_HAND);
        else if(this.isAmmo(player.getItemInHand(InteractionHand.MAIN_HAND))) return player.getItemInHand(InteractionHand.MAIN_HAND);
        else {
            for(int i = 0; i < player.inventory.getContainerSize(); ++i) {
                ItemStack itemstack = player.inventory.getItem(i);
                if(this.isAmmo(itemstack)) return itemstack;
            } return null;
        }
    }
    /*
     Trying to find ammo stack.
     If SUCCESS, can continue.
     If ItemStack is not null, we need to shrink it.
     */
    protected InteractionResultHolder<ItemStack> tryFindAmmo(Player player) {
        ItemStack stack = null;
        InteractionResult result = InteractionResult.SUCCESS;
        if(!player.isCreative() && needsAmmo()) {
            stack = findAmmo(player);
            if(stack == null || stack.getCount() < 1) result = InteractionResult.FAIL;
        } return new InteractionResultHolder<>(result, stack);
    }
    //Trying to get capability and check if we have enough arcana.
    protected InteractionResultHolder<Arcana> tryCheckArcana(Player player) {
        Arcana arcana = null;
        InteractionResult result = InteractionResult.SUCCESS;
        if(this.arcanaConsumedUse > 0) {
            arcana = player.getCapability(ArcanaProvider.ARCANA).orElseThrow(RuntimeException::new);
            if(arcana == null || arcana.getArcana() < this.arcanaConsumedUse) result = InteractionResult.FAIL;
        } return new InteractionResultHolder<>(result, arcana);
    }
    //Trying to detect if we can use the item.
    protected boolean canUseRangedWeapon(Player player, ItemStack stack) {return (player.isCreative() || stack.getMaxDamage() <= 0 || stack.getDamageValue() < stack.getMaxDamage());}
    protected void spawnEntity(Level world, Player player, ItemStack stack, BulletType bulletType, String entityType) {
        ThrowableProjectile bullet;
        //Class has the most priority
        if(entityType != null) {
            try{bullet = (ThrowableProjectile)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(DivineRPG.MODID, entityType)).create(world);}
            catch(Exception e) {
                e.printStackTrace();
                //Weapon will not work, so it would be better to crush
                throw new RuntimeException(e.getMessage());
            }
        }
        //In other cases we look to a BulletType field
        else if(bulletType.getParticle() != ParticleTypes.BUBBLE) bullet = new EntityParticleBullet(EntityRegistry.PARTICLE_BULLET.get(), world, player, bulletType);
        else if(bulletType.getRed() != 0 && bulletType.getGreen() != 0 && bulletType.getBlue() != 0) bullet = new EntityColoredBullet(EntityRegistry.COLORED_BULLET.get(), player, world, bulletType);
        else bullet = new EntityShooterBullet(EntityRegistry.SHOOTER_BULLET.get(), player, world, bulletType);
        bullet.shootFromRotation(player, player.xRot, player.yRot, 0, 1.5F, .5F);
        world.addFreshEntity(bullet);
    }
    protected void doPreUsageEffects(Level world, Player player) {}
    protected void doPostUsageEffects(Level world, Player player) {}
    @OnlyIn(Dist.CLIENT)
    @Override public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if(bulletType != null && bulletType.getDamage() != 0 && !(this instanceof ItemStaff) && !(this instanceof ItemLaVekor)
            && !(this instanceof ItemScythe) && !(this instanceof ItemVetheanDisk) && !(this instanceof ItemModThrowable)
            && !(this instanceof ItemModShotgun) && !(this instanceof ItemSerenadeOfDeath)) tooltip.add(LocalizeUtils.rangedDam((int)bulletType.getDamage()));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if(!(this instanceof ItemModThrowable) && !(this instanceof ItemVetheanDisk)) {
            tooltip.add(needsAmmo() ? LocalizeUtils.ammo(ammoSupplier) : LocalizeUtils.infiniteAmmo());
            if(!canBeDepleted()) stack.getOrCreateTag().putBoolean("Unbreakable", true);
        }
    }
}