package com.sparkle.note.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages search history for the application.
 * Stores the last 10 search queries using DataStore.
 */
@Singleton
class SearchHistoryManager @Inject constructor(
    private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(name = "search_history")
    
    companion object {
        private val SEARCH_HISTORY_KEY = stringSetPreferencesKey("search_queries")
        private const val MAX_HISTORY_SIZE = 10
    }
    
    /**
     * Gets the search history as a Flow.
     * @return Flow of list of search queries, newest first
     */
    val searchHistory: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            preferences[SEARCH_HISTORY_KEY]?.toList() ?: emptyList()
        }
    
    /**
     * Adds a search query to the history.
     * Maintains only the last 10 queries and removes duplicates.
     * @param query The search query to add
     */
    suspend fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[SEARCH_HISTORY_KEY]?.toMutableList() ?: mutableListOf()
            
            // Remove the query if it already exists
            currentHistory.remove(query)
            
            // Add the new query at the beginning
            currentHistory.add(0, query)
            
            // Keep only the last 10 queries
            if (currentHistory.size > MAX_HISTORY_SIZE) {
                currentHistory.removeAt(currentHistory.size - 1)
            }
            
            preferences[SEARCH_HISTORY_KEY] = currentHistory.toSet()
        }
    }
    
    /**
     * Clears all search history.
     */
    suspend fun clearSearchHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(SEARCH_HISTORY_KEY)
        }
    }
    
    /**
     * Removes a specific search query from history.
     * @param query The query to remove
     */
    suspend fun removeSearchQuery(query: String) {
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[SEARCH_HISTORY_KEY]?.toMutableList() ?: mutableListOf()
            currentHistory.remove(query)
            preferences[SEARCH_HISTORY_KEY] = currentHistory.toSet()
        }
    }
}