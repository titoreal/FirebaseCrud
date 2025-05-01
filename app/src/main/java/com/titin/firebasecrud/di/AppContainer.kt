package com.titin.firebasecrud.di

import android.annotation.SuppressLint
import android.content.Context
import com.titin.firebasecrud.data.repository.UploadRepository
import com.titin.firebasecrud.data.source.FirebaseDataSource

class AppContainer(private val context: Context) {
    // Fuentes de datos
    val firebaseDataSource = FirebaseDataSource()

    // Repositorios
    val uploadRepository: UploadRepository by lazy {
        UploadRepository(firebaseDataSource)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: AppContainer? = null

        fun getInstance(context: Context): AppContainer {
            return INSTANCE ?: synchronized(this) {
                val instance = AppContainer(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}