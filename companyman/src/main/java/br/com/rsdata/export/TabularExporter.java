package br.com.rsdata.export;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import br.com.rsdata.exception.ExportException;
import br.com.rsdata.service.EmpresaExportService;
import br.com.rsdata.service.RamoAtividadeExportService;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utilitário genérico para exportação de dados tabulares (uma lista de
 * cabeçalhos + uma lista de linhas de células) para os formatos CSV, XLS,
 * ODT e PDF. É reaproveitado por {@link EmpresaExportService} e
 * {@link RamoAtividadeExportService}, que apenas transformam suas
 * respectivas entidades em linhas de texto antes de delegar a geração do
 * arquivo para esta classe.
 */
public final class TabularExporter {

    private TabularExporter() {
    }

    // ---------------------------------------------------------------
    // CSV
    // ---------------------------------------------------------------

    public static byte[] paraCsv(String[] cabecalhos, List<String[]> linhas) {
        StringBuilder sb = new StringBuilder();
        // BOM UTF-8, para o Excel reconhecer acentuação corretamente ao abrir o CSV.
        sb.append('\uFEFF');

        sb.append(formatarLinhaCsv(cabecalhos)).append("\r\n");
        for (String[] linha : linhas) {
            sb.append(formatarLinhaCsv(linha)).append("\r\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String formatarLinhaCsv(String[] campos) {
        StringBuilder linha = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                linha.append(';');
            }
            linha.append(escaparCampoCsv(campos[i]));
        }
        return linha.toString();
    }

    private static String escaparCampoCsv(String valor) {
        String v = valor == null ? "" : valor;
        boolean precisaEscapar = v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r");
        if (!precisaEscapar) {
            return v;
        }
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    // ---------------------------------------------------------------
    // XLS (Apache POI - formato binário HSSF)
    // ---------------------------------------------------------------

    public static byte[] paraXls(String tituloPlanilha, String[] cabecalhos, List<String[]> linhas) {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(tituloPlanilha);

            CellStyle estiloCabecalho = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fonteCabecalho = workbook.createFont();
            fonteCabecalho.setBold(true);
            estiloCabecalho.setFont(fonteCabecalho);

            Row linhaCabecalho = sheet.createRow(0);
            for (int i = 0; i < cabecalhos.length; i++) {
                Cell celula = linhaCabecalho.createCell(i);
                celula.setCellValue(cabecalhos[i]);
                celula.setCellStyle(estiloCabecalho);
            }

            int numeroLinha = 1;
            for (String[] linha : linhas) {
                Row linhaDados = sheet.createRow(numeroLinha++);
                for (int i = 0; i < linha.length; i++) {
                    linhaDados.createCell(i).setCellValue(linha[i] == null ? "" : linha[i]);
                }
            }

            for (int i = 0; i < cabecalhos.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ExportException("Falha ao gerar arquivo XLS.", e);
        }
    }

    // ---------------------------------------------------------------
    // PDF (OpenPDF - fork mantido do iText, mesmo namespace com.lowagie.text)
    // ---------------------------------------------------------------

    public static byte[] paraPdf(String titulo, String[] cabecalhos, List<String[]> linhas) {
        Document documento = new Document(PageSize.A4.rotate(), 24, 24, 36, 36);
        
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(documento, out);
            documento.open();

            Font fonteTitulo = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph paragrafoTitulo = new Paragraph(titulo, fonteTitulo);
            paragrafoTitulo.setSpacingAfter(12);
            documento.add(paragrafoTitulo);

            PdfPTable tabela = new PdfPTable(cabecalhos.length);
            tabela.setWidthPercentage(100);

            Font fonteCabecalho = new Font(Font.HELVETICA, 10, Font.BOLD);
            for (String cabecalho : cabecalhos) {
                PdfPCell celula = new PdfPCell(new Paragraph(cabecalho, fonteCabecalho));
                celula.setBackgroundColor(new java.awt.Color(31, 41, 55));
                celula.setPadding(6);
                celula.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabela.addCell(celula);
            }

            Font fonteCelula = new Font(Font.HELVETICA, 9);
            for (String[] linha : linhas) {
                for (String valor : linha) {
                    PdfPCell celula = new PdfPCell(new Paragraph(valor == null ? "" : valor, fonteCelula));
                    celula.setPadding(5);
                    tabela.addCell(celula);
                }
            }

            documento.add(tabela);
            documento.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new ExportException("Falha ao gerar arquivo PDF.", e);
        }
    }

