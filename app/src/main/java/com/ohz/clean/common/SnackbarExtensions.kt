package com.ohz.clean.common

import android.widget.TextView
import com.google.android.material.R
import com.google.android.material.snackbar.Snackbar
import kotlin.apply


fun Snackbar.enableBigText(maxLines: Int = 6) = apply {
    val layout = view as Snackbar.SnackbarLayout
    val textView = layout.findViewById<TextView>(R.id.snackbar_text)
    textView.maxLines = maxLines
}