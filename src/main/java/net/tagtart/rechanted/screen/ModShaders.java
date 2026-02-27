package net.tagtart.rechanted.screen;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.tagtart.rechanted.Rechanted;

import javax.annotation.Nullable;
import java.io.IOException;

@EventBusSubscriber(modid = Rechanted.MOD_ID, value = Dist.CLIENT)
public class ModShaders {

    public static ShaderInstance CLONED_ITEM_SHADER;
    public static ShaderInstance ENCHANT_TABLE_FBM_LINE_SHADER;
    public static ShaderInstance ENCHANT_TABLE_LOOT_POOL_GRID_SHADER;

    @SubscribeEvent
    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "shaders/program/cloned_item_shader"),
                        DefaultVertexFormat.NEW_ENTITY),
                shader -> CLONED_ITEM_SHADER = shader
        );

        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "shaders/program/ench_table_line_shader"),
                        DefaultVertexFormat.POSITION_TEX),
                shader -> ENCHANT_TABLE_FBM_LINE_SHADER = shader
        );

        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(Rechanted.MOD_ID, "shaders/program/ench_table_grid_shader"),
                        DefaultVertexFormat.POSITION_TEX),
                shader -> ENCHANT_TABLE_LOOT_POOL_GRID_SHADER = shader
        );
    }


}
