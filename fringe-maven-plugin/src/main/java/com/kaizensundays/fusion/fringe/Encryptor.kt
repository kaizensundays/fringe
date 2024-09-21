package com.kaizensundays.fusion.fringe

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
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created: Sunday 9/1/2024, 12:22 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
@Suppress("PrivatePropertyName")
class Encryptor {

    private val beginString = "Fringe"

    private val version = 1

    private val AES_BLOCK_SIZE = 16
    private val NUMBER_OF_BLOCKS = 1024
    private val KEY_SIZE_BITS = 256
    private val IV_SIZE = 16
    private val HEADER_SIZE = 16

    private val chars = ('0'..'9') + ('A'..'Z') + ('a'..'z')

    var progressCounterTerm = 1000

    fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(KEY_SIZE_BITS)
        return keyGen.generateKey()
    }

    fun writeKey(key: SecretKey, keyFile: String) {
        val base64 = Base64.getEncoder().encodeToString(key.encoded)
        File(keyFile).writeText(base64)
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

    fun header(beginString: String, version: Int, iv: ByteArray): String {
        val s = ("%s%02d").format(beginString, version)
        return ("%-16s%s").format(s, String(iv))
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

    private fun encrypt(inputStream: InputStream, outputStream: OutputStream, key: SecretKey, iv: ByteArray) {

        val cipher = getAESCipher(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))

        inputStream.use { inStream ->
            outputStream.use { outStream ->
                if (outputStream is FileOutputStream) {
                    val header = header(beginString, version, iv)
                    outStream.write(header.toByteArray())
                }
                process(inStream, outStream, cipher)
            }
        }
    }

    fun encrypt(input: ByteArray, key: SecretKey, iv: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()
        encrypt(ByteArrayInputStream(input), outputStream, key, iv)
        return outputStream.toByteArray()
    }

    fun encrypt(inputFile: String, outputFile: String, key: SecretKey, iv: ByteArray) {
        encrypt(FileInputStream(inputFile), FileOutputStream(outputFile), key, iv)
    }

    private fun readIV(inputStream: FileInputStream): ByteArray {
        val headerBytes = inputStream.readNBytes(HEADER_SIZE)
        val header = String(headerBytes)
        val version = header.substring(beginString.length, beginString.length + 2)
        println("version=$version")
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
        val iv = readIV(inputStream)
        decrypt(inputStream, FileOutputStream(outputFile), key, iv)
    }

}