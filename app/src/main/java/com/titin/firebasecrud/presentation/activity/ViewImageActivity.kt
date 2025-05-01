package com.titin.firebasecrud.presentation.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.titin.firebasecrud.databinding.ActivityViewImageBinding
import com.titin.firebasecrud.databinding.DialogEditUploadBinding
import com.titin.firebasecrud.domain.model.Upload
import com.titin.firebasecrud.presentation.adapter.ImageAdapter
import com.titin.firebasecrud.presentation.viewmodel.ViewImageViewModel
import com.titin.firebasecrud.presentation.viewmodel.ViewImageViewModelFactory
import com.titin.firebasecrud.util.DateUtil
import com.titin.firebasecrud.util.Resource
import com.titin.firebasecrud.util.gone
import com.titin.firebasecrud.util.loadImage
import com.titin.firebasecrud.util.showToast
import com.titin.firebasecrud.util.visible
import java.util.Calendar
import com.titin.firebasecrud.FirebaseCrudApp
import com.titin.firebasecrud.di.AppContainer

class ViewImageActivity : BaseActivity<ActivityViewImageBinding>(), ImageAdapter.OnItemClickListener {
    companion object {
        private const val PICK_IMAGE_REQUEST = 2
    }

    private val viewModel: ViewImageViewModel by viewModels {
        ViewImageViewModelFactory((application as FirebaseCrudApp).appContainer.uploadRepository)
    }

    private lateinit var adapter: ImageAdapter
    private lateinit var dialogBinding: DialogEditUploadBinding

    override fun inflateBinding(): ActivityViewImageBinding =
        ActivityViewImageBinding.inflate(layoutInflater)

    override fun setupUI() {
        setupRecyclerView()
        binding.fabAdd.setOnClickListener {
            // Iniciar MainActivity para añadir un nuevo registro
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun setupObservers() {
        viewModel.uploads.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.progressCircular.gone()
                    resource.data?.let { uploads ->
                        adapter.submitList(uploads)
                    }
                }

                is Resource.Error -> {
                    binding.progressCircular.gone()
                    showToast(resource.message)
                }

                is Resource.Loading -> {
                    binding.progressCircular.visible()
                }
            }
        }

        viewModel.currentOperation.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    showToast("Operación exitosa")
                }

                is Resource.Error -> {
                    showToast(resource.message)
                }

                is Resource.Loading -> {
                    // Mostrar indicador de carga si es necesario
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@ViewImageActivity)
            adapter = ImageAdapter(this@ViewImageActivity).also {
                this@ViewImageActivity.adapter = it
            }
        }

        adapter.setOnItemClickListener(this)
    }

    override fun onItemClick(position: Int) {
        // Implementación para visualización ampliada si se desea
        viewModel.uploads.value?.data?.get(position)?.let { upload ->
            // Por ejemplo, mostrar detalles en pantalla completa
            showToast("Imagen seleccionada: ${upload.imgName}")
        }
    }

    override fun onEditClick(position: Int) {
        viewModel.uploads.value?.data?.get(position)?.let { selectedItem ->
            viewModel.prepareForEdit(selectedItem)
            showEditDialog(selectedItem)
        }
    }

    override fun onDeleteClick(position: Int) {
        viewModel.uploads.value?.data?.get(position)?.let { selectedItem ->
            // Confirmación de eliminación
            AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro que desea eliminar esta imagen?")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.deleteUpload(selectedItem)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun showEditDialog(upload: Upload) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBinding = DialogEditUploadBinding.inflate(layoutInflater)
        dialogBuilder.setView(dialogBinding.root)

        // Cargar la imagen actual
        dialogBinding.imgEditPreview.loadImage(upload.imgUrl)
        dialogBinding.etEditName.setText(upload.imgName)
        dialogBinding.etEditAuthor.setText(upload.author)
        dialogBinding.tvEditDate.text = upload.creationDate

        val calendar = DateUtil.parseDate(upload.creationDate)

        dialogBinding.btnChangeImage.setOnClickListener {
            openFileChooser()
        }

        dialogBinding.btnEditDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    dialogBinding.tvEditDate.text = DateUtil.formatDate(calendar)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        dialogBuilder.setTitle("Editar registro")
        dialogBuilder.setPositiveButton("Actualizar") { _, _ ->
            val name = dialogBinding.etEditName.text.toString().trim()
            val author = dialogBinding.etEditAuthor.text.toString().trim()
            val date = dialogBinding.tvEditDate.text.toString().trim()

            if (name.isEmpty()) {
                showToast("Nombre no puede estar vacío")
                return@setPositiveButton
            }

            viewModel.updateUpload(name, author, date, contentResolver)
        }

        dialogBuilder.setNegativeButton("Cancelar") { dialog, _ ->
            viewModel.setEditImageUri(null)
            viewModel.setCurrentUpload(null)
            dialog.dismiss()
        }

        dialogBuilder.create().show()
    }

    private fun openFileChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.data != null
        ) {
            viewModel.setEditImageUri(data.data)
            dialogBinding.imgEditPreview.loadImage(data.data.toString())
        }
    }
}