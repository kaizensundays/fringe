package com.kaizensundays.fusion.fringe

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.security.Security
import java.util.concurrent.CompletableFuture

/**
 * Created: Saturday 11/2/2024, 9:02 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
class AbstractFringeMojoTest {

    @BeforeEach
    fun before() {
        Security.addProvider(BouncyCastleProvider())
    }

    @Test
    fun getBase64Key() {
        // UI
        val observer = "September"

        val mojo = object : AbstractFringeMojo() {
            override fun doExecute() {
            }
        }
        mojo.observer = mock()

        val done = CompletableFuture.completedFuture(observer)

        whenever(mojo.observer.build()).thenReturn(done)

        System.clearProperty("key")

        val salt = mojo.encryptor.generateSalt();

        var value = mojo.getBase64Key(salt)

        assertEquals(44, value.length)

        // env
        val lake = "Reiden"

        val base64Key = mojo.encryptor.sha256(lake)

        System.setProperty("key", base64Key)

        value = mojo.getBase64Key(salt)

        assertEquals(base64Key, value)
    }

}