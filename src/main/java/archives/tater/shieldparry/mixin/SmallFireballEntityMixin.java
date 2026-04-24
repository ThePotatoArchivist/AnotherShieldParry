package archives.tater.shieldparry.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;

@Mixin(SmallFireball.class)
public class SmallFireballEntityMixin {
    @WrapOperation(
            method = "onHitEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSources;fireball(Lnet/minecraft/world/entity/projectile/hurtingprojectile/Fireball;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/damagesource/DamageSource;")
    )
    private DamageSource deflectedFireball(DamageSources instance, Fireball source, Entity attacker, Operation<DamageSource> original) {
        return attacker == ((ProjectileEntityAccessor) this).getLastDeflectedBy() ? instance.thrown(source, attacker) : original.call(instance, source, attacker);
    }
}
