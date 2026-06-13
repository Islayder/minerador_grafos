package br.pucminas.tgc.githubgraph.app.desktop;

import br.pucminas.tgc.githubgraph.github.CollectionExecutionResult;
import br.pucminas.tgc.githubgraph.github.CollectionProfile;
import br.pucminas.tgc.githubgraph.github.CollectionPropertiesLoader;
import br.pucminas.tgc.githubgraph.github.CollectionReportFormatter;
import br.pucminas.tgc.githubgraph.app.RealCollectionApplicationService;
import br.pucminas.tgc.githubgraph.service.GraphExportApplicationService;
import br.pucminas.tgc.githubgraph.service.GraphSummaryService;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gera capturas da CLI e da interface desktop para o relatório LaTeX.
 */
public final class ReportFigureExporter {

    private static final Path OUTPUT_DIR = Path.of("docs", "relatorio", "figuras");

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "false");
        Files.createDirectories(OUTPUT_DIR);

        CollectionProfile profile = new CollectionPropertiesLoader().loadConfiguredProfile();
        RealCollectionApplicationService collectionService = new RealCollectionApplicationService();
        CollectionExecutionResult execution = collectionService.collectAndBuild(profile);
        new GraphExportApplicationService().exportAllGraphs(execution.getGraphBuildResult());
        GraphSummaryService summaryService = new GraphSummaryService();
        String summary = summaryService.summarize(execution.getGraphBuildResult());
        String postCollection = CollectionReportFormatter.formatPostCollection(execution).trim();

        exportCliScreenshot(profile, summary, postCollection);
        exportDesktopScreenshot(summary, postCollection);

        System.out.println("Figuras de interface salvas em " + OUTPUT_DIR.toAbsolutePath().normalize());
    }

    private static void exportCliScreenshot(
            CollectionProfile profile,
            String summary,
            String postCollection) throws IOException {
        String menu = """
                === github-collaboration-graph ===
                Repositorio configurado: %s (mineracao completa FULL_REPOSITORY)
                1. Mostrar informacoes do projeto
                2. Minerar repositorio inteiro configurado (fluxo principal)
                3. Executar demonstracao offline (plano B)
                4. Exibir resumo dos grafos carregados
                5. Analisar grafo integrado
                6. Exportar todos os grafos para GEXF
                7. Gerar relatorios em output/
                8. Sair
                9. Modo stress offline (sem API; mede limite interno)
                Escolha uma opcao: 4

                """.formatted(profile.getRepositorySlug());

        String text = menu + postCollection + System.lineSeparator() + System.lineSeparator() + summary;
        renderTerminalImage(text, OUTPUT_DIR.resolve("tela_cli.png"), 1280, 920);
    }

    private static void exportDesktopScreenshot(String summary, String postCollection) throws Exception {
        DesktopController controller = new DesktopController();
        String log = """
                --- Mineracao concluida (cache/github/) ---
                %s

                %s
                """.formatted(postCollection, summary);

        SwingUtilities.invokeAndWait(() -> {
            try {
                MainFrame frame = new MainFrame(controller);
                frame.seedScreenshotLog(log);
                frame.setSize(1040, 820);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.revalidate();
                frame.repaint();
                Thread.sleep(250);

                BufferedImage image = new BufferedImage(
                        frame.getWidth(),
                        frame.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
                frame.paintAll(graphics);
                graphics.dispose();

                ImageIO.write(image, "png", OUTPUT_DIR.resolve("tela_desktop.png").toFile());
                frame.setVisible(false);
                frame.dispose();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Falha ao exportar tela desktop", exception);
            } catch (IOException exception) {
                throw new IllegalStateException("Falha ao exportar tela desktop", exception);
            }
        });
    }

    private static void renderTerminalImage(String text, Path outputPath, int width, int height)
            throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(new Color(12, 16, 24));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(new Color(220, 230, 245));
        graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        int lineHeight = 18;
        int x = 24;
        int y = 32;
        for (String line : text.replace("\r", "").split("\n", -1)) {
            if (y > height - 24) {
                graphics.drawString("...", x, y);
                break;
            }
            graphics.drawString(line, x, y);
            y += lineHeight;
        }
        graphics.dispose();
        ImageIO.write(image, "png", outputPath.toFile());
    }

    private ReportFigureExporter() {
    }
}
