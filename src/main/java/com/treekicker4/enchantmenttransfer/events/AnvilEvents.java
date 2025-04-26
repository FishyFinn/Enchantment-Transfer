package com.treekicker4.enchantmenttransfer.events;
import com.treekicker4.enchantmenttransfer.EnchantmentTransfer;
import com.treekicker4.enchantmenttransfer.core.EnchantmentTransferConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = EnchantmentTransfer.MODID)
public class AnvilEvents {
    @SubscribeEvent
    public static void giveEnchantedBook(AnvilUpdateEvent event) {
        //If player is trying to move enchants from item to empty book
        if (event.getLeft().isEnchanted() && event.getRight().getItem() == Items.BOOK) {
            Map<Enchantment, Integer> leftItemEnchantments = EnchantmentHelper.getEnchantments(event.getLeft());
            Map<Enchantment, Integer> finalBookEnchantments = new HashMap<>();
            ItemStack finalBook = new ItemStack(Items.ENCHANTED_BOOK);

            int enchantmentLimit = EnchantmentTransferConfig.limit_value.get();
            if (leftItemEnchantments.size() > enchantmentLimit) {
                int count = 0;
                for (Map.Entry<Enchantment, Integer> originalEnchantmentEntry : leftItemEnchantments.entrySet()) {
                    if (count >= enchantmentLimit) {
                        break;
                    }
                    finalBookEnchantments.put(originalEnchantmentEntry.getKey(), originalEnchantmentEntry.getValue());
                    count++;
                }
            } else {
                EnchantmentHelper.setEnchantments(finalBookEnchantments, finalBook);
            }
            event.setCost(EnchantmentTransfer.transferCost(leftItemEnchantments));
            event.setOutput(finalBook);
            event.setMaterialCost(1);
            return;
        }

        //If player is trying to move 1 enchant from enchanted to a blank book
        if (event.getLeft().getItem() == Items.ENCHANTED_BOOK && event.getRight().getItem() == Items.BOOK) {
            Map<Enchantment, Integer> bookEnchantments = EnchantmentHelper.getEnchantments(event.getLeft());

            Map<Enchantment, Integer> finalBookEnchantments = new HashMap<>();
            ItemStack finalBook = new ItemStack(Items.ENCHANTED_BOOK);

            //add the first enchantment from the entry to the finalBookEnchantments
            if (EnchantmentTransferConfig.limit_value.get() > 0 && !bookEnchantments.isEmpty()) {
                Map.Entry<Enchantment, Integer> firstEntry = bookEnchantments.entrySet().iterator().next();
                finalBookEnchantments.put(firstEntry.getKey(), firstEntry.getValue());
            }

            EnchantmentHelper.setEnchantments(finalBookEnchantments, finalBook);
            event.setCost(EnchantmentTransfer.transferCost());
            event.setOutput(finalBook);
            event.setMaterialCost(1);
            return;
        }


    }

    @SubscribeEvent
    public static void giveItemBack(AnvilRepairEvent event) {
        //If player is trying to move enchants from item to empty book
        if (event.getLeft().isEnchanted() && event.getRight().getItem() == Items.BOOK) {
            if (EnchantmentTransferConfig.return_value.get() != 0) {
                ItemStack disenchanted = event.getLeft().copy();
                EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(event.getRight()), disenchanted);
                if (EnchantmentTransferConfig.fixed_value.get() == 0) {
                    event.getEntity().giveExperienceLevels(1);
                }
                if(!event.getEntity().getInventory().add(disenchanted))
                {
                    event.getEntity().drop(disenchanted, false);
                }
            }
        }
        //If player is trying to move 1 enchant from enchanted to a blank book
        if (event.getLeft().getItem() == Items.ENCHANTED_BOOK && event.getRight().getItem() == Items.BOOK) {
            if (EnchantmentTransferConfig.return_value.get() != 0) {
                ItemStack originalBook = event.getLeft().copy();
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(originalBook);
                Map.Entry<Enchantment, Integer> firstEnchantment = EnchantmentHelper.getEnchantments(originalBook).entrySet().iterator().next();
                enchantments.remove(firstEnchantment.getKey(), firstEnchantment.getValue());

                if (EnchantmentTransferConfig.fixed_value.get() == 0) {
                    event.getEntity().giveExperienceLevels(1);
                }
                if(!event.getEntity().getInventory().add(originalBook))
                {
                    event.getEntity().drop(originalBook, false);
                }
            }
        }
    }
}
