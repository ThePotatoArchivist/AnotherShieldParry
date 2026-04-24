package archives.tater.shieldparry.mixin;

import archives.tater.shieldparry.Parrier;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({Hoglin.class, Zoglin.class})
public class HoglinMixin {
    @WrapWithCondition(
            method = "knockback",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/hoglin/HoglinBase;throwTarget(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)V")
    )
    private boolean cancelKnockback(LivingEntity attacker, LivingEntity target) {
        return !(target instanceof Parrier parrier) || !parrier.asp$didParry();
    }
}
