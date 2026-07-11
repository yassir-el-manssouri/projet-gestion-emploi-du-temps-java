package main.dao.impl;

import main.dao.interfaces.ISeanceDAO;
import main.model.Seance;
import main.utils.FileUtils;
import java.util.*;
import java.util.stream.Collectors;

public class SeanceFileDAO implements ISeanceDAO {
    private static final String FILE_NAME = "seances.txt";

    // ─── Singleton : une seule instance partagee dans toute l'appli
    private static SeanceFileDAO instance;

    public static SeanceFileDAO getInstance() {
        if (instance == null) {
            instance = new SeanceFileDAO();
        }
        return instance;
    }

    private final Map<String, Seance> seances = new LinkedHashMap<>();

    private SeanceFileDAO() {
        loadData();
    }

    /** Recharge depuis le fichier. */
    public void reload() {
        seances.clear();
        loadData();
    }

    private void loadData() {
        List<String> lines = FileUtils.readAllLines(FILE_NAME);
        for (String line : lines) {
            Seance seance = FileUtils.fromLineToSeance(line);
            if (seance != null) {
                seances.put(seance.getId(), seance);
            }
        }
    }

    private void saveData() {
        List<String> lines = new ArrayList<>();
        for (Seance seance : seances.values()) {
            lines.add(FileUtils.seanceToLine(seance));
        }
        FileUtils.writeAllLines(FILE_NAME, lines);
    }

    @Override
    public boolean create(Seance seance) {
        if (seances.containsKey(seance.getId())) return false;
        seances.put(seance.getId(), seance);
        saveData();
        return true;
    }

    @Override
    public Seance read(String id) {
        return seances.get(id);
    }

    @Override
    public boolean update(Seance seance) {
        if (!seances.containsKey(seance.getId())) return false;
        seances.put(seance.getId(), seance);
        saveData();
        return true;
    }

    @Override
    public boolean delete(String id) {
        if (!seances.containsKey(id)) return false;
        seances.remove(id);
        saveData();
        return true;
    }

    @Override
    public List<Seance> findAll() {
        return new ArrayList<>(seances.values());
    }

    @Override
    public List<Seance> findByClasse(String classeId) {
        return seances.values().stream()
                .filter(s -> classeId.equals(s.getClasseId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Seance> findByProfesseur(String professeurId) {
        return seances.values().stream()
                .filter(s -> professeurId.equals(s.getProfesseurId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Seance> findBySalle(String salleId) {
        return seances.values().stream()
                .filter(s -> salleId.equals(s.getSalleId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Seance> findBySemaine(String semaine) {
        return seances.values().stream()
                .filter(s -> semaine.equals(s.getSemaine()))
                .collect(Collectors.toList());
    }
}
