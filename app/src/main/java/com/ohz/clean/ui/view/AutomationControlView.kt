package com.ohz.clean.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.ohz.clean.databinding.AutomationControlViewBinding
import com.ohz.clean.ui.view.progress.Progress
import com.ohz.clean.ui.view.progress.layoutInflator
import eu.darken.sdmse.common.ca.CaString
import eu.darken.sdmse.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import kotlin.apply
import kotlin.text.isEmpty

class AutomationControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val ui = AutomationControlViewBinding.inflate(layoutInflator, this)

    fun setProgress(data: Progress.Data?) {
        log(TAG, VERBOSE) { "setProgress($data)" }
        isVisible = data != null

        if (data == null) {
            ui.clickScreenMascotContainer.pauseAnimation()
            return
        }

        ui.clickScreenMascotContainer.apply {
            if (!isAnimating) playAnimation()
        }

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
            isGone = data.count is Progress.Count.None
            when (data.count) {
                is Progress.Count.Counter -> {
                    isIndeterminate = false
                    progress = data.count.current.toInt()
                    max = data.count.max.toInt()
                }

                is Progress.Count.Percent -> {
                    isIndeterminate = false
                    progress = data.count.current.toInt()
                    max = data.count.max.toInt()
                }

                is Progress.Count.Indeterminate -> {
                    isIndeterminate = true
                }

                is Progress.Count.Size -> {}
                is Progress.Count.None -> {}
            }
        }
        ui.progressText.apply {
            text = data.count.displayValue(context)
            isInvisible = data.count is Progress.Count.Indeterminate || data.count is Progress.Count.None
        }
    }

    fun setTitle(title: CaString, subtitle: CaString) {
        val t = title.get(context)
        val s = subtitle.get(context)
        log(TAG, VERBOSE) { "setTitle($t,$s)" }
        ui.title.text = t
        ui.subtitle.text = s
    }

    fun setCancelListener(listener: OnClickListener?) {
        log(TAG) { "setCancelListener($listener)" }
        ui.cancelAction.setOnClickListener(listener)
    }

    companion object {
        val TAG: String = logTag("Automation", "Service", "ControlView")
    }
}