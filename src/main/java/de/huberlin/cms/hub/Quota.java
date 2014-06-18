package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus Michler
 *
 */
public class Quota extends HubObject {
    // TODO hub.sql anpassen
    private String name;
    private Double percentage;
    private List<Criterion> rankingCriteria;
    private List<Criterion> inclusionCriteria;

    Quota(String id, ApplicationService service, String name, Double percentage,
        List<Criterion> rankingCriteria, List<Criterion> inclusionCriteria) {
        super(id, service);
        this.name = name;
        this.percentage = percentage;
        this.rankingCriteria = rankingCriteria;
        this.inclusionCriteria = inclusionCriteria;
    }

    Quota(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert den Benutzer über den Datenbankcursor
        this(results.getString("id"), service, results.getString("name"), results
            .getDouble("percentage"), new ArrayList<Criterion>(),
            new ArrayList<Criterion>());
        // TODO Criterion-Objekte laden
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
        return rankingCriteria;
    }

    /**
     * Kriterien für die Aufnahme von Bewerbungen in die Quote
     */
    public List<Criterion> getInclusionCriteria() {
        return inclusionCriteria;
    }

}
