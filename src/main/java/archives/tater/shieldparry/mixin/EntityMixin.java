package archives.tater.shieldparry.mixin;

import archives.tater.shieldparry.AnotherShieldParry;
import archives.tater.shieldparry.ParriesAttackComponent;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import static archives.tater.shieldparry.AnotherShieldParry.PARRIES_ATTACK;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    private World world;

    @SuppressWarnings("ConstantValue")
    @ModifyReturnValue(
            method = "getProjectileDeflection",
            at = @At("RETURN")
    )
    private ProjectileDeflection deflectParriedProjectile(ProjectileDeflection original, @Local(argsOnly = true) ProjectileEntity projectile) {
        if (!((Object) this instanceof LivingEntity livingEntity) || !(world instanceof ServerWorld serverWorld)) return original;

        var blockingItem = livingEntity.getBlockingItem();
        if (blockingItem == null) return original;

        var parryComponent = blockingItem.get(PARRIES_ATTACK);
        if (parryComponent == null || !parryComponent.canParry(livingEntity)) return original;

        var blockingComponent = blockingItem.get(DataComponentTypes.BLOCKS_ATTACKS);
        if (blockingComponent == null) return original;

        var damageSource = projectile.getDamageSources().mobProjectile(projectile, projectile.getOwner() instanceof LivingEntity owner ? owner : null);
        if (!AnotherShieldParry.isBlockedByShield(livingEntity, damageSource, blockingComponent)) return original;

        parryComponent.onParry(livingEntity, serverWorld);
        blockingComponent.playBlockSound(serverWorld, livingEntity);

        if ((Object) this instanceof ServerPlayerEntity serverPlayer) {
            Criteria.ENTITY_HURT_PLAYER.trigger(serverPlayer, damageSource, 0f, 0f, true);
        }

        return ParriesAttackComponent.PARRY_PROJECTILE;
    }
}
