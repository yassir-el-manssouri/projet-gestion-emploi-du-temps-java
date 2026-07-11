package main.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Matiere implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String nom;
    private String filiere;
    private String niveau;
    private int minutesParSemaine;
    private String type;
    private List<String> profIds; // profs affectes a cette matiere

    public Matiere() {
        this.profIds = new ArrayList<>();
    }

    public Matiere(String id, String nom, String filiere, String niveau, int minutesParSemaine, String type) {
        this.id = id;
        this.nom = nom;
        this.filiere = filiere;
        this.niveau = niveau;
        this.minutesParSemaine = minutesParSemaine;
        this.type = type;
        this.profIds = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public int getMinutesParSemaine() { return minutesParSemaine; }
    public void setMinutesParSemaine(int minutesParSemaine) { this.minutesParSemaine = minutesParSemaine; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<String> getProfIds() { return profIds; }
    public void setProfIds(List<String> profIds) { this.profIds = profIds != null ? profIds : new ArrayList<>(); }

    public boolean hasProfAffecte() {
        return profIds != null && !profIds.isEmpty();
    }

    @Override
    public String toString() {
        return nom + " (" + minutesParSemaine + "min)";
    }
}
