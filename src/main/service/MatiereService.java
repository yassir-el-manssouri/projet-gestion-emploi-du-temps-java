package main.service;

import main.dao.impl.MatiereFileDAO;
import main.dao.impl.ProfFileDAO;
import main.dao.interfaces.IMatiereDAO;
import main.dao.interfaces.IProfDAO;
import main.model.Matiere;
import main.model.Professeur;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatiereService {
    private IMatiereDAO matiereDAO;
    private IProfDAO profDAO;

    public MatiereService() {
        this.matiereDAO = MatiereFileDAO.getInstance();
        this.profDAO    = ProfFileDAO.getInstance();
    }

    public boolean addMatiere(Matiere matiere) {
        return matiereDAO.create(matiere);
    }

    public Matiere getMatiere(String id) {
        return matiereDAO.read(id);
    }

    public boolean updateMatiere(Matiere matiere) {
        return matiereDAO.update(matiere);
    }

    /**
     * Supprime une matiere ET nettoie son ID dans tous les Professeur.matieresIds.
     */
    public boolean deleteMatiere(String id) {
        for (Professeur p : profDAO.findAll()) {
            List<String> ids = p.getMatieresIds();
            if (ids != null && ids.remove(id)) {
                profDAO.update(p);
            }
        }
        return matiereDAO.delete(id);
    }

    public List<Matiere> getAllMatieres() {
        return matiereDAO.findAll();
    }

    public List<Matiere> getMatieresByFiliere(String filiere) {
        return matiereDAO.findByFiliere(filiere);
    }

    public List<Matiere> getMatieresByFiliereAndNiveau(String filiere, String niveau) {
        return matiereDAO.findByFiliereAndNiveau(filiere, niveau);
    }

    /**
     * Retourne les matieres affectees a un prof donne.
     */
    public List<Matiere> getMatieresByProf(String profId) {
        List<Matiere> result = new ArrayList<>();
        for (Matiere m : matiereDAO.findAll()) {
            if (m.getProfIds() != null && m.getProfIds().contains(profId)) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * Affecte un prof a une matiere (sans doublon).
     * Synchronise aussi Professeur.matieresIds <-> Matiere.profIds.
     */
    public boolean affecterProfAMatiere(String matiereId, String profId) {
        Matiere m = matiereDAO.read(matiereId);
        if (m == null) return false;

        if (!m.getProfIds().contains(profId)) {
            m.getProfIds().add(profId);
            if (!matiereDAO.update(m)) return false;
        }

        Professeur p = profDAO.read(profId);
        if (p != null) {
            List<String> mIds = p.getMatieresIds();
            if (mIds == null) { mIds = new ArrayList<>(); p.setMatieresIds(mIds); }
            if (!mIds.contains(matiereId)) {
                mIds.add(matiereId);
                profDAO.update(p);
            }
        }
        return true;
    }

    /**
     * Retire un prof d'une matiere.
     * Synchronise aussi Professeur.matieresIds <-> Matiere.profIds.
     */
    public boolean retirerProfDeMatiere(String matiereId, String profId) {
        Matiere m = matiereDAO.read(matiereId);
        if (m == null) return false;

        m.getProfIds().remove(profId);
        if (!matiereDAO.update(m)) return false;

        Professeur p = profDAO.read(profId);
        if (p != null && p.getMatieresIds() != null) {
            p.getMatieresIds().remove(matiereId);
            profDAO.update(p);
        }
        return true;
    }

    public void initializeDefaultMatieres() {
        String[][] matieres1AP = {
            {"Mathematiques (Analyse, Algebre)", "AP", "1A", "4"},
            {"Algorithmique", "AP", "1A", "2"},
            {"Programmation C", "AP", "1A", "4"},
            {"Structure des ordinateurs", "AP", "1A", "2"},
            {"Systemes d'exploitation (intro)", "AP", "1A", "2"},
            {"Reseaux informatiques (bases)", "AP", "1A", "2"},
            {"Electronique de base", "AP", "1A", "2"},
            {"Anglais / Francais / Communication", "AP", "1A", "2"},
            {"Culture digitale", "AP", "1A", "2"}
        };
        String[][] matieres2AP = {
            {"Programmation orientee objet (Java / C++)", "AP", "2A", "4"},
            {"Structures de donnees", "AP", "2A", "2"},
            {"Bases de donnees (SQL)", "AP", "2A", "2"},
            {"Reseaux (TCP/IP)", "AP", "2A", "2"},
            {"Systemes d'exploitation avances", "AP", "2A", "2"},
            {"Genie logiciel", "AP", "2A", "2"},
            {"Probabilites & statistiques", "AP", "2A", "2"},
            {"Developpement Web (HTML, CSS, JS)", "AP", "2A", "4"},
            {"Communication professionnelle", "AP", "2A", "2"}
        };
        String[][] matieres3IIR = {
            {"Developpement Web (PHP, Frameworks)", "IIR", "3A", "4"},
            {"Administration systemes (Linux / Windows Server)", "IIR", "3A", "2"},
            {"Reseaux avances (Cisco)", "IIR", "3A", "2"},
            {"Bases de donnees avancees", "IIR", "3A", "2"},
            {"UML & conception logicielle", "IIR", "3A", "2"},
            {"Securite informatique (intro)", "IIR", "3A", "2"}
        };
        String[][] matieres4IR = {
            {"Cloud Computing", "IR-CIR", "4A", "2"},
            {"Cybersecurite", "IR-CIR", "4A", "2"},
            {"DevOps", "IR-DEV", "4A", "2"},
            {"Developpement mobile", "IR-DEV", "4A", "4"},
            {"Big Data (intro)", "IR-IA", "4A", "2"},
            {"Intelligence Artificielle (bases)", "IR-IA", "4A", "4"}
        };
        String[][] matieres5IIR = {
            {"Machine Learning", "IIR", "5A", "4"},
            {"Architecture logicielle avancee", "IIR", "5A", "2"},
            {"Microservices", "IIR", "5A", "2"},
            {"Securite avancee", "IIR", "5A", "2"},
            {"Projet de fin d'etudes (PFE)", "IIR", "5A", "6"},
            {"Stage en entreprise", "IIR", "5A", "0"}
        };
        String[][] matieresGII = {
            {"Automates programmables (PLC)", "GII", "3A", "2"},
            {"Systemes embarques", "GII", "3A", "2"},
            {"Robotique", "GII", "4A", "2"},
            {"Electronique industrielle", "GII", "3A", "2"},
            {"Traitement du signal", "GII", "3A", "2"},
            {"Informatique industrielle", "GII", "4A", "2"},
            {"Supervision industrielle (SCADA)", "GII", "4A", "2"}
        };
        String[][] matieresGI = {
            {"Gestion de production", "GI", "3A", "2"},
            {"Logistique & Supply Chain", "GI", "3A", "2"},
            {"Management de projet", "GI", "4A", "2"},
            {"Qualite (ISO)", "GI", "4A", "2"},
            {"Recherche operationnelle", "GI", "4A", "2"},
            {"Statistiques industrielles", "GI", "3A", "2"}
        };
        addMatieres(matieres1AP);
        addMatieres(matieres2AP);
        addMatieres(matieres3IIR);
        addMatieres(matieres4IR);
        addMatieres(matieres5IIR);
        addMatieres(matieresGII);
        addMatieres(matieresGI);
    }

    private void addMatieres(String[][] matieresList) {
        for (String[] m : matieresList) {
            String id = UUID.randomUUID().toString();
            Matiere matiere = new Matiere(id, m[0], m[1], m[2], Integer.parseInt(m[3]), "Cours");
            if (matiereDAO.read(id) == null) {
                matiereDAO.create(matiere);
            }
        }
    }
}
