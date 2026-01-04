package com.sparkle.note.utils

import com.sparkle.note.domain.model.Inspiration
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache for temporarily storing deleted items for undo functionality.
 * Items are automatically removed after a specified timeout (default: 5 minutes).
 */
class DeletionCache {
    private val cache = ConcurrentHashMap<Long, CacheEntry>()
    
    /**
     * Adds a deleted item to the cache.
     * @param id The ID of the deleted item
     * @param item The deleted item
     */
    fun put(id: Long, item: Inspiration) {
        cache[id] = CacheEntry(item, System.currentTimeMillis())
    }
    
    /**
     * Retrieves a deleted item from the cache.
     * @param id The ID of the deleted item
     * @return The deleted item, or null if not found or expired
     */
    fun get(id: Long): Inspiration? {
        val entry = cache[id] ?: return null
        
        // Check if entry has expired (5 minutes = 300,000ms)
        if (System.currentTimeMillis() - entry.timestamp > CACHE_TIMEOUT_MS) {
            cache.remove(id)
            return null
        }
        
        return entry.item
    }
    
    /**
     * Removes an item from the cache (e.g., after successful undo).
     * @param id The ID of the item to remove
     */
    fun remove(id: Long) {
        cache.remove(id)
    }
    
    /**
     * Clears all expired entries from the cache.
     */
    fun cleanupExpired() {
        val now = System.currentTimeMillis()
        cache.entries.removeIf { (now - it.value.timestamp) > CACHE_TIMEOUT_MS }
    }
    
    /**
     * Gets the number of items in the cache.
     */
    fun size(): Int = cache.size
    
    private data class CacheEntry(
        val item: Inspiration,
        val timestamp: Long
    )
    
    companion object {
        private const val CACHE_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
    }
}
