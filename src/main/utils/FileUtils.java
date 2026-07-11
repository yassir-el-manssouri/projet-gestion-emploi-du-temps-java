package main.utils;

import main.model.*;
import java.io.*;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.*;

public class FileUtils {
    
    private static final String DATA_DIR = "data/";
    
    public static List<String> readAllLines(String filename) {
        List<String> lines = new ArrayList<>();
        File file = new File(DATA_DIR + filename);
        if (!file.exists()) {
            return lines;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
    
    public static void writeAllLines(String filename, List<String> lines) {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_DIR + filename))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String salleToLine(Salle salle) {
        return String.format("%s|%s|%s|%d|%s|%d",
            salle.getId(), salle.getNom(), salle.getSite(),
            salle.getEtage(), salle.getType(), salle.getCapacite());
    }
    
    public static Salle fromLineToSalle(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 6) {
            return new Salle(parts[0], parts[1], parts[2], 
                           Integer.parseInt(parts[3]), parts[4], 
                           Integer.parseInt(parts[5]));
        }
        return null;
    }
    
    public static String classeToLine(Classe classe) {
        return String.format("%s|%s|%s|%s|%d",
            classe.getId(), classe.getNom(), classe.getNiveau(),
            classe.getFiliere(), classe.getEffectif());
    }
    
    public static Classe fromLineToClasse(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 5) {
            return new Classe(parts[0], parts[1], parts[2], 
                            parts[3], Integer.parseInt(parts[4]));
        }
        return null;
    }
    
    public static String professeurToLine(Professeur prof) {
        return String.format("%s|%s|%s|%s|%s|%d|%s|%s",
            prof.getId(), prof.getNom(), prof.getPrenom(),
            prof.getEmail(), prof.getTelephone(), prof.getMinutesParSemaine(),
            String.join(",", prof.getClassesIds()),
            String.join(",", prof.getMatieresIds()));
    }
    
    public static Professeur fromLineToProfesseur(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 8) {
            Professeur prof = new Professeur(parts[0], parts[1], parts[2], parts[3], parts[4]);
            prof.setMinutesParSemaine(Integer.parseInt(parts[5]));
            if (!parts[6].isEmpty()) {
                prof.setClassesIds(new ArrayList<>(Arrays.asList(parts[6].split(","))));
            }
            if (!parts[7].isEmpty()) {
                prof.setMatieresIds(new ArrayList<>(Arrays.asList(parts[7].split(","))));
            }
            return prof;
        }
        return null;
    }
    
    /**
     * Format: id|nom|filiere|niveau|minutesParSemaine|type|profId1,profId2,...
     * Le 7eme champ (profIds) est optionnel pour compatibilite avec anciens fichiers.
     */
    public static String matiereToLine(Matiere matiere) {
        String profIdsStr = (matiere.getProfIds() != null && !matiere.getProfIds().isEmpty())
            ? String.join(",", matiere.getProfIds())
            : "";
        return String.format("%s|%s|%s|%s|%d|%s|%s",
            matiere.getId(), matiere.getNom(), matiere.getFiliere(),
            matiere.getNiveau(), matiere.getMinutesParSemaine(), matiere.getType(),
            profIdsStr);
    }
    
    public static Matiere fromLineToMatiere(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 6) {
            Matiere matiere = new Matiere(parts[0], parts[1], parts[2], parts[3],
                             Integer.parseInt(parts[4]), parts[5]);
            // Champ profIds optionnel (7eme champ)
            if (parts.length >= 7 && !parts[6].isEmpty()) {
                matiere.setProfIds(new ArrayList<>(Arrays.asList(parts[6].split(","))));
            }
            return matiere;
        }
        return null;
    }
    
    public static String seanceToLine(Seance seance) {
        return String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s",
            seance.getId(), seance.getClasseId(), seance.getMatiereId(),
            seance.getProfesseurId(), seance.getSalleId(), seance.getJour().toString(),
            seance.getHeureDebut().toString(), seance.getHeureFin().toString(),
            seance.getSemaine());
    }
    
    public static Seance fromLineToSeance(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 9) {
            return new Seance(parts[0], parts[1], parts[2], parts[3], parts[4],
                            DayOfWeek.valueOf(parts[5]), LocalTime.parse(parts[6]),
                            LocalTime.parse(parts[7]), parts[8]);
        }
        return null;
    }
}
