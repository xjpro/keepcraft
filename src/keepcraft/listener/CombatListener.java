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
		double originalDamage = event.getDamage(); // todo get from one of the above places
		double baseDamage;
		boolean isArrowHit;

		// todo do we need to reset damage reduction by protection enchantments?

		if (event.getCause().equals(DamageCause.ENTITY_ATTACK)) {
			if (!(event.getDamager() instanceof Player)) return; // entity hitting not a player
			damager = (Player) event.getDamager();
			isArrowHit = false;

			baseDamage = getUnenchantedDamageByWeapon(originalDamage, damager.getEquipment().getItemInMainHand()); // todo is this needed?
			// todo reapply damage enchantments with reduced formula

		} else if (event.getCause().equals(DamageCause.PROJECTILE)) {
			Arrow projectile = (Arrow) event.getDamager();
			if (!(projectile.getShooter() instanceof Player)) return; // entity hitting not a player
			damager = (Player) projectile.getShooter();
			isArrowHit = true;

			baseDamage = getUnenchantedDamageByBow(originalDamage, damager.getEquipment().getItemInMainHand()); // todo is this needed?
			baseDamage *= 0.5; // reduce overall damage by some amount todo tweak amount
			// todo reapply damage enchantments with a reduced formula

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

		// We assume at this point we've arrived at a good damage amount pre armor and prot ench mitigation
		// todo assumption: this allows change to base damage
		event.setDamage(EntityDamageEvent.DamageModifier.BASE, baseDamage);

		// Apply damage reduction for wearing armor
		int defensePoints = Armor.getDefensePoints(damaged);
		defensePoints *= defensePoints / 89.0; // Reduce defense points on sliding scale of 0-22.5% (more armor gets more nerf)

		// Calculate the damage mitigation using the original damage formula but with our reduced armor values
		// Original formula: damage = damage * ( 1 - min( 20, max( defensePoints / 5, defensePoints - damage / ( 2 + toughness / 4 ) ) ) / 25 )
		// Note: "toughness" is ignored, further nerfing diamond armor
		// todo assumption: this allows change to armor modifier
		event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, originalDamage - (originalDamage * (1 - Math.min(20, Math.max(defensePoints / 5, defensePoints - originalDamage / 2)) / 25)));

		// Apply damage reduction for wearing enchantments
		double enchantmentProtectionFactor = Armor.getEnchantmentProtectionFactor(damaged);
		enchantmentProtectionFactor *= 0.6666; // Reduce protection factor by x% todo tweak this amount

		// Calculate the damage mitigation using the original EPF formula but with our reduced EPF
		// Original formula: damage = damage * ( 1 - epf / 25 )
		// todo assumption: this allows change to prot ench modifier
		event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, originalDamage - (originalDamage * (1 - enchantmentProtectionFactor / 25)));

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

	private double calcDamageAdditionToBalanceArmor(EntityDamageByEntityEvent event, Player defender) {
		int armorValue = Armor.getDefensePoints(defender);
		return event.getDamage() * (armorValue / 75.0);
	}

	private double calcDamageAdditionToBalanceProtectionEnchantments(EntityDamageByEntityEvent event, Player defender) {
		double enchantmentProtectionFactor = Armor.getEnchantmentProtectionFactor(defender);
		return event.getDamage() * (enchantmentProtectionFactor / 35.0);
	}

	private double calcDamageReductionToBalanceAttackEnchantments(EntityDamageByEntityEvent event, Player attacker) {
		ItemStack weapon = attacker.getEquipment().getItemInMainHand();
		if (weapon == null) {
			// todo investigate possibility of switching off quickly (probably applies only to bows)
		} else {
			Material weaponType = weapon.getType();
			if (event.getDamager() instanceof Arrow && weaponType != Material.BOW) {
				// todo investigate possibility of switching off quickly (probably applies only to bows)
			}
			// Damage enchanted bow
			else if (weaponType == Material.BOW && weapon.getEnchantments().containsKey(Enchantment.ARROW_DAMAGE)) {
				int enchantmentLevel = weapon.getEnchantmentLevel(Enchantment.ARROW_DAMAGE);

				// Default: Increases arrow damage by 25% × (level + 1), rounded up to nearest half-heart
				double originalModifier = 0.25 * (enchantmentLevel + 1);
				// Adjusted: Run log10(enchantment level + 0.2) to get an adjusted increase that has diminishing returns
				double adjustedModifier = Math.log10(enchantmentLevel + 0.2);

				// level 1 = +50% adjusted to +8%
				// level 2 = +75% adjusted to +34%
				// level 3 = +100% adjusted to +51%
				// level 4 = +125% adjusted to +62%
				// level 5 = +150% adjusted to +72.0%

				double damageWithoutEnchantment = event.getDamage() / (1 + originalModifier);
				return event.getDamage() - (damageWithoutEnchantment * (1 + adjustedModifier));
			}
			// Damage enchanted anything else
			else if (weapon.getEnchantments().containsKey(Enchantment.DAMAGE_ALL)) { // all non-bow, but we can assume a sword
				int enchantmentLevel = weapon.getEnchantmentLevel(Enchantment.DAMAGE_ALL);

				// Adds 1 extra damage for the first level, and 0.5 (Heart.svg × 1⁄4) for each additional level.
				// level 1 = 1 adjusted to .223
				// level 2 = 1.5 adjusted to 0.811
				// level 3 = 2 adjusted to 1.18
				// level 4 = 2.5 adjusted to 1.45
				// level 5 = 3 adjusted to 1.66

				double originalIncreasedDamageFromEnchantment = 1 + ((enchantmentLevel - 1) * .5);
				double adjustedIncreaseDamageFromEnchantment = Math.log(enchantmentLevel + 0.25);
				return originalIncreasedDamageFromEnchantment - adjustedIncreaseDamageFromEnchantment;
			}
		}

		return 0;
	}

	private double getUnenchantedDamageByWeapon(double damage, ItemStack weapon) {
		if (weapon.getEnchantments().containsKey(Enchantment.DAMAGE_ALL)) { // all non-bow, but we can assume a sword
			int enchantmentLevel = weapon.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
			return damage - (0.5 * (enchantmentLevel - 1)) - 1;
		}
		return damage;
	}

	private double getUnenchantedDamageByBow(double damage, ItemStack bow) {
		if (bow == null || bow.getType() != Material.BOW) {
			// todo investigate possibility of switching off quickly (probably applies only to bows)
			return damage;

		}

		// Damage enchanted bow
		if (bow.getEnchantments().containsKey(Enchantment.ARROW_DAMAGE)) {
			int enchantmentLevel = bow.getEnchantmentLevel(Enchantment.ARROW_DAMAGE);
			// undo original formula: Increases arrow damage by 25% × (level + 1), rounded up to nearest half-heart
			return Math.round(damage / (1 + (0.25 * (enchantmentLevel + 1))) * 2) / 2.0;
		}
		return damage;
	}

}
