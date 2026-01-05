package com.sparkle.note.domain.model

/**
 * Domain model representing an inspiration note.
 * This is the core business entity used throughout the application.
 */
data class Inspiration(
    val id: Long = 0,
    val content: String,
    val themeName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val wordCount: Int
) {
    /**
     * Validates the inspiration content according to business rules.
     * @return ValidationResult indicating if the content is valid
     */
    fun validateContent(): ValidationResult {
        return when {
            content.isBlank() -> ValidationResult.Empty
            content.length > MAX_CONTENT_LENGTH -> ValidationResult.TooLong
            else -> ValidationResult.Valid
        }
    }
    
    companion object {
        const val MAX_CONTENT_LENGTH = 500
    }
}

/**
 * Represents the validation result for inspiration content.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    object Empty : ValidationResult()
    object TooLong : ValidationResult()
    data class Invalid(val reason: String = "Invalid") : ValidationResult()
}