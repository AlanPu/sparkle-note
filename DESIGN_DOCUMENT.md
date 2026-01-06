# Sparkle Note 项目完整设计文档

## 📖 目录

1. [项目概览](#1-项目概览)
2. [技术架构详解](#2-技术架构详解)
3. [UI设计文档](#3-ui设计文档)
4. [数据模型和接口定义](#4-数据模型和接口定义)
5. [核心功能实现](#5-核心功能实现)
6. [开发规范和最佳实践](#6-开发规范和最佳实践)
7. [部署和发布](#7-部署和发布)

---

## 1. 项目概览

### 1.1 项目背景和目标

**Sparkle Note** 是一款现代化的灵感记录应用，旨在帮助用户快速捕捉和管理日常灵感。项目采用最新的 Android 开发技术栈，提供优雅的用户体验和强大的功能特性。

**核心目标：**
- 提供快速、便捷的灵感记录体验
- 支持多主题分类管理
- 实现智能搜索和筛选功能
- 提供数据备份和导出功能
- 打造现代化的用户界面

### 1.2 核心功能特性

| 功能模块 | 特性描述 |
|---------|---------|
| 📝 灵感记录 | 快速输入、主题分类、字符计数、自动保存 |
| 🎨 主题管理 | 主题CRUD、动态加载、主题统计、颜色标识 |
| 🔍 智能搜索 | 模糊搜索、多主题筛选、时间过滤、搜索历史 |
| 📦 批量操作 | 多选操作、批量导出、批量删除、格式转换 |
| 💾 数据备份 | JSON备份、Markdown导出、CSV导出、版本控制 |
| 🌙 主题系统 | 4种内置主题、深色模式、动态切换、个性化定制 |

### 1.3 技术架构总览

```
┌─────────────────────────────────────┐
│         UI Layer (Compose)         │
├─────────────────────────────────────┤
│     Presentation Layer (MVVM)       │
├─────────────────────────────────────┤
│         Domain Layer               │
├─────────────────────────────────────┤
│          Data Layer                │
└─────────────────────────────────────┘
```

### 1.4 开发环境要求

- **开发语言**：Kotlin 1.9.23
- **目标平台**：Android 7.0+ (API 24+)
- **构建工具**：Gradle 8.0+
- **IDE**：Android Studio Hedgehog 或更高版本
- **JDK**：17

---

## 2. 技术架构详解

### 2.1 代码结构分析

```
com.sparkle.note/
├── SparkleNoteApplication.kt     # 应用入口
├── MainActivity.kt              # 主活动
├── data/                        # 数据层
│   ├── database/               # 数据库
│   ├── entity/                 # 数据实体
│   └── repository/            # 仓库实现
├── domain/                      # 领域层
│   ├── model/                  # 领域模型
│   └── repository/             # 仓库接口
├── ui/                          # UI层
│   ├── components/            # UI组件
│   ├── screens/               # 屏幕页面
│   ├── navigation/            # 导航配置
│   └── theme/                 # 主题系统
└── utils/                     # 工具类
```

### 2.2 技术栈详细列表

#### 核心技术
- **Kotlin 1.9.23** - 主要开发语言
- **Android SDK 34** - 目标平台
- **Jetpack Compose BOM 2024.02.00** - UI框架
- **Material3** - 设计系统
- **Room 2.6.1** - 数据库ORM
- **Dagger Hilt 2.50** - 依赖注入
- **Kotlin Coroutines 1.8.0** - 异步编程

#### 辅助库
- **DataStore 1.0.0** - 数据持久化
- **Compose Navigation** - 导航管理
- **Kotlin Serialization 1.6.3** - JSON序列化

#### 测试框架
- **JUnit 4/5** - 单元测试
- **Truth 1.4.2** - 断言库
- **Turbine 1.0.0** - Flow测试
- **Mockito Kotlin 5.2.1** - Mock框架

### 2.3 架构模式实现

#### Clean Architecture 分层

1. **Presentation Layer**
   - Compose UI组件
   - ViewModel状态管理
   - UI状态模型

2. **Domain Layer**
   - 业务逻辑封装
   - 领域模型定义
   - 仓库接口契约

3. **Data Layer**
   - Room数据库
   - 数据实体映射
   - 仓库接口实现

#### MVVM 模式

```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: InspirationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    fun addInspiration(content: String, theme: Theme) {
        viewModelScope.launch {
            val inspiration = Inspiration(
                content = content,
                themeName = theme.name
            )
            repository.insertInspiration(inspiration)
        }
    }
}
```

### 2.4 依赖注入设计

#### Hilt 模块配置

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideInspirationDatabase(@ApplicationContext context: Context): InspirationDatabase {
        return Room.databaseBuilder(
            context,
            InspirationDatabase::class.java,
            "inspiration_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideInspirationDao(database: InspirationDatabase): InspirationDao {
        return database.inspirationDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindInspirationRepository(
        inspirationRepositoryImpl: InspirationRepositoryImpl
    ): InspirationRepository
}
```

### 2.5 状态管理方案

#### 多层状态架构

```kotlin
// UI层状态
@Composable
fun InspirationCard(inspiration: Inspiration) {
    var expanded by remember { mutableStateOf(false) }
    // 组件级状态
}

// ViewModel状态
class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<MainEvent>()
    val events: SharedFlow<MainEvent> = _events.asSharedFlow()
}

// 数据层状态
@Dao
interface InspirationDao {
    @Query("SELECT * FROM inspirations")
    fun getAllInspirations(): Flow<List<InspirationEntity>>
}
```

### 2.6 数据流设计

#### 响应式数据流

```
用户操作 → UI事件 → ViewModel → Repository → DAO → Database
     ↑                                                    ↓
     ←────────────────── Flow ←───────────────────────────
```

#### 状态组合

```kotlin
val inspirations = combine(
    repository.getAllInspirations(),
    searchQuery,
    selectedTheme
) { inspirations, query, theme ->
    filterInspirations(inspirations, query, theme)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
)
```

---

## 3. UI设计文档

### 3.1 UI组件库

#### 3.1.1 核心功能组件

**QuickRecordSection** - 快速记录组件
```kotlin
@Composable
fun QuickRecordSection(
    content: String,
    onContentChange: (String) -> Unit,
    selectedTheme: Theme?,
    onThemeSelect: (Theme) -> Unit,
    onSave: () -> Unit,
    themes: List<Theme>
)
```
- 功能：灵感内容输入、主题选择、保存操作
- 特色：带动画效果、字符计数、主题选择器集成
- 布局：卡片式设计，包含标题、输入框、主题选择行、保存按钮

**InspirationCard** - 灵感卡片组件
```kotlin
@Composable
fun InspirationCard(
    content: String,
    themeName: String,
    createdAtText: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
)
```
- 功能：展示灵感内容、主题信息、创建时间
- 特色：动画入场效果、长按菜单、主题emoji显示
- 布局：圆角卡片，包含头部信息区和内容区

#### 3.1.2 交互组件

**EnhancedThemeSelector** - 增强主题选择器
```kotlin
@Composable
fun EnhancedThemeSelector(
    selectedTheme: Theme?,
    onThemeSelected: (Theme) -> Unit,
    onCreateNewTheme: (String) -> Unit
)
```
- 功能：主题选择、动态主题加载、主题创建
- 特色：下拉菜单设计、实时主题数据、快速创建功能

**InspirationCardLongPressMenu** - 长按菜单
```kotlin
@Composable
fun InspirationCardLongPressMenu(
    inspiration: Inspiration,
    onDismiss: () -> Unit,
    onCopyContent: () -> Unit,
    onOpenLink: (String) -> Unit
)
```
- 功能：复制内容、打开链接、关闭操作
- 特色：智能链接检测、内容预览、主题信息显示

### 3.2 主要屏幕界面

#### 3.2.1 主屏幕 (EnhancedMainScreen)

**布局结构**
```
┌─────────────────────────────────────┐
│ 顶部操作栏 (ActionBar)              │
├─────────────────────────────────────┤
│                                     │
│  灵感卡片列表区域                      │
│  ┌───────────────────────────────┐   │
│  │ 灵感卡片1                    │   │
│  ├───────────────────────────────┤   │
│  │ 灵感卡片2                    │   │
│  ├───────────────────────────────┤   │
│  │ ...                          │   │
│  └───────────────────────────────┘   │
│                                     │
├─────────────────────────────────────┤
│ 底部快速记录区 (BottomQuickRecord)   │
└─────────────────────────────────────┘
```

**设计特点**
- **移动优先**：底部输入设计符合手机使用习惯
- **空间最大化**：卡片列表占据主要空间
- **操作便捷**：常用功能一键可达
- **视觉层次**：通过阴影、间距建立清晰的层次结构

#### 3.2.2 高级搜索屏幕

**功能特性**
- 多关键词模糊搜索
- 多主题同时筛选
- 时间范围过滤（今天/本周/本月/全部）
- 搜索历史记录
- 搜索结果实时显示

**界面布局**
- 顶部搜索栏
- 筛选条件区域（主题选择器、时间过滤器）
- 搜索结果列表
- 空状态提示

#### 3.2.3 批量操作屏幕

**核心功能**
- 多选模式切换
- 批量选择/取消选择
- 批量删除（带确认）
- 批量导出（Markdown/JSON/CSV）
- 操作进度显示

### 3.3 主题系统设计

#### 3.3.1 四种内置主题

**1. 北欧风格 (NORDIC)** - 默认主题
- 主色：#4A90E2 (北欧蓝)
- 强调色：#50C878 (北欧绿)
- 背景：#1A1A1A (深灰)
- 表面：#2A2A2A (中灰)

**2. 深邃夜空 (DEEP_NIGHT)**
- 主色：#00D4FF (霓虹蓝)
- 背景：#0D0D0D (纯黑)
- 表面：#1A1A1A (深灰)

**3. 薄荷晨露 (MINT_MORNING)**
- 主色：#4ECDC4 (薄荷绿)
- 背景：#F8FFFE (极淡薄荷)
- 清新明亮风格

**4. 学院蓝调 (SCHOLAR_BLUE)**
- 主色：#2C5F8D (学术蓝)
- 辅助色：#8B4513 (皮革棕)
- 经典学术风格

#### 3.3.2 主题管理架构

```kotlin
@Composable
fun SparkleNoteTheme(
    themeStyle: ThemeStyle = ThemeStyle.NORDIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeStyle) {
        ThemeStyle.NORDIC -> if (darkTheme) NordicDarkColorScheme else NordicLightColorScheme
        // ... 其他主题
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### 3.4 交互设计和用户体验

#### 3.4.1 交互模式

**主要交互方式**
1. **点击操作**：卡片展开、按钮响应
2. **长按操作**：上下文菜单、多选模式
3. **滑动操作**：列表滚动、快速操作
4. **输入交互**：实时搜索、字符计数

**动画设计**
- 入场动画：组件加载时的缩放和淡入效果
- 状态动画：选择状态的平滑过渡
- 反馈动画：按钮点击的视觉反馈

#### 3.4.2 无障碍设计

- **语义化内容**：所有交互元素都有清晰的内容描述
- **触摸目标**：按钮和可点击区域符合最小触摸目标要求（48dp）
- **高对比度**：色彩对比度符合WCAG标准
- **文字缩放**：支持系统字体大小调整

---

## 4. 数据模型和接口定义

### 4.1 领域模型 (Domain Models)

#### 4.1.1 灵感模型 (Inspiration)

```kotlin
data class Inspiration(
    val id: Long = 0,
    val content: String,
    val themeName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val wordCount: Int = content.length
) {
    fun validateContent(): ValidationResult {
        return when {
            content.isBlank() -> ValidationResult.Error("内容不能为空")
            content.length > 1000 -> ValidationResult.Error("内容不能超过1000字符")
            else -> ValidationResult.Success
        }
    }
}
```

#### 4.1.2 主题模型 (Theme)

```kotlin
data class Theme(
    val name: String,
    val icon: String = "💡",
    val color: Long = 0xFF4A90E2,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis(),
    val inspirationCount: Int = 0
) {
    fun validateName(): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("主题名称不能为空")
            name.length > 20 -> ValidationResult.Error("主题名称不能超过20字符")
            else -> ValidationResult.Success
        }
    }
}
```

### 4.2 数据实体 (Data Entities)

#### 4.2.1 灵感实体 (InspirationEntity)

```kotlin
@Entity(
    tableName = "inspirations",
    foreignKeys = [ForeignKey(
        entity = ThemeEntity::class,
        parentColumns = ["name"],
        childColumns = ["theme_name"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["content"]),
        Index(value = ["theme_name"]),
        Index(value = ["created_at"])
    ]
)
data class InspirationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val theme_name: String,
    val created_at: Long,
    val word_count: Int
)
```

#### 4.2.2 主题实体 (ThemeEntity)

```kotlin
@Entity(tableName = "themes")
data class ThemeEntity(
    @PrimaryKey val name: String,
    val icon: String = "💡",
    val color: Long = 0xFF4A90E2,
    val description: String = "",
    val createdAt: Long,
    val lastUsed: Long,
    val inspirationCount: Int = 0
)
```

### 4.3 仓库接口 (Repository Interfaces)

#### 4.3.1 灵感仓库接口

```kotlin
interface InspirationRepository {
    fun getAllInspirations(): Flow<List<Inspiration>>
    fun searchInspirations(keyword: String): Flow<List<Inspiration>>
    fun getInspirationsByTheme(themeName: String): Flow<List<Inspiration>>
    suspend fun insertInspiration(inspiration: Inspiration): Result<Unit>
    suspend fun deleteInspiration(inspiration: Inspiration): Result<Unit>
    suspend fun getInspirationById(id: Long): Inspiration?
    suspend fun getInspirationsCount(): Long
    suspend fun exportInspirationsToFile(format: String): Result<File>
    suspend fun backupData(): Result<File>
}
```

#### 4.3.2 主题仓库接口

```kotlin
interface ThemeRepository {
    fun getAllThemes(): Flow<List<Theme>>
    fun getThemesByUsage(): Flow<List<Theme>>
    suspend fun createTheme(theme: Theme): Result<Unit>
    suspend fun updateTheme(theme: Theme): Result<Unit>
    suspend fun deleteTheme(themeName: String): Result<Unit>
    suspend fun themeExists(themeName: String): Boolean
    suspend fun updateThemeLastUsed(themeName: String): Result<Unit>
}
```

### 4.4 DAO接口定义

#### 4.4.1 灵感数据访问对象

```kotlin
@Dao
interface InspirationDao {
    @Query("SELECT * FROM inspirations ORDER BY created_at DESC")
    fun getAllInspirations(): Flow<List<InspirationEntity>>
    
    @Query("SELECT * FROM inspirations WHERE content LIKE '%' || :keyword || '%' ORDER BY created_at DESC")
    fun searchInspirations(keyword: String): Flow<List<InspirationEntity>>
    
    @Query("SELECT * FROM inspirations WHERE theme_name = :themeName ORDER BY created_at DESC")
    fun getInspirationsByTheme(themeName: String): Flow<List<InspirationEntity>>
    
    @Insert
    suspend fun insertInspiration(inspiration: InspirationEntity)
    
    @Update
    suspend fun updateInspiration(inspiration: InspirationEntity)
    
    @Delete
    suspend fun deleteInspiration(inspiration: InspirationEntity)
    
    @Query("DELETE FROM inspirations WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("SELECT COUNT(*) FROM inspirations")
    suspend fun getInspirationsCount(): Long
    
    @Query("SELECT COUNT(*) FROM inspirations WHERE theme_name = :themeName")
    suspend fun getInspirationsCountByTheme(themeName: String): Long
}
```

#### 4.4.2 主题数据访问对象

```kotlin
@Dao
interface ThemeDao {
    @Query("SELECT * FROM themes ORDER BY lastUsed DESC")
    fun getAllThemes(): Flow<List<ThemeEntity>>
    
    @Query("SELECT * FROM themes ORDER BY inspirationCount DESC")
    fun getThemesByUsage(): Flow<List<ThemeEntity>>
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTheme(theme: ThemeEntity)
    
    @Update
    suspend fun updateTheme(theme: ThemeEntity)
    
    @Delete
    suspend fun deleteTheme(theme: ThemeEntity)
    
    @Query("DELETE FROM themes WHERE name = :themeName")
    suspend fun deleteThemeByName(themeName: String)
    
    @Query("SELECT EXISTS(SELECT 1 FROM themes WHERE name = :themeName)")
    suspend fun themeExists(themeName: String): Boolean
    
    @Query("UPDATE themes SET lastUsed = :timestamp WHERE name = :themeName")
    suspend fun updateThemeLastUsed(themeName: String, timestamp: Long)
    
    @Query("UPDATE themes SET inspirationCount = :count WHERE name = :themeName")
    suspend fun updateThemeInspirationCount(themeName: String, count: Int)
}
```

### 4.5 UI状态模型

#### 4.5.1 主屏幕状态

```kotlin
data class MainUiState(
    val inspirations: List<Inspiration> = emptyList(),
    val availableThemes: List<Theme> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class MainEvent {
    data class ShowError(val message: String) : MainEvent()
    data class ShowDeleteSuccess(val message: String) : MainEvent()
    data class CopyToClipboard(val content: String) : MainEvent()
    data class OpenLink(val url: String) : MainEvent()
}
```

#### 4.5.2 主题管理状态

```kotlin
data class ThemeManagementUiState(
    val themes: List<Theme> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingTheme: Theme? = null
)
```

### 4.6 数据转换逻辑

#### 4.6.1 实体到领域模型转换

```kotlin
fun InspirationEntity.toDomain(): Inspiration {
    return Inspiration(
        id = this.id,
        content = this.content,
        themeName = this.theme_name,
        createdAt = this.created_at,
        wordCount = this.word_count
    )
}

fun ThemeEntity.toDomain(): Theme {
    return Theme(
        name = this.name,
        icon = this.icon,
        color = this.color,
        description = this.description,
        createdAt = this.createdAt,
        lastUsed = this.lastUsed,
        inspirationCount = this.inspirationCount
    )
}
```

#### 4.6.2 领域模型到实体转换

```kotlin
fun Inspiration.toEntity(): InspirationEntity {
    return InspirationEntity(
        id = this.id,
        content = this.content,
        theme_name = this.themeName,
        created_at = this.createdAt,
        word_count = this.wordCount
    )
}

fun Theme.toEntity(): ThemeEntity {
    return ThemeEntity(
        name = this.name,
        icon = this.icon,
        color = this.color,
        description = this.description,
        createdAt = this.createdAt,
        lastUsed = this.lastUsed,
        inspirationCount = this.inspirationCount
    )
}
```

---

## 5. 核心功能实现

### 5.1 灵感记录功能

#### 5.1.1 创建灵感流程

```kotlin
class MainViewModel @Inject constructor(
    private val inspirationRepository: InspirationRepository,
    private val themeRepository: ThemeRepository
) : ViewModel() {
    
    fun addInspiration() {
        val content = _uiState.value.inputText.trim()
        val theme = _selectedTheme.value
        
        if (content.isBlank()) {
            _events.emit(MainEvent.ShowError("请输入灵感内容"))
            return
        }
        
        if (theme == null) {
            _events.emit(MainEvent.ShowError("请选择主题"))
            return
        }
        
        viewModelScope.launch {
            try {
                val inspiration = Inspiration(
                    content = content,
                    themeName = theme.name,
                    createdAt = System.currentTimeMillis()
                )
                
                val result = inspirationRepository.insertInspiration(inspiration)
                
                if (result.isSuccess) {
                    _uiState.update { it.copy(inputText = "") }
                    _events.emit(MainEvent.ShowDeleteSuccess("灵感已保存"))
                    // 更新主题最后使用时间
                    themeRepository.updateThemeLastUsed(theme.name)
                } else {
                    _events.emit(MainEvent.ShowError(result.exceptionOrNull()?.message ?: "保存失败"))
                }
            } catch (e: Exception) {
                _events.emit(MainEvent.ShowError(e.message ?: "保存失败"))
            }
        }
    }
}
```

#### 5.1.2 删除灵感流程

```kotlin
fun deleteInspiration(inspiration: Inspiration) {
    viewModelScope.launch {
        try {
            val result = inspirationRepository.deleteInspiration(inspiration)
            
            if (result.isSuccess) {
                _events.emit(MainEvent.ShowDeleteSuccess("灵感已删除"))
                // 更新主题统计信息
                themeRepository.updateThemeLastUsed(inspiration.themeName)
            } else {
                _events.emit(MainEvent.ShowError(result.exceptionOrNull()?.message ?: "删除失败"))
            }
        } catch (e: Exception) {
            _events.emit(MainEvent.ShowError(e.message ?: "删除失败"))
        }
    }
}
```

### 5.2 主题管理功能

#### 5.2.1 动态主题加载

```kotlin
class ThemeManagementViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {
    
    init {
        loadThemes()
    }
    
    private fun loadThemes() {
        viewModelScope.launch {
            themeRepository.getAllThemes()
                .collect { themes ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            themes = themes,
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    fun createTheme(themeName: String) {
        viewModelScope.launch {
            try {
                // 验证主题名称
                if (themeName.isBlank()) {
                    _events.emit(ThemeManagementEvent.ShowError("主题名称不能为空"))
                    return@launch
                }
                
                // 检查主题是否已存在
                if (themeRepository.themeExists(themeName)) {
                    _events.emit(ThemeManagementEvent.ShowError("主题已存在"))
                    return@launch
                }
                
                // 创建新主题
                val newTheme = Theme(
                    name = themeName,
                    icon = "💡",
                    color = 0xFF4A90E2,
                    createdAt = System.currentTimeMillis(),
                    lastUsed = System.currentTimeMillis()
                )
                
                val result = themeRepository.createTheme(newTheme)
                
                if (result.isSuccess) {
                    _events.emit(ThemeManagementEvent.ShowSuccess("主题创建成功"))
                    hideCreateDialog()
                } else {
                    _events.emit(ThemeManagementEvent.ShowError(result.exceptionOrNull()?.message ?: "创建失败"))
                }
            } catch (e: Exception) {
                _events.emit(ThemeManagementEvent.ShowError(e.message ?: "创建失败"))
            }
        }
    }
}
```

### 5.3 搜索功能实现

#### 5.3.1 模糊搜索

```kotlin
class AdvancedSearchViewModel @Inject constructor(
    private val inspirationRepository: InspirationRepository
) : ViewModel() {
    
    fun performSearch() {
        val query = _searchQuery.value.trim()
        val selectedThemes = _selectedThemes.value
        val timeFilter = _timeFilter.value
        
        if (query.isBlank() && selectedThemes.isEmpty() && timeFilter == TimeFilter.ALL) {
            // 如果没有筛选条件，显示所有数据
            loadAllInspirations()
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // 组合多个搜索条件
                val searchFlow = combine(
                    inspirationRepository.getAllInspirations(),
                    flowOf(query),
                    flowOf(selectedThemes),
                    flowOf(timeFilter)
                ) { inspirations, query, themes, filter ->
                    filterInspirations(inspirations, query, themes, filter)
                }
                
                searchFlow.collect { filteredInspirations ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            inspirations = filteredInspirations,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "搜索失败",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    private fun filterInspirations(
        inspirations: List<Inspiration>,
        query: String,
        selectedThemes: Set<String>,
        timeFilter: TimeFilter
    ): List<Inspiration> {
        return inspirations.filter { inspiration ->
            // 关键词匹配
            val matchesQuery = query.isBlank() || 
                inspiration.content.contains(query, ignoreCase = true)
            
            // 主题匹配
            val matchesTheme = selectedThemes.isEmpty() || 
                selectedThemes.contains(inspiration.themeName)
            
            // 时间匹配
            val matchesTime = when (timeFilter) {
                TimeFilter.TODAY -> isToday(inspiration.createdAt)
                TimeFilter.THIS_WEEK -> isThisWeek(inspiration.createdAt)
                TimeFilter.THIS_MONTH -> isThisMonth(inspiration.createdAt)
                TimeFilter.ALL -> true
            }
            
            matchesQuery && matchesTheme && matchesTime
        }
    }
}
```

### 5.4 批量操作功能

#### 5.4.1 多选模式管理

```kotlin
class BatchOperationViewModel @Inject constructor(
    private val inspirationRepository: InspirationRepository,
    private val exportManager: ExportManager
) : ViewModel() {
    
    private val _selectedInspirations = MutableStateFlow<Set<Inspiration>>(emptySet())
    val selectedInspirations: StateFlow<Set<Inspiration>> = _selectedInspirations.asStateFlow()
    
    fun toggleInspirationSelection(inspiration: Inspiration) {
        val currentSelection = _selectedInspirations.value
        _selectedInspirations.value = if (currentSelection.contains(inspiration)) {
            currentSelection - inspiration
        } else {
            currentSelection + inspiration
        }
    }
    
    fun toggleSelectAll() {
        val allInspirations = _uiState.value.inspirations
        val currentSelection = _selectedInspirations.value
        
        _selectedInspirations.value = if (currentSelection.size == allInspirations.size) {
            emptySet()
        } else {
            allInspirations.toSet()
        }
    }
    
    fun performBatchDelete() {
        val selected = _selectedInspirations.value.toList()
        
        viewModelScope.launch {
            try {
                val results = selected.map { inspiration ->
                    inspirationRepository.deleteInspiration(inspiration)
                }
                
                val successCount = results.count { it.isSuccess }
                
                if (successCount == selected.size) {
                    _events.emit(BatchOperationEvent.ShowSuccess("成功删除 ${selected.size} 条灵感"))
                    _selectedInspirations.value = emptySet()
                } else {
                    _events.emit(BatchOperationEvent.ShowError("部分删除失败，成功删除 $successCount 条"))
                }
            } catch (e: Exception) {
                _events.emit(BatchOperationEvent.ShowError("批量删除失败: ${e.message}"))
            }
        }
    }
}
```

### 5.5 数据备份和导出功能

#### 5.5.1 Markdown导出

```kotlin
object ExportManager {
    
    fun exportToMarkdown(inspirations: List<Inspiration>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = dateFormat.format(Date())
        
        val markdown = buildString {
            appendLine("# Sparkle Note 导出")
            appendLine("生成时间: $currentTime")
            appendLine("导出数量: ${inspirations.size} 条")
            appendLine()
            
            inspirations.forEach { inspiration ->
                appendLine("## ${inspiration.themeName} ${getThemeEmoji(inspiration.themeName)}")
                appendLine("**创建时间:** ${dateFormat.format(Date(inspiration.createdAt))}")
                appendLine("**字数:** ${inspiration.wordCount}")
                appendLine()
                appendLine(inspiration.content)
                appendLine()
                appendLine("---")
                appendLine()
            }
        }
        
        return markdown
    }
    
    fun exportBatchToMarkdown(inspirations: List<Inspiration>): String {
        return if (inspirations.size == 1) {
            exportToMarkdown(inspirations)
        } else {
            // 按主题分组导出
            val groupedByTheme = inspirations.groupBy { it.themeName }
            
            buildString {
                appendLine("# Sparkle Note 批量导出")
                appendLine("生成时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
                appendLine("导出数量: ${inspirations.size} 条")
                appendLine("涉及主题: ${groupedByTheme.keys.size} 个")
                appendLine()
                
                groupedByTheme.forEach { (themeName, themeInspirations) ->
                    appendLine("## $themeName ${getThemeEmoji(themeName)} (${themeInspirations.size}条)")
                    appendLine()
                    
                    themeInspirations.forEach { inspiration ->
                        appendLine("### ${SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(inspiration.createdAt))}")
                        appendLine("${inspiration.content}")
                        appendLine()
                    }
                    
                    appendLine("---")
                    appendLine()
                }
            }
        }
    }
}
```

#### 5.5.2 JSON备份

```kotlin
object BackupManager {
    
    @Serializable
    data class BackupData(
        val version: String = "1.0",
        val createdAt: Long,
        val inspirations: List<InspirationBackup>,
        val themes: List<ThemeBackup>,
        val metadata: BackupMetadata
    )
    
    @Serializable
    data class InspirationBackup(
        val id: Long,
        val content: String,
        val themeName: String,
        val createdAt: Long,
        val wordCount: Int
    )
    
    @Serializable
    data class ThemeBackup(
        val name: String,
        val icon: String,
        val color: Long,
        val description: String,
        val createdAt: Long,
        val lastUsed: Long,
        val inspirationCount: Int
    )
    
    @Serializable
    data class BackupMetadata(
        val appVersion: String,
        val exportFormat: String,
        val totalInspirations: Int,
        val totalThemes: Int,
        val exportTimestamp: Long
    )
    
    fun createBackup(inspirations: List<Inspiration>, themes: List<Theme>): String {
        val backupData = BackupData(
            createdAt = System.currentTimeMillis(),
            inspirations = inspirations.map { inspiration ->
                InspirationBackup(
                    id = inspiration.id,
                    content = inspiration.content,
                    themeName = inspiration.themeName,
                    createdAt = inspiration.createdAt,
                    wordCount = inspiration.wordCount
                )
            },
            themes = themes.map { theme ->
                ThemeBackup(
                    name = theme.name,
                    icon = theme.icon,
                    color = theme.color,
                    description = theme.description,
                    createdAt = theme.createdAt,
                    lastUsed = theme.lastUsed,
                    inspirationCount = theme.inspirationCount
                )
            },
            metadata = BackupMetadata(
                appVersion = BuildConfig.VERSION_NAME,
                exportFormat = "JSON",
                totalInspirations = inspirations.size,
                totalThemes = themes.size,
                exportTimestamp = System.currentTimeMillis()
            )
        )
        
        return Json.encodeToString(backupData)
    }
}
```

---

## 6. 开发规范和最佳实践

### 6.1 代码规范

#### 6.1.1 命名规范

**类命名**
- 使用 PascalCase
- 后缀明确：Repository, ViewModel, Screen, Component, Dao, Entity
- 示例：`InspirationRepository`, `MainViewModel`, `EnhancedMainScreen`

**函数命名**
- 使用 camelCase
- 动词开头，描述清楚功能
- 示例：`getAllInspirations()`, `performSearch()`, `toggleThemeSelection()`

**变量命名**
- 使用 camelCase
- 避免缩写，除非是行业标准
- 示例：`selectedTheme`, `searchQuery`, `inspirationCount`

#### 6.1.2 代码结构

**文件组织**
```kotlin
// 1. 导入语句
import ...

// 2. 类/接口定义
class InspirationRepositoryImpl @Inject constructor(
    private val inspirationDao: InspirationDao,
    private val themeDao: ThemeDao
) : InspirationRepository {
    
    // 3. 公共函数（按功能分组）
    override fun getAllInspirations(): Flow<List<Inspiration>> {
        // 实现代码
    }
    
    // 4. 私有函数
    private fun updateThemeStats(themeName: String) {
        // 实现代码
    }
    
    // 5. 扩展函数
    private fun InspirationEntity.toDomain(): Inspiration {
        // 转换逻辑
    }
}
```

### 6.2 提交规范

项目采用 **Conventional Commits** 规范：

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

**提交类型**
- `feat`: 新功能
- `fix`: 错误修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

**示例**
```
feat: 添加主题管理功能

- 实现主题的CRUD操作
- 添加主题选择器组件
- 支持动态主题加载

Closes #123
```

### 6.3 测试策略

#### 6.3.1 单元测试覆盖

**ViewModel测试**
```kotlin
@Test
fun `addInspiration with valid content should succeed`() = runTest {
    // Given
    val content = "测试灵感内容"
    val theme = Theme("测试主题")
    coEvery { repository.insertInspiration(any()) } returns Result.success(Unit)
    
    // When
    viewModel.updateInputText(content)
    viewModel.setSelectedTheme(theme)
    viewModel.addInspiration()
    
    // Then
    coVerify { repository.insertInspiration(match {
        it.content == content && it.themeName == theme.name
    }) }
}
```

**Repository测试**
```kotlin
@Test
fun `getAllInspirations should return flow of inspirations`() = runTest {
    // Given
    val testInspirations = listOf(
        InspirationEntity(1, "内容1", "主题1", 1000, 6),
        InspirationEntity(2, "内容2", "主题2", 2000, 6)
    )
    coEvery { dao.getAllInspirations() } returns flowOf(testInspirations)
    
    // When
    val result = repository.getAllInspirations()
    
    // Then
    result.test {
        val inspirations = awaitItem()
        assertThat(inspirations).hasSize(2)
        assertThat(inspirations[0].content).isEqualTo("内容1")
        cancel()
    }
}
```

#### 6.3.2 UI测试

```kotlin
@Test
fun `inspiration card should display content and theme`() {
    composeTestRule.setContent {
        InspirationCard(
            content = "测试灵感内容",
            themeName = "工作",
            createdAtText = "刚刚"
        )
    }
    
    composeTestRule.onNodeWithText("测试灵感内容").assertIsDisplayed()
    composeTestRule.onNodeWithText("工作").assertIsDisplayed()
    composeTestRule.onNodeWithText("刚刚").assertIsDisplayed()
}
```

### 6.4 性能优化

#### 6.4.1 数据库优化

**索引优化**
```kotlin
@Entity(
    tableName = "inspirations",
    indices = [
        Index(value = ["content"]),      // 搜索优化
        Index(value = ["theme_name"]),   // 主题筛选优化
        Index(value = ["created_at"])    // 排序优化
    ]
)
```

**查询优化**
```kotlin
@Query("""
    SELECT * FROM inspirations 
    WHERE content LIKE '%' || :keyword || '%' 
    AND theme_name IN (:themes)
    AND created_at >= :startTime
    ORDER BY created_at DESC
    LIMIT :limit
""")
suspend fun searchInspirationsAdvanced(
    keyword: String,
    themes: List<String>,
    startTime: Long,
    limit: Int = 100
): List<InspirationEntity>
```

#### 6.4.2 UI性能优化

**列表优化**
```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(
        items = inspirations,
        key = { it.id }  // 使用稳定key提升性能
    ) { inspiration ->
        InspirationCard(
            content = inspiration.content,
            themeName = inspiration.themeName,
            createdAtText = formatTimeAgo(inspiration.createdAt),
            onClick = { /* 处理点击 */ },
            onLongClick = { /* 处理长按 */ }
        )
    }
}
```

**动画优化**
```kotlin
@Composable
fun InspirationCard(/* 参数 */) {
    val scale = remember { Animatable(0.95f) }
    val alpha = remember { Animatable(0.8f) }
    
    LaunchedEffect(Unit) {
        // 并行动画提升性能
        launch {
            scale.animateTo(1f, animationSpec = tween(300))
        }
        launch {
            alpha.animateTo(1f, animationSpec = tween(200))
        }
    }
}
```

#### 6.4.3 内存优化

**协程作用域管理**
```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: InspirationRepository
) : ViewModel() {
    
    // 使用viewModelScope自动管理生命周期
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    override fun onCleared() {
        super.onCleared()
        // 清理资源
    }
}
```

**资源清理**
```kotlin
@Composable
fun InspirationCard(/* 参数 */) {
    val context = LocalContext.current
    
    DisposableEffect(Unit) {
        onDispose {
            // 清理资源
        }
    }
}
```

---

## 7. 部署和发布

### 7.1 构建配置

#### 7.1.1 APK输出配置

```kotlin
android {
    // ... 其他配置
    
    // 配置APK输出文件名
    applicationVariants.configureEach {
        outputs.configureEach {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                val buildType = buildType.name
                outputFileName = when (buildType) {
                    "release" -> "sparkle-note.apk"
                    "debug" -> "sparkle-note-debug.apk"
                    else -> "sparkle-note-${buildType}.apk"
                }
            }
        }
    }
}
```

#### 7.1.2 版本管理

```kotlin
android {
    defaultConfig {
        applicationId = "com.sparkle.note"
        minSdk = 24
        targetSdk = 34
        versionCode = 1                    // 内部版本号，递增
        versionName = "1.0.0"              // 用户可见版本号
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
}
```

### 7.2 构建命令

#### 7.2.1 开发构建

```bash
# 调试版本
./gradlew assembleDebug

# 发布版本
./gradlew assembleRelease

# 清理并构建
./gradlew clean assembleRelease
```

#### 7.2.2 测试构建

```bash
# 运行单元测试
./gradlew test

# 运行UI测试
./gradlew connectedAndroidTest

# 生成测试报告
./gradlew jacocoTestReport
```

### 7.3 发布流程

#### 7.3.1 发布前检查清单

- [ ] 所有功能测试通过
- [ ] 代码审查完成
- [ ] 版本号正确更新
- [ ] 更新日志编写完成
- [ ] 性能测试通过
- [ ] 安全扫描通过

#### 7.3.2 发布渠道

1. **Google Play Store** - 主要发布渠道
2. **GitHub Releases** - 开源版本发布
3. **内部测试** - 小范围测试发布

#### 7.3.3 发布后监控

- 崩溃率监控
- 用户反馈收集
- 性能指标跟踪
- 版本迭代计划

---

## 总结

Sparkle Note 项目展现了现代 Android 应用开发的最佳实践：

### 🏗️ 架构优势
- **Clean Architecture**：清晰的分层架构，职责分离
- **MVVM模式**：响应式编程，状态管理清晰
- **依赖注入**：Hilt实现，解耦和可测试性强

### 🎨 设计亮点
- **Material Design 3**：现代化的设计语言
- **主题系统**：4种精美主题，支持深色模式
- **响应式UI**：适配不同屏幕尺寸和设备

### ⚡ 技术特色
- **Jetpack Compose**：声明式UI开发
- **Kotlin Flow**：响应式数据流
- **Room数据库**：类型安全的数据库访问

### 📈 功能完整
- **灵感记录**：快速记录和管理
- **主题管理**：灵活的主题分类
- **智能搜索**：多维度筛选和搜索
- **数据备份**：多种格式导出和备份

这个项目为 Android 应用开发提供了一个优秀的参考实现，涵盖了从架构设计到UI实现，从功能开发到测试部署的完整开发流程。