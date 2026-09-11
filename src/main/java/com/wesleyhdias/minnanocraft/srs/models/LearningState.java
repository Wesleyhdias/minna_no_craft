package com.wesleyhdias.minnanocraft.srs.models;

/**
 * Represents the Spaced Repetition System (SRS) lifecycle state of a tracked word or token.
 */
public enum LearningState {

    /** The word is queued in the dictionary pool and awaiting an available active learning slot. */
    WAITING,

    /** The word is actively being learned, displayed in target scripts, and monitored for inactivity decay. */
    ACTIVE,

    /** The word has reached the mastery exposure threshold and is considered fully acquired. */
    MASTERED
}
