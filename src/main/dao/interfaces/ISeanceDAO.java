package main.dao.interfaces;

import main.model.Seance;
import java.util.List;

public interface ISeanceDAO {
    boolean create(Seance seance);
    Seance read(String id);
    boolean update(Seance seance);
    boolean delete(String id);
    List<Seance> findAll();
    List<Seance> findByClasse(String classeId);
    List<Seance> findByProfesseur(String professeurId);
    List<Seance> findBySalle(String salleId);
    List<Seance> findBySemaine(String semaine);
}