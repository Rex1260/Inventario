package com.example.inventario.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.inventario.model.Equipo
import com.example.inventario.model.Prestamo
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ContratoGenerator {

    fun generarContratoPDF(
        context: Context,
        prestamo: Prestamo,
        equipos: List<Equipo>,
        firma: Bitmap?
    ): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val textPaint = Paint().apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val boldPaint = Paint().apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val titlePaint = Paint().apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val margin = 50f
        val contentWidth = 495f
        var pageNumber = 1
        
        // Función para crear una nueva página
        fun startNewPage(): Pair<PdfDocument.Page, Canvas> {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber++).create()
            val page = pdfDocument.startPage(pageInfo)
            return Pair(page, page.canvas)
        }

        var (currentPage, canvas) = startNewPage()
        var y = 60f

        // Header
        canvas.drawText("DIE CUT SOLUTIONS S. DE R. L. DE C. V.", margin, y, titlePaint)
        y += 20f
        canvas.drawText("CONTRATO DE COMODATO - FOLIO: ${prestamo.folio}", margin, y, boldPaint)
        y += 30f

        val textoIntroduccion = "CONTRATO DE COMODATO QUE CELEBRAN, POR UNA PARTE, DIE CUT SOLUTIONS S. DE R. L. DE C. V., COMO COMODANTE (EN LO SUCESIVO DENOMINADA “DIE CUT”), Y, POR LA OTRA PARTE, LA PERSONA CUYO NOMBRE SE INDICA AL CALCE (EN LO SUCESIVO DESIGNADA LA “COMODATARIA”), AL TENOR DE LAS SIGUIENTES:"
        
        val clausulas = listOf(
            "C L Á U S U L A S:",
            "PRIMERA. “DIE CUT” en este acto entrega en comodato a el “Comodatario” y ésta recibe el “Equipo” que se identifica y detalla abajo en este mismo instrumento. Queda expresamente convenido que la propiedad del “Equipo” es y continuará siendo de “DIE CUT” durante la vigencia de este contrato.",
            "SEGUNDA. El “Comodatario” se obliga a: 1. Realizar el uso del “Equipo” acorde con su naturaleza. 2. Cerciorarse que el “Equipo” no sea operado por personal no calificado. 3. Poner toda la diligencia necesaria en su conservación. 4. Realizar a su costo todos los gastos de reparación correctiva o mantenimiento preventivo por negligencia. 5. Abstenerse de efectuar modificaciones al “Equipo”.",
            "TERCERA. El “Comodatario” será responsable de cualquier pérdida o daño que sufriere el “Equipo”, cualquiera que sea la causa a partir de su recepción. En caso de pérdida total, se obliga a pagar el valor de adquisición del mismo.",
            "CUARTA. El “Comodatario” se compromete a no retirar de las instalaciones el “Equipo” sin antes recibir autorización escrita de “DIE CUT”. No podrá conceder a terceros el uso o goce del mismo.",
            "QUINTA. El “Comodatario” se obliga a devolver a “DIE CUT” el “Equipo” en el momento que se le solicite o al término de su relación laboral. Será responsable de daños y perjuicios por retraso en la devolución.",
            "SEXTA. Para la interpretación y cumplimiento de este contrato, las partes señalan como sus domicilios los indicados al calce de este instrumento.",
            "SÉPTIMA. Para cualquier controversia, las partes se someten a la jurisdicción de los tribunales competentes de la ciudad de Guadalajara, Jalisco."
        )

        // Dibujar Introducción
        y = drawWrappedText(textoIntroduccion, margin, y, contentWidth, canvas, textPaint, 842f)

        // Dibujar Cláusulas
        clausulas.forEach { clausula ->
            if (y > 750f) {
                pdfDocument.finishPage(currentPage)
                val newPage = startNewPage()
                currentPage = newPage.first
                canvas = newPage.second
                y = 60f
            }
            y += 10f
            y = drawWrappedText(clausula, margin, y, contentWidth, canvas, if(clausula.startsWith("C L")) boldPaint else textPaint, 842f)
        }

        y += 20f
        canvas.drawText("EQUIPOS EN RESGUARDO:", margin, y, boldPaint)
        y += 15f

        equipos.forEach { equipo ->
            if (y > 780f) {
                pdfDocument.finishPage(currentPage)
                val newPage = startNewPage()
                currentPage = newPage.first
                canvas = newPage.second
                y = 60f
            }
            canvas.drawText("• ${equipo.noInventario} - ${equipo.nombre} (Serie: ${equipo.numeroSerie ?: "S/N"})", margin + 10, y, textPaint)
            y += 12f
        }

        y += 30f

        // Espacio para firma
        if (y > 650f) {
            pdfDocument.finishPage(currentPage)
            val newPage = startNewPage()
            currentPage = newPage.first
            canvas = newPage.second
            y = 60f
        }

        canvas.drawText("ACEPTO LOS TÉRMINOS Y RECIBO DE CONFORMIDAD:", margin, y, boldPaint)
        y += 20f

        firma?.let {
            val scaledFirma = Bitmap.createScaledBitmap(it, 180, 100, true)
            canvas.drawBitmap(scaledFirma, margin, y, null)
            y += 110f
        }

        canvas.drawText("__________________________________", margin, y, textPaint)
        y += 12f
        canvas.drawText(prestamo.nombreComodatario?.uppercase() ?: "FIRMA DEL COMODATARIO", margin, y, boldPaint)
        y += 10f
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Fecha de firma: $dateStr", margin, y, textPaint)
        
        y += 30f
        canvas.drawText("Este documento tiene validez digital y fue generado por el Sistema de Inventarios DIE CUT SOLUTIONS.", margin, y, textPaint.apply { textSize = 7f; color = Color.GRAY })

        pdfDocument.finishPage(currentPage)

        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, "Contrato_${prestamo.folio}_${System.currentTimeMillis()}.pdf")
        
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            pdfDocument.close()
            null
        }
    }

    private fun drawWrappedText(text: String, x: Float, y: Float, maxWidth: Float, canvas: Canvas, paint: Paint, pageHeight: Float): Float {
        var currentY = y
        val words = text.split(" ")
        var line = ""
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) <= maxWidth) {
                line = testLine
            } else {
                canvas.drawText(line, x, currentY, paint)
                line = word
                currentY += paint.textSize + 4f
            }
        }
        canvas.drawText(line, x, currentY, paint)
        return currentY + paint.textSize + 4f
    }
}
