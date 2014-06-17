package de.huberlin.cms.hub;

/**
 * @author Markus Michler
 *
 */
public class Quota /* extends HubObject */{
    private String name;
    private Double percentage;

    /* private List<Criterion> rankingCriteria; */
    /* private List<Criterion> inclusionCriteria; */

    Quota(String id, ApplicationService service, String name, Double value /*,List<Criterion> rankingCriteria, List<Criterion> inclusionCriteria*/) {
        /* super(id, service); */
        this.name = name;
        this.percentage = value;
        /* this.rankingCriteria = rankingCriteria; 
        this.inclusionCriteria = inclusionCriteria;*/
    }

    //TODO Konstruktor mit Resultset

    /**
     * Name der Quote
     */
    public String getName() {
        return name;
    }

    /**
     *
     */
    public Double getPercentage() {
        return percentage;
    }

}
