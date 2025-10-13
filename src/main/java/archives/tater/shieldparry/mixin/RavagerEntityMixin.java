package archives.tater.shieldparry.mixin;

import archives.tater.shieldparry.Parrier;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.RavagerEntity;

@Mixin(RavagerEntity.class)
public class RavagerEntityMixin {
    @WrapWithCondition(
            method = "knockback",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/RavagerEntity;knockBack(Lnet/minecraft/entity/Entity;)V")
    )
    private boolean cancelKnockback(RavagerEntity instance, Entity entity) {
        return !(entity instanceof Parrier parrier) || !parrier.asp$didParry();
    }
}
