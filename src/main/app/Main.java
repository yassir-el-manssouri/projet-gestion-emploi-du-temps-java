package main.app;

import main.presentation.MainFrame;
import main.service.SalleService;
import main.service.MatiereService;
import main.service.ProfService;
import javax.swing.*;
import java.awt.Color;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        // Create necessary directories
        createDirectories();
        
        // Initialize default data
        initializeDefaultData();
        
        // Launch application
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                UIManager.put("Table.background", Color.WHITE);
                UIManager.put("Table.foreground", Color.BLACK);
                UIManager.put("Table.selectionBackground", new Color(180, 180, 180));
                UIManager.put("Table.selectionForeground", Color.BLACK);
                UIManager.put("Table.alternateRowColor", new Color(245, 245, 245));
            } catch (Exception e) {
                e.printStackTrace();
            }
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
    
    private static void createDirectories() {
        new File("data").mkdirs();
        new File("data/exported").mkdirs();
    }
    
    private static void initializeDefaultData() {
        // Initialize salles
        SalleService salleService = new SalleService();
        salleService.initializeDefaultSalles();
        
        // Initialize matieres
        MatiereService matiereService = new MatiereService();
        matiereService.initializeDefaultMatieres();
        
        // Initialize professeurs (nouveau)
        ProfService profService = new ProfService();
        profService.initializeDefaultProfesseurs();
        
        System.out.println("✅ Toutes les données par défaut ont été initialisées avec succès!");
        System.out.println("   - Salles: Initialisées");
        System.out.println("   - Matières: Initialisées");
        System.out.println("   - Professeurs: Initialisés (noms marocains)");
    }
}