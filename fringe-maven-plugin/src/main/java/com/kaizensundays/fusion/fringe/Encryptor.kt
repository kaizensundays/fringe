package com.kaizensundays.fusion.fringe

import org.bouncycastle.jcajce.provider.digest.SHA256
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created: Sunday 9/1/2024, 12:22 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
@Suppress("PrivatePropertyName")
class Encryptor {

    private val beginString = "Fringe"

    private val version = 3

    private val AES_BLOCK_SIZE = 16
    private val NUMBER_OF_BLOCKS = 1024
    private val KEY_SIZE_BITS = 256
    private val SALT_SIZE = 16
    private val IV_SIZE = 16
    private val HEADER_SIZE = 16
    private val PBE_ITERATIONS_COUNT = 1000

    private val chars = ('0'..'9') + ('A'..'Z') + ('a'..'z')

    var progressCounterTerm = 1000

    fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(KEY_SIZE_BITS)
        return keyGen.generateKey()
    }

    fun generateKey(text: String, salt: ByteArray): SecretKey {
        val keySpec = PBEKeySpec(text.toCharArray(), salt, PBE_ITERATIONS_COUNT, KEY_SIZE_BITS)
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256", "BC")
        return keyFactory.generateSecret(keySpec)
    }

    fun generateBase64Key(text: String, salt: ByteArray): String {
        val key = generateKey(text, salt)
        return Base64.getEncoder().encodeToString(key.encoded)
    }

    fun writeKey(key: SecretKey, keyFile: String) {
        val base64 = Base64.getEncoder().encodeToString(key.encoded)
        File(keyFile).writeText(base64)
    }

    fun sha256(s: String): String {
        val digest = SHA256.Digest()
        val bytes = digest.digest(s.toByteArray())
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun getKey(base64: String): SecretKey {
        val bytes = Base64.getDecoder().decode(base64)
        return SecretKeySpec(bytes, "AES")
    }

    fun readKey(keyFile: String): SecretKey {
        val base64 = File(keyFile).readText().trim()
        return getKey(base64)
    }

    private fun getRandomChars(size: Int): String {
        return (1..size).map { chars.random() }.joinToString("")
    }

    fun getRandomBytes(size: Int): ByteArray {
        return getRandomChars(size).toByteArray()
    }

    fun generateSalt(): ByteArray {
        return getRandomBytes(SALT_SIZE)
    }

    fun generateIV(): ByteArray {
        return getRandomBytes(IV_SIZE)
    }

    fun readIV(file: File): ByteArray {
        val iv = file.readBytes()
        check(iv.size == IV_SIZE) { "$IV_SIZE bytes expected" }
        return iv
    }

    private fun getAESCipher(mode: Int, key: SecretKey, ivSpec: IvParameterSpec): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC")
        cipher.init(mode, key, ivSpec)
        return cipher
    }

    fun header(beginString: String, version: Int, salt: ByteArray, iv: ByteArray): String {
        val s = ("%s%02d").format(beginString, version)
        return ("%-16s%s%s").format(s, String(salt), String(iv))
    }

    private fun process(inputStream: InputStream, outputStream: OutputStream, cipher: Cipher) {
        var count = 0
        CipherOutputStream(outputStream, cipher).use { cos ->
            val buffer = ByteArray(NUMBER_OF_BLOCKS * AES_BLOCK_SIZE)
            var bytesRead: Int
            while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                cos.write(buffer, 0, bytesRead)
                if (++count % progressCounterTerm == 0) {
                    print('.')
                }
            }
        }
    }

    private fun encrypt(inputStream: InputStream, outputStream: OutputStream, key: SecretKey, salt: ByteArray, iv: ByteArray) {

        val cipher = getAESCipher(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))

        inputStream.use { inStream ->
            outputStream.use { outStream ->
                if (outputStream is FileOutputStream) {
                    val header = header(beginString, version, salt, iv)
                    outStream.write(header.toByteArray())
                }
                process(inStream, outStream, cipher)
            }
        }
    }

    fun encrypt(input: ByteArray, key: SecretKey, salt: ByteArray, iv: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()
        encrypt(ByteArrayInputStream(input), outputStream, key, salt, iv)
        return outputStream.toByteArray()
    }

    fun encrypt(inputFile: String, outputFile: String, key: SecretKey, salt: ByteArray, iv: ByteArray) {
        encrypt(FileInputStream(inputFile), FileOutputStream(outputFile), key, salt, iv)
    }

    private fun readVersion(inputStream: FileInputStream): String {
        val headerBytes = inputStream.readNBytes(HEADER_SIZE)
        val header = String(headerBytes)
        return header.substring(beginString.length, beginString.length + 2)
    }

    private fun readSalt(inputStream: FileInputStream): ByteArray {
        return inputStream.readNBytes(SALT_SIZE) ?: ByteArray(0)
    }

    private fun readIV(inputStream: FileInputStream): ByteArray {
        return inputStream.readNBytes(IV_SIZE) ?: ByteArray(0)
    }

    private fun decrypt(inputStream: InputStream, outputStream: OutputStream, key: SecretKey, iv: ByteArray) {

        val cipher = getAESCipher(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

        inputStream.use { inStream ->
            outputStream.use { outStream ->
                process(inStream, outStream, cipher)
            }
        }
    }

    fun decrypt(input: ByteArray, key: SecretKey, iv: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()
        decrypt(ByteArrayInputStream(input), outputStream, key, iv)
        return outputStream.toByteArray()
    }

    fun decrypt(inputFile: String, outputFile: String, key: SecretKey) {
        val inputStream = FileInputStream(inputFile)
        val version = readVersion(inputStream)
        if ("03" == version) {
            readSalt(inputStream)
        }
        val iv = readIV(inputStream)
        decrypt(inputStream, FileOutputStream(outputFile), key, iv)
    }

    fun readHeader(inputFile: String): Pair<String, ByteArray> {
        return try {
            FileInputStream(inputFile).use { inputStream ->
                val version = readVersion(inputStream)
                val salt = if ("03" == version) readSalt(inputStream) else ByteArray(0)
                Pair(version, salt)
            }
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

}