package com.sparkle.note.utils

/**
 * Theme name matcher for handling theme name variations and translations.
 * Provides intelligent matching between backup theme names and existing themes.
 */
object ThemeNameMatcher {
    
    /**
     * Finds the best matching theme name from existing themes.
     * 
     * @param targetThemeName The theme name from backup
     * @param existingThemeNames List of existing theme names
     * @return The best matching existing theme name, or null if no good match
     */
    fun findBestMatch(targetThemeName: String, existingThemeNames: List<String>): String? {
        val normalizedTarget = normalizeThemeName(targetThemeName)
        val normalizedExisting = existingThemeNames.map { normalizeThemeName(it) }
        
        // 1. 精确匹配（标准化后）
        val exactMatchIndex = normalizedExisting.indexOf(normalizedTarget)
        if (exactMatchIndex != -1) {
            return existingThemeNames[exactMatchIndex]
        }
        
        // 2. 包含匹配
        val containsMatch = existingThemeNames.find { existing ->
            existing.contains(targetThemeName, ignoreCase = true) ||
            targetThemeName.contains(existing, ignoreCase = true)
        }
        if (containsMatch != null) {
            return containsMatch
        }
        
        // 3. 语义相似匹配
        val semanticMatch = findSemanticMatch(targetThemeName, existingThemeNames)
        if (semanticMatch != null) {
            return semanticMatch
        }
        
        // 4. 英文/中文对照匹配
        val translationMatch = findTranslationMatch(targetThemeName, existingThemeNames)
        if (translationMatch != null) {
            return translationMatch
        }
        
        return null
    }
    
    /**
     * Normalizes theme name for comparison.
     */
    private fun normalizeThemeName(themeName: String): String {
        return themeName.trim().lowercase()
            .replace(" ", "")
            .replace("_", "")
            .replace("-", "")
    }
    
    /**
     * Finds semantic matches based on theme categories.
     */
    private fun findSemanticMatch(targetTheme: String, existingThemes: List<String>): String? {
        val semanticGroups = mapOf(
            "工作" to listOf("work", "job", "career", "business", "office", "task"),
            "学习" to listOf("study", "education", "knowledge", "school", "university", "research"),
            "生活" to listOf("life", "daily", "living", "personal", "family", "home"),
            "创意" to listOf("creative", "idea", "innovation", "design", "art", "creation"),
            "技术" to listOf("tech", "technology", "programming", "coding", "development"),
            "健康" to listOf("health", "fitness", "wellness", "exercise", "medical"),
            "旅行" to listOf("travel", "trip", "journey", "tour", "vacation", "adventure"),
            "美食" to listOf("food", "cooking", "cuisine", "recipe", "restaurant", "dining"),
            "运动" to listOf("sport", "exercise", "workout", "fitness", "training"),
            "音乐" to listOf("music", "song", "melody", "concert", "musical"),
            "电影" to listOf("movie", "film", "cinema", "video", "watching"),
            "读书" to listOf("book", "reading", "literature", "novel", "study"),
            "游戏" to listOf("game", "gaming", "play", "entertainment", "fun")
        )
        
        val targetGroup = semanticGroups.entries.find { entry ->
            entry.key.equals(targetTheme, ignoreCase = true) ||
            entry.value.any { it.equals(targetTheme, ignoreCase = true) }
        }
        
        if (targetGroup != null) {
            existingThemes.forEach { existing ->
                if (targetGroup.key.equals(existing, ignoreCase = true) ||
                    targetGroup.value.any { it.equals(existing, ignoreCase = true) }) {
                    return existing
                }
            }
        }
        
        return null
    }
    
    /**
     * Finds translation matches between Chinese and English.
     */
    private fun findTranslationMatch(targetTheme: String, existingThemes: List<String>): String? {
        val translations = mapOf(
            "工作" to "work",
            "学习" to "study",
            "生活" to "life",
            "创意" to "creative",
            "技术" to "tech",
            "健康" to "health",
            "旅行" to "travel",
            "美食" to "food",
            "运动" to "sport",
            "音乐" to "music",
            "电影" to "movie",
            "读书" to "book",
            "游戏" to "game"
        )
        
        // 中文到英文
        if (translations.containsKey(targetTheme)) {
            val englishEquivalent = translations[targetTheme]!!
            existingThemes.find { it.equals(englishEquivalent, ignoreCase = true) }?.let {
                return it
            }
        }
        
        // 英文到中文
        val reverseTranslations = translations.entries.associate { (k, v) -> v to k }
        if (reverseTranslations.containsKey(targetTheme)) {
            val chineseEquivalent = reverseTranslations[targetTheme]!!
            existingThemes.find { it.equals(chineseEquivalent, ignoreCase = true) }?.let {
                return it
            }
        }
        
        return null
    }
    
    /**
     * Suggests theme name alternatives for missing themes.
     */
    fun suggestAlternatives(missingTheme: String, existingThemes: List<String>): List<String> {
        val suggestions = mutableListOf<String>()
        
        // 添加语义相关的建议
        val semanticMatch = findSemanticMatch(missingTheme, existingThemes)
        if (semanticMatch != null) {
            suggestions.add(semanticMatch)
        }
        
        // 添加翻译相关的建议
        val translationMatch = findTranslationMatch(missingTheme, existingThemes)
        if (translationMatch != null) {
            suggestions.add(translationMatch)
        }
        
        // 添加包含关系的建议
        existingThemes.forEach { existing ->
            if (existing.contains(missingTheme, ignoreCase = true) ||
                missingTheme.contains(existing, ignoreCase = true)) {
                if (!suggestions.contains(existing)) {
                    suggestions.add(existing)
                }
            }
        }
        
        return suggestions.take(3) // 最多返回3个建议
    }
}