package io.github.meistermods.enchantism;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

// Example common configuration.
// This class keeps the original Forge MDK examples so they can be replaced
// gradually as Enchantism's actual configuration options are implemented.
@Mod.EventBusSubscriber(modid = Enchantism.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block during common setup.")
            .define("logDirtBlock", true);

    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("An example integer configuration value.")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("The message displayed before the example magic number.")
            .define("magicNumberIntroduction", "The magic number is... ");

    // A list of item resource locations to log during common setup.
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of item IDs to log during common setup.")
            .defineListAllowEmpty(
                    "items",
                    List.of("minecraft:iron_ingot"),
                    Config::validateItemName
            );

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction = "";
    public static Set<Item> items = Set.of();

    private static boolean validateItemName(final Object value)
    {
        if (!(value instanceof String itemName))
        {
            return false;
        }

        ResourceLocation location = ResourceLocation.tryParse(itemName);
        return location != null && ForgeRegistries.ITEMS.containsKey(location);
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        // Ignore configuration events belonging to other mods or config files.
        if (event.getConfig().getSpec() != SPEC)
        {
            return;
        }

        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // Convert configured item IDs into a set of registered items.
        items = ITEM_STRINGS.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .map(ForgeRegistries.ITEMS::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }
}
