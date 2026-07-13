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

    private static MetadadosExportacao criarMetadados(String titulo) {
        return MetadadosExportacao.criar(titulo, LINHAS.size());
    }

    @Test
    @DisplayName("CSV deve conter BOM UTF-8, título, data/hora, total de registros e linhas com escaping correto")
    void deveGerarCsvComMetadadosEEscapingCorreto() {
        MetadadosExportacao metadados = criarMetadados("Relatório de Teste");
        byte[] csv = TabularExporter.paraCsv(metadados, CABECALHOS, LINHAS);
        String conteudo = new String(csv, StandardCharsets.UTF_8);

        assertTrue(conteudo.startsWith("\uFEFF"), "deve iniciar com o BOM UTF-8");
        assertTrue(conteudo.contains("Relatório de Teste"), "deve conter o título do relatório");
        assertTrue(conteudo.contains("Gerado em"), "deve conter a data/hora de geração");
        assertTrue(conteudo.contains("Total de registros: 2"), "deve conter o total de registros");
        assertTrue(conteudo.contains("Nome;Cidade"), "deve conter a linha de cabeçalho");
        
        // Campo com ';' deve vir entre aspas
        assertTrue(conteudo.contains("\"Ana; Souza\";São Paulo"));
        
        // Aspas internas devem ser escapadas duplicando-as
        assertTrue(conteudo.contains("\"Empresa \"\"Boa\"\"\";Rio de Janeiro"));
    }

    @Test
    @DisplayName("XLS gerado deve conter título, metadados e os dados, lidos de volta pelo Apache POI")
    void deveGerarXlsComMetadadosLegivelPeloPoi() throws IOException {
        MetadadosExportacao metadados = criarMetadados("Relatório de Teste");
        byte[] xls = TabularExporter.paraXls(metadados, "MinhaPlanilha", CABECALHOS, LINHAS);

        try (HSSFWorkbook workbook = new HSSFWorkbook(new ByteArrayInputStream(xls))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("MinhaPlanilha", sheet.getSheetName());

            assertEquals("Relatório de Teste", sheet.getRow(0).getCell(0).getStringCellValue());
            assertTrue(sheet.getRow(1).getCell(0).getStringCellValue().startsWith("Gerado em"));
            assertEquals("Total de registros: 2", sheet.getRow(2).getCell(0).getStringCellValue());
            // linha 3 (índice) é a linha em branco de separação

            Row cabecalho = sheet.getRow(4);
            assertEquals("Nome", cabecalho.getCell(0).getStringCellValue());
            assertEquals("Cidade", cabecalho.getCell(1).getStringCellValue());

            Row primeiraLinha = sheet.getRow(5);
            assertEquals("Ana; Souza", primeiraLinha.getCell(0).getStringCellValue());
            assertEquals("São Paulo", primeiraLinha.getCell(1).getStringCellValue());
        }
    }

    @Test
    @DisplayName("PDF gerado deve iniciar com a assinatura binária padrão (%PDF)")
    void deveGerarPdfComAssinaturaValida() {
        MetadadosExportacao metadados = criarMetadados("Relatório de Teste");
        byte[] pdf = TabularExporter.paraPdf(metadados, CABECALHOS, LINHAS);

        byte[] assinaturaEsperada = "%PDF".getBytes(StandardCharsets.US_ASCII);
        byte[] assinaturaReal = new byte[4];
        System.arraycopy(pdf, 0, assinaturaReal, 0, 4);

        assertArrayEquals(assinaturaEsperada, assinaturaReal);
        assertTrue(pdf.length > 100, "o PDF gerado não deveria estar vazio/truncado");
    }

    @Test
    @DisplayName("ODT gerado deve ser um ZIP válido com mimetype não comprimido, manifest, content.xml, "
            + "styles.xml (cabeçalho/rodapé) e Pictures/logo.png")
    void deveGerarOdtComEstruturaValidaCabecalhoERodape() throws IOException {
        MetadadosExportacao metadados = criarMetadados("Relatório de Teste");
        byte[] odt = TabularExporter.paraOdt(metadados, CABECALHOS, LINHAS);

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(odt))) {
            ZipEntry primeiraEntrada = zip.getNextEntry();
            assertEquals("mimetype", primeiraEntrada.getName(), "a primeira entrada deve ser 'mimetype'");
            assertEquals(ZipEntry.STORED, primeiraEntrada.getMethod(), "'mimetype' não deve ser comprimida");

            byte[] conteudoMimetype = zip.readAllBytes();
            assertEquals("application/vnd.oasis.opendocument.text",
                    new String(conteudoMimetype, StandardCharsets.US_ASCII));

            boolean encontrouManifesto = false;
            boolean encontrouContent = false;
            boolean encontrouStyles = false;
            boolean encontrouLogo = false;
            
            ZipEntry entrada;
            
            while ((entrada = zip.getNextEntry()) != null) {
                switch (entrada.getName()) {
                    case "META-INF/manifest.xml":
                        encontrouManifesto = true;
                        break;
                    case "content.xml":
                        encontrouContent = true;
            
                        String conteudo = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                        assertTrue(conteudo.contains("Relatório de Teste"));
                        assertTrue(conteudo.contains("Gerado em"), "deve conter a data/hora de geração");
                        assertTrue(conteudo.contains("Total de registros: 2"));
                        assertTrue(conteudo.contains("Ana; Souza"));
                        assertTrue(conteudo.contains("&quot;Boa&quot;"), "aspas devem ser escapadas no XML");
            
                        break;
                    case "styles.xml":
                        encontrouStyles = true;
            
                        String estilos = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                        assertTrue(estilos.contains("<style:header>"), "deve definir um cabeçalho de página");
                        assertTrue(estilos.contains("<style:footer>"), "deve definir um rodapé de página");
                        assertTrue(estilos.contains("<text:page-number>"), "rodapé deve conter o número da página");
                        assertTrue(estilos.contains("<text:page-count>"), "rodapé deve conter o total de páginas");
                        assertTrue(estilos.contains("Pictures/logo.png"), "cabeçalho deve referenciar o logo");
            
                        break;
                    case "Pictures/logo.png":
                        encontrouLogo = true;
                        byte[] logoBytes = zip.readAllBytes();
                        assertTrue(logoBytes.length > 0, "o logo embutido não deveria estar vazio");
                        break;
                    default:
                        // outras entradas não são relevantes para este teste
                }
            }

            assertTrue(encontrouManifesto, "deve conter META-INF/manifest.xml");
            assertTrue(encontrouContent, "deve conter content.xml");
            assertTrue(encontrouStyles, "deve conter styles.xml");
            assertTrue(encontrouLogo, "deve conter Pictures/logo.png");
        }
    }
}
