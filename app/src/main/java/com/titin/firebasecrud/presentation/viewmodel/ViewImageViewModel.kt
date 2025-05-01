package com.titin.firebasecrud.presentation.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titin.firebasecrud.data.repository.UploadRepository
import com.titin.firebasecrud.domain.model.Upload
import com.titin.firebasecrud.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ViewImageViewModel(private val repository: UploadRepository) : ViewModel() {
    private val uploadsMutable = MutableLiveData<Resource<List<Upload>>>()
    val uploads: LiveData<Resource<List<Upload>>> = uploadsMutable

    private val currentOperationMutable = MutableLiveData<Resource<Unit>>()
    val currentOperation: LiveData<Resource<Unit>> = currentOperationMutable

    private val editImageUriMutable = MutableLiveData<Uri?>(null)
    private val currentUploadMutable = MutableLiveData<Upload?>(null)

    init {
        loadUploads()
    }

    private fun loadUploads() {
        viewModelScope.launch {
            uploadsMutable.value = Resource.Loading()
            try {
                repository.getUploads().collectLatest { result ->
                    uploadsMutable.value = result
                }
            } catch (e: Exception) {
                uploadsMutable.value = Resource.Error("Error al cargar imágenes: ${e.message}")
            }
        }
    }

    fun setEditImageUri(uri: Uri?) {
        editImageUriMutable.value = uri
    }

    fun prepareForEdit(upload: Upload) {
        // Establece el upload actual que se está editando
        currentUploadMutable.value = upload
        // Establecer explícitamente el URI de edición en nulo al iniciar la edición
        editImageUriMutable.value = null
    }

    fun setCurrentUpload(upload: Upload?) {
        currentUploadMutable.value = upload
    }

    fun deleteUpload(upload: Upload) {
        viewModelScope.launch {
            currentOperationMutable.value = Resource.Loading()
            try {
                val result = repository.deleteUpload(upload)
                currentOperationMutable.value = result

                // No es necesario actualizar la lista local manualmente
                // ya que está observando la fuente de datos en tiempo real
            } catch (e: Exception) {
                currentOperationMutable.value = Resource.Error("Error al eliminar: ${e.message}")
            }
        }
    }

    fun updateUpload(
        name: String,
        author: String,
        date: String,
        contentResolver: ContentResolver
    ) {
        val upload = currentUploadMutable.value?.copy() ?: run {
            currentOperationMutable.value = Resource.Error("No hay elemento seleccionado para editar")
            return
        }

        val editImageUri = editImageUriMutable.value

        // Validar datos
        if (name.isEmpty()) {
            currentOperationMutable.value = Resource.Error("El nombre no puede estar vacío")
            return
        }

        // Actualizar el modelo
        upload.imgName = name
        upload.author = author
        upload.creationDate = date

        viewModelScope.launch {
            currentOperationMutable.value = Resource.Loading()

            try {
                // Realizar la operación según corresponda
                val result = if (editImageUri != null) {
                    repository.updateUploadWithNewImage(upload, editImageUri, contentResolver)
                } else {
                    repository.updateUploadWithoutNewImage(upload)
                }

                currentOperationMutable.value = result

                // Limpiar el estado de edición
                editImageUriMutable.value = null
                currentUploadMutable.value = null
            } catch (e: Exception) {
                currentOperationMutable.value = Resource.Error("Error al actualizar: ${e.message}")
            }
        }
    }
}