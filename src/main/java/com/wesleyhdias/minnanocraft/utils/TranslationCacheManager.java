package com.wesleyhdias.minnanocraft.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages the caching system for item translations to optimize rendering performance.
 * <p>
 * It holds the raw string cache for the Item Builders and acts as a central coordinator
 * for cache invalidation. It uses a deferred clearance mechanism ({@code pendingClear})
 * to ensure that text changes (due to SRS level ups/downs) only occur when the player
 * looks away from the item, preventing jarring visual updates.
 */
public class TranslationCacheManager {

    /**
     * The maximum number of entries allowed in the builder cache to prevent memory leaks.
     */
    private static final int MAX_SIZE = 50;

    /**
     * A deferred flag indicating that a vocabulary level has changed (progressed or regressed).
     * If {@code true}, all caches should be cleared at the next safe interaction gap
     * (e.g., when the mouse leaves the tooltip) to apply the new text formats.
     */
    public static boolean pendingClear = false;

    /**
     * An LRU (Least Recently Used) cache for storing the final built strings of item names.
     * This prevents the builders from re-processing the same structures on every render frame.
     */
    public static final Map<String, String> BUILDER_CACHE = new LinkedHashMap<>(MAX_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > MAX_SIZE;
        }
    };

    /**
     * Instantly clears all translation caches (both pure data and visual components)
     * and lowers the pending clear flag.
     * <p>
     * This is typically triggered by exposure trackers when the player stops hovering
     * over an item or switches items.
     */
    public static void clearAll() {
        // Clears the pure text cache from the builders
        BUILDER_CACHE.clear();

        // Triggers the visual layer (colors/underlines) to clear its own cache
        TooltipFormatter.clearCache();

        // Resets the flag since all text is now ready to be freshly generated
        pendingClear = false;
    }
}