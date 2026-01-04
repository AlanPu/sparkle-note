# Sparkle Note - 开发规范与工程实践

> **重要声明**: 本文档为强制性开发规范，所有开发活动必须严格遵守。违反规范将导致代码审查失败。
>
> **版本**: 1.1
> **生效日期**: 2024-01-03
> **文档状态**: 已批准（必须遵守）

---

## 🚨 强制性规则（不可妥协）

### 1. 文档创建规范

**核心原则**: 最小化文档，代码即文档。

#### 1.1 文档创建审批流程

```
需要创建文档 → 评估是否必要 → 提出创建申请 → 人工审批 → 授权创建 → 在docs/目录下创建
```

**什么是必要的文档？**

| 文档类型 | 必要性 | 说明 |
|---------|-------|------|
| 设计方案（DESIGN.md） | ✅ 必须 | 已创建，记录系统架构和设计决策 |
| 开发规范（本文件） | ✅ 必须 | 强制执行的开发标准 |
| README.md | ✅ 必须 | 项目基本信息，仅包含项目描述和快速开始指南 |
| API接口文档 | ⚠️ 条件必须 | 只有供外部调用的公共API才需要 |
| 数据库迁移文档 | ⚠️ 条件必须 | 只有复杂的数据库schema变更才需要 |
| 测试文档 | ❌ 非必须 | 测试代码本身就是文档 |
| 流程文档 | ❌ 非必须 | 代码和注释说明流程 |
| 会议记录 | ❌ 禁止 | 使用代码提交信息记录决策 |

**文档创建审批流程：**

1. **识别需求**: 开发过程中发现需要创建新文档
2. **必要性评估**: 对照上表评估是否真正必要
3. **创建申请**: 向项目负责人提出书面申请，格式如下：

```markdown
### 文档创建申请

**申请创建的文档**: docs/API.md

**创建理由**: 
需要为InspirationRepository的公共API编写接口文档，因为...

**文档内容大纲**:
- Repository接口说明
- 各方法参数和返回值
- 使用示例

**预计篇幅**: 1-2页

**维护计划**: 接口变更时同步更新
```

4. **人工审批**: 等待明确的书面批准（评论回复"批准创建"）
5. **创建文档**: 获得授权后方可创建
6. **定期审查**: 每季度审查文档是否仍有必要

**违规后果**: 未经批准创建的文档将被立即删除，相关提交将被要求重写。

---

### 2. 测试驱动开发（TDD）规范

**核心原则**: 没有测试的代码就是错误代码。

#### 2.1 TDD工作流程（严格遵守）

```
需求分析 → 编写测试 → 运行测试（失败）→ 编写实现代码 → 运行测试（通过）→ 重构 → 提交代码
```

**具体规则：**

1. **先写测试**: 任何功能开发或代码修改，必须先编写单元测试
2. **测试失败**: 新测试必须首先失败（红色），证明测试有效
3. **实现代码**: 编写最少量的代码使测试通过
4. **测试通过**: 所有测试必须100%通过（绿色）
5. **重构**: 在安全网（测试）保护下重构代码
6. **回归测试**: 每次提交前运行完整的测试套件

#### 2.2 测试覆盖率要求

| 代码类型 | 覆盖率要求 | 说明 |
|---------|-----------|------|
| ViewModel | 100% | 所有业务逻辑必须有测试 |
| Repository | 100% | 所有数据操作必须有测试 |
| UseCase（如有） | 100% | 所有用例必须有测试 |
| Utils/Helper | 100% | 所有工具类必须有测试 |
| Compose UI | ≥ 80% | 核心交互路径必须测试 |
| Database/DAO | 100% | 所有查询必须测试 |
| API Service | 100% | 所有接口调用必须测试 |

**覆盖率计算工具**: JaCoCo

**阈值设置**:
```gradle
// build.gradle.kts
coverage {
    minViewModelCoverage = 1.0      // 100%
    minRepositoryCoverage = 1.0     // 100%
    minOverallCoverage = 0.9        // 90%
}
```

#### 2.3 测试命名规范

