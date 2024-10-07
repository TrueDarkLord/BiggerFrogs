package me.truedarklord.biggerFrogs.listeners;

import me.truedarklord.biggerFrogs.BiggerFrogs;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class FeedFrog implements Listener {

    private final BiggerFrogs plugin;

    public FeedFrog(BiggerFrogs plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onFeed(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Frog frog)) return;

        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        ItemStack item = player.getInventory().getItem(hand);

        if (item == null) return;

        if (!item.getType().toString().equals(plugin.getConfig().get("frog-food", ""))) return;

        takeFood(player, hand, item);

        AttributeInstance size = frog.getAttribute(Attribute.GENERIC_SCALE);
        if (size == null) return;

        double oldSize = size.getBaseValue();
        size.setBaseValue(++oldSize);

        if (oldSize > plugin.getConfig().getDouble("explosion.max-frog-size", 5)) {
            frog.getWorld().spawnParticle(Particle.EXPLOSION, frog.getLocation(), 20);
            frog.getWorld().playSound(frog.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2f, 1f);
            frog.damage(50, player);

            areaDamage(player, frog);
        }
    }

    private void areaDamage(Player damager, Entity frog) {
        double range = plugin.getConfig().getDouble("explosion.range", 5.0);
        double damage = plugin.getConfig().getDouble("explosion.damage", 20.0);

        if (!(damage > 0)) return;

        damager.getNearbyEntities(range, range, range).forEach(x -> {
            if (x instanceof LivingEntity) explosionDamage(damager, frog, (LivingEntity) x, damage);
        });
    }

    private void explosionDamage(Player damager, Entity frog, LivingEntity damagee, double damage) {
        DamageSource.Builder damageSource = DamageSource.builder(DamageType.EXPLOSION)
            .withDirectEntity(frog)
            .withCausingEntity(damager);

        EntityDamageEvent event = new EntityDamageEvent(damagee, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, damageSource.build(), damage);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        damagee.damage(event.getDamage(), damageSource.build());
    }

    private void takeFood(Player player, EquipmentSlot slot, ItemStack item) {
        int foodAmount = item.getAmount() - 1;

        if (foodAmount == 0) {
            player.getInventory().setItem(slot, null);
            return;
        }
        item.setAmount(foodAmount);

    }

}
