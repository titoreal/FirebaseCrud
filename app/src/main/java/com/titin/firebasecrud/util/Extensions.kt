package com.titin.firebasecrud.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.squareup.picasso.Picasso
import com.titin.firebasecrud.FirebaseCrudApp
import com.titin.firebasecrud.R


// Container shortcuts
val Context.appContainer
    get() = (applicationContext as FirebaseCrudApp).appContainer

// View visibility
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

// Toast extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// ImageView extensions for Picasso
fun ImageView.loadImage(url: String?, @DrawableRes placeholder: Int = R.drawable.imagepreview) {
    if (url.isNullOrEmpty()) {
        setImageResource(placeholder)
    } else {
        Picasso.get()
            .load(url)
            .placeholder(placeholder)
            .error(placeholder)
            .into(this)
    }
}

// Keyboard management
fun Activity.hideKeyboard() {
    val view = currentFocus ?: View(this)
    (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(view.windowToken, 0)
}