package telepathicgrunt.cowtools.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import telepathicgrunt.cowtools.CowToolsMod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ToolB extends Item {
    private static final TagKey<EntityType<?>> ADDITIONAL_CAN_PULL = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(CowToolsMod.MODID, "tool_b_additional_can_pull"));
    private static final TagKey<EntityType<?>> CANNOT_PULL_EVER = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(CowToolsMod.MODID, "tool_b_cannot_pull_ever"));
    private static final TagKey<Item> COW_TRADE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CowToolsMod.MODID, "tool_b_cow_trade"));

    public ToolB() {
        super(new Properties().durability(300).component(DataComponents.TOOL, createToolProperties()));
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(), 1.0F, 1);
    }

    public static void grantTool(PlayerInteractEvent.EntityInteract entityInteractEvent) {
        if (entityInteractEvent.getEntity() != null &&
            entityInteractEvent.getTarget().getType().is(CowToolsMod.COWS_FOR_GETTING_TOOLS) &&
            entityInteractEvent.getItemStack().is(COW_TRADE))
        {
            if (!entityInteractEvent.getEntity().getAbilities().instabuild) {
                entityInteractEvent.getItemStack().shrink(1);
            }
            entityInteractEvent.getEntity().addItem(CowToolsMod.TOOL_B.get().getDefaultInstance());
        }
    }

    public static void sweepPull(PlayerInteractEvent.RightClickItem rightClickItemEvent) {
        if (rightClickItemEvent.getEntity() != null) {
            ItemStack stack = rightClickItemEvent.getItemStack();
            if (!stack.is(CowToolsMod.TOOL_B)) {
                return;
            }

            Level level = rightClickItemEvent.getLevel();
            Player player = rightClickItemEvent.getEntity();

            Set<Entity> entitiesToPull = new HashSet<>();
            Vec3 offset = player.getLookAngle().scale(3);
            AABB boundingBox = player.getBoundingBox().move(player.getLookAngle());
            for (int i = 0; i < 3; i++) {
                boundingBox = boundingBox.inflate(1.5, 1.5, 1.5).move(offset);
                for (Entity entity : level.getEntitiesOfClass(Entity.class, boundingBox)) {
                    if ((entity instanceof LivingEntity || entity instanceof Boat || entity instanceof Minecart || entity.getType().is(ADDITIONAL_CAN_PULL))
                        && (!entity.getType().is(CANNOT_PULL_EVER)))
                    {
                        entitiesToPull.add(entity);
                    }
                }
            }

            entitiesToPull.forEach(entity -> {
                double strength = player.distanceTo(entity) / 3.5D;
                Vec3 vec3 = entity.getDeltaMovement();
                Vec3 directionToPlayer = entity.position().subtract(player.position()).normalize();
                double x = directionToPlayer.x();
                double y = directionToPlayer.y();
                double z = directionToPlayer.z();

                while (x * x + y * y + z * z < 1.0E-5F) {
                    x = (Math.random() - Math.random()) * 0.01;
                    y = (Math.random() - Math.random()) * 0.01;
                    z = (Math.random() - Math.random()) * 0.01;
                }

                Vec3 vec31 = new Vec3(x, y, z).normalize().scale(strength);
                entity.setDeltaMovement(
                    vec3.x / 2.0 - vec31.x,
                    vec3.y / 2.0 - vec31.y,
                    vec3.z / 2.0 - vec31.z
                );
            });

            if (!entitiesToPull.isEmpty()) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
                player.swing(rightClickItemEvent.getHand(), true);
                player.sweepAttack();
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(rightClickItemEvent.getHand()));
            }
        }
    }
}
