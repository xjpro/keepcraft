/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.summit.keepcraft.data.models;

import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Me
 */
public class Armor 
{
    private static class ArmorValues
    {
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
    
    public static int getArmorValue(ItemStack[] inventory)
    {
        int totalArmor = 0;
        for(ItemStack item : inventory)
        {
            switch(item.getType())
            {
            case LEATHER_HELMET: totalArmor += ArmorValues.LeatherHelm; break;
            case CHAINMAIL_HELMET: totalArmor += ArmorValues.ChainHelm; break;
            case IRON_HELMET: totalArmor += ArmorValues.IronHelm; break;
            case GOLD_HELMET: totalArmor += ArmorValues.GoldHelm; break;
            case DIAMOND_HELMET: totalArmor += ArmorValues.DiamondHelm; break;
            case LEATHER_CHESTPLATE: totalArmor += ArmorValues.LeatherChest; break;
            case CHAINMAIL_CHESTPLATE: totalArmor += ArmorValues.ChainChest; break;
            case IRON_CHESTPLATE: totalArmor += ArmorValues.IronChest; break;
            case GOLD_CHESTPLATE: totalArmor += ArmorValues.GoldChest; break;
            case DIAMOND_CHESTPLATE: totalArmor += ArmorValues.DiamondChest; break;
            case LEATHER_LEGGINGS: totalArmor += ArmorValues.LeatherLegs; break;
            case CHAINMAIL_LEGGINGS: totalArmor += ArmorValues.ChainLegs; break;
            case IRON_LEGGINGS: totalArmor += ArmorValues.IronLegs; break;
            case GOLD_LEGGINGS: totalArmor += ArmorValues.GoldLegs; break;
            case DIAMOND_LEGGINGS: totalArmor += ArmorValues.DiamondLegs; break;
            case LEATHER_BOOTS: totalArmor += ArmorValues.LeatherBoots; break;
            case CHAINMAIL_BOOTS: totalArmor += ArmorValues.ChainBoots; break;
            case IRON_BOOTS: totalArmor += ArmorValues.IronBoots; break;
            case GOLD_BOOTS: totalArmor += ArmorValues.GoldBoots; break;
            case DIAMOND_BOOTS: totalArmor += ArmorValues.DiamondBoots; break;
            }
        }
        return totalArmor;
    }
            
    
}
