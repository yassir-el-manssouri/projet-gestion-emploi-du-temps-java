package main.service;

import main.dao.impl.ProfFileDAO;
import main.dao.impl.MatiereFileDAO;
import main.dao.interfaces.IProfDAO;
import main.dao.interfaces.IMatiereDAO;
import main.model.Professeur;
import main.model.Matiere;
import java.util.List;
import java.util.UUID;

public class ProfService {
    private IProfDAO profDAO;
    private IMatiereDAO matiereDAO;
    
    private static final String[] NOMS = {
        "Benali", "El Fassi", "Amrani", "Bennani", "El Mansouri",
        "Berrada", "El Alami", "Idrissi", "Lamrani", "Cherkaoui",
        "Tazi", "El Kaddouri", "Fassi", "El Mountassir", "El Bouazzaoui",
        "Zniber", "Sbihi", "Benjelloun", "El Kadiri", "Belkacem",
        "El Ghazi", "El Haloui", "El Malki", "El Ouazzani", "Sebti"
    };
    
    private static final String[] PRENOMS = {
        "Mohamed", "Ahmed", "Fatima", "Hassan", "Khadija",
        "Youssef", "Amina", "Omar", "Karima", "Rachid",
        "Sanaa", "Nabil", "Nadia", "Tarik", "Leila",
        "Said", "Meryem", "Karim", "Soukaina", "Reda",
        "Hind", "Sofiane", "Imane", "Mehdi", "Salima"
    };
    
    public ProfService() {
        this.profDAO    = ProfFileDAO.getInstance();
        this.matiereDAO = MatiereFileDAO.getInstance();
        // Appeler l'initialisation ici
        initializeDefaultProfesseurs();
    }
    
    public boolean addProfesseur(Professeur prof) {
        return profDAO.create(prof);
    }
    
    public Professeur getProfesseur(String id) {
        return profDAO.read(id);
    }
    
    public boolean updateProfesseur(Professeur prof) {
        return profDAO.update(prof);
    }
    
    /**
     * Supprime un professeur ET nettoie son ID dans toutes les Matiere.profIds.
     */
    public boolean deleteProfesseur(String id) {
        // Nettoyage en cascade côté Matière
        for (Matiere m : matiereDAO.findAll()) {
            List<String> profIds = m.getProfIds();
            if (profIds != null && profIds.remove(id)) {
                matiereDAO.update(m);
            }
        }
        return profDAO.delete(id);
    }
    
    public List<Professeur> getAllProfesseurs() {
        return profDAO.findAll();
    }
    
    public void initializeDefaultProfesseurs() {
        // Vérifier si des professeurs existent déjà
        List<Professeur> existing = profDAO.findAll();
        if (!existing.isEmpty()) {
            System.out.println("Des professeurs existent déjà: " + existing.size());
            return;
        }
        
        System.out.println("Initialisation des professeurs marocains...");
        
        // Créer 30 professeurs marocains
        for (int i = 0; i < 30; i++) {
            String id = UUID.randomUUID().toString();
            String nom = NOMS[i % NOMS.length];
            String prenom = PRENOMS[i % PRENOMS.length];
            String email = (prenom + "." + nom + "@emsi.ma").toLowerCase();
            String telephone = "06" + String.format("%08d", (int)(Math.random() * 99999999));
            
            Professeur prof = new Professeur(id, nom, prenom, email, telephone);
            prof.setMinutesParSemaine((8 + (i % 12)) * 60);
            
            boolean success = profDAO.create(prof);
            if (success) {
                System.out.println("✓ Professeur ajouté: " + prenom + " " + nom);
            }
        }
        
        // Ajouter des professeurs importants
        addImportantProfessors();
        
        System.out.println("✅ Initialisation terminée! Total: " + profDAO.findAll().size() + " professeurs");
    }
    
    private void addImportantProfessors() {
        String[][] importantProfs = {
            {"El Bouchti", "Mohamed", "med.elbouchti@emsi.ma", "0612345601", "20"},
            {"Benchekroun", "Karima", "karima.benchekroun@emsi.ma", "0612345602", "18"},
            {"El Fahli", "Rachid", "rachid.elfahli@emsi.ma", "0612345603", "16"},
            {"Tazi", "Nadia", "nadia.tazi@emsi.ma", "0612345604", "14"},
            {"Amghar", "Youssef", "youssef.amghar@emsi.ma", "0612345605", "16"},
            {"Bennis", "Fatima", "fatima.bennis@emsi.ma", "0612345606", "12"},
            {"Chekroun", "Hassan", "hassan.chekroun@emsi.ma", "0612345607", "10"},
            {"El Mahdaoui", "Sanae", "sanae.elmahdaoui@emsi.ma", "0612345608", "8"},
            {"Guezzar", "Omar", "omar.guezzar@emsi.ma", "0612345609", "18"},
            {"Hilali", "Leila", "leila.hilali@emsi.ma", "0612345610", "14"}
        };
        
        for (String[] profData : importantProfs) {
            String id = UUID.randomUUID().toString();
            Professeur prof = new Professeur(id, profData[1], profData[0], profData[2], profData[3]);
            prof.setMinutesParSemaine(Integer.parseInt(profData[4]));
            profDAO.create(prof);
        }
    }
}