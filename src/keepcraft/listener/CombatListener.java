package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.data.models.Armor;
import keepcraft.data.models.User;
import keepcraft.services.ChatService;
import keepcraft.services.UserService;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.metadata.FixedMetadataValue;

public class CombatListener implements Listener {

	private static float PowerDamageBonusPerLevel = 0.05f; // each level of Power (bows) gives 5% damage bonus
	private static float SharpnessDamageBonusPerLevel = 0.05f; // each level of Sharpness (swords & axes) gives 5% damage bonus
	private static float ArrowDamageReduction = 0.15f; // arrow damage reduced by 15%
	private static int FoodRemovedOnArrowHit = 2; // food removed when hit by an arrow
	private static float ProtectionDamageReductionPerPoint = 0.0075f; // damage reduction by point of Protection (armor)

	private final UserService userService;

	public CombatListener(UserService userService) {
		this.userService = userService;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) return; // entity being hit not a player

//		System.out.println("----start----");
//		System.out.println("base: " + event.getDamage(EntityDamageEvent.DamageModifier.BASE));
//		System.out.println("armor: " + event.getDamage(EntityDamageEvent.DamageModifier.ARMOR));
//		System.out.println("magic: " + event.getDamage(EntityDamageEvent.DamageModifier.MAGIC));
//		System.out.println("final: " + event.getFinalDamage());

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

			int enchantmentLevel = damager.getEquipment().getItemInMainHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL);
			baseDamage = getUnenchantedDamageByWeapon(originalDamage, enchantmentLevel);

			// Reapply damage enchantments with reduced formula
			baseDamage *= 1 + (enchantmentLevel * SharpnessDamageBonusPerLevel); // Each level gives +5%

			// todo sweeping edge attack

		} else if (event.getCause().equals(DamageCause.PROJECTILE) && event.getDamager() instanceof Arrow) {
			// give SummitMC bow 1 0 {ench:[{id:48,lvl:5}]}
			Arrow arrow = (Arrow) event.getDamager();
			if (!(arrow.getShooter() instanceof Player)) return; // entity hitting not a player
			damager = (Player) arrow.getShooter();
			isArrowHit = true;

			int powerEnchantmentLevel = arrow.hasMetadata("power") ? arrow.getMetadata("power").get(0).asInt() : 0;
			baseDamage = getUnenchantedDamageByBow(originalDamage, powerEnchantmentLevel);

			// Reduce base damage of archery overall
			baseDamage *= 1 - ArrowDamageReduction;

			// Reapply damage enchantments with reduced formula
			baseDamage *= 1 + (powerEnchantmentLevel * PowerDamageBonusPerLevel); // Each level gives +5%
		} else {
			return; // other damage types
		}

		Player damaged = (Player) event.getEntity();
		User damagerUser = userService.getOnlineUser(damager.getName());
		User damagedUser = userService.getOnlineUser(damaged.getName());
		if (damagerUser.getTeam() == damagedUser.getTeam()) {
			// todo Since the addition of teams (see: TeamService) this if block should no longer be necessary
			// Team members cannot damage each other
			event.setCancelled(true);
			return;
		}

		// When two players damage each other, set both in combat
		damagerUser.setInCombat();
		damagedUser.setInCombat();

		// We assume at this point we've arrived at a good damage amount pre armor and magical protection
		event.setDamage(EntityDamageEvent.DamageModifier.BASE, baseDamage);

		// Note: DamageModifiers other than BASE are mitigation and thus should have negative values

		// Apply damage reduction for wearing armor
		double defensePoints = Armor.getDefensePoints(damaged);
		// Reduce armor on a sliding scale where more reduction is applied when more armor is present
		// Diamond armor (20 points) = 25% less effective than vanilla
		// Iron armor (15 points) = 18.75% less effective than vanilla
		// Leather armor (7 points) = 8.75% less effective than vanilla
		defensePoints *= 1 + (defensePoints / 80);

		// original formula
		// damage = damage * ( 1 - min( 20, max( defensePoints / 5, defensePoints - damage / ( 2 + toughness / 4 ) ) ) / 25 )
		// Note we completely ignore the toughness attribute that diamond gets, further reducing its effectiveness
		double damageReductionFromArmor = baseDamage - (baseDamage * (1 - Math.min(20, Math.max(defensePoints / 5, defensePoints - baseDamage / 2)) / 25));

		event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, -damageReductionFromArmor);

		// Apply damage reduction for wearing enchantments
		int enchantmentProtectionFactor = Armor.getEnchantmentProtectionFactor(damaged, event.getCause());
		// Each point of enchantment protection provides 0.75% (vanilla is 4%) damage reduction, maxing out at 15%
		double magicalArmorReduction = baseDamage * (ProtectionDamageReductionPerPoint * enchantmentProtectionFactor);
		event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, -magicalArmorReduction);

		if (isArrowHit && event.getFinalDamage() > 0) {
			// Remove food when damaged by an arrow
			damaged.setFoodLevel(Math.max(0, damaged.getFoodLevel() - FoodRemovedOnArrowHit));
		}

		// todo thorns
//		System.out.println("----end----");
//		System.out.println("base: " + event.getDamage(EntityDamageEvent.DamageModifier.BASE));
//		System.out.println("armor: " + event.getDamage(EntityDamageEvent.DamageModifier.ARMOR));
//		System.out.println("magic: " + event.getDamage(EntityDamageEvent.DamageModifier.MAGIC));
//		System.out.println("final: " + event.getFinalDamage());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onShootBow(EntityShootBowEvent event) {
		if (event.isCancelled()) return;
		if (event.getEntity() instanceof Player && event.getProjectile() instanceof Arrow) {
			event.getProjectile().setMetadata("power", new FixedMetadataValue(Keepcraft.getPlugin(), event.getBow().getEnchantmentLevel(Enchantment.ARROW_DAMAGE)));
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityCombustByEnchantedWeapon(EntityCombustByEntityEvent event) {
		if (event.isCancelled()) return;
		//System.out.println(event.getEntityType() + " " + event.getEntity());

		if (event.getEntityType().equals(EntityType.ARROW)) {
			// todo seems to be a bug here where friendlies can light each other on fire
			// Flame arrows, by default burn target for 5 seconds
			// There's only one level to this so we don't need to check
			// Reduce duration to 2 seconds
			event.getCombuster().setFireTicks(40);
		} else if (event.getEntityType().equals(EntityType.PLAYER)) {
			// Fire Aspect weapons, by default burn target for 4 seconds (80 time tick) per level
			int enchantmentLevel = ((Player) event.getEntity()).getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.FIRE_ASPECT);

			// Reduce duration to 2 seconds per level
			event.getCombuster().setFireTicks(enchantmentLevel * 40);
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
				target.setInCombat();

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

	private double getUnenchantedDamageByWeapon(double damage, int sharpnessEnchantmentLevel) {
		return sharpnessEnchantmentLevel > 0 ? damage - (0.5 * (sharpnessEnchantmentLevel - 1)) - 1 : damage;
	}

	private double getUnenchantedDamageByBow(double damage, int powerEnchantmentLevel) {
		return powerEnchantmentLevel > 0 ? Math.round(damage / (1 + (0.25 * (powerEnchantmentLevel + 1))) * 2) / 2.0 : damage;
	}

}
