package com.treekicker4.enchantmenttransfer.events;
import com.treekicker4.enchantmenttransfer.EnchantmentTransfer;
import com.treekicker4.enchantmenttransfer.core.EnchantmentTransferConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
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


@Mod.EventBusSubscriber(modid = EnchantmentTransfer.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnvilEvents {
    @SubscribeEvent
    public static void giveEnchantedBook(AnvilUpdateEvent event) {
        ItemStack leftItem = event.getLeft();
        ItemStack rightItem = event.getRight();
        if(leftItem.isEmpty() || rightItem.isEmpty()) {return;}
        if(rightItem.isEnchanted()) {return;}

        //If player is trying to move enchants from item to empty book
        if (leftItem.getItem() != Items.ENCHANTED_BOOK && leftItem.isEnchanted() && rightItem.getItem() == Items.BOOK) {
            Map<Enchantment, Integer> leftItemEnchantments = EnchantmentHelper.getEnchantments(leftItem);
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
                finalBookEnchantments = leftItemEnchantments;
                EnchantmentHelper.setEnchantments(finalBookEnchantments, finalBook);
            }
            event.setCost(EnchantmentTransfer.transferCost(leftItemEnchantments));
            event.setOutput(finalBook);
            event.setMaterialCost(1);
            return;
        } else if (leftItem.getItem() == Items.ENCHANTED_BOOK && rightItem.getItem() == Items.BOOK) {
            Map<Enchantment, Integer> bookEnchantments = EnchantmentHelper.getEnchantments(leftItem);
            if (bookEnchantments.isEmpty() || bookEnchantments.size() == 1) {return;}
            Map<Enchantment, Integer> finalBookEnchantments = new HashMap<>();
            ItemStack finalBook = new ItemStack(Items.ENCHANTED_BOOK);

            //add the first enchantment from the entry to the finalBookEnchantments
            if (EnchantmentTransferConfig.limit_value.get() > 0) {
                Map.Entry<Enchantment, Integer> firstEntry = bookEnchantments.entrySet().iterator().next();
                finalBookEnchantments.put(firstEntry.getKey(), firstEntry.getValue());

                EnchantmentHelper.setEnchantments(finalBookEnchantments, finalBook);
                event.setCost(EnchantmentTransfer.transferCost());
                event.setOutput(finalBook);
                event.setMaterialCost(1);
            }
            return;
        }
        if (leftItem.isStackable()){
            event.setOutput(ItemStack.EMPTY);
            event.setCost(0);
            event.setMaterialCost(0);
            event.getPlayer().sendSystemMessage(Component.literal("Enchantment Transfer: ").append("This item cannot be enchanted!"));
            event.setCanceled(true);
            return;
        }
        event.getPlayer().sendSystemMessage(Component.literal("Enchantment Transfer: ").append(leftItem.getHighlightTip(leftItem.getHoverName())));
        return;
    }

    @SubscribeEvent
    public static void giveItemBack(AnvilRepairEvent event) {
        if (EnchantmentTransferConfig.return_value.get() == 0) {return;}
        ItemStack leftItem = event.getLeft();
        ItemStack rightItem = event.getRight();
        Player player = event.getEntity();
        if( leftItem.isEmpty() || rightItem.isEmpty()) {return;}

        //If player is trying to move enchants from item to empty book
        if (leftItem.isEnchanted() && leftItem.getItem() != Items.ENCHANTED_BOOK && rightItem.getItem() == Items.BOOK &! rightItem.isEnchanted()) {
            ItemStack returnItem = leftItem.copy();
            EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(rightItem), returnItem);

            if (EnchantmentTransferConfig.fixed_value.get() == 0) {
                player.giveExperienceLevels(1);
            }
            if(!player.getInventory().add(returnItem))
            {
                player.drop(returnItem, false);
            }
            return;
        }
        //If player is trying to move 1 enchant from enchanted to a blank book
        if (leftItem.getItem() == Items.ENCHANTED_BOOK && rightItem.getItem() == Items.BOOK) {
            ItemStack returnItem = new ItemStack(Items.ENCHANTED_BOOK);
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(leftItem);
            if(enchantments.size()<=1) {return;}

            Map.Entry<Enchantment, Integer> firstEnchantment = enchantments.entrySet().iterator().next();
            enchantments.remove(firstEnchantment.getKey());

            EnchantmentHelper.setEnchantments(enchantments, returnItem);

            if (EnchantmentTransferConfig.fixed_value.get() == 0) {
                player.giveExperienceLevels(1);
            }

            if (!player.getInventory().add(returnItem)) {
                player.drop(returnItem, false);
            }
        }
    }
}
