package ru.tech.imageresizershrinker.domain.repository

interface CipherRepository {

    fun generateRandomString(len: Int): String

    suspend fun decrypt(data: ByteArray, key: String): ByteArray

    suspend fun encrypt(data: ByteArray, key: String): ByteArray

}