package com.petcompass.gui;

import com.petcompass.network.SelectPetPacket;
import com.petcompass.util.PetUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

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
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, Component.translatable("gui.petcompass.search"));
        this.searchBox.setResponder(this::onSearchChanged);
        this.addWidget(this.searchBox);
        
        // Pet list in the middle
        int listTop = 50;
        int listBottom = this.height - 64;
        this.petList = new PetSelectionList(this.minecraft, this.width, listBottom - listTop, listTop, 36);
        this.addWidget(this.petList);
        refreshList();
        
        // Track button
        this.trackButton = Button.builder(Component.translatable("gui.petcompass.track"), button -> {
            if (selectedPet != null) {
                PacketDistributor.sendToServer(new SelectPetPacket(selectedPet.uuid()));
                this.onClose();
            }
        }).bounds(this.width / 2 - 155, this.height - 52, 150, 20).build();
        this.trackButton.active = false;
        this.addRenderableWidget(this.trackButton);
        
        // Cancel button
        this.cancelButton = Button.builder(Component.translatable("gui.cancel"), button -> {
            this.onClose();
        }).bounds(this.width / 2 + 5, this.height - 52, 150, 20).build();
        this.addRenderableWidget(this.cancelButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        
        // Pet count
        String countText = filteredPets.size() + " pet(s) found";
        guiGraphics.drawCenteredString(this.font, countText, this.width / 2, this.height - 28, 0xAAAAAA);
        
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
            super(minecraft, width, height, top, itemHeight);
        }

        @Override
        public int getRowWidth() {
            return 260;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 144;
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
            guiGraphics.drawString(font, pet.name(), left + 5, top + 2, 0xFFFFFF);
            
            // Pet type (only show if different from name to avoid "Wolf / Wolf")
            String typeKey = pet.entityType();
            Component typeName = Component.translatable(typeKey);
            String typeString = typeName.getString();
            if (!typeString.equals(pet.name())) {
                guiGraphics.drawString(font, typeName, left + 5, top + 14, 0xAAAAAA);
            }
            
            // Distance/coords
            String coords = String.format("X: %d, Y: %d, Z: %d", pet.x(), pet.y(), pet.z());
            guiGraphics.drawString(font, coords, left + 5, top + 14 + (typeString.equals(pet.name()) ? 0 : 12), 0x888888);
            
            // Highlight if selected
            if (selectedPet != null && selectedPet.uuid().equals(pet.uuid())) {
                guiGraphics.fill(left, top, left + width, top + height, 0x33FFFFFF);
            }
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
