package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.data.models.Armor;
import keepcraft.data.models.User;
import keepcraft.services.ChatService;
import keepcraft.services.UserService;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class CombatListener implements Listener {

	private final UserService userService;

	public CombatListener(UserService userService) {
		this.userService = userService;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) return; // entity being hit not a player

		System.out.println("pre : " + event.getDamage());
		System.out.println("origi damag1: " + event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE));
		System.out.println("origi damag2: " + event.getDamage(EntityDamageEvent.DamageModifier.BASE));
		System.out.println("armor reduc1: " + event.getOriginalDamage(EntityDamageEvent.DamageModifier.ARMOR));
		System.out.println("armor reduc2: " + event.getDamage(EntityDamageEvent.DamageModifier.ARMOR));
		System.out.println("magic reduc1: " + event.getOriginalDamage(EntityDamageEvent.DamageModifier.MAGIC));
		System.out.println("magic reduc2: " + event.getDamage(EntityDamageEvent.DamageModifier.MAGIC));
		System.out.println("final: " + event.getFinalDamage());

		// Determine damager and base damage based on type of attack
		Player damager;
		double originalDamage = event.getDamage();
		double baseDamage;
		boolean isArrowHit;

		if (event.getCause().equals(DamageCause.ENTITY_ATTACK)) {
			// give SummitMC iron_sword 1 0 {ench:[{id:16,lvl:5}]}
			if (!(event.getDamager() instanceof Player)) return; // entity hitting not a player
			damager = (Player) event.getDamager();
			isArrowHit = false;

			baseDamage = getUnenchantedDamageByWeapon(originalDamage, damager.getEquipment().getItemInMainHand());

			// Reapply damage enchantments with reduced formula
			int enchantmentLevel = damager.getEquipment().getItemInMainHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL);
			baseDamage *= 1 + (enchantmentLevel * 0.05); // Each level gives +5%

			// todo sweeping edge attack
			// todo fire enchantment

		} else if (event.getCause().equals(DamageCause.PROJECTILE)) {
			// give SummitMC bow 1 0 {ench:[{id:48,lvl:5}]}
			Arrow projectile = (Arrow) event.getDamager();
			if (!(projectile.getShooter() instanceof Player)) return; // entity hitting not a player
			damager = (Player) projectile.getShooter();
			isArrowHit = true;

			baseDamage = getUnenchantedDamageByBow(originalDamage, damager.getEquipment().getItemInMainHand());
			System.out.println("base damage without ench: " + baseDamage);
			baseDamage *= 0.75; // reduce base damage of archery overall

			// Reapply damage enchantments with reduced formula
			int enchantmentLevel = damager.getEquipment().getItemInMainHand().getEnchantmentLevel(Enchantment.ARROW_DAMAGE);
			baseDamage *= 1 + (enchantmentLevel * 0.05); // Each level gives +5%

			// todo fire enchantment

		} else {
			return; // other damage types
		}

		Player damaged = (Player) event.getEntity();
		User damagerUser = userService.getOnlineUser(damager.getName());
		User damagedUser = userService.getOnlineUser(damaged.getName());
		if (damagerUser.getFaction() == damagedUser.getFaction()) {
			// Team members cannot damage each other
			event.setCancelled(true);
			return;
		}

		if (isArrowHit) {
			// Remove food from bar when hit by an arrow
			if (damaged.getFoodLevel() >= 2) {
				damaged.setFoodLevel(damaged.getFoodLevel() - 2);
			}

			// todo buff shields vs arrows via event.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, ) ?
		}

		// We assume at this point we've arrived at a good damage amount pre armor and magical protection
		event.setDamage(EntityDamageEvent.DamageModifier.BASE, baseDamage);

		// Note: DamageModifiers other than BASE are mitigation and thus should have negative values

		// Apply damage reduction for wearing armor
		double defensePoints = Armor.getDefensePoints(damaged);
		// Reduction of armor on a sliding scale where more reduction is applied when more armor is present
		// Diamond armor (20 points) = 25% less effective than vanilla
		// Iron armor (15 points) = 18.75% less effective than vanilla
		// Leather armor (7 points) = 8.75% less effective than vanilla
		double armorReduction = defensePoints / 80;
		event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, event.getDamage(EntityDamageEvent.DamageModifier.ARMOR) * (1 - armorReduction));

		// Apply damage reduction for wearing enchantments
		int enchantmentProtectionFactor = Armor.getEnchantmentProtectionFactor(damaged);
		// Each point of enchantment protection provides 1% (vanilla is 4%) damage reduction, maxing out at 20%
		double magicalArmorReduction = originalDamage * (1 - enchantmentProtectionFactor / 100);
		event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, -magicalArmorReduction);

		// todo thorns

		System.out.println("post: " + event.getFinalDamage());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(EntityDeathEvent event) {
		event.setDroppedExp(0);

		if (event.getEntity() instanceof Player) {
			// Get target
			Player player = (Player) event.getEntity();
			User target = userService.getOnlineUser(player.getName());
			if (target == null) {
				Keepcraft.error("Unknown player died");
				return;
			}

			if (target.isAdmin()) {
				event.getDrops().clear();
			}

			PlayerDeathEvent e = (PlayerDeathEvent) event;

			String message = e.getDeathMessage();
			String[] parts = message.trim().split(" ");
			String attackerName = parts[parts.length - 1];

			User attackerUser = userService.getOnlineUser(attackerName);

			if (attackerUser != null) {
				String causeSection = "";
				for (int i = 1; i < parts.length - 1; i++) {
					causeSection += parts[i] + " ";
				}

				e.setDeathMessage(target.getColoredName() + ChatService.Info + " " + causeSection + attackerUser.getColoredName());
			} else {
				String causeSection = "";
				for (int i = 1; i < parts.length; i++) {
					causeSection += parts[i] + " ";
				}

				e.setDeathMessage(target.getColoredName() + ChatService.Info + " " + causeSection);
			}
		}
		// This was put in to stop farmable monster spawners:
//        else {
//            Entity target = event.getEntity();
//            EntityDamageEvent damageEvent = target.getLastDamageCause();
//            DamageCause cause = (damageEvent == null) ? null : damageEvent.getCause();
//            if (cause == null || !cause.equals(DamageCause.ENTITY_ATTACK)) {
//                event.setDroppedExp(0);
//                event.getDrops().clear();
//            }
//        }
	}

	private double getUnenchantedDamageByWeapon(double damage, ItemStack weapon) {
		int enchantmentLevel = weapon.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
		return damage - (0.5 * (enchantmentLevel - 1)) - 1;
	}

	private double getUnenchantedDamageByBow(double damage, ItemStack bow) {
		if (bow == null || bow.getType() != Material.BOW) {
			// todo investigate possibility of switching off quickly (probably applies only to bows)
			return damage;
		}

		int enchantmentLevel = bow.getEnchantmentLevel(Enchantment.ARROW_DAMAGE);
		return enchantmentLevel > 0 ? Math.round(damage / (1 + (0.25 * (enchantmentLevel + 1))) * 2) / 2.0 : damage;
	}

}
