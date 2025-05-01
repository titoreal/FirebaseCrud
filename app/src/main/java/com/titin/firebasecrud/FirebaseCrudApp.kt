package com.titin.firebasecrud

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.titin.firebasecrud.di.AppContainer

class FirebaseCrudApp : Application() {
    // Lazy init AppContainer
    val appContainer by lazy { AppContainer.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        // Habilitar persistencia offline de Firebase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}