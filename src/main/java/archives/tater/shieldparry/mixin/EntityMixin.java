package archives.tater.shieldparry.mixin;

import archives.tater.shieldparry.AnotherShieldParry;
import archives.tater.shieldparry.ParriesAttackComponent;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static archives.tater.shieldparry.AnotherShieldParry.PARRIES_ATTACK;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    private Level level;

    @SuppressWarnings("ConstantValue")
    @ModifyReturnValue(
            method = "deflection",
            at = @At("RETURN")
    )
    private ProjectileDeflection deflectParriedProjectile(ProjectileDeflection original, @Local(argsOnly = true) Projectile projectile) {
        if (!((Object) this instanceof LivingEntity livingEntity) || !(level instanceof ServerLevel serverWorld)) return original;

        var blockingItem = livingEntity.getItemBlockingWith();
        if (blockingItem == null) return original;

        var parryComponent = blockingItem.get(PARRIES_ATTACK);
        if (parryComponent == null || !parryComponent.canParry(livingEntity)) return original;

        var blockingComponent = blockingItem.get(DataComponents.BLOCKS_ATTACKS);
        if (blockingComponent == null) return original;

        var damageSource = projectile.damageSources().mobProjectile(projectile, projectile.getOwner() instanceof LivingEntity owner ? owner : null);
        if (!AnotherShieldParry.isBlockedByShield(livingEntity, damageSource, blockingComponent)) return original;

        parryComponent.onParry(livingEntity, serverWorld);
        blockingComponent.onBlocked(serverWorld, livingEntity);

        if ((Object) this instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger(serverPlayer, damageSource, 0f, 0f, true);
        }

        return ParriesAttackComponent.PARRY_PROJECTILE;
    }
}
