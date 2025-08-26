package com.ohz.clean.ui.view.progress

import android.content.Context
import android.text.format.Formatter
import com.ohz.clean.R
import com.ohz.clean.common.easterEggProgressMsg
import eu.darken.sdmse.common.ca.CaDrawable
import eu.darken.sdmse.common.ca.CaString
import eu.darken.sdmse.common.ca.toCaString
import kotlinx.coroutines.flow.Flow
import kotlin.math.ceil

interface Progress {

    data class Data(
        val icon: CaDrawable? = null,
        val primary: CaString = R.string.general_progress_loading.toCaString(),
        val secondary: CaString = easterEggProgressMsg.toCaString(),
        val count: Count = Count.Indeterminate(),
        val extra: Any? = null
    )

    interface Host {
        val progress: Flow<Data?>
    }

    interface Client {
        fun updateProgress(update: (Data?) -> Data?)
    }

    sealed interface Count {
        val current: Long
        val max: Long
        fun displayValue(context: Context): String?

        data class Percent(override val current: Long, override val max: Long) : Count {

            constructor(current: Int, max: Int) : this(current.toLong(), max.toLong())
            constructor(max: Int) : this(0, max)
            constructor(max: Long) : this(0, max)

            override fun displayValue(context: Context): String {
                if (current == 0L && max == 0L) return "NaN"
                if (current == 0L) return "0%"
                return "${ceil(((current.toDouble() / max.toDouble()) * 100)).toInt()}%"
            }

            fun increment(value: Int = 1): Percent {
                return Percent(current + value, max)
            }
        }

        class Counter(override val current: Long, override val max: Long) : Count {

            constructor(current: Int, max: Int) : this(current.toLong(), max.toLong())
            constructor(max: Int) : this(0, max)
            constructor(max: Long) : this(0, max)

            override fun displayValue(context: Context): String = "$current/$max"

            fun increment(value: Int = 1) = Counter(current + value, max)
        }

        data class Size(override val current: Long, override val max: Long) : Count {
            override fun displayValue(context: Context): String {
                val curSize = Formatter.formatShortFileSize(context, current)
                val maxSize = Formatter.formatShortFileSize(context, max)
                return "$curSize/$maxSize"
            }
        }

        data class Indeterminate(override val current: Long = 0, override val max: Long = 0) : Count {
            override fun displayValue(context: Context): String = ""
        }

        data class None(override val current: Long = -1, override val max: Long = -1) : Count {
            override fun displayValue(context: Context): String? = null
        }
    }
}