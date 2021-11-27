package me.szumielxd.bungeeannouncements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Announcement implements Runnable {

	
	private final BungeeAnnouncements plugin;
	private final String id;
	private final List<Component> messages = new ArrayList<>();
	private final List<Component> legacyMessages = new ArrayList<>();
	private final List<String> serverList;
	private final boolean blacklist;
	private int counter = -1;
	
	
	public Announcement(BungeeAnnouncements plugin, String id, SerializableAnnouncement announcement) {
		this.plugin = plugin;
		this.id = id;
		this.blacklist = announcement.isBlacklistMode();
		this.serverList = announcement.getServerList().stream().map(String::toLowerCase).collect(Collectors.toList());
		
		LegacyComponentSerializer altSerializer = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().hexColors().build();
		LegacyComponentSerializer altLegacySerializer = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build();
		int i = 0;
		for (String msg : announcement.getMessages()) {
			i++;
			try {
				// JSON
				this.messages.add(GsonComponentSerializer.gson().deserializeFromTree(new Gson().fromJson(msg, JsonObject.class)));
			} catch (Exception e) {
				Component comp = MiniMessage.get().deserialize(msg);
				String str = altSerializer.serialize(comp);
				// MiniMessage
				if (!str.equalsIgnoreCase(msg.replace("\\n", "\n"))) this.messages.add(comp);
				// Legacy
				else this.messages.add(altSerializer.deserializeOr(ChatColor.translateAlternateColorCodes('&', msg).replace("\\n", "\n"), Component.text("INVALID("+i+")").color(NamedTextColor.RED)));
			}
		}
		i = 0;
		for (String msg : announcement.getLegacyMessages()) {
			i++;
			try {
				// JSON
				this.legacyMessages.add(GsonComponentSerializer.colorDownsamplingGson().deserializeFromTree(new Gson().fromJson(msg, JsonObject.class)));
			} catch (Exception e) {
				Component comp = this.downsampleComponent(MiniMessage.get().deserialize(msg));
				String str = altSerializer.serialize(comp);
				// MiniMessage
				if (!str.equalsIgnoreCase(msg.replace("\\n", "\n"))) this.legacyMessages.add(comp);
				// Legacy
				else this.legacyMessages.add(altLegacySerializer.deserializeOr(ChatColor.translateAlternateColorCodes('&', msg).replace("\\n", "\n"), Component.text("INVALID("+i+")").color(NamedTextColor.RED)));
			}
		}
	}
	
	
	@Override
	public void run() {
		Collection<ProxiedPlayer> players = this.plugin.getProxy().getPlayers();
		this.counter++;
		if (this.counter >= this.messages.size() || this.counter >= this.legacyMessages.size()) this.counter = 0;
		if (players.isEmpty() || this.messages.isEmpty() || this.legacyMessages.isEmpty()) return;
		for (ProxiedPlayer player : players) {
			if (player.getServer() == null) return;
			if (this.blacklist != this.serverList.contains(player.getServer().getInfo().getName().toLowerCase())) {
				if (player.hasPermission("bungeeannouncements.announce."+this.id)) {
					TextReplacementConfig pcfg = TextReplacementConfig.builder().matchLiteral("{player}").replacement(player.getName()).build();
					TextReplacementConfig scfg = TextReplacementConfig.builder().matchLiteral("{server}").replacement(player.getServer().getInfo().getName()).build();
					if (player.getPendingConnection().getVersion() > 734) {
						this.plugin.adventure().player(player).sendMessage(this.messages.get(this.counter).replaceText(pcfg).replaceText(scfg));
					} else {
						this.plugin.adventure().player(player).sendMessage(this.legacyMessages.get(this.counter).replaceText(pcfg).replaceText(scfg));
					}
				}
			}
		}
	}
	
	
	public String getId() {
		return this.id;
	}
	public boolean isBlacklisted() {
		return this.blacklist;
	}
	public List<String> getServerList() {
		return new ArrayList<>(this.serverList);
	}
	public Component[] getNext() {
		int c = this.counter + 1;
		if (c >= messages.size() || c >= this.legacyMessages.size()) c = 0;
		return new Component[] { this.messages.get(c), this.legacyMessages.get(c) };
	}
	public int getCounter() {
		return this.counter;
	}
	public Component[] getMessage(int index) {
		int c = index;
		if (c >= messages.size() || c >= this.legacyMessages.size()) c = 0;
		return new Component[] { this.messages.get(c), this.legacyMessages.get(c) };
	}
	
	
	private Component downsampleComponent(Component comp) {
		
		// self
		if (comp.color() != null && !(comp.color() instanceof NamedTextColor)) {
			comp = comp.color(NamedTextColor.nearestTo(comp.color()));
		}
		
		// children
		if (!comp.children().isEmpty()) {
			comp = comp.children(comp.children().stream().map(this::downsampleComponent).collect(Collectors.toList()));
		}
		
		// hover
		if (comp.hoverEvent() != null && comp.hoverEvent().value() instanceof Component) {
			comp = comp.hoverEvent(this.downsampleComponent((Component) comp.hoverEvent().value()));
		}
		return comp;
	}
	

}
