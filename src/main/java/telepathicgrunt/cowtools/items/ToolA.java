package telepathicgrunt.cowtools.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import telepathicgrunt.cowtools.CowToolsMod;

import java.util.List;
import java.util.Optional;

public class ToolA extends Item {
    private static final TagKey<Item> COW_TRADE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CowToolsMod.MODID, "tool_a_cow_trade"));

    public ToolA() {
        super(new Item.Properties().durability(64).component(DataComponents.TOOL, createToolProperties()));
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(), 1.0F, 1);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
        list.add(Component.translatable("item.cow_tools.tool_a.hint").withStyle(ChatFormatting.YELLOW).withStyle(ChatFormatting.ITALIC));
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
        else if (state.is(Blocks.CHIPPED_ANVIL)) {
            return Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING));
        }

        ResourceLocation rl = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String modifiedPath = modifiedPath(rl.getPath());
        if (modifiedPath != null) {
            Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), modifiedPath));
            if (block != null && !block.defaultBlockState().isAir()) {
                BlockState newState = block.defaultBlockState();
                for (Property<?> property : state.getProperties()) {
                    if (newState.hasProperty(property)) {
                        newState = getStateWithProperty(newState, state, property);
                    }
                }
                return newState;
            }
        }

        return null;
    }

    private static <T extends Comparable<T>> BlockState getStateWithProperty(BlockState state, BlockState stateToCopy, Property<T> property) {
        return state.setValue(property, stateToCopy.getValue(property));
    }

    private static String modifiedPath(String path) {
        String modifiedPath = replacedSubstrings(path,
                "cracked",
                "mossy",
                "polished",
                "chiseled",
                "smooth",
                "cut");

        return modifiedPath.equals(path) ? null : modifiedPath;
    }

    private static String replacedSubstrings(String original, String... replacements) {
        String newString = original;
        for (String replacement : replacements) {
            newString = original
                    .replaceFirst("^" + replacement + "_", "")
                    .replaceFirst("_" + replacement + "$", "")
                    .replaceFirst("_" + replacement + "_", "_");

            if (!newString.equals(original)) {
                break;
            }
        }
        return newString;
    }
}
