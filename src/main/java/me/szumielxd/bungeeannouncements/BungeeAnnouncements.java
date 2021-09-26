package me.szumielxd.bungeeannouncements;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

public class BungeeAnnouncements extends Plugin {
	
	
	private BungeeAudiences adventure = null;
	
	
	public BungeeAudiences adventure() {
		if (this.adventure == null) throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
		return this.adventure;
	}
	
	
	@Override
	public void onEnable() {
		this.adventure = BungeeAudiences.create(this);
		Config.load(new File(this.getDataFolder(), "config.yml"), this);
		this.getProxy().getPluginManager().registerCommand(this, new MainCommand(this));
		this.getLogger().info("Loading announcements...");
		int i = 0;
		if (!Config.ANNOUNCEMENTS.getValueMap().isEmpty()) for(Entry<String, ?> entry : Config.ANNOUNCEMENTS.getValueMap().entrySet()) {
			SerializableAnnouncement ann = new SerializableAnnouncement((Configuration) entry.getValue());
			if (ann.getMessages().isEmpty()) {
				this.getLogger().info(String.format("Announcement with id `%s` is empty. Skipping...", entry.getKey()));
				continue;
			}
			this.getProxy().getScheduler().schedule(this, new Announcement(this, entry.getKey(), ann), ann.getDelay(), ann.getPeriod(), TimeUnit.SECONDS);
			i++;
		}
		this.getLogger().info(String.format("Successfully loaded %d announcements!", i));
	}
	
	
	@Override
	public void onDisable() {
		this.getLogger().info("Disabling all announcements...");
		this.getProxy().getScheduler().cancel(this);
		if (this.adventure != null) {
			this.adventure.close();
			this.adventure = null;
		}
		try {
			Class<?> BungeeAudiencesImpl = Class.forName("net.kyori.adventure.platform.bungeecord.BungeeAudiencesImpl");
			Field f = BungeeAudiencesImpl.getDeclaredField("INSTANCES");
			f.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<String, BungeeAudiences> INSTANCES = (Map<String, BungeeAudiences>) f.get(null);
			INSTANCES.remove(this.getDescription().getName());
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		this.getProxy().getPluginManager().unregisterCommands(this);
		this.getLogger().info("Well done. Time to sleep!");
	}
	

}
