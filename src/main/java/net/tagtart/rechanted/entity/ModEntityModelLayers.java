package net.tagtart.rechanted.entity;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tagtart.rechanted.Rechanted;

public class ModEntityModelLayers {

    public static final ModelLayerLocation TROPHY_BOOK =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "trophy_book"),
                    "main"
            );
}
