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
abstract class AbstractFringeMojo : AbstractMojo() {

    internal var logger: Logger = LoggerFactory.getLogger(javaClass)

    protected val keyFile = "KEY"

    private val timeoutSec = 100L

    internal var encryptor = Encryptor()

    fun getBase64Key(): String {

        var base64Key = System.getProperty("key", "")

        if (base64Key == null) {
            val done = Observer().build()

            val text = done.get(timeoutSec, TimeUnit.SECONDS)

            require(text.isNotEmpty())

            base64Key = encryptor.sha256(text)
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