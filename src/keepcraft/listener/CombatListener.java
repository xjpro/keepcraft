package keepcraft.listener;

import keepcraft.services.UserService;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import keepcraft.Chat;
import keepcraft.data.models.Armor;
import keepcraft.data.models.User;

public class CombatListener implements Listener {

    private final UserService userService;

    public CombatListener(UserService userService) {
        this.userService = userService;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent event) {
        DamageCause cause = event.getCause();
        if (cause.equals(DamageCause.ENTITY_ATTACK) || cause.equals(DamageCause.PROJECTILE)) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
            Entity damager = damageEvent.getDamager();
            Entity damaged = damageEvent.getEntity();

            if (damaged instanceof Player) {
                boolean arrowHit = false;
                Player attacker = null;

                if (damager instanceof Player) {
                    attacker = (Player) damager;
                } else if (damager instanceof Arrow) {
                    Arrow proj = (Arrow) damager;
                    LivingEntity shooter = (LivingEntity) proj.getShooter();
                    if (shooter instanceof Player) {
                        attacker = (Player) shooter;
                        arrowHit = true;
                    }
                } else if (damager instanceof ThrownPotion) {
                    // TODO: implement potion shit someday, when Bukkit updates
                }

                if (attacker != null) {
                    User attackingUser = userService.getOnlineUser(attacker.getName());
                    Player defender = (Player) damaged;
                    User defendingUser = userService.getOnlineUser(defender.getName());

                    if (attackingUser.getFaction() == defendingUser.getFaction()) {
                        event.setCancelled(true);
                        return;
                    }

                    int armorValue = Armor.getArmorValue(defender.getInventory().getArmorContents());
                    int extraDamage = (int) Math.ceil(event.getDamage() * (armorValue / 75.0));

                    // In order to prevent infinite loops but still get proper death messages
                    // set health to 0 if this extra damage is going to kill the player
                    if (defender.getHealth() - extraDamage <= 0) {
                        defender.setHealth(0);
                    } else {
                        defender.damage(extraDamage); // damage has no source so it doesn't loop back into this same function
                    }

                    if (arrowHit && defender.getFoodLevel() > 0) {
                        defender.setFoodLevel(defender.getFoodLevel() - 3);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            // Get target
            Player p = (Player) event.getEntity();
            User target = userService.getOnlineUser(p.getName());

            if (target != null && target.isAdmin()) {
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

                e.setDeathMessage(target.getColoredName() + Chat.Info + " " + causeSection + attackerUser.getColoredName());
            } else {
                String causeSection = "";
                for (int i = 1; i < parts.length; i++) {
                    causeSection += parts[i] + " ";
                }

                e.setDeathMessage(target.getColoredName() + Chat.Info + " " + causeSection);
            }
        } else {
            Entity target = event.getEntity();
            EntityDamageEvent damageEvent = target.getLastDamageCause();
            DamageCause cause = (damageEvent == null) ? null : damageEvent.getCause();
            if (cause == null || !cause.equals(DamageCause.ENTITY_ATTACK)) {
                event.setDroppedExp(0);
                event.getDrops().clear();
            }
        }
    }

}
