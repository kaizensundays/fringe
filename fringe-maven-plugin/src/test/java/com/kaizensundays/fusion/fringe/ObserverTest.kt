package com.kaizensundays.fusion.fringe

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.security.Security
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created: Monday 10/28/2024, 8:47 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
@Suppress("MemberVisibilityCanBePrivate")
class ObserverTest {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    val targetDir = "target/.fringe"

    val encryptor = Encryptor()

    @BeforeEach
    fun before() {
        File(targetDir).mkdirs()
        Security.addProvider(BouncyCastleProvider())
        encryptor.progressCounterTerm = 1
    }

    @Test
    fun encryptEndDecrypt() {

        val iv = "0123456789ABCDEF".toByteArray()

        val observer = "September"

        val sha256 = encryptor.sha256(observer)

        val key = encryptor.getKey(sha256)

        val sample = "Anthropomorphism"

        val encrypted = encryptor.encrypt(sample.toByteArray(), key, iv)

        val encoded = Base64.getEncoder().encodeToString(encrypted)

        val file = File("$targetDir/sample")

        file.writeText(encoded)

        val text = file.readText().trim()

        assertEquals(encoded, text)

        val decoded = Base64.getDecoder().decode(text)

        val decrypted = encryptor.decrypt(decoded, key, iv)

        assertEquals(sample, String(decrypted))
    }

    fun isRunningFromMaven(): Boolean {
        return System.getProperty("surefire.test.class.path") != null
    }

    @Test
    fun ui() {
        if (isRunningFromMaven()) {
            return
        }

        val done = Observer().build()

        val text = done.get(100, TimeUnit.SECONDS)

        println("text=$text")

        assertTrue(text != null && text.isNotBlank())
    }

}