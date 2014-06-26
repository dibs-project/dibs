package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Markus Michler
 */
public class Quota extends HubObject {
    private String name;
    private Double percentage;

    Quota(Map<String, Object> args) {

        super(null, null);
    }

    Quota(String id, ApplicationService service, String name, Double percentage) {
        super(id, service);
        this.name = name;
        this.percentage = percentage;
    }

    Quota(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert die Quote über den Datenbankcursor
        this(results.getString("id"), service, results.getString("name"), results
            .getDouble("percentage"));

    }

    void addRankingCriteria(List<Criterion> rankingCriteria) {
        // TODO stub
    }

    /**
     * Name der Quote
     */
    public String getName() {
        return name;
    }

    /**
     * Prozentualer Anteil der Quote an der Gesamtzahl der vergebenen Studienplätze
     */
    public Double getPercentage() {
        return percentage;
    }

    /**
     * Kriterien für die Sortierung auf der Rangliste
     */
    public List<Criterion> getRankingCriteria() {
        List<Criterion> rankingCriteria = new ArrayList<Criterion>();
        ResultSet results;
        try {
            String query =
                "SELECT criterion FROM quota_ranking_criteria WHERE quota_id = ?";
            PreparedStatement statement = service.getDb().prepareStatement(query);
            statement.setString(1, id);
            results = statement.executeQuery();
            while (results.next()) {
                rankingCriteria.add((Criterion) Class.forName(
                    results.getString("criterion")).newInstance());
            }
        } catch (SQLException e) {
            throw new IOError(e);
        } catch (InstantiationException e) {
            // nicht erreichbar, da alle Kriterien instanziiert werden können.
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            // nicht erreichbar, da alle Methoden der Kriterien per Reflection erreichbar
            // sind.
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            // nicht erreichbar, da der Klassenname des Kriteriums nicht durch
            // Benutzereingaben beeinflusst wird.
            throw new RuntimeException(e);
        }
        // TODO mit Kriterienliste statt Classloader arbeiten (siehe Information).
        return rankingCriteria;
    }

    // TODO Kriterien für die Aufnahme von Bewerbungen in die Quote:
    // List<Criterion> getInclusionCriteria()

}
