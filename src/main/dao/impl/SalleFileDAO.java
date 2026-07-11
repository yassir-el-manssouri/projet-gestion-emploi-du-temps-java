package main.dao.impl;

import main.dao.interfaces.ISalleDAO;
import main.model.Salle;
import main.utils.FileUtils;
import java.util.*;
import java.util.stream.Collectors;

public class SalleFileDAO implements ISalleDAO {
    private static final String FILE_NAME = "salles.txt";

    private static SalleFileDAO instance;
    public static SalleFileDAO getInstance() {
        if (instance == null) instance = new SalleFileDAO();
        return instance;
    }

    private final Map<String, Salle> salles = new LinkedHashMap<>();

    private SalleFileDAO() {
        loadData();
    }
    
    private void loadData() {
        List<String> lines = FileUtils.readAllLines(FILE_NAME);
        for (String line : lines) {
            Salle salle = FileUtils.fromLineToSalle(line);
            if (salle != null) {
                salles.put(salle.getId(), salle);
            }
        }
    }
    
    private void saveData() {
        List<String> lines = new ArrayList<>();
        for (Salle salle : salles.values()) {
            lines.add(FileUtils.salleToLine(salle));
        }
        FileUtils.writeAllLines(FILE_NAME, lines);
    }
    
    @Override
    public boolean create(Salle salle) {
        if (salles.containsKey(salle.getId())) return false;
        salles.put(salle.getId(), salle);
        saveData();
        return true;
    }
    
    @Override
    public Salle read(String id) {
        return salles.get(id);
    }
    
    @Override
    public boolean update(Salle salle) {
        if (!salles.containsKey(salle.getId())) return false;
        salles.put(salle.getId(), salle);
        saveData();
        return true;
    }
    
    @Override
    public boolean delete(String id) {
        if (!salles.containsKey(id)) return false;
        salles.remove(id);
        saveData();
        return true;
    }
    
    @Override
    public List<Salle> findAll() {
        return new ArrayList<>(salles.values());
    }
    
    @Override
    public List<Salle> findBySite(String site) {
        return salles.values().stream()
                .filter(s -> s.getSite().equals(site))
                .collect(Collectors.toList());
    }
}