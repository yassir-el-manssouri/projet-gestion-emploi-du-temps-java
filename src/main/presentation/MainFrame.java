package main.presentation;

import main.presentation.panels.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    private static final Color SIDEBAR_BG     = Color.BLACK;
    private static final Color SIDEBAR_HOVER  = new Color(40, 40, 40);
    private static final Color SIDEBAR_ACTIVE = new Color(70, 70, 70);
    private static final Color SIDEBAR_TEXT   = Color.WHITE;
    private static final Color CONTENT_BG     = Color.WHITE;
    private static final Font  SIDEBAR_FONT   = new Font("Tahoma", Font.BOLD, 13);

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton activeButton = null;

    private final String[][] navItems = {
        {"Salles",              "SALLES"},
        {"Classes",             "CLASSES"},
        {"Professeurs",         "PROFS"},
        {"Matieres",            "MATIERES"},
        {"Affectations",        "AFFECTATIONS"},
        {"Emploi du Temps",     "EMPLOI"},
    };

    public MainFrame() {
        setTitle("Emplois du Temps - EMSI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(190, 0));

        // Logo / titre
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 20));
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setMaximumSize(new Dimension(190, 80));
        JLabel logoLabel = new JLabel("EMSI");
        logoLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
        logoLabel.setForeground(Color.WHITE);
        JLabel subLabel = new JLabel("Gestion EDT");
        subLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
        subLabel.setForeground(new Color(180, 180, 180));
        JPanel logoInner = new JPanel();
        logoInner.setLayout(new BoxLayout(logoInner, BoxLayout.Y_AXIS));
        logoInner.setBackground(SIDEBAR_BG);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoInner.add(logoLabel);
        logoInner.add(subLabel);
        logoPanel.add(logoInner);
        sidebar.add(logoPanel);

        // Separateur
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(80, 80, 80));
        sep.setBackground(new Color(80, 80, 80));
        sep.setMaximumSize(new Dimension(190, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(12));

        // Content
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CONTENT_BG);
        contentPanel.add(new SallePanel(),        "SALLES");
        contentPanel.add(new ClassePanel(),       "CLASSES");
        contentPanel.add(new ProfPanel(),         "PROFS");
        contentPanel.add(new MatierePanel(),      "MATIERES");
        contentPanel.add(new AffectationPanel(),  "AFFECTATIONS");
        contentPanel.add(new EmploiPanel(),       "EMPLOI");

        // Boutons nav
        for (String[] item : navItems) {
            JButton btn = createNavButton(item[0], item[1]);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel version = new JLabel("v2.0", SwingConstants.CENTER);
        version.setFont(new Font("Tahoma", Font.PLAIN, 10));
        version.setForeground(new Color(120, 120, 120));
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(version);
        sidebar.add(Box.createVerticalStrut(12));

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Activer le premier bouton par defaut
        Component[] comps = sidebar.getComponents();
        for (Component c : comps) {
            if (c instanceof JButton) {
                ((JButton) c).doClick();
                break;
            }
        }
    }

    private JButton createNavButton(String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setFont(SIDEBAR_FONT);
        btn.setForeground(SIDEBAR_TEXT);
        btn.setBackground(SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
        btn.setMaximumSize(new Dimension(190, 48));
        btn.setPreferredSize(new Dimension(190, 48));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(SIDEBAR_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(SIDEBAR_BG);
            }
        });

        btn.addActionListener(e -> {
            if (activeButton != null) {
                activeButton.setBackground(SIDEBAR_BG);
                activeButton.setForeground(SIDEBAR_TEXT);
                activeButton.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
            }
            activeButton = btn;
            btn.setBackground(SIDEBAR_ACTIVE);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, Color.WHITE),
                BorderFactory.createEmptyBorder(12, 17, 12, 10)
            ));
            cardLayout.show(contentPanel, cardName);
        });

        return btn;
    }
}
