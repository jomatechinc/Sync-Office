package com.example.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthHelper {
    private const val TAG = "FirebaseAuthHelper"

    val isFirebaseAvailable: Boolean by lazy {
        try {
            FirebaseAuth.getInstance() != null
        } catch (e: NoClassDefFoundError) {
            Log.w(TAG, "Firebase SDK classes not found. Using Secure Local Auth Mode.", e)
            false
        } catch (e: IllegalStateException) {
            Log.w(TAG, "FirebaseApp is not initialized. Using Secure Local Auth Mode.", e)
            false
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Auth not available. Using Secure Local Auth Mode.", e)
            false
        }
    }

    val auth: FirebaseAuth?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
}
