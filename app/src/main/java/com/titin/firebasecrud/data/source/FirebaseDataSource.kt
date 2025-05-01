package com.titin.firebasecrud.data.source

import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.titin.firebasecrud.domain.model.Upload
import com.titin.firebasecrud.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException

class FirebaseDataSource {
    private val storageRef: StorageReference = FirebaseStorage.getInstance().getReference("uploads")
    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("uploads")

    // Añadir esto a FirebaseDataSource
    fun getDatabaseReference(): DatabaseReference {
        return databaseRef
    }

    fun getFileExtension(contentType: String?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType)
    }

    suspend fun uploadImage(imageUri: Uri, contentType: String?): Resource<Uri> {
        return try {
            val fileReference = storageRef.child(
                "${System.currentTimeMillis()}.${getFileExtension(contentType)}"
            )

            // Primero subimos el archivo
            fileReference.putFile(imageUri).await()

            // Luego obtenemos la URL de descarga
            val downloadUri = fileReference.downloadUrl.await()
            Resource.Success(downloadUri)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error during upload")
        }
    }

    fun observeUploads(): Flow<Resource<List<Upload>>> = callbackFlow {
        // Primero enviamos un estado de carga
        trySend(Resource.Loading())

        val uploadsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val uploads = mutableListOf<Upload>()
                for (postSnapshot in snapshot.children) {
                    val upload = postSnapshot.getValue(Upload::class.java)
                    upload?.let {
                        it.key = postSnapshot.key
                        uploads.add(it)
                    }
                }
                // Ordenar por fecha más reciente si es necesario
                // uploads.sortByDescending { it.creationDate }
                trySend(Resource.Success(uploads))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        }

        // Usar addValueEventListener en lugar de addListenerForSingleValueEvent
        // para recibir actualizaciones continuas
        databaseRef.addValueEventListener(uploadsListener)

        awaitClose {
            databaseRef.removeEventListener(uploadsListener)
        }
    }

    suspend fun saveUpload(upload: Upload): Resource<String> {
        return try {
            val key = upload.key ?: databaseRef.push().key ?:
            throw IOException("Failed to generate database key")

            upload.key = key
            databaseRef.child(key).setValue(upload).await()
            Resource.Success(key)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error saving upload")
        }
    }

    suspend fun deleteUpload(upload: Upload): Resource<Unit> {
        return try {
            // Verifica si la URL es válida antes de intentar eliminar
            if (upload.imgUrl.isNotEmpty()) {
                try {
                    // Delete from storage
                    val imageRef = storageRef.storage.getReferenceFromUrl(upload.imgUrl)
                    imageRef.delete().await()
                } catch (e: Exception) {
                    // Si hay error al eliminar la imagen (por ejemplo, si ya no existe),
                    // registramos el error pero continuamos para eliminar el registro de la base de datos
                    Log.w("FirebaseDataSource", "Error deleting image: ${e.message}")
                }
            }

            // Delete from database
            upload.key?.let { key ->
                databaseRef.child(key).removeValue().await()
            } ?: throw IOException("Upload key is null")

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error deleting upload")
        }
    }

    suspend fun updateUploadWithNewImage(
        upload: Upload,
        imageUri: Uri,
        contentType: String?
    ): Resource<Unit> {
        return try {
            // Intenta eliminar la imagen antigua solo si existe una URL válida
            if (upload.imgUrl.isNotEmpty()) {
                try {
                    val oldImageRef = storageRef.storage.getReferenceFromUrl(upload.imgUrl)
                    oldImageRef.delete().await()
                } catch (e: Exception) {
                    // Si hay error al eliminar la imagen antigua (por ejemplo, si ya no existe),
                    // registramos el error pero continuamos con la subida de la nueva
                    Log.w("FirebaseDataSource", "Error deleting old image: ${e.message}")
                }
            }

            // Upload new image
            val fileReference = storageRef.child(
                "${System.currentTimeMillis()}.${getFileExtension(contentType)}"
            )
            fileReference.putFile(imageUri).await()
            val downloadUri = fileReference.downloadUrl.await()

            // Update model with new URL
            upload.imgUrl = downloadUri.toString()

            // Save to database
            upload.key?.let { key ->
                databaseRef.child(key).setValue(upload).await()
            } ?: throw IOException("Upload key is null")

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error updating with new image: ${e.message}")
        }
    }
}