package com.taskmate.app.ui

import androidx.appcompat.app.AppCompatActivity

/**
 * Заедничка база за сите Activity. Јазикот се управува преку
 * AppCompatDelegate.setApplicationLocales (per-app locales), па тука
 * нема потреба од рачно менување на конфигурацијата.
 */
abstract class BaseActivity : AppCompatActivity()