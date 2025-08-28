package com.ohz.clean.utils

import android.content.Context
import android.os.Build
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import java.io.File

object StorageUtil {

    data class StorageInfo(
        val totalSize: Long,
        val usedSize: Long,
        val freeSize: Long
    )

    fun getTotalStorageInfo(context: Context): StorageInfo {
        var totalSize = 0L
        var freeSize = 0L

        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0+ 可以枚举所有卷
            val volumes: List<StorageVolume> = storageManager.storageVolumes
            for (volume in volumes) {
                try {
                    val path: File? = volume.directory
                    if (path != null) {
                        val stat = StatFs(path.absolutePath)
                        totalSize += stat.blockSizeLong * stat.blockCountLong
                        freeSize += stat.blockSizeLong * stat.availableBlocksLong
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // 低版本只看外部存储
            val path = android.os.Environment.getExternalStorageDirectory()
            val stat = StatFs(path.absolutePath)
            totalSize = stat.blockSizeLong * stat.blockCountLong
            freeSize = stat.blockSizeLong * stat.availableBlocksLong
        }

        val usedSize = totalSize - freeSize
        return StorageInfo(totalSize, usedSize, freeSize)
    }
}