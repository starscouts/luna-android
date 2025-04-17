package dev.equestria.luna

import java.io.BufferedReader
import java.io.InputStreamReader

object MemoryInfo {
    private fun getValue(name: String): Double {
        val lines = mutableListOf<String>()
        val process = Runtime.getRuntime().exec("cat /proc/meminfo")
        process.waitFor()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line = reader.readLine()
        while (line != null) {
            lines.add(line)
            line = reader.readLine()
        }

        lines.toTypedArray().forEach {
            if (it.startsWith("$name:")) {
                val regex = """ +""".toRegex()
                val parts = regex.replace(it, " ").split(" ")
                return parts[1].toDouble()
            }
        }

        return 0.0
    }

    val MemTotal: Double
        get() = getValue("MemTotal")

    val MemFree: Double
        get() = getValue("MemFree")

    val MemAvailable: Double
        get() = getValue("MemAvailable")

    val Buffers: Double
        get() = getValue("Buffers")

    val Cached: Double
        get() = getValue("Cached")

    val SwapCached: Double
        get() = getValue("SwapCached")

    val Active: Double
        get() = getValue("Active")

    val Inactive: Double
        get() = getValue("Inactive")

    val ActiveAnon: Double
        get() = getValue("Active(anon)")

    val InactiveAnon: Double
        get() = getValue("Inactive(anon)")

    val ActiveFile: Double
        get() = getValue("Active(file)")

    val InactiveFile: Double
        get() = getValue("Inactive(file)")

    val Unevictable: Double
        get() = getValue("Unevictable")

    val Mlocked: Double
        get() = getValue("Mlocked")

    val SwapTotal: Double
        get() = getValue("SwapTotal")

    val SwapFree: Double
        get() = getValue("SwapFree")

    val Dirty: Double
        get() = getValue("Dirty")

    val Writeback: Double
        get() = getValue("Writeback")

    val AnonPages: Double
        get() = getValue("AnonPages")

    val Mapped: Double
        get() = getValue("Mapped")

    val Shmem: Double
        get() = getValue("Shmem")

    val KReclaimable: Double
        get() = getValue("KReclaimable")

    val Slab: Double
        get() = getValue("Slab")

    val SReclaimable: Double
        get() = getValue("SReclaimable")

    val SUnreclaim: Double
        get() = getValue("SUnreclaim")

    val KernelStack: Double
        get() = getValue("KernelStack")

    val ShadowCallStack: Double
        get() = getValue("ShadowCallStack")

    val PageTables: Double
        get() = getValue("PageTables")

    val NFSUnstable: Double
        get() = getValue("NFS_Unstable")

    val Bounce: Double
        get() = getValue("Bounce")

    val WritebackTmp: Double
        get() = getValue("WritebackTmp")

    val CommitLimit: Double
        get() = getValue("CommitLimit")

    val CommittedAS: Double
        get() = getValue("Committed_AS")

    val VmallocTotal: Double
        get() = getValue("VmallocTotal")

    val VmallocUsed: Double
        get() = getValue("VmallocUsed")

    val VmallocChunk: Double
        get() = getValue("VmallocChunk")

    val Percpu: Double
        get() = getValue("Percpu")

    val AnonHugePages: Double
        get() = getValue("AnonHugePages")

    val ShmemHugePages: Double
        get() = getValue("ShmemHugePages")

    val ShmemPmdMapped: Double
        get() = getValue("ShmemPmdMapped")

    val FileHugePages: Double
        get() = getValue("FileHugePages")

    val FilePmdMapped: Double
        get() = getValue("FilePmdMapped")

    val CmaTotal: Double
        get() = getValue("CmaTotal")

    val CmaFree: Double
        get() = getValue("CmaFree")
}