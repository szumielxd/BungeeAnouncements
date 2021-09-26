package me.szumielxd.bungeeannouncements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

public enum Config {
	
	PREFIX("common.prefix", "&d&lB&e&lA&r &8&l»&r &7", true),
	DEBUG("common.debug", false),
	MESSAGES_PERM_ERROR("message.perm-error", "&cNo, you can't", true),
	MESSAGES_COMMAND_ERROR("message.command-error", "&4An error occured while attempting to perform this command. Please report this to admin.", true),
	MESSAGES_CONSOLE_ERROR("message.console-error", "&cNot for console ;c", true),
	COMMAND_NAME("command.name", "bungeeannouncements"),
	COMMAND_ALIASES("command.aliases", Arrays.asList(new String[] { "bcann", "bcannounce", "bungeeann", "bungeeannounce" })),
	COMMAND_SUB_RELOAD_EXECUTE("command.sub.reload.execute", "Reloading...", true),
	COMMAND_SUB_RELOAD_ERROR("command.sub.reload.error", "&cAn error occured while reloading plugin. See console for more info.", true),
	COMMAND_SUB_RELOAD_SUCCESS("command.sub.reload.success", "&aSuccessfully reloaded {plugin} v{version}", true),
	ANNOUNCEMENTS("announcements", Collections.singletonMap("default", new SerializableAnnouncement(20*60, 60, false, new ArrayList<>(), Arrays.asList(""), Arrays.asList("")).toConfiguration()));
	
	
	//////////////////////////////////////////////////////////////////////
	
	private final String path;
	private List<String> texts;
	private String text;
	private int number;
	private boolean bool;
	private Map<String, ?> map;
	private boolean colored = false;
	private Class<?> type;
	
	
	private Config(String path, String text) {
		this(path, text, false);
	}
	private Config(String path, String text, boolean colored) {
		this.path = path;
		this.colored = colored;
		setValue(text);
	}
	private Config(String path, List<String> texts) {
		this(path, texts, false);
	}
	private Config(String path, List<String> texts, boolean colored) {
		this.path = path;
		this.colored = colored;
		setValue(texts);
	}
	private Config(String path, int number) {
		this.path = path;
		setValue(number);
	}
	private Config(String path, boolean bool) {
		this.path = path;
		setValue(bool);
	}
	private Config(String path, Map<String, Configuration> valueMap) {
		this.path = path;
		setValue(valueMap);
	}
	
	
	
	//////////////////////////////////////////////////////////////////////
	
	public void setValue(String text) {
		this.type = String.class;
		this.text = text;
		this.texts = new ArrayList<>(Arrays.asList(new String[] { this.text }));
		this.number = text.length();
		this.bool = !text.isEmpty();
		this.map = new HashMap<>();
	}
	public void setValue(List<String> texts) {
		this.type = String[].class;
		this.text = String.join(", ", texts);
		this.texts = texts;
		this.number = texts.size();
		this.bool = !texts.isEmpty();
		this.map = texts.stream().collect(Collectors.toMap(v -> Integer.toString(texts.indexOf(v)), v -> v));
	}
	public void setValue(int number) {
		this.type = Integer.class;
		this.text = Integer.toString(number);
		this.texts = new ArrayList<>(Arrays.asList(new String[] { this.text }));
		this.number = number;
		this.bool = number > 0;
		this.map = new HashMap<>();
	}
	public void setValue(boolean bool) {
		this.type = Boolean.class;
		this.text = Boolean.toString(bool);
		this.texts = new ArrayList<>(Arrays.asList(new String[] { this.text }));
		this.number = bool? 1 : 0;
		this.bool = bool;
		this.map = new HashMap<>();
	}
	public <T> void setValue(Map<String, T> valueMap) {
		this.type = Map.class;
		this.text = valueMap.toString();
		this.texts = valueMap.values().stream().map(v -> v.toString()).collect(Collectors.toList());
		this.number = valueMap.size();
		this.bool = !valueMap.isEmpty();
		this.map = valueMap;
	}
	
	
	public String getString() {
		return this.text;
	}
	public String toString() {
		return this.text;
	}
	public List<String> getStringList() {
		return new ArrayList<>(this.texts);
	}
	public int getInt() {
		return this.number;
	}
	public boolean getBoolean() {
		return this.bool;
	}
	public Map<String, ?> getValueMap() {
		return this.map;
	}
	public boolean isColored() {
		return this.colored;
	}
	public Class<?> getType() {
		return this.type;
	}
	public String getPath() {
		return this.path;
	}
	
	
	
