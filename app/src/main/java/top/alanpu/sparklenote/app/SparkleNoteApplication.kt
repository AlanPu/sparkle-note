package top.alanpu.sparklenote.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class for Sparkle Note.
 * Enables Hilt dependency injection throughout the app.
 */
@HiltAndroidApp
class SparkleNoteApplication : Application() {
    
    @Inject
    lateinit var themeManager: top.alanpu.sparklenote.app.ui.theme.ThemeManager
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize theme settings
        applicationScope.launch {
            // Set default theme if not configured
            themeManager.themeStyle.collect { _ ->
                // Theme is already loaded by ThemeManager
            }
        }
    }
}