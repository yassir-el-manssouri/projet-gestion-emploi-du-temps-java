package main.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EmploiDuTemps implements Serializable {
    private static final long serialVersionUID = 1L;
    private String classeId;
    private String semaine;
    private List<Seance> seances;
    
    public EmploiDuTemps() {
        this.seances = new ArrayList<>();
    }
    
    public EmploiDuTemps(String classeId, String semaine) {
        this.classeId = classeId;
        this.semaine = semaine;
        this.seances = new ArrayList<>();
    }
    
    public String getClasseId() { return classeId; }
    public void setClasseId(String classeId) { this.classeId = classeId; }
    public String getSemaine() { return semaine; }
    public void setSemaine(String semaine) { this.semaine = semaine; }
    public List<Seance> getSeances() { return seances; }
    public void setSeances(List<Seance> seances) { this.seances = seances; }
    
    public void addSeance(Seance seance) {
        this.seances.add(seance);
    }
    
    public void removeSeance(Seance seance) {
        this.seances.remove(seance);
    }
}