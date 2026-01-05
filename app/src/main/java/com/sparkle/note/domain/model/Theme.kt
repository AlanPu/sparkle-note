package com.sparkle.note.domain.model

/**
 * Domain model representing a theme.
 * Themes are independent entities that can exist without inspirations.
 */
data class Theme(
    val name: String,
    val icon: String = "💡",
    val color: Long = 0xFF4A90E2, // Default blue color
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis(),
    val inspirationCount: Int = 0
) {
    /**
     * Validates the theme name according to business rules.
     * @return ValidationResult indicating if the name is valid
     */
    fun validateName(): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Empty
            name.length > MAX_NAME_LENGTH -> ValidationResult.TooLong
            name.contains("__THEME_MARKER__") -> ValidationResult.Invalid()
            else -> ValidationResult.Valid
        }
    }
    
    companion object {
        const val MAX_NAME_LENGTH = 50
    }
}