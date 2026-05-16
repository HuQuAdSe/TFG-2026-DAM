package org.example.voluntariadomadrid.utils;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PdfGenerator {

    public static boolean generarCertificado(
            String nombreOrganizacion,
            String nombreVoluntario,
            String rutaDestino) {

        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(rutaDestino));
            doc.open();

            doc.add(new Paragraph("La entidad " + nombreOrganizacion
                    + " certifica que la persona " + nombreVoluntario
                    + " ha realizado el voluntariado."));

            doc.add(new Paragraph("Fecha de emision: "
                    + new SimpleDateFormat("dd/MM/yyyy").format(new Date())));

            doc.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}