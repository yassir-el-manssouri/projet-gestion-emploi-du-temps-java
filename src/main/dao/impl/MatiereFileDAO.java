package main.dao.impl;

import main.dao.interfaces.IMatiereDAO;
import main.model.Matiere;
import main.utils.FileUtils;
import java.util.*;
import java.util.stream.Collectors;

public class MatiereFileDAO implements IMatiereDAO {
    private static final String FILE_NAME = "matieres.txt";

    // ─── Singleton : une seule instance, une seule HashMap pour toute l'appli
    private static MatiereFileDAO instance;

    public static MatiereFileDAO getInstance() {
        if (instance == null) {
            instance = new MatiereFileDAO();
        }
        return instance;
    }

    private final Map<String, Matiere> matieres = new LinkedHashMap<>();

    private MatiereFileDAO() {
        loadData();
    }

    /** Recharge depuis le fichier (utile après une modif externe). */
    public void reload() {
        matieres.clear();
        loadData();
    }

    private void loadData() {
        List<String> lines = FileUtils.readAllLines(FILE_NAME);
        for (String line : lines) {
            Matiere m = FileUtils.fromLineToMatiere(line);
            if (m != null) {
                matieres.put(m.getId(), m);
            }
        }
    }

    private void saveData() {
        List<String> lines = new ArrayList<>();
        for (Matiere m : matieres.values()) {
            lines.add(FileUtils.matiereToLine(m));
        }
        FileUtils.writeAllLines(FILE_NAME, lines);
    }

    @Override
    public boolean create(Matiere matiere) {
        if (matieres.containsKey(matiere.getId())) return false;
        matieres.put(matiere.getId(), matiere);
        saveData();
        return true;
    }

    @Override
    public Matiere read(String id) {
        return matieres.get(id);
    }

    @Override
    public boolean update(Matiere matiere) {
        if (!matieres.containsKey(matiere.getId())) return false;
        matieres.put(matiere.getId(), matiere);
        saveData();
        return true;
    }

    @Override
    public boolean delete(String id) {
        if (!matieres.containsKey(id)) return false;
        matieres.remove(id);
        saveData();
        return true;
    }

    @Override
    public List<Matiere> findAll() {
        return new ArrayList<>(matieres.values());
    }

    @Override
    public List<Matiere> findByFiliere(String filiere) {
        return matieres.values().stream()
                .filter(m -> m.getFiliere().equals(filiere))
                .collect(Collectors.toList());
    }

    @Override
    public List<Matiere> findByFiliereAndNiveau(String filiere, String niveau) {
        return matieres.values().stream()
                .filter(m -> m.getFiliere().equals(filiere) && m.getNiveau().equals(niveau))
                .collect(Collectors.toList());
    }
}