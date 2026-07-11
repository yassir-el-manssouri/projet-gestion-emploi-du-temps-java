package main.presentation.panels;

import main.model.Matiere;
import main.model.Professeur;
import main.service.MatiereService;
import main.service.ProfService;
import main.utils.DataSyncManager;
import main.utils.DataSyncManager.EventType;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel d'affectation des professeurs aux matieres.
 * L'administrateur choisit quelle(s) matiere(s) est enseignee par quel(s) prof(s).
 * Chaque matiere doit avoir au moins un prof affecte.
 */
public class AffectationPanel extends JPanel implements DataSyncManager.DataChangeListener {

    private MatiereService matiereService;
    private ProfService profService;

    // Panneau gauche : liste des matieres
    private JTable matiereTable;
    private DefaultTableModel matiereTableModel;
    private JTextField searchMatiereField;
    private JComboBox<String> filiereFilterCombo;

    // Panneau droite : profs affectes + profs disponibles
    private JList<String> profsAffectesListUI;
    private DefaultListModel<String> profsAffectesModel;
    private JList<String> profsDispoListUI;
    private DefaultListModel<String> profsDispoModel;

    private JLabel matiereSelecteeLabel;
    private JLabel statLabel;

    // Matiere couramment selectionnee
    private Matiere matiereSelectionnee = null;

    private final Color BG           = Color.WHITE;
    private final Color HEADER_BG    = new Color(50, 50, 50);
    private final Color OK_COLOR     = new Color(0, 130, 0);
    private final Color WARN_COLOR   = new Color(180, 0, 0);
    private final Color ACCENT       = new Color(220, 220, 220);

    public AffectationPanel() {
        matiereService = new MatiereService();
        profService    = new ProfService();
        initUI();
        loadMatieres();
        DataSyncManager.getInstance().addListener(this);
    }

    /**
     * Réagit aux événements extérieurs (ProfPanel / MatierePanel).
     *  - Prof supprimé  → nettoyer ses affectations dans toutes les matières,
     *                     puis recharger les deux panneaux
     *  - Prof modifié   → rafraîchir la liste des profs (nom affiché peut changer)
     *  - Prof ajouté    → rafraîchir les profs disponibles
     *  - Matière supprimée / ajoutée / modifiée → recharger le tableau des matières
     */
    @Override
    public void onDataChanged(EventType event) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            switch (event) {
                case PROF_DELETED:
                    // Nettoyage en cascade : retirer le prof de toutes les matières
                    nettoyerProfsSupprimes();
                    loadMatieres();
                    refreshRightPanel();
                    break;
                case PROF_ADDED:
                case PROF_UPDATED:
                    refreshRightPanel();
                    break;
                case MATIERE_ADDED:
                case MATIERE_UPDATED:
                case MATIERE_DELETED:
                    loadMatieres();
                    refreshRightPanel();
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * Suppression en cascade : parcourt toutes les matières et retire les profIds
     * qui ne correspondent plus à un professeur existant.
     */
    private void nettoyerProfsSupprimes() {
        List<String> profsExistantsIds = new ArrayList<>();
        for (Professeur p : profService.getAllProfesseurs()) {
            profsExistantsIds.add(p.getId());
        }
        for (Matiere m : matiereService.getAllMatieres()) {
            List<String> profIds = m.getProfIds();
            if (profIds != null) {
                boolean changed = profIds.removeIf(id -> !profsExistantsIds.contains(id));
                if (changed) {
                    matiereService.updateMatiere(m);
                }
            }
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(BG);

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildCenter(),    BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ─── Barre du haut ──────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setBackground(BG);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("Affectation Professeurs / Matieres");
        title.setFont(new Font("Tahoma", Font.BOLD, 18));
        title.setForeground(Color.BLACK);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(BG);

        searchMatiereField = new JTextField(14);
        styleField(searchMatiereField);
        searchMatiereField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { loadMatieres(); }
        });

        filiereFilterCombo = new JComboBox<>(new String[]{
            "Toutes filieres","AP","GC","GF","IIR","GESI","GI","IAII","IFA","IR-CIR","IR-DEV","IR-IA","GII"
        });
        styleCombo(filiereFilterCombo);
        filiereFilterCombo.addActionListener(e -> loadMatieres());

        JButton btnRefresh = createButton("Rafraichir");
        btnRefresh.addActionListener(e -> { loadMatieres(); refreshRightPanel(); });

        right.add(new JLabel("Recherche:"));
        right.add(searchMatiereField);
        right.add(new JLabel("Filiere:"));
        right.add(filiereFilterCombo);
        right.add(btnRefresh);

        top.add(title, BorderLayout.WEST);
        top.add(right,  BorderLayout.EAST);
        return top;
    }

