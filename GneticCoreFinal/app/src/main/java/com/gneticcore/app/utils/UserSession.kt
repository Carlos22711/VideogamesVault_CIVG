package com.gneticcore.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.gneticcore.app.data.entity.UserRole

object UserSession {
    private const val PREFS_NAME = "gneticcore_session"
    private const val KEY_USER_ID      = "user_id"
    private const val KEY_USERNAME     = "username"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_ROLE         = "role"
    private const val NO_USER = -1

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    val isLoggedIn: Boolean get() = prefs.getInt(KEY_USER_ID, NO_USER) != NO_USER
    val userId: Int get() = prefs.getInt(KEY_USER_ID, NO_USER)
    val username: String get() = prefs.getString(KEY_USERNAME, "") ?: ""
    val displayName: String get() = prefs.getString(KEY_DISPLAY_NAME, "") ?: ""
    val role: String get() = prefs.getString(KEY_ROLE, UserRole.USER) ?: UserRole.USER
    val isAdmin: Boolean get() = role == UserRole.ADMIN

    fun login(id: Int, username: String, displayName: String, role: String) {
        prefs.edit()
            .putInt(KEY_USER_ID, id)
            .putString(KEY_USERNAME, username)
            .putString(KEY_DISPLAY_NAME, displayName)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
