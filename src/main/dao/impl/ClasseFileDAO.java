package main.dao.impl;

import main.dao.interfaces.IClasseDAO;
import main.model.Classe;
import main.utils.FileUtils;
import java.util.*;
import java.util.stream.Collectors;

public class ClasseFileDAO implements IClasseDAO {
    private static final String FILE_NAME = "classes.txt";

    private static ClasseFileDAO instance;
    public static ClasseFileDAO getInstance() {
        if (instance == null) instance = new ClasseFileDAO();
        return instance;
    }

    private final Map<String, Classe> classes = new LinkedHashMap<>();

    private ClasseFileDAO() {
        loadData();
        initializeDefaultClasses();
    }
    
    private void loadData() {
        List<String> lines = FileUtils.readAllLines(FILE_NAME);
        for (String line : lines) {
            Classe classe = FileUtils.fromLineToClasse(line);
            if (classe != null) {
                classes.put(classe.getId(), classe);
            }
        }
    }
    
    private void saveData() {
        List<String> lines = new ArrayList<>();
        for (Classe classe : classes.values()) {
            lines.add(FileUtils.classeToLine(classe));
        }
        FileUtils.writeAllLines(FILE_NAME, lines);
    }
    
    private void initializeDefaultClasses() {
        // 1ère année
        for (int i = 1; i <= 21; i++) createClassIfNotExists("1AP" + i, "1AP" + i, "1A", "AP");
        for (int i = 1; i <= 3; i++) createClassIfNotExists("1GC" + i, "1GC" + i, "1A", "GC");
        for (int i = 1; i <= 2; i++) createClassIfNotExists("1GF" + i, "1GF" + i, "1A", "GF");
        
        // 2ème année
        for (int i = 1; i <= 21; i++) createClassIfNotExists("2AP" + i, "2AP" + i, "2A", "AP");
        for (int i = 1; i <= 3; i++) createClassIfNotExists("2GC" + i, "2GC" + i, "2A", "GC");
        for (int i = 1; i <= 2; i++) createClassIfNotExists("2GF" + i, "2GF" + i, "2A", "GF");
        
        // 3ème année
        for (int i = 1; i <= 22; i++) createClassIfNotExists("3IIR" + i, "3IIR" + i, "3A", "IIR");
        for (int i = 1; i <= 2; i++) createClassIfNotExists("3GC" + i, "3GC" + i, "3A", "GC");
        createClassIfNotExists("3GESI1", "3GESI1", "3A", "GESI");
        createClassIfNotExists("3GI1", "3GI1", "3A", "GI");
        createClassIfNotExists("3IAII1", "3IAII1", "3A", "IAII");
        for (int i = 1; i <= 4; i++) createClassIfNotExists("3IFA" + i, "3IFA" + i, "3A", "IFA");
        
        // 4ème année
        for (int i = 1; i <= 3; i++) createClassIfNotExists("4GC" + i, "4GC" + i, "4A", "GC");
        createClassIfNotExists("4GI1", "4GI1", "4A", "GI");
        createClassIfNotExists("4GIAII1", "4GIAII1", "4A", "GIAII");
        createClassIfNotExists("4IFA-CCA1", "4IFA-CCA1", "4A", "IFA-CCA");
        for (int i = 1; i <= 2; i++) createClassIfNotExists("4IFA-IF" + i, "4IFA-IF" + i, "4A", "IFA-IF");
        for (int i = 1; i <= 6; i++) createClassIfNotExists("4IR-CIR" + i, "4IR-CIR" + i, "4A", "IR-CIR");
        for (int i = 1; i <= 6; i++) createClassIfNotExists("4IR-DEV" + i, "4IR-DEV" + i, "4A", "IR-DEV");
        for (int i = 1; i <= 8; i++) createClassIfNotExists("4IR-IA" + i, "4IR-IA" + i, "4A", "IR-IA");
        
        // 5ème année
        for (int i = 1; i <= 16; i++) createClassIfNotExists("5IIR" + i, "5IIR" + i, "5A", "IIR");
        for (int i = 1; i <= 3; i++) createClassIfNotExists("5GC" + i, "5GC" + i, "5A", "GC");
        createClassIfNotExists("5GI1", "5GI1", "5A", "GI");
        createClassIfNotExists("5GIAII1", "5GIAII1", "5A", "GIAII");
        createClassIfNotExists("5IFA-CCA1", "5IFA-CCA1", "5A", "IFA-CCA");
        for (int i = 1; i <= 2; i++) createClassIfNotExists("5IFA-IF" + i, "5IFA-IF" + i, "5A", "IFA-IF");
        
        saveData();
    }
    
    private void createClassIfNotExists(String id, String nom, String niveau, String filiere) {
        if (!classes.containsKey(id)) {
            classes.put(id, new Classe(id, nom, niveau, filiere, 30));
        }
    }
    
    @Override
    public boolean create(Classe classe) {
        if (classes.containsKey(classe.getId())) return false;
        classes.put(classe.getId(), classe);
        saveData();
        return true;
    }
    
    @Override
    public Classe read(String id) {
        return classes.get(id);
    }
    
    @Override
    public boolean update(Classe classe) {
        if (!classes.containsKey(classe.getId())) return false;
        classes.put(classe.getId(), classe);
        saveData();
        return true;
    }
    
    @Override
    public boolean delete(String id) {
        if (!classes.containsKey(id)) return false;
        classes.remove(id);
        saveData();
        return true;
    }
    
    @Override
    public List<Classe> findAll() {
        return new ArrayList<>(classes.values());
    }
    
    @Override
    public List<Classe> findByNiveau(String niveau) {
        return classes.values().stream()
                .filter(c -> c.getNiveau().equals(niveau))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Classe> findByFiliere(String filiere) {
        return classes.values().stream()
                .filter(c -> c.getFiliere().equals(filiere))
                .collect(Collectors.toList());
    }
}