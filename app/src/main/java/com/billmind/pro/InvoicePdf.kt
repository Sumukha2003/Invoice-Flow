package com.billmind.pro

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object InvoicePdf {
    fun create(context: Context, invoice: Invoice, qr: Bitmap): File {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val c = page.canvas
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        p.color = Color.rgb(29, 158, 117)
        c.drawRect(0f, 0f, 595f, 92f, p)
        p.color = Color.WHITE
        p.textSize = 28f
        p.isFakeBoldText = true
        c.drawText("Bill Mind Pro", 36f, 42f, p)
        p.textSize = 15f
        p.isFakeBoldText = false
        c.drawText("GST Invoice ${invoice.number}", 36f, 68f, p)

        p.color = Color.rgb(25, 32, 29)
        p.textSize = 16f
        p.isFakeBoldText = true
        c.drawText("Customer", 36f, 130f, p)
        p.isFakeBoldText = false
        p.textSize = 14f
        c.drawText(invoice.customerName, 36f, 154f, p)
        c.drawText("Date: ${invoice.createdAt.asDate()}", 380f, 130f, p)
        c.drawText("Due: ${invoice.dueAt.asDate()}", 380f, 154f, p)

        p.isFakeBoldText = true
        c.drawText("Item", 36f, 205f, p)
        c.drawText("Qty", 285f, 205f, p)
        c.drawText("Rate", 340f, 205f, p)
        c.drawText("GST", 420f, 205f, p)
        c.drawText("Total", 490f, 205f, p)
        p.isFakeBoldText = false
        var y = 235f
        invoice.lines.forEach {
            c.drawText(it.itemName.take(28), 36f, y, p)
            c.drawText(it.quantity.toString(), 292f, y, p)
            c.drawText(it.price.money(), 330f, y, p)
            c.drawText("${it.gstRate.toInt()}%", 425f, y, p)
            c.drawText((it.price * it.quantity * (1 + it.gstRate / 100)).money(), 480f, y, p)
            y += 28f
        }

        y += 28f
        p.isFakeBoldText = true
        c.drawText("Subtotal", 360f, y, p)
        c.drawText(invoice.subtotal.money(), 470f, y, p)
        y += 26f
        c.drawText("GST", 360f, y, p)
        c.drawText(invoice.gst.money(), 470f, y, p)
        y += 30f
        p.textSize = 18f
        c.drawText("Grand Total", 330f, y, p)
        c.drawText(invoice.total.money(), 470f, y, p)

        val scaledQr = Bitmap.createScaledBitmap(qr, 118, 118, false)
        c.drawBitmap(scaledQr, 36f, 650f, p)
        p.textSize = 13f
        p.isFakeBoldText = false
        c.drawText("Scan QR to verify invoice", 36f, 790f, p)

        doc.finishPage(page)
        val file = File(context.cacheDir, "${invoice.number}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND)
            .setType("application/pdf")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share invoice PDF"))
    }
}
