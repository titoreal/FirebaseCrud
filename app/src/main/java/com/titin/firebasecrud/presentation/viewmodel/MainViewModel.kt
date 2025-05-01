package com.titin.firebasecrud.presentation.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titin.firebasecrud.data.repository.UploadRepository
import com.titin.firebasecrud.domain.model.Upload
import com.titin.firebasecrud.util.DateUtil
import com.titin.firebasecrud.util.Resource
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(private val repository: UploadRepository) : ViewModel() {
    private val uploadStatusMutable = MutableLiveData<Resource<Unit>>()
    val uploadStatus: LiveData<Resource<Unit>> = uploadStatusMutable

    private val uploadProgressMutable = MutableLiveData<Int>()
    val uploadProgress: LiveData<Int> = uploadProgressMutable

    private val selectedDateMutable = MutableLiveData<Calendar>(Calendar.getInstance())
    val selectedDate: LiveData<Calendar> = selectedDateMutable

    private val selectedImageUriMutable = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = selectedImageUriMutable

    fun setSelectedDate(calendar: Calendar) {
        selectedDateMutable.value = calendar
    }

    fun setSelectedImageUri(uri: Uri?) {
        selectedImageUriMutable.value = uri
    }

    fun uploadImage(
        description: String,
        author: String,
        contentResolver: ContentResolver
    ) {
        val imageUri = selectedImageUriMutable.value ?: run {
            uploadStatusMutable.value = Resource.Error("No se ha seleccionado una imagen")
            return
        }

        uploadStatusMutable.value = Resource.Loading()

        viewModelScope.launch {
            try {
                // Upload image first to get URL
                when (val imageResult = repository.uploadImage(imageUri, contentResolver)) {
                    is Resource.Success -> {
                        val imageUrl = imageResult.data?.toString() ?: run {
                            uploadStatusMutable.value = Resource.Error("URL de imagen no disponible")
                            return@launch
                        }

                        val dateStr = DateUtil.formatDate(selectedDateMutable.value!!)

                        // Create upload object
                        val upload = Upload(
                            imgName = description.ifEmpty { "Sin nombre" },
                            imgUrl = imageUrl,
                            author = author,
                            creationDate = dateStr
                        )

                        // Save to database
                        when (val saveResult = repository.saveUpload(upload)) {
                            is Resource.Success -> {
                                uploadStatusMutable.value = Resource.Success(Unit)
                            }
                            is Resource.Error -> {
                                uploadStatusMutable.value = Resource.Error(
                                    saveResult.message.ifEmpty { "Error al guardar la información" }
                                )
                            }
                            is Resource.Loading -> {
                                // Ignorar estado de carga anidado
                            }
                        }
                    }
                    is Resource.Error -> {
                        uploadStatusMutable.value = Resource.Error(
                            imageResult.message.ifEmpty { "Error al subir la imagen" }
                        )
                    }
                    is Resource.Loading -> {
                        // Ignorar estado de carga anidado
                    }
                }
            } catch (e: Exception) {
                uploadStatusMutable.value = Resource.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun resetForm() {
        selectedImageUriMutable.value = null
        selectedDateMutable.value = Calendar.getInstance()
        uploadProgressMutable.value = 0
    }
}