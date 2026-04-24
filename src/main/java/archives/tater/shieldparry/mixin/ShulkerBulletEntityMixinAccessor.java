package archives.tater.shieldparry.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import org.jetbrains.annotations.Nullable;

@Mixin(ShulkerBullet.class)
public interface ShulkerBulletEntityMixinAccessor {
    @Accessor
    void setFinalTarget(@Nullable EntityReference<Entity> target);
}
