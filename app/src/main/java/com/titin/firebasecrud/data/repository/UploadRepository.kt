package com.titin.firebasecrud.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.titin.firebasecrud.data.source.FirebaseDataSource
import com.titin.firebasecrud.domain.model.Upload
import com.titin.firebasecrud.util.Resource
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

class UploadRepository(private val dataSource: FirebaseDataSource) {

    // Initialize the databaseRef property by getting it from the dataSource
    private val databaseRef: DatabaseReference = dataSource.getDatabaseReference()

    fun getUploads(): Flow<Resource<List<Upload>>> {
        return dataSource.observeUploads()
    }

    suspend fun saveUpload(upload: Upload): Resource<String> {
        return dataSource.saveUpload(upload)
    }

    suspend fun deleteUpload(upload: Upload): Resource<Unit> {
        return dataSource.deleteUpload(upload)
    }

    suspend fun uploadImage(imageUri: Uri, contentResolver: ContentResolver): Resource<Uri> {
        val contentType = contentResolver.getType(imageUri)
        return dataSource.uploadImage(imageUri, contentType)
    }

    suspend fun updateUploadWithNewImage(
        upload: Upload,
        imageUri: Uri,
        contentResolver: ContentResolver
    ): Resource<Unit> {
        val contentType = contentResolver.getType(imageUri)
        return dataSource.updateUploadWithNewImage(upload, imageUri, contentType)
    }

    suspend fun updateUploadWithoutNewImage(upload: Upload): Resource<Unit> {
        return try {
            // Asegúrate de que estamos usando una copia para no modificar el original accidentalmente
            val uploadCopy = upload.copy()

            uploadCopy.key?.let { key ->
                // No modificar la imgUrl aquí
                databaseRef.child(key).setValue(uploadCopy).await()
                Resource.Success(Unit)
            } ?: throw IOException("Upload key is null")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error updating upload")
        }
    }
}