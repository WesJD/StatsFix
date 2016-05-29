package io.github.wesjd.statsfix;

import nl.lolmewn.stats.bukkit.api.event.StatsHolderUpdateEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StatsFix extends JavaPlugin implements Listener {

    private final Material[] ignoredTypes = { Material.DIAMOND_ORE, Material.GOLD_ORE, Material.COAL_ORE, Material.EMERALD_ORE, Material.GLOWING_REDSTONE_ORE,
            Material.REDSTONE_ORE, Material.IRON_ORE, Material.LAPIS_ORE, Material.QUARTZ_ORE };
    private final Queue<UUID> queuedIgnore = new ConcurrentLinkedQueue<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        final Player player = e.getPlayer();
        final Block block = e.getBlock();
        final Material type = block.getType();
        if(Arrays.stream(ignoredTypes).anyMatch(mat -> mat == type)) {
            final ItemStack stack = player.getInventory().getItemInMainHand();
            if(stack.containsEnchantment(Enchantment.SILK_TOUCH)) queuedIgnore.add(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStatUpdate(StatsHolderUpdateEvent e) {
        e.setCancelled(e.getStat().getName().equals("Blocks broken") && queuedIgnore.contains(e.getHolder().getUuid()));
        if(e.isCancelled()) queuedIgnore.remove(e.getHolder().getUuid());
    }

}
