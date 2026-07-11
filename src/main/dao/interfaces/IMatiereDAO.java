package main.dao.interfaces;

import main.model.Matiere;
import java.util.List;

public interface IMatiereDAO {
    boolean create(Matiere matiere);
    Matiere read(String id);
    boolean update(Matiere matiere);
    boolean delete(String id);
    List<Matiere> findAll();
    List<Matiere> findByFiliere(String filiere);
    List<Matiere> findByFiliereAndNiveau(String filiere, String niveau);
}