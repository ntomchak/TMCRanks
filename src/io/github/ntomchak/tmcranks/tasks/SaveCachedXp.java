package io.github.ntomchak.tmcranks.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.ntomchak.tmcranks.PlayerExpData;
import io.github.ntomchak.tmcranks.TMCRanks;

public class SaveCachedXp extends BukkitRunnable{
  private HashMap<UUID, PlayerExpData> playerXp;
  private HashMap<UUID, Integer> lastAutoSaved;
  
  public SaveCachedXp(HashMap<UUID, PlayerExpData> playerXp, HashMap<UUID, Integer> lastAutoSaved) {
    this.playerXp = playerXp;
    this.lastAutoSaved = lastAutoSaved;
  }
  
  @Override
  public void run() {
    int updated = 0;
    for (Map.Entry<UUID, PlayerExpData> entry : playerXp.entrySet()) {
      UUID uuid = entry.getKey();
      boolean skip = false;
      int newXp = PlayerExpData.getTotalXp(entry.getValue());
      if (lastAutoSaved.containsKey(entry.getKey())) {
        int oldXp = lastAutoSaved.get(uuid);

        if (oldXp == newXp) {
          skip = true;
        }
      }
      if (!skip) {
        updated++;
        TMCRanks.getInstance().updateDB(uuid, entry.getValue());
        lastAutoSaved.put(uuid, newXp);
      }
    }
    Bukkit.getConsoleSender().sendMessage("saved cached xp, " + updated + " updated, " + playerXp.size()
        + " in hashmap, " + Bukkit.getOnlinePlayers().size() + " players online");
  }
}
