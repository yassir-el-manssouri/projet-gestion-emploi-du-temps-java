package main.presentation.panels;

import main.model.*;
import main.service.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Collections;
import java.io.*;

public class EmploiPanel extends JPanel {
    private JComboBox<String> classeCombo;
    private JTable emploiTable;
    private DefaultTableModel tableModel;
    private EmploiService emploiService;
    private ClasseService classeService;
    private MatiereService matiereService;
    private ProfService profService;
    private SalleService salleService;

    private JLabel classeLabel;
    private JButton generateButton;
    private JButton exportButton;
    private JButton clearButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    // Palette noir et blanc uniquement
    private final Color PRIMARY_COLOR   = Color.BLACK;
    private final Color BACKGROUND_COLOR = Color.WHITE;
    private final Color HEADER_BG       = new Color(180, 180, 180);
    private final Color CELL_FILLED_BG  = new Color(230, 230, 230);
    private final Color CELL_EMPTY_BG   = Color.WHITE;
    private final Color GRID_COLOR      = new Color(100, 100, 100);

    public EmploiPanel() {
        emploiService  = new EmploiService();
        classeService  = new ClasseService();
        matiereService = new MatiereService();
        profService    = new ProfService();
        salleService   = new SalleService();
        initializeComponents();
        loadClasses();
        setupKeyboardShortcuts();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(BACKGROUND_COLOR);

        add(createStyledTopPanel(),   BorderLayout.NORTH);
        add(createStyledTablePanel(), BorderLayout.CENTER);
        add(createStyledBottomPanel(),BorderLayout.SOUTH);
    }

    private JPanel createStyledTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel selectionPanel = new JPanel(new GridBagLayout());
        selectionPanel.setBackground(BACKGROUND_COLOR);
        selectionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        classeLabel = createStyledLabel("Classe:");
        gbc.gridx = 0; gbc.gridy = 0;
        selectionPanel.add(classeLabel, gbc);

        classeCombo = createStyledComboBox();
        classeCombo.setPreferredSize(new Dimension(250, 30));
        classeCombo.addActionListener(e -> onClassSelected());
        gbc.gridx = 1; gbc.gridy = 0;
        selectionPanel.add(classeCombo, gbc);

        gbc.gridx = 3; gbc.gridy = 0;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        generateButton = createStyledButton("Generer");
        generateButton.addActionListener(e -> generateEmploi());

        exportButton = createStyledButton("Exporter TXT");
        exportButton.addActionListener(e -> exportToTxt());


        clearButton = createStyledButton("Effacer");
        clearButton.addActionListener(e -> clearEmploi());

        buttonPanel.add(generateButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 4; gbc.gridy = 0;
        gbc.weightx = 1.0;
        selectionPanel.add(buttonPanel, gbc);

        topPanel.add(selectionPanel, BorderLayout.NORTH);
        return topPanel;
    }

