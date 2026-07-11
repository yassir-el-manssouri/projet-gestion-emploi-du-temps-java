package main.service;

import main.dao.impl.*;
import main.dao.interfaces.*;
import main.model.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

public class EmploiService {
    private ISeanceDAO seanceDAO;
    private IProfDAO profDAO;
    private ISalleDAO salleDAO;
    private IClasseDAO classeDAO;
    private IMatiereDAO matiereDAO;
    
    public EmploiService() {
        this.seanceDAO  = SeanceFileDAO.getInstance();
        this.profDAO    = ProfFileDAO.getInstance();
        this.salleDAO   = SalleFileDAO.getInstance();
        this.classeDAO  = ClasseFileDAO.getInstance();
        this.matiereDAO = MatiereFileDAO.getInstance();
    }
    
    public boolean addSeance(Seance seance) {
        if (hasConflict(seance)) {
            return false;
        }
        return seanceDAO.create(seance);
    }
    
    public boolean updateSeance(Seance seance) {
        return seanceDAO.update(seance);
    }

    /**
     * Retourne null si pas de conflit, sinon un message descriptif.
     */
    public String getConflictDescription(Seance seance) {
        // Conflit salle
        List<Seance> salleSeances = seanceDAO.findBySalle(seance.getSalleId());
        for (Seance s : salleSeances) {
            if (s.getJour().equals(seance.getJour()) &&
                s.getSemaine().equals(seance.getSemaine()) &&
                !s.getId().equals(seance.getId())) {
                if (timesOverlap(s.getHeureDebut(), s.getHeureFin(),
                                seance.getHeureDebut(), seance.getHeureFin())) {
                    Salle salle = salleDAO.read(seance.getSalleId());
                    String nomSalle = salle != null ? salle.getNom() : seance.getSalleId();
                    return String.format(
                        "⚠️ CONFLIT SALLE : La salle \"%s\" est déjà occupée\n" +
                        "   le %s de %s à %s (%s).",
                        nomSalle, formatJour(seance.getJour()),
                        seance.getHeureDebut(), seance.getHeureFin(),
                        seance.getSemaine());
                }
            }
        }
        
        // Conflit professeur
        List<Seance> profSeances = seanceDAO.findByProfesseur(seance.getProfesseurId());
        for (Seance s : profSeances) {
            if (s.getJour().equals(seance.getJour()) &&
                s.getSemaine().equals(seance.getSemaine()) &&
                !s.getId().equals(seance.getId())) {
                if (timesOverlap(s.getHeureDebut(), s.getHeureFin(),
                                seance.getHeureDebut(), seance.getHeureFin())) {
                    Professeur prof = profDAO.read(seance.getProfesseurId());
                    String nomProf = prof != null ? prof.getFullName() : seance.getProfesseurId();
                    return String.format(
                        "⚠️ CONFLIT PROFESSEUR : Le professeur \"%s\" a déjà un cours\n" +
                        "   le %s de %s à %s (%s).",
                        nomProf, formatJour(seance.getJour()),
                        seance.getHeureDebut(), seance.getHeureFin(),
                        seance.getSemaine());
                }
            }
        }
        
        // Conflit classe
        List<Seance> classSeances = seanceDAO.findByClasse(seance.getClasseId());
        for (Seance s : classSeances) {
            if (s.getJour().equals(seance.getJour()) &&
                s.getSemaine().equals(seance.getSemaine()) &&
                !s.getId().equals(seance.getId())) {
                if (timesOverlap(s.getHeureDebut(), s.getHeureFin(),
                                seance.getHeureDebut(), seance.getHeureFin())) {
                    Classe classe = classeDAO.read(seance.getClasseId());
                    String nomClasse = classe != null ? classe.getNom() : seance.getClasseId();
                    return String.format(
                        "⚠️ CONFLIT CLASSE : La classe \"%s\" a déjà un cours\n" +
                        "   le %s de %s à %s (%s).",
                        nomClasse, formatJour(seance.getJour()),
                        seance.getHeureDebut(), seance.getHeureFin(),
                        seance.getSemaine());
                }
            }
        }
        
        return null; // Pas de conflit
    }

    public boolean hasConflict(Seance seance) {
        return getConflictDescription(seance) != null;
    }
    
