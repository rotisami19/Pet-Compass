package com.petcompass.gui;

import com.petcompass.PetCompassConstants;
import com.petcompass.network.PetCompassNetworking;
import com.petcompass.network.SelectPetPacket;
import com.petcompass.util.PetUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Main GUI screen for the Pet Compass.
 * Shows a list of tamed pets that the player can select to track.
 */
public class PetCompassScreen extends Screen {

    private final List<PetUtils.TamedPetInfo> allPets;
    private List<PetUtils.TamedPetInfo> filteredPets;
    
    private PetSelectionList petList;
    private EditBox searchBox;
    private Button trackButton;
    private Button cancelButton;
    
    private PetUtils.TamedPetInfo selectedPet;

    public PetCompassScreen(List<PetUtils.TamedPetInfo> pets) {
        super(Component.translatable("gui.petcompass.title"));
        this.allPets = pets;
        this.filteredPets = new ArrayList<>(pets);
    }

    @Override
    protected void init() {
        super.init();

        // Search box at the top
        this.searchBox = new EditBox(this.font, this.width / 2 - PetCompassConstants.SEARCH_BOX_WIDTH / 2,
            PetCompassConstants.SEARCH_BOX_Y, PetCompassConstants.SEARCH_BOX_WIDTH,
            PetCompassConstants.SEARCH_BOX_HEIGHT, Component.translatable("gui.petcompass.search"));
        this.searchBox.setResponder(this::onSearchChanged);
        this.addWidget(this.searchBox);

        // Pet list in the middle
        int listTop = PetCompassConstants.LIST_TOP_OFFSET;
        int listBottom = this.height - PetCompassConstants.LIST_BOTTOM_MARGIN;
        int listHeight = listBottom - listTop;
        this.petList = new PetSelectionList(this.minecraft, this.width, listHeight, listTop, PetCompassConstants.ENTRY_HEIGHT);
        this.addWidget(this.petList);
        refreshList();
        
        // Track button
        this.trackButton = Button.builder(Component.translatable("gui.petcompass.track"), button -> {
            if (selectedPet != null) {
                // Send all pet data so we can track even unloaded pets
                PetCompassNetworking.CHANNEL.send(PacketDistributor.SERVER.noArg(), new SelectPetPacket(
                    selectedPet.uuid(),
                    selectedPet.name(),
                    selectedPet.x(),
                    selectedPet.y(),
                    selectedPet.z(),
                    selectedPet.dimension()
                ));
                this.onClose();
            }
        }).bounds(this.width / 2 - PetCompassConstants.BUTTON_LEFT_OFFSET,
            this.height - PetCompassConstants.BUTTON_Y_OFFSET,
            PetCompassConstants.BUTTON_WIDTH, PetCompassConstants.BUTTON_HEIGHT).build();
        this.trackButton.active = false;
        this.addRenderableWidget(this.trackButton);

        // Cancel button
        this.cancelButton = Button.builder(Component.translatable("gui.cancel"), button -> {
            this.onClose();
        }).bounds(this.width / 2 + PetCompassConstants.BUTTON_SPACING,
            this.height - PetCompassConstants.BUTTON_Y_OFFSET,
            PetCompassConstants.BUTTON_WIDTH, PetCompassConstants.BUTTON_HEIGHT).build();
        this.addRenderableWidget(this.cancelButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, PetCompassConstants.TITLE_Y,
            PetCompassConstants.COLOR_WHITE);

        // Pet count
        String countText = filteredPets.size() + " pet(s) found";
        guiGraphics.drawCenteredString(this.font, countText, this.width / 2,
            this.height - PetCompassConstants.PET_COUNT_Y_OFFSET, PetCompassConstants.COLOR_GRAY);
        
        // Render pet list
        this.petList.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Render search box
        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox.isFocused()) {
            return this.searchBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.searchBox.isFocused()) {
            return this.searchBox.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void onSearchChanged(String text) {
        filteredPets.clear();
        String search = text.toLowerCase();
        for (PetUtils.TamedPetInfo pet : allPets) {
            if (pet.name().toLowerCase().contains(search)) {
                filteredPets.add(pet);
            }
        }
        refreshList();
    }

    private void refreshList() {
        this.petList.children().clear();
        for (PetUtils.TamedPetInfo pet : filteredPets) {
            this.petList.children().add(new PetEntry(pet));
        }
    }

    public void selectPet(PetUtils.TamedPetInfo pet) {
        this.selectedPet = pet;
        this.trackButton.active = pet != null;
    }

    // ========== Inner Classes ==========

    class PetSelectionList extends ObjectSelectionList<PetEntry> {
        public PetSelectionList(Minecraft minecraft, int width, int height, int top, int itemHeight) {
            // 1.20.1 signature: Minecraft, width, height, top, bottom, itemHeight
            super(minecraft, width, height, top, top + height, itemHeight);
        }

        @Override
        public int getRowWidth() {
            return PetCompassConstants.LIST_ROW_WIDTH;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + PetCompassConstants.SCROLLBAR_OFFSET;
        }
    }

    class PetEntry extends ObjectSelectionList.Entry<PetEntry> {
        private final PetUtils.TamedPetInfo pet;

        public PetEntry(PetUtils.TamedPetInfo pet) {
            this.pet = pet;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            // Pet name
            guiGraphics.drawString(font, pet.name(), left + PetCompassConstants.LIST_ITEM_PADDING,
                top + PetCompassConstants.ENTRY_FIRST_LINE_Y, PetCompassConstants.COLOR_WHITE);

            // Pet type (only show if different from name to avoid "Wolf / Wolf")
            String typeKey = pet.entityType();
            Component typeName = Component.translatable(typeKey);
            String typeString = typeName.getString();
            int yOffset = PetCompassConstants.ENTRY_SECOND_LINE_Y;
            if (!typeString.equals(pet.name())) {
                guiGraphics.drawString(font, typeName, left + PetCompassConstants.LIST_ITEM_PADDING,
                    top + yOffset, PetCompassConstants.COLOR_GRAY);
                yOffset += PetCompassConstants.ENTRY_LINE_SPACING;
            }

            // Dimension indicator with color coding
            String dimDisplay = formatDimension(pet.dimension());
            int dimColor = getDimensionColor(pet.dimension());
            guiGraphics.drawString(font, dimDisplay, left + PetCompassConstants.LIST_ITEM_PADDING,
                top + yOffset, dimColor);

            // Distance/coords on the right side
            String coords = String.format("X: %d, Z: %d", pet.x(), pet.z());
            int coordsWidth = font.width(coords);
            guiGraphics.drawString(font, coords, left + width - coordsWidth - PetCompassConstants.LIST_ITEM_PADDING,
                top + PetCompassConstants.ENTRY_FIRST_LINE_Y, PetCompassConstants.COLOR_DARK_GRAY);

            // Highlight if selected
            if (selectedPet != null && selectedPet.uuid().equals(pet.uuid())) {
                guiGraphics.fill(left, top, left + width, top + height, PetCompassConstants.COLOR_BG_HIGHLIGHT);
            }
        }
        
        private String formatDimension(String dim) {
            if (dim == null) return "?";
            if (dim.contains("overworld")) return "Overworld";
            if (dim.contains("nether")) return "Nether";
            if (dim.contains("end")) return "The End";
            // For modded dimensions, extract the name
            if (dim.contains(":")) {
                String name = dim.substring(dim.indexOf(':') + 1);
                return name.substring(0, 1).toUpperCase() + name.substring(1).replace('_', ' ');
            }
            return dim;
        }
        
        private int getDimensionColor(String dim) {
            if (dim == null) return PetCompassConstants.COLOR_DIM_UNKNOWN;
            if (dim.contains("overworld")) return PetCompassConstants.COLOR_DIM_OVERWORLD;
            if (dim.contains("nether")) return PetCompassConstants.COLOR_DIM_NETHER;
            if (dim.contains("end")) return PetCompassConstants.COLOR_DIM_END;
            return PetCompassConstants.COLOR_DIM_MODDED;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                selectPet(pet);
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal(pet.name());
        }
    }
}
