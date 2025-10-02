package archives.tater.shieldparry.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.projectile.ShulkerBulletEntity;

import org.jetbrains.annotations.Nullable;

@Mixin(ShulkerBulletEntity.class)
public interface ShulkerBulletEntityMixinAccessor {
    @Accessor
    void setTarget(@Nullable LazyEntityReference<Entity> target);
}
