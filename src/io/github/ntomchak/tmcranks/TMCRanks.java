package io.github.ntomchak.tmcranks;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import io.github.ntomchak.tmcranks.tasks.GetTop20;
import io.github.ntomchak.tmcranks.tasks.SaveCachedXp;
import net.milkbowl.vault.permission.Permission;
import ws.toomanyco.TMCAnnouncer.TMCAnnouncer;
import net.milkbowl.vault.chat.Chat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;

public class TMCRanks extends JavaPlugin {

  private static Connection sqlConnection;
  private static TMCRanks instance;
  private HashMap<UUID, PlayerExpData> playerXp;
  private static Permission perms;
  private static Chat chat = null;
  private static TMCAnnouncer TMCAnnouncer;

  public void onEnable() {
    setupPermissions();
    setupChat();
    playerXp = new HashMap<UUID, PlayerExpData>();
    HashMap<UUID, Integer> addLater = new HashMap<UUID, Integer>();
    HashMap<UUID, Integer> lastAutoSaved = new HashMap<UUID, Integer>();
    TMCAnnouncer = (TMCAnnouncer) Bukkit.getPluginManager().getPlugin("TMCAnnouncer");
    instance = this;
    CopyOnWriteArrayList<String> top = new CopyOnWriteArrayList<String>();
    openConnection();

    LevelCommand level = new LevelCommand(top, playerXp);
    this.getCommand("level").setExecutor(level);
    this.getCommand("leveltop").setExecutor(level);

    getServer().getPluginManager().registerEvents(new Listeners(playerXp, lastAutoSaved, addLater), this);
    new SaveCachedXp(playerXp, lastAutoSaved).runTaskTimer(this, 20L * 300, 20L * 300);

    BukkitScheduler scheduler = getServer().getScheduler();
    scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
      @Override
      public void run() {
        GetTop20 gett = new GetTop20(top);
        gett.runTaskAsynchronously(getInstance());
        Bukkit.getConsoleSender().sendMessage("updated xp leaderboard");
      }
    }, 0L, 20L * 300); // 20*300=5 minutes
  }

  public void onDisable() {
    for (Map.Entry<UUID, PlayerExpData> entry : playerXp.entrySet()) {
      UUID uuid = entry.getKey();
      int xp = PlayerExpData.getTotalXp(entry.getValue().getLevel()) + entry.getValue().getProgress();
      try {
        PreparedStatement ps = TMCRanks.getInstance().getConnection()
            .prepareStatement("UPDATE `players` SET `xp` = ? WHERE `uuid` = ?");
        ps.setInt(1, xp);
        ps.setString(2, uuid.toString());
        ps.execute();
        ps.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    try {
      sqlConnection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public Connection getConnection() {
    return sqlConnection;
  }

  public static TMCRanks getInstance() {
    return instance;
  }

  private void openConnection() {
    
    try {
      Class.forName("com.mysql.jdbc.Driver");
      sqlConnection = DriverManager
          .getConnection("jdbc:mysql://xxx.xxx.xxx.xxx:3306/xxx?user=xxx&password=xxx&autoReconnect=true");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Permission getPermissions() {
    return perms;
  }

  public static Chat getChat() {
    return chat;
  }

  public TMCAnnouncer getTMCAnnounce() {
    return TMCAnnouncer;
  }

  // run in main thread only
  public void updateDB(UUID uuid, PlayerExpData data) {
    int xp = PlayerExpData.getTotalXp(data);
    new BukkitRunnable() {
      public void run() {
        try {
          PreparedStatement ps = TMCRanks.getInstance().getConnection()
              .prepareStatement("UPDATE `players` SET `xp` = ? WHERE `uuid` = ?");
          ps.setInt(1, xp);
          ps.setString(2, uuid.toString());
          ps.execute();
          ps.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }.runTaskAsynchronously(this);
  }

  private boolean setupPermissions() {
    RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
    perms = rsp.getProvider();
    return perms != null;
  }

  private boolean setupChat() {
    RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
    chat = rsp.getProvider();
    return chat != null;
  }
}
