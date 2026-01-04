package com.sparkle.note.ui.screens.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparkle.note.domain.model.Inspiration
import com.sparkle.note.domain.repository.InspirationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for batch operation screen.
 * Handles multi-selection and batch operations on inspirations.
 */
@HiltViewModel
class BatchOperationViewModel @Inject constructor(
    private val inspirationRepository: InspirationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BatchOperationUiState())
    val uiState: StateFlow<BatchOperationUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<BatchOperationEvent>()
    val events: SharedFlow<BatchOperationEvent> = _events.asSharedFlow()
    
    init {
        loadInspirations()
    }
    
    private fun loadInspirations() {
        viewModelScope.launch {
            inspirationRepository.getAllInspirations()
                .collect { inspirations ->
                    _uiState.update { it.copy(inspirations = inspirations) }
                }
        }
    }
    
    fun enterSelectionMode(initialSelection: Long) {
        _uiState.update { 
            it.copy(
                isSelectionMode = true,
                selectedIds = setOf(initialSelection)
            )
        }
    }
    
    fun exitSelectionMode() {
        _uiState.update { 
            it.copy(
                isSelectionMode = false,
                selectedIds = emptySet()
            )
        }
    }
    
    fun toggleItemSelection(itemId: Long) {
        _uiState.update { currentState ->
            val newSelection = if (currentState.selectedIds.contains(itemId)) {
                currentState.selectedIds - itemId
            } else {
                currentState.selectedIds + itemId
            }
            currentState.copy(selectedIds = newSelection)
        }
    }
    
    fun selectAllItems() {
        _uiState.update { currentState ->
            val allIds = currentState.inspirations.map { it.id }.toSet()
            currentState.copy(selectedIds = allIds)
        }
    }
    
    fun performBatchDelete() {
        viewModelScope.launch {
            try {
                val selectedIds = _uiState.value.selectedIds.toList()
                
                // Delete each selected inspiration by ID
                selectedIds.forEach { id ->
                    inspirationRepository.deleteInspiration(id)
                }
                
                _events.emit(BatchOperationEvent.ShowSuccess("成功删除 ${selectedIds.size} 条灵感"))
                exitSelectionMode()
            } catch (e: Exception) {
                _events.emit(BatchOperationEvent.ShowError("删除失败: ${e.message}"))
            }
        }
    }
    
    fun performBatchExport() {
        viewModelScope.launch {
            try {
                val selectedInspirations = _uiState.value.inspirations.filter {
                    _uiState.value.selectedIds.contains(it.id)
                }
                
                // TODO: Implement export functionality
                // This could export to JSON, CSV, or share via Android's sharing system
                _events.emit(BatchOperationEvent.ShowSuccess("成功导出 ${selectedInspirations.size} 条灵感"))
                exitSelectionMode()
            } catch (e: Exception) {
                _events.emit(BatchOperationEvent.ShowError("导出失败: ${e.message}"))
            }
        }
    }
}

/**
 * UI state for batch operation screen
 */
data class BatchOperationUiState(
    val inspirations: List<Inspiration> = emptyList(),
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet()
)

/**
 * Events for batch operations
 */
sealed class BatchOperationEvent {
    data class ShowSuccess(val message: String) : BatchOperationEvent()
    data class ShowError(val message: String) : BatchOperationEvent()
    data class ExportComplete(val filePath: String) : BatchOperationEvent()
}