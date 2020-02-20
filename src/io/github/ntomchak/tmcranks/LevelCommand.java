package io.github.ntomchak.tmcranks;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelCommand implements CommandExecutor {

  private CopyOnWriteArrayList<String> top;
  private HashMap<UUID, PlayerExpData> playerXp;

  public LevelCommand(CopyOnWriteArrayList<String> top, HashMap<UUID, PlayerExpData> playerXp) {
    this.top = top;
    this.playerXp = playerXp;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (cmd.getName().equalsIgnoreCase("leveltop")) {
      levelTop(sender, args);
      return true;
    } else if (cmd.getName().equalsIgnoreCase("level")) {
      levelCommand(sender);
      return true;
    }
    return true;
  }

  private void levelTop(CommandSender sender, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(ChatColor.BLUE + "Top Levels #1-10: ");
      for (int i = 0; i < 10; i++) {
        sender.sendMessage(top.get(i).toString());
      }
      sender.sendMessage(
          ChatColor.BLUE + "Use " + ChatColor.DARK_AQUA + "/leveltop 2 " + ChatColor.BLUE + "to see #11-20.");
    } else if (args[0].equalsIgnoreCase("2")) {
      sender.sendMessage(ChatColor.BLUE + "Top Levels #11-20: ");
      for (int i = 10; i < 20; i++) {
        sender.sendMessage(top.get(i).toString());
      }
    }
  }

  private void levelCommand(CommandSender sender) {
    if ((sender instanceof Player)) {
      Player p = (Player) sender;
      String xpPctBar = "";
      String xpToPctBar = "";
      PlayerExpData data = playerXp.get(p.getUniqueId());
      int pctTo = (int) (((float) data.getProgress() / (float) data.getTarget()) * 100);
      for (int i = 0; i < pctTo / 10; i++)
        xpPctBar += "|";
      for (int i = 0; i < (10 - pctTo / 10); i++)
        xpToPctBar += "|";
      p.sendMessage(ChatColor.BLUE + "Your level is: " + ChatColor.DARK_AQUA + data.getLevel());
      p.sendMessage(ChatColor.BLUE + "Progress to level " + (int) (data.getLevel() + 1) + ": " + ChatColor.DARK_AQUA
          + xpPctBar + ChatColor.WHITE + xpToPctBar + ChatColor.DARK_AQUA + " " + pctTo + "%");
    }
  }
}
