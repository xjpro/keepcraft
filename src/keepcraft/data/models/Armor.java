package keepcraft.data.models;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
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
				case GOLD_HELMET:
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
				case GOLD_CHESTPLATE:
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
				case GOLD_LEGGINGS:
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
				case GOLD_BOOTS:
					totalArmor += ArmorValue.GoldBoots;
					break;
				case DIAMOND_BOOTS:
					totalArmor += ArmorValue.DiamondBoots;
					break;
			}
		}

		return totalArmor;
	}

	public static int getEnchantmentProtectionFactor(LivingEntity entity) {
		EntityEquipment equipment = entity.getEquipment();
		int enchantmentProtectionFactor = 0;

		ItemStack helmet = equipment.getHelmet();
		if (helmet != null) {
			enchantmentProtectionFactor += helmet.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
		}

		ItemStack chestPlate = equipment.getChestplate();
		if (chestPlate != null) {
			enchantmentProtectionFactor += chestPlate.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
		}

		ItemStack leggings = equipment.getLeggings();
		if (leggings != null) {
			enchantmentProtectionFactor += leggings.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
		}

		ItemStack boots = equipment.getBoots();
		if (boots != null) {
			enchantmentProtectionFactor += boots.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
		}

		return Math.min(enchantmentProtectionFactor, 20);
	}
}
