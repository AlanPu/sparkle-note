feat(utils): add undo delete functionality and empty state UI

**Added Features:**
- Undo delete functionality with DeletionCache (5-minute timeout)
- MainViewModel integration for undo operations
- EmptyState UI component for no-data scenarios
- ThemeViewModel foundation (WIP)

**Modified Files:**
- Added: app/src/main/java/com/sparkle/note/utils/DeletionCache.kt
- Added: app/src/main/java/com/sparkle/note/ui/components/EmptyState.kt
- Added: app/src/main/java/com/sparkle/note/ui/screens/themes/ThemeViewModel.kt
- Modified: app/src/main/java/com/sparkle/note/ui/screens/main/MainViewModel.kt
- Modified: app/src/main/java/com/sparkle/note/domain/repository/InspirationRepository.kt
- Modified: app/src/main/java/com/sparkle/note/data/repository/InspirationRepositoryImpl.kt
- Modified: app/src/main/java/com/sparkle/note/data/repository/MockInspirationRepository.kt
- Modified: app/src/main/java/com/sparkle/note/data/database/dao/InspirationDao.kt

**Technical Details:**
- DeletionCache uses ConcurrentHashMap for thread-safe storage
- Supports 5-minute undo window for deleted inspirations
- MainEvent.ShowDeleteSuccess includes deleted item data for undo
- EmptyState shows friendly message and creation CTA

**Testing:**
- ThemeViewModel tests need completion (unresolved coroutine timing issues)
- DeletionCache ready for manual testing
- EmptyState component ready for UI integration

Next Steps:
- Complete ThemeViewModel tests
- Implement ThemeManagementScreen UI
- Add time filter functionality
- Integrate EmptyState into main screen

Part of Day 2: Core Features milestone
