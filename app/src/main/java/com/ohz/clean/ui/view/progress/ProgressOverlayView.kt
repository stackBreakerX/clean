package com.ohz.clean.ui.view.progress

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.ohz.clean.databinding.ViewProgressOverlayBinding
import com.ohz.clean.ui.view.progress.Progress.Count

class ProgressOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val ui = ViewProgressOverlayBinding.inflate(layoutInflator, this)

    fun setProgress(data: Progress.Data?) {
        isVisible = data != null
        if (data == null) return

        ui.primary.apply {
            val newText = data.primary.get(context)
            text = newText
            isInvisible = newText.isEmpty()
        }
        ui.secondary.apply {
            val newText = data.secondary.get(context)
            text = newText
            isInvisible = newText.isEmpty()
        }

        ui.progress.apply {
            isGone = data.count is Count.None
            when (data.count) {
                is Count.Counter -> {
                    isIndeterminate = data.count.current == 0L
                    progress = data.count.current.toInt()
                    max = data.count.max.toInt()
                }
                is Count.Percent -> {
                    isIndeterminate = data.count.current == 0L
                    progress = data.count.current.toInt()
                    max = data.count.max.toInt()
                }
                is Count.Indeterminate -> {
                    isIndeterminate = true
                }
                is Count.Size -> {}
                is Count.None -> {}
            }
        }
        ui.progressText.apply {
            text = data.count.displayValue(context)
            isInvisible = data.count is Count.Indeterminate || data.count is Count.None || data.count.current == 0L
        }
    }

}