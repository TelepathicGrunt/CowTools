package telepathicgrunt.cowtools;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import telepathicgrunt.cowtools.items.ToolA;
import telepathicgrunt.cowtools.items.ToolB;
import telepathicgrunt.cowtools.items.ToolC;

@Mod(CowToolsMod.MODID)
public class CowToolsMod {
    public static final String MODID = "cow_tools";

    public static final TagKey<EntityType<?>> COWS_FOR_GETTING_TOOLS = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(CowToolsMod.MODID, "cows_for_getting_tools"));

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<Item> TOOL_A = ITEMS.register("tool_a", ToolA::new);
    public static final DeferredItem<Item> TOOL_B = ITEMS.register("tool_b", ToolB::new);
    public static final DeferredItem<Item> TOOL_C = ITEMS.register("tool_c", ToolC::new);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COW_TOOLS_TAB = CREATIVE_MODE_TABS.register("cow_tools",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cow_tools"))
            .icon(() -> TOOL_A.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(TOOL_A.get());
                output.accept(TOOL_B.get());
                output.accept(TOOL_C.get());
            }).build());

    public CowToolsMod(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(ToolA::grantTool);
        NeoForge.EVENT_BUS.addListener(ToolA::repairAnvil);

        NeoForge.EVENT_BUS.addListener(ToolB::grantTool);
        NeoForge.EVENT_BUS.addListener(ToolB::sweepPull);

        NeoForge.EVENT_BUS.addListener(ToolC::grantTool);
        NeoForge.EVENT_BUS.addListener(ToolC::oreBreak);
    }
}
