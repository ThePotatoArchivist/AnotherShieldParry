package archives.tater.shieldparry.mixin;

import archives.tater.shieldparry.ParriesAttackComponent;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {
    @Shadow
    public abstract @Nullable Entity getOwner();

    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapWithCondition(
            method = "deflect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;setOwner(Lnet/minecraft/entity/LazyEntityReference;)V")
    )
    private boolean noSetOwner(ProjectileEntity instance, @Nullable LazyEntityReference<Entity> owner, @Local(argsOnly = true)ProjectileDeflection deflection) {
        return deflection != ParriesAttackComponent.PARRY_PROJECTILE;
    }

    @SuppressWarnings("ConstantValue")
    @Inject(
            method = "onEntityHit",
            at = @At("TAIL")
    )
    private void damageDragon(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (getEntityWorld() instanceof ServerWorld serverWorld && (Object) this instanceof DragonFireballEntity && entityHitResult.getEntity() instanceof EnderDragonPart && !(getOwner() instanceof EnderDragonEntity)) {
            entityHitResult.getEntity().damage(serverWorld, getDamageSources().thrown(this, getOwner()), ParriesAttackComponent.DRAGON_FIREBALL_DAMAGE);
        }
    }
}
