package com.sylvcraft;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ConsoleChat extends JavaPlugin {

	@Override
	public void onEnable() {
		saveDefaultConfig();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Map<String, String> data = new HashMap<String, String>();
		
		if (sender instanceof Player && getConfig().getBoolean("config.console-only", true)) {
			msg(sender, "console-only", data);
			return true;
		}
		
		if (args.length == 0) {
			msg(sender, "help", data);
			return true;
		}
		
		switch (args[0].toLowerCase()) {
		case ":reload":
			if (!sender.hasPermission("consolechat.reload")) {
				msg(sender, "access-denied", data);
				return true;
			}
			
			reloadConfig();
			msg(sender, "reloaded", data);
			break;
		case ":console":
			if (!sender.hasPermission("consolechat.toggle")) {
				msg(sender, "access-denied", data);
				return true;
			}
			
			getConfig().set("config.console-only", !getConfig().getBoolean("config.console-only", true));
			saveConfig();
			data.put("%value%", getConfig().getBoolean("config.console-only", false)?"true":"false");
			msg(sender, "console-toggle", data);
			break;
		case ":prefix":
			if (args.length < 2) {
				msg(sender, "help", data);
				return true;
			}
			
			if (!sender.hasPermission("consolechat.set.prefix")) {
				msg(sender, "access-denied", data);
				return true;
			}
			
			String msgPrefix = StringUtils.join(args, " ", 1, args.length);
			data.put("%prefix%", msgPrefix);
			getConfig().set("config.prefix", msgPrefix);
			saveConfig();
			msg(sender, "prefix-set", data);
			break;
			
		case ":suffix":
			if (args.length < 2) {
				msg(sender, "help", data);
				return true;
			}
			
			if (!sender.hasPermission("consolechat.set.suffix")) {
				msg(sender, "access-denied", data);
				return true;
			}
			
			String msgSuffix = StringUtils.join(args, " ", 1, args.length);
			data.put("%suffixt%", msgSuffix);
			getConfig().set("config.suffix", msgSuffix);
			saveConfig();
			msg(sender, "suffix-set", data);
			break;
			
		default:
			if (args.length < 2) {
				msg(sender, "help", data);
				return true;
			}
			
			String msg = getConfig().getString("config.prefix", ""); 
			msg += StringUtils.join(args, " ", 1, args.length);
			msg += getConfig().getString("config.suffix", "");
			data.put("%msg%", msg);

			if (args[0].equals("*")) {
				if (!sender.hasPermission("consolechat.send.global")) {
					msg(sender, "access-denied", data);
					return true;
				}
				
				for (Player p : getServer().getOnlinePlayers()) {
					if (sender instanceof Player && p == (Player)sender) continue;
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
				}
				msg(sender, "sent-global", data);
			} else {
				if (!sender.hasPermission("consolechat.send")) {
					msg(sender, "access-denied", data);
					return true;
				}
				
				Player recipient = getServer().getPlayer(args[0]);
				if (recipient == null) {
					msg(sender, "invalid-player", data);
					return true;
				}
				
				data.put("%player%", args[0]);
				recipient.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
				msg(sender, "sent", data);
			}
			break;
		}
		return true;
	}

	public void msg(CommandSender recipient, String msgCode, Map<String, String> data) {
		String msg = getConfig().getString("messages." + msgCode, msgCode) + " ";
		for (Map.Entry<String, String> msgData : data.entrySet()) msg = msg.replaceAll(msgData.getKey(), msgData.getValue());
		for (String line : msg.split("%br%")) recipient.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
	}
}
