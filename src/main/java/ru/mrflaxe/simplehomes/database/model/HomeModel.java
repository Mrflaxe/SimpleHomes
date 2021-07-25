package ru.mrflaxe.simplehomes.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;

import java.util.Objects;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "homes")
public class HomeModel implements Comparable<HomeModel> {
	
    @DatabaseField(generatedId = true)
    private int id;
	@DatabaseField(columnName = "holder", canBeNull = false)
	private String holder;
	@DatabaseField(columnName = "home_name", canBeNull = false)
	private String homeName;
	
	@DatabaseField(columnName = "world", canBeNull = false)
	private String world;
	@DatabaseField(columnName = "x", canBeNull = false)
	private int x;
	@DatabaseField(columnName = "y", canBeNull = false)
	private int y;
	@DatabaseField(columnName = "z", canBeNull = false)
	private int z;
	@DatabaseField(columnName = "yaw", canBeNull = false)
	private float yaw;
	@DatabaseField(columnName = "pitch", canBeNull = false)
	private float pitch;
	
	@DatabaseField(columnName = "icon")
    private String iconMaterial;
	
	public HomeModel(String holder, String homeName, Location location) {
		this.holder = holder;
		this.homeName = homeName;
	    
		this.world = location.getWorld().getName();
		this.x = location.getBlockX();
		this.y = location.getBlockY();
		this.z = location.getBlockZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
		
	    this.iconMaterial = chooseMaterial().toString();
	}
	
	public Location getLocation() {
		World bukkitWorld = Bukkit.getWorld(world);
		return bukkitWorld != null ? new Location(bukkitWorld, x + 0.5, y, z + 0.5, yaw, pitch) : null;
	}
	
	public Material getMaterial() {
	    try {
            Material type = Material.valueOf(iconMaterial.toUpperCase());
            return type != null ? type : Material.GRASS_BLOCK;
        } catch (EnumConstantNotPresentException ex) {
            return Material.GRASS_BLOCK;
        }
	}

	public void changeIcon(String iconMaterial) {
	    this.iconMaterial = iconMaterial;
    }
	
	/**
	 * Gets a default material of this home by special algorithm =)
	 * 
	 * @return a default material of this home which depends on height and biome
	 */
	public Material chooseMaterial() {
        World bukkitWorld = Bukkit.getWorld(world);
        if(bukkitWorld == null) return Material.BARRIER;
        
        Environment environment = bukkitWorld.getEnvironment();
        String biome = bukkitWorld.getBlockAt(x, y, z).getBiome().toString();
        
        // Overworld
        if(environment == Environment.NORMAL) {
            if(y <= 32) return Material.STONE;
            
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
            
        // The Nether
        } else if(environment == Environment.NETHER) {
            if(biome.startsWith("BASALT")) return Material.BASALT;
            if(biome.startsWith("CRIMSON")) return Material.CRIMSON_STEM;
            if(biome.startsWith("SOUL")) return Material.SOUL_SAND;
            if(biome.startsWith("WARPED")) return Material.WARPED_STEM;
            return Material.NETHERRACK;
            
        // The End
        } else if(environment == Environment.THE_END) {
            if(Math.abs(x) < 750 || Math.abs(z) < 750) return Material.END_STONE;
            return Material.CHORUS_FLOWER;
        }
        
        return Material.GRASS_BLOCK;
    }
	
	@Override
	public int compareTo(HomeModel other) {
	    return homeName.compareToIgnoreCase(other.homeName);
	}

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        HomeModel homeModel = (HomeModel) o;
        return id == homeModel.id &&
                x == homeModel.x &&
                y == homeModel.y &&
                z == homeModel.z &&
                Float.compare(homeModel.yaw, yaw) == 0 &&
                Float.compare(homeModel.pitch, pitch) == 0 &&
                Objects.equals(holder, homeModel.holder) &&
                Objects.equals(homeName, homeModel.homeName) &&
                Objects.equals(world, homeModel.world) &&
                Objects.equals(iconMaterial, homeModel.iconMaterial);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, holder, homeName, world, x, y, z, yaw, pitch, iconMaterial);
    }

    @Override
    public String toString() {
        return "Home{" +
                "id=" + id +
                ", holder=" + holder +
                ", homeName='" + homeName + '\'' +
                ", world='" + world + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                ", icon='" + iconMaterial + '\'' +
                '}';
    }

}