	//////////////////////////////////////////////////////////////////////
	
	public static void load(File file, BungeeAnnouncements plugin) {
		plugin.getLogger().info("Loading configuration from '" + file.getName() + "'");
		if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
		try {
			if(!file.exists()) file.createNewFile();
			YamlConfiguration yml = (YamlConfiguration) YamlConfiguration.getProvider(YamlConfiguration.class);
			Configuration config = yml.load(file);
			if(loadConfig(config) > 0) yml.save(config, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static int loadConfig(Configuration config) {
		int modify = 0;
		for (Config val : Config.values()) {
			if(!config.contains(val.getPath())) modify++;
			if (val.getType().equals(String.class)) {
				if (val.isColored())val.setValue(getColoredStringOrSetDefault(config, val.getPath(), val.getString()));
				else val.setValue(getStringOrSetDefault(config, val.getPath(), val.getString()));
			} else if (val.getType().equals(String[].class)) {
				if (val.isColored())val.setValue(getColoredStringListOrSetDefault(config, val.getPath(), val.getStringList()));
				else val.setValue(getStringListOrSetDefault(config, val.getPath(), val.getStringList()));
			} else if (val.getType().equals(Integer.class)) val.setValue(getIntOrSetDefault(config, val.getPath(), val.getInt()));
			else if (val.getType().equals(Boolean.class)) val.setValue(getBooleanOrSetDefault(config, val.getPath(), val.getBoolean()));
			else if (val.getType().equals(Map.class)) val.setValue(getMapOrSetDefault(config, val.getPath(), val.getValueMap()));
		}
		return modify;
	}
	
	/*private static <T> T getOrSetDefault(Configuration config, String path, T def) {
		SZChatFilterAPI.get().getPlugin().debug("Config::getOrSetDefault -> "+config.contains(path)+","+Map.class.isInstance(def)+","+!config.getSection(path).getKeys().isEmpty()+","+def.getClass().isInstance(config.get(path)));
		if (config.contains(path) && def.getClass().isInstance(config.get(path))) return config.get(path, def);
		config.set(path, def);
		return def;
	}*/
	
	@SuppressWarnings("unchecked")
	private static <T> Map<String,T> getMapOrSetDefault(Configuration config, String path, Map<String,T> def) {
		if (config.contains(path)) {
			Map<String,T> map = new HashMap<>();
			Configuration section = config.getSection(path);
			for (String key : section.getKeys()) {
				map.put(key, (T) section.get(key));
			}
			return map;
		}
		config.set(path, def);
		return def;
	}
	
	private static int getIntOrSetDefault(Configuration config, String path, int def) {
		if (config.contains(path)) return config.getInt(path);
		config.set(path, def);
		return def;
	}
	
	private static boolean getBooleanOrSetDefault(Configuration config, String path, boolean def) {
		if (config.contains(path)) return config.getBoolean(path);
		config.set(path, def);
		return def;
	}
	
	private static String getStringOrSetDefault(Configuration config, String path, String def) {
		if (config.contains(path)) return config.getString(path);
		config.set(path, def);
		return def;
	}
	
	private static String getColoredStringOrSetDefault(Configuration config, String path, String def) {
		String str = ChatColor.translateAlternateColorCodes('&', getStringOrSetDefault(config, path, def.replace('§', '&')));
		return str;
	}
	
	private static ArrayList<String> getStringListOrSetDefault(Configuration config, String path, List<String> def) {
		if(config.contains(path)) return new ArrayList<>(config.getStringList(path));
		config.set(path, def);
		return new ArrayList<>(def);
	}
	
	private static ArrayList<String> getColoredStringListOrSetDefault(Configuration config, String path, List<String> def) {
		ArrayList<String> list = getStringListOrSetDefault(config, path, def.stream().map(str -> str.replace('§', '&')).collect(Collectors.toCollection(ArrayList::new)));
		return list.stream().map((str) -> ChatColor.translateAlternateColorCodes('&', str))
				.collect(Collectors.toCollection(ArrayList::new));
	}

}
