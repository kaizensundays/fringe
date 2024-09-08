package com.kaizensundays.fusion.fringe

import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo

/**
 * Created: Saturday 9/7/2024, 12:41 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
@Mojo(name = "decrypt", defaultPhase = LifecyclePhase.NONE)
class DecryptMojo : AbstractFringeMojo() {

    override fun doExecute() {

        val inputFile = System.getProperty("i", "")
        require(inputFile.isNotEmpty())

        val outputFile = System.getProperty("o", "")
        require(outputFile.isNotEmpty())

        val key = encryptor.readKey(keyFile)

        encryptor.decrypt(inputFile, outputFile, key)
    }

}