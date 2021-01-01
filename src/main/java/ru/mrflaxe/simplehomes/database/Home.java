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
	
    @DatabaseField(generatedId = true)
    private int id;
	@DatabaseField(columnName = "player_name")
	private String playerName;
	@DatabaseField(columnName = "home_name")
	private String homeName;
	@DatabaseField
	private String world;
	@DatabaseField
	private int x;
	@DatabaseField
	private int y;
	@DatabaseField
	private int z;
	
	public Home(String playerName, String homeName, String world, int x, int y, int z) {
		this.playerName = playerName;
		this.homeName = homeName;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Location getLocation() {
		World bukkitWorld = Bukkit.getWorld(world);
		return bukkitWorld != null ? new Location(bukkitWorld, x + 0.5, y, z + 0.5) : null;
	}
}
