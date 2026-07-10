package io.github.meistermods.enchantism.client.screen;

import io.github.meistermods.enchantism.menu.EnchantmentApplicatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@SuppressWarnings("null")
public final class EnchantmentApplicatorScreen extends AbstractContainerScreen<EnchantmentApplicatorMenu>
{
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(
            "minecraft",
            "textures/gui/container/furnace.png"
        );

    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 17;

    public EnchantmentApplicatorScreen(
        EnchantmentApplicatorMenu menu,
        Inventory inventory,
        Component title
    )
    {
        super(menu, inventory, title);
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
        GuiGraphics graphics,
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
        GuiGraphics graphics,
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

        renderProgressArrow(graphics);
    }

    private void renderProgressArrow(GuiGraphics graphics)
    {
        int progressWidth =
            this.menu.getScaledProgress(PROGRESS_WIDTH);

        if (progressWidth <= 0)
        {
            return;
        }

        /*
         * The furnace texture stores the completed arrow at UV 176, 14.
         * It is drawn gradually from left to right.
         */
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