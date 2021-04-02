package me.mataxeplay.terezkycrewcore;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Troll implements Listener {
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();
		ItemStack item = event.getItem();

		if(!player.hasPermission("tc.troll.arrow"))
			return;

		if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			if(item != null && item.getType() == Material.ARROW) {
				// player.sendMessage("You have right click an arrow!");

				double force = 3.0;
				double randomize = 0.15;

				for(int i = 0; i < 10; i++) {
					Vector direction = player.getLocation().getDirection();
					direction.add(new Vector((Math.random() - 0.5) * randomize, (Math.random() - 0.5) * randomize, (Math.random() - 0.5) * randomize));
					direction.multiply(force);
					Arrow arrow = player.launchProjectile(Arrow.class, direction);
					arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
				}
			}
		}
	}
}
