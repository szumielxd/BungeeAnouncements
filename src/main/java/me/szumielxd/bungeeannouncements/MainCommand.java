package me.szumielxd.bungeeannouncements;

import java.util.ArrayList;
import java.util.function.Function;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class MainCommand extends Command implements TabExecutor {
	
	private final BungeeAnnouncements plugin;

	public MainCommand(BungeeAnnouncements plugin) {
		super(Config.COMMAND_NAME.getString(), "bungeeannouncements.command", Config.COMMAND_ALIASES.getStringList().toArray(new String[0]));
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
				if (sender.hasPermission("bungeeannouncements.command.reload")) {
					boolean failed = false;
					Function<String,String> replacer = (str) -> {
						return str.replace("{plugin}", this.plugin.getDescription().getName()).replace("{version}", this.plugin.getDescription().getVersion());
					};
					sender.sendMessage(TextComponent.fromLegacyText(replacer.apply(Config.PREFIX.getString()+Config.COMMAND_SUB_RELOAD_EXECUTE.getString())));
					try {
						this.plugin.onDisable();
					} catch (Exception e) {
						e.printStackTrace();
						failed = true;
					}
					try {
						this.plugin.onEnable();
					} catch (Exception e) {
						e.printStackTrace();
						failed = true;
					}
					if (failed) sender.sendMessage(TextComponent.fromLegacyText(replacer.apply(Config.PREFIX.getString()+Config.COMMAND_SUB_RELOAD_ERROR.getString())));
					else sender.sendMessage(TextComponent.fromLegacyText(replacer.apply(Config.PREFIX.getString()+Config.COMMAND_SUB_RELOAD_SUCCESS.getString())));
				} else {
					sender.sendMessage(TextComponent.fromLegacyText(Config.PREFIX.getString()+Config.MESSAGES_PERM_ERROR.getString()));
				}
				return;
			}
		}
		sender.sendMessage(TextComponent.fromLegacyText(Config.PREFIX.getString()+"/"+Config.COMMAND_NAME.getString()+" reload|rl"));
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		ArrayList<String> list = new ArrayList<>();
		if (sender.hasPermission("bungeeannouncements.command")) {
			if (args.length == 1) {
				if ("reload".startsWith(args[0].toLowerCase()) && sender.hasPermission("bungeeannouncements.command.reload")) list.add("reload");
				if ("rl".startsWith(args[0].toLowerCase()) && sender.hasPermission("bungeeannouncements.command.reload")) list.add("rl");
				return list;
			}
		}
		return list;
	}

}