    // ─── Zone centrale : gauche (matieres) + droite (affectation) ───────────

    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeftPanel(), buildRightPanel());
        split.setDividerLocation(580);
        split.setResizeWeight(0.55);
        split.setBorder(null);
        split.setBackground(BG);
        return split;
    }

    // Panneau gauche : tableau des matieres
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            "  Matieres  ", 0, 0,
            new Font("Tahoma", Font.BOLD, 13), Color.BLACK));

        String[] cols = {"ID", "Nom Matiere", "Filiere", "Niv.", "Profs affectes"};
        matiereTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        matiereTable = new JTable(matiereTableModel);
        matiereTable.setRowHeight(34);
        matiereTable.setFont(new Font("Tahoma", Font.PLAIN, 12));
        matiereTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        matiereTable.setShowGrid(true);
        matiereTable.setGridColor(new Color(200, 200, 200));
        matiereTable.setSelectionBackground(ACCENT);
        matiereTable.setSelectionForeground(Color.BLACK);

        // En-tete
        JTableHeader header = matiereTable.getTableHeader();
        header.setFont(new Font("Tahoma", Font.BOLD, 12));
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 38));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(HEADER_BG); setForeground(Color.WHITE);
                setFont(new Font("Tahoma", Font.BOLD, 12));
                setHorizontalAlignment(CENTER); setOpaque(true);
                return this;
            }
        });

        // Renderer colonne "Profs affectes" : vert si >= 1, rouge si 0
        matiereTable.getColumn("Profs affectes").setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setHorizontalAlignment(CENTER);
                setForeground(Color.BLACK);
                String val = v != null ? v.toString() : "0";
                int count = 0;
                try { count = Integer.parseInt(val.replaceAll("[^0-9]","")); } catch(Exception ignored){}
                if (sel) {
                    setBackground(ACCENT);
                } else {
                    setBackground(count > 0 ? new Color(220, 255, 220) : new Color(255, 220, 220));
                }
                return this;
            }
        });

        matiereTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        matiereTable.getColumnModel().getColumn(0).setMaxWidth(0);
        matiereTable.getColumnModel().getColumn(0).setMinWidth(0);
        matiereTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        matiereTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        matiereTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        matiereTable.getColumnModel().getColumn(4).setPreferredWidth(110);

        matiereTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onMatiereSelected();
        });

        p.add(new JScrollPane(matiereTable), BorderLayout.CENTER);
        return p;
    }

    // Panneau droit : profs affectes + profs disponibles
    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            "  Gestion des affectations  ", 0, 0,
            new Font("Tahoma", Font.BOLD, 13), Color.BLACK));
        p.setPreferredSize(new Dimension(380, 0));

        // Titre matiere selectionnee
        matiereSelecteeLabel = new JLabel("← Selectionnez une matiere");
        matiereSelecteeLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        matiereSelecteeLabel.setForeground(new Color(80, 80, 80));
        matiereSelecteeLabel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        // Profs deja affectes
        JLabel lblAffectes = new JLabel("Professeurs affectes a cette matiere :");
        lblAffectes.setFont(new Font("Tahoma", Font.BOLD, 12));
        profsAffectesModel = new DefaultListModel<>();
        profsAffectesListUI = new JList<>(profsAffectesModel);
        styleList(profsAffectesListUI);

        JButton btnRetirer = createButton("← Retirer");
        btnRetirer.addActionListener(e -> retirerProf());
        btnRetirer.setToolTipText("Retirer le prof selectionne de cette matiere");

        JPanel affectesPanel = new JPanel(new BorderLayout(0, 4));
        affectesPanel.setBackground(BG);
        affectesPanel.add(lblAffectes, BorderLayout.NORTH);
        affectesPanel.add(new JScrollPane(profsAffectesListUI), BorderLayout.CENTER);
        JPanel retirerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        retirerPanel.setBackground(BG);
        retirerPanel.add(btnRetirer);
        affectesPanel.add(retirerPanel, BorderLayout.SOUTH);

        // Separateur + bouton Affecter
        JButton btnAffecter = createButton("Affecter →");
        btnAffecter.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnAffecter.addActionListener(e -> affecterProf());
        btnAffecter.setToolTipText("Affecter le prof selectionne a cette matiere");

        JPanel midPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        midPanel.setBackground(BG);
        midPanel.add(btnAffecter);

        // Profs disponibles (non encore affectes a cette matiere)
        JLabel lblDispo = new JLabel("Professeurs disponibles :");
        lblDispo.setFont(new Font("Tahoma", Font.BOLD, 12));
        profsDispoModel = new DefaultListModel<>();
        profsDispoListUI = new JList<>(profsDispoModel);
        styleList(profsDispoListUI);

        JPanel dispoPanel = new JPanel(new BorderLayout(0, 4));
        dispoPanel.setBackground(BG);
        dispoPanel.add(lblDispo, BorderLayout.NORTH);
        dispoPanel.add(new JScrollPane(profsDispoListUI), BorderLayout.CENTER);

        // Assemblage
        JPanel body = new JPanel(new GridLayout(3, 1, 0, 8));
        body.setBackground(BG);
        body.add(affectesPanel);
        body.add(midPanel);
        body.add(dispoPanel);

        p.add(matiereSelecteeLabel, BorderLayout.NORTH);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    // ─── Barre de statut ────────────────────────────────────────────────────

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(BG);
        statLabel = new JLabel("Pret.");
        statLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
        statLabel.setForeground(Color.DARK_GRAY);
        bar.add(statLabel);
        return bar;
    }

    // ─── Chargement des donnees ──────────────────────────────────────────────

    private void loadMatieres() {
        matiereTableModel.setRowCount(0);
        String search   = searchMatiereField.getText().toLowerCase();
        String filiere  = (String) filiereFilterCombo.getSelectedItem();

        int total = 0, sansProf = 0;
        for (Matiere m : matiereService.getAllMatieres()) {
            boolean matchSearch  = search.isEmpty()
                || m.getNom().toLowerCase().contains(search)
                || m.getFiliere().toLowerCase().contains(search);
            boolean matchFiliere = "Toutes filieres".equals(filiere) || m.getFiliere().equals(filiere);
            if (matchSearch && matchFiliere) {
                int nbProfs = m.getProfIds() == null ? 0 : m.getProfIds().size();
                matiereTableModel.addRow(new Object[]{
                    m.getId(),
                    m.getNom(),
                    m.getFiliere(),
                    m.getNiveau(),
                    nbProfs + " prof(s)"
                });
                total++;
                if (nbProfs == 0) sansProf++;
            }
        }
        statLabel.setText("Matieres affichees: " + total
            + "   |   Sans prof: " + sansProf
            + (sansProf > 0 ? "  ⚠ Toute matiere doit avoir au moins 1 prof !" : "  ✓ Toutes les matieres ont un prof"));
        statLabel.setForeground(sansProf > 0 ? WARN_COLOR : OK_COLOR);
    }

    private void onMatiereSelected() {
        int row = matiereTable.getSelectedRow();
        if (row == -1) {
            matiereSelectionnee = null;
            matiereSelecteeLabel.setText("← Selectionnez une matiere");
            profsAffectesModel.clear();
            profsDispoModel.clear();
            return;
        }
        String id = (String) matiereTableModel.getValueAt(row, 0);
        matiereSelectionnee = matiereService.getMatiere(id);
        if (matiereSelectionnee != null) {
            matiereSelecteeLabel.setText("Matiere : " + matiereSelectionnee.getNom()
                + "  [" + matiereSelectionnee.getFiliere() + " - " + matiereSelectionnee.getNiveau() + "]");
        }
        refreshRightPanel();
    }

    private void refreshRightPanel() {
        profsAffectesModel.clear();
        profsDispoModel.clear();
        if (matiereSelectionnee == null) return;

        // Recharger depuis le service (donnees fraiches)
        matiereSelectionnee = matiereService.getMatiere(matiereSelectionnee.getId());
        if (matiereSelectionnee == null) return;

        List<String> affectesIds = matiereSelectionnee.getProfIds();
        List<Professeur> tousProfs = profService.getAllProfesseurs();

        for (Professeur p : tousProfs) {
            if (affectesIds != null && affectesIds.contains(p.getId())) {
                profsAffectesModel.addElement(p.getId() + " | " + p.getFullName());
            } else {
                profsDispoModel.addElement(p.getId() + " | " + p.getFullName());
            }
        }
    }

    // ─── Actions affectation / retrait ──────────────────────────────────────

    private void affecterProf() {
        if (matiereSelectionnee == null) {
            JOptionPane.showMessageDialog(this,
                "Veuillez d'abord selectionner une matiere dans le tableau.",
                "Aucune matiere selectionnee", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String selected = profsDispoListUI.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                "Veuillez selectionner un professeur dans la liste des disponibles.",
                "Aucun prof selectionne", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String profId = selected.split(" \\| ")[0];
        boolean ok = matiereService.affecterProfAMatiere(matiereSelectionnee.getId(), profId);
        if (ok) {
            refreshRightPanel();
            loadMatieres();
            // Reselectionner la meme ligne
            reselectionnerMatiere();
            statLabel.setText("Prof affecte avec succes !");
            statLabel.setForeground(OK_COLOR);
            DataSyncManager.getInstance().fireEvent(EventType.AFFECTATION_CHANGED, this);
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'affectation.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void retirerProf() {
        if (matiereSelectionnee == null) return;
        String selected = profsAffectesListUI.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                "Veuillez selectionner un professeur dans la liste des affectes.",
                "Aucun prof selectionne", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String profId = selected.split(" \\| ")[0];

        // Avertissement si c'est le dernier prof
        List<String> profIds = matiereSelectionnee.getProfIds();
        if (profIds != null && profIds.size() == 1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Attention : ceci va retirer le dernier professeur de cette matiere.\n" +
                "Chaque matiere doit avoir au moins 1 prof. Continuer quand meme ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        boolean ok = matiereService.retirerProfDeMatiere(matiereSelectionnee.getId(), profId);
        if (ok) {
            refreshRightPanel();
            loadMatieres();
            reselectionnerMatiere();
            statLabel.setText("Prof retire avec succes.");
            statLabel.setForeground(WARN_COLOR);
            DataSyncManager.getInstance().fireEvent(EventType.AFFECTATION_CHANGED, this);
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors du retrait.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Re-selectionne la matiere courante dans le tableau apres rechargement. */
    private void reselectionnerMatiere() {
        if (matiereSelectionnee == null) return;
        String targetId = matiereSelectionnee.getId();
        for (int i = 0; i < matiereTableModel.getRowCount(); i++) {
            if (targetId.equals(matiereTableModel.getValueAt(i, 0))) {
                matiereTable.setRowSelectionInterval(i, i);
                matiereTable.scrollRectToVisible(matiereTable.getCellRect(i, 0, true));
                break;
            }
        }
    }

    // ─── Helpers UI ─────────────────────────────────────────────────────────

    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Tahoma", Font.BOLD, 11));
        b.setBackground(Color.WHITE);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(ACCENT); }
            public void mouseExited(MouseEvent e)  { b.setBackground(Color.WHITE); }
        });
        return b;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Tahoma", Font.PLAIN, 12));
        f.setForeground(Color.BLACK);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(5, 7, 5, 7)));
    }

    private void styleCombo(JComboBox<String> c) {
        c.setFont(new Font("Tahoma", Font.PLAIN, 12));
        c.setBackground(Color.WHITE);
        c.setForeground(Color.BLACK);
    }

    private void styleList(JList<String> list) {
        list.setFont(new Font("Tahoma", Font.PLAIN, 12));
        list.setForeground(Color.BLACK);
        list.setBackground(Color.WHITE);
        list.setSelectionBackground(ACCENT);
        list.setSelectionForeground(Color.BLACK);
        list.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
        list.setFixedCellHeight(28);
    }
}
