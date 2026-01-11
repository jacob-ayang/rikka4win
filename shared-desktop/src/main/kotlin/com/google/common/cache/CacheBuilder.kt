package com.google.common.cache

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class CacheBuilder private constructor() {
    companion object {
        fun newBuilder(): CacheBuilder = CacheBuilder()
    }

    fun expireAfterWrite(duration: Long, unit: TimeUnit): CacheBuilder = this

    fun <K : Any, V : Any> build(): Cache<K, V> = SimpleCache()
}

interface Cache<K : Any, V : Any> {
    fun getIfPresent(key: K): V?
    fun put(key: K, value: V)
}

private class SimpleCache<K : Any, V : Any> : Cache<K, V> {
    private val data = ConcurrentHashMap<K, V>()

    override fun getIfPresent(key: K): V? = data[key]

    override fun put(key: K, value: V) {
        data[key] = value
    }
}
