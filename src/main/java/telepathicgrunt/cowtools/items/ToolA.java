package telepathicgrunt.cowtools.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import telepathicgrunt.cowtools.CowToolsMod;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ToolA extends Item {
    private static final TagKey<Item> COW_TRADE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CowToolsMod.MODID, "tool_a_cow_trade"));
    private static final TagKey<Block> CANNOT_REVERT_STATE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(CowToolsMod.MODID, "tool_a_cannot_revert_state"));

    public ToolA(Properties itemProperty) {
        super(itemProperty.durability(64).component(DataComponents.TOOL, createToolProperties()));
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(), 1.0F, 1, true);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, list, tooltipFlag);
        list.accept(Component.translatable("item.cow_tools.tool_a.hint").withStyle(ChatFormatting.YELLOW).withStyle(ChatFormatting.ITALIC));
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
            stack.hurtAndBreak(1, player, rightClickBlockEvent.getHand());
            level.setBlock(rightClickBlockEvent.getHitVec().getBlockPos(), repairState, 3);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_USE, player.getSoundSource(), 1.0F, 1.0F);
            player.swing(rightClickBlockEvent.getHand(), true);
            rightClickBlockEvent.setCancellationResult(InteractionResult.FAIL);
        }
    }

    private static BlockState repairState(BlockState state) {
        if (state.is(CANNOT_REVERT_STATE)) {
            return null;
        }

        Identifier rl = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String modifiedPath = modifiedPath(rl.getPath());
        if (modifiedPath != null) {
            Optional<Holder.Reference<Block>> blockOptional = BuiltInRegistries.BLOCK.get(Identifier.fromNamespaceAndPath(rl.getNamespace(), modifiedPath));
            if (blockOptional.isPresent() && !blockOptional.get().value().defaultBlockState().isAir()) {
                BlockState newState = blockOptional.get().value().defaultBlockState();
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
        String modifiedPath = replaceFirstMatchSubstring(path, "damaged", "chipped");
        if (!modifiedPath.equals(path)) {
            return modifiedPath;
        }

        modifiedPath = removeFirstMatchedSubstring(path,
                "cracked",
                "mossy",
                "polished",
                "chiseled",
                "smooth",
                "cut",
                "chipped");
        return modifiedPath.equals(path) ? null : modifiedPath;
    }

    private static String replaceFirstMatchSubstring(String original, String match, String replacement) {
        return original
                .replaceFirst("^" + match + "_", replacement + "_")
                .replaceFirst("_" + match + "$", "_" + replacement)
                .replaceFirst("_" + match + "_", "_" + replacement + "_");
    }

    private static String removeFirstMatchedSubstring(String original, String... removals) {
        String newString = original;
        for (String removal : removals) {
            newString = original
                    .replaceFirst("^" + removal + "_", "")
                    .replaceFirst("_" + removal + "$", "")
                    .replaceFirst("_" + removal + "_", "_");

            if (!newString.equals(original)) {
                break;
            }
        }
        return newString;
    }
}
