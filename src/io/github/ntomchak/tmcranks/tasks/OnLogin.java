package io.github.ntomchak.tmcranks.tasks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.ntomchak.tmcranks.PlayerExpData;
import io.github.ntomchak.tmcranks.TMCRanks;

public class OnLogin extends BukkitRunnable {
  private UUID uuid;
  private String name;
  private HashMap<UUID, Integer> addLater;
  private HashMap<UUID, PlayerExpData> playerXp;

  public OnLogin(UUID uuid, String name, HashMap<UUID, Integer> addLater, HashMap<UUID, PlayerExpData> playerXp) {
    this.uuid = uuid;
    this.name = name;
    this.addLater = addLater;
    this.playerXp = playerXp;
  }

  public void run() {
    try {
      PreparedStatement st = TMCRanks.getInstance().getConnection()
          .prepareStatement(" SELECT * FROM `players` WHERE `uuid` = ?    ");

      st.setString(1, uuid.toString());
      ResultSet rs = st.executeQuery();
      if (!rs.next()) {
        PreparedStatement ps = TMCRanks.getInstance().getConnection()
            .prepareStatement("INSERT INTO `players` (`uuid`, `name`, `xp`) VALUES (?, ?, 0)");
        ps.setString(1, uuid.toString());
        ps.setString(2, name);
        ps.execute();
        ps.close();

        new BukkitRunnable() {
          public void run() {
            if (Bukkit.getPlayer(uuid).isOnline()) {
              PlayerExpData ped = new PlayerExpData();
              playerXp.put(uuid, ped);
              if (addLater.containsKey(uuid)) {
                ped.addXp(addLater.get(uuid), uuid);
                addLater.remove(uuid);
              }
            }
          }
        }.runTask(TMCRanks.getInstance());

      } else {
        if (!rs.getString("name").equalsIgnoreCase(name)) {
          PreparedStatement ps = TMCRanks.getInstance().getConnection()
              .prepareStatement("UPDATE `players` SET `name` =? WHERE `uuid`=?");
          ps.setString(1, name);
          ps.setString(2, uuid.toString());
          ps.execute();
          ps.close();
        }
        final int xp = rs.getInt("xp");
        final int level = PlayerExpData.getLevel(xp);
        final int progress = xp - PlayerExpData.getTotalXp(level);
        final int target = PlayerExpData.calcTarget(level);

        new BukkitRunnable() {
          public void run() {
            if (Bukkit.getPlayer(uuid).isOnline()) {
              PlayerExpData ped = new PlayerExpData(progress, target, level);
              playerXp.put(uuid, ped);
              if (addLater.containsKey(uuid)) {
                ped.addXp(addLater.get(uuid), uuid);
              }
            }
          }
        }.runTask(TMCRanks.getInstance());
      }

      rs.close();
      st.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
