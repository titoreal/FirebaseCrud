package com.titin.firebasecrud.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {

    private var _binding: T? = null  // Usar _binding en lugar de *binding
    protected val binding get() = _binding!!

    protected abstract fun inflateBinding(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding()  // Usar _binding
        setContentView(binding.root)

        setupUI()
        setupObservers()
    }

    // Los comentarios también estaban malformados
    /**
     * Configura la UI, incluyendo listeners, adaptadores, etc.
     */
    protected open fun setupUI() {}

    /**
     * Configura los observadores de LiveData
     */
    protected open fun setupObservers() {}

    override fun onDestroy() {
        super.onDestroy()
        _binding = null  // Usar _binding
    }
}