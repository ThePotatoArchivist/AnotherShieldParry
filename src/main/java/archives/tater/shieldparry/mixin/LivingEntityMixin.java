package archives.tater.shieldparry.mixin;

import archives.tater.shieldparry.Parrier;
import archives.tater.shieldparry.ParriesAttackComponent;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static archives.tater.shieldparry.AnotherShieldParry.PARRIES_ATTACK;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements Parrier {
    @Unique
    private boolean didParry = false;

    @ModifyExpressionValue(
            method = "getItemBlockingWith",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BlocksAttacks;blockDelayTicks()I")
    )
    private int removeBlockDelay(int original) {
        return 0;
    }

    @Inject(
            method = "applyItemBlocking",
            at = @At("RETURN")
    )
    private void resetParry(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        didParry = false;
    }

    @Inject(
            method = "applyItemBlocking",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;")
    )
    private void checkParry(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Float> cir, @Local ItemStack blockingItem, @Share("parryComponent") LocalRef<ParriesAttackComponent> parryComponent, @Share("parried") LocalBooleanRef parried) {
        var parry = blockingItem.get(PARRIES_ATTACK);
        if (parry == null) return;
        parryComponent.set(parry);
        var isParry = parry.canParry((LivingEntity) (Object) this);
        parried.set(isParry);
        didParry = isParry;
    }

    @ModifyExpressionValue(
            method = "applyItemBlocking",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BlocksAttacks;resolveBlockedDamage(Lnet/minecraft/world/damagesource/DamageSource;FD)F")
    )
    private float modifyDamage(float original, @Share("parryComponent") LocalRef<ParriesAttackComponent> parryComponent, @Share("parried") LocalBooleanRef parried) {
        return parried.get() || parryComponent.get() == null ? original : parryComponent.get().failedBlockReduction() * original;
    }

    @Inject(
            method = "applyItemBlocking",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BlocksAttacks;hurtBlockingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;F)V")
    )
    private void componentOnParry(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Float> cir, @Share("parryComponent") LocalRef<ParriesAttackComponent> parryComponent, @Share("parried") LocalBooleanRef parried) {
        if (parried.get() && parryComponent.get() != null) {
            parryComponent.get().onParry((LivingEntity) (Object) this, source.getEntity() instanceof LivingEntity livingEntity ? livingEntity : null, world);
        }
    }

    @WrapWithCondition(
            method = "hurtServer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V")
    )
    private boolean preventSelfKnockback(LivingEntity instance, double strength, double x, double z, @Local(ordinal = 0) boolean blocked) {
        return !blocked;
    }

    @Override
    public boolean asp$didParry() {
        return didParry;
    }
}