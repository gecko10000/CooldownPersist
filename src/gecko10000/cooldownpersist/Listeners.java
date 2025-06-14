package gecko10000.cooldownpersist;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import io.papermc.paper.event.player.PlayerItemGroupCooldownEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class Listeners implements Listener {

    private static final long GC_INTERVAL = 20 * 60 * 10; // 10 minutes

    private final CooldownPersist plugin;
    // Stores end ticks of the cooldowns.
    private final Map<UUID, Map<NamespacedKey, Integer>> allCooldowns = new HashMap<>();

    public Listeners(CooldownPersist plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::gc, GC_INTERVAL, GC_INTERVAL);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onUseItem(PlayerItemGroupCooldownEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int endTick = plugin.getServer().getCurrentTick() + event.getCooldown();
        allCooldowns.computeIfAbsent(uuid, u -> new HashMap<>()).put(event.getCooldownGroup(), endTick);
    }

    @EventHandler
    private void onAdd(EntityAddToWorldEvent event) {
        if (!(event.getEntity() instanceof HumanEntity player)) return;
        Map<NamespacedKey, Integer> cooldowns = allCooldowns.get(player.getUniqueId());
        if (cooldowns == null) return;
        int currentTick = plugin.getServer().getCurrentTick();
        cooldowns.forEach((key, endTick) -> {
            int cooldown = endTick - currentTick;
            if (cooldown < 1) return;
            player.setCooldown(key, cooldown);
        });
    }

    private void gc() {
        int currentTick = plugin.getServer().getCurrentTick();
        Iterator<UUID> uuidIterator = allCooldowns.keySet().iterator();
        while (uuidIterator.hasNext()) {
            UUID uuid = uuidIterator.next();
            // Remove individual expired cooldowns
            Map<NamespacedKey, Integer> cooldowns = allCooldowns.get(uuid);
            Iterator<Map.Entry<NamespacedKey, Integer>> cooldownIterator = cooldowns.entrySet().iterator();
            while (cooldownIterator.hasNext()) {
                Map.Entry<NamespacedKey, Integer> cooldown = cooldownIterator.next();
                if (currentTick <= cooldown.getValue()) continue;
                //plugin.getLogger().info("Removing " + cooldown.getKey());
                cooldownIterator.remove();
            }
            // Remove empty cooldown maps
            if (cooldowns.isEmpty()) {
                //plugin.getLogger().info("Removing " + uuid);
                uuidIterator.remove();
            }
        }
    }

}
