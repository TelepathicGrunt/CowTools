package telepathicgrunt.cowtools.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import telepathicgrunt.cowtools.CowToolsMod;

import java.util.List;

public class ToolA extends Item {
    private static final TagKey<Item> COW_TRADE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CowToolsMod.MODID, "tool_a_cow_trade"));

    public ToolA() {
        super(new Item.Properties().durability(64).component(DataComponents.TOOL, createToolProperties()));
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
            entityInteractEvent.getEntity().addItem(CowToolsMod.TOOL_A.get().getDefaultInstance());
        }
    }

    public static void repairAnvil(PlayerInteractEvent.RightClickBlock rightClickBlockEvent) {
        if (rightClickBlockEvent.getEntity() != null && rightClickBlockEvent.getEntity().isCrouching()) {
            ItemStack stack = rightClickBlockEvent.getItemStack();
            if (!stack.is(CowToolsMod.TOOL_A)) {
                return;
            }

            Level level = rightClickBlockEvent.getLevel();
            BlockState repairState = repairState(level.getBlockState(rightClickBlockEvent.getHitVec().getBlockPos()));
            if (repairState == null) {
                return;
            }

            Player player = rightClickBlockEvent.getEntity();
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(rightClickBlockEvent.getHand()));
            level.setBlock(rightClickBlockEvent.getHitVec().getBlockPos(), repairState, 3);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_USE, player.getSoundSource(), 1.0F, 1.0F);
            player.swing(rightClickBlockEvent.getHand(), true);
            rightClickBlockEvent.setCancellationResult(InteractionResult.FAIL);
        }
    }

    private static BlockState repairState(BlockState state) {
        if (state.is(Blocks.DAMAGED_ANVIL)) {
            return Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING));
        }
        else {
            return state.is(Blocks.CHIPPED_ANVIL) ?
                    Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING)) : null;
        }
    }
}
