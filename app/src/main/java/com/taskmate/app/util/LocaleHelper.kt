package com.taskmate.app.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Помага при менување на јазикот на апликацијата во рантајм.
 * Изборот се чува во SharedPreferences и се применува преку
 * attachBaseContext() во секоја Activity.
 */
object LocaleHelper {

    fun getPersistedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(Constants.KEY_LANGUAGE, Constants.LANG_MK) ?: Constants.LANG_MK
    }

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(Constants.KEY_LANGUAGE, language)
            .apply()
    }

    /** Враќа Context со применет избран јазик (се повикува од attachBaseContext). */
    fun applyLanguage(context: Context): Context {
        val language = getPersistedLanguage(context)
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
