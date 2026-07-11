package main.service;

import main.dao.impl.ClasseFileDAO;
import main.dao.interfaces.IClasseDAO;
import main.model.Classe;
import java.util.List;

public class ClasseService {
    private IClasseDAO classeDAO;
    
    public ClasseService() {
        this.classeDAO = ClasseFileDAO.getInstance();
    }
    
    public boolean addClasse(Classe classe) {
        return classeDAO.create(classe);
    }
    
    public Classe getClasse(String id) {
        return classeDAO.read(id);
    }
    
    public boolean updateClasse(Classe classe) {
        return classeDAO.update(classe);
    }
    
    public boolean deleteClasse(String id) {
        return classeDAO.delete(id);
    }
    
    public List<Classe> getAllClasses() {
        return classeDAO.findAll();
    }
    
    public List<Classe> getClassesByNiveau(String niveau) {
        return classeDAO.findByNiveau(niveau);
    }
    
    public List<Classe> getClassesByFiliere(String filiere) {
        return classeDAO.findByFiliere(filiere);
    }
}