package me.mataxeplay.terezkycrewcore;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;

public class VoidSpawnTeleport implements Listener {

	private Plugin plugin = Main.getPlugin(Main.class);

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		// System.out.println("entity damage event: " + event.getEntity().getName());

		if(!(event.getEntity() instanceof Player))
			return;

		Player player = (Player) event.getEntity();

		// System.out.println("player damage event: " + player.getName());

		if(event.getCause() == DamageCause.VOID && player.getWorld().getName().equals(plugin.getConfig().getString("spawn.world"))) {
			// player.performCommand("spawn");
			event.setCancelled(true);
			player.teleport(player.getWorld().getSpawnLocation());
		}
	}
}