```kotlin
// 格式: [被测方法]_[场景]_[预期结果]

// ✅ 正确示例
@Test
fun `saveInspiration_withValidData_insertsIntoDatabase()`() { ... }

@Test
fun `exportInspiration_whenSingleCard_generatesCorrectMarkdown()`() { ... }

@Test
fun `searchInspiration_withKeyword_filtersResults()`() { ... }

// ❌ 错误示例
@Test
fun testSave() { ... }                    // 不明场景

@Test
fun `save`() { ... }                      // 没有场景和预期

@Test
fun `save_shouldWork()`() { ... }         // 预期不明确
```

#### 2.4 测试结构规范

所有测试必须遵循AAA模式（Arrange-Act-Assert）：

```kotlin
@Test
fun `exportInspiration_whenMultipleCards_generatesCorrectFormat()`() {
    // Arrange - 准备测试数据
    val inspirations = listOf(
        Inspiration(content = "First idea", themeName = "Tech"),
        Inspiration(content = "Second idea", themeName = "Design")
    )
    
    // Act - 执行被测操作
    val result = ExportManager.exportBatch(inspirations)
    
    // Assert - 验证结果
    assertThat(result).contains("# Tech")
    assertThat(result).contains("First idea")
    assertThat(result).contains("Second idea")
    assertThat(result.lines().size).isGreaterThan(10)
}
```

#### 2.5 测试禁止事项

❌ **绝对禁止**: 不编写测试直接提交功能代码

❌ **绝对禁止**: 测试通过前提交代码

❌ **绝对禁止**: 降低测试覆盖率阈值

❌ **绝对禁止**: 注释掉或删除失败的测试

✅ **出现问题时**: 修复代码或改进测试

#### 2.6 测试工具标准

必须使用的测试库：

```gradle
dependencies {
    // Unit Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")  // Flow测试
    testImplementation("com.google.truth:truth:1.3.0")    // 断言库
    
    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

**测试命令**: `./gradlew test` 和 `./gradlew connectedAndroidTest`

---

### 3. 构建与运行验证规范

**核心原则**: 每次代码修改必须保证项目能够成功编译并正常运行。

#### 3.1 强制验证要求

在任何代码提交（包括工作进度保存）之前，必须完成以下验证：

**必须执行的验证命令**:
```bash
# 1. 完整编译检查（必须）
./gradlew build

# 2. 运行所有单元测试（必须）
./gradlew test

# 3. 运行代码质量检查（必须）
./gradlew ktlintCheck

# 4. 快速安装测试（推荐）
./gradlew installDebug  # 验证应用能否安装到设备
```

**验证结果要求**:
- ✅ 编译必须0错误（ERROR）
- ✅ 编译必须0警告（WARNING）
- ✅ 所有测试必须100%通过
- ✅ 代码检查必须通过
- ✅ 应用必须能正常启动到主界面

#### 3.2 禁止的行为

❌ **绝对禁止**: 提交会导致编译失败的代码到任何分支

❌ **绝对禁止**: 提交会导致测试失败的代码到任何分支

❌ **绝对禁止**: 提交后有警告或错误的代码（警告即错误原则）

❌ **绝对禁止**: "我本地可以运行"作为违反此规则的借口

❌ **绝对禁止**: 临时注释掉测试或代码来通过验证

✅ **正确做法**: 在提交前完整执行验证流程，确保构建健康

#### 3.3 IDE实时检查

**在Android Studio中必须启用**:
1. **实时编译检查**: `Settings > Build, Execution, Deployment > Compiler > Build project automatically`
2. **Kotlin错误高亮**: 确保IDE正确配置Kotlin插件
3. **Lint检查**: `Analyze > Run Inspection by Name` 定期运行

**开发过程中的持续验证**:
- 每次修改后观察IDE是否有红色错误提示
- 每次保存后等待IDE后台编译完成
- 立即修复任何编译错误，不允许错误累积

#### 3.4 持续集成验证

**CI/CD流水线强制要求**（见3.3节工具配置）:
```yaml
# GitHub Actions 示例
- name: Build Check
  run: ./gradlew build --warning-mode all
  
- name: Test Check
  run: ./gradlew test
  
- name: Lint Check
  run: ./gradlew ktlintCheck
