/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package keepcraft.data.models;

import org.bukkit.inventory.ItemStack;

public class Armor {

    private static class ArmorValue {

        public static short LeatherHelm = 1;
        public static short ChainHelm = 2;
        public static short IronHelm = 2;
        public static short GoldHelm = 2;
        public static short DiamondHelm = 3;
        public static short PumpkinHelm = 0;

        public static short LeatherChest = 3;
        public static short ChainChest = 5;
        public static short IronChest = 6;
        public static short GoldChest = 5;
        public static short DiamondChest = 8;

        public static short LeatherLegs = 2;
        public static short ChainLegs = 4;
        public static short IronLegs = 5;
        public static short GoldLegs = 3;
        public static short DiamondLegs = 6;

        public static short LeatherBoots = 1;
        public static short ChainBoots = 1;
        public static short IronBoots = 2;
        public static short GoldBoots = 1;
        public static short DiamondBoots = 3;
    }

    public static int getArmorValue(ItemStack[] inventory) {
        int totalArmor = 0;
        for (ItemStack item : inventory) {
            switch (item.getType()) {
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

}
