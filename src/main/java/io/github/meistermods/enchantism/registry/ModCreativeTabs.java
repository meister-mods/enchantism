package io.github.meistermods.enchantism.registry;

import io.github.meistermods.enchantism.Enchantism;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public final class ModCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB,
            Enchantism.MOD_ID
        );

    public static final RegistryObject<CreativeModeTab> ENCHANTISM_TAB =
        CREATIVE_TABS.register(
            "enchantism",
            () -> CreativeModeTab.builder()
                .title(Component.translatable(
                    "itemGroup.enchantism"
                ))
                .icon(() ->
                    ModBlocks.SPECIAL_ENCHANTMENT_TABLE_ITEM
                        .get()
                        .getDefaultInstance()
                )
                .displayItems((parameters, output) ->
                {
                    output.accept(
                        ModBlocks.SPECIAL_ENCHANTMENT_TABLE_ITEM.get()
                    );

                    output.accept(
                        ModBlocks.ENCHANTMENT_APPLICATOR_ITEM.get()
                    );
                })
                .build()
        );

    private ModCreativeTabs()
    {
    }

    public static void register(IEventBus modEventBus)
    {
        CREATIVE_TABS.register(modEventBus);
    }
}
