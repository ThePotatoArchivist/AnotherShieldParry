package archives.tater.shieldparry.mixin;

import archives.tater.shieldparry.Parrier;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ravager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Ravager.class)
public class RavagerEntityMixin {
    @WrapWithCondition(
            method = "blockedByItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Ravager;strongKnockback(Lnet/minecraft/world/entity/Entity;)V")
    )
    private boolean cancelKnockback(Ravager instance, Entity entity) {
        return !(entity instanceof Parrier parrier) || !parrier.asp$didParry();
    }
}
