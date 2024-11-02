package com.kaizensundays.fusion.fringe

import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo

/**
 * Created: Sunday 9/1/2024, 12:25 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
@Mojo(name = "encrypt", defaultPhase = LifecyclePhase.NONE)
class EncryptMojo : AbstractFringeMojo() {

    override fun doExecute() {

        val inputFile = System.getProperty("i", "")
        require(inputFile.isNotEmpty())

        val outputFile = System.getProperty("o", "")
        require(outputFile.isNotEmpty())

        val base64Key = getBase64Key()
        require(base64Key.isNotEmpty())

        val key = encryptor.getKey(base64Key)
        val iv = encryptor.getRandomBytes(16)

        encryptor.encrypt(inputFile, outputFile, key, iv)
    }

}