package main.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton Observable qui centralise la synchronisation des données entre panneaux.
 * Quand un prof ou une matière est ajouté / modifié / supprimé,
 * tous les abonnés (panneaux) sont notifiés et se rechargent.
 */
public class DataSyncManager {

    // ─── Types d'événements ─────────────────────────────────────────────────
    public enum EventType {
        PROF_ADDED,
        PROF_UPDATED,
        PROF_DELETED,
        MATIERE_ADDED,
        MATIERE_UPDATED,
        MATIERE_DELETED,
        AFFECTATION_CHANGED  // affecterProf / retirerProf
    }

    // ─── Interface abonnés ──────────────────────────────────────────────────
    public interface DataChangeListener {
        void onDataChanged(EventType event);
    }

    // ─── Singleton ──────────────────────────────────────────────────────────
    private static DataSyncManager instance;

    public static DataSyncManager getInstance() {
        if (instance == null) {
            instance = new DataSyncManager();
        }
        return instance;
    }

    private DataSyncManager() {}

    // ─── Liste des abonnés ──────────────────────────────────────────────────
    private final List<DataChangeListener> listeners = new ArrayList<>();

    public void addListener(DataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Émet un événement vers tous les abonnés (sauf la source pour éviter boucle).
     *
     * @param event  type d'événement
     * @param source panneau émetteur (peut être null)
     */
    public void fireEvent(EventType event, DataChangeListener source) {
        for (DataChangeListener l : new ArrayList<>(listeners)) {
            if (l != source) {
                l.onDataChanged(event);
            }
        }
    }

    /** Commodité : émet vers tous les abonnés sans exception. */
    public void fireEvent(EventType event) {
        fireEvent(event, null);
    }
}
