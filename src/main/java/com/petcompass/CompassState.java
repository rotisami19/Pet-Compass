package com.petcompass;

/**
 * Represents the state of the Pet Compass.
 */
public enum CompassState {
    INACTIVE(0),
    SEARCHING(1),
    FOUND(2),
    NOT_FOUND(3);

    private final int id;

    CompassState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CompassState fromId(int id) {
        for (CompassState state : values()) {
            if (state.id == id) {
                return state;
            }
        }
        return INACTIVE;
    }
}
