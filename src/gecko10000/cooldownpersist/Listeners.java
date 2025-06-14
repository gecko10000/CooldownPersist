package gecko10000.cooldownpersist;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import io.papermc.paper.event.player.PlayerItemGroupCooldownEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Listeners implements Listener {

    private final CooldownPersist plugin;

    public Listeners(CooldownPersist plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onUseItem(PlayerItemGroupCooldownEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int endTick = plugin.getServer().getCurrentTick() + event.getCooldown();
        plugin.allCooldowns.computeIfAbsent(uuid, u -> new HashMap<>()).put(event.getCooldownGroup(), endTick);
    }

    @EventHandler
    private void onAdd(EntityAddToWorldEvent event) {
        if (!(event.getEntity() instanceof HumanEntity player)) return;
        Map<NamespacedKey, Integer> cooldowns = plugin.allCooldowns.get(player.getUniqueId());
        if (cooldowns == null) return;
        int currentTick = plugin.getServer().getCurrentTick();
        cooldowns.forEach((key, endTick) -> {
            int cooldown = endTick - currentTick;
            if (cooldown < 1) return;
            player.setCooldown(key, cooldown);
        });
    }

}
