package me.mataxeplay.terezkycrewcore;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.gmail.filoghost.holographicdisplays.api.Hologram;

public class Parkour implements Listener {
	private Plugin plugin = Main.getPlugin(Main.class);
	private ArrayList<ParkourPlayer> pplayers = new ArrayList<ParkourPlayer>();
	private int interval = -1;

	private World parkourWorld;
	private Location parkourStartPos;
	private Location parkourRegionPos1;
	private Location parkourRegionPos2;

	public Parkour() {
		this.loadConfig();

		Hologram hologram = HologramsAPI.createHologram(plugin, parkourStartPos);
		TextLine textLine = hologram.appendTextLine("A hologram line");
	}

	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		Entity entity = event.getRightClicked();
		String name = entity.getName();
		boolean isNPC = entity.hasMetadata("NPC");
		FileConfiguration config = plugin.getConfig();

		if(isNPC) {

			if(name.equals(config.getString("parkour.npc.start"))) {
				ParkourPlayer added = this.addPlayer(player);
				if(added != null)
					player.sendMessage(this.getMessage("join"));
				else
					player.sendMessage(this.getMessage("already"));
			}

			if(name.equals(config.getString("parkour.npc.end"))) {
				ParkourPlayer removed = this.removePlayer(player);
				if(removed != null)
					this.playerFinished(removed);
				else
					player.sendMessage(this.getMessage("not_joined"));
			}

			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		ParkourPlayer pplayer = this.getParkourPlayer(player);

		if(pplayer == null)
			return;
		if(pplayer.isInRegion(parkourRegionPos1, parkourRegionPos2))
			return;

		if(pplayer.isInRegionY(parkourRegionPos1, parkourRegionPos2)) {
			this.removePlayer(player);
			player.sendMessage(this.getMessage("left"));
		} else {
			pplayer.reset();
			player.teleport(this.parkourStartPos);
			player.sendMessage(this.getMessage("fall"));
		}
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		this.removePlayer(player);
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Player player = event.getEntity();

		this.removePlayer(player);
	}

	public void playerFinished(ParkourPlayer pplayer) {
		Player player = pplayer.getPlayer();
		String duration = pplayer.getDuration();
		String broadcastMessage = this.getMessage("finished_others").replace("{player}", player.getDisplayName()).replace("{time}", duration);
		FileConfiguration config = plugin.getConfig();

		player.sendMessage(this.getMessage("finished").replace("{time}", duration));
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + config.getInt("parkour.reward"));
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2f, 1f);

		for(Player p : parkourWorld.getPlayers()) {
			if(p != player)
				p.sendMessage(broadcastMessage);
		}
	}

	public ParkourPlayer addPlayer(Player player) {
		if(this.isPlayerJoined(player))
			return null;

		ParkourPlayer pplayer = new ParkourPlayer(player);
		pplayer.saveInventory();
		pplayers.add(pplayer);
		player.setCollidable(false);

		if(interval == -1) {
			interval = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
				@Override
				public void run() {
					for(ParkourPlayer pplayer : pplayers) {
						pplayer.sendActionBar(getMessage("actionbar").replace("{time}", pplayer.getDuration()));
					}
				}
			}, 0L, 1L);
		}

		return pplayer;
	}

	public ParkourPlayer removePlayer(Player player) {
		if(!this.isPlayerJoined(player))
			return null;

		ParkourPlayer pplayer = this.getParkourPlayer(player);
		pplayer.restoreInventory();
		pplayers.remove(pplayer);
		player.setCollidable(true);

		if(pplayers.size() == 0) {
			Bukkit.getServer().getScheduler().cancelTask(interval);
			interval = -1;
		}

		return pplayer;
	}

	public ParkourPlayer getParkourPlayer(Player player) {
		return pplayers.stream().filter(pplayer -> pplayer.getPlayer() == player).findAny().orElse(null);
	}

	public boolean isPlayerJoined(Player player) {
		return this.getParkourPlayer(player) != null;
	}

	public String getMessage(String message) {
		return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("parkour.messages." + message));
	}

	public void loadConfig() {
		FileConfiguration config = plugin.getConfig();
		String world_name = config.getString("parkour.location.start.world");

		// Load world
		new WorldCreator(world_name).createWorld();
		World world = Bukkit.getServer().getWorld(world_name);

		this.parkourWorld = world;
		this.parkourStartPos = new Location(world, config.getDouble("parkour.location.start.x"), config.getDouble("parkour.location.start.y"), config.getDouble("parkour.location.start.z"), (float) config.getDouble("parkour.location.start.yaw"), (float) config.getDouble("parkour.location.start.pitch"));
		this.parkourRegionPos1 = new Location(world, config.getDouble("parkour.region.pos1.x"), config.getDouble("parkour.region.pos1.y"), config.getDouble("parkour.region.pos1.z"));
		this.parkourRegionPos2 = new Location(world, config.getDouble("parkour.region.pos2.x"), config.getDouble("parkour.region.pos2.y"), config.getDouble("parkour.region.pos2.z"));
	}
}