    private JPanel createStyledTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        String[] columns = {"Horaires", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        emploiTable = new JTable(tableModel);
        emploiTable.setRowHeight(70);
        emploiTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        emploiTable.setShowGrid(true);
        emploiTable.setGridColor(GRID_COLOR);
        emploiTable.setSelectionBackground(CELL_FILLED_BG);
        emploiTable.setSelectionForeground(Color.BLACK);

        JTableHeader header = emploiTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBackground(HEADER_BG);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        emploiTable.setDefaultRenderer(Object.class, new StyledTableCellRenderer());

        emploiTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = emploiTable.getSelectedRow();
                    int col = emploiTable.getSelectedColumn();
                    if (row >= 0 && col > 0) editSeance(row, col);
                }
            }
        });

        initializeTimeSlots();

        JScrollPane scrollPane = new JScrollPane(emploiTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStyledBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BACKGROUND_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setForeground(Color.DARK_GRAY);
        bottomPanel.add(progressBar, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(BACKGROUND_COLOR);
        statusLabel = new JLabel("Pret a generer l'emploi du temps");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(Color.DARK_GRAY);
        statusPanel.add(statusLabel);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        return bottomPanel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(Color.BLACK);
        return label;
    }

    private JComboBox<String> createStyledComboBox() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.BLACK);
        return combo;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(new Color(220, 220, 220)); }
            public void mouseExited(MouseEvent e)  { button.setBackground(Color.WHITE); }
        });
        return button;
    }

    private void initializeTimeSlots() {
        LocalTime[] timeSlots = {
            LocalTime.of(8, 0),  LocalTime.of(10, 0),
            LocalTime.of(14, 0), LocalTime.of(16, 0)
        };
        for (LocalTime start : timeSlots) {
            LocalTime end = start.plusMinutes(120);
            String timeSlot = String.format("%02d:%02d - %02d:%02d",
                start.getHour(), start.getMinute(), end.getHour(), end.getMinute());
            tableModel.addRow(new Object[]{timeSlot, "", "", "", "", "", ""});
        }
    }

    private void loadClasses() {
        List<Classe> classes = classeService.getAllClasses();
        classeCombo.removeAllItems();
        for (Classe c : classes) {
            classeCombo.addItem(c.getId() + " - " + c.getNom());
        }
    }

    private void loadSemaines() { /* plus utilise */ }

    // Cle de semaine fixe (pas de filtre par semaine)
    private String getSemaineKey() {
        return "Semaine 1";
    }

    private void onClassSelected() {
        if (classeCombo.getSelectedItem() != null) loadEmploi();
    }

    private void onWeekSelected() { /* plus utilise */ }

    private void loadEmploi() {
        if (classeCombo.getSelectedItem() == null) return;

        String selectedItem = (String) classeCombo.getSelectedItem();
        String classeId = selectedItem.split(" - ")[0];
        String semaine = getSemaineKey();

        clearTable();

        EmploiDuTemps edt = emploiService.getEmploiForClass(classeId, semaine);

        for (Seance seance : edt.getSeances()) {
            int row = getRowForTime(seance.getHeureDebut());
            int col = getColumnForDay(seance.getJour());
            if (row >= 0 && col >= 0) {
                Matiere    matiere = matiereService.getMatiere(seance.getMatiereId());
                Professeur prof    = profService.getProfesseur(seance.getProfesseurId());
                Salle      salle   = salleService.getSalle(seance.getSalleId());

                String nomMatiere = (matiere != null) ? matiere.getNom()     : "";
                String nomProf    = (prof    != null) ? prof.getFullName()   : "";
                String nomSalle   = (salle   != null) ? salle.getNom()       : "";
                String heureDebut = seance.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm"));
                String heureFin   = seance.getHeureFin().format(DateTimeFormatter.ofPattern("HH:mm"));

                String text = String.format(
                    "<html><div style='text-align:center;padding:4px;'>" +
                    "<span style='color:black;font-size:10px;'><b>%s - %s</b></span><br>" +
                    "<b style='color:black;'>%s</b><br>" +
                    "<span style='color:black;font-size:11px;'>%s</span><br>" +
                    "<span style='color:black;font-size:10px;'>%s</span>" +
                    "</div></html>",
                    heureDebut, heureFin, nomMatiere, nomProf, nomSalle
                );
                tableModel.setValueAt(text, row, col);
            }
        }

        statusLabel.setText("Emploi du temps charge pour " + selectedItem + " - " + semaine);
    }

    private void generateEmploi() {
        if (classeCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Veuillez selectionner une classe", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedItem = (String) classeCombo.getSelectedItem();
        String classeId = selectedItem.split(" - ")[0];
        String semaine  = getSemaineKey();

        EmploiDuTemps existingEdt = emploiService.getEmploiForClass(classeId, semaine);
        if (!existingEdt.getSeances().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Un emploi du temps existe deja pour cette classe. Voulez-vous le regenerer ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            for (Seance seance : existingEdt.getSeances()) {
                emploiService.deleteSeance(seance.getId());
            }
        }

        generateAutomaticEmploi(classeId, semaine);
        loadEmploi();

        statusLabel.setText("Emploi du temps genere pour " + selectedItem);
        JOptionPane.showMessageDialog(this,
            "Emploi du temps genere avec succes !\nVous pouvez l'exporter en fichier texte.",
            "Succes", JOptionPane.INFORMATION_MESSAGE);
    }

    private void generateAutomaticEmploi(String classeId, String semaine) {
        Classe classe = classeService.getClasse(classeId);
        if (classe == null) return;

        // Recuperer uniquement les matieres de la filiere ET du niveau de la classe
        List<Matiere> matieres = new ArrayList<>(
            matiereService.getMatieresByFiliereAndNiveau(classe.getFiliere(), classe.getNiveau())
        );
        if (matieres.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Aucune matiere trouvee pour la filiere " + classe.getFiliere()
                + " niveau " + classe.getNiveau() + ".\nVerifiez le catalogue des matieres.",
                "Aucune matiere", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Professeur> tousProfs = profService.getAllProfesseurs();
        List<Salle>      salles    = salleService.getAllSalles();

        if (salles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Veuillez d'abord ajouter des salles.",
                "Salles manquantes", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tousProfs.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Veuillez d'abord ajouter des professeurs.",
                "Profs manquants", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Avertissement non-bloquant si certaines matieres n'ont pas de prof affecte
        List<String> sansProf = new ArrayList<>();
        for (Matiere m : matieres) {
            if (m.getProfIds() == null || m.getProfIds().isEmpty()) sansProf.add(m.getNom());
        }
        if (!sansProf.isEmpty()) {
            int choix = JOptionPane.showConfirmDialog(this,
                "Attention : " + sansProf.size() + " matiere(s) sans prof affecte :\n"
                + String.join(", ", sansProf)
                + "\n\nUn prof disponible sera choisi automatiquement.\nContinuer ?",
                "Profs non affectes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choix != JOptionPane.YES_OPTION) return;
        }

        LocalTime[] timeSlots = {LocalTime.of(8,0), LocalTime.of(10,0), LocalTime.of(14,0), LocalTime.of(16,0)};
        DayOfWeek[] days      = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                  DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};

        // Chaque matiere placee UNE SEULE FOIS, ordre aleatoire pour repartir sur la semaine
        List<Matiere> matieresAplacer = new ArrayList<>(matieres);
        Collections.shuffle(matieresAplacer);

        int dayIdx  = 0;
        int slotIdx = 0;

        for (Matiere matiere : matieresAplacer) {
            boolean placed = false;

            // Utiliser la duree configuree dans la matiere (minutesParSemaine),
            // avec 120 min comme fallback si la valeur est 0 ou negative
            int dureeMinutes = matiere.getMinutesParSemaine() > 0
                ? matiere.getMinutesParSemaine()
                : 120;

            for (int d = 0; d < days.length && !placed; d++) {
                DayOfWeek day = days[(dayIdx + d) % days.length];
                for (int s = 0; s < timeSlots.length && !placed; s++) {
                    LocalTime timeSlot = timeSlots[(slotIdx + s) % timeSlots.length];
                    LocalTime fin = timeSlot.plusMinutes(dureeMinutes);

                    // 1. Chercher parmi les profs AFFECTES a cette matiere (priorite)
                    Professeur selectedProf = null;
                    List<String> profIds = matiere.getProfIds();
                    if (profIds != null) {
                        for (String pid : profIds) {
                            Professeur p = profService.getProfesseur(pid);
                            if (p != null && emploiService.verifierDisponibiliteProf(p, day, timeSlot, fin, semaine)) {
                                selectedProf = p;
                                break;
                            }
                        }
                        // Fallback : premier prof affecte meme s'il a un conflit horaire
                        if (selectedProf == null && !profIds.isEmpty()) {
                            selectedProf = profService.getProfesseur(profIds.get(0));
                        }
                    }

                    // 2. Fallback : aucun prof affecte → prendre n'importe quel prof disponible
                    if (selectedProf == null) {
                        for (Professeur p : tousProfs) {
                            if (emploiService.verifierDisponibiliteProf(p, day, timeSlot, fin, semaine)) {
                                selectedProf = p;
                                break;
                            }
                        }
                        // Dernier recours : premier prof de la liste
                        if (selectedProf == null) selectedProf = tousProfs.get(0);
                    }

                    // 3. Choisir une salle disponible
                    Salle selectedSalle = null;
                    for (Salle salle : salles) {
                        if (emploiService.verifierDisponibiliteSalle(salle, day, timeSlot, fin, semaine)) {
                            selectedSalle = salle;
                            break;
                        }
                    }
                    if (selectedSalle == null) selectedSalle = salles.get(0);

                    // 4. Verifier que la classe n'a pas deja un cours a ce creneau
                    boolean classeLibre = emploiService.verifierDisponibiliteClasse(classeId, day, timeSlot, fin, semaine);
                    if (!classeLibre) continue; // ce creneau est deja pris pour cette classe

                    // 5. Creer et sauvegarder la seance
                    String id = UUID.randomUUID().toString();
                    Seance seance = new Seance(id, classeId, matiere.getId(),
                        selectedProf.getId(), selectedSalle.getId(),
                        day, timeSlot, fin, semaine);
                    emploiService.addSeance(seance);
                    placed = true;
                    slotIdx = (slotIdx + s + 1) % timeSlots.length;
                    dayIdx  = (dayIdx  + d + 1) % days.length;
                }
            }
        }

        autoSaveToFile(classe, semaine);
    }

    private void autoSaveToFile(Classe classe, String semaine) {
        String filename = "emploi_du_temps_" + classe.getId() + "_" + semaine.replace(" ", "_") + ".txt";
        String filepath = "data/exported/" + filename;

        File exportDir = new File("data/exported");
        if (!exportDir.exists()) exportDir.mkdirs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(filepath))) {
            writer.println("=".repeat(80));
            writer.println("                    EMPLOI DU TEMPS - EMSI");
            writer.println("=".repeat(80));
            writer.println();
            writer.printf("Classe: %s (%s - %s)%n", classe.getNom(), classe.getNiveau(), classe.getFiliere());
            writer.printf("Semaine: %s%n", semaine);
            writer.printf("Date d'export: %s%n", new Date().toString());
            writer.println();
            writer.println("-".repeat(80));

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String timeSlot = (String) tableModel.getValueAt(i, 0);
                writer.printf("%-15s", timeSlot);
                for (int j = 1; j < tableModel.getColumnCount(); j++) {
                    String value = (String) tableModel.getValueAt(i, j);
                    String plain = (value != null && !value.isEmpty())
                        ? value.replaceAll("<[^>]*>", "").trim() : "-";
                    writer.printf(" | %-35s", plain);
                }
                writer.println();
                writer.println("-".repeat(80));
            }

            writer.println();
            writer.println("=".repeat(80));
            writer.println("Fin de l'emploi du temps - Genere automatiquement");
            statusLabel.setText("Emploi du temps sauvegarde dans: " + filename);

        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    private void exportToTxt() {
        if (classeCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Aucune classe selectionnee", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedItem = (String) classeCombo.getSelectedItem();
        String classeId = selectedItem.split(" - ")[0];
        Classe classe = classeService.getClasse(classeId);
        String semaine = getSemaineKey();

        JFileChooser fileChooser = new JFileChooser("data/exported");
        fileChooser.setSelectedFile(new File("emploi_" + classe.getNom() + "_" + semaine.replace(" ", "_") + ".txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("=".repeat(80));
                writer.println("                    EMPLOI DU TEMPS - EMSI");
                writer.println("=".repeat(80));
                writer.println();
                writer.printf("Classe: %s (%s - %s)%n", classe.getNom(), classe.getNiveau(), classe.getFiliere());
                writer.printf("Semaine: %s%n", semaine);
                writer.printf("Date d'export: %s%n", new Date().toString());
                writer.println();
                writer.println("-".repeat(80));

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String timeSlot = (String) tableModel.getValueAt(i, 0);
                    writer.printf("%-15s", timeSlot);
                    for (int j = 1; j < tableModel.getColumnCount(); j++) {
                        String value = (String) tableModel.getValueAt(i, j);
                        String plain = (value != null && !value.isEmpty())
                            ? value.replaceAll("<[^>]*>", "").trim() : "-";
                        writer.printf(" | %-35s", plain);
                    }
                    writer.println();
                    writer.println("-".repeat(80));
                }

                writer.println();
                writer.println("=".repeat(80));
                JOptionPane.showMessageDialog(this, "Export reussi vers: " + file.getName(), "Succes", JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("Exporte vers: " + file.getName());

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'export: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void checkConflicts() {
        List<String> conflicts = emploiService.getAllConflicts();
        if (conflicts.isEmpty()) {
            statusLabel.setText("Aucun conflit detecte dans l'emploi du temps");
            JOptionPane.showMessageDialog(this,
                "Aucun conflit detecte !\nL'emploi du temps est coherent.",
                "Verification des conflits", JOptionPane.INFORMATION_MESSAGE);
        } else {
            statusLabel.setText(conflicts.size() + " conflit(s) detecte(s)");
            StringBuilder sb = new StringBuilder();
            sb.append(conflicts.size()).append(" conflit(s) detecte(s) :\n\n");
            for (String c : conflicts) sb.append(c).append("\n");
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setBackground(Color.WHITE);
            textArea.setForeground(Color.BLACK);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(550, 300));
            JOptionPane.showMessageDialog(this, scrollPane, "Conflits detectes", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearEmploi() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Voulez-vous vraiment effacer l'emploi du temps pour cette classe ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION && classeCombo.getSelectedItem() != null) {
            String selectedItem = (String) classeCombo.getSelectedItem();
            String classeId = selectedItem.split(" - ")[0];
            String semaine  = getSemaineKey();

            EmploiDuTemps edt = emploiService.getEmploiForClass(classeId, semaine);
            for (Seance seance : edt.getSeances()) emploiService.deleteSeance(seance.getId());

            clearTable();
            statusLabel.setText("Emploi du temps efface pour " + selectedItem);
            JOptionPane.showMessageDialog(this, "Emploi du temps efface avec succes", "Succes", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearTable() {
        for (int i = 0; i < tableModel.getRowCount(); i++)
            for (int j = 1; j < tableModel.getColumnCount(); j++)
                tableModel.setValueAt("", i, j);
    }

    private void editSeance(int row, int col) {
        String currentValue = (String) tableModel.getValueAt(row, col);
        if (currentValue == null || currentValue.isEmpty()) {
            addSeanceAtSlot(row, col);
        } else {
            int option = JOptionPane.showOptionDialog(this,
                "Que voulez-vous faire ?", "Modifier seance",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"Modifier", "Supprimer", "Annuler"}, "Modifier");
            if (option == 0) modifySeanceAtSlot(row, col);
            else if (option == 1) deleteSeanceAtSlot(row, col);
        }
    }

    private void addSeanceAtSlot(int row, int col)    { showAddSeanceDialog(row, col); }
    private void modifySeanceAtSlot(int row, int col) { showAddSeanceDialog(row, col); }

    private void deleteSeanceAtSlot(int row, int col) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Voulez-vous vraiment supprimer cette seance ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.setValueAt("", row, col);
            statusLabel.setText("Seance supprimee");
        }
    }

    private void showAddSeanceDialog(int row, int col) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Ajouter / Modifier une seance", true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> matiereBox = new JComboBox<>();
        JComboBox<String> profBox    = new JComboBox<>();
        JComboBox<String> salleBox   = new JComboBox<>();

        // Label d'avertissement prof (affiche si la matiere n'a aucun prof affecte)
        JLabel profWarningLabel = new JLabel(" ");
        profWarningLabel.setFont(new Font("Tahoma", Font.ITALIC, 11));
        profWarningLabel.setForeground(new Color(180, 0, 0));

        // Remplir les matieres filtrées par filiere ET niveau de la classe
        String selectedClassItem = (String) classeCombo.getSelectedItem();
        String classId = selectedClassItem != null ? selectedClassItem.split(" - ")[0] : "";
        Classe classeSelectionnee = classeService.getClasse(classId);
        List<Matiere> matieresDispos = (classeSelectionnee != null)
            ? matiereService.getMatieresByFiliereAndNiveau(classeSelectionnee.getFiliere(), classeSelectionnee.getNiveau())
            : matiereService.getAllMatieres();

        for (Matiere m : matieresDispos) {
            String flag = (m.getProfIds() == null || m.getProfIds().isEmpty()) ? " [!]" : "";
            matiereBox.addItem(m.getId() + " - " + m.getNom() + flag);
        }
        for (Salle s : salleService.getAllSalles()) salleBox.addItem(s.getId() + " - " + s.getNom());

        // Fonction qui met a jour profBox selon la matiere selectionnee
        Runnable updateProfBox = () -> {
            profBox.removeAllItems();
            if (matiereBox.getSelectedItem() == null) return;
            String mid = ((String) matiereBox.getSelectedItem()).split(" - ")[0];
            Matiere mat = matiereService.getMatiere(mid);
            if (mat == null) return;
            List<String> pids = mat.getProfIds();
            if (pids == null || pids.isEmpty()) {
                // Aucun prof affecte : avertir et proposer tous les profs
                profWarningLabel.setText("⚠ Aucun prof affecte a cette matiere ! Allez dans l'onglet Affectations.");
                for (Professeur p : profService.getAllProfesseurs())
                    profBox.addItem(p.getId() + " - " + p.getFullName());
            } else {
                profWarningLabel.setText("✓ " + pids.size() + " prof(s) affecte(s)");
                profWarningLabel.setForeground(new Color(0, 130, 0));
                for (String pid : pids) {
                    Professeur p = profService.getProfesseur(pid);
                    if (p != null) profBox.addItem(p.getId() + " - " + p.getFullName());
                }
            }
        };

        matiereBox.addActionListener(e -> updateProfBox.run());
        updateProfBox.run(); // initialiser avec la 1ere matiere

        int gridRow = 0;
        gbc.gridx = 0; gbc.gridy = gridRow;
        JLabel lblMat = new JLabel("Matiere:");
        lblMat.setFont(new Font("Tahoma", Font.BOLD, 12));
        panel.add(lblMat, gbc);
        gbc.gridx = 1; panel.add(matiereBox, gbc);
        gridRow++;

        gbc.gridx = 0; gbc.gridy = gridRow;
        JLabel lblProf = new JLabel("Professeur:");
        lblProf.setFont(new Font("Tahoma", Font.BOLD, 12));
        panel.add(lblProf, gbc);
        gbc.gridx = 1; panel.add(profBox, gbc);
        gridRow++;

        gbc.gridx = 1; gbc.gridy = gridRow; panel.add(profWarningLabel, gbc);
        gridRow++;

        gbc.gridx = 0; gbc.gridy = gridRow;
        JLabel lblSalle = new JLabel("Salle:");
        lblSalle.setFont(new Font("Tahoma", Font.BOLD, 12));
        panel.add(lblSalle, gbc);
        gbc.gridx = 1; panel.add(salleBox, gbc);

        JButton saveButton   = createStyledButton("Enregistrer");
        JButton cancelButton = createStyledButton("Annuler");
        gridRow++;
        gbc.gridx = 0; gbc.gridy = gridRow; panel.add(saveButton,   gbc);
        gbc.gridx = 1;                      panel.add(cancelButton, gbc);

        saveButton.addActionListener(e -> {
            if (matiereBox.getSelectedItem() == null || profBox.getSelectedItem() == null || salleBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(dialog, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String matiereId = ((String) matiereBox.getSelectedItem()).split(" - ")[0];
            String profId    = ((String) profBox.getSelectedItem()).split(" - ")[0];
            String salleId   = ((String) salleBox.getSelectedItem()).split(" - ")[0];

            String selectedItem = (String) classeCombo.getSelectedItem();
            String classeId = selectedItem.split(" - ")[0];
            String semaine  = getSemaineKey();
            String timeSlot = (String) tableModel.getValueAt(row, 0);
            String[] times  = timeSlot.split(" - ");
            LocalTime start = LocalTime.parse(times[0]);
            LocalTime end   = LocalTime.parse(times[1]);
            DayOfWeek day   = getDayForColumn(col);

            Matiere    matiere = matiereService.getMatiere(matiereId);
            Professeur prof    = profService.getProfesseur(profId);
            Salle      salle   = salleService.getSalle(salleId);

            String id = UUID.randomUUID().toString();
            Seance seance = new Seance(id, classeId, matiereId, profId, salleId, day, start, end, semaine);

            String conflict = emploiService.getConflictDescription(seance);
            if (conflict != null) {
                int choice = JOptionPane.showOptionDialog(dialog,
                    conflict + "\n\nVoulez-vous tout de meme enregistrer ?",
                    "Conflit detecte", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, new String[]{"Forcer l'enregistrement", "Annuler"}, "Annuler");
                if (choice != 0) return;
                emploiService.updateSeance(seance);
            }

            if (conflict == null) emploiService.addSeance(seance);

            String nomMatiere = (matiere != null) ? matiere.getNom()   : "";
            String nomProf    = (prof    != null) ? prof.getFullName() : "";
            String nomSalle   = (salle   != null) ? salle.getNom()     : "";
            String hD = start.format(DateTimeFormatter.ofPattern("HH:mm"));
            String hF = end.format(DateTimeFormatter.ofPattern("HH:mm"));

            String text = String.format(
                "<html><div style='text-align:center;padding:4px;'>" +
                "<span style='color:black;font-size:10px;'><b>%s - %s</b></span><br>" +
                "<b style='color:black;'>%s</b><br>" +
                "<span style='color:black;font-size:11px;'>%s</span><br>" +
                "<span style='color:black;font-size:10px;'>%s</span>" +
                "</div></html>",
                hD, hF, nomMatiere, nomProf, nomSalle
            );
            tableModel.setValueAt(text, row, col);
            dialog.dispose();
            statusLabel.setText("Seance ajoutee avec succes");
        });

        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private DayOfWeek getDayForColumn(int col) {
        switch (col) {
            case 1: return DayOfWeek.MONDAY;
            case 2: return DayOfWeek.TUESDAY;
            case 3: return DayOfWeek.WEDNESDAY;
            case 4: return DayOfWeek.THURSDAY;
            case 5: return DayOfWeek.FRIDAY;
            case 6: return DayOfWeek.SATURDAY;
            default: return DayOfWeek.MONDAY;
        }
    }

    private int getRowForTime(LocalTime time) {
        LocalTime[] slots = {LocalTime.of(8,0), LocalTime.of(10,0), LocalTime.of(14,0), LocalTime.of(16,0)};
        for (int i = 0; i < slots.length; i++) if (time.equals(slots[i])) return i;
        return -1;
    }

    private int getColumnForDay(DayOfWeek day) {
        switch (day) {
            case MONDAY:    return 1;
            case TUESDAY:   return 2;
            case WEDNESDAY: return 3;
            case THURSDAY:  return 4;
            case FRIDAY:    return 5;
            case SATURDAY:  return 6;
            default:        return -1;
        }
    }

    private void setupKeyboardShortcuts() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK), "generate");
        getActionMap().put("generate", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { generateEmploi(); }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "export");
        getActionMap().put("export", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { exportToTxt(); }
        });
    }

    class StyledTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setForeground(Color.BLACK);
            setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);

            if (column == 0) {
                // Colonne horaires : fond gris clair, bold
                setBackground(new Color(210, 210, 210));
                setFont(new Font("SansSerif", Font.BOLD, 12));
            } else {
                boolean filled = (value != null && !value.toString().isEmpty());
                setBackground(filled ? CELL_FILLED_BG : CELL_EMPTY_BG);
                setFont(new Font("SansSerif", Font.PLAIN, 11));
                if (filled) {
                    setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
                }
            }

            if (isSelected) {
                setBackground(new Color(180, 180, 180));
            }

            return this;
        }
    }
}
