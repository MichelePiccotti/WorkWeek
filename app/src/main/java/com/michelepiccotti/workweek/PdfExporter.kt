package com.michelepiccotti.workweek

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    fun exportSummary(context: Context, text: String): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint().apply { textSize = 14f }

        val lines = text.split("\n")
        var y = 40f
        for (line in lines) {
            canvas.drawText(line, 20f, y, paint)
            y += 22f
        }

        document.finishPage(page)

        val file = File(context.getExternalFilesDir(null), "riepilogo.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        return file
    }
}
