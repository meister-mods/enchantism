package io.github.meistermods.enchantism.client.screen;

import javax.annotation.Nonnull;

import io.github.meistermods.enchantism.menu.ElementInfuserMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@SuppressWarnings({"null", "deprecation"})
public final class ElementInfuserScreen
        extends AbstractContainerScreen<ElementInfuserMenu>
{
    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryParse(
                    "minecraft:textures/gui/container/furnace.png"
            );

    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 17;

    public ElementInfuserScreen(
            @Nonnull ElementInfuserMenu menu,
            @Nonnull Inventory inventory,
            @Nonnull Component title
    )
    {
        super(
                menu,
                inventory,
                title
        );
    }

    @Override
    protected void init()
    {
        super.init();

        this.titleLabelX = 8;
        this.titleLabelY = 6;

        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    public void render(
            @Nonnull GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    )
    {
        this.renderBackground(graphics);

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        this.renderTooltip(
                graphics,
                mouseX,
                mouseY
        );
    }

    @Override
    protected void renderBg(
            @Nonnull GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    )
    {
        graphics.blit(
                TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0,
                this.imageWidth,
                this.imageHeight
        );

        this.renderProgressArrow(graphics);
    }

    private void renderProgressArrow(
            @Nonnull GuiGraphics graphics
    )
    {
        int progressWidth =
                this.menu.getScaledProgress(
                        PROGRESS_WIDTH
                );

        if (progressWidth <= 0)
        {
            return;
        }

        graphics.blit(
                TEXTURE,
                this.leftPos + 79,
                this.topPos + 34,
                176,
                14,
                progressWidth,
                PROGRESS_HEIGHT
        );
    }
}