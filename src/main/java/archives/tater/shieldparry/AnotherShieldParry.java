package archives.tater.shieldparry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;

import net.minecraft.component.ComponentType;
import net.minecraft.component.type.BlocksAttacksComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static java.lang.Math.acos;

public class AnotherShieldParry implements ModInitializer {
	public static final String MOD_ID = "anothershieldparry";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ComponentType<ParriesAttackComponent> PARRIES_ATTACK = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            id("parries_attack"),
            ComponentType.<ParriesAttackComponent>builder()
                    .codec(ParriesAttackComponent.CODEC)
                    .packetCodec(ParriesAttackComponent.PACKET_CODEC)
                    .cache()
                    .build()
    );

    public static boolean isBlockedByShield(LivingEntity entity, DamageSource source, BlocksAttacksComponent blocksAttackComponent) {
        Vec3d vec3d = source.getPosition();
        var angle = vec3d != null
                ? acos(vec3d
                        .subtract(entity.getEntityPos())
                        .multiply(1, 0, 1)
                        .normalize()
                        .dotProduct(entity.getRotationVector(0.0F, entity.getHeadYaw())))
                : Math.PI;
        return blocksAttackComponent.getDamageReductionAmount(source, 1, angle) > 0;
    }

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(Items.SHIELD, builder -> {
                builder.add(PARRIES_ATTACK, new ParriesAttackComponent(
                        6,
                        0.75f,
                        1.0,
                        10,
                        40,
                        Optional.of(Registries.SOUND_EVENT.getEntry(SoundEvents.ITEM_MACE_SMASH_AIR)) // TODO custom sound event
                ));
            });
        });
	}
}