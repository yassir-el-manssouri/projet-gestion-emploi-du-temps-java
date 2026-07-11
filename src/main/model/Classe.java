package main.model;

import java.io.Serializable;

public class Classe implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String nom;
    private String niveau; // 1A, 2A, 3A, 4A, 5A
    private String filiere;
    private int effectif;
    
    public Classe() {}
    
    public Classe(String id, String nom, String niveau, String filiere, int effectif) {
        this.id = id;
        this.nom = nom;
        this.niveau = niveau;
        this.filiere = filiere;
        this.effectif = effectif;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    public int getEffectif() { return effectif; }
    public void setEffectif(int effectif) { this.effectif = effectif; }
    
    @Override
    public String toString() {
        return nom + " (" + niveau + " - " + filiere + ")";
    }
}