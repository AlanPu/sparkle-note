package top.alanpu.sparklenote.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import top.alanpu.sparklenote.app.R
import top.alanpu.sparklenote.app.ui.theme.ThemeManager
import top.alanpu.sparklenote.app.ui.theme.ThemeStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for theme settings screen.
 * Manages theme preferences and settings.
 */
@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val themeManager: ThemeManager
) : ViewModel() {
    
    val currentThemeStyle: StateFlow<ThemeStyle> = themeManager.themeStyle
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeStyle.NORDIC
        )
    
    val isDarkMode: StateFlow<Boolean> = themeManager.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    fun setThemeStyle(themeStyle: ThemeStyle) {
        viewModelScope.launch {
            themeManager.setThemeStyle(themeStyle)
        }
    }
    
    fun setDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkMode(isDarkMode)
        }
    }
}

/**
 * Theme settings screen for customizing app appearance.
 * Allows users to select different color schemes and dark/light mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    navController: NavController,
    viewModel: ThemeSettingsViewModel = hiltViewModel()
) {
    val currentThemeStyle by viewModel.currentThemeStyle.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("主题设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_description_back)
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
                .verticalScroll(rememberScrollState())
        ) {
            // Theme Style Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "主题风格",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "选择您喜欢的配色方案",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Theme style options
                    ThemeStyle.values().forEach { themeStyle ->
                        ThemeStyleOption(
                            themeStyle = themeStyle,
                            isSelected = themeStyle == currentThemeStyle,
                            onClick = { viewModel.setThemeStyle(themeStyle) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            
            // Dark Mode Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isDarkMode) "深色模式" else "浅色模式",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isDarkMode) "适合夜间使用" else "适合日间使用",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                }
            }
            
            // Theme Preview Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "主题预览",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Color preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ColorPreviewCircle(
                            color = MaterialTheme.colorScheme.primary,
                            label = "主色"
                        )
                        ColorPreviewCircle(
                            color = MaterialTheme.colorScheme.secondary,
                            label = "辅色"
                        )
                        ColorPreviewCircle(
                            color = MaterialTheme.colorScheme.background,
                            label = "背景"
                        )
                        ColorPreviewCircle(
                            color = MaterialTheme.colorScheme.surface,
                            label = "表面"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Individual theme style option component
 */
@Composable
private fun ThemeStyleOption(
    themeStyle: ThemeStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder(true)
        } else {
            null
        },
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getThemeStyleName(themeStyle),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = getThemeDescription(themeStyle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Theme color preview
            ThemeColorPreview(themeStyle)
            
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Color preview for theme styles
 */
@Composable
private fun ThemeColorPreview(themeStyle: ThemeStyle) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val colors = when (themeStyle) {
            ThemeStyle.NORDIC -> listOf(
                Color(0xFF4A90E2), // Nordic Blue
                Color(0xFF50C878), // Nordic Green
                Color(0xFF2A2A2A)  // Dark Surface
            )
            ThemeStyle.DEEP_NIGHT -> listOf(
                Color(0xFF00D4FF), // Neon Blue
                Color(0xFF00FF88), // Neon Green
                Color(0xFF0D0D0D)  // Pure Black
            )
            ThemeStyle.MINT_MORNING -> listOf(
                Color(0xFF4ECDC4), // Mint Green
                Color(0xFF44A08D), // Forest Green
                Color(0xFFF8FFFE)  // Light Mint
            )
            ThemeStyle.SCHOLAR_BLUE -> listOf(
                Color(0xFF2C5F8D), // Academic Blue
                Color(0xFF8B4513), // Leather Brown
                Color(0xFFFAFBFC)  // Light Background
            )
        }
        
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, shape = MaterialTheme.shapes.small)
            )
        }
    }
}

/**
 * Color preview circle component
 */
@Composable
private fun ColorPreviewCircle(
    color: Color,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Get theme style name for display
 */
private fun getThemeStyleName(themeStyle: ThemeStyle): String {
    return when (themeStyle) {
        ThemeStyle.NORDIC -> "北欧风格"
        ThemeStyle.DEEP_NIGHT -> "深邃夜空"
        ThemeStyle.MINT_MORNING -> "薄荷晨露"
        ThemeStyle.SCHOLAR_BLUE -> "学院蓝调"
    }
}

/**
 * Get theme description
 */
private fun getThemeDescription(themeStyle: ThemeStyle): String {
    return when (themeStyle) {
        ThemeStyle.NORDIC -> "经典的北欧简约设计"
        ThemeStyle.DEEP_NIGHT -> "纯黑背景配霓虹蓝绿点缀"
        ThemeStyle.MINT_MORNING -> "清新薄荷绿配柔和色调"
        ThemeStyle.SCHOLAR_BLUE -> "知性深蓝配经典学术风"
    }
}