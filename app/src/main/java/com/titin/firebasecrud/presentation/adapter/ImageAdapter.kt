package com.titin.firebasecrud.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.titin.firebasecrud.R
import com.titin.firebasecrud.databinding.ImageItemBinding
import com.titin.firebasecrud.domain.model.Upload

class ImageAdapter(private val context: Context) :
    ListAdapter<Upload, ImageAdapter.ImageViewHolder>(UploadDiffCallback()) {

    interface OnItemClickListener {
        fun onItemClick(position: Int) {}
        fun onEditClick(position: Int) {}
        fun onDeleteClick(position: Int) {}
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImageItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        getItem(position).let { upload ->
            holder.bind(upload)
        }
    }

    inner class ImageViewHolder(
        private val binding: ImageItemBinding,
        private val listener: OnItemClickListener?
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            setupListeners()
        }

        fun bind(upload: Upload) {
            binding.imgDescription.text = upload.imgName
            binding.imgAuthor.text = "Autor: ${upload.author}"
            binding.imgDate.text = "Fecha: ${upload.creationDate}"

            Picasso.get()
                .load(upload.imgUrl)
                .placeholder(R.drawable.imagepreview)
                .fit()
                .centerCrop()
                .into(binding.imageView)
        }

        private fun setupListeners() {
            binding.root.setOnClickListener {
                checkPositionAndNotify { pos -> listener?.onItemClick(pos) }
            }

            binding.btnEdit.setOnClickListener {
                checkPositionAndNotify { pos -> listener?.onEditClick(pos) }
            }

            binding.btnDelete.setOnClickListener {
                checkPositionAndNotify { pos -> listener?.onDeleteClick(pos) }
            }
        }

        private inline fun checkPositionAndNotify(action: (Int) -> Unit) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                action(position)
            }
        }
    }

    private class UploadDiffCallback : DiffUtil.ItemCallback<Upload>() {
        override fun areItemsTheSame(oldItem: Upload, newItem: Upload): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(oldItem: Upload, newItem: Upload): Boolean {
            // Compara todos los campos relevantes
            return oldItem.imgName == newItem.imgName &&
                    oldItem.imgUrl == newItem.imgUrl &&
                    oldItem.author == newItem.author &&
                    oldItem.creationDate == newItem.creationDate
        }
    }
}