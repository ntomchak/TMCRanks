package io.github.ntomchak.tmcranks.tasks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.scheduler.BukkitRunnable;

import io.github.ntomchak.tmcranks.TMCRanks;

public class GetTop20 extends BukkitRunnable {
  
  private CopyOnWriteArrayList<String> top;

	public GetTop20(CopyOnWriteArrayList<String> top) {
	  this.top = top;
	}

	public void run() {
	  ArrayList<String> top20 = new ArrayList<String>(20);
		try {
			PreparedStatement statement = TMCRanks.getInstance().getConnection()
					.prepareStatement("SELECT * FROM `players` ORDER BY `xp` DESC LIMIT 20");
			ResultSet rs = statement.executeQuery();
			for (int i = 0; rs.next() && i < 20; i++) {// while(rs.next() && i < 21){
				top20.add( ((i + 1) + ". " + rs.getString("name") + " - " + getLevel(rs.getInt("xp"))));
			}
			rs.close();
			statement.close();
			top.clear();
			top.addAll(top20);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int getLevel(int xp) {
		if (xp < 353) {
			return ((int) Math.sqrt(36 + 4 * xp) - 6) / (2);
		} else if (xp < 1508) {
			return (int) ((40.5 + Math.sqrt(1640.25 - 10 * (360 - xp))) / 5);
		} else {
			return (int) ((162.5 + Math.sqrt(26406.25 - 18 * (2220 - xp))) / 9);
		}
	}

}
