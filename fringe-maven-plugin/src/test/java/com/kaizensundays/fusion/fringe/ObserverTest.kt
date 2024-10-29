package com.kaizensundays.fusion.fringe

import org.bouncycastle.jcajce.provider.digest.SHA256
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.security.Security
import java.util.*

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

    fun sha256(s: String): String {
        val digest = SHA256.Digest()
        val bytes = digest.digest(s.toByteArray())
        return Base64.getEncoder().encodeToString(bytes)
    }

    @Test
    fun encryptEndDecrypt() {

        val iv = "0123456789ABCDEF".toByteArray()

        val observer = "September"

        val sha256 = sha256(observer)

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

}