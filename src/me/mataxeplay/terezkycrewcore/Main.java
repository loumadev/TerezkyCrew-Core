package me.mataxeplay.terezkycrewcore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	// ArrayList<Player> ScoreboardPlayers = new ArrayList<Player>();
	// HashMap<String, Team> ScoreboardItems = new HashMap<String, Team>();
	// HashMap<String, String> ScoreboardEntries = new HashMap<String, String>();
	// final float MAX_TPS = 20f;
	// final float SLEEP_PERCENTAGE = 50f * 0.01f;
	// float TPS = MAX_TPS;

	// @EventHandler
	// public void onJoin(PlayerJoinEvent event) {
	// Player player = event.getPlayer();
	// if(player.isOp())
	// player.sendMessage("You can use \"/ss\" to toggle server statistics monitor!");
	// }

	// @EventHandler
	// public void onQuit(PlayerQuitEvent event) {
	// if(ScoreboardPlayers.contains(event.getPlayer()))
	// ScoreboardPlayers.remove(event.getPlayer());
	// checkSleeping(false);
	// }

	// @EventHandler
	// public void onBedEnter(PlayerBedEnterEvent event) {
	// checkSleeping(true);
	// }

	// private void checkSleeping(boolean current) {
	// World world = Bukkit.getWorld("world");
	// int players = world.getPlayers().size();
	// int numSleeping = world.getPlayers().stream().filter(e ->
	// e.isSleeping()).collect(Collectors.toList()).size() + (current ? 1 : 0);
	// float percent = (float) numSleeping / players;
	// boolean skipNight = percent >= SLEEP_PERCENTAGE;

	// // System.out.println(numSleeping + "/" + players + " are sleeping > " + percent + "% (needed " +
	// // SLEEP_PERCENTAGE + "%)" + " > skip night: " + (skipNight ? "true" : "false"));

	// if(skipNight) {
	// Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
	// public void run() {
	// world.setTime(1000);
	// }
	// }, (long) (20 * 3));
	// }
	// }

	@Override
	public void onEnable() {
		System.out.println("Plugin is starting!");
		loadConfig();

		PluginManager pm = getServer().getPluginManager();

		pm.registerEvents(new VoidSpawnTeleport(), this);
		pm.registerEvents(new GeneralNPCs(), this);
		pm.registerEvents(new Parkour(), this);
		pm.registerEvents(new Troll(), this);

		this.getCommand("survival").setExecutor(new SurvivalTeleport());
		// this.getCommand("discord").setExecutor(new GeneralCommands());

		// ScoreboardEntries.put("players", ChatColor.BLUE + "" + ChatColor.WHITE);
		// ScoreboardEntries.put("tps", ChatColor.RED + "" + ChatColor.WHITE);
		// ScoreboardEntries.put("lag", ChatColor.GREEN + "" + ChatColor.WHITE);
		// ScoreboardEntries.put("memory_usage", ChatColor.BLACK + "" + ChatColor.WHITE);

		// BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		// scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
		// long now = System.currentTimeMillis();
		// long prevTick = now;
		// long timer1 = now;
		// // long timer2 = now;

		// @Override
		// public void run() {
		// long now = System.currentTimeMillis();

		// TPS = 1000f / (float) (now - prevTick);
		// prevTick = now;


		// if(now > timer1 + 500) {
		// timer1 = now;

		// for(Player player : Bukkit.getOnlinePlayers()) {
		// Location loc = player.getLocation();
		// String text = ChatColor.GOLD + "X: " + ChatColor.WHITE + Math.round(loc.getX()) + ChatColor.GOLD
		// + " Y: " + ChatColor.WHITE + Math.round(loc.getY()) + ChatColor.GOLD + " Z: " + ChatColor.WHITE +
		// Math.round(loc.getZ());

		// player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
		// }

		// updateScoreboard();
		// }
		// }
		// }, 0, 0);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!command.getName().toLowerCase().equals("tc"))
			return false;

		if(args.length != 0 && args[0].equals("reload") && sender.hasPermission("tc.config.reload")) {
			reloadConfig();
			sender.sendMessage("Configuration reloaded!");
			return true;
		}

		// if(sender instanceof Player) {
		// Player player = (Player) sender;

		// if(ScoreboardPlayers.contains(player)) {
		// ScoreboardPlayers.remove(player);
		// removeScoreboard(player);
		// } else {
		// ScoreboardPlayers.add(player);
		// createScoreboard(player);
		// updateScoreboard();
		// }
		// } else {
		// Runtime r = Runtime.getRuntime();
		// long memUsed = (r.totalMemory() - r.freeMemory());
		// System.out.println("Memory used: " + formatSize(memUsed) + "/" + formatSize(r.totalMemory()));
		// }

		sender.sendMessage("You don't have permissions for this command!");
		return true;
	}

	@Override
	public void onDisable() {
		System.out.println("Plugin is disabling!");
	}

	public void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	// public void createScoreboard(Player player) {
	// Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
	// Objective obj = board.registerNewObjective("ServerStatus", "dummy", ChatColor.GOLD + "" +
	// ChatColor.BOLD + "ServerStatus");
	// obj.setDisplaySlot(DisplaySlot.SIDEBAR);

	// for(Map.Entry<String, String> entry : ScoreboardEntries.entrySet()) {
	// String name = entry.getKey();
	// ScoreboardItems.put(name, board.registerNewTeam(name));
	// ScoreboardItems.get(name).addEntry(entry.getValue());
	// }

	// obj.getScore(ChatColor.GRAY + "+----------------+").setScore(15);
	// obj.getScore(ChatColor.GOLD + "" + ChatColor.BOLD + "Players:").setScore(14);
	// obj.getScore(ChatColor.RED + "").setScore(12);
	// obj.getScore(ChatColor.GOLD + "" + ChatColor.BOLD + "TPS:").setScore(11);
	// obj.getScore(ChatColor.AQUA + "").setScore(9);
	// obj.getScore(ChatColor.GOLD + "" + ChatColor.BOLD + "Lag:").setScore(8);
	// obj.getScore(ChatColor.BLUE + "").setScore(6);
	// obj.getScore(ChatColor.GOLD + "" + ChatColor.BOLD + "Memory usage:").setScore(5);
	// obj.getScore(ChatColor.GRAY + "+----------------+" + ChatColor.WHITE).setScore(3);

	// player.setScoreboard(board);
	// }

	// public void removeScoreboard(Player player) {
	// player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	// }

	// public void updateScoreboard() {
	// if(ScoreboardPlayers.size() == 0)
	// return;

	// float a = 1f - (Math.min(TPS, MAX_TPS) / MAX_TPS);
	// ChatColor color = percentageColor(a);
	// String tps = color + Float.toString(Math.round(TPS * 100f) / 100f) + ChatColor.GRAY + " (" +
	// color + Math.round(1000f / TPS) + "ms" + ChatColor.GRAY + ")";
	// String players = ChatColor.GREEN + "" + Bukkit.getOnlinePlayers().size() + ChatColor.GRAY + "/" +
	// ChatColor.GREEN + Bukkit.getMaxPlayers();
	// String lag = color + Float.toString(Math.round(a * 10000f) / 100f) + "%";
	// String memory_usage = getMemoryUsage();

	// // System.out.println(ScoreboardPlayers.size() + ": " + ScoreboardPlayers.stream().map(e ->
	// // e.getDisplayName()).collect(Collectors.joining(", ")));

	// for(Player player : ScoreboardPlayers) {
	// Objective obj = player.getScoreboard().getObjective(DisplaySlot.SIDEBAR);

	// // System.out.println(player.getDisplayName());

	// ScoreboardItems.get("players").setPrefix(players);
	// ScoreboardItems.get("tps").setPrefix(tps);
	// ScoreboardItems.get("lag").setPrefix(lag);
	// ScoreboardItems.get("memory_usage").setPrefix(memory_usage);

	// obj.getScore(ScoreboardEntries.get("players")).setScore(13);
	// obj.getScore(ScoreboardEntries.get("tps")).setScore(10);
	// obj.getScore(ScoreboardEntries.get("lag")).setScore(7);
	// obj.getScore(ScoreboardEntries.get("memory_usage")).setScore(4);
	// }
	// }

	// private String getMemoryUsage() {
	// Runtime r = Runtime.getRuntime();
	// long total = r.totalMemory();
	// long used = (total - r.freeMemory());
	// float ratio = (float) used / (float) total;
	// ChatColor color = percentageColor(ratio);

	// return color + formatSize(used) + ChatColor.GRAY + "/" + ChatColor.GREEN + formatSize(total) +
	// ChatColor.GRAY + " (" + color + Math.round(ratio * 100) + "%" + ChatColor.GRAY + ")";
	// }

	// private ChatColor percentageColor(float percentage) {
	// if(percentage < 0.5)
	// return ChatColor.GREEN;
	// else if(percentage < 0.75)
	// return ChatColor.YELLOW;
	// else if(percentage < 0.9)
	// return ChatColor.GOLD;
	// else
	// return ChatColor.RED;
	// }

	// private String formatSize(long bytes) {
	// return Math.round(bytes * 9.537e-7) + "MB";
	// }
}