    /**
     * Vérifie tous les conflits dans l'emploi du temps et retourne la liste.
     */
    public List<String> getAllConflicts() {
        List<String> conflicts = new ArrayList<>();
        List<Seance> allSeances = seanceDAO.findAll();
        Set<String> checked = new HashSet<>();
        
        for (Seance s1 : allSeances) {
            for (Seance s2 : allSeances) {
                if (s1.getId().equals(s2.getId())) continue;
                String pairKey = s1.getId().compareTo(s2.getId()) < 0 
                    ? s1.getId() + "_" + s2.getId() 
                    : s2.getId() + "_" + s1.getId();
                if (checked.contains(pairKey)) continue;
                checked.add(pairKey);
                
                if (!s1.getJour().equals(s2.getJour()) || 
                    !s1.getSemaine().equals(s2.getSemaine())) continue;
                if (!timesOverlap(s1.getHeureDebut(), s1.getHeureFin(),
                                  s2.getHeureDebut(), s2.getHeureFin())) continue;
                
                // Conflit salle
                if (s1.getSalleId() != null && s1.getSalleId().equals(s2.getSalleId())) {
                    Salle salle = salleDAO.read(s1.getSalleId());
                    String nomSalle = salle != null ? salle.getNom() : s1.getSalleId();
                    conflicts.add(String.format("⚠️ CONFLIT SALLE \"%s\" : %s %s-%s (%s)",
                        nomSalle, formatJour(s1.getJour()), s1.getHeureDebut(), s1.getHeureFin(), s1.getSemaine()));
                }
                // Conflit professeur
                if (s1.getProfesseurId() != null && s1.getProfesseurId().equals(s2.getProfesseurId())) {
                    Professeur prof = profDAO.read(s1.getProfesseurId());
                    String nomProf = prof != null ? prof.getFullName() : s1.getProfesseurId();
                    conflicts.add(String.format("⚠️ CONFLIT PROF \"%s\" : %s %s-%s (%s)",
                        nomProf, formatJour(s1.getJour()), s1.getHeureDebut(), s1.getHeureFin(), s1.getSemaine()));
                }
                // Conflit classe
                if (s1.getClasseId() != null && s1.getClasseId().equals(s2.getClasseId())) {
                    Classe classe = classeDAO.read(s1.getClasseId());
                    String nomClasse = classe != null ? classe.getNom() : s1.getClasseId();
                    conflicts.add(String.format("⚠️ CONFLIT CLASSE \"%s\" : %s %s-%s (%s)",
                        nomClasse, formatJour(s1.getJour()), s1.getHeureDebut(), s1.getHeureFin(), s1.getSemaine()));
                }
            }
        }
        return conflicts;
    }
    
    private String formatJour(DayOfWeek jour) {
        switch (jour) {
            case MONDAY: return "Lundi";
            case TUESDAY: return "Mardi";
            case WEDNESDAY: return "Mercredi";
            case THURSDAY: return "Jeudi";
            case FRIDAY: return "Vendredi";
            case SATURDAY: return "Samedi";
            default: return jour.toString();
        }
    }
    
    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }
    
    public boolean deleteSeance(String id) {
        return seanceDAO.delete(id);
    }
    
    public Seance getSeance(String id) {
        return seanceDAO.read(id);
    }
    
    public List<Seance> getAllSeances() {
        return seanceDAO.findAll();
    }
    
    public List<Seance> getSeancesByClasse(String classeId) {
        return seanceDAO.findByClasse(classeId);
    }
    
    public List<Seance> getSeancesByProfesseur(String profId) {
        return seanceDAO.findByProfesseur(profId);
    }
    
    public List<Seance> getSeancesBySalle(String salleId) {
        return seanceDAO.findBySalle(salleId);
    }
    
    public List<Seance> getSeancesBySemaine(String semaine) {
        return seanceDAO.findBySemaine(semaine);
    }
    
    public EmploiDuTemps getEmploiForClass(String classeId, String semaine) {
        EmploiDuTemps edt = new EmploiDuTemps(classeId, semaine);
        List<Seance> seances = seanceDAO.findByClasse(classeId);
        for (Seance seance : seances) {
            if (seance.getSemaine().equals(semaine)) {
                edt.addSeance(seance);
            }
        }
        return edt;
    }
    
    public boolean verifierDisponibiliteClasse(String classeId, DayOfWeek jour,
            LocalTime heureDebut, LocalTime heureFin, String semaine) {
        List<Seance> seancesClasse = seanceDAO.findByClasse(classeId);
        for (Seance s : seancesClasse) {
            if (s.getJour().equals(jour) && s.getSemaine().equals(semaine)) {
                if (timesOverlap(s.getHeureDebut(), s.getHeureFin(), heureDebut, heureFin)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean verifierDisponibiliteProf(Professeur prof, DayOfWeek jour, 
            LocalTime heureDebut, LocalTime heureFin, String semaine) {
        List<Seance> seancesProf = seanceDAO.findByProfesseur(prof.getId());
        for (Seance s : seancesProf) {
            if (s.getJour().equals(jour) && s.getSemaine().equals(semaine)) {
                if (timesOverlap(s.getHeureDebut(), s.getHeureFin(), heureDebut, heureFin)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean verifierDisponibiliteSalle(Salle salle, DayOfWeek jour, 
            LocalTime heureDebut, LocalTime heureFin, String semaine) {
        List<Seance> seancesSalle = seanceDAO.findBySalle(salle.getId());
        for (Seance s : seancesSalle) {
            if (s.getJour().equals(jour) && s.getSemaine().equals(semaine)) {
                if (timesOverlap(s.getHeureDebut(), s.getHeureFin(), heureDebut, heureFin)) {
                    return false;
                }
            }
        }
        return true;
    }
}
