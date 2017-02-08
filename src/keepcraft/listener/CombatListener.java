package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.data.models.Armor;
import keepcraft.data.models.User;
import keepcraft.services.ChatService;
import keepcraft.services.UserService;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
		DamageCause cause = event.getCause();
		if (cause.equals(DamageCause.ENTITY_ATTACK) || cause.equals(DamageCause.PROJECTILE)) {
			Entity damager = event.getDamager();
			Entity damaged = event.getEntity();

			if (damaged instanceof Player) {
				boolean arrowHit = false;
				Player attacker = null;

				if (damager instanceof Player) {
					attacker = (Player) damager;
				} else if (damager instanceof Arrow) {
					Arrow projectile = (Arrow) damager;
					LivingEntity shooter = (LivingEntity) projectile.getShooter();

					if (shooter instanceof Player) {
						attacker = (Player) shooter;
						arrowHit = true;
					}
				}

				if (attacker != null) {
					User attackingUser = userService.getOnlineUser(attacker.getName());
					Player defender = (Player) damaged;
					User defendingUser = userService.getOnlineUser(defender.getName());

					if (attackingUser.getFaction() == defendingUser.getFaction()) {
						// Team members cannot damage each other
						event.setCancelled(true);
						return;
					}

					// Remove food from bar when hit by an arrow
					if (arrowHit && defender.getFoodLevel() > 0) {
						defender.setFoodLevel(defender.getFoodLevel() - 2);
					}

					// event.getDamage() is damage INCLUDING enchantment power but BEFORE armor mitigation
					// event.getFinalDamage() is damage AFTER enchantment power AND armor mitigation

					//System.out.println("original damage " + event.getDamage());
					//System.out.println("original final damage " + event.getFinalDamage());

					double damageAdditionToBalanceArmor = calcDamageAdditionToBalanceArmor(event, defender);
					double damageAdditionToBalanceProtectionEnchantments = calcDamageAdditionToBalanceProtectionEnchantments(event, defender);
					double damageReductionToBalanceAttackEnchantments = calcDamageReductionToBalanceAttackEnchantments(event, attacker);
					//System.out.println("damageAdditionToBalanceArmor: " + damageAdditionToBalanceArmor);
					//System.out.println("damageAdditionToBalanceProtectionEnchantments: " + damageAdditionToBalanceProtectionEnchantments);
					//System.out.println("damageReductionToBalanceAttackEnchantments: " + damageReductionToBalanceAttackEnchantments);

					event.setDamage(event.getDamage() + damageAdditionToBalanceArmor + damageAdditionToBalanceProtectionEnchantments - damageReductionToBalanceAttackEnchantments);
					//System.out.println("changed damage " + event.getDamage());
					//System.out.println("changed final damage " + event.getFinalDamage());
				}
			}
		}
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
		int armorValue = Armor.getArmorValue(defender.getInventory());
		return event.getDamage() * (armorValue / 75.0);
	}

	private double calcDamageAdditionToBalanceProtectionEnchantments(EntityDamageByEntityEvent event, Player defender) {
		double enchantmentProtectionFactor = Armor.getEnchantmentProtectionFactor(defender.getInventory());
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

}
