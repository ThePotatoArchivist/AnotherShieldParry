package archives.tater.shieldparry.mixin;

import archives.tater.shieldparry.Parrier;
import archives.tater.shieldparry.ParriesAttackComponent;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

import static archives.tater.shieldparry.AnotherShieldParry.PARRIES_ATTACK;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements Parrier {
    @Unique
    private boolean didParry = false;

    @ModifyExpressionValue(
            method = "getBlockingItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/BlocksAttacksComponent;getBlockDelayTicks()I")
    )
    private int removeBlockDelay(int original) {
        return 0;
    }

    @Inject(
            method = "getDamageBlockedAmount",
            at = @At("RETURN")
    )
    private void resetParry(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        didParry = false;
    }

    @Inject(
            method = "getDamageBlockedAmount",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;")
    )
    private void checkParry(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Float> cir, @Local ItemStack blockingItem, @Share("parryComponent") LocalRef<ParriesAttackComponent> parryComponent, @Share("parried") LocalBooleanRef parried) {
        var parry = blockingItem.get(PARRIES_ATTACK);
        if (parry == null) return;
        parryComponent.set(parry);
        var isParry = parry.canParry((LivingEntity) (Object) this);
        parried.set(isParry);
        didParry = isParry;
    }

    @ModifyExpressionValue(
            method = "getDamageBlockedAmount",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/BlocksAttacksComponent;getDamageReductionAmount(Lnet/minecraft/entity/damage/DamageSource;FD)F")
    )
    private float modifyDamage(float original, @Share("parryComponent") LocalRef<ParriesAttackComponent> parryComponent, @Share("parried") LocalBooleanRef parried) {
        return parried.get() || parryComponent.get() == null ? original : parryComponent.get().failedBlockReduction() * original;
    }

    @Inject(
            method = "getDamageBlockedAmount",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/BlocksAttacksComponent;onShieldHit(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;F)V")
    )
    private void componentOnParry(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Float> cir, @Share("parryComponent") LocalRef<ParriesAttackComponent> parryComponent, @Share("parried") LocalBooleanRef parried) {
        if (parried.get() && parryComponent.get() != null) {
            parryComponent.get().onParry((LivingEntity) (Object) this, source.getAttacker() instanceof LivingEntity livingEntity ? livingEntity : null, world);
        }
    }

    @WrapWithCondition(
            method = "damage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V")
    )
    private boolean preventSelfKnockback(LivingEntity instance, double strength, double x, double z, @Local(ordinal = 0) boolean blocked) {
        return !blocked;
    }

    @Override
    public boolean asp$didParry() {
        return didParry;
    }
}