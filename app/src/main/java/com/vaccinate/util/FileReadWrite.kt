package com.vaccinate.util

import java.io.File

class FileReadWrite(private val fileName: String) {
    private val file = File(fileName)
    fun write(data: String) {
        file.writeText(data)
    }

    fun append(data: String) {
        file.appendText(data+"\n")
    }

    fun read(): String {
        return file.readText(Charsets.UTF_8)
    }

    fun delete() {
        file.delete()
    }
}