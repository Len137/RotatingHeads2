package cz.gennario.newrotatingheads.newstuff;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

public class Cooldowns implements Listener {

    /*
    This was added because on 1.21.1 any spam of interactions could lag the server
     */

    public static HashMap<UUID, Long> ClickCooldown = new HashMap<>();
    public static final long COOLDOWN_TIME = 500;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ClickCooldown.put(event.getPlayer().getUniqueId(), new Timestamp(System.currentTimeMillis()).getTime() - COOLDOWN_TIME);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        ClickCooldown.remove(event.getPlayer().getUniqueId());
    }
}
