package com.kaizensundays.fusion.fringe

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.FileNotFoundException
import java.security.Security


/**
 * Created: Sunday 8/25/2024, 1:03 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
@Suppress("MemberVisibilityCanBePrivate")
class EncryptorTest {

    val targetDir = "target/.fringe"

    val quote = "Everything is not as it seems"

    val encryptor = Encryptor()

    @BeforeEach
    fun before() {
        File(targetDir).mkdirs()
        Security.addProvider(BouncyCastleProvider())
        encryptor.progressCounterTerm = 1
    }

    @Test
    fun readIV() {

        val file = File("$targetDir/iv16")

        var iv = encryptor.getRandomBytes(16)
        file.writeBytes(iv)
        assertArrayEquals(iv, encryptor.readIV(file))

        iv = encryptor.getRandomBytes(11)
        file.writeBytes(iv)
        assertThrows<IllegalStateException> { encryptor.readIV(file) }

        assertThrows<FileNotFoundException> { encryptor.readIV(File("$targetDir/no-such-file")) }
    }

    @Test
    fun generateKey() {

        val key = encryptor.generateKey()

        assertEquals(32, key.encoded.size)
    }

    @Test
    fun readAndWriteKey() {

        (1..3).forEach { n ->

            val key = encryptor.generateKey()

            val keyFile = "$targetDir/key$n"

            encryptor.writeKey(key, "$targetDir/key$n")

            val read = encryptor.readKey(keyFile)

            assertArrayEquals(key.encoded, read.encoded)
        }
    }

    @Test
    fun header() {
        val iv = "0123456789ABCDEF"
        assertEquals(16, iv.length)
        val header = encryptor.header("Begin", 1, iv.toByteArray())
        assertEquals(32, header.length)
        assertEquals("Begin01         $iv", header)
    }

    @Test
    fun encryptEndDecrypt() {

        val key = encryptor.generateKey()
        val iv = encryptor.generateIV()

        // test byte array size
        val bytes = encryptor.getRandomBytes(1000)
        var encrypted = encryptor.encrypt(bytes, key, iv)

        assertTrue(encrypted.isNotEmpty())

        encrypted = encryptor.encrypt(quote.toByteArray(), key, iv)

        val decrypted = encryptor.decrypt(encrypted, key, iv)

        assertEquals(quote, String(decrypted))
    }

    @Test
    fun encryptEndDecryptFile() {

        val inputFile = File("pom.xml").canonicalPath
        val encryptedFile = "$targetDir/pom.xml.aes"
        val decryptedFile = "$targetDir/pom.xml.txt"

        val key = encryptor.generateKey()
        val iv = encryptor.generateIV()

        encryptor.encrypt(inputFile, encryptedFile, key, iv)

        encryptor.decrypt(encryptedFile, decryptedFile, key)
    }

}