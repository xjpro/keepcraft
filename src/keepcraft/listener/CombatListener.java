package keepcraft.listener;

import keepcraft.Keepcraft;
import keepcraft.data.models.Armor;
import keepcraft.data.models.User;
import keepcraft.services.ChatService;
import keepcraft.services.UserService;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Random;

public class CombatListener implements Listener {

	private static final float POWER_DAMAGE_BONUS_PER_LEVEL = 0.05f; // each level of Power (bows) gives 5% damage bonus
	private static final float SHARPNESS_DAMAGE_BONUS_PER_LEVEL = 0.05f; // each level of Sharpness (swords & axes) gives 5% damage bonus
	private static final float ARROW_DAMAGE_REDUCTION = 0.15f; // arrow damage reduced by 15%
	private static final int FOOD_REMOVED_ON_ARROW_HIT = 2; // food removed when hit by an arrow
	private static final float PROTECTION_DAMAGE_REDUCTION_PER_POINT = 0.0075f; // damage reduction by point of Protection (armor)
	private static Random random = new Random();

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
		double modifiedBaseDamage;
		boolean isArrowHit;

		if (event.getCause().equals(DamageCause.ENTITY_ATTACK)) {
			// give SummitMC iron_sword 1 0 {ench:[{id:16,lvl:5}]}
			if (!(event.getDamager() instanceof Player)) return; // entity hitting not a player
			damager = (Player) event.getDamager();
			isArrowHit = false;

			int enchantmentLevel = damager.getEquipment().getItemInMainHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL);
			modifiedBaseDamage = getUnenchantedDamageByWeapon(originalDamage, enchantmentLevel);

			// Reapply damage enchantments with reduced formula
			modifiedBaseDamage *= 1 + (enchantmentLevel * SHARPNESS_DAMAGE_BONUS_PER_LEVEL); // Each level gives +5%

			// todo sweeping edge attack

		} else if (event.getCause().equals(DamageCause.PROJECTILE) && event.getDamager() instanceof Arrow) {
			// give SummitMC bow 1 0 {ench:[{id:48,lvl:5}]}
			Arrow arrow = (Arrow) event.getDamager();
			if (!(arrow.getShooter() instanceof Player)) return; // entity hitting not a player
			damager = (Player) arrow.getShooter();
			isArrowHit = true;

			int powerEnchantmentLevel = arrow.hasMetadata("power") ? arrow.getMetadata("power").get(0).asInt() : 0;
			modifiedBaseDamage = getUnenchantedDamageByBow(originalDamage, powerEnchantmentLevel);

			// Reduce base damage of archery overall
			modifiedBaseDamage *= 1 - ARROW_DAMAGE_REDUCTION;

			// Reapply damage enchantments with reduced formula
			modifiedBaseDamage *= 1 + (powerEnchantmentLevel * POWER_DAMAGE_BONUS_PER_LEVEL); // Each level gives +5%
		} else {
			return; // other damage types
		}

		Player damaged = (Player) event.getEntity();
		User damagerUser = userService.getOnlineUser(damager.getName());
		User damagedUser = userService.getOnlineUser(damaged.getName());
		if (damagerUser.getTeam() == damagedUser.getTeam()) {
			// Team members cannot damage each other
			event.setCancelled(true);
			return;
		}

		// When two players damage each other, set both in combat
		damagerUser.setInCombat();
		damagedUser.setInCombat();

		// We assume at this point we've arrived at a good damage amount pre armor and magical protection
		event.setDamage(EntityDamageEvent.DamageModifier.BASE, modifiedBaseDamage);

		// Note: DamageModifiers other than BASE are mitigation and thus should have negative values

		// Apply damage reduction for wearing armor
		double originalDefensePoints = Armor.getDefensePoints(damaged);
		// Defense points modified with diminishing returns formula
		double modifiedDefensePoints = -0.017 * Math.pow(originalDefensePoints, 2) + 1.083 * originalDefensePoints;

		// original formula
		// damage = damage * ( 1 - min( 20, max( defensePoints / 5, defensePoints - damage / ( 2 + toughness / 4 ) ) ) / 25 )
		// Note we completely ignore the toughness attribute that diamond gets, further reducing its effectiveness
		double damageReductionFromArmor = modifiedBaseDamage - (modifiedBaseDamage * (1 - Math.min(20, Math.max(modifiedDefensePoints / 5, modifiedDefensePoints - modifiedBaseDamage / 2)) / 25));

		event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, -damageReductionFromArmor);

		// Apply damage reduction for wearing enchantments
		// give SummitMC diamond_boots 1 0 {ench:[{id:1,lvl:4}]}
		int enchantmentProtectionFactor = Armor.getEnchantmentProtectionFactor(damaged, event.getCause());
		// Each point of enchantment protection provides 0.75% (vanilla is 4%) damage reduction, maxing out at 15%
		double magicalArmorReduction = modifiedBaseDamage * (PROTECTION_DAMAGE_REDUCTION_PER_POINT * enchantmentProtectionFactor);
		event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, -magicalArmorReduction);

		if (isArrowHit && event.getFinalDamage() > 0) {
			// Remove food when damaged by an arrow
			damaged.setFoodLevel(Math.max(0, damaged.getFoodLevel() - FOOD_REMOVED_ON_ARROW_HIT));
		}

//		if (Armor.isWearingFullDiamondArmor(damaged)) {
//			// No knockback
//			damaged.setVelocity(new Vector(0, 0, 0));
//		}

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

//	@EventHandler
//	public void onEntityFallDamage(EntityDamageEvent event) {
//		if (!event.isCancelled() && (event.getEntity() instanceof LivingEntity) && event.getCause() == DamageCause.FALL) {
//			if (Armor.isWearingFullDiamondArmor((LivingEntity) event.getEntity())) {
//				event.setCancelled(true);
//			}
//		}
//	}

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

			if (random.nextDouble() > 0.80) {
				// 20% chance to drop their head
				ItemStack skull = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (byte) 3);
				SkullMeta meta = (SkullMeta) skull.getItemMeta();
				meta.setOwner(player.getName());
				skull.setItemMeta(meta);
				player.getWorld().dropItemNaturally(player.getLocation(), skull);
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
