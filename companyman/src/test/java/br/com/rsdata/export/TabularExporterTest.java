package br.com.rsdata.export;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TabularExporter - geração de arquivos CSV, XLS, ODT e PDF")
class TabularExporterTest {

    private static final String[] CABECALHOS = {"Nome", "Cidade"};
    private static final List<String[]> LINHAS = List.of(
            new String[]{"Ana; Souza", "São Paulo"},
            new String[]{"Empresa \"Boa\"", "Rio de Janeiro"}
    );

    @Test
    @DisplayName("CSV deve conter BOM UTF-8, cabeçalho e linhas com escaping correto")
    void devGerarCsvComEscapingCorreto() {
        byte[] csv = TabularExporter.paraCsv(CABECALHOS, LINHAS);
        String conteudo = new String(csv, StandardCharsets.UTF_8);

        assertTrue(conteudo.startsWith("\uFEFF"), "deve iniciar com o BOM UTF-8");
        assertTrue(conteudo.contains("Nome;Cidade"), "deve conter a linha de cabeçalho");
        
        // Campo com ';' deve vir entre aspas
        assertTrue(conteudo.contains("\"Ana; Souza\";São Paulo"));
        
        // Aspas internas devem ser escapadas duplicando-as
        assertTrue(conteudo.contains("\"Empresa \"\"Boa\"\"\";Rio de Janeiro"));
    }

    @Test
    @DisplayName("XLS gerado deve ser lido de volta pelo Apache POI com os mesmos dados")
    void deveGerarXlsValidoLegivelPeloPoi() throws IOException {
        byte[] xls = TabularExporter.paraXls("MinhaPlanilha", CABECALHOS, LINHAS);

        try (HSSFWorkbook workbook = new HSSFWorkbook(new ByteArrayInputStream(xls))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("MinhaPlanilha", sheet.getSheetName());

            Row cabecalho = sheet.getRow(0);
            assertEquals("Nome", cabecalho.getCell(0).getStringCellValue());
            assertEquals("Cidade", cabecalho.getCell(1).getStringCellValue());

            Row primeiraLinha = sheet.getRow(1);
            assertEquals("Ana; Souza", primeiraLinha.getCell(0).getStringCellValue());
            assertEquals("São Paulo", primeiraLinha.getCell(1).getStringCellValue());
        }
    }

    @Test
    @DisplayName("PDF gerado deve iniciar com a assinatura binária padrão (%PDF)")
    void deveGerarPdfComAssinaturaValida() {
        byte[] pdf = TabularExporter.paraPdf("Relatório de Teste", CABECALHOS, LINHAS);

        byte[] assinaturaEsperada = "%PDF".getBytes(StandardCharsets.US_ASCII);
        byte[] assinaturaReal = new byte[4];
        
        System.arraycopy(pdf, 0, assinaturaReal, 0, 4);

        assertArrayEquals(assinaturaEsperada, assinaturaReal);
        assertTrue(pdf.length > 100, "o PDF gerado não deveria estar vazio/truncado");
    }

    @Test
    @DisplayName("ODT gerado deve ser um ZIP válido com mimetype não comprimido, manifest e content.xml")
    void deveGerarOdtComEstruturaValida() throws IOException {
        byte[] odt = TabularExporter.paraOdt("Relatório de Teste", CABECALHOS, LINHAS);

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(odt))) {
            ZipEntry primeiraEntrada = zip.getNextEntry();
            
            assertEquals("mimetype", primeiraEntrada.getName(), "a primeira entrada deve ser 'mimetype'");
            assertEquals(ZipEntry.STORED, primeiraEntrada.getMethod(), "'mimetype' não deve ser comprimida");

            byte[] conteudoMimetype = zip.readAllBytes();
            
            assertEquals("application/vnd.oasis.opendocument.text",
                new String(conteudoMimetype, StandardCharsets.US_ASCII));

            boolean encontrouManifesto = false;
            boolean encontrouContent = false;
            
            ZipEntry entrada;
            
            while ((entrada = zip.getNextEntry()) != null) {
                if (entrada.getName().equals("META-INF/manifest.xml")) {
                    encontrouManifesto = true;
                } else if (entrada.getName().equals("content.xml")) {
                    encontrouContent = true;

                    String conteudo = new String(zip.readAllBytes(), StandardCharsets.UTF_8);

                    assertTrue(conteudo.contains("Relatório de Teste"));
                    assertTrue(conteudo.contains("Ana; Souza"));
                    assertTrue(conteudo.contains("&quot;Boa&quot;"), "aspas devem ser escapadas no XML");
                }
            }

            assertTrue(encontrouManifesto, "deve conter META-INF/manifest.xml");
            assertTrue(encontrouContent, "deve conter content.xml");
        }
    }
}
