package net.tagtart.rechantment;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.tagtart.rechantment.attachments.ModAttachments;
import net.tagtart.rechantment.block.ModBlocks;
import net.tagtart.rechantment.block.entity.ModBlockEntities;
import net.tagtart.rechantment.block.renderer.RechantmentTableRenderer;
import net.tagtart.rechantment.component.ModDataComponents;
import net.tagtart.rechantment.command.ModCommands;
import net.tagtart.rechantment.config.RechantmentCommonConfigs;
import net.tagtart.rechantment.effect.ModEffects;
import net.tagtart.rechantment.enchantment.ModEnchantmentEffects;
import net.tagtart.rechantment.enchantment.ModEnchantments;
import net.tagtart.rechantment.entity.ModEntities;
import net.tagtart.rechantment.entity.renderer.ReturnGemBeamEntityRenderer;
import net.tagtart.rechantment.item.ModCreativeModeTabs;
import net.tagtart.rechantment.item.ModItemProperties;
import net.tagtart.rechantment.item.ModItems;
import net.tagtart.rechantment.loot.ModLootModifiers;
import net.tagtart.rechantment.screen.ModMenuTypes;
import net.tagtart.rechantment.screen.RechantmentTablePoolDisplayScreen;
import net.tagtart.rechantment.screen.RechantmentTableScreen;
import net.tagtart.rechantment.sound.ModSounds;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Rechantment.MOD_ID)
public class Rechantment {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "rechantment";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod
    // is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and
    // pass them in automatically.
    public Rechantment(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in
        // this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(ModCommands::register);

        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModSounds.register(modEventBus);
        ModEffects.register(modEventBus);

        ModCreativeModeTabs.register(modEventBus);
        ModEnchantmentEffects.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        // Register our mod's ModConfigSpec so that FML can create and load the config
        // file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, RechantmentCommonConfigs.SPEC);

        // Register the config screen to enable the config button in the Mods menu
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

            ModItemProperties.addCustomItemProperties();

        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.RECHANTMENT_TABLE_MENU.get(), RechantmentTableScreen::new);
            event.register(ModMenuTypes.RECHANTMENT_TABLE_POOL_DISPLAY_MENU.get(), RechantmentTablePoolDisplayScreen::new);
        }

        @SubscribeEvent
        public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.RETURN_GEM_BEAM_ENTITY.get(), ReturnGemBeamEntityRenderer::new);
            event.registerEntityRenderer(ModEntities.THROWN_WARP_GEM_ENTITY.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(ModEntities.LUCKY_GEM_ENTITY.get(), ThrownItemRenderer::new);

            event.registerBlockEntityRenderer(ModBlockEntities.RECHANTMENT_TABLE_BE.get(), RechantmentTableRenderer::new);
        }
    }
}
