package archives.tater.shieldparry;

import archives.tater.shieldparry.mixin.ShulkerBulletEntityMixinAccessor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public record ParriesAttackComponent(
        int parryTicks,
        float failedBlockReduction,
        double knockbackStrength,
        int normalCooldown,
        int disableCooldown,
        Optional<RegistryEntry<SoundEvent>> parrySound
) {

    public boolean canParry(LivingEntity entity) {
        return entity.getItemUseTime() < parryTicks;
    }

    public void onParry(LivingEntity entity, ServerWorld world, LivingEntity attacker) {
        attacker.takeKnockback(knockbackStrength, entity.getX() - attacker.getX(), entity.getZ() - attacker.getZ());
        onParry(entity, world, attacker.getWeaponDisableBlockingForSeconds() > 0f);
    }

    public void onParry(LivingEntity entity, ServerWorld world) {
        onParry(entity, world, false);
    }

    private void onParry(LivingEntity entity, ServerWorld world, boolean disable) {
        playParrySound(world, entity);
        if (entity instanceof ServerPlayerEntity serverPlayer)
            serverPlayer.getItemCooldownManager().set(entity.getActiveItem(), disable ? disableCooldown : normalCooldown);
        entity.clearActiveItem();
    }

    public void playParrySound(World world, LivingEntity from) {
        parrySound.ifPresent(
                sound -> world.playSound(null, from.getX(), from.getY(), from.getZ(), sound, from.getSoundCategory(), 1.0F, 0.8F + world.random.nextFloat() * 0.4F)
        );
    }

    public static final Codec<ParriesAttackComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("parry_ticks").forGetter(ParriesAttackComponent::parryTicks),
            Codec.floatRange(0, 1).fieldOf("failed_block_reduction").forGetter(ParriesAttackComponent::failedBlockReduction),
            Codec.doubleRange(0, Float.MAX_VALUE).fieldOf("knockback_strength").forGetter(ParriesAttackComponent::knockbackStrength),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("normal_cooldown").forGetter(ParriesAttackComponent::normalCooldown),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("disable_cooldown").forGetter(ParriesAttackComponent::disableCooldown),
            SoundEvent.ENTRY_CODEC.optionalFieldOf("parry_sound").forGetter(ParriesAttackComponent::parrySound)
    ).apply(instance, ParriesAttackComponent::new));

    public static final PacketCodec<RegistryByteBuf, ParriesAttackComponent> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ParriesAttackComponent::parryTicks,
            PacketCodecs.FLOAT, ParriesAttackComponent::failedBlockReduction,
            PacketCodecs.DOUBLE, ParriesAttackComponent::knockbackStrength,
            PacketCodecs.INTEGER, ParriesAttackComponent::normalCooldown,
            PacketCodecs.INTEGER, ParriesAttackComponent::disableCooldown,
            PacketCodecs.optional(SoundEvent.ENTRY_PACKET_CODEC), ParriesAttackComponent::parrySound,
            ParriesAttackComponent::new
    );

    public static double getAimedProjectileSpeed(ProjectileEntity projectile) {
        return switch (projectile) {
            case FireballEntity ignored -> 3.0;
            case DragonFireballEntity ignored -> 4.0;
            case AbstractWindChargeEntity ignored -> 1.5;
            default -> 1.0;
        };
    }

    public static float DRAGON_FIREBALL_DAMAGE = 40;

    public static ProjectileDeflection PARRY_PROJECTILE = (projectile, hitEntity, random) -> {
        var owner = projectile.getOwner();
        if (projectile instanceof ExplosiveProjectileEntity && owner instanceof MobEntity && hitEntity != null) {
            projectile.setVelocity(hitEntity.getRotationVector().multiply(getAimedProjectileSpeed(projectile)));
        } else if (projectile instanceof ShulkerBulletEntity && owner instanceof ShulkerEntity) {
            projectile.setVelocity(Vec3d.ZERO);
            ((ShulkerBulletEntityMixinAccessor) projectile).setTarget(LazyEntityReference.of(owner));
        } else
            projectile.setVelocity(projectile.getVelocity().negate());
        if (hitEntity != null) {
            var pickupType = projectile instanceof PersistentProjectileEntity persistentProjectile ? persistentProjectile.pickupType : null;
            projectile.setOwner(hitEntity);
            if (projectile instanceof PersistentProjectileEntity persistentProjectile)
                persistentProjectile.pickupType = pickupType;
        }
        projectile.velocityDirty = true;
    };
}
