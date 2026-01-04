package com.sparkle.note.utils

import com.sparkle.note.domain.repository.InspirationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages search suggestions based on existing content.
 * Analyzes inspiration content and themes to provide relevant suggestions.
 */
@Singleton
class SearchSuggestionManager @Inject constructor(
    private val repository: InspirationRepository
) {
    
    /**
     * Gets search suggestions based on the current query.
     * @param query The current search query
     * @return Flow of list of suggestions
     */
    fun getSearchSuggestions(query: String): Flow<List<String>> {
        if (query.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        
        return repository.getAllInspirations().map { inspirations ->
            val suggestions = mutableSetOf<String>()
            val lowerQuery = query.lowercase()
            
            // Extract words from content that match the query
            inspirations.forEach { inspiration ->
                val words = extractWords(inspiration.content)
                words.forEach { word ->
                    if (word.lowercase().contains(lowerQuery) && word.length > 1) {
                        suggestions.add(word)
                    }
                }
                
                // Add theme suggestions
                if (inspiration.themeName.lowercase().contains(lowerQuery)) {
                    suggestions.add(inspiration.themeName)
                }
            }
            
            // Sort by relevance (length and match position)
            suggestions.sortedWith(compareBy({ it.length }, { it.lowercase().indexOf(lowerQuery) }))
                .take(5) // Limit to 5 suggestions
        }
    }
    
    /**
     * Gets popular search terms based on frequency.
     * @return Flow of list of popular terms
     */
    fun getPopularSearchTerms(): Flow<List<String>> {
        return repository.getAllInspirations().map { inspirations ->
            val termFrequency = mutableMapOf<String, Int>()
            
            // Analyze all content and count word frequencies
            inspirations.forEach { inspiration ->
                val words = extractWords(inspiration.content)
                words.forEach { word ->
                    if (word.length > 2) { // Only count meaningful words
                        termFrequency[word] = termFrequency.getOrDefault(word, 0) + 1
                    }
                }
                
                // Count theme frequencies
                termFrequency[inspiration.themeName] = termFrequency.getOrDefault(inspiration.themeName, 0) + 1
            }
            
            // Sort by frequency and return top terms
            termFrequency.entries
                .sortedByDescending { it.value }
                .take(8)
                .map { it.key }
        }
    }
    
    /**
     * Extracts meaningful words from text content.
     * @param text The text to analyze
     * @return List of extracted words
     */
    private fun extractWords(text: String): List<String> {
        // Remove punctuation and split by whitespace
        return text.replace(Regex("[^\\w\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && it.length > 1 }
            .distinct()
    }
    
    /**
     * Gets recent themes for theme-based suggestions.
     * @return Flow of list of recent themes
     */
    fun getRecentThemes(): Flow<List<String>> {
        return repository.getDistinctThemes().map { themes ->
            themes.sorted().take(10)
        }
    }
}