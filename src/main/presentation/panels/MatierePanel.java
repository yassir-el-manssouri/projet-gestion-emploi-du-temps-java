package main.presentation.panels;

import main.model.Matiere;
import main.service.MatiereService;
import main.utils.DataSyncManager;
import main.utils.DataSyncManager.EventType;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.UUID;

public class MatierePanel extends JPanel implements DataSyncManager.DataChangeListener {
    private JTable table;
    private DefaultTableModel tableModel;
    private MatiereService matiereService;
    private JTextField searchField;
    private JComboBox<String> filiereFilterCombo;

    private final Color BACKGROUND_COLOR  = Color.WHITE;
    private final Color HEADER_BG         = new Color(50, 50, 50);
    private final Color ROW_ODD           = new Color(245, 245, 245);
    private final Color SELECTION_COLOR   = new Color(180, 180, 180);
    private final Color CONFLICT_BG       = new Color(255, 220, 220);
    private final Color OK_BG             = new Color(220, 255, 220);

    public MatierePanel() {
        matiereService = new MatiereService();
        initializeComponents();
        loadData();
        setupKeyboardShortcuts();
        DataSyncManager.getInstance().addListener(this);
    }

    /**
     * Recharge la table quand un prof est supprimé ou qu'une affectation change
     * (la colonne "profs affectés" serait fausse sinon).
     */
    @Override
    public void onDataChanged(EventType event) {
        switch (event) {
            case PROF_ADDED:
            case PROF_UPDATED:
            case PROF_DELETED:
            case AFFECTATION_CHANGED:
                javax.swing.SwingUtilities.invokeLater(this::loadData);
                break;
            default:
                break;
        }
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(BACKGROUND_COLOR);
        add(createTopPanel(),    BorderLayout.NORTH);
        add(createTablePanel(),  BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JLabel titleLabel = new JLabel("Catalogue des Matieres");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLACK);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(BACKGROUND_COLOR);
        searchField = new JTextField(15);
        searchField.setFont(new Font("Tahoma", Font.PLAIN, 12));
        searchField.setForeground(Color.BLACK);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterTable(); }
        });
        filiereFilterCombo = new JComboBox<>(new String[]{"Toutes filieres", "AP", "GC", "GF", "IIR", "GESI", "GI", "IAII", "IFA", "IR-CIR", "IR-DEV", "IR-IA", "GII"});
        filiereFilterCombo.setFont(new Font("Tahoma", Font.PLAIN, 12));
        filiereFilterCombo.setBackground(Color.WHITE);
        filiereFilterCombo.setForeground(Color.BLACK);
        filiereFilterCombo.addActionListener(e -> filterTable());
        JButton addButton     = createButton("+ Nouveau");
        JButton editButton    = createButton("Editer");
        JButton deleteButton  = createButton("Retirer");
        JButton refreshButton = createButton("Rafraichir");
        addButton.addActionListener(e -> addMatiere());
        editButton.addActionListener(e -> editMatiere());
        deleteButton.addActionListener(e -> deleteMatiere());
        refreshButton.addActionListener(e -> loadData());
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Filiere:"));
        searchPanel.add(filiereFilterCombo);
        searchPanel.add(addButton);
        searchPanel.add(editButton);
        searchPanel.add(deleteButton);
        searchPanel.add(refreshButton);
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        return topPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        // Colonnes visibles: ID, Nom, Filiere, Niveau, Min/Semaine, Type
        String[] columns = {"", "ID", "Nom", "Filiere", "Niveau", "Min/Semaine", "Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return c == 0; }
            public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : Object.class; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(36);
        table.setFont(new Font("Tahoma", Font.PLAIN, 12));
        table.setShowGrid(true);
        table.setGridColor(new Color(200, 200, 200));
        table.setSelectionBackground(SELECTION_COLOR);
        table.setSelectionForeground(Color.BLACK);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Tahoma", Font.BOLD, 13));
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        // Forcer le rendu en-tete
        final Color hBg = HEADER_BG;
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                setBackground(hBg); setForeground(Color.WHITE);
                setFont(new Font("Tahoma", Font.BOLD, 13));
                setHorizontalAlignment(CENTER); setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                setOpaque(true); return this;
            }
        });
        table.setDefaultRenderer(Object.class, new BwTableCellRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(BACKGROUND_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        int total = matiereService.getAllMatieres().size();
        JLabel statsLabel = new JLabel("Matieres disponibles : " + total);
        statsLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
        statsLabel.setForeground(Color.BLACK);
        bottomPanel.add(statsLabel);
        return bottomPanel;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Tahoma", Font.BOLD, 11));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(new Color(220, 220, 220)); }
            public void mouseExited(MouseEvent e)  { button.setBackground(Color.WHITE); }
        });
        return button;
    }

    private void filterTable() {
        String searchText = searchField.getText().toLowerCase();
        String selectedFiliere = (String) filiereFilterCombo.getSelectedItem();
        tableModel.setRowCount(0);
        for (Matiere m : matiereService.getAllMatieres()) {
            boolean matchSearch = searchText.isEmpty() ||
                m.getNom().toLowerCase().contains(searchText) ||
                m.getFiliere().toLowerCase().contains(searchText) ||
                m.getId().toLowerCase().contains(searchText);
            boolean matchFiliere = selectedFiliere.equals("Toutes filieres") ||
                m.getFiliere().equals(selectedFiliere);
            if (matchSearch && matchFiliere) {
                tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    m.getId(),
                    m.getNom(), m.getFiliere(), m.getNiveau(),
                    m.getMinutesParSemaine(), m.getType()
                });
            }
        }
    }

    private boolean nomMatiereExisteDeja(String nom, String filiere, String niveau, String excludeId) {
        for (Matiere m : matiereService.getAllMatieres()) {
            if (m.getNom().equalsIgnoreCase(nom.trim()) &&
                m.getFiliere().equals(filiere) &&
                m.getNiveau().equals(niveau) &&
                (excludeId == null || !m.getId().equals(excludeId))) {
                return true;
            }
        }
        return false;
    }

    private void loadData() { filterTable(); }
    private void addMatiere()  { showMatiereDialog(null); }
    private java.util.List<Integer> getCheckedRows() {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) if ((Boolean)tableModel.getValueAt(i, 0)) list.add(i);
        return list;
    }
    private void editMatiere() {
        java.util.List<Integer> checked = getCheckedRows();
        if (checked.isEmpty()) { JOptionPane.showMessageDialog(this, "Cochez une matiere pour editer", "Attention", JOptionPane.WARNING_MESSAGE); return; }
        if (checked.size() > 1) { JOptionPane.showMessageDialog(this, "Cochez une seule matiere pour editer", "Attention", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(checked.get(0), 1);
        Matiere m = matiereService.getMatiere(id);
        if (m != null) showMatiereDialog(m);
    }
    private void deleteMatiere() {
        java.util.List<Integer> checked = getCheckedRows();
        if (checked.isEmpty()) { JOptionPane.showMessageDialog(this, "Cochez au moins une matiere a supprimer", "Attention", JOptionPane.WARNING_MESSAGE); return; }
        String msg = checked.size() == 1 ? "Supprimer cette matiere ?" : "Supprimer les " + checked.size() + " matieres cochees ?";
        if (JOptionPane.showConfirmDialog(this, msg, "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            int errors = 0;
            for (int i = checked.size() - 1; i >= 0; i--) {
                String id = (String) tableModel.getValueAt(checked.get(i), 1);
                if (!matiereService.deleteMatiere(id)) errors++;
            }
            loadData();
            if (errors > 0) JOptionPane.showMessageDialog(this, errors + " matiere(s) n'ont pas pu etre supprimee(s)", "Erreur", JOptionPane.ERROR_MESSAGE);
            else DataSyncManager.getInstance().fireEvent(EventType.MATIERE_DELETED, this);
        }
    }

    private void showMatiereDialog(Matiere matiere) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            matiere == null ? "Ajouter une matiere" : "Modifier une matiere", true);
        dialog.setSize(510, 470);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nomField = createTextField();
        JComboBox<String> filiereCombo = new JComboBox<>(new String[]{"AP", "GC", "GF", "IIR", "GESI", "GI", "IAII", "IFA", "IR-CIR", "IR-DEV", "IR-IA", "GII"});
        styleCombo(filiereCombo);
        JComboBox<String> niveauCombo = new JComboBox<>(new String[]{"1A", "2A", "3A", "4A", "5A"});
        styleCombo(niveauCombo);
        JSpinner heuresSpinner = new JSpinner(new SpinnerNumberModel(120, 0, 600, 30));
        heuresSpinner.setFont(new Font("Tahoma", Font.PLAIN, 12));
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Cours", "TD", "TP"});
        styleCombo(typeCombo);

        JLabel conflitLabel = new JLabel("  ");
        conflitLabel.setFont(new Font("Tahoma", Font.BOLD, 11));

        if (matiere != null) {
            nomField.setText(matiere.getNom());
            filiereCombo.setSelectedItem(matiere.getFiliere());
            niveauCombo.setSelectedItem(matiere.getNiveau());
            heuresSpinner.setValue(matiere.getMinutesParSemaine());
            typeCombo.setSelectedItem(matiere.getType());
        }

        final String excludeId = matiere != null ? matiere.getId() : null;

        // Detection automatique de conflit (nom + filiere + niveau) en temps reel
        Runnable verifier = () -> {
            String nom = nomField.getText().trim();
            String filiere = (String) filiereCombo.getSelectedItem();
            String niveau = (String) niveauCombo.getSelectedItem();
            if (nom.isEmpty()) {
                conflitLabel.setText("  ");
                nomField.setBackground(Color.WHITE);
            } else if (nomMatiereExisteDeja(nom, filiere, niveau, excludeId)) {
                conflitLabel.setText("  ⚠ Conflit : cette matiere existe deja pour cette filiere/niveau !");
                conflitLabel.setForeground(new Color(180, 0, 0));
                nomField.setBackground(CONFLICT_BG);
            } else {
                conflitLabel.setText("  ✓ Pas de conflit");
                conflitLabel.setForeground(new Color(0, 130, 0));
                nomField.setBackground(OK_BG);
            }
        };

        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { verifier.run(); }
            public void removeUpdate(DocumentEvent e)  { verifier.run(); }
            public void changedUpdate(DocumentEvent e) { verifier.run(); }
        };
        nomField.getDocument().addDocumentListener(dl);
        filiereCombo.addActionListener(e -> verifier.run());
        niveauCombo.addActionListener(e -> verifier.run());

        int r = 0;
        addField(panel, gbc, r++, "Nom:", nomField);
        gbc.gridx = 1; gbc.gridy = r++;
        panel.add(conflitLabel, gbc);
        addField(panel, gbc, r++, "Filiere:", filiereCombo);
        addField(panel, gbc, r++, "Niveau:", niveauCombo);
        addField(panel, gbc, r++, "Duree seance (min):", heuresSpinner);
        addField(panel, gbc, r++, "Type:", typeCombo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton saveBtn   = createButton("Sauvegarder");
        JButton cancelBtn = createButton("Fermer");
        saveBtn.addActionListener(e -> {
            String nom = nomField.getText().trim();
            String filiere = (String) filiereCombo.getSelectedItem();
            String niveau = (String) niveauCombo.getSelectedItem();
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Remplir le nom", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (nomMatiereExisteDeja(nom, filiere, niveau, excludeId)) {
                JOptionPane.showMessageDialog(dialog,
                    "Conflit : cette matiere existe deja pour la filiere " + filiere + " niveau " + niveau + ".",
                    "Conflit detecte", JOptionPane.ERROR_MESSAGE);
                nomField.requestFocus();
                return;
            }
            if (matiere == null) {
                Matiere newM = new Matiere(UUID.randomUUID().toString(), nom, filiere, niveau,
                    (int)heuresSpinner.getValue(), (String)typeCombo.getSelectedItem());
                if (matiereService.addMatiere(newM)) {
                    dialog.dispose();
                    loadData();
                    DataSyncManager.getInstance().fireEvent(EventType.MATIERE_ADDED, MatierePanel.this);
                }
            } else {
                matiere.setNom(nom); matiere.setFiliere(filiere);
                matiere.setNiveau(niveau);
                matiere.setMinutesParSemaine((int)heuresSpinner.getValue());
                matiere.setType((String)typeCombo.getSelectedItem());
                if (matiereService.updateMatiere(matiere)) {
                    dialog.dispose();
                    loadData();
                    DataSyncManager.getInstance().fireEvent(EventType.MATIERE_UPDATED, MatierePanel.this);
                }
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(saveBtn); btnPanel.add(cancelBtn);
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JTextField createTextField() {
        JTextField f = new JTextField(20);
        f.setFont(new Font("Tahoma", Font.PLAIN, 12));
        f.setForeground(Color.BLACK);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return f;
    }

    private void styleCombo(JComboBox<String> c) {
        c.setFont(new Font("Tahoma", Font.PLAIN, 12));
        c.setBackground(Color.WHITE);
        c.setForeground(Color.BLACK);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Tahoma", Font.BOLD, 12));
        lbl.setForeground(Color.BLACK);
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void setupKeyboardShortcuts() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "focusSearch");
        getActionMap().put("focusSearch", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { searchField.requestFocus(); }
        });
    }

    class BwTableCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            // Appel super minimal - on ecrase tout apres
            super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            setFont(new Font("Tahoma", Font.PLAIN, 12));
            // Forcer le texte en noir, peu importe le Look and Feel
            setForeground(Color.BLACK);
            boolean checked = (Boolean) tableModel.getValueAt(row, 0);
            if (checked) {
                setBackground(new Color(210, 228, 255));
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
            }
            setHorizontalAlignment(LEFT);
            return this;
        }
    }
}
