package io.github.meistermods.enchantism.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.meistermods.enchantism.menu.SpecialEnchantmentMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class SpecialEnchantmentScreen extends AbstractContainerScreen<SpecialEnchantmentMenu>
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    "minecraft",
                    "textures/gui/container/enchanting_table.png"
            );

    private static final int OPTION_X = 60;
    private static final int OPTION_Y = 14;
    private static final int OPTION_WIDTH = 108;
    private static final int OPTION_HEIGHT = 19;

    public SpecialEnchantmentScreen(
            SpecialEnchantmentMenu menu,
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
        this.inventoryLabelX = 8;
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
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    )
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.blit(
                TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0,
                this.imageWidth,
                this.imageHeight
        );

        for (int option = 0; option < 3; option++)
        {
            renderOption(graphics, option, mouseX, mouseY);
        }
    }

    private void renderOption(
            GuiGraphics graphics,
            int option,
            int mouseX,
            int mouseY
    )
    {
        int x = this.leftPos + OPTION_X;
        int y = this.topPos + OPTION_Y + option * OPTION_HEIGHT;
        int requiredLevel = this.menu.costs[option];
        int materialCost = option + 1;

        boolean hovered = mouseX >= x
                && mouseX < x + OPTION_WIDTH
                && mouseY >= y
                && mouseY < y + OPTION_HEIGHT;

        boolean usable = canUseOption(option);

        int backgroundColor;

        if (requiredLevel <= 0)
        {
            backgroundColor = 0x70000000;
        }
        else if (!usable)
        {
            backgroundColor = hovered ? 0x90A03030 : 0x80702020;
        }
        else
        {
            backgroundColor = hovered ? 0xA0608040 : 0x80506030;
        }

        graphics.fill(
                x,
                y,
                x + OPTION_WIDTH,
                y + OPTION_HEIGHT - 1,
                backgroundColor
        );

        Component optionText = requiredLevel <= 0
                ? Component.translatable(
                        "container.enchantism.no_offer"
                )
                : Component.translatable(
                        "container.enchantism.option",
                        option + 1,
                        requiredLevel,
                        materialCost
                );

        int textColor = usable ? 0xFFFFFF : 0xB0B0B0;

        graphics.drawString(
                this.font,
                optionText,
                x + 5,
                y + 5,
                textColor,
                false
        );
    }

    private boolean canUseOption(int option)
    {
        Minecraft minecraft = this.minecraft;

        if (minecraft == null || minecraft.player == null)
        {
            return false;
        }

        int requiredLevel = this.menu.costs[option];
        int materialCost = option + 1;

        if (requiredLevel <= 0)
        {
            return false;
        }

        if (this.menu.getMaterialCount() < materialCost)
        {
            return false;
        }

        if (minecraft.player.getAbilities().instabuild)
        {
            return true;
        }

        return minecraft.player.experienceLevel >= materialCost
            && minecraft.player.experienceLevel >= requiredLevel;
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    )
    {
        for (int option = 0; option < 3; option++)
        {
            int x = this.leftPos + OPTION_X;
            int y = this.topPos + OPTION_Y + option * OPTION_HEIGHT;

            if (mouseX >= x
                    && mouseX < x + OPTION_WIDTH
                    && mouseY >= y
                    && mouseY < y + OPTION_HEIGHT
                    && canUseOption(option))
            {
                if (this.minecraft != null
                        && this.minecraft.gameMode != null
                        && this.minecraft.player != null)
                {
                    this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId,
                            option
                    );

                    this.menu.clickMenuButton(
                            this.minecraft.player,
                            option
                    );

                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
