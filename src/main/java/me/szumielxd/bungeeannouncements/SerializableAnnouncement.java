package me.szumielxd.bungeeannouncements;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.config.Configuration;

public class SerializableAnnouncement implements Cloneable {
	
	
	private int delay;
	private int period;
	private boolean blacklist;
	private List<String> serverList;
	private List<String> messages;
	private List<String> legacyMessages;
	
	
	public SerializableAnnouncement() {}
	
	public SerializableAnnouncement(int delay, int period, boolean blacklist, List<String> serverList, List<String> messages, List<String> legacyMessages) {
		this.delay = delay;
		this.period = period;
		this.blacklist = blacklist;
		this.serverList = serverList;
		this.messages = messages;
		this.legacyMessages = legacyMessages;
	}
	
	public SerializableAnnouncement(Configuration configuration) {
		this.delay = configuration.getInt("delay", 60);
		this.period = configuration.getInt("period", 60);
		this.blacklist = configuration.getBoolean("blacklist", true);
		this.serverList = configuration.getStringList("serverList");
		this.messages = configuration.getStringList("messages");
		this.legacyMessages = !configuration.getStringList("legacyMessages").isEmpty() ? configuration.getStringList("legacyMessages") : new ArrayList<>(this.messages);
	}
	
	
	public int getDelay() {
		return this.delay;
	}
	public int getPeriod() {
		return this.period;
	}
	public boolean isBlacklistMode() {
		return this.blacklist;
	}
	public List<String> getServerList() {
		return this.serverList;
	}
	public List<String> getMessages() {
		return this.messages;
	}
	public List<String> getLegacyMessages() {
		return this.legacyMessages;
	}
	
	public void setDelay(int delay) {
		this.delay = delay;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	public void setLegacyMessages(List<String> legacyMessages) {
		this.legacyMessages = legacyMessages;
	}
	
	public Configuration toConfiguration() {
		Configuration cfg = new Configuration();
		cfg.set("delay", this.delay);
		cfg.set("messages", this.messages);
		if (!this.messages.equals(this.legacyMessages)) cfg.set("legacyMessages", this.legacyMessages);
		return cfg;
	}
	
	public SerializableAnnouncement clone() {
		return new SerializableAnnouncement(this.delay, this.period, this.blacklist, new ArrayList<>(this.serverList), new ArrayList<>(this.messages), new ArrayList<>(this.legacyMessages));
	}

}
