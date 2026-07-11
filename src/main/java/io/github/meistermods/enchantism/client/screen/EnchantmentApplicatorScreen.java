package io.github.meistermods.enchantism.client.screen;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.blockentity.EnchantmentApplicatorBlockEntity;
import io.github.meistermods.enchantism.menu.EnchantmentApplicatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@SuppressWarnings({"null"})
public final class EnchantmentApplicatorScreen
    extends AbstractContainerScreen<EnchantmentApplicatorMenu> {
  private static final ResourceLocation TEXTURE =
      ResourceLocation.tryParse(Enchantism.MOD_ID + ":textures/gui/enchantment_applicator.png");

  private static final int PROGRESS_WIDTH = 24;
  private static final int PROGRESS_HEIGHT = 17;

  public EnchantmentApplicatorScreen(
      EnchantmentApplicatorMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);

    this.imageWidth = 212;
    this.imageHeight = 166;

    this.inventoryLabelY = 72;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    this.renderBackground(graphics);

    super.render(graphics, mouseX, mouseY, partialTick);

    this.renderElementCostTooltips(graphics, mouseX, mouseY);

    this.renderTooltip(graphics, mouseX, mouseY);
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

    this.renderProgressArrow(graphics);
  }

  private void renderProgressArrow(GuiGraphics graphics) {
    int progressWidth = this.menu.getScaledProgress(PROGRESS_WIDTH);

    if (progressWidth <= 0) {
      return;
    }

    graphics.blit(
        TEXTURE, this.leftPos + 119, this.topPos + 34, 212, 0, progressWidth, PROGRESS_HEIGHT);
  }

  private void renderElementCostTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
    int elementStartX = 26;
    int elementStartY = 17;

    for (int row = 0; row < 3; row++) {
      for (int column = 0; column < 3; column++) {
        int slot = row * 3 + column;

        int slotX = this.leftPos + elementStartX + column * 18;

        int slotY = this.topPos + elementStartY + row * 18;

        if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
          int cost = EnchantmentApplicatorBlockEntity.getElementCost(slot);

          graphics.renderTooltip(
              this.font,
              Component.translatable("tooltip.enchantism.element_cost", cost),
              mouseX,
              mouseY);

          return;
        }
      }
    }
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);

    graphics.drawString(
        this.font,
        this.playerInventoryTitle,
        this.inventoryLabelX,
        this.inventoryLabelY,
        0x404040,
        false);

    int elementStartX = 26;
    int elementStartY = 17;

    for (int row = 0; row < 3; row++) {
      for (int column = 0; column < 3; column++) {
        int slot = row * 3 + column;

        int cost = EnchantmentApplicatorBlockEntity.getElementCost(slot);

        int x = elementStartX + column * 18;

        int y = elementStartY + row * 18;

        graphics.drawString(this.font, Integer.toString(cost), x, y, 0x606060, false);
      }
    }
  }
}
