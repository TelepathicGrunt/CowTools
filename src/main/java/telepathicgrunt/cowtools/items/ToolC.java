package telepathicgrunt.cowtools.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import telepathicgrunt.cowtools.CowToolsMod;

import java.util.List;

public class ToolC extends Item {
    private static final TagKey<Item> COW_TRADE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CowToolsMod.MODID, "tool_c_cow_trade"));
    private static final TagKey<Block> DUPLICATE_DROPS_OF = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(CowToolsMod.MODID, "tool_c_duplicate_drops_of"));
    private static final TagKey<Block> CANNOT_DUPLICATE_DROPS_OF_EVER = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(CowToolsMod.MODID, "tool_c_cannot_duplicate_drops_of_ever"));

    public ToolC() {
        super(new Properties().durability(1000).component(DataComponents.TOOL, createToolProperties()));
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(Tool.Rule.minesAndDrops(DUPLICATE_DROPS_OF, 5.0F)), 1.0F, 1);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
        list.add(Component.translatable("item.cow_tools.tool_c.hint").withStyle(ChatFormatting.YELLOW).withStyle(ChatFormatting.ITALIC));
    }

    public static void grantTool(PlayerInteractEvent.EntityInteract entityInteractEvent) {
        if (entityInteractEvent.getEntity() != null &&
            entityInteractEvent.getTarget().getType().is(CowToolsMod.COWS_FOR_GETTING_TOOLS) &&
            entityInteractEvent.getItemStack().is(COW_TRADE))
        {
            if (!entityInteractEvent.getEntity().getAbilities().instabuild) {
                entityInteractEvent.getItemStack().shrink(1);
            }
            entityInteractEvent.getEntity().addItem(CowToolsMod.TOOL_C.get().getDefaultInstance());
        }
    }

    public static void oreBreak(BlockEvent.BreakEvent breakEvent) {
        if (breakEvent.getPlayer() != null && breakEvent.getLevel() instanceof ServerLevel serverLevel) {
            ItemStack stack = breakEvent.getPlayer().getMainHandItem();
            if (!stack.is(CowToolsMod.TOOL_C)) {
                return;
            }

            BlockState blockState = breakEvent.getState();
            if (blockState.is(DUPLICATE_DROPS_OF) && !blockState.is(CANNOT_DUPLICATE_DROPS_OF_EVER)) {
                for (int extraLootTableRools = 0; extraLootTableRools < 5; extraLootTableRools++) {
                    Block.dropResources(
                            blockState,
                            serverLevel,
                            breakEvent.getPos(),
                            serverLevel.getBlockEntity(breakEvent.getPos()),
                            breakEvent.getPlayer(),
                            stack);
                }
                stack.hurtAndBreak(124, breakEvent.getPlayer(), EquipmentSlot.MAINHAND);
            }
        }
    }
}
