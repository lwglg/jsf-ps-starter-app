package br.com.rsdata.export;

import br.com.rsdata.exception.ExportException;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Utilitário genérico para exportação de dados tabulares (uma lista de
 * cabeçalhos + uma lista de linhas de células) para os formatos CSV, XLS,
 * ODT e PDF. É reaproveitado por {@link EmpresaExportService} e
 * {@link RamoAtividadeExportService}, que apenas transformam suas
 * respectivas entidades em linhas de texto antes de delegar a geração do
 * arquivo para esta classe.
 *
 * Todos os formatos incluem o número de registros e a data/hora de
 * geração ({@link MetadadosExportacao}). Em PDF e ODT — onde o conceito de
 * "página" existe de fato — também é incluído um cabeçalho com o logo da
 * empresa e um rodapé com paginação ("Página X de Y").
 */
public final class TabularExporter {

    private TabularExporter() {
    }

    // ---------------------------------------------------------------
    // CSV
    // ---------------------------------------------------------------

    public static byte[] paraCsv(MetadadosExportacao metadados, String[] cabecalhos, List<String[]> linhas) {
        StringBuilder sb = new StringBuilder();
        // BOM UTF-8, para o Excel reconhecer acentuação corretamente ao abrir o CSV.
        sb.append('\uFEFF');

        sb.append(formatarLinhaCsv(new String[]{metadados.getTitulo()})).append("\r\n");
        sb.append(formatarLinhaCsv(new String[]{metadados.getTextoGeracao()})).append("\r\n");
        sb.append(formatarLinhaCsv(new String[]{metadados.getTextoTotalRegistros()})).append("\r\n");
        sb.append("\r\n");

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

    public static byte[] paraXls(MetadadosExportacao metadados, String tituloPlanilha,
                                  String[] cabecalhos, List<String[]> linhas) {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(tituloPlanilha);

            CellStyle estiloTitulo = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fonteTitulo = workbook.createFont();
            fonteTitulo.setBold(true);
            fonteTitulo.setFontHeightInPoints((short) 13);
            estiloTitulo.setFont(fonteTitulo);

            CellStyle estiloMetadados = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fonteMetadados = workbook.createFont();
            fonteMetadados.setItalic(true);
            fonteMetadados.setColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_50_PERCENT.getIndex());
            estiloMetadados.setFont(fonteMetadados);

            int linhaAtual = 0;
            Row linhaTitulo = sheet.createRow(linhaAtual++);
            Cell celulaTitulo = linhaTitulo.createCell(0);
            celulaTitulo.setCellValue(metadados.getTitulo());
            celulaTitulo.setCellStyle(estiloTitulo);

            Row linhaGeracao = sheet.createRow(linhaAtual++);
            Cell celulaGeracao = linhaGeracao.createCell(0);
            celulaGeracao.setCellValue(metadados.getTextoGeracao());
            celulaGeracao.setCellStyle(estiloMetadados);

            Row linhaTotal = sheet.createRow(linhaAtual++);
            Cell celulaTotal = linhaTotal.createCell(0);
            celulaTotal.setCellValue(metadados.getTextoTotalRegistros());
            celulaTotal.setCellStyle(estiloMetadados);

            linhaAtual++; // linha em branco

            CellStyle estiloCabecalho = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fonteCabecalho = workbook.createFont();
            fonteCabecalho.setBold(true);
            estiloCabecalho.setFont(fonteCabecalho);

            Row linhaCabecalho = sheet.createRow(linhaAtual++);
            for (int i = 0; i < cabecalhos.length; i++) {
                Cell celula = linhaCabecalho.createCell(i);
                celula.setCellValue(cabecalhos[i]);
                celula.setCellStyle(estiloCabecalho);
            }

            for (String[] linha : linhas) {
                Row linhaDados = sheet.createRow(linhaAtual++);
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

    public static byte[] paraPdf(MetadadosExportacao metadados, String[] cabecalhos, List<String[]> linhas) {
        // Margem superior/inferior maior para dar espaço ao cabeçalho (logo +
        // nome da empresa) e ao rodapé (paginação) desenhados pelo CabecalhoRodapePdf.
        Document documento = new Document(PageSize.A4.rotate(), 24, 24, 70, 50);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(documento, out);
            writer.setPageEvent(new CabecalhoRodapePdf(metadados));
            documento.open();

            Font fonteTitulo = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph paragrafoTitulo = new Paragraph(metadados.getTitulo(), fonteTitulo);
            paragrafoTitulo.setSpacingAfter(4);
            documento.add(paragrafoTitulo);

            Font fonteMetadados = new Font(Font.HELVETICA, 9, Font.ITALIC, new Color(107, 114, 128));
            Paragraph paragrafoGeracao = new Paragraph(
                    metadados.getTextoGeracao() + "  |  " + metadados.getTextoTotalRegistros(), fonteMetadados);
            paragrafoGeracao.setSpacingAfter(12);
            documento.add(paragrafoGeracao);

            PdfPTable tabela = new PdfPTable(cabecalhos.length);
            tabela.setWidthPercentage(100);

            Font fonteCabecalho = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            for (String cabecalho : cabecalhos) {
                PdfPCell celula = new PdfPCell(new Paragraph(cabecalho, fonteCabecalho));
                celula.setBackgroundColor(new Color(31, 41, 55));
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

    /**
     * Desenha, em toda página do PDF, um cabeçalho (logo + nome da empresa)
     * e um rodapé com paginação no formato "Página X de Y". O total de
     * páginas (Y) só é conhecido ao final da geração, por isso é resolvido
     * via um {@link PdfTemplate} — um "espaço reservado" desenhado em cada
     * página e preenchido de fato apenas em {@code onCloseDocument}.
     */
    private static final class CabecalhoRodapePdf extends PdfPageEventHelper {

        private final MetadadosExportacao metadados;
        private PdfTemplate templateTotalPaginas;
        private BaseFont baseFont;
        private Image logo;

        private CabecalhoRodapePdf(MetadadosExportacao metadados) {
            this.metadados = metadados;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            try {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                logo = Image.getInstance(LogoMonogramGenerator.gerarPng(metadados.getNomeEmpresa()));
            } catch (Exception e) {
                throw new ExportException("Falha ao preparar cabeçalho/rodapé do PDF.", e);
            }
            templateTotalPaginas = writer.getDirectContent().createTemplate(30, 15);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte conteudo = writer.getDirectContent();
            conteudo.saveState();

            // Cabeçalho: logo à esquerda + nome da empresa ao lado
            float logoAltura = 24f;
            float logoLargura = 24f;
            float topoPagina = document.getPageSize().getHeight() - 36;
            try {
                logo.scaleToFit(logoLargura, logoAltura);
                logo.setAbsolutePosition(document.leftMargin(), topoPagina - logoAltura + 6);
                conteudo.addImage(logo);
            } catch (Exception e) {
                throw new ExportException("Falha ao desenhar o logo no cabeçalho do PDF.", e);
            }

            conteudo.beginText();
            conteudo.setFontAndSize(baseFont, 11);
            conteudo.setTextMatrix(document.leftMargin() + logoLargura + 8, topoPagina - logoAltura / 2);
            conteudo.showText(metadados.getNomeEmpresa());
            conteudo.endText();

            // Linha divisória sutil abaixo do cabeçalho
            conteudo.setLineWidth(0.5f);
            conteudo.setColorStroke(new Color(209, 213, 219));
            conteudo.moveTo(document.leftMargin(), topoPagina - logoAltura - 4);
            conteudo.lineTo(document.getPageSize().getWidth() - document.rightMargin(), topoPagina - logoAltura - 4);
            conteudo.stroke();

            // Rodapé: "Página X de Y"
            String textoRodape = "Página " + writer.getPageNumber() + " de ";
            float larguraTexto = baseFont.getWidthPoint(textoRodape, 8);
            float baseRodape = document.bottom() - 25;
            float xRodape = document.getPageSize().getWidth() - document.rightMargin() - larguraTexto - 30;

            conteudo.beginText();
            conteudo.setFontAndSize(baseFont, 8);
            conteudo.setTextMatrix(xRodape, baseRodape);
            conteudo.showText(textoRodape);
            conteudo.endText();

            conteudo.addTemplate(templateTotalPaginas, xRodape + larguraTexto, baseRodape);

            conteudo.restoreState();
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            templateTotalPaginas.beginText();
            templateTotalPaginas.setFontAndSize(baseFont, 8);
            templateTotalPaginas.showText(String.valueOf(writer.getPageNumber()));
            templateTotalPaginas.endText();
        }
    }

    // ---------------------------------------------------------------
    // ODT (construído manualmente como um pacote ODF/ZIP mínimo válido,
    // sem dependências externas — mimetype + META-INF/manifest.xml +
    // content.xml + styles.xml (cabeçalho/rodapé) + Pictures/logo.png,
    // conforme especificação OASIS ODF 1.2).
    // ---------------------------------------------------------------

    public static byte[] paraOdt(MetadadosExportacao metadados, String[] cabecalhos, List<String[]> linhas) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] logoPng = LogoMonogramGenerator.gerarPng(metadados.getNomeEmpresa());

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

                // 3) content.xml (título, metadados, tabela de dados)
                zip.putNextEntry(new ZipEntry("content.xml"));
                zip.write(construirConteudoOdt(metadados, cabecalhos, linhas).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();

                // 4) styles.xml (cabeçalho com logo + rodapé com paginação "Página X de Y")
                zip.putNextEntry(new ZipEntry("styles.xml"));
                zip.write(construirEstilosOdt(metadados).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();

                // 5) Pictures/logo.png (logo exibido no cabeçalho de cada página)
                zip.putNextEntry(new ZipEntry("Pictures/logo.png"));
                zip.write(logoPng);
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
                + "  <manifest:file-entry manifest:full-path=\"styles.xml\" manifest:media-type=\"text/xml\"/>\n"
                + "  <manifest:file-entry manifest:full-path=\"Pictures/logo.png\" manifest:media-type=\"image/png\"/>\n"
                + "</manifest:manifest>\n";
    }

    private static String construirConteudoOdt(MetadadosExportacao metadados, String[] cabecalhos,
                                                List<String[]> linhas) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<office:document-content ")
                .append("xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" ")
                .append("xmlns:table=\"urn:oasis:names:tc:opendocument:xmlns:table:1.0\" ")
                .append("xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\" ")
                .append("office:version=\"1.2\">\n");
        xml.append("  <office:body>\n");
        xml.append("    <office:text>\n");
        xml.append("      <text:h text:outline-level=\"1\">").append(escaparXml(metadados.getTitulo()))
                .append("</text:h>\n");
        xml.append("      <text:p>").append(escaparXml(metadados.getTextoGeracao())).append("</text:p>\n");
        xml.append("      <text:p>").append(escaparXml(metadados.getTextoTotalRegistros())).append("</text:p>\n");
        xml.append("      <text:p/>\n");

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

    /**
     * Define o cabeçalho (logo + nome da empresa) e o rodapé (paginação,
     * via os campos nativos do ODF {@code <text:page-number/>} e
     * {@code <text:page-count/>}) aplicados a toda página do documento.
     */
    private static String construirEstilosOdt(MetadadosExportacao metadados) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<office:document-styles ")
                .append("xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" ")
                .append("xmlns:style=\"urn:oasis:names:tc:opendocument:xmlns:style:1.0\" ")
                .append("xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\" ")
                .append("xmlns:draw=\"urn:oasis:names:tc:opendocument:xmlns:drawing:1.0\" ")
                .append("xmlns:fo=\"urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0\" ")
                .append("xmlns:svg=\"urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0\" ")
                .append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" ")
                .append("office:version=\"1.2\">\n");

        xml.append("  <office:automatic-styles>\n");
        xml.append("    <style:page-layout style:name=\"pm1\">\n");
        xml.append("      <style:page-layout-properties fo:margin-top=\"2cm\" fo:margin-bottom=\"1.5cm\" ")
                .append("fo:margin-left=\"2cm\" fo:margin-right=\"2cm\"/>\n");
        xml.append("      <style:header-style><style:header-footer-properties fo:min-height=\"1.5cm\" ")
                .append("fo:margin-bottom=\"0.3cm\"/></style:header-style>\n");
        xml.append("      <style:footer-style><style:header-footer-properties fo:min-height=\"1cm\" ")
                .append("fo:margin-top=\"0.3cm\"/></style:footer-style>\n");
        xml.append("    </style:page-layout>\n");
        xml.append("  </office:automatic-styles>\n");

        xml.append("  <office:master-styles>\n");
        xml.append("    <style:master-page style:name=\"Standard\" style:page-layout-name=\"pm1\">\n");

        xml.append("      <style:header>\n");
        xml.append("        <text:p>")
                .append("<draw:frame draw:name=\"LogoCabecalho\" svg:width=\"1cm\" svg:height=\"1cm\" ")
                .append("text:anchor-type=\"as-char\">")
                .append("<draw:image xlink:href=\"Pictures/logo.png\" xlink:type=\"simple\" ")
                .append("xlink:show=\"embed\" xlink:actuate=\"onLoad\"/></draw:frame>")
                .append("<text:tab/>").append(escaparXml(metadados.getNomeEmpresa()))
                .append("</text:p>\n");
        xml.append("      </style:header>\n");

        xml.append("      <style:footer>\n");
        xml.append("        <text:p>Página <text:page-number>1</text:page-number> de ")
                .append("<text:page-count>1</text:page-count></text:p>\n");
        xml.append("      </style:footer>\n");

        xml.append("    </style:master-page>\n");
        xml.append("  </office:master-styles>\n");
        xml.append("</office:document-styles>\n");
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