```

**CI失败处理**:
1. 立即修复导致CI失败的问题
2. 不得以任何理由合并CI失败的PR
3. 主分支（main/develop）CI失败是最高优先级问题

#### 3.5 特殊情况处理

**无法立即修复的构建问题**:

如果发现构建问题但无法立即解决：

1. **停止其他开发工作**: 构建健康是第一优先级
2. **创建紧急修复Issue**: 标题格式 `[URGENT] Build failure: [问题描述]`
3. **通知团队成员**: 在团队频道广播构建问题
4. **回退到最后稳定版本**: 如有必要，回退代码到上次成功构建
5. **24小时修复原则**: 严重构建问题必须在24小时内解决

**技术债务影响构建**:

如果技术债务导致构建问题，必须在PR中：
- 明确说明技术债务影响
- 提供技术债务偿还计划
- 获得架构师级别批准后合并

---

### 4. 代码注释与审查规范

**核心原则**: 代码自解释，注释解释"为什么"而非"是什么"。

#### 4.1 注释语言要求

**强制规定**: 所有代码注释必须使用英文。

✅ **正确示例**:
```kotlin
/**
 * Exports a single inspiration card to markdown format.
 * 
 * The exported markdown includes YAML front matter with all metadata
 * followed by the inspiration content. This format is compatible
 * with most markdown processors.
 * 
 * @param inspiration The inspiration entity to export
 * @return String containing the markdown formatted content
 * 
 * @see Inspiration
 * @see ExportManager.exportBatch
 */
