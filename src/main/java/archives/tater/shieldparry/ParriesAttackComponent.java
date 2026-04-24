package archives.tater.shieldparry;

import archives.tater.shieldparry.mixin.ShulkerBulletEntityMixinAccessor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.entity.projectile.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ParriesAttackComponent(
        int parryTicks,
        float failedBlockReduction,
        double knockbackStrength,
        int normalCooldown,
        int disableCooldown,
        Optional<Holder<SoundEvent>> parrySound
) {

    public boolean canParry(LivingEntity entity) {
        return entity.getTicksUsingItem() < parryTicks;
    }

    public void onParry(LivingEntity entity, ServerLevel world) { // called by projectiles
        onParry(entity, world, false, true);
    }

    public void onParry(LivingEntity entity, @Nullable LivingEntity attacker, ServerLevel world) {
        if (attacker == null || !attacker.isAlive()) {
            onParry(entity, world, false, false);
            return;
        }
        attacker.knockback(knockbackStrength, entity.getX() - attacker.getX(), entity.getZ() - attacker.getZ());
        onParry(entity, world, attacker.getSecondsToDisableBlocking() > 0f, true);
    }

    private void onParry(LivingEntity entity, ServerLevel world, boolean disable, boolean swingHand) {
        playParrySound(world, entity);
        if (entity instanceof ServerPlayer serverPlayer)
            serverPlayer.getCooldowns().addCooldown(entity.getUseItem(), disable ? disableCooldown : normalCooldown);
        entity.releaseUsingItem();
        if (swingHand)
            entity.swing(entity.getUsedItemHand(), true);
    }

    public void playParrySound(Level world, LivingEntity from) {
        parrySound.ifPresent(
                sound -> world.playSound(null, from.getX(), from.getY(), from.getZ(), sound, from.getSoundSource(), 1.0F, 0.8F + world.random.nextFloat() * 0.4F)
        );
    }

    public static final Codec<ParriesAttackComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("parry_ticks").forGetter(ParriesAttackComponent::parryTicks),
            Codec.floatRange(0, 1).fieldOf("failed_block_reduction").forGetter(ParriesAttackComponent::failedBlockReduction),
            Codec.doubleRange(0, Float.MAX_VALUE).fieldOf("knockback_strength").forGetter(ParriesAttackComponent::knockbackStrength),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("normal_cooldown").forGetter(ParriesAttackComponent::normalCooldown),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("disable_cooldown").forGetter(ParriesAttackComponent::disableCooldown),
            SoundEvent.CODEC.optionalFieldOf("parry_sound").forGetter(ParriesAttackComponent::parrySound)
    ).apply(instance, ParriesAttackComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ParriesAttackComponent> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ParriesAttackComponent::parryTicks,
            ByteBufCodecs.FLOAT, ParriesAttackComponent::failedBlockReduction,
            ByteBufCodecs.DOUBLE, ParriesAttackComponent::knockbackStrength,
            ByteBufCodecs.INT, ParriesAttackComponent::normalCooldown,
            ByteBufCodecs.INT, ParriesAttackComponent::disableCooldown,
            ByteBufCodecs.optional(SoundEvent.STREAM_CODEC), ParriesAttackComponent::parrySound,
            ParriesAttackComponent::new
    );

    public static double getAimedProjectileSpeed(Projectile projectile) {
        return switch (projectile) {
            case LargeFireball ignored -> 3.0;
            case DragonFireball ignored -> 4.0;
            case AbstractWindCharge ignored -> 1.5;
            default -> 1.0;
        };
    }

    public static float DRAGON_FIREBALL_DAMAGE = 40;

    public static ProjectileDeflection PARRY_PROJECTILE = (projectile, hitEntity, random) -> {
        var owner = projectile.getOwner();
        if (projectile instanceof AbstractHurtingProjectile && owner instanceof Mob && hitEntity != null) {
            projectile.setDeltaMovement(hitEntity.getLookAngle().scale(getAimedProjectileSpeed(projectile)));
        } else if (projectile instanceof ShulkerBullet && owner instanceof Shulker) {
            projectile.setDeltaMovement(Vec3.ZERO);
            ((ShulkerBulletEntityMixinAccessor) projectile).setFinalTarget(EntityReference.of(owner));
        } else
            projectile.setDeltaMovement(projectile.getDeltaMovement().reverse());
        if (hitEntity != null) {
            var pickupType = projectile instanceof AbstractArrow persistentProjectile ? persistentProjectile.pickup : null;
            projectile.setOwner(hitEntity);
            if (projectile instanceof AbstractArrow persistentProjectile)
                persistentProjectile.pickup = pickupType;
        }
        projectile.needsSync = true;
    };
}
