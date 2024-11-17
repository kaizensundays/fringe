package com.kaizensundays.fusion.fringe

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import javax.crypto.SecretKey

/**
 * Created: Monday 9/2/2024, 12:06 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
class EncryptMojoTest {

    private val encryptor: Encryptor = mock()
    private val key: SecretKey = mock()
    private val salt = ByteArray(16)
    private val iv = ByteArray(16)

    private val mojo = EncryptMojo()

    @BeforeEach
    fun before() {
        mojo.encryptor = encryptor

        System.setProperty("i", "inputFile")
        System.setProperty("o", "outputFile")
        System.setProperty("key", "1234567")
    }

    @Test
    fun encrypt() {

        whenever(encryptor.getKey(any())).thenReturn(key)
        whenever(encryptor.generateSalt()).thenReturn(salt)
        whenever(encryptor.generateIV()).thenReturn(iv)

        mojo.execute()

        verify(encryptor).getKey(any())
        verify(encryptor).generateSalt()
        verify(encryptor).generateIV()
        verify(encryptor).encrypt("inputFile", "outputFile", key, salt, iv)
        verifyNoMoreInteractions(encryptor)
    }

    @Test
    fun logsException() {

        mojo.logger = mock()

        whenever(encryptor.getKey(any())).thenReturn(key)
        whenever(encryptor.generateSalt()).thenReturn(salt)
        whenever(encryptor.generateIV()).thenReturn(iv)

        whenever(encryptor.encrypt("inputFile", "outputFile", key, salt, iv))
            .thenThrow(IllegalStateException("text"))

        mojo.execute()

        verify(mojo.logger).error("text")
        verifyNoMoreInteractions(mojo.logger)
    }
}