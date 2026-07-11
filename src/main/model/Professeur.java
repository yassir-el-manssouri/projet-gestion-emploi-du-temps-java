package main.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Professeur implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private int minutesParSemaine;
    private List<String> classesIds;
    private List<String> matieresIds;
    
    public Professeur() {
        this.classesIds = new ArrayList<>();
        this.matieresIds = new ArrayList<>();
        this.minutesParSemaine = 480;
    }
    
    public Professeur(String id, String nom, String prenom, String email, String telephone) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.classesIds = new ArrayList<>();
        this.matieresIds = new ArrayList<>();
        this.minutesParSemaine = 480;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public int getMinutesParSemaine() { return minutesParSemaine; }
    public void setMinutesParSemaine(int minutesParSemaine) { this.minutesParSemaine = minutesParSemaine; }
    public List<String> getClassesIds() { return classesIds; }
    public void setClassesIds(List<String> classesIds) { this.classesIds = classesIds; }
    public List<String> getMatieresIds() { return matieresIds; }
    public void setMatieresIds(List<String> matieresIds) { this.matieresIds = matieresIds; }
    
    public String getFullName() {
        return prenom + " " + nom;
    }
    
    @Override
    public String toString() {
        return getFullName();
    }
}