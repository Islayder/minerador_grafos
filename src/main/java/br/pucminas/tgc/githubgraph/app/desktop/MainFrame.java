package br.pucminas.tgc.githubgraph.app.desktop;

import br.pucminas.tgc.githubgraph.app.StressProfile;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Janela principal da interface desktop (apenas apresentacao; delega ao {@link DesktopController}).
 */
public final class MainFrame extends JFrame {

    private final DesktopController controller;
    private final JLabel statusLabel;
    private final JLabel tokenLabel;
    private final JProgressBar progressBar;
    private final JTextArea outputArea;
    private final JComboBox<StressProfile> stressCombo;
    private final JTextArea profileInfoArea;
    private final List<JButton> operationButtons = new ArrayList<>();
    private final JButton clearOutputButton;

    public MainFrame(DesktopController controller) {
        super("GitHub Collaboration Graph");
        this.controller = controller;

        statusLabel = new JLabel();
        tokenLabel = new JLabel();
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setStringPainted(false);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 820);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        stressCombo = new JComboBox<>(StressProfile.values());
        stressCombo.setSelectedItem(StressProfile.SMALL);
        stressCombo.addActionListener(event -> {
            StressProfile profile = (StressProfile) stressCombo.getSelectedItem();
            if (profile != null) {
                controller.setSelectedStressProfile(profile);
            }
        });

        profileInfoArea = new JTextArea(7, 40);
        profileInfoArea.setEditable(false);
        profileInfoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        profileInfoArea.setLineWrap(true);
        profileInfoArea.setWrapStyleWord(true);
        profileInfoArea.setText(controller.describeMiningConfiguration());
        profileInfoArea.setCaretPosition(0);

        clearOutputButton = new JButton("Limpar saida");
        clearOutputButton.addActionListener(event -> outputArea.setText(""));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createConfigPanel(), BorderLayout.CENTER);
        add(createMainPanel(), BorderLayout.SOUTH);

        refreshStatusLabels();
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        JPanel stressRow = new JPanel(new BorderLayout(8, 0));
        stressRow.add(new JLabel("Perfil stress offline (nao usa API):"), BorderLayout.WEST);
        stressRow.add(stressCombo, BorderLayout.CENTER);
        panel.add(stressRow, BorderLayout.SOUTH);

        JScrollPane profileScroll = new JScrollPane(profileInfoArea);
        profileScroll.setBorder(BorderFactory.createTitledBorder("Configuracao (config/collection.properties)"));
        panel.add(profileScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));

        JLabel title = new JLabel("GitHub Collaboration Graph");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JLabel subtitle = new JLabel("Mineracao completa de giscus/giscus (FULL_REPOSITORY)");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 14f));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.add(title);
        titles.add(subtitle);
        panel.add(titles, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 12, 12, 12));

        panel.add(createActionsPanel(), BorderLayout.NORTH);
        panel.add(createStatusPanel(), BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setPreferredSize(new Dimension(980, 340));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Log de execucao"));
        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Status"));

        JPanel labels = new JPanel(new GridLayout(2, 1, 4, 4));
        labels.add(statusLabel);
        labels.add(tokenLabel);
        panel.add(labels, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 3, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Acoes"));

        registerOperationButton(
                panel,
                "Minerar repositorio inteiro",
                "Minerando repositorio " + controller.getConfiguredRepositorySlug() + "...",
                controller::mineFullRepository);
        registerOperationButton(panel, "Modo offline", "Executando modo offline...", controller::runOfflineMode);
        registerOperationButton(
                panel,
                "Stress offline",
                "Executando stress offline...",
                controller::runStressOfflineMode);
        registerOperationButton(panel, "Mostrar resumo", "Gerando resumo...", controller::showSummary);
        registerOperationButton(
                panel,
                "Analisar grafo integrado",
                "Analisando grafo integrado...",
                controller::analyzeIntegratedGraph);
        registerOperationButton(panel, "Exportar GEXF", "Exportando GEXF...", controller::exportGexfFiles);
        registerOperationButton(panel, "Gerar relatorios", "Gerando relatorios...", controller::generateTextReports);

        panel.add(clearOutputButton);
        return panel;
    }

    private void registerOperationButton(
            JPanel panel,
            String label,
            String runningStatus,
            DesktopTask task) {
        JButton button = new JButton(label);
        button.addActionListener(event -> runBackgroundTask(runningStatus, task));
        operationButtons.add(button);
        panel.add(button);
    }

    private void runBackgroundTask(String runningStatus, DesktopTask task) {
        setProcessingState(true, runningStatus);
        appendLog("---" + System.lineSeparator());

        long startedAt = System.currentTimeMillis();

        SwingWorker<String, String> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return task.run(this::publish);
            }

            @Override
            protected void process(List<String> chunks) {
                for (String chunk : chunks) {
                    appendLog(chunk);
                }
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    long elapsedSeconds = Math.max(0L, (System.currentTimeMillis() - startedAt) / 1000L);
                    appendLog(System.lineSeparator() + "Tempo decorrido: " + elapsedSeconds + " s");
                    appendLog(System.lineSeparator() + result);
                    appendLog(System.lineSeparator() + "Operacao concluida com sucesso.");
                } catch (Exception exception) {
                    long elapsedSeconds = Math.max(0L, (System.currentTimeMillis() - startedAt) / 1000L);
                    appendLog(System.lineSeparator() + "Tempo decorrido: " + elapsedSeconds + " s");
                    appendLog("Erro: " + exception.getMessage());
                    Throwable cause = exception.getCause();
                    if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
                        appendLog("Causa: " + cause.getMessage());
                    }
                } finally {
                    setProcessingState(false, null);
                }
            }
        };
        worker.execute();
    }

    private void setProcessingState(boolean processing, String runningStatus) {
        for (JButton button : operationButtons) {
            button.setEnabled(!processing);
        }
        stressCombo.setEnabled(!processing);
        clearOutputButton.setEnabled(true);

        progressBar.setIndeterminate(processing);
        progressBar.setVisible(processing);

        if (processing) {
            statusLabel.setText("Status: " + runningStatus);
        } else {
            refreshStatusLabels();
        }
    }

    private void refreshStatusLabels() {
        statusLabel.setText("Status: " + controller.getStatusLine());
        tokenLabel.setText(controller.getTokenStatusLine());
    }

    private void appendLog(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        outputArea.append(text.trim() + System.lineSeparator());
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    void seedScreenshotLog(String text) {
        outputArea.setText(text == null ? "" : text);
        outputArea.setCaretPosition(0);
    }
}
