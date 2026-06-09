package com.taskmate.app.ui

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.taskmate.app.util.LocaleHelper
import java.util.Locale

abstract class BaseActivity : AppCompatActivity() {

    private var savedBase: Context? = null

    override fun attachBaseContext(newBase: Context) {
        savedBase = newBase
        super.attachBaseContext(newBase)
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        val base = savedBase
        if (overrideConfiguration != null && base != null) {
            val language = LocaleHelper.getPersistedLanguage(base)
            overrideConfiguration.setLocale(Locale(language))
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}