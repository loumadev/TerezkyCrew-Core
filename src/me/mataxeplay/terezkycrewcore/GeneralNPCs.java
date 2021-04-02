package me.mataxeplay.terezkycrewcore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.Plugin;

public class GeneralNPCs implements Listener {

	private Plugin plugin = Main.getPlugin(Main.class);

	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		Entity entity = event.getRightClicked();
		String name = entity.getName();
		boolean isNPC = entity.hasMetadata("NPC");
		FileConfiguration config = plugin.getConfig();

		if(isNPC) {

			if(name.equals(config.getString("spawn.npc.survival"))) {
				player.performCommand("survival");
			}

			event.setCancelled(true);
		}
	}
}
