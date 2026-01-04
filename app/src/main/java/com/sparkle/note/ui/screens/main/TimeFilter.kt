package com.sparkle.note.ui.screens.main

/**
 * Represents different time filter options for inspirations.
 */
enum class TimeFilter {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH;
    
    companion object {
        fun getDisplayName(filter: TimeFilter): String {
            return when (filter) {
                ALL -> "全部"
                TODAY -> "今天"
                THIS_WEEK -> "本周"
                THIS_MONTH -> "本月"
            }
        }
    }
}