fun exportSingle(inspiration: Inspiration): String {
    // Build YAML front matter for metadata
    val metadata = buildString {
        appendLine("---")
        appendLine("theme: ${inspiration.themeName}")
        appendLine("createdAt: ${formatDate(inspiration.createdAt)}")
        appendLine("---")
    }
    
    // Combine metadata with content
    return metadata + inspiration.content
}
```

❌ **错误示例**:
```kotlin
// 导出单卡
// 这里生成markdown格式
fun exportSingle(inspiration: Inspiration): String {
    // 构建元数据
    val metadata = ...
    return metadata + inspiration.content
}
```

#### 4.2 必须注释的场景

必须添加注释（文档注释 `/** */`）的地方：

1. **公共API**: 所有`public`/`internal`的类、接口、函数、属性
2. **复杂业务逻辑**: 算法、状态机、工作流等
3. **特殊处理**: 为什么使用这种方案而不是常规方案
4. **风险点**: 可能导致性能问题或bug的地方
5. **TODO**: 临时解决方案，需要后续改进

```kotlin
/**
 * ViewModel for managing inspiration list and search.
 * 
 * This ViewModel uses StateFlow to expose UI state for Compose binding.
 * It handles user interactions from the UI layer and coordinates with
 * the repository layer for data operations.
 * 
 * State management:
 * - Holds current list of inspirations
 * - Manages search queries and filters
 * - Tracks loading and error states
 * 
 * TODO: Consider adding pagination when dataset grows beyond 500 items
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: InspirationRepository
) : ViewModel() {
    // Implementation
}
```

#### 4.3 代码自解释原则

尽量通过代码本身表达意图：

```kotlin
// ❌ 不好的代码 - 需要注释说明
// Calculate tax amount
val t = p * 0.08

// ✅ 好的代码 - 自解释
val taxAmount = price * TAX_RATE
```

#### 4.4 Plan模式代码更改

**核心原则**: 所有代码更改必须经过Plan-Review-Execute流程。

##### 4.4.1 Plan模式工作流程

```
识别问题/需求 → 编写修改计划 → 人工审查 → 批准 → 执行修改 → 提交代码
```

##### 4.4.2 修改计划格式

**Plan模式必须包含以下内容**：

```markdown
### 修改计划: [简要描述]

**相关文件**: 
- `app/data/repository/InspirationRepository.kt`
- `app/data/database/dao/InspirationDao.kt`

**修改原因**:
当前搜索功能只支持精确匹配，用户反馈需要模糊搜索以提高查找效率。

**具体修改内容**:
1. 在InspirationDao中修改search()方法的SQL查询：
   - 从: `WHERE content = :keyword`
   - 到: `WHERE content LIKE '%' || :keyword || '%'`

2. 添加数据库索引优化搜索性能：
   ```sql
   CREATE INDEX idx_content ON inspirations(content)
   ```

3. 更新Repository接口文档说明模糊搜索行为

**影响范围**:
- 搜索功能逻辑
- 数据库性能（需要测试）

**测试计划**:
- 添加测试用例`search_withPartialKeyword_returnsMatchingResults()`
- 验证性能：在1000条数据下搜索时间<100ms

**风险评估**:
- 低风险：只是查询方式修改，数据无变更
- 性能风险：可能的全表扫描，已通过索引缓解

**预计工作量**: 2小时

**审批状态**: ⬜ 待审批 | ✅ 已批准

**审批人**: _____________
```

##### 4.4.3 审批流程

1. **编写修改计划**: 在代码仓库中创建Issue或PR描述
2. **提交审查**: @项目负责人进行审查
3. **审查反馈**: 审查人提出意见或批准
4. **修改完善**: 根据反馈调整计划
5. **明确批准**: 审查人回复"计划批准，可以执行"
6. **执行修改**: 严格按照批准的计划修改代码
7. **对比检查**: 修改完成后与计划对比，确保无遗漏
8. **提交代码**: 提交信息中引用Issue/PR编号

##### 4.4.4 禁止的行为

❌ **绝对禁止**: 未经Plan和批准直接修改代码

❌ **绝对禁止**: 批准的Plan与实际修改不一致

❌ **绝对禁止**: 口头批准后修改，无书面记录

❌ **绝对禁止**: Plan批准后超过7天未执行（需重新审批）

✅ **正确做法**: 所有修改都有书面Plan和批准记录

##### 4.4.5 使用工具支持

**推荐工具**: GitHub Issues / Pull Requests

**Issue模板**:
```markdown
Title: [Plan] 添加模糊搜索支持

## 修改计划

**修改原因**: ...

**具体修改**: ...

**测试计划**: ...

**审批状态**: 待审批
```

**批准标记**: 在Issue评论中使用`✅ Approved`标签

---

## 🎯 代码质量标准

### 1. Kotlin编码规范

遵循[Kotlin官方编码规范](https://kotlinlang.org/docs/coding-conventions.html)和以下补充规则：

#### 1.1 命名规范

```kotlin
// 类和接口: PascalCase
class InspirationViewModel
interface InspirationRepository

// 函数和属性: camelCase
fun saveInspiration()
val inspirationList

// 常量: UPPER_SNAKE_CASE
const val MAX_CONTENT_LENGTH = 500

// 私有成员: 下划线前缀 + camelCase
private var _inspirationFlow

// Compose函数: PascalCase + 描述性后缀
@Composable
fun InspirationCardItem()

// Test函数: 描述性语句 + 下划线分隔
@Test
fun `saveInspiration_withValidData_insertsIntoDatabase()`()
```

#### 1.2 代码格式化

使用`ktlint`进行自动格式化：

```bash
# 检查格式
./gradlew ktlintCheck

# 自动修复
./gradlew ktlintFormat
```

**必须配置的规则**:
- 缩进：4个空格
- 最大行宽：100字符
- 导入排序：按字母顺序
- 无通配符导入

#### 1.3 函数规范

```kotlin
// ✅ 好的函数 - 单一职责，长度适中
fun validateInspirationContent(content: String): ValidationResult {
    return when {
        content.isBlank() -> ValidationResult.Empty
        content.length > MAX_CONTENT_LENGTH -> ValidationResult.TooLong
        else -> ValidationResult.Valid
    }
}

// ❌ 不好的函数 - 过长，职责过多
fun handleInspirationSave(content: String, theme: String, context: Context, 
                         repository: InspirationRepository, analytics: Analytics) {
    // 100行代码...
}
```

**函数长度限制**: 单个函数不超过30行（不包括注释）

### 2. Compose规范

#### 2.1 Composable函数规范

```kotlin
// ✅ 好的Composable
@Composable
fun InspirationCard(
    inspiration: Inspiration,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Implementation
}

// ❌ 不好的Composable - 参数过多，职责不明确
@Composable
fun Card() { ... }
```

**规则**：
- Composable函数必须包含可读的描述性名称
- `modifier`参数必须是可选的，且有默认值`Modifier`
- 状态提升：尽可能将状态提升到调用者
- 使用`remember`缓存昂贵计算

#### 2.2 State管理

```kotlin
// ✅ 推荐：使用StateFlow和ViewModel
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (val state = uiState) {
        is MainUiState.Loading -> LoadingScreen()
        is MainUiState.Success -> InspirationList(state.inspirations)
        is MainUiState.Error -> ErrorScreen(state.message)
    }
}

// ❌ 避免：在Composable中直接管理状态
@Composable
fun BadExample() {
    var inspirations by remember { mutableStateOf(emptyList<Inspiration>()) }
    // 直接在这里加载数据...
}
```

### 3. 依赖注入规范

使用Hilt进行依赖注入：

```kotlin
// 模块定义
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InspirationDatabase {
        return Room.databaseBuilder(
            context,
            InspirationDatabase::class.java,
            "inspiration.db"
        ).build()
    }
}

// ViewModel注入
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: InspirationRepository
) : ViewModel() { ... }

// UI注入
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) { ... }
```

**禁止**: 手动创建Repository或Database实例

### 4. 异常处理

```kotlin
// ✅ 好的异常处理
class InspirationRepository @Inject constructor(
    private val dao: InspirationDao
) {
    suspend fun saveInspiration(inspiration: Inspiration): Result<Unit> {
        return try {
            dao.insert(inspiration.toEntity())
            Result.success(Unit)
        } catch (e: SQLiteException) {
            Log.e(TAG, "Failed to save inspiration: ${e.message}", e)
            Result.failure(DatabaseException("Failed to save inspiration", e))
        }
    }
}

// ❌ 不好的异常处理 - 吞掉异常
suspend fun saveInspiration(inspiration: Inspiration) {
    try {
        dao.insert(inspiration.toEntity())
    } catch (e: Exception) {
        // 什么也不做！
    }
}
```

**规则**: 使用`Result<T>`或自定义密封类返回结果，不抛出异常

---

## 📦 提交规范

### 1. Git提交信息格式

采用[Conventional Commits](https://www.conventionalcommits.org/)规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type**: 
- `feat`: 新功能
- `fix`: Bug修复
- `test`: 测试相关
- `refactor`: 重构（不改变功能）
- `docs`: 文档
- `style`: 格式调整
- `perf`: 性能优化
- `chore`: 构建/工具

**Scope**: 影响的模块（如: `database`, `ui`, `repository`）

**示例**:
```
feat(ui): add quick record section

Add a new UI section for quickly recording inspirations.
Includes a text field, theme selector, and save button.

Closes #12
```

```
test(repository): add search functionality tests

Add unit tests for the new searchInspiration method.
Cover exact match, partial match, and empty result scenarios.
```

```
fix(database): resolve migration issue

Fix crash when migrating from version 1 to 2.
Add proper fallback for missing columns.

BREAKING CHANGE: Changes database schema
```

### 2. 提交前检查清单

提交代码前必须完成：

**构建与运行验证（强制）**:
- [ ] 完整编译通过：`./gradlew build`（0错误，0警告）
- [ ] 所有单元测试通过：`./gradlew test`（100%通过）
- [ ] 代码格式检查通过：`./gradlew ktlintCheck`
- [ ] 安装测试通过：`./gradlew installDebug`（应用能正常启动）

**代码质量验证**:
- [ ] 测试覆盖率达标：`./gradlew jacocoTestReport`（≥90%）
- [ ] 提交信息符合[Conventional Commits](https://www.conventionalcommits.org/)规范
- [ ] 相关联Issue编号在提交消息中
- [ ] 代码自审完成，符合本文档所有规范

**文档检查**:
- [ ] 公共API添加了英文文档注释（如果需要）
- [ ] 复杂业务逻辑添加了"为什么"的注释
- [ ] Plan模式修改有书面记录和批准（如果需要）

**紧急情况**:
- [ ] 如果是紧急修复，在提交消息中标注`HOTFIX`

### 3. 分支管理

```
main (保护分支)
├── develop (开发分支)
│   ├── feature/quick-record
│   ├── feature/search
│   └── feature/export
├── release/v1.0.0
└── hotfix/crash-on-startup
```

**规则**:
- `main`分支受保护，只能接受PR
- `develop`是主开发分支
- Feature分支：feature/描述性名称
- Release分支：release/v版本号
- Hotfix分支：hotfix/问题描述

---

## 🔍 代码审查清单

### Pull Request要求

PR描述模板：

```markdown
## 变更内容

Brief description of changes...

## 测试覆盖

- [ ] Unit tests added/updated
- [ ] UI tests added/updated
- [ ] Manual testing completed
- [ ] Test coverage ≥ 90%

## 检查清单

- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Code is commented in English
- [ ] No debug code left
- [ ] No sensitive data exposed

## 关联Issue

Closes #123

## 截图（如适用）

Add screenshots for UI changes.
```

### 审查重点

审查人必须检查：

1. **功能正确性**: 代码是否实现了预期功能
2. **测试质量**: 测试是否全面，是否覆盖边界情况
3. **代码质量**: 是否符合本规范
4. **性能**: 是否有潜在的性能问题
5. **安全性**: 是否有安全漏洞
6. **可读性**: 代码是否易于理解

### 审查反馈

- 使用**Comment**: 一般建议，可选修改
- 使用**Request Changes**: 必须修改的问题
- 使用**Approve**: 批准合并

---

## 🛠️ 工具配置

### 1. IDE设置

**Android Studio配置**:

1. 安装插件:
   - ktlint
   - .gitignore
   - Rainbow Brackets

2. Code Style配置:
   ```xml
   <!-- .idea/codeStyles/Project.xml -->
   <option name="RIGHT_MARGIN" value="100" />
   <option name="WRAP_WHEN_TYPING_REACHES_RIGHT_MARGIN" value="true" />
   ```

### 2. 预提交钩子

在`.git/hooks/pre-commit`中配置：

```bash
#!/bin/sh
# Run tests before commit
./gradlew test ktlintCheck

# Check if tests passed
if [ $? -ne 0 ]; then
    echo "Tests or linting failed. Commit aborted."
    exit 1
fi
```

### 3. CI/CD配置

**GitHub Actions工作流（建议）**:

```yaml
name: Android CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Run Tests
        run: ./gradlew test
      
      - name: Check Coverage
        run: ./gradlew jacocoTestReport
      
      - name: Lint Check
        run: ./gradlew ktlintCheck
```

---

## 📏 度量指标

### 代码质量指标

| 指标 | 目标值 | 监控工具 |
|-----|-------|---------|
| 测试覆盖率 | ≥ 90% | JaCoCo |
| 代码重复率 | ≤ 3% | Detekt |
| 复杂度/方法 | ≤ 10 | Detekt |
| 警告数量 | 0 | Android Lint |
| 技术债务 | 无阻塞问题 | SonarQube |

### 开发效率指标

- **PR Review时间**: < 4小时
- **CI构建时间**: < 10分钟
- **测试执行时间**: < 3分钟
- **代码行/PR**: < 300行（不含测试）

---

## 📚 附录

### A. 常用术语表

| 英文术语 | 中文解释 | 使用场景 |
|---------|---------|---------|
| Inspiration | 灵感 | 核心数据实体 |
| Theme | 主题 | 灵感分类 |
| Repository | 仓库 | 数据层抽象 |
| Dao | 数据访问对象 | Room数据库操作 |
| ViewModel | 视图模型 | UI状态管理 |
| Composable | 可组合函数 | Compose UI组件 |
| TDD | 测试驱动开发 | 开发方法论 |
| Plan Mode | 计划模式 | 代码修改流程 |

### B. 参考资源

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Best Practices](https://developer.android.com/jetpack/compose/best-practices)
- [TDD Guide](https://developer.android.com/training/testing/fundamentals)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Hilt Documentation](https://dagger.dev/hilt/)

---

## 📜 变更历史

| 版本 | 日期 | 作者 | 变更描述 |
|-----|------|-----|---------|
| 1.1 | 2024-12-19 | Kimi CLI | 新增：构建与运行验证规范（第3条），每次修改必须保证编译通过并正常运行 |
| 1.0 | 2024-01-03 | Kimi CLI | 初始版本，强制执行规则 |

---

**文档结束**

**重要**: 本文档的任何修改都必须经过原始批准流程，并获得所有相关方同意。
