package br.pucminas.tgc.githubgraph.app.desktop;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ponto de entrada da interface desktop (Java Swing).
 */
public final class DesktopMain {

    private DesktopMain() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Mantém o look and feel padrão se o do sistema não estiver disponível.
            }
            DesktopController controller = new DesktopController();
            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }
}
