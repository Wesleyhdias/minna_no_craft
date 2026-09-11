package com.wesleyhdias.minnanocraft.srs.models;

/**
 * Defines the types of player interaction events that grant exposure score
 * or apply penalties within the Spaced Repetition System (SRS).
 */
public enum ExpEvents {

    /** Continuous focus caused by hovering over an item tooltip in an inventory screen. */
    HOVER,

    /** Continuous focus caused by looking at an item in hand or on the HUD. */
    HUD_LOOK,

    /** General passive observation event when an item or word is displayed on screen. */
    SEEN,

    /** Active dictionary lookup performed while hovering over an item tooltip. */
    HOVER_LOOKUP,

    /** Direct dictionary lookup performed via keybind or interface search. */
    LOOKUP
}