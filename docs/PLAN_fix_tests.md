### 修改计划：修复MockRepository测试中ID分配问题

**相关文件**:
- `app/src/main/java/com/sparkle/note/data/repository/MockInspirationRepository.kt`
- `app/src/test/java/com/sparkle/note/data/repository/MockInspirationRepositoryTest.kt`

**修改原因**:
测试 `deleteInspiration_removesCorrectData()` 和 `saveInspiration_assignsUniqueIds()` 失败，原因是MockRepository使用`System.currentTimeMillis()`作为ID生成策略，可能导致在同一毫秒内生成的ID重复，违反了ID唯一性的要求。

**具体修改内容**:
1. 在MockInspirationRepository中添加原子计数器（AtomicLong）替代时间戳
2. 使用递增的Long值作为ID，确保唯一性
3. 保持所有公共API不变，不影响其他代码

**修改代码**:
```kotlin
// 在类中添加：
private val idCounter = java.util.concurrent.atomic.AtomicLong(1L)

// 修改saveInspiration方法：
val newInspiration = if (inspiration.id == 0L) {
    inspiration.copy(id = idCounter.incrementAndGet())
} else {
    inspiration
}
```

**影响范围**:
- 仅MockInspirationRepository内部实现
- 修复测试用例，提高测试可靠性
- 产品代码不受影响（产品使用InspirationRepositoryImpl）

**测试计划**:
- 运行原有测试用例，验证全部通过
- 特别验证ID唯一性相关的测试

**风险评估**:
- 极低风险：仅是测试工具类的实现改进，不影响生产代码
- 修复后测试更稳定，避免偶然失败

**预计工作量**: 15分钟

**审批状态**: ⬜ 待审批 | ✅ 已批准

**审批人**: _____________
