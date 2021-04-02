package me.mataxeplay.terezkycrewcore;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ParkourPlayer {
	private Player player;
	private long timestamp;
	private ItemStack[] inventory;
	private ItemStack[] armor;

	public ParkourPlayer(Player player) {
		this.player = player;
		this.reset();
	}

	public void saveInventory() {
		inventory = player.getInventory().getContents();
		armor = player.getInventory().getArmorContents();

		this.clearInventory();
		this.clearArmor();
	}

	public void restoreInventory() {
		if(inventory != null)
			player.getInventory().setContents(inventory);
		else
			this.clearInventory();

		if(armor != null)
			player.getInventory().setArmorContents(armor);
		else
			this.clearArmor();
	}

	private void clearInventory() {
		player.getInventory().clear();
	}

	private void clearArmor() {
		player.getInventory().setHelmet(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setBoots(null);
	}

	public void reset() {
		this.timestamp = System.currentTimeMillis();
	}

	public Player getPlayer() {
		return this.player;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public String getDuration() {
		long delta = System.currentTimeMillis() - this.timestamp + (long) (Math.random() * 10.0);
		double ms = Math.floor((delta /= 1L) % 1000L);
		double s = Math.floor((delta /= 1000L) % 60L);
		double m = Math.floor((delta /= 60L));

		return String.format("%02.0f", m) + ":" + String.format("%02.0f", s) + ":" + String.format("%03.0f", ms);
	}

	public boolean isInRegion(Location pos1, Location pos2) {
		Location target = this.player.getLocation();
		double x1 = Math.min(pos1.getX(), pos2.getX());
		double y1 = Math.min(pos1.getY(), pos2.getY());
		double z1 = Math.min(pos1.getZ(), pos2.getZ());

		double x2 = Math.max(pos2.getX(), pos1.getX());
		double y2 = Math.max(pos2.getY(), pos1.getY());
		double z2 = Math.max(pos2.getZ(), pos1.getZ());

		double x = target.getX();
		double y = target.getY();
		double z = target.getZ();

		boolean isX = x > x1 && x < x2;
		boolean isY = y > y1 && y < y2;
		boolean isZ = z > z1 && z < z2;

		return isX && isY && isZ;
	}

	public boolean isInRegionY(Location pos1, Location pos2) {
		Location target = this.player.getLocation();
		double y1 = Math.min(pos1.getY(), pos2.getY());
		double y2 = Math.max(pos2.getY(), pos1.getY());
		double y = target.getY();
		boolean isY = y > y1 && y < y2;

		return isY;
	}

	public void sendActionBar(String message) {
		this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
	}
}
