package ru.mrflaxe.simplehomes.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
    private String material;
	@DatabaseField
	private float yaw;
	@DatabaseField
	private float pitch;
	@DatabaseField
	private int x;
	@DatabaseField
	private int y;
	@DatabaseField
	private int z;
	
	public Home(String playerName, String homeName, String world, float yaw, float pitch, int x, int y, int z) {
		this.playerName = playerName;
		this.homeName = homeName;
		this.world = world;
		this.yaw = yaw;
		this.pitch = pitch;
		this.x = x;
		this.y = y;
		this.z = z;
	    this.material = chooseMaterial().toString();
	}
	
	public Location getLocation() {
		World bukkitWorld = Bukkit.getWorld(world);
		return bukkitWorld != null ? new Location(bukkitWorld, x + 0.5, y, z + 0.5, yaw, pitch) : null;
	}
	
	/**
	 * Gets a default material of this home by special algorithm =)
	 * 
	 * @return a default material of this home which depends on height and biome
	 */
	public Material chooseMaterial() {
	    System.out.println(y);
        if(y <= 30) return Material.STONE;
        
        World bukkitWorld = Bukkit.getWorld(world);
        String biome = bukkitWorld.getBlockAt(x, y, z).getBiome().toString();
        
        if(world.equals("world")) {
            if(biome.startsWith("MOUNTAIN") || biome.equals("GRAVELLY_MOUNTAINS") || biome.equals("STONE_SHORE")) return Material.STONE;
            if(biome.startsWith("BADLANDS") || biome.endsWith("BADLANDS_PLATEAU") || biome.equals("ERODED_BADLANDS")) return Material.ORANGE_TERRACOTTA;
            if(biome.startsWith("FROZEN") || biome.equals("ICE_SPIKES")) return Material.ICE;
            if(biome.startsWith("DEEP_") || biome.endsWith("OCEAN")) return Material.BRAIN_CORAL;
            if(biome.startsWith("JUNGLE") || biome.startsWith("BAMBOO")) return Material.JUNGLE_LOG;
            if(biome.startsWith("DESERT") || biome.equals("BEACH")) return Material.SAND;
            if(biome.startsWith("BRICH") || biome.startsWith("TALL")) return Material.BIRCH_LOG;
            if(biome.startsWith("TAIGA") || biome.startsWith("GIANT")) return Material.SPRUCE_LOG;
            if(biome.startsWith("SAVANNA") || biome.startsWith("SHATTERED")) return Material.ACACIA_LOG;
            if(biome.startsWith("SNOWY")) return Material.SNOW_BLOCK;
            if(biome.startsWith("SWAMP")) return Material.LILY_PAD;
            if(biome.startsWith("DARK")) return Material.DARK_OAK_LOG;
        }
        
        if(world.equals("world_nether")) {
            if(biome.startsWith("BASALT")) return Material.BASALT;
            if(biome.startsWith("CRIMSON")) return Material.CRIMSON_STEM;
            if(biome.startsWith("SOUL")) return Material.SOUL_SAND;
            if(biome.startsWith("WARPED")) return Material.WARPED_STEM;
            return Material.NETHERRACK;
        }
        
        if(world.equals("world_the_end")) return Material.END_STONE;
        
        return Material.GRASS_BLOCK;
    }
}
