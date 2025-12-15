package com.floveit.floveitcontrol.filetransfer

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

object FileSender {
    suspend fun sendFile(
        host: String,
        port: Int,
        bytes: ByteArray,
        displayName: String,
        mime: String
    ) = withContext(Dispatchers.IO) {

        val selector = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selector).tcp().connect(host, port)

        try {
            val w = socket.openWriteChannel(autoFlush = false)
            val r = socket.openReadChannel()

            // header
            w.writeStringUtf8("FILE $displayName|$mime|${bytes.size}\n")
            // payload
            w.writeFully(bytes, 0, bytes.size)
            w.flush()

            // wait for server ack
            val ack = withTimeout(10_000L) { r.readUTF8Line() }
            if (ack != "OK") throw IllegalStateException("File send failed, server replied: $ack")

        } finally {
            socket.close()
            selector.close()
        }
    }
}
