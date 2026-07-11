package main.presentation.panels;

import main.model.Salle;
import main.service.SalleService;
import main.utils.Constants;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.UUID;

public class SallePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private SalleService salleService;
    private JTextField searchField;
    private JComboBox<String> siteFilterCombo;
    private JCheckBox selectAllBox;

    private final Color BACKGROUND_COLOR = Color.WHITE;
    private final Color HEADER_BG        = new Color(50, 50, 50);
    private final Color ROW_ODD          = new Color(245, 245, 245);
    private final Color SELECTION_COLOR  = new Color(220, 235, 255);
    private final Color CONFLICT_BG      = new Color(255, 220, 220);
    private final Color OK_BG            = new Color(220, 255, 220);
    // index de la colonne checkbox
    private static final int CHK_COL = 0;

    public SallePanel() {
        salleService = new SalleService();
        initializeComponents();
        loadData();
        setupKeyboardShortcuts();
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
        JLabel titleLabel = new JLabel("Repertoire des Salles");
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
        siteFilterCombo = new JComboBox<>();
        siteFilterCombo.setFont(new Font("Tahoma", Font.PLAIN, 12));
        siteFilterCombo.setBackground(Color.WHITE);
        siteFilterCombo.setForeground(Color.BLACK);
        siteFilterCombo.addItem("Tous les sites");
        for (String site : Constants.SITES) siteFilterCombo.addItem(site);
        siteFilterCombo.addActionListener(e -> filterTable());
        JButton addButton     = createButton("+ Nouveau");
        JButton editButton    = createButton("Editer");
        JButton deleteButton  = createButton("Retirer");
        JButton refreshButton = createButton("Rafraichir");
        addButton.addActionListener(e -> addSalle());
        editButton.addActionListener(e -> editSalle());
        deleteButton.addActionListener(e -> deleteSalle());
        refreshButton.addActionListener(e -> loadData());
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Site:"));
        searchPanel.add(siteFilterCombo);
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
        // Colonne 0 = checkbox, puis les donnees
        String[] columns = {"", "ID", "Nom", "Site", "Etage", "Type", "Capacite"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return c == CHK_COL; }
            public Class<?> getColumnClass(int c) { return c == CHK_COL ? Boolean.class : Object.class; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(36);
        table.setFont(new Font("Tahoma", Font.PLAIN, 12));
        table.setShowGrid(true);
        table.setGridColor(new Color(200, 200, 200));
        table.setSelectionBackground(SELECTION_COLOR);
        table.setSelectionForeground(Color.BLACK);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Checkbox dans le header pour tout cocher/decocher
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
                if (col == CHK_COL) {
                    selectAllBox.setSize(new Dimension(table.getColumnModel().getColumn(CHK_COL).getWidth(), 42));
                    return selectAllBox;
                }
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                setBackground(hBg); setForeground(Color.WHITE);
                setFont(new Font("Tahoma", Font.BOLD, 13));
                setHorizontalAlignment(CENTER); setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                setOpaque(true); return this;
            }
        });
        // Clic sur header col 0 → toggle all
        header.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int col = header.columnAtPoint(e.getPoint());
                if (col == CHK_COL) {
                    selectAllBox.setSelected(!selectAllBox.isSelected());
                    toggleSelectAll(selectAllBox.isSelected());
                }
            }
        });

        // Renderer checkbox colonne 0
        table.getColumnModel().getColumn(CHK_COL).setCellRenderer(new CheckboxCellRenderer());
        table.getColumnModel().getColumn(CHK_COL).setPreferredWidth(36);
        table.getColumnModel().getColumn(CHK_COL).setMaxWidth(36);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.setDefaultRenderer(Object.class, new BwTableCellRenderer());

        // Clic sur une ligne → toggle sa checkbox
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
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void toggleSelectAll(boolean checked) {
        for (int i = 0; i < tableModel.getRowCount(); i++)
            tableModel.setValueAt(checked, i, CHK_COL);
    }

    private void updateSelectAllState() {
        int total = tableModel.getRowCount();
        int checked = 0;
        for (int i = 0; i < total; i++)
            if ((Boolean) tableModel.getValueAt(i, CHK_COL)) checked++;
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
        int total = salleService.getAllSalles().size();
        JLabel statsLabel = new JLabel("Total des salles: " + total);
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
        String selectedSite = (String) siteFilterCombo.getSelectedItem();
        tableModel.setRowCount(0);
        for (Salle salle : salleService.getAllSalles()) {
            boolean matchSearch = searchText.isEmpty() ||
                salle.getNom().toLowerCase().contains(searchText) ||
                salle.getSite().toLowerCase().contains(searchText) ||
                salle.getType().toLowerCase().contains(searchText);
            boolean matchSite = selectedSite.equals("Tous les sites") || salle.getSite().equals(selectedSite);
            if (matchSearch && matchSite) {
                tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    salle.getId(),
                    salle.getNom(), salle.getSite(), salle.getEtage(), salle.getType(), salle.getCapacite()
                });
            }
        }
        if (selectAllBox != null) selectAllBox.setSelected(false);
    }

    private boolean nomSalleExisteDeja(String nom, String excludeId) {
        for (Salle s : salleService.getAllSalles()) {
            if (s.getNom().equalsIgnoreCase(nom.trim()) &&
                (excludeId == null || !s.getId().equals(excludeId))) {
                return true;
            }
        }
        return false;
    }

    private void loadData() { filterTable(); }
    private void addSalle()  { showSalleDialog(null); }

    private void editSalle() {
        java.util.List<Integer> checked = getCheckedRows();
        if (checked.isEmpty()) { JOptionPane.showMessageDialog(this, "Cochez une salle pour editer", "Attention", JOptionPane.WARNING_MESSAGE); return; }
        if (checked.size() > 1) { JOptionPane.showMessageDialog(this, "Cochez une seule salle pour editer", "Attention", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(checked.get(0), 1);
        Salle s = salleService.getSalle(id);
        if (s != null) showSalleDialog(s);
    }

    private void deleteSalle() {
        java.util.List<Integer> checked = getCheckedRows();
        if (checked.isEmpty()) { JOptionPane.showMessageDialog(this, "Cochez au moins une salle a supprimer", "Attention", JOptionPane.WARNING_MESSAGE); return; }
        String msg = checked.size() == 1 ? "Supprimer cette salle ?" : "Supprimer les " + checked.size() + " salles cochees ?";
        if (JOptionPane.showConfirmDialog(this, msg, "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            int errors = 0;
            for (int i = checked.size() - 1; i >= 0; i--) {
                String id = (String) tableModel.getValueAt(checked.get(i), 1);
                if (!salleService.deleteSalle(id)) errors++;
            }
            loadData();
            if (errors > 0) JOptionPane.showMessageDialog(this, errors + " salle(s) n'ont pas pu etre supprimee(s)", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSalleDialog(Salle salle) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            salle == null ? "Ajouter une salle" : "Modifier une salle", true);
        dialog.setSize(490, 440);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nomField = createTextField();
        JComboBox<String> siteCombo = new JComboBox<>();
        siteCombo.setFont(new Font("Tahoma", Font.PLAIN, 12));
        siteCombo.setBackground(Color.WHITE);
        siteCombo.setForeground(Color.BLACK);
        for (String site : Constants.SITES) siteCombo.addItem(site);
        JSpinner etageSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        etageSpinner.setFont(new Font("Tahoma", Font.PLAIN, 12));
        JTextField typeField = createTextField();
        JSpinner capaciteSpinner = new JSpinner(new SpinnerNumberModel(30, 10, 500, 5));
        capaciteSpinner.setFont(new Font("Tahoma", Font.PLAIN, 12));

        JLabel conflitLabel = new JLabel("  ");
        conflitLabel.setFont(new Font("Tahoma", Font.BOLD, 11));

        if (salle != null) {
            nomField.setText(salle.getNom());
            siteCombo.setSelectedItem(salle.getSite());
            etageSpinner.setValue(salle.getEtage());
            typeField.setText(salle.getType());
            capaciteSpinner.setValue(salle.getCapacite());
        }

        final String excludeId = salle != null ? salle.getId() : null;

        DocumentListener dl = new DocumentListener() {
            private void verifier() {
                String nom = nomField.getText().trim();
                if (nom.isEmpty()) {
                    conflitLabel.setText("  ");
                    nomField.setBackground(Color.WHITE);
                } else if (nomSalleExisteDeja(nom, excludeId)) {
                    conflitLabel.setText("  ⚠ Conflit : nom de salle deja utilise !");
                    conflitLabel.setForeground(new Color(180, 0, 0));
                    nomField.setBackground(CONFLICT_BG);
                } else {
                    conflitLabel.setText("  ✓ Nom disponible");
                    conflitLabel.setForeground(new Color(0, 130, 0));
                    nomField.setBackground(OK_BG);
                }
            }
            public void insertUpdate(DocumentEvent e)  { verifier(); }
            public void removeUpdate(DocumentEvent e)  { verifier(); }
            public void changedUpdate(DocumentEvent e) { verifier(); }
        };
        nomField.getDocument().addDocumentListener(dl);

        int r = 0;
        addField(panel, gbc, r++, "Nom:", nomField);
        gbc.gridx = 1; gbc.gridy = r++;
        panel.add(conflitLabel, gbc);
        addField(panel, gbc, r++, "Site:", siteCombo);
        addField(panel, gbc, r++, "Etage:", etageSpinner);
        addField(panel, gbc, r++, "Type:", typeField);
        addField(panel, gbc, r++, "Capacite:", capaciteSpinner);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton saveBtn = createButton("Sauvegarder");
        JButton cancelBtn = createButton("Fermer");
        saveBtn.addActionListener(e -> {
            String nom = nomField.getText().trim();
            String type = typeField.getText().trim().toUpperCase();
            if (nom.isEmpty() || type.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Remplir tous les champs", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (nomSalleExisteDeja(nom, excludeId)) {
                JOptionPane.showMessageDialog(dialog,
                    "Conflit : une salle avec ce nom existe deja.\nVeuillez choisir un autre nom.",
                    "Conflit detecte", JOptionPane.ERROR_MESSAGE);
                nomField.requestFocus();
                return;
            }
            if (salle == null) {
                Salle newS = new Salle(UUID.randomUUID().toString(), nom, (String)siteCombo.getSelectedItem(),
                    (int)etageSpinner.getValue(), type, (int)capaciteSpinner.getValue());
                if (salleService.addSalle(newS)) { dialog.dispose(); loadData(); }
            } else {
                salle.setNom(nom); salle.setSite((String)siteCombo.getSelectedItem());
                salle.setEtage((int)etageSpinner.getValue()); salle.setType(type);
                salle.setCapacite((int)capaciteSpinner.getValue());
                if (salleService.updateSalle(salle)) { dialog.dispose(); loadData(); }
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

    // Renderer pour la colonne checkbox
    class CheckboxCellRenderer extends JCheckBox implements TableCellRenderer {
        public CheckboxCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
        }
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setSelected(value != null && (Boolean) value);
            setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
            return this;
        }
    }

    class BwTableCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            setFont(new Font("Tahoma", Font.PLAIN, 12));
            setForeground(Color.BLACK);
            boolean checked = (Boolean) tableModel.getValueAt(row, CHK_COL);
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
