package com.treekicker4.enchantmenttransfer;

import com.mojang.logging.LogUtils;
import com.treekicker4.enchantmenttransfer.core.EnchantmentTransferConfig;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.Map;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EnchantmentTransfer.MODID)
public class EnchantmentTransfer
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "enchantmenttransfer";

    public EnchantmentTransfer()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EnchantmentTransferConfig.SPEC, "enchantmenttransfer-common.toml");

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
    }

    public static int transferCost(Map<Enchantment, Integer> enchantments) {

        if (EnchantmentTransferConfig.fixed_value.get() != 1000) {
            return Math.max(EnchantmentTransferConfig.fixed_value.get(),1);
        }

        int totalXPCost = 0;
        totalXPCost = (int) (totalXPCost * EnchantmentTransferConfig.factor_value.get());
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            if (entry.getKey() != null) {
                //add 1 to totalXPCost for each enchantment
                totalXPCost++;

                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();

                int enchantmentRarityCost;
                switch (enchantment.getRarity()) {
                    default:
                        enchantmentRarityCost = 1;
                    case COMMON:
                        enchantmentRarityCost = 1;
                        break;
                    case UNCOMMON:
                        enchantmentRarityCost = 2;
                        break;
                    case RARE:
                        enchantmentRarityCost = 3;
                        break;
                    case VERY_RARE:
                        enchantmentRarityCost = 4;
                }

                totalXPCost += enchantmentRarityCost * level;
            }
        }

        double factor_value = EnchantmentTransferConfig.factor_value.get();
        if (factor_value > 0.0) {
            return (int) Math.round(totalXPCost * factor_value);
        } else {
            return 1;
        }
    }
    public static int transferCost() {
        if (EnchantmentTransferConfig.fixed_value.get() != 1000) {
            return Math.max(EnchantmentTransferConfig.fixed_value.get(),1);
        }
        double factor_value = EnchantmentTransferConfig.factor_value.get();
        if (factor_value > 0.0) {
            return (int) Math.round(factor_value);
        } else {
            return 1;
        }
    }
}
