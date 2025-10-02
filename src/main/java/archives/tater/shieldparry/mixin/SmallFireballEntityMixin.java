package archives.tater.shieldparry.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;

@Mixin(SmallFireballEntity.class)
public class SmallFireballEntityMixin {
    @WrapOperation(
            method = "onEntityHit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSources;fireball(Lnet/minecraft/entity/projectile/AbstractFireballEntity;Lnet/minecraft/entity/Entity;)Lnet/minecraft/entity/damage/DamageSource;")
    )
    private DamageSource deflectedFireball(DamageSources instance, AbstractFireballEntity source, Entity attacker, Operation<DamageSource> original) {
        return attacker == ((ProjectileEntityAccessor) this).getLastDeflectedEntity() ? instance.thrown(source, attacker) : original.call(instance, source, attacker);
    }
}
