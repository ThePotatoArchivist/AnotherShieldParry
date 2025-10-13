package archives.tater.shieldparry.mixin;

import archives.tater.shieldparry.Parrier;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.ZoglinEntity;

@Mixin({HoglinEntity.class, ZoglinEntity.class})
public class HoglinMixin {
    @WrapWithCondition(
            method = "knockback",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/Hoglin;knockback(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V")
    )
    private boolean cancelKnockback(LivingEntity attacker, LivingEntity target) {
        return !(target instanceof Parrier parrier) || !parrier.asp$didParry();
    }
}
