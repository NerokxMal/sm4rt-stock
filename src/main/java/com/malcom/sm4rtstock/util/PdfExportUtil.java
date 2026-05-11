package com.malcom.sm4rtstock.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class PdfExportUtil {

    private PdfExportUtil() {}

    public static byte[] generarTabla(String titulo, List<String> headers, List<List<String>> rows) {
        List<String> lines = new ArrayList<>();
        lines.add(titulo + " | Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        lines.add("");
        lines.add(String.join(" | ", headers));
        lines.add("-".repeat(120));

        int maxRows = 40;
        int limite = Math.min(rows.size(), maxRows);
        for (int i = 0; i < limite; i++) {
            lines.add(String.join(" | ", rows.get(i)));
        }
        if (rows.size() > maxRows) {
            lines.add("... (" + (rows.size() - maxRows) + " filas adicionales no mostradas en este PDF)");
        }

        return construirPdf(lines);
    }

    private static byte[] construirPdf(List<String> lines) {
        StringBuilder streamContent = new StringBuilder();
        streamContent.append("BT\n/F1 10 Tf\n");

        int y = 800;
        for (String rawLine : lines) {
            if (y < 40) break;
            String line = limitar(rawLine, 120);
            streamContent.append("1 0 0 1 40 ").append(y).append(" Tm\n");
            streamContent.append("(").append(escape(line)).append(") Tj\n");
            y -= 16;
        }
        streamContent.append("ET\n");

        byte[] contentBytes = streamContent.toString().getBytes(StandardCharsets.ISO_8859_1);

        List<String> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>");
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");

        String streamObject = "<< /Length " + contentBytes.length + " >>\nstream\n"
                + new String(contentBytes, StandardCharsets.ISO_8859_1) + "endstream";
        objects.add(streamObject);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, "%PDF-1.4\n");

        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            write(out, (i + 1) + " 0 obj\n");
            write(out, objects.get(i));
            write(out, "\nendobj\n");
        }

        int xrefOffset = out.size();
        write(out, "xref\n");
        write(out, "0 " + (objects.size() + 1) + "\n");
        write(out, "0000000000 65535 f \n");
        for (Integer offset : offsets) {
            write(out, String.format("%010d 00000 n \n", offset));
        }

        write(out, "trailer\n");
        write(out, "<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n");
        write(out, "startxref\n");
        write(out, String.valueOf(xrefOffset));
        write(out, "\n%%EOF");

        return out.toByteArray();
    }

    private static void write(ByteArrayOutputStream out, String value) {
        out.writeBytes(value.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private static String limitar(String value, int maxLength) {
        if (value == null) return "";
        if (value.length() <= maxLength) return value;
        return value.substring(0, maxLength - 3) + "...";
    }
}
