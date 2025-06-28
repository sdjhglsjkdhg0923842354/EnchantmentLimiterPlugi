package com.example.enchantmentlimiter;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;
import java.util.Map;

public class EnchantmentLimiterPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    private boolean isIllegalEnchantment(Enchantment ench, int level) {
        if (ench == Enchantment.THORNS || ench == Enchantment.FIRE_ASPECT) return true;
        if (ench == Enchantment.DAMAGE_ALL && level > 4) return true;
        if (ench == Enchantment.PROTECTION_ENVIRONMENTAL && level > 3) return true;
        return false;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Iterator<Map.Entry<Enchantment, Integer>> it = event.getEnchantsToAdd().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Enchantment, Integer> entry = it.next();
            if (isIllegalEnchantment(entry.getKey(), entry.getValue())) {
                it.remove();
            }
        }
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        if (event.getResult() != null) {
            ItemStack result = event.getResult();
            for (Map.Entry<Enchantment, Integer> entry : result.getEnchantments().entrySet()) {
                if (isIllegalEnchantment(entry.getKey(), entry.getValue())) {
                    event.setResult(null);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.ANVIL || event.getInventory().getType() == InventoryType.GRINDSTONE) {
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getItemMeta() != null) {
                for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                    if (isIllegalEnchantment(entry.getKey(), entry.getValue())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getCaught() instanceof ItemStack) {
            ItemStack item = (ItemStack) event.getCaught();
            if (item != null && item.getEnchantments() != null) {
                for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                    if (isIllegalEnchantment(entry.getKey(), entry.getValue())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.ENCHANTED_BOOK) {
            for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                if (isIllegalEnchantment(entry.getKey(), entry.getValue())) {
                    player.sendMessage("§cThis enchanted book is not allowed!");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
