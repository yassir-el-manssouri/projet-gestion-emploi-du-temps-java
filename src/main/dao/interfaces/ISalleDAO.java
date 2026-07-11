package main.dao.interfaces;

import main.model.Salle;
import java.util.List;

public interface ISalleDAO {
    boolean create(Salle salle);
    Salle read(String id);
    boolean update(Salle salle);
    boolean delete(String id);
    List<Salle> findAll();
    List<Salle> findBySite(String site);
}