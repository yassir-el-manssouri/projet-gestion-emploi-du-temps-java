package main.service;

import main.dao.impl.SalleFileDAO;
import main.dao.interfaces.ISalleDAO;
import main.model.Salle;
import java.util.List;

public class SalleService {
    private ISalleDAO salleDAO;
    
    public SalleService() {
        this.salleDAO = SalleFileDAO.getInstance();
    }
    
    public boolean addSalle(Salle salle) {
        return salleDAO.create(salle);
    }
    
    public Salle getSalle(String id) {
        return salleDAO.read(id);
    }
    
    public boolean updateSalle(Salle salle) {
        return salleDAO.update(salle);
    }
    
    public boolean deleteSalle(String id) {
        return salleDAO.delete(id);
    }
    
    public List<Salle> getAllSalles() {
        return salleDAO.findAll();
    }
    
    public List<Salle> getSallesBySite(String site) {
        return salleDAO.findBySite(site);
    }
    
    public void initializeDefaultSalles() {
        String[] sites = {
            "EMSI Agdal 1", "EMSI Agdal 2", "EMSI Hassan",
            "EMSI centre 1", "EMSI centre 2", "EMSI bouregrag", "EMSI souissi"
        };
        
        for (String site : sites) {
            if (site.equals("EMSI Agdal 1") || site.equals("EMSI centre 1") || site.equals("EMSI centre 2")) {
                for (int etage = 1; etage <= 4; etage++) {
                    String[] types = {"A", "B", "C", "LI"};
                    for (String type : types) {
                        String id = site.replace(" ", "_") + "_E" + etage + "_" + type;
                        String nom = "Salle " + type + " - Etage " + etage;
                        if (salleDAO.read(id) == null) {
                            salleDAO.create(new Salle(id, nom, site, etage, type, 40));
                        }
                    }
                }
            } else if (site.equals("EMSI Agdal 2")) {
                for (int etage = 1; etage <= 6; etage++) {
                    String[] types = {"Amphi", "A", "B", "C", "D", "E", "F", "LI"};
                    for (String type : types) {
                        String id = site.replace(" ", "_") + "_E" + etage + "_" + type;
                        String nom = type.equals("Amphi") ? "Amphithéâtre" : "Salle " + type;
                        nom += " - Etage " + etage;
                        if (salleDAO.read(id) == null) {
                            int capacite = type.equals("Amphi") ? 200 : 45;
                            salleDAO.create(new Salle(id, nom, site, etage, type, capacite));
                        }
                    }
                }
            } else {
                for (int etage = 1; etage <= 6; etage++) {
                    String[] types = {"A", "B", "C", "LI"};
                    for (String type : types) {
                        String id = site.replace(" ", "_") + "_E" + etage + "_" + type;
                        String nom = "Salle " + type + " - Etage " + etage;
                        if (salleDAO.read(id) == null) {
                            salleDAO.create(new Salle(id, nom, site, etage, type, 45));
                        }
                    }
                }
            }
        }
    }
}