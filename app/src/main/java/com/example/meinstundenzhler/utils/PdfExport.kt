package com.example.meinstundenzhler.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.meinstundenzhler.data.MonthlyList
import com.example.meinstundenzhler.data.MonthlyListRepository
import com.example.meinstundenzhler.data.Shift
import com.example.meinstundenzhler.data.ShiftRepository
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/* ---------- Öffnen (Cache) ODER Speichern (SAF) ---------- */

suspend fun cacheMonthPdf(
    context: Context,
    listId: Long,
    monthlyRepo: MonthlyListRepository,
    shiftRepo: ShiftRepository
): Uri {
    val monthly = monthlyRepo.getById(listId).first() ?: error("Liste nicht gefunden")
    val shifts = shiftRepo.getByMonthlyList(listId).first().sortedBy { it.startEpochMillis }

    val pdf = PdfDocument()
    drawMonthReport(pdf, monthly, shifts)

    val fileName = "Abrechnung_${monthName(monthly.monthIndex)}_${monthly.year}.pdf"
    val outFile = File(context.cacheDir, fileName)
    outFile.outputStream().use { pdf.writeTo(it) }
    pdf.close()

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        outFile
    )
}

suspend fun exportMonthAsPdf(
    context: Context,
    uri: Uri,
    listId: Long,
    monthlyRepo: MonthlyListRepository,
    shiftRepo: ShiftRepository
) {
    val monthly = monthlyRepo.getById(listId).first() ?: error("Liste nicht gefunden")
    val shifts = shiftRepo.getByMonthlyList(listId).first().sortedBy { it.startEpochMillis }

    val pdf = PdfDocument()
    drawMonthReport(pdf, monthly, shifts)

    context.contentResolver.openOutputStream(uri).use { out ->
        pdf.writeTo(out!!)
    }
    pdf.close()
}

/* -------------------- Zeichnen -------------------- */

private fun drawMonthReport(
    pdf: PdfDocument,
    monthly: MonthlyList,
    shifts: List<Shift>
) {
    // A4 @ 72dpi
    val pageW = 595
    val pageH = 842
    val margin = 36f
    val line = 16f
    val bottom = pageH - margin

    // Spaltenpositionen
    val colDate = margin
    val colFrom = margin + 110
    val colTo   = colFrom + 60
    val colDur  = colTo + 60
    val colAmtRight = pageW - margin   // rechtsbündig

    // Pinsel
    val text = Paint().apply { isAntiAlias = true; textSize = 12f }
    val bold = Paint(text).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
    val section = Paint(bold).apply { textSize = 14f }
    val divider = Paint(text).apply { color = Color.LTGRAY; strokeWidth = 0.7f }

    // Formatter
    val fmtCurr = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    val zone = ZoneId.systemDefault()
    val dfDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    // Summen (Pause wird berücksichtigt, aber NICHT gedruckt)
    val minutes = shifts.sumOf { s -> computeDurationMinutes(s.startEpochMillis, s.endEpochMillis, s.breakMinutes) }
    val earned = minutes / 60.0 * monthly.hourlyWage
    val totalWithCarry = earned + monthly.previousDebt
    val carryThisMonth = monthly.monthlyIncome?.let { totalWithCarry - it }

    // Lokaler Seitenzähler
    var pageIndex = 0
    fun newPage(): PdfDocument.Page {
        pageIndex += 1
        val info = PdfDocument.PageInfo.Builder(pageW, pageH, pageIndex).create()
        return pdf.startPage(info)
    }
    fun Paint.drawRight(textStr: String, xRight: Float, y: Float, canvas: android.graphics.Canvas) {
        canvas.drawText(textStr, xRight - measureText(textStr), y, this)
    }

    var page = newPage()
    var c = page.canvas
    var y = margin

    // --- Header: NUR Titel (keine Kennzahlen) ---
    fun drawHeader() {
        c.drawText(
            "Abrechnung – ${monthName(monthly.monthIndex)} ${monthly.year}",
            margin, y, bold.apply { textSize = 18f }
        )
        bold.textSize = 12f
        y += line * 1.2f
    }

    // --- Tabellenkopf: direkt unter dem Titel ---
    fun drawTableHeader() {
        c.drawText("Datum", colDate, y, bold)
        c.drawText("Von",   colFrom, y, bold)
        c.drawText("Bis",   colTo,   y, bold)
        c.drawText("Dauer", colDur,  y, bold)
        bold.drawRight("Betrag", colAmtRight, y, c)
        y += line * 0.8f
        c.drawLine(margin, y, pageW - margin, y, text)
        y += line
    }

    // — Helfer für Tabellenumbruch
    fun checkPageForTable(lines: Float = 1f) {
        if (y > bottom - line * lines) {
            pdf.finishPage(page)
            page = newPage()
            c = page.canvas
            y = margin
            drawHeader()
            drawTableHeader()
        }
    }

    // — Helfer: K/V-Zeile in der Zusammenfassung
    fun drawKV(label: String, value: String, emphasize: Boolean = false, valueColor: Int? = null) {
        val lp = if (emphasize) bold else text
        val vp = Paint(if (emphasize) bold else text).apply { valueColor?.let { color = it } }
        c.drawText(label, margin, y, lp)
        vp.drawRight(value, colAmtRight, y, c)
        y += line
    }

    // Seite beginnen
    drawHeader()
    drawTableHeader()

    // --- Tabellenzeilen (ohne Pause/Notiz)
    shifts.forEach { s ->
        val start = Instant.ofEpochMilli(s.startEpochMillis).atZone(zone)
        val end   = Instant.ofEpochMilli(s.endEpochMillis).atZone(zone)
        val dMin  = computeDurationMinutes(s.startEpochMillis, s.endEpochMillis, s.breakMinutes)
        val amount = dMin / 60.0 * monthly.hourlyWage

        checkPageForTable()
        c.drawText(dfDate.format(start), colDate, y, text)
        c.drawText("%02d:%02d".format(start.hour, start.minute), colFrom, y, text)
        c.drawText("%02d:%02d".format(end.hour,   end.minute),   colTo,   y, text)
        c.drawText(formatHours(dMin), colDur, y, text)
        text.drawRight(fmtCurr.format(amount), colAmtRight, y, c)
        y += line
    }

    // --- Zusammenfassung unter der Tabelle ---
    fun ensureSpace(linesNeeded: Float) {
        if (y > bottom - line * linesNeeded) {
            pdf.finishPage(page)
            page = newPage()
            c = page.canvas
            y = margin
            drawHeader()
        } else {
            y += line * 0.6f
        }
    }

    fun drawSummary() {
        ensureSpace(7f)
        c.drawLine(margin, y, pageW - margin, y, divider)
        y += line * 0.8f
        c.drawText("Zusammenfassung", margin, y, section)
        y += line

        drawKV("Übertrag Vormonat", "%+.2f €".format(monthly.previousDebt))
        drawKV("Arbeitszeit", "${formatHours(minutes)} h")
        drawKV("Verdienst", fmtCurr.format(earned))
        drawKV("Summe inkl. Übertrag", fmtCurr.format(totalWithCarry), emphasize = true)

        monthly.monthlyIncome?.let { inc ->
            drawKV("Monatlicher Verdienst", fmtCurr.format(inc))
            val carry = carryThisMonth ?: 0.0
            val color = if (carry < 0) Color.RED else Color.rgb(0, 120, 0)
            drawKV("Übertrag (dieser Monat)", "%+.2f €".format(carry), emphasize = true, valueColor = color)
        }
    }

    drawSummary()
    pdf.finishPage(page)
}
