package ru.mrflaxe.simplehomes.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "homes")
public class Home {
	
	@DatabaseField
	private String playerName;
	@DatabaseField
	private String name;
	@DatabaseField
	private String world;
	@DatabaseField
	private int x;
	@DatabaseField
	private int y;
	@DatabaseField
	private int z;
	
	public Home(String playerName, String name, String world, int x, int y, int z) {
		this.playerName = playerName;
		this.name = name;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Location getLocation() {
		World bukkitWorld = Bukkit.getWorld(world);
		return bukkitWorld.getBlockAt(x, y, z).getLocation();
	}
}
