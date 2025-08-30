//package com.ohz.clean.common
//
//import dagger.Reusable
//import eu.darken.sdmse.common.debug.logging.Logging.Priority.VERBOSE
//import eu.darken.sdmse.common.debug.logging.log
//import eu.darken.sdmse.common.debug.logging.logTag
//import kotlinx.coroutines.flow.asFlow
//import kotlinx.coroutines.flow.filter
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.onEach
//import kotlinx.coroutines.flow.toList
//import javax.inject.Inject
//import kotlin.collections.forEach
//import kotlin.collections.plus
//import kotlin.collections.toSet
//
//@Reusable
//class FilterSource @Inject constructor(
//    private val filterFactories: Set<@JvmSuppressWildcards SystemCleanerFilter.Factory>,
//    private val customFilterRepo: CustomFilterRepo,
//    private val customFilterLoader: CustomFilterLoader.Factory,
//) {
//
//    init {
//        filterFactories.forEach { log(TAG, VERBOSE) { "Available filter: $it" } }
//    }
//
//    suspend fun create(onlyEnabled: Boolean): Set<SystemCleanerFilter> {
//        val builtInFilters = filterFactories
//            .asFlow()
//            .filter { !onlyEnabled || it.isEnabled() }
//            .map { it.create() }
//            .onEach {
//                log(TAG, VERBOSE) { "Initializing $it" }
//                it.initialize()
//            }
//            .toList()
//
//        val customFilters = customFilterRepo.configs.first()
//            .asFlow()
//            .map { customFilterLoader.create(it) }
//            .filter { !onlyEnabled || it.isEnabled() }
//            .map { it.create() }
//            .onEach {
//                log(TAG, VERBOSE) { "Initializing $it" }
//                it.initialize()
//            }
//            .toList()
//
//        return (builtInFilters + customFilters).toSet()
//    }
//
//
//    companion object {
//        private val TAG = logTag("SystemCleaner", "FilterSource")
//    }
//}