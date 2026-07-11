package main.model;

import java.io.Serializable;

public class Salle implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String nom;
    private String site;
    private int etage;
    private String type;
    private int capacite;
    
    public Salle() {}
    
    public Salle(String id, String nom, String site, int etage, String type, int capacite) {
        this.id = id;
        this.nom = nom;
        this.site = site;
        this.etage = etage;
        this.type = type;
        this.capacite = capacite;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }
    public int getEtage() { return etage; }
    public void setEtage(int etage) { this.etage = etage; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    
    @Override
    public String toString() {
        return nom + " (" + site + ", Etage " + etage + ")";
    }
}