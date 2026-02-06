package com.petcompass;

/**
 * Central constants for Pet Compass mod.
 * Extracted from hardcoded values throughout the codebase for easier maintenance.
 */
public final class PetCompassConstants {

    private PetCompassConstants() {
        // Utility class - prevent instantiation
    }

    // ========== Search & Distance Constants ==========

    /**
     * Default search radius for finding tamed pets (in blocks).
     * Used when requesting the initial list of pets from the server.
     */
    public static final int DEFAULT_SEARCH_RADIUS = 5000;

    /**
     * Search radius for finding nearby pets that are loaded (in blocks).
     * Used when selecting a pet to get its live position.
     */
    public static final int NEARBY_SEARCH_RADIUS = 200;

    /**
     * Search radius for finding pets when checking achievements (in blocks).
     */
    public static final int ACHIEVEMENT_SEARCH_RADIUS = 50;

    /**
     * Minimum initial distance for the "Find Lost Pet" achievement (in blocks).
     * Pets closer than this won't count for the achievement.
     */
    public static final int MINIMUM_DISTANCE_THRESHOLD = 100;

    /**
     * Proximity threshold for considering a pet "found" (in blocks).
     * Used for achievement tracking.
     */
    public static final int PROXIMITY_THRESHOLD = 10;

    /**
     * Placeholder distance shown for cross-dimension pets.
     */
    public static final int CROSS_DIMENSION_DISTANCE = 99999;

    // ========== Timing Constants ==========

    /**
     * How often to check for achievement progress (in ticks).
     * 20 ticks = 1 second.
     */
    public static final int ACHIEVEMENT_CHECK_INTERVAL = 20;

    // ========== UI Layout Constants ==========

    /** Width of the search box in the GUI */
    public static final int SEARCH_BOX_WIDTH = 200;

    /** Height of the search box in the GUI */
    public static final int SEARCH_BOX_HEIGHT = 20;

    /** Y position of the search box from the top */
    public static final int SEARCH_BOX_Y = 22;

    /** Top offset for the pet list */
    public static final int LIST_TOP_OFFSET = 50;

    /** Bottom margin for the pet list */
    public static final int LIST_BOTTOM_MARGIN = 64;

    /** Width of buttons (Track/Cancel) */
    public static final int BUTTON_WIDTH = 150;

    /** Height of buttons */
    public static final int BUTTON_HEIGHT = 20;

    /** Y offset from bottom for buttons */
    public static final int BUTTON_Y_OFFSET = 52;

    /** Height of each pet entry in the list */
    public static final int ENTRY_HEIGHT = 36;

    /** Width of the list row */
    public static final int LIST_ROW_WIDTH = 260;

    /** Horizontal spacing for list items */
    public static final int LIST_ITEM_PADDING = 5;

    /** Vertical spacing between lines in pet entries */
    public static final int ENTRY_LINE_SPACING = 10;

    /** First line vertical offset in entry */
    public static final int ENTRY_FIRST_LINE_Y = 2;

    /** Second line vertical offset in entry */
    public static final int ENTRY_SECOND_LINE_Y = 14;

    /** Y offset for pet count text from bottom */
    public static final int PET_COUNT_Y_OFFSET = 28;

    /** Y offset for title from top */
    public static final int TITLE_Y = 8;

    /** Scrollbar position offset */
    public static final int SCROLLBAR_OFFSET = 144;

    /** Button spacing from center */
    public static final int BUTTON_SPACING = 5;

    /** Center offset for first button */
    public static final int BUTTON_LEFT_OFFSET = 155;

    // ========== Color Palette ==========

    // Base Colors
    /** Pure white for titles and primary text */
    public static final int COLOR_WHITE = 0xFFFFFF;

    /** Pure white with full alpha for rendering */
    public static final int COLOR_WHITE_ALPHA = 0xFFFFFFFF;

    /** Medium gray for secondary text */
    public static final int COLOR_GRAY = 0xAAAAAA;

    /** Dark gray for tertiary text and coordinates */
    public static final int COLOR_DARK_GRAY = 0x888888;

    // Accent Colors
    /** Green accent color for positive indicators */
    public static final int COLOR_GREEN = 0xFF55FF55;

    /** Yellow color for nearby indicators */
    public static final int COLOR_YELLOW = 0xFFFFFF55;

    /** Cyan color for very close indicators */
    public static final int COLOR_CYAN = 0xFF55FFFF;

    /** Dark cyan for blinking alternate state */
    public static final int COLOR_DARK_CYAN = 0xFF00AAAA;

    /** Light blue/purple for dimension indicators */
    public static final int COLOR_LIGHT_PURPLE = 0xAAAAFF;

    // Background Colors
    /** Semi-transparent black for HUD backgrounds */
    public static final int COLOR_BG_BLACK = 0x80000000;

    /** Semi-transparent green for very close background */
    public static final int COLOR_BG_GREEN = 0x8000AA00;

    /** Semi-transparent dark yellow for nearby background */
    public static final int COLOR_BG_YELLOW = 0x80333300;

    /** Semi-transparent white for selection highlight */
    public static final int COLOR_BG_HIGHLIGHT = 0x33FFFFFF;

    // Dimension-Specific Colors
    /** Green for Overworld dimension */
    public static final int COLOR_DIM_OVERWORLD = 0x55FF55;

    /** Red for Nether dimension */
    public static final int COLOR_DIM_NETHER = 0xFF5555;

    /** Purple for The End dimension */
    public static final int COLOR_DIM_END = 0xAA55AA;

    /** Cyan for modded/unknown dimensions */
    public static final int COLOR_DIM_MODDED = 0x55FFFF;

    /** Gray for unknown/null dimensions */
    public static final int COLOR_DIM_UNKNOWN = 0x888888;

    // ========== Animation Constants ==========

    /** Blink animation interval in milliseconds */
    public static final int BLINK_INTERVAL_MS = 500;

    // ========== HUD Layout Constants ==========

    /** HUD panel X position */
    public static final int HUD_X = 10;

    /** HUD panel Y position */
    public static final int HUD_Y = 10;

    /** HUD panel width */
    public static final int HUD_PANEL_WIDTH = 150;

    /** HUD panel default height */
    public static final int HUD_PANEL_HEIGHT = 72;

    /** HUD panel height when nearby */
    public static final int HUD_PANEL_HEIGHT_NEARBY = 75;

    /** HUD padding/margin */
    public static final int HUD_PADDING = 5;

    /** HUD line spacing */
    public static final int HUD_LINE_HEIGHT = 12;

    /** Paw icon offset from right edge */
    public static final int HUD_PAW_OFFSET = 20;
}
