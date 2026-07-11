package io.github.meistermods.enchantism.client.screen;

import io.github.meistermods.enchantism.Enchantism;
import io.github.meistermods.enchantism.menu.ElementInfuserMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@SuppressWarnings({"null"})
public final class ElementInfuserScreen extends AbstractContainerScreen<ElementInfuserMenu> {
  private static final ResourceLocation TEXTURE =
      ResourceLocation.tryParse(Enchantism.MOD_ID + ":textures/gui/element_infuser.png");

  private static final int PROGRESS_WIDTH = 24;
  private static final int PROGRESS_HEIGHT = 17;

  public ElementInfuserScreen(ElementInfuserMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);

    this.imageWidth = 176;
    this.imageHeight = 166;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    this.renderBackground(graphics);

    super.render(graphics, mouseX, mouseY, partialTick);

    this.renderTooltip(graphics, mouseX, mouseY);
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

    int progressWidth = this.menu.getScaledProgress(PROGRESS_WIDTH);

    if (progressWidth > 0) {
      graphics.blit(
          TEXTURE, this.leftPos + 79, this.topPos + 34, 176, 0, progressWidth, PROGRESS_HEIGHT);
    }
  }
}
