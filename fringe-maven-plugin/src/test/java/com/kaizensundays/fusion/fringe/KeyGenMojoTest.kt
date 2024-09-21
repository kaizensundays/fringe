package com.kaizensundays.fusion.fringe

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import javax.crypto.SecretKey

/**
 * Created: Saturday 9/7/2024, 4:58 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
class KeyGenMojoTest {

    private val mojo = KeyGenMojo()

    @Test
    fun test() {
        val encryptor: Encryptor = mock()
        mojo.encryptor = encryptor

        val key: SecretKey = mock()

        whenever(encryptor.generateKey()).thenReturn(key)

        mojo.execute()

        verify(encryptor).generateKey()
        verify(encryptor).writeKey(key, "KEY")
        verifyNoMoreInteractions(encryptor)
    }

}