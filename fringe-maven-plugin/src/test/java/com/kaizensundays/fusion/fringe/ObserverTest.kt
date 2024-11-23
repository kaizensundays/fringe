package com.kaizensundays.fusion.fringe

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

    val targetDir = "target/.fringe"

    val encryptor = Encryptor()

    @BeforeEach
    fun before() {
        File(targetDir).mkdirs()
        Security.addProvider(BouncyCastleProvider())
        encryptor.progressCounterTerm = 1
    }

    @Test
    fun sha256() {

        // https://emn178.github.io/online-tools/sha256.html

        val sha256 = listOf(
            "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=",
            "i7DPbrmxfQ99IrRW8SElfcElTh8BZlNwR2OD6ndt9BQ=",
            "pm2JcOVpqUQjTL7oxBQ1f6YJLBq3efvWN0M/I+Koq7g=",
            "zVWeoKvKCF7xRCc8qvcmYYO7oiLznYlOdJZvwEuP/xg=",
            "B4JqMcdE+5Ot/u2oYa8Yx3mvlW8IcFxaU2xy+rgeguI=",
            "WvLY0LGsUJ8F1HsZKPPVI9/fCEhNEk/kQ4IGYeOT/B4="
        )

        listOf(
            "", "1234567", "August", "September",
            "In this game of skill one must have above all else, patience.",
            "The only thing better than a cow is a human!",
        ).zip(sha256)
            .forEach { (t, s) ->
                assertEquals(s, encryptor.sha256(t))
            }
    }

    @Test
    fun encryptEndDecrypt() {

        val iv = "0123456789ABCDEF".toByteArray()

        val observer = "September"

        val salt = encryptor.generateSalt()

        var key = encryptor.generateKey(observer, salt)

        val sample = "Anthropomorphism"

        val encrypted = encryptor.encrypt(sample.toByteArray(), key, salt, iv)

        val encoded = Base64.getEncoder().encodeToString(encrypted)

        val file = File("$targetDir/sample")

        file.writeText(encoded)

        val text = file.readText().trim()

        assertEquals(encoded, text)

        val decoded = Base64.getDecoder().decode(text)

        key = encryptor.generateKey(observer, salt)

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