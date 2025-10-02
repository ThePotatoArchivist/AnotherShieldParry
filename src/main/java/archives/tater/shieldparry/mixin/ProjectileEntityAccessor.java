package archives.tater.shieldparry.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;

@Mixin(ProjectileEntity.class)
public interface ProjectileEntityAccessor {
    @Accessor
    Entity getLastDeflectedEntity();
}
