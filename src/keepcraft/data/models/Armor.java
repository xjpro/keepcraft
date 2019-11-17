package keepcraft.data.models;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class Armor {

	private static class ArmorValue {
		static short LeatherHelm = 1;
		static short ChainHelm = 2;
		static short IronHelm = 2;
		static short GoldHelm = 2;
		static short DiamondHelm = 3;

		static short LeatherChest = 3;
		static short ChainChest = 5;
		static short IronChest = 6;
		static short GoldChest = 5;
		static short DiamondChest = 8;

		static short LeatherLegs = 2;
		static short ChainLegs = 4;
		static short IronLegs = 5;
		static short GoldLegs = 3;
		static short DiamondLegs = 6;

		static short LeatherBoots = 1;
		static short ChainBoots = 1;
		static short IronBoots = 2;
		static short GoldBoots = 1;
		static short DiamondBoots = 3;
	}

	public static int FULL_IRON_ARMOR = ArmorValue.IronHelm + ArmorValue.IronChest + ArmorValue.IronLegs + ArmorValue.IronBoots;

	public static boolean isWearingFullLeatherArmor(LivingEntity entity) {
		EntityEquipment equipment = entity.getEquipment();
		if (equipment.getHelmet() == null || equipment.getHelmet().getType() != Material.LEATHER_HELMET)
			return false;
		if (equipment.getChestplate() == null || equipment.getChestplate().getType() != Material.LEATHER_CHESTPLATE)
			return false;
		if (equipment.getLeggings() == null || equipment.getLeggings().getType() != Material.LEATHER_LEGGINGS)
			return false;
		if (equipment.getBoots() == null || equipment.getBoots().getType() != Material.LEATHER_BOOTS)
			return false;
		return true;
	}

	public static boolean isWearingFullDiamondArmor(LivingEntity entity) {
		EntityEquipment equipment = entity.getEquipment();
		if (equipment.getHelmet() == null || equipment.getHelmet().getType() != Material.DIAMOND_HELMET)
			return false;
		if (equipment.getChestplate() == null || equipment.getChestplate().getType() != Material.DIAMOND_CHESTPLATE)
			return false;
		if (equipment.getLeggings() == null || equipment.getLeggings().getType() != Material.DIAMOND_LEGGINGS)
			return false;
		if (equipment.getBoots() == null || equipment.getBoots().getType() != Material.DIAMOND_BOOTS)
			return false;
		return true;
	}

	public static int getDefensePoints(LivingEntity entity) {
		EntityEquipment equipment = entity.getEquipment();
		int totalArmor = 0;

		ItemStack helmet = equipment.getHelmet();
		if (helmet != null) {
			switch (helmet.getType()) {
				case LEATHER_HELMET:
					totalArmor += ArmorValue.LeatherHelm;
					break;
				case CHAINMAIL_HELMET:
					totalArmor += ArmorValue.ChainHelm;
					break;
				case IRON_HELMET:
					totalArmor += ArmorValue.IronHelm;
					break;
				case GOLDEN_HELMET:
					totalArmor += ArmorValue.GoldHelm;
					break;
				case DIAMOND_HELMET:
					totalArmor += ArmorValue.DiamondHelm;
					break;
			}
		}

		ItemStack chestPlate = equipment.getChestplate();
		if (chestPlate != null) {
			switch (chestPlate.getType()) {
				case LEATHER_CHESTPLATE:
					totalArmor += ArmorValue.LeatherChest;
					break;
				case CHAINMAIL_CHESTPLATE:
					totalArmor += ArmorValue.ChainChest;
					break;
				case IRON_CHESTPLATE:
					totalArmor += ArmorValue.IronChest;
					break;
				case GOLDEN_CHESTPLATE:
					totalArmor += ArmorValue.GoldChest;
					break;
				case DIAMOND_CHESTPLATE:
					totalArmor += ArmorValue.DiamondChest;
					break;
			}
		}

		ItemStack leggings = equipment.getLeggings();
		if (leggings != null) {
			switch (leggings.getType()) {
				case LEATHER_LEGGINGS:
					totalArmor += ArmorValue.LeatherLegs;
					break;
				case CHAINMAIL_LEGGINGS:
					totalArmor += ArmorValue.ChainLegs;
					break;
				case IRON_LEGGINGS:
					totalArmor += ArmorValue.IronLegs;
					break;
				case GOLDEN_LEGGINGS:
					totalArmor += ArmorValue.GoldLegs;
					break;
				case DIAMOND_LEGGINGS:
					totalArmor += ArmorValue.DiamondLegs;
					break;
			}
		}

		ItemStack boots = equipment.getBoots();
		if (boots != null) {
			switch (boots.getType()) {
				case LEATHER_BOOTS:
					totalArmor += ArmorValue.LeatherBoots;
					break;
				case CHAINMAIL_BOOTS:
					totalArmor += ArmorValue.ChainBoots;
					break;
				case IRON_BOOTS:
					totalArmor += ArmorValue.IronBoots;
					break;
				case GOLDEN_BOOTS:
					totalArmor += ArmorValue.GoldBoots;
					break;
				case DIAMOND_BOOTS:
					totalArmor += ArmorValue.DiamondBoots;
					break;
			}
		}

		return totalArmor;
	}

	public static int getEnchantmentProtectionFactor(LivingEntity entity, EntityDamageEvent.DamageCause damageCause) {
		EntityEquipment equipment = entity.getEquipment();
		int enchantmentProtectionFactor = 0;
		enchantmentProtectionFactor += getItemProtectionFactor(equipment.getHelmet(), damageCause);
		enchantmentProtectionFactor += getItemProtectionFactor(equipment.getChestplate(), damageCause);
		enchantmentProtectionFactor += getItemProtectionFactor(equipment.getLeggings(), damageCause);
		enchantmentProtectionFactor += getItemProtectionFactor(equipment.getBoots(), damageCause);
		return Math.min(enchantmentProtectionFactor, 20);
	}

	private static int getItemProtectionFactor(ItemStack item, EntityDamageEvent.DamageCause damageCause) {
		if (item == null) return 0;

		int enchantmentProtectionFactor = item.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL); // gives level * 1 points

		// TODO check that these work:
		if (damageCause == EntityDamageEvent.DamageCause.PROJECTILE) {
			enchantmentProtectionFactor += item.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE) * 2; // gives level * 2 points
		} else if (damageCause == EntityDamageEvent.DamageCause.FIRE || damageCause == EntityDamageEvent.DamageCause.FIRE_TICK || damageCause == EntityDamageEvent.DamageCause.LAVA) {
			enchantmentProtectionFactor += item.getEnchantmentLevel(Enchantment.PROTECTION_FIRE) * 2; // gives level * 2 points
		} else if (damageCause == EntityDamageEvent.DamageCause.FALL) {
			enchantmentProtectionFactor += item.getEnchantmentLevel(Enchantment.PROTECTION_FALL) * 3; // gives level * 3 points
		} else if (damageCause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || damageCause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
			enchantmentProtectionFactor += item.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS) * 2; // gives level * 2 points
		}
		return enchantmentProtectionFactor;
	}
}
