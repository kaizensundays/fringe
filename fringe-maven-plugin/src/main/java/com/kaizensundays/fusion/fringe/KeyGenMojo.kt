package com.kaizensundays.fusion.fringe

import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo

/**
 * Created: Sunday 8/25/2024, 12:30 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
@Mojo(name = "key-gen", defaultPhase = LifecyclePhase.NONE)
class KeyGenMojo : AbstractFringeMojo() {

    override fun doExecute() {

        val key = encryptor.generateKey()

        encryptor.writeKey(key, keyFile)
    }

}