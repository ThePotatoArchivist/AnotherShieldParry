package archives.tater.shieldparry.mixin;

import archives.tater.shieldparry.ParriesAttackComponent;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;

@Mixin(Projectile.class)
public abstract class ProjectileEntityMixin extends Entity {
    @Shadow
    public abstract @Nullable Entity getOwner();

    public ProjectileEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @WrapWithCondition(
            method = "deflect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;setOwner(Lnet/minecraft/world/entity/EntityReference;)V")
    )
    private boolean noSetOwner(Projectile instance, @Nullable EntityReference<Entity> owner, @Local(argsOnly = true)ProjectileDeflection deflection) {
        return deflection != ParriesAttackComponent.PARRY_PROJECTILE;
    }

    @SuppressWarnings("ConstantValue")
    @Inject(
            method = "onHitEntity",
            at = @At("TAIL")
    )
    private void damageDragon(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (level() instanceof ServerLevel serverWorld && (Object) this instanceof DragonFireball && entityHitResult.getEntity() instanceof EnderDragonPart && !(getOwner() instanceof EnderDragon)) {
            entityHitResult.getEntity().hurtServer(serverWorld, damageSources().thrown(this, getOwner()), ParriesAttackComponent.DRAGON_FIREBALL_DAMAGE);
        }
    }
}
