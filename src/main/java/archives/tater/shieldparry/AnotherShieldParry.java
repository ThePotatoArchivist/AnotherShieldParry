package archives.tater.shieldparry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static java.lang.Math.acos;

public class AnotherShieldParry implements ModInitializer {
	public static final String MOD_ID = "anothershieldparry";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final DataComponentType<ParriesAttackComponent> PARRIES_ATTACK = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            id("parries_attack"),
            DataComponentType.<ParriesAttackComponent>builder()
                    .persistent(ParriesAttackComponent.CODEC)
                    .networkSynchronized(ParriesAttackComponent.PACKET_CODEC)
                    .cacheEncoding()
                    .build()
    );

    public static boolean isBlockedByShield(LivingEntity entity, DamageSource source, BlocksAttacks blocksAttackComponent) {
        Vec3 vec3d = source.getSourcePosition();
        var angle = vec3d != null
                ? acos(vec3d
                        .subtract(entity.position())
                        .multiply(1, 0, 1)
                        .normalize()
                        .dot(entity.calculateViewVector(0.0F, entity.getYHeadRot())))
                : Math.PI;
        return blocksAttackComponent.resolveBlockedDamage(source, 1, angle) > 0;
    }

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(Items.SHIELD, builder -> {
                builder.set(PARRIES_ATTACK, new ParriesAttackComponent(
                        6,
                        0.75f,
                        1.0,
                        10,
                        40,
                        Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.MACE_SMASH_AIR)) // TODO custom sound event
                ));
            });
        });
	}
}