package com.ohz.clean.common

import com.ohz.clean.common.storage.StorageId
import eu.darken.sdmse.common.ca.CaString

data class DeviceStorage(
    val id: StorageId,
    val label: CaString,
    val type: Type,
    val hardware: Hardware,
    val spaceCapacity: Long,
    val spaceFree: Long,
    val setupIncomplete: Boolean
) {
    val spaceUsed: Long
        get() = spaceCapacity - spaceFree

    enum class Type {
        PRIMARY,
        SECONDARY,
        PORTABLE,
        ;
    }

    enum class Hardware {
        BUILT_IN,
        SDCARD,
        USB,
        ;
    }

}


