package gecko10000.cooldownpersist;

import org.bukkit.plugin.java.JavaPlugin;

public class CooldownPersist extends JavaPlugin {

    @Override
    public void onEnable() {
        new Listeners(this);
    }
}
