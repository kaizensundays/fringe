package com.kaizensundays.fusion.fringe

import org.apache.maven.plugin.AbstractMojo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.security.Security
import java.util.concurrent.TimeUnit

/**
 * Created: Sunday 9/1/2024, 12:25 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
@SuppressWarnings("kotlin:S6518")
abstract class AbstractFringeMojo : AbstractMojo() {

    internal var logger: Logger = LoggerFactory.getLogger(javaClass)

    protected val keyFile = "KEY"

    private val timeoutSec = 1000L

    internal var encryptor = Encryptor()

    internal var observer = Observer()

    fun getText(): String {

        val done = observer.build()

        val text = done.get(timeoutSec, TimeUnit.SECONDS)
        require(text.isNotEmpty())

        return text
    }

    fun getBase64Key(salt: ByteArray): String {

        var base64Key = System.getProperty("key", "")

        if (base64Key.isNullOrBlank()) {

            val text = getText()

            base64Key = encryptor.generateBase64Key(text, salt)
        }

        return base64Key
    }

    fun getBase64Key(inputFile: String): String {

        var base64Key = System.getProperty("key", "")

        if (base64Key.isNullOrBlank()) {

            val text = getText()

            val (version, salt) = encryptor.readHeader(inputFile)

            base64Key = when (version) {
                "03" -> encryptor.generateBase64Key(text, salt)
                "01" -> encryptor.sha256(text)
                else -> error("Unexpected version: $version")
            }
        }

        return base64Key
    }

    abstract fun doExecute()

    override fun execute() {
        try {
            val t0 = System.currentTimeMillis()

            Security.addProvider(BouncyCastleProvider())

            doExecute()

            val t1 = System.currentTimeMillis()
            logger.info("Done in {} ms", t1 - t0)
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }
}