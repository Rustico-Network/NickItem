package de.thexxturboxx.autonick;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.BanList.Type;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.huskehhh.mysql.mysql.MySQL;

public class AutoNick extends JavaPlugin implements Listener {
	
	public static AutoNick instance;
	public static File path = new File("plugins/AutoNick"), dataPath;
	MySQL MySQL = null;
    Connection c = null;
    public static final String TABLE = "AutoNick",
    		DATABASE = "AutoNick";
	
	public static AutoNick getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		try {
			loadConfiguration();
			if(!getConfig().contains("MySQL.hostname") || getConfig().getString("MySQL.hostname").equals("null")) {
				set("MySQL.hostname", "null");
				set("MySQL.port", "null");
				set("MySQL.username", "null");
				set("MySQL.password", "null");
				getServer().getLogger().info("Bitte gib Deine MySQL-Daten in der Config ein!");
				getServer().shutdown();
			} else {
				MySQL = new MySQL(getConfig().getString("MySQL.hostname"),
								  getConfig().getString("MySQL.port"),
								  DATABASE,
								  getConfig().getString("MySQL.username"),
								  getConfig().getString("MySQL.password"));
				c = MySQL.openConnection();
				Statement s = c.createStatement();
				s.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE + " (UUID VARCHAR(40) PRIMARY KEY, nicked BOOLEAN);");
			}
			getServer().getPluginManager().registerEvents(this, this);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public void set(String key, Object value) {
		getConfig().set(key, value);
		saveConfig();
	}
	
	public void loadConfiguration() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
	public void simplePardon(String name) {
		getServer().getBanList(Type.NAME).pardon(name);
	}
	
	public void simpleBan(String name, String reason) {
		getServer().getBanList(Type.NAME).addBan(name, reason, null, null);
	}
	
	public static File getPluginPath() {
		return path;
	}
	
	public static File getDataPath() {
		return dataPath;
	}
	
	public static String getPrefix() {
		return ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + "NICK" + ChatColor.GRAY + "] ";
	}
	
	public static double round(double value, int decimal) {
	    return (double) Math.round(value * Math.pow(10d, decimal)) / Math.pow(10d, decimal);
	}
	
	@EventHandler
	public void giveItem(PlayerJoinEvent e) {
		if(e.getPlayer().hasPermission("nick.cmd.nick")) {
			try {
				Statement s = c.createStatement();
				ResultSet res = s.executeQuery("SELECT * FROM " + TABLE + " WHERE UUID = '" + e.getPlayer().getUniqueId().toString() + "';");
				boolean nicked = false;
				if(res.next()) {
					nicked = res.getBoolean("nicked");
				} else {
					s.executeUpdate("INSERT INTO " + TABLE + " (UUID, nicked) VALUES ('" + e.getPlayer().getUniqueId().toString() + "','FALSE');");
				}
				e.getPlayer().getInventory().setItem(8, getAutoNickItem(nicked));
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@EventHandler
	public void nickSwitch(PlayerInteractEvent e) {
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Statement s;
			try {
				s = c.createStatement();
				if(e.getPlayer().getItemInHand().isSimilar(getAutoNickItem(true))) {
					e.getPlayer().getInventory().setItem(8, getAutoNickItem(false));
					s.executeUpdate("UPDATE " + TABLE + " SET nicked = FALSE WHERE UUID = '" + e.getPlayer().getUniqueId().toString() + "'");
				} else if(e.getPlayer().getItemInHand().isSimilar(getAutoNickItem(false))) {
					e.getPlayer().getInventory().setItem(8, getAutoNickItem(true));
					s.executeUpdate("UPDATE " + TABLE + " SET nicked = TRUE WHERE UUID = '" + e.getPlayer().getUniqueId().toString() + "'");
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private ItemStack getAutoNickItem(boolean autoNick) {
		ItemStack stack = new ItemStack(Material.NAME_TAG);
		stack.getItemMeta().setDisplayName("§6AutoNick " + booleanToString(autoNick));
		return stack;
	}
	
	private String booleanToString(boolean b) {
		if(b) {
			return "§aeingeschaltet";
		} else {
			return "§4ausgeschaltet";
		}
	}
	
}