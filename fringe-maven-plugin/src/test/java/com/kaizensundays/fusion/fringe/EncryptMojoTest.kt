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
    private val iv = ByteArray(16)

    private val mojo = EncryptMojo()

    @BeforeEach
    fun before() {
        mojo.encryptor = encryptor

        System.setProperty("i", "inputFile")
        System.setProperty("o", "outputFile")
    }

    @Test
    fun test() {

        whenever(encryptor.readKey(any())).thenReturn(key)
        whenever(encryptor.getRandomBytes(16)).thenReturn(iv)

        mojo.execute()

        verify(encryptor).readKey(any())
        verify(encryptor).getRandomBytes(16)
        verify(encryptor).encrypt("inputFile", "outputFile", key, iv)
        verifyNoMoreInteractions(encryptor)
    }

    @Test
    fun logsException() {

        mojo.logger = mock()

        whenever(encryptor.readKey(any())).thenReturn(key)
        whenever(encryptor.getRandomBytes(16)).thenReturn(iv)

        whenever(encryptor.encrypt("inputFile", "outputFile", key, iv))
            .thenThrow(IllegalStateException("text"))

        mojo.execute()

        verify(mojo.logger).error("text")
        verifyNoMoreInteractions(mojo.logger)
    }
}