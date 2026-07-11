package main.dao.interfaces;

import main.model.Classe;
import java.util.List;

public interface IClasseDAO {
    boolean create(Classe classe);
    Classe read(String id);
    boolean update(Classe classe);
    boolean delete(String id);
    List<Classe> findAll();
    List<Classe> findByNiveau(String niveau);
    List<Classe> findByFiliere(String filiere);
}