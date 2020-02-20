package io.github.ntomchak.tmcranks;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.github.ntomchak.tmcranks.tasks.OnLogin;

public class Listeners implements Listener {
  
  private HashMap<UUID, PlayerExpData> playerXp;
  private HashMap<UUID, Integer> lastAutoSaved;
  private HashMap<UUID, Integer> addLater;
  
  
  public Listeners(HashMap<UUID, PlayerExpData> playerXp, HashMap<UUID, Integer> lastAutoSaved, HashMap<UUID, Integer> addLater) {
    this.playerXp = playerXp;
    this.lastAutoSaved = lastAutoSaved;
    this.addLater = addLater;
  }
  
  @EventHandler
  public void onExpGet(PlayerExpChangeEvent event) {
    Player p = event.getPlayer();
    int amount = event.getAmount();
    if (!playerXp.containsKey(p.getUniqueId())) {
      System.out.println(p.getName() + " got xp before their data loaded from the TMCRanks database.");
      if (!addLater.containsKey(p.getUniqueId())) {
        addLater.put(p.getUniqueId(), amount);
      } else {
        int curr = addLater.get(p.getUniqueId());
        addLater.put(p.getUniqueId(), curr + amount);
      }
    } else {
      playerXp.get(p.getUniqueId()).addXp(amount, p.getUniqueId());
    }
  }
  
  @EventHandler
  public void onDisconnect(PlayerQuitEvent event) {
    Player p = event.getPlayer();
    if (playerXp.containsKey(p.getUniqueId())) {
      PlayerExpData data = playerXp.get(p.getUniqueId());
      playerXp.remove(p.getUniqueId());
      lastAutoSaved.remove(p.getUniqueId());
      TMCRanks.getInstance().updateDB(p.getUniqueId(), data);
    }
  }
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    OnLogin onlo = new OnLogin(event.getPlayer().getUniqueId(), event.getPlayer().getName(), addLater, playerXp);

    onlo.runTaskAsynchronously(TMCRanks.getInstance());
  }

}
