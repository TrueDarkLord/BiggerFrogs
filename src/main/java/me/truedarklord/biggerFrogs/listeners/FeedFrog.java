package me.truedarklord.biggerFrogs.listeners;

import me.truedarklord.biggerFrogs.BiggerFrogs;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
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
        double maxSize = plugin.getConfig().getDouble("explosion.max-frog-size", 5);

        if (item == null) return;

        // Validate food
        if (!item.getType().toString().equals(plugin.getConfig().get("frog-food", ""))) return;

        // Check Size
        AttributeInstance sizeAttribute = frog.getAttribute(Attribute.GENERIC_SCALE);
        if (sizeAttribute == null) return;
        double oldSize = sizeAttribute.getBaseValue();

        if (oldSize > maxSize) return;

        // Explode
        if ((oldSize == maxSize) && plugin.getConfig().getBoolean("explosion.explode")) explode(player, frog);

        // Increase size
        takeFood(player, hand, item);
        sizeAttribute.setBaseValue(oldSize + 1);
    }

    private void explode(Player player, LivingEntity livingEntity) {
        World world = livingEntity.getWorld();
        Location loc = livingEntity.getLocation();

        world.playSound(loc, Sound.ENTITY_TNT_PRIMED, 1f, 1f);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            world.spawnParticle(Particle.EXPLOSION, loc, 30);
            world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1f);
            livingEntity.damage(50, player);

            areaDamage(player, livingEntity);
        }, 40);

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
