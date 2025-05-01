package com.titin.firebasecrud.presentation.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.widget.Toast
import androidx.activity.viewModels
import com.titin.firebasecrud.FirebaseCrudApp
import com.titin.firebasecrud.databinding.ActivityMainBinding
import com.titin.firebasecrud.presentation.viewmodel.MainViewModel
import com.titin.firebasecrud.presentation.viewmodel.MainViewModelFactory
import com.titin.firebasecrud.util.DateUtil
import com.titin.firebasecrud.util.Resource
import com.titin.firebasecrud.util.hideKeyboard
import com.titin.firebasecrud.util.loadImage
import com.titin.firebasecrud.util.showToast
import java.util.Calendar
import com.titin.firebasecrud.di.AppContainer

class MainActivity : BaseActivity<ActivityMainBinding>() {

    companion object {
        private const val CHOOSE_IMAGE = 1
    }

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as FirebaseCrudApp).appContainer.uploadRepository)
    }

    override fun inflateBinding(): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater)

    override fun setupUI() {
        setupClickListeners()
        updateDateDisplay()
    }

    override fun setupObservers() {
        viewModel.selectedDate.observe(this) {
            updateDateDisplay()
        }

        viewModel.selectedImageUri.observe(this) { uri ->
            binding.imgPreview.loadImage(uri?.toString())
        }

        viewModel.uploadProgress.observe(this) { progress ->
            binding.uploadProgress.progress = progress
        }

        viewModel.uploadStatus.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    showToast("Guardado exitoso", Toast.LENGTH_LONG)
                    viewModel.resetForm()
                    binding.imgDescription.setText("")
                    binding.imgAuthor.setText("")
                }
                is Resource.Error -> {
                    showToast(resource.message, Toast.LENGTH_LONG)
                }
                is Resource.Loading -> {
                    showToast("Guardando...", Toast.LENGTH_SHORT)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnDatePicker.setOnClickListener { showDatePickerDialog() }

        binding.viewGallery.setOnClickListener {
            startActivity(Intent(this, ViewImageActivity::class.java))
        }

        binding.btnUploadImage.setOnClickListener {
            if (viewModel.uploadStatus.value is Resource.Loading) {
                showToast("Guardando", Toast.LENGTH_LONG)
            } else {
                uploadImage()
                hideKeyboard()
            }
        }

        binding.chooseImage.setOnClickListener { showFileChoose() }
    }

    private fun showDatePickerDialog() {
        val currentDate = viewModel.selectedDate.value ?: Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                viewModel.setSelectedDate(newDate)
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        val date = viewModel.selectedDate.value ?: Calendar.getInstance()
        binding.tvSelectedDate.text = DateUtil.formatDate(date)
    }

    private fun showFileChoose() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, CHOOSE_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK
            && data != null && data.data != null
        ) {
            viewModel.setSelectedImageUri(data.data)
        }
    }

    private fun uploadImage() {
        val imgDescription = binding.imgDescription.text.toString().trim()
        val imgAuthor = binding.imgAuthor.text.toString().trim()

        if (viewModel.selectedImageUri.value == null) {
            showToast("Ningún archivo seleccionado", Toast.LENGTH_SHORT)
            return
        }

        if (imgDescription.isEmpty()) {
            binding.imgDescription.error = "Descripción requerida"
            return
        }

        viewModel.uploadImage(imgDescription, imgAuthor, contentResolver)
    }
}