package br.com.rsdata.export;

import br.com.rsdata.exception.ExportException;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Gera, de forma inteiramente programática (sem dependências externas),
 * uma imagem simples de "monograma" com as iniciais do nome da empresa —
 * usada no cabeçalho dos relatórios em PDF e ODT.
 *
 * O logo real da aplicação (ver {@code com.empresa.controller.SobreSistemaBean},
 * {@code APP_LOGO_PATH}) é um arquivo SVG servido como recurso estático da
 * webapp; como as classes deste pacote são POJOs simples, sem acesso ao
 * {@code ServletContext} (necessário para localizar esse arquivo em disco)
 * e sem uma biblioteca de rasterização de SVG no classpath, optou-se por
 * este monograma como uma solução autocontida e sem dependências extras.
 * Para embutir o arquivo de logo real (PNG/JPEG) nos relatórios, basta
 * substituir o uso desta classe por uma leitura de bytes a partir do
 * {@code ServletContext} (disponível em {@code ExportDownloadServlet}).
 */
public final class LogoMonogramGenerator {

    private static final int TAMANHO_PIXELS = 48;

    private LogoMonogramGenerator() {
    }

    /** Gera o monograma como uma imagem PNG (bytes). */
    public static byte[] gerarPng(String nomeEmpresa) {
        BufferedImage imagem = gerarBufferedImage(nomeEmpresa);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(imagem, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ExportException("Falha ao gerar o monograma do logo para o relatório.", e);
        }
    }

    /** Gera o monograma como {@link BufferedImage}, para uso direto em bibliotecas de PDF. */
    public static BufferedImage gerarBufferedImage(String nomeEmpresa) {
        BufferedImage imagem = new BufferedImage(TAMANHO_PIXELS, TAMANHO_PIXELS, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics();

        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(31, 41, 55));
            g.fillRoundRect(0, 0, TAMANHO_PIXELS, TAMANHO_PIXELS, 10, 10);

            String iniciais = obterIniciais(nomeEmpresa);

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 20));

            FontMetrics metricas = g.getFontMetrics();

            int x = (TAMANHO_PIXELS - metricas.stringWidth(iniciais)) / 2;
            int y = (TAMANHO_PIXELS - metricas.getHeight()) / 2 + metricas.getAscent();

            g.drawString(iniciais, x, y);
        } finally {
            g.dispose();
        }

        return imagem;
    }

    private static String obterIniciais(String nomeEmpresa) {
        if (nomeEmpresa == null || nomeEmpresa.isBlank()) {
            return "E";
        }

        String[] partes = nomeEmpresa.trim().split("\\s+");
        StringBuilder iniciais = new StringBuilder();

        for (int i = 0; i < Math.min(2, partes.length) && iniciais.length() < 2; i++) {
            if (!partes[i].isEmpty()) {
                iniciais.append(Character.toUpperCase(partes[i].charAt(0)));
            }
        }
        return iniciais.length() > 0 ? iniciais.toString() : "E";
    }
}
