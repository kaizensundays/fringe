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
 * Created: Saturday 9/7/2024, 12:57 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
class DecryptMojoTest {

    private val encryptor: Encryptor = mock()
    private val key: SecretKey = mock()

    private val mojo = DecryptMojo()

    @BeforeEach
    fun before() {
        mojo.encryptor = encryptor

        System.setProperty("i", "inputFile")
        System.setProperty("o", "outputFile")
        System.setProperty("key", "1234567")
    }

    @Test
    fun decrypt() {

        whenever(encryptor.getKey(any())).thenReturn(key)

        mojo.execute()

        verify(encryptor).getKey(any())
        verify(encryptor).decrypt("inputFile", "outputFile", key)
        verifyNoMoreInteractions(encryptor)
    }

}