package io.github.ntomchak.tmcranks;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.permission.Permission;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

//non static methods from this class should only be called from the main thread
public class PlayerExpData {
  private int level;
  private int progress; // the amount of xp beyond the current level
  private int target; // the amount of xp beyond the current level that, once reached, will lead to
                      // the next level

  public PlayerExpData(int progress, int needed, int level) {
    this.progress = progress;
    this.target = needed;
    this.level = level;
  }

  public PlayerExpData() {
    level = 0;
    progress = 0;
    target = 7;
  }

  public int getLevel() {
    return level;
  }

  public int getProgress() {
    return progress;
  }

  public int getTarget() {
    return target;
  }

  public void addXp(int xp, UUID uuid) {
    progress += xp;
    if (progress >= target) {
      final int oldLevel = level;
      while (progress >= target) {
        level++;
        progress -= target;
        target = calcTarget(level);
      }
      // update database here, change prefixes and permissions
      final int aLevel = level;
      final int totalXp = getTotalXp(level) + progress;
      new BukkitRunnable() {
        public void run() {
          final int oldRank = getRank(oldLevel);
          final int newRank = getRank(aLevel);
          final String suffix = getSuffix(aLevel);
          String levelUpMessage = ChatColor.BLUE + "You have reached level " + ChatColor.DARK_AQUA + aLevel;
          if (newRank > oldRank) {
            levelUpMessage += ChatColor.BLUE + " and unlocked " + ChatColor.DARK_AQUA + getKit(newRank) + ChatColor.BLUE
                + ".";
          } else if (level < 5) {
            levelUpMessage += ChatColor.BLUE + " from gaining vanilla xp. See " + ChatColor.DARK_AQUA + "/help level"
                + ChatColor.BLUE + " for more info.";
          } else {
            levelUpMessage += ChatColor.BLUE + ".";
          }

          final String t = levelUpMessage;

          new BukkitRunnable() {
            public void run() {
              Player p = Bukkit.getPlayer(uuid);
              p.sendMessage(t);
              TMCRanks.getChat().setPlayerSuffix(null, p, suffix);

              if (newRank > oldRank) {
                Permission perms = TMCRanks.getPermissions();
                perms.playerRemoveGroup(null, p, Integer.toString(oldRank));
                perms.playerAddGroup(null, p, Integer.toString(newRank));
                TMCRanks.getInstance().getTMCAnnounce().updatePlayer(p);
              }
            }
          }.runTask(TMCRanks.getInstance());
          try {
            PreparedStatement ps = TMCRanks.getInstance().getConnection()
                .prepareStatement("UPDATE `players` SET `xp` = ? WHERE `uuid` = ?");
            ps.setInt(1, totalXp);
            ps.setString(2, uuid.toString());
            ps.execute();
            ps.close();
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }.runTaskAsynchronously(TMCRanks.getInstance());
    }
  }

  public double getPercentToNextLevel() {
    return ((double) progress) / ((double) target);
  }

  public static int calcTarget(int currentLevel) {
    if (currentLevel < 16) {
      return 2 * currentLevel + 7;
    } else if (currentLevel < 31) {
      return 5 * currentLevel - 38;
    } else {
      return 9 * currentLevel - 158;
    }
  }

  public static int getLevel(int xp) {
    if (xp < 353) {
      return ((int) Math.sqrt(36 + 4 * xp) - 6) / (2);
    } else if (xp < 1508) {
      return (int) ((40.5 + Math.sqrt(1640.25 - 10 * (360 - xp))) / 5);
    } else {
      return (int) ((162.5 + Math.sqrt(26406.25 - 18 * (2220 - xp))) / 9);
    }
  }

  public static int getTotalXp(int level) {
    if (level < 17) {
      return (level * level) + (6 * level);
    } else if (level < 32) {
      return (int) ((2.5 * Math.pow(level, 2)) - (40.5 * (double) level) + 360);
    } else {
      return (int) ((4.5 * Math.pow(level, 2)) - (162.5 * (double) level) + 2220);
    }
  }

  public static int getTotalXp(PlayerExpData data) {
    return getTotalXp(data.getLevel()) + data.getProgress();
  }

  private int getRank(int level) {
    if (level < 5) {
      return 0;
    } else if (level < 10) {
      return 1;
    } else if (level < 20) {
      return 2;
    } else if (level < 30) {
      return 3;
    } else if (level < 50) {
      return 4;
    } else if (level < 70) {
      return 5;
    } else if (level < 100) {
      return 6;
    } else if (level < 130) {
      return 7;
    } else if (level < 160) {
      return 8;
    } else if (level < 200) {
      return 9;
    } else if (level < 250) {
      return 10;
    } else if (level < 300) {
      return 11;
    } else {
      return 12;
    }
  }

  private String getKit(int rank) {
    switch (rank) {
    case 1:
      return "/kit 5";
    case 2:
      return "/kit 10";
    case 3:
      return "/kit 20";
    case 4:
      return "/kit 30";
    case 5:
      return "/kit 50";
    case 6:
      return "/kit 70";
    case 7:
      return "/kit 100";
    case 8:
      return "/kit 130";
    case 9:
      return "/kit 160";
    case 10:
      return "/kit 200";
    case 11:
      return "/kit 250";
    case 12:
      return "/kit 300";
    default:
      return "&cerror";
    }
  }

  private String getSuffix(int level) {
    if (level < 50) {
      return " &8" + level;
    } else if (level < 100) {
      return " &6" + level;
    } else if (level < 200) {
      return " &5" + level;
    } else if (level < 300) {
      return " &b" + level;
    } else {
      return " &b&l" + level;
    }
  }
}
