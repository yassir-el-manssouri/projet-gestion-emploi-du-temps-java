package main.presentation.panels;

import main.model.Professeur;
import main.service.ProfService;
import main.utils.DataSyncManager;
import main.utils.DataSyncManager.EventType;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.UUID;

public class ProfPanel extends JPanel implements DataSyncManager.DataChangeListener {
    private JTable table;
    private DefaultTableModel tableModel;
    private ProfService profService;
    private JTextField searchField;
    private JLabel statsLabel;
    private JCheckBox selectAllBox;

    private final Color BACKGROUND_COLOR = Color.WHITE;
    private final Color HEADER_BG        = new Color(50, 50, 50);
    private final Color CONFLICT_BG      = new Color(255, 220, 220);
    private final Color OK_BG            = new Color(220, 255, 220);
    private static final int CHK_COL = 0;

    public ProfPanel() {
        profService = new ProfService();
        initializeComponents();
        loadData();
        setupKeyboardShortcuts();
        DataSyncManager.getInstance().addListener(this);
    }

    /**
     * Réagit aux événements émis par les autres panneaux.
     * On se rafraîchit uniquement si matieres ou affectations ont changé
     * (pour que la colonne éventuelle "matières" reste cohérente).
     */
    @Override
    public void onDataChanged(EventType event) {
        // Le ProfPanel n'affiche pas de colonne matière pour l'instant,
        // mais on recharge quand même pour garder les stats à jour.
        switch (event) {
            case MATIERE_ADDED:
            case MATIERE_UPDATED:
            case MATIERE_DELETED:
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
        JLabel titleLabel = new JLabel("Liste des Professeurs");
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
        JButton addButton     = createButton("+ Nouveau");
        JButton editButton    = createButton("Editer");
        JButton deleteButton  = createButton("Retirer");
        JButton refreshButton = createButton("Rafraichir");
        addButton.addActionListener(e -> addProfesseur());
        editButton.addActionListener(e -> editProfesseur());
        deleteButton.addActionListener(e -> deleteProfesseur());
        refreshButton.addActionListener(e -> loadData());
        searchPanel.add(searchField);
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
        String[] columns = {"", "ID", "Nom", "Prenom", "Email", "Telephone", "Min/Semaine"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return c == CHK_COL; }
            public Class<?> getColumnClass(int c) { return c == CHK_COL ? Boolean.class : Object.class; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(36);
        table.setFont(new Font("Tahoma", Font.PLAIN, 12));
        table.setShowGrid(true);
        table.setGridColor(new Color(200, 200, 200));
        table.setSelectionBackground(new Color(210, 228, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        selectAllBox = new JCheckBox();
        selectAllBox.setBackground(HEADER_BG);
        selectAllBox.setHorizontalAlignment(SwingConstants.CENTER);
        selectAllBox.addActionListener(e -> toggleSelectAll(selectAllBox.isSelected()));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Tahoma", Font.BOLD, 13));
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        final Color hBg = HEADER_BG;
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean foc, int row, int col) {
                if (col == CHK_COL) { selectAllBox.setSize(new Dimension(table.getColumnModel().getColumn(CHK_COL).getWidth(), 42)); return selectAllBox; }
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                setBackground(hBg); setForeground(Color.WHITE);
                setFont(new Font("Tahoma", Font.BOLD, 13));
                setHorizontalAlignment(CENTER); setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                setOpaque(true); return this;
            }
        });
        header.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int col = header.columnAtPoint(e.getPoint());
                if (col == CHK_COL) { selectAllBox.setSelected(!selectAllBox.isSelected()); toggleSelectAll(selectAllBox.isSelected()); }
            }
        });
        table.getColumnModel().getColumn(CHK_COL).setCellRenderer(new CheckboxCellRenderer());
        table.getColumnModel().getColumn(CHK_COL).setPreferredWidth(36);
        table.getColumnModel().getColumn(CHK_COL).setMaxWidth(36);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.setDefaultRenderer(Object.class, new BwTableCellRenderer());
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col != CHK_COL) {
                    boolean cur = (Boolean) tableModel.getValueAt(row, CHK_COL);
                    tableModel.setValueAt(!cur, row, CHK_COL);
                    updateSelectAllState();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void toggleSelectAll(boolean checked) {
        for (int i = 0; i < tableModel.getRowCount(); i++) tableModel.setValueAt(checked, i, CHK_COL);
    }
    private void updateSelectAllState() {
        int total = tableModel.getRowCount(), checked = 0;
        for (int i = 0; i < total; i++) if ((Boolean) tableModel.getValueAt(i, CHK_COL)) checked++;
        selectAllBox.setSelected(checked == total && total > 0);
        table.getTableHeader().repaint();
    }
    private java.util.List<Integer> getCheckedRows() {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++)
            if ((Boolean) tableModel.getValueAt(i, CHK_COL)) list.add(i);
        return list;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(BACKGROUND_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
        statsLabel.setForeground(Color.BLACK);
        updateStats();
        bottomPanel.add(statsLabel);
        return bottomPanel;
    }
    private void updateStats() {
        if (statsLabel != null) statsLabel.setText("Professeurs enregistres : " + profService.getAllProfesseurs().size());
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
        tableModel.setRowCount(0);
        for (Professeur p : profService.getAllProfesseurs()) {
            boolean match = searchText.isEmpty() ||
                p.getNom().toLowerCase().contains(searchText) ||
                p.getPrenom().toLowerCase().contains(searchText) ||
                p.getEmail().toLowerCase().contains(searchText) ||
                p.getId().toLowerCase().contains(searchText);
            if (match) {
                tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    p.getId(), p.getNom(), p.getPrenom(), p.getEmail(),
                    p.getTelephone(), p.getMinutesParSemaine()
                });
            }
        }
        if (selectAllBox != null) selectAllBox.setSelected(false);
        updateStats();
    }

    private boolean emailExisteDeja(String email, String excludeId) {
        for (Professeur p : profService.getAllProfesseurs()) {
            if (p.getEmail().equalsIgnoreCase(email.trim()) &&
                (excludeId == null || !p.getId().equals(excludeId))) return true;
        }
        return false;
    }

    private void loadData() { filterTable(); }
    private void addProfesseur()  { showProfDialog(null); }
    private void editProfesseur() {
        java.util.List<Integer> checked = getCheckedRows();
        if (checked.isEmpty()) { JOptionPane.showMessageDialog(this, "Cochez un professeur pour editer", "Attention", JOptionPane.WARNING_MESSAGE); return; }
        if (checked.size() > 1) { JOptionPane.showMessageDialog(this, "Cochez un seul professeur pour editer", "Attention", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(checked.get(0), 1);
        for (Professeur p : profService.getAllProfesseurs()) {
            if (p.getId().equals(id)) { showProfDialog(p); break; }
        }
    }
    private void deleteProfesseur() {
        java.util.List<Integer> checked = getCheckedRows();
        if (checked.isEmpty()) { JOptionPane.showMessageDialog(this, "Cochez au moins un professeur a supprimer", "Attention", JOptionPane.WARNING_MESSAGE); return; }
        String msg = checked.size() == 1 ? "Supprimer ce professeur ?" : "Supprimer les " + checked.size() + " professeurs coches ?";
        if (JOptionPane.showConfirmDialog(this, msg, "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            int errors = 0;
            for (int i = checked.size() - 1; i >= 0; i--) {
                String id = (String) tableModel.getValueAt(checked.get(i), 1);
                if (!profService.deleteProfesseur(id)) errors++;
            }
            loadData();
            if (errors > 0) JOptionPane.showMessageDialog(this, errors + " professeur(s) n'ont pas pu etre supprimes", "Erreur", JOptionPane.ERROR_MESSAGE);
            else DataSyncManager.getInstance().fireEvent(EventType.PROF_DELETED, this);
        }
    }

    private void showProfDialog(Professeur prof) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            prof == null ? "Ajouter un professeur" : "Modifier un professeur", true);
        dialog.setSize(510, 460);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nomField       = createTextField();
        JTextField prenomField    = createTextField();
        JTextField emailField     = createTextField();
        JTextField telephoneField = createTextField();
        JSpinner heuresSpinner    = new JSpinner(new SpinnerNumberModel(480, 0, 2400, 30));
        heuresSpinner.setFont(new Font("Tahoma", Font.PLAIN, 12));
        JLabel conflitLabel = new JLabel("  ");
        conflitLabel.setFont(new Font("Tahoma", Font.BOLD, 11));

        if (prof != null) {
            nomField.setText(prof.getNom());
            prenomField.setText(prof.getPrenom());
            emailField.setText(prof.getEmail());
            telephoneField.setText(prof.getTelephone());
            heuresSpinner.setValue(prof.getMinutesParSemaine());
        }
        final String excludeId = prof != null ? prof.getId() : null;
        DocumentListener dlEmail = new DocumentListener() {
            private void verifier() {
                String email = emailField.getText().trim();
                if (email.isEmpty()) { conflitLabel.setText("  "); emailField.setBackground(Color.WHITE); }
                else if (emailExisteDeja(email, excludeId)) { conflitLabel.setText("  ⚠ Conflit : cet email est deja utilise !"); conflitLabel.setForeground(new Color(180, 0, 0)); emailField.setBackground(CONFLICT_BG); }
                else { conflitLabel.setText("  ✓ Email disponible"); conflitLabel.setForeground(new Color(0, 130, 0)); emailField.setBackground(OK_BG); }
            }
            public void insertUpdate(DocumentEvent e)  { verifier(); }
            public void removeUpdate(DocumentEvent e)  { verifier(); }
            public void changedUpdate(DocumentEvent e) { verifier(); }
        };
        emailField.getDocument().addDocumentListener(dlEmail);

        int r = 0;
        addField(panel, gbc, r++, "Nom:", nomField);
        addField(panel, gbc, r++, "Prenom:", prenomField);
        addField(panel, gbc, r++, "Email:", emailField);
        gbc.gridx = 1; gbc.gridy = r++; panel.add(conflitLabel, gbc);
        addField(panel, gbc, r++, "Telephone:", telephoneField);
        addField(panel, gbc, r++, "Min/semaine:", heuresSpinner);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton saveBtn   = createButton("Sauvegarder");
        JButton cancelBtn = createButton("Fermer");
        saveBtn.addActionListener(e -> {
            String nom = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String email = emailField.getText().trim();
            if (nom.isEmpty() || prenom.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Remplir les champs obligatoires", "Erreur", JOptionPane.ERROR_MESSAGE); return; }
            if (!email.isEmpty() && emailExisteDeja(email, excludeId)) { JOptionPane.showMessageDialog(dialog, "Conflit : cet email est deja utilise par un autre professeur.", "Conflit detecte", JOptionPane.ERROR_MESSAGE); emailField.requestFocus(); return; }
            if (prof == null) {
                Professeur newP = new Professeur(UUID.randomUUID().toString(), nom, prenom, email, telephoneField.getText().trim());
                newP.setMinutesParSemaine((int)heuresSpinner.getValue());
                if (profService.addProfesseur(newP)) {
                    dialog.dispose();
                    loadData();
                    DataSyncManager.getInstance().fireEvent(EventType.PROF_ADDED, ProfPanel.this);
                }
            } else {
                prof.setNom(nom); prof.setPrenom(prenom);
                prof.setEmail(email); prof.setTelephone(telephoneField.getText().trim());
                prof.setMinutesParSemaine((int)heuresSpinner.getValue());
                if (profService.updateProfesseur(prof)) {
                    dialog.dispose();
                    loadData();
                    DataSyncManager.getInstance().fireEvent(EventType.PROF_UPDATED, ProfPanel.this);
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

    class CheckboxCellRenderer extends JCheckBox implements TableCellRenderer {
        public CheckboxCellRenderer() { setHorizontalAlignment(SwingConstants.CENTER); setOpaque(true); }
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setSelected(value != null && (Boolean) value);
            setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
            return this;
        }
    }

    class BwTableCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
            setOpaque(true); setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            setFont(new Font("Tahoma", Font.PLAIN, 12)); setForeground(Color.BLACK);
            boolean checked = (Boolean) tableModel.getValueAt(row, CHK_COL);
            setBackground(checked ? new Color(210, 228, 255) : (row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245)));
            setHorizontalAlignment(LEFT);
            return this;
        }
    }
}