    // ---------------------------------------------------------------
    // ODT (construído manualmente como um pacote ODF/ZIP mínimo válido,
    // sem dependências externas — mimetype + META-INF/manifest.xml +
    // content.xml, conforme especificação OASIS ODF 1.2).
    // ---------------------------------------------------------------

    public static byte[] paraOdt(String titulo, String[] cabecalhos, List<String[]> linhas) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
                // 1) "mimetype" deve ser a primeira entrada do ZIP e NÃO pode ser comprimida
                //    (exigência da especificação ODF, usada por leitores para identificar o tipo).
                byte[] mimetypeBytes = "application/vnd.oasis.opendocument.text"
                        .getBytes(StandardCharsets.US_ASCII);
                
                ZipEntry mimetypeEntry = new ZipEntry("mimetype");
                mimetypeEntry.setMethod(ZipEntry.STORED);
                mimetypeEntry.setSize(mimetypeBytes.length);
                mimetypeEntry.setCompressedSize(mimetypeBytes.length);
                
                CRC32 crc = new CRC32();
                crc.update(mimetypeBytes);
                mimetypeEntry.setCrc(crc.getValue());
            
                zip.putNextEntry(mimetypeEntry);
                zip.write(mimetypeBytes);
                zip.closeEntry();

                // 2) META-INF/manifest.xml
                zip.putNextEntry(new ZipEntry("META-INF/manifest.xml"));
                zip.write(construirManifestoOdt().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();

                // 3) content.xml (título + tabela de dados)
                zip.putNextEntry(new ZipEntry("content.xml"));
                zip.write(construirConteudoOdt(titulo, cabecalhos, linhas).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new ExportException("Falha ao gerar arquivo ODT.", e);
        }
    }

    private static String construirManifestoOdt() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<manifest:manifest xmlns:manifest=\"urn:oasis:names:tc:opendocument:xmlns:manifest:1.0\" "
            + "manifest:version=\"1.2\">\n"
            + "  <manifest:file-entry manifest:full-path=\"/\" manifest:version=\"1.2\" "
            + "manifest:media-type=\"application/vnd.oasis.opendocument.text\"/>\n"
            + "  <manifest:file-entry manifest:full-path=\"content.xml\" manifest:media-type=\"text/xml\"/>\n"
            + "</manifest:manifest>\n";
    }

    private static String construirConteudoOdt(String titulo, String[] cabecalhos, List<String[]> linhas) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<office:document-content ")
                .append("xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" ")
                .append("xmlns:table=\"urn:oasis:names:tc:opendocument:xmlns:table:1.0\" ")
                .append("xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\" ")
                .append("office:version=\"1.2\">\n");
        xml.append("  <office:body>\n");
        xml.append("    <office:text>\n");
        xml.append("      <text:h text:outline-level=\"1\">").append(escaparXml(titulo)).append("</text:h>\n");
        xml.append("      <table:table table:name=\"Dados\">\n");
        xml.append("        <table:table-column table:number-columns-repeated=\"")
                .append(cabecalhos.length).append("\"/>\n");

        xml.append("        <table:table-row>\n");

        for (String cabecalho : cabecalhos) {
            xml.append("          <table:table-cell office:value-type=\"string\"><text:p>")
                    .append(escaparXml(cabecalho)).append("</text:p></table:table-cell>\n");
        }

        xml.append("        </table:table-row>\n");

        for (String[] linha : linhas) {
            xml.append("        <table:table-row>\n");

            for (String valor : linha) {
                xml.append("          <table:table-cell office:value-type=\"string\"><text:p>")
                        .append(escaparXml(valor == null ? "" : valor)).append("</text:p></table:table-cell>\n");
            }
            xml.append("        </table:table-row>\n");
        }

        xml.append("      </table:table>\n");
        xml.append("    </office:text>\n");
        xml.append("  </office:body>\n");
        xml.append("</office:document-content>\n");

        return xml.toString();
    }

    private static String escaparXml(String valor) {
        return valor
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
