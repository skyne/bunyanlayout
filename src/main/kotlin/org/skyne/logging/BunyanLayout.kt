package org.skyne.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.LayoutBase
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class BunyanLayout : LayoutBase<ILoggingEvent?>() {
    companion object {
        private var BUNYAN_LEVEL: MutableMap<Level, Int>? = null
        private val GSON = GsonBuilder().disableHtmlEscaping().create()
        private fun formatAsIsoUTCDateTime(timeStamp: Long): String {
            val instant = Instant.ofEpochMilli(timeStamp)
            return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT)
        }

        init {
            BUNYAN_LEVEL = HashMap()
            (BUNYAN_LEVEL as HashMap<Level, Int>)[Level.ERROR] = 50
            (BUNYAN_LEVEL as HashMap<Level, Int>)[Level.WARN] = 40
            (BUNYAN_LEVEL as HashMap<Level, Int>)[Level.INFO] = 30
            (BUNYAN_LEVEL as HashMap<Level, Int>)[Level.DEBUG] = 20
            (BUNYAN_LEVEL as HashMap<Level, Int>)[Level.TRACE] = 10
        }
    }

    override fun doLayout(event: ILoggingEvent?): String? {
        if (event != null) {
            val jsonEvent = JsonObject()
            jsonEvent.addProperty("v", 0)
            jsonEvent.addProperty("level", BUNYAN_LEVEL!![event.level])
            jsonEvent.addProperty("levelStr", event.level.toString())
            jsonEvent.addProperty("name", event.loggerName)
            try {
                jsonEvent.addProperty("hostname", InetAddress.getLocalHost().hostName)
            } catch (e: UnknownHostException) {
                jsonEvent.addProperty("hostname", "unkown")
            }
            jsonEvent.addProperty("pid", getThreadId(event))
            jsonEvent.addProperty("time", formatAsIsoUTCDateTime(event.timeStamp))
            jsonEvent.addProperty("msg", event.formattedMessage)
            if (event.level.isGreaterOrEqual(Level.ERROR) && event.throwableProxy != null) {
                val jsonError = JsonObject()
                val e = event.throwableProxy
                jsonError.addProperty("message", e.message)
                jsonError.addProperty("name", e.className)
                val stackTrace = StringBuilder()
                val stackTraceElementProxyArray = e.stackTraceElementProxyArray
                for (element in stackTraceElementProxyArray) {
                    stackTrace.append(element.stackTraceElement.toString()).append('\n')
                }
                jsonError.addProperty("stack", stackTrace.toString())
                jsonEvent.add("err", jsonError)
            }
            return """
            ${GSON.toJson(jsonEvent)}

            """.trimIndent()
        }

        return null;
    }

    private fun getThreadId(event: ILoggingEvent): Long {
        val threadId: Long
        val threadName = event.threadName
        threadId = if (Thread.currentThread().name == threadName) {
            Thread.currentThread().id
        } else {
            try {
                threadName.substring(threadName.lastIndexOf('-')).toLong()
            } catch (e: NumberFormatException) {
                0
            }
        }
        return threadId
    }
}
