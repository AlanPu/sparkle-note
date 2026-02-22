package top.alanpu.sparklenote.app.utils

import top.alanpu.sparklenote.app.domain.model.Inspiration
import top.alanpu.sparklenote.app.domain.repository.InspirationRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages batch operations for inspirations.
 * Handles multi-selection, batch deletion, and batch export functionality.
 */
@Singleton
class BatchOperationManager @Inject constructor(
    private val repository: InspirationRepository
) {
    
    private val _selectedItems = MutableStateFlow<Set<Long>>(emptySet())
    val selectedItems: StateFlow<Set<Long>> = _selectedItems.asStateFlow()
    
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()
    
    /**
     * Enters or exits selection mode.
     */
    fun setSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) {
            clearSelection()
        }
    }
    
    /**
     * Toggles selection of an item.
     */
    fun toggleSelection(itemId: Long) {
        val currentSelection = _selectedItems.value.toMutableSet()
        if (currentSelection.contains(itemId)) {
            currentSelection.remove(itemId)
        } else {
            currentSelection.add(itemId)
        }
        _selectedItems.value = currentSelection
    }
    
    /**
     * Selects or deselects an item.
     */
    fun setItemSelected(itemId: Long, selected: Boolean) {
        val currentSelection = _selectedItems.value.toMutableSet()
        if (selected) {
            currentSelection.add(itemId)
        } else {
            currentSelection.remove(itemId)
        }
        _selectedItems.value = currentSelection
    }
    
    /**
     * Selects all items from the current filtered list.
     */
    suspend fun selectAll(currentItems: List<Inspiration>) {
        val allIds = currentItems.map { it.id }.toSet()
        _selectedItems.value = allIds
    }
    
    /**
     * Clears all selections.
     */
    fun clearSelection() {
        _selectedItems.value = emptySet()
    }
    
    /**
     * Checks if an item is selected.
     */
    fun isItemSelected(itemId: Long): Boolean {
        return _selectedItems.value.contains(itemId)
    }
    
    /**
     * Gets the count of selected items.
     */
    fun getSelectedCount(): Int {
        return _selectedItems.value.size
    }
    
    /**
     * Performs batch deletion of selected items.
     * @return Result indicating success or failure
     */
    suspend fun performBatchDelete(): Result<Int> {
        return try {
            val selectedIds = _selectedItems.value.toList()
            var deletedCount = 0
            
            selectedIds.forEach { itemId ->
                repository.deleteInspiration(itemId)
                deletedCount++
            }
            
            // Clear selection after successful deletion
            clearSelection()
            
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Performs batch export of selected items.
     * @return Result containing the exported markdown content
     */
    suspend fun performBatchExport(): Result<String> {
        return try {
            val selectedIds = _selectedItems.value.toList()
            val inspirations = mutableListOf<Inspiration>()
            
            // Get all selected inspirations
            repository.getAllInspirations().first().forEach { inspiration ->
                if (selectedIds.contains(inspiration.id)) {
                    inspirations.add(inspiration)
                }
            }
            
            if (inspirations.isEmpty()) {
                return Result.failure(Exception("No items selected for export"))
            }
            
            // Export to markdown
            val markdown = repository.exportToMarkdown(inspirations)
            
            Result.success(markdown)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets the selected inspirations.
     * @return Flow of list of selected inspirations
     */
    fun getSelectedInspirations(): Flow<List<Inspiration>> {
        return repository.getAllInspirations().map { allInspirations ->
            allInspirations.filter { inspiration ->
                _selectedItems.value.contains(inspiration.id)
            }
        }
    }
    
    /**
     * Checks if selection mode is active and there are selected items.
     */
    fun hasActiveSelection(): Boolean {
        return _isSelectionMode.value && _selectedItems.value.isNotEmpty()
    }
    
    /**
     * Exits selection mode and clears selection.
     */
    fun exitSelectionMode() {
        setSelectionMode(false)
    }
}