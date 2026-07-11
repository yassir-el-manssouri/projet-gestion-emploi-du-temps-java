package main.dao.impl;

import main.dao.interfaces.IProfDAO;
import main.model.Professeur;
import main.utils.FileUtils;
import java.util.*;

public class ProfFileDAO implements IProfDAO {
    private static final String FILE_NAME = "profs.txt";

    // ─── Singleton : une seule instance, une seule HashMap pour toute l'appli
    private static ProfFileDAO instance;

    public static ProfFileDAO getInstance() {
        if (instance == null) {
            instance = new ProfFileDAO();
        }
        return instance;
    }

    private final Map<String, Professeur> professeurs = new LinkedHashMap<>();

    private ProfFileDAO() {
        loadData();
    }

    /** Recharge depuis le fichier (utile après une modif externe). */
    public void reload() {
        professeurs.clear();
        loadData();
    }

    private void loadData() {
        List<String> lines = FileUtils.readAllLines(FILE_NAME);
        for (String line : lines) {
            Professeur prof = FileUtils.fromLineToProfesseur(line);
            if (prof != null) {
                professeurs.put(prof.getId(), prof);
            }
        }
    }

    private void saveData() {
        List<String> lines = new ArrayList<>();
        for (Professeur prof : professeurs.values()) {
            lines.add(FileUtils.professeurToLine(prof));
        }
        FileUtils.writeAllLines(FILE_NAME, lines);
    }

    @Override
    public boolean create(Professeur prof) {
        if (professeurs.containsKey(prof.getId())) return false;
        professeurs.put(prof.getId(), prof);
        saveData();
        return true;
    }

    @Override
    public Professeur read(String id) {
        return professeurs.get(id);
    }

    @Override
    public boolean update(Professeur prof) {
        if (!professeurs.containsKey(prof.getId())) return false;
        professeurs.put(prof.getId(), prof);
        saveData();
        return true;
    }

    @Override
    public boolean delete(String id) {
        if (!professeurs.containsKey(id)) return false;
        professeurs.remove(id);
        saveData();
        return true;
    }

    @Override
    public List<Professeur> findAll() {
        return new ArrayList<>(professeurs.values());
    }
}