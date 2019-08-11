package com.sylvcraft;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
			showHelp(sender, data);
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

		case ":seeglobal":
			if (!sender.hasPermission("consolechat.toggle.seeglobal")) {
				msg(sender, "access-denied", data);
				return true;
			}
			
			getConfig().set("config.see-own-global", !getConfig().getBoolean("config.see-own-global", false));
			saveConfig();
			data.put("%value%", getConfig().getBoolean("config.see-own-global", false)?"true":"false");
			msg(sender, "see-own-toggle", data);
			break;

		case ":console":
			if (!sender.hasPermission("consolechat.toggle.console")) {
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
				boolean listCode = false;
				if (sender.hasPermission("consolechat.send.code") ||
						sender.hasPermission("consolechat.set.prefix.code") ||
						sender.hasPermission("consolechat.send.global.code")) listCode = true;

				if (sender.hasPermission("consolechat.send") || listCode) {
					String prefix = getConfig().getString("config.prefix");
					if (prefix == null) {
						msg(sender, "no-prefix", data);
						return true;
					}
					data.put("%prefix%", prefix);
					msg(sender, "prefix-show", data);
					if (!listCode) return true;
				}
				if (listCode) {
					listPrefixes(sender);
					return true;
				}
				
				showHelp(sender, data);
				return true;
			}
			
			if (!sender.hasPermission("consolechat.set.prefix") && !sender.hasPermission("consolechat.set.prefix.code")) {
				msg(sender, "access-denied", data);
				return true;
			}
			
			setPrefix(sender, args);			
			break;
			
		case ":suffix":
			if (args.length < 2) {
				if (sender.hasPermission("consolechat.send") ||
						sender.hasPermission("consolechat.send.code") || 
						sender.hasPermission("consolechat.set.suffix") || 
						sender.hasPermission("consolechat.send.global.code")) {
					String suffix = getConfig().getString("config.suffix");
					if (suffix == null) {
						msg(sender, "no-suffix", data);
						return true;
					}
					data.put("%suffix%", suffix);
					msg(sender, "suffix-show", data);
					return true;
				}
				
				showHelp(sender, data);
				return true;
			}
			
			if (!sender.hasPermission("consolechat.set.suffix")) {
				msg(sender, "access-denied", data);
				return true;
			}
			
			String msgSuffix = StringUtils.join(args, " ", 1, args.length);
			data.put("%suffix%", msgSuffix);
			getConfig().set("config.suffix", msgSuffix);
			saveConfig();
			msg(sender, "suffix-set", data);
			break;
			
		default:
			if (args.length < 2) {
				showHelp(sender, data);
				return true;
			}

			sendMessage(sender, args);
			break;
		}
		return true;
	}

	public void showHelp(CommandSender sender, Map<String, String> data) {
		int helped = 0;
		if (sender.hasPermission("consolechat.set.prefix")) { msg(sender, "help-prefix", data); helped++; }
		if (sender.hasPermission("consolechat.set.prefix.code")) { msg(sender, "help-prefix-code", data); helped++; }
		if (sender.hasPermission("consolechat.set.suffix")) { msg(sender, "help-suffix", data); helped++; }
		if (sender.hasPermission("consolechat.toggle.console")) { msg(sender, "help-console", data); helped++; }
		if (sender.hasPermission("consolechat.toggle.seeglobal")) { msg(sender, "help-seeglobal", data); helped++; }
		if (sender.hasPermission("consolechat.reload")) { msg(sender, "help-reload", data); helped++; }
		if (sender.hasPermission("consolechat.send")) { msg(sender, "help-send-player", data); helped++; }
		if (sender.hasPermission("consolechat.send.code")) { msg(sender, "help-send-player-code", data); helped++; }
		if (sender.hasPermission("consolechat.send.global")) { msg(sender, "help-send-global", data); helped++; }
		if (sender.hasPermission("consolechat.send.global.code")) { msg(sender, "help-send-global-code", data); helped++; }
		if (helped == 0) msg(sender, "access-denied", data);
	}

	public void msg(CommandSender recipient, String msgCode, Map<String, String> data) {
		String msg = getConfig().getString("messages." + msgCode, msgCode) + " ";
		for (Map.Entry<String, String> msgData : data.entrySet()) msg = msg.replaceAll(msgData.getKey(), msgData.getValue());
		for (String line : msg.split("%br%")) recipient.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
	}
	
	
	public void listPrefixes(CommandSender sender) {
		Map<String, String> data = new HashMap<String, String>();
		if (getConfig().getConfigurationSection("config.prefixes") == null) {
			msg(sender, "prefixlist-empty", data);
			return;
		}

		Set<String> prefixes = getConfig().getConfigurationSection("config.prefixes").getKeys(false);
		if (prefixes.size() == 0) {
			msg(sender, "prefixlist-empty", data);
			return;
		}
		
		msg(sender, "prefixlist-header", data);
		for (String prefix : prefixes) {
			data.put("%prefix%", prefix);
			data.put("%value%", getConfig().getString("config.prefixes." + prefix, ""));
			msg(sender, "prefixlist-data", data);
		}
	}
	
	public void setPrefix(CommandSender sender, String[] args) {
		Map<String, String> data = new HashMap<String, String>();
		if (args[1].length() >= 2 && args[1].substring(0, 1).equals(":") && !args[1].substring(1, 1).equals(":")) {
			if (!sender.hasPermission("consolechat.set.prefix.code")) {
				msg(sender, "access-denied", data);
				return;
			}
			String prefixCode = args[1].substring(1, args[1].length());
			String msgPrefix = StringUtils.join(args, " ", 2, args.length);
			data.put("%prefix%", prefixCode);
			data.put("%value%", msgPrefix);
			getConfig().set("config.prefixes." + prefixCode, msgPrefix);
			saveConfig();
			msg(sender, "prefix-code-set", data);
			return;
		}
		
		if (!sender.hasPermission("consolechat.set.prefix")) {
			msg(sender, "access-denied", data);
			return;
		}

		String msgPrefix = StringUtils.join(args, " ", 1, args.length);
		data.put("%prefix%", msgPrefix);
		getConfig().set("config.prefix", msgPrefix);
		saveConfig();
		msg(sender, "prefix-set", data);
	}
	
	public void sendMessage(CommandSender sender, String[] args) {
		Map<String, String> data = new HashMap<String, String>();
		String prefix = "", msg = "", recipient = "", suffix = getConfig().getString("config.suffix", "");
		Boolean doGlobal = false; int msgStart = 0;
		
		if (args[0].equals("*")) {
			doGlobal = true;
			msgStart++;
		} else {
			recipient = args[0];
			msgStart++;
		}
		
		if (args[msgStart].length() >= 2 && args[msgStart].substring(0,1).equals(":") && !args[msgStart].substring(1,1).equals(":")) {
			if ((doGlobal && !sender.hasPermission("consolechat.send.global.code")) || (!doGlobal && !sender.hasPermission("consolechat.send.code"))) {
				msg(sender, "access-denied", data);
				return;
			}
			
			prefix = getConfig().getString("config.prefixes." + args[msgStart].substring(1, args[msgStart].length()), "");
			msgStart++;
		} else {
			prefix = getConfig().getString("config.prefix", "");
		}

		msg = StringUtils.join(args, " ", msgStart, args.length);
		if (msg.trim().equals("")) {
			msg(sender, "no-message", data);
			return;
		}

		data.put("%msg%", prefix + msg + suffix);
		if (doGlobal) {
			if (!sender.hasPermission("consolechat.send.global")) {
				msg(sender, "access-denied", data);
				return;
			}
			
			for (Player p : getServer().getOnlinePlayers()) {
				if (sender instanceof Player && p == (Player)sender && !getConfig().getBoolean("config.see-own-global", false)) continue;
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg + suffix));
			}
			msg(sender, "sent-global", data);
			return;
		} else {
			if (!sender.hasPermission("consolechat.send")) {
				msg(sender, "access-denied", data);
				return;
			}
			
			Player r = getServer().getPlayer(recipient);
			if (r == null) {
				msg(sender, "invalid-player", data);
				return;
			}
			
			data.put("%player%", args[0]);
			r.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg + suffix));
			msg(sender, "sent", data);
		}
	}
}
