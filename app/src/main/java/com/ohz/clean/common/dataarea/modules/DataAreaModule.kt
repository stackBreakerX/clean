package com.ohz.clean.common.dataarea.modules

import com.ohz.clean.common.dataarea.DataArea


interface DataAreaModule {

    suspend fun firstPass(): Collection<DataArea> = emptySet()

    suspend fun secondPass(firstPass: Collection<DataArea>): Collection<DataArea> = emptySet()
}