package com.sparkle.note.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.input.KeyboardType
import com.sparkle.note.ui.screens.main.TimeFilter

/**
 * Advanced search screen with multiple search criteria.
 * Supports content search, theme filtering, and time range filtering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchScreen(
    onNavigateBack: () -> Unit,
    onInspirationClick: (Long) -> Unit,
    viewModel: AdvancedSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("高级搜索") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search input section
            SearchInputSection(
                query = searchQuery,
                onQueryChange = { 
                    searchQuery = it
                    showSuggestions = it.isNotBlank()
                    viewModel.updateSearchQuery(it)
                },
                onSearch = {
                    viewModel.performSearch()
                    showSuggestions = false
                },
                suggestions = if (showSuggestions) uiState.searchSuggestions else emptyList(),
                onSuggestionClick = { suggestion ->
                    searchQuery = suggestion
                    showSuggestions = false
                    viewModel.updateSearchQuery(suggestion)
                    viewModel.performSearch()
                },
                searchHistory = uiState.searchHistory,
                onHistoryItemClick = { historyItem ->
                    searchQuery = historyItem
                    viewModel.updateSearchQuery(historyItem)
                    viewModel.performSearch()
                },
                onClearHistory = viewModel::clearSearchHistory
            )
            
            // Filter chips
            FilterChipsSection(
                selectedTheme = uiState.selectedTheme,
                onThemeChange = viewModel::updateSelectedTheme,
                timeFilter = uiState.timeFilter,
                onTimeFilterChange = viewModel::updateTimeFilter
            )
            
            // Search results
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.searchResults.isEmpty() && searchQuery.isNotBlank() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "未找到相关灵感",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                uiState.searchResults.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.searchResults) { inspiration ->
                            InspirationSearchResultItem(
                                inspiration = inspiration,
                                onClick = { onInspirationClick(inspiration.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchInputSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    searchHistory: List<String>,
    onHistoryItemClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Search input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("搜索灵感内容、主题...") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FilledTonalButton(
                onClick = onSearch,
                modifier = Modifier.height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search suggestions
        if (suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "搜索建议",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    suggestions.forEach { suggestion ->
                        Text(
                            text = suggestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionClick(suggestion) }
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Search history
        if (query.isBlank() && searchHistory.isNotEmpty()) {
            SearchHistorySection(
                history = searchHistory,
                onHistoryItemClick = onHistoryItemClick,
                onClearHistory = onClearHistory
            )
        }
    }
}

@Composable
fun SearchHistorySection(
    history: List<String>,
    onHistoryItemClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "搜索历史",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onClearHistory) {
                Text("清除")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        history.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHistoryItemClick(item) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipsSection(
    selectedTheme: String?,
    onThemeChange: (String?) -> Unit,
    timeFilter: TimeFilter,
    onTimeFilterChange: (TimeFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "筛选条件",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Theme filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTheme == null,
                onClick = { onThemeChange(null) },
                label = { Text("全部主题") }
            )
            FilterChip(
                selected = selectedTheme == "工作",
                onClick = { onThemeChange("工作") },
                label = { Text("工作") }
            )
            FilterChip(
                selected = selectedTheme == "生活",
                onClick = { onThemeChange("生活") },
                label = { Text("生活") }
            )
            FilterChip(
                selected = selectedTheme == "学习",
                onClick = { onThemeChange("学习") },
                label = { Text("学习") }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Time filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = timeFilter == TimeFilter.ALL,
                onClick = { onTimeFilterChange(TimeFilter.ALL) },
                label = { Text("全部时间") }
            )
            FilterChip(
                selected = timeFilter == TimeFilter.TODAY,
                onClick = { onTimeFilterChange(TimeFilter.TODAY) },
                label = { Text("今天") }
            )
            FilterChip(
                selected = timeFilter == TimeFilter.THIS_WEEK,
                onClick = { onTimeFilterChange(TimeFilter.THIS_WEEK) },
                label = { Text("本周") }
            )
            FilterChip(
                selected = timeFilter == TimeFilter.THIS_MONTH,
                onClick = { onTimeFilterChange(TimeFilter.THIS_MONTH) },
                label = { Text("本月") }
            )
        }
    }
}

@Composable
fun InspirationSearchResultItem(
    inspiration: com.sparkle.note.domain.model.Inspiration,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = inspiration.themeName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = android.text.format.DateFormat.format("MM月dd日", inspiration.createdAt).toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = inspiration.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            // Tags removed - Inspiration model doesn't have tags field
        }
    }
}