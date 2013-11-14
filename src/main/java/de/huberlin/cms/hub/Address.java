/*
 * hub
 */

package de.huberlin.cms.hub;

/**
 * Postadresse.
 */
public class Address {
    /** Straße und Hausnummer. */
    public String street;
    /** Postleitzahl. */
    public String postalCode;
    /** Ort. */
    public String town;
    /** Land, bzw.&nbsp;Staat. */
    public String country;
    /** Adresszusatz. */
    public String supplement;

    /**
     * Erzeugt eine Postadresse mit leeren Feldern.
     */
    public Address() {
        this.street = null;
        this.postalCode = null;
        this.town = null;
        this.country = null;
        this.supplement = null;
    };

    /**
     * Erzeugt eine Postadresse.
     */
    public Address(String street, String postalCode, String town, String country,
        String supplement) {
        this.street = street;
        this.postalCode = postalCode;
        this.town = town;
        this.country = country;
        this.supplement = supplement;
    }
}
