package com.example.meinstundenzhler.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import java.util.Locale

val Context.dataStore by preferencesDataStore("settings")

object Keys {
    val THEME = stringPreferencesKey("theme") // system | light | dark
    val DEFAULT_WAGE = doublePreferencesKey("default_wage")
    val DEFAULT_BREAK = intPreferencesKey("default_break")
    val ROUNDING = intPreferencesKey("rounding_minutes")
    val PDF_OPEN_DIRECT = booleanPreferencesKey("pdf_open_direct")
    val SHOW_TNUM = booleanPreferencesKey("show_tnum")
}

data class SettingsState(
    val theme: String,
    val defaultWage: Double,
    val defaultBreak: Int,
    val rounding: Int,
    val pdfOpenDirect: Boolean,
    val showTnum: Boolean
)

class SettingsRepo(private val ctx: Context) {

    // Mappe alte Werte ("Hell"/"Dunkel") auf die neuen Keys
    val flow = ctx.dataStore.data.map { p ->
        val raw = (p[Keys.THEME] ?: "system").lowercase(Locale.ROOT)
        val themeKey = when (raw) {
            "dark", "dunkel" -> "dark"
            "light", "hell"  -> "light"
            "system"         -> "system"
            else             -> "system"
        }

        SettingsState(
            theme = themeKey,
            defaultWage   = p[Keys.DEFAULT_WAGE] ?: 14.0,
            defaultBreak  = p[Keys.DEFAULT_BREAK] ?: 0,
            rounding      = p[Keys.ROUNDING] ?: 0,
            pdfOpenDirect = p[Keys.PDF_OPEN_DIRECT] ?: true,
            showTnum      = p[Keys.SHOW_TNUM] ?: true
        )
    }

    suspend fun setTheme(v: String) = ctx.dataStore.edit { it[Keys.THEME] = v }
    // weitere Setter kannst du bei Bedarf ergänzen
}
