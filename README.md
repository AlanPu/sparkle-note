# ✨ Sparkle Note

一款现代化的灵感记录应用，帮助您快速捕捉和管理日常灵感。

## 🌟 主要功能

### 📝 灵感记录
- 快速输入和保存灵感
- 支持多主题分类管理
- 实时字符计数和验证
- 优雅的卡片式展示

### 🎨 主题管理
- 4种精美内置主题（北欧风格、深邃夜空、薄荷晨露、学院蓝调）
- 支持自定义主题创建
- 深色/浅色模式自动切换
- 主题使用统计和排序

### 🔍 智能搜索
- 多关键词模糊搜索
- 多主题同时筛选
- 时间范围过滤（今天/本周/本月/全部）
- 搜索历史记录

### 📦 批量操作
- 多选模式批量管理
- 批量导出（Markdown/JSON/CSV格式）
- 批量删除功能
- 操作撤销支持

### 💾 数据备份
- JSON格式完整数据备份
- Markdown格式优雅导出
- 版本控制和元数据管理
- 文件分享和存储

## 🏗️ 技术架构

### 核心技术栈
- **Kotlin 1.9.23** - 现代化开发语言
- **Jetpack Compose** - 声明式UI框架
- **Material Design 3** - 最新设计系统
- **Room Database** - SQLite对象映射
- **Hilt** - 依赖注入框架
- **Kotlin Coroutines + Flow** - 响应式编程

### 架构模式
- **Clean Architecture** - 清晰的分层架构
- **MVVM** - Model-View-ViewModel模式
- **Repository Pattern** - 数据访问抽象
- **单向数据流** - 响应式状态管理

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog 或更高版本
- JDK 17
- Android SDK 34
- 最低支持 Android 7.0 (API 24)

### 构建项目
```bash
# 克隆项目
git clone https://github.com/your-username/sparkle-note.git

# 进入项目目录
cd sparkle-note

# 构建调试版本
./gradlew assembleDebug

# 构建发布版本
./gradlew assembleRelease
```

### 运行测试
```bash
# 运行单元测试
./gradlew test

# 运行UI测试
./gradlew connectedAndroidTest
```

## 📱 界面预览

### 主界面
- 底部输入设计，符合移动设备使用习惯
- 卡片式灵感展示，支持长按操作
- 实时主题切换，深色模式支持

### 高级搜索
- 多维度筛选和搜索
- 直观的筛选条件展示
- 搜索结果实时更新

### 主题管理
- 主题列表和统计信息
- 主题创建和编辑功能
- 主题使用频率排序

## 🎯 开发规范

### 代码规范
- 遵循 Kotlin 编码规范
- 使用 Conventional Commits 提交规范
- 强制代码审查和单元测试
- 代码覆盖率目标 ≥ 90%

### 架构原则
- 严格的分层架构，每层职责清晰
- 依赖倒置，面向接口编程
- 响应式编程，状态管理统一
- 错误处理和用户反馈完善

## 🤝 贡献指南

1. Fork 项目仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'feat: 添加新功能'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

### 提交规范
```
feat: 添加新功能
fix: 修复错误
docs: 更新文档
style: 代码格式调整
refactor: 代码重构
test: 添加测试
chore: 构建过程或辅助工具变动
```

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

---

**✨ 让灵感如火花般闪耀，让记录如呼吸般自然！**