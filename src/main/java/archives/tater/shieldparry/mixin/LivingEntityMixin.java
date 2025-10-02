package archives.tater.shieldparry.mixin;

import archives.tater.shieldparry.ParriesAttackComponent;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

import static archives.tater.shieldparry.AnotherShieldParry.PARRIES_ATTACK;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract ItemStack getActiveItem();

    @ModifyExpressionValue(
            method = "getBlockingItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/BlocksAttacksComponent;getBlockDelayTicks()I")
    )
    private int removeBlockDelay(int original) {
        return 0;
    }

    @Inject(
            method = "getDamageBlockedAmount",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;")
    )
    private void checkParry(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Float> cir, @Local ItemStack blockingItem, @Share("parryComponent") LocalRef<ParriesAttackComponent> parryComponent, @Share("parried") LocalBooleanRef parried) {
        var parry = blockingItem.get(PARRIES_ATTACK);
        if (parry == null) return;
        parryComponent.set(parry);
        parried.set(parry.canParry((LivingEntity) (Object) this));
    }

    @ModifyExpressionValue(
            method = "getDamageBlockedAmount",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/BlocksAttacksComponent;getDamageReductionAmount(Lnet/minecraft/entity/damage/DamageSource;FD)F")
    )
    private float modifyDamage(float original, @Share("parryComponent") LocalRef<ParriesAttackComponent> parryComponent, @Share("parried") LocalBooleanRef parried) {
        return parried.get() || parryComponent.get() == null ? original : parryComponent.get().failedBlockReduction() * original;
    }

    @WrapOperation(
            method = "getDamageBlockedAmount",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeShieldHit(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;)V")
    )
    private void changeKnockback(LivingEntity instance, ServerWorld world, LivingEntity attacker, Operation<Void> original, @Share("parryComponent") LocalRef<ParriesAttackComponent> parryComponent, @Share("parried") LocalBooleanRef parried) {
        if (!parried.get() || parryComponent.get() == null) {
            original.call(instance, world, attacker);
            return;
        }
        parryComponent.get().onParry(instance, world, attacker);
    }
}