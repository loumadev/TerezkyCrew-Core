package me.mataxeplay.terezkycrewcore;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SurvivalTeleport implements CommandExecutor {

	private Plugin plugin = Main.getPlugin(Main.class);

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!command.getName().toLowerCase().equals("survival"))
			return false;

		if(sender instanceof Player) {
			Player player = (Player) sender;

			List<String> list = plugin.getConfig().getStringList("survival.worlds");
			if(list.contains(player.getWorld().getName())) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.survival.already_in_world")));
				return true;
			}

			player.performCommand("rtp world");
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.survival.teleport")));
		} else {
			System.out.println("You cannot use this command!");
		}

		return true;
	}
}
