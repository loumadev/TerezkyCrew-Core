package me.mataxeplay.terezkycrewcore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
import com.gmail.filoghost.holographicdisplays.api.Hologram;

public class Parkour implements Listener {
	private Plugin plugin = Main.getPlugin(Main.class);
	private ArrayList<ParkourPlayer> pplayers = new ArrayList<ParkourPlayer>();
	private int interval = -1;

	private World parkourWorld;
	private Location parkourStartPos;
	private Location parkourHologramPos;
	private Location parkourRegionPos1;
	private Location parkourRegionPos2;
	private Hologram parkourHologram;
	private FileConfiguration parkourData;
	private File dataFile;
	private int max_top;

	public Parkour() {
		this.loadConfig();

		parkourHologram = HologramsAPI.createHologram(plugin, parkourHologramPos);
		this.updateHologram();
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
		String duration = pplayer.getDuration(false);
		String broadcastMessage = this.getMessage("finished_others").replace("{player}", player.getDisplayName()).replace("{time}", duration);
		FileConfiguration config = plugin.getConfig();

		player.sendMessage(this.getMessage("finished").replace("{time}", duration));
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + config.getInt("parkour.reward"));
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2f, 1f);

		// Top records
		ArrayList<HashMap<String, Object>> top = new ArrayList<HashMap<String, Object>>();
		String playerName = player.getName();
		long playerTime = pplayer.getDelta();

		// Load data
		for(int i = 0; i < max_top; i++) {
			String position = "top." + (i + 1) + ".";
			String name = parkourData.getString(position + "name");
			long time = parkourData.getLong(position + "time");

			if(name == null || name.length() == 0) continue;

			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", name);
			map.put("time", time);

			top.add(map);
		}

		// Add current result
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", playerName);
		map.put("time", playerTime);
		top.add(map);

		// Sort top results
		top.sort((a, b) -> (int) ((long) a.get("time") - (long) b.get("time")));

		// Remove duplicates
		HashSet<Object> seen = new HashSet<>();
		top.removeIf(e -> !seen.add(e.get("name")));

		// Save top `max_top`
		for(int i = 0; i < Math.min(max_top, top.size()); i++) {
			String position = "top." + (i + 1) + ".";
			parkourData.set(position + "name", top.get(i).get("name"));
			parkourData.set(position + "time", top.get(i).get("time"));
		}

		// Save data
		try {
			parkourData.save(dataFile);
		} catch(IOException e) {
			e.printStackTrace();
		}

		// Update hologram
		this.updateHologram();

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
						pplayer.sendActionBar(getMessage("actionbar").replace("{time}", pplayer.getDuration(true)));
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

	public void updateHologram() {
		parkourHologram.clearLines();

		for(int i = 0; i < max_top; i++) {
			String position = "top." + (i + 1) + ".";
			String name = parkourData.getString(position + "name");
			long time = parkourData.getLong(position + "time");

			String line = this.getMessage("hologram_top")
				.replace("{n}", "" + (i + 1));

			if(name == null || name.length() == 0) {
				line = line
					.replace("{username}", "N/A")
					.replace("{time}", "N/A");
			} else {
				line = line
					.replace("{username}", name)
					.replace("{time}", ParkourPlayer.formatDuration(time));
			}

			parkourHologram.insertTextLine(i, line);
		}
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

	public Location getLocation(String path) {
		FileConfiguration config = plugin.getConfig();
		World default_world = Bukkit.getWorld(config.getString("parkour.location.start.world"));

		// Load values
		World world = Bukkit.getWorld(config.getString(path + ".world"));
		double x = config.getDouble(path + ".x");
		double y = config.getDouble(path + ".y");
		double z = config.getDouble(path + ".z");
		float yaw = (float) config.getDouble(path + ".yaw");
		float pitch = (float) config.getDouble(path + ".pitch");

		// Default values
		world = world != null ? world : default_world;

		return new Location(world, x, y, z, yaw, pitch);
	}

	public void loadConfig() {
		// Create Data folder
		File folder = new File(plugin.getDataFolder(), "parkour");
		if(!folder.exists()) folder.mkdirs();

		// Create Data file
		this.dataFile = new File(plugin.getDataFolder(), "parkour" + File.separator + "data.yml");
		try {
			if(!this.dataFile.exists()) this.dataFile.createNewFile();
		} catch(IOException e) {
			e.printStackTrace();
		}

		// Load configurations
		this.parkourData = YamlConfiguration.loadConfiguration(this.dataFile);
		FileConfiguration config = plugin.getConfig();
		String world_name = config.getString("parkour.location.start.world");

		// Load world
		new WorldCreator(world_name).createWorld();
		World world = Bukkit.getWorld(world_name);

		// Assign variables
		this.max_top = config.getInt("parkour.max_top");
		this.parkourWorld = world;
		this.parkourStartPos = this.getLocation("parkour.location.start");
		this.parkourHologramPos = this.getLocation("parkour.location.hologram");
		this.parkourRegionPos1 = this.getLocation("parkour.region.pos1");
		this.parkourRegionPos2 = this.getLocation("parkour.region.pos2");
	}
}
