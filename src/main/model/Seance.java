package main.model;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class Seance implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String classeId;
    private String matiereId;
    private String professeurId;
    private String salleId;
    private DayOfWeek jour;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String semaine;
    
    public Seance() {}
    
    public Seance(String id, String classeId, String matiereId, String professeurId, 
                  String salleId, DayOfWeek jour, LocalTime heureDebut, LocalTime heureFin, String semaine) {
        this.id = id;
        this.classeId = classeId;
        this.matiereId = matiereId;
        this.professeurId = professeurId;
        this.salleId = salleId;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.semaine = semaine;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClasseId() { return classeId; }
    public void setClasseId(String classeId) { this.classeId = classeId; }
    public String getMatiereId() { return matiereId; }
    public void setMatiereId(String matiereId) { this.matiereId = matiereId; }
    public String getProfesseurId() { return professeurId; }
    public void setProfesseurId(String professeurId) { this.professeurId = professeurId; }
    public String getSalleId() { return salleId; }
    public void setSalleId(String salleId) { this.salleId = salleId; }
    public DayOfWeek getJour() { return jour; }
    public void setJour(DayOfWeek jour) { this.jour = jour; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
    public String getSemaine() { return semaine; }
    public void setSemaine(String semaine) { this.semaine = semaine; }
}