package com.taskmate.app.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.taskmate.app.util.LocaleHelper

/**
 * Заедничка база за сите Activity — го применува избраниот јазик
 * пред да се прикаже било кој изглед.
 */
abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase))
    }
}
