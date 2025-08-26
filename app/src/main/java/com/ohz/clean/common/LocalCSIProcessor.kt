package com.ohz.clean.common

import eu.darken.sdmse.common.pkgs.PkgRepo
import eu.darken.sdmse.common.pkgs.isInstalled

interface LocalCSIProcessor : CSIProcessor {

    suspend fun PkgRepo.isInstalled(owner: Owner): Boolean = this.isInstalled(owner.pkgId, owner.userHandle)

    override suspend fun findOwners(areaInfo: AreaInfo): CSIProcessor.Result {
        require(hasJurisdiction(areaInfo.type)) { "Wrong jurisdiction: ${areaInfo.type}" }
        return CSIProcessor.Result()
    }
}