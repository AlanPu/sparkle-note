package com.sparkle.note

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Sparkle Note.
 * Enables Hilt dependency injection throughout the app.
 */
@HiltAndroidApp
class SparkleNoteApplication : Application()