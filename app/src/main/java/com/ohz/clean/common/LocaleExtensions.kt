package com.ohz.clean.common

import android.os.LocaleList
import java.util.Locale

fun LocaleList.toList(): List<Locale> = List(this.size()) { index -> this.get(index) }

val LocaleList.primary: Locale
    get() = get(0)