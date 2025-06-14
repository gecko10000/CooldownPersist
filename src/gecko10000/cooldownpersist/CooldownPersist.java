package gecko10000.cooldownpersist;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CooldownPersist extends JavaPlugin {

    private static final long GC_INTERVAL = 20 * 60 * 10; // 10 minutes

    public final Map<UUID, Map<NamespacedKey, Integer>> allCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        new Listeners(this);
        this.getServer().getScheduler().runTaskTimer(this, this::gc, GC_INTERVAL, GC_INTERVAL);
    }

    private void gc() {
        int currentTick = this.getServer().getCurrentTick();
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
