package com.kaizensundays.fusion.fringe

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

    private val mojo = DecryptMojo()

    @Test
    fun test() {
        val encryptor: Encryptor = mock()
        mojo.encryptor = encryptor

        System.setProperty("i", "inputFile")
        System.setProperty("o", "outputFile")

        val key: SecretKey = mock()

        whenever(encryptor.readKey(any())).thenReturn(key)

        mojo.execute()

        verify(encryptor).readKey(any())
        verify(encryptor).decrypt("inputFile", "outputFile", key)
        verifyNoMoreInteractions(encryptor)
    }

}