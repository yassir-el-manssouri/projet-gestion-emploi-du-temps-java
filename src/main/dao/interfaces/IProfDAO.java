package main.dao.interfaces;

import main.model.Professeur;
import java.util.List;

public interface IProfDAO {
    boolean create(Professeur prof);
    Professeur read(String id);
    boolean update(Professeur prof);
    boolean delete(String id);
    List<Professeur> findAll();
}