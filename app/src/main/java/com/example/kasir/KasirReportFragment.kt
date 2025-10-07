package com.example.kasir

import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kasir.databinding.FragmentKasirReportBinding
import java.io.File
import java.io.FileOutputStream

class KasirReportFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_kasir_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnExport = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnExportLaporan)
        val tvTotalTransaksi = view.findViewById<android.widget.TextView>(R.id.tvTotalTransaksiLaporan)
        val tvTotalOmzet = view.findViewById<android.widget.TextView>(R.id.tvTotalOmzetLaporan)
        btnExport.setOnClickListener {
            exportLaporanToPdf(
                tvTotalTransaksi.text.toString(),
                tvTotalOmzet.text.toString()
            )
        }
    }

    private fun exportLaporanToPdf(totalTransaksi: String, totalOmzet: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 400, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()
        paint.textSize = 16f
        canvas.drawText("Laporan Penjualan", 80f, 40f, paint)
        paint.textSize = 12f
        canvas.drawText("Total Transaksi: $totalTransaksi", 40f, 80f, paint)
        canvas.drawText("Total Omzet: $totalOmzet", 40f, 110f, paint)
        pdfDocument.finishPage(page)
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LaporanKasir.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(requireContext(), "PDF berhasil disimpan di Download", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal menyimpan PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
        pdfDocument.close()
    }

    // Tidak perlu onDestroyView untuk binding manual
}
