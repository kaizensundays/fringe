package com.kaizensundays.fusion.fringe

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.CompletableFuture

/**
 * Created: Saturday 11/2/2024, 9:02 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
class AbstractFringeMojoTest {

    @Test
    fun getBase64Key() {
        // UI
        val observer = "September"

        val mojo = object : AbstractFringeMojo() {
            override fun doExecute() {
            }
        }
        mojo.observer = mock()

        var base64Key = mojo.encryptor.sha256(observer)

        val done = CompletableFuture.completedFuture(observer)

        whenever(mojo.observer.build()).thenReturn(done)

        System.clearProperty("key")

        var value = mojo.getBase64Key()

        assertEquals(base64Key, value)

        // env
        val lake = "Reiden"

        base64Key = mojo.encryptor.sha256(lake)

        System.setProperty("key", base64Key)

        value = mojo.getBase64Key()

        assertEquals(base64Key, value)
    }

}