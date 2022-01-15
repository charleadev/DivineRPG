package divinerpg.entities.vanilla.overworld;

import divinerpg.entities.IAttackTimer;
import divinerpg.entities.base.EntityDivineTameable;
import divinerpg.util.EntityStats;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntitySmelter extends EntityDivineTameable implements IAttackTimer {
    private static final DataParameter<Integer> ATTACK_TIMER = EntityDataManager.defineId(EntitySmelter.class,
            DataSerializers.INT);

    public <T extends Entity> EntitySmelter(EntityType<T> type, World worldIn) {
        super((EntityType<? extends TameableEntity>) type, worldIn);
        setHealth(getMaxHealth());
    }

    protected EntitySmelter(EntityType<? extends TameableEntity> type, World worldIn, PlayerEntity player) {
        super(type, worldIn);
        setHealth(getMaxHealth());
        tame(player);
    }


    protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return 3.2F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ATTACK_TIMER, Integer.valueOf(0));
    }

    @Override
    public void tick() {
        super.tick();
        if (getAttackTimer() > 0) {
            this.entityData.set(ATTACK_TIMER, Integer.valueOf(getAttackTimer() - 1));
        }
    }

    @Override
    public int getAttackTimer() {
        return this.entityData.get(ATTACK_TIMER).intValue();
    }

    public static AttributeModifierMap.MutableAttribute attributes() {
        return MonsterEntity.createMonsterAttributes().add(Attributes.MAX_HEALTH, EntityStats.smelterHealth).add(Attributes.ATTACK_DAMAGE, EntityStats.smelterDamage).add(Attributes.MOVEMENT_SPEED, EntityStats.smelterSpeed).add(Attributes.FOLLOW_RANGE, EntityStats.smelterFollowRange);
    }

    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        if (!this.level.isClientSide) {
            ItemStack itemstack = player.getItemInHand(hand);
            Item item = itemstack.getItem();
            if (this.isTame()) {
                if (item == Items.FLINT && this.getHealth() < this.getMaxHealth()) {
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                    if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                        this.tame(player);
                        this.navigation.recomputePath();
                        this.setTarget((LivingEntity) null);
                        this.level.broadcastEntityEvent(this, (byte) 7);
                        this.heal(4.0F);
                    } else {
                        this.level.broadcastEntityEvent(this, (byte) 6);
                        this.heal(4.0F);
                    }
                } else {
                    tame(player);
                    this.setTame(true);
                }
            }
            return super.mobInteract(player, hand);
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean attack = super.doHurtTarget(entity);
        if (attack) {
            entity.setDeltaMovement(-MathHelper.sin(this.xRot * (float) Math.PI / 180.0F), 0.1D,
                    MathHelper.cos(this.xRot * (float) Math.PI / 180.0F));
            entity.setSecondsOnFire(5);
            this.entityData.set(ATTACK_TIMER, 10);
        }
        return attack;
    }

}