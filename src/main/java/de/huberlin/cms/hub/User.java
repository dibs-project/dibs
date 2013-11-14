/*
 * hub
 */

package de.huberlin.cms.hub;

/**
 * Benutzer, der mit dem Bewerbungssystem interagiert.
 * <p>
 * Der Name eines Benutzers setzt sich wie folgt zusammen:
 * <pre>
 *    Doktor Maurice   van   Moss
 *    title  firstName infix surname
 * </pre>
 */
public abstract class User {

    /**
     * Geschlecht.
     */
    public enum Gender {
        /** männlich / Mann */
        MALE,
        /** weiblich / Frau */
        FEMALE;

        /**
         * Konvertiert einen onlbew_reg.geschl- zu einem Gender-Wert.
         *
         * @param value Wert aus onlbew_reg.geschl
         * @return korrespondierenden Gender-Wert
         */
        @Deprecated
        static Gender fromGeschl(String value) {
            if (value.equals("M")) {
                return MALE;
            } else if (value.equals("W")) {
                return FEMALE;
            } else {
                // unerreichbar
                throw new RuntimeException();
            }
        }
    }

    protected int id;
    protected String surname;
    protected String firstName;
    protected String title;
    protected String infix;
    protected Gender gender;
    protected String email;
    protected String huAccount;
    protected String password;

    /**
     * Initialisiert den User.
     */
    public User(int id, String surname, String firstName, String title, String infix,
            Gender gender, String email, String huAccount, String password) {
        this.id = id;
        this.surname = surname;
        this.firstName = firstName;
        this.title = title;
        this.infix = infix;
        this.gender = gender;
        this.email = email;
        this.huAccount = huAccount;
        this.password = password;
    }

    /**
     * Eindeutige ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Nachname / Familienname.
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Vorname.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Titel, z.B.&nbsp;Doktor.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Namensinfix, bzw.&nbsp;Namenszusatz, z.B.&nbsp;von, zu, etc.
     */
    public String getInfix() {
        return infix;
    }

    /**
     * Geschlecht.
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Email-Adresse.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Name des HU-Accounts.
     */
    public String getHuAccount() {
        return huAccount;
    }

    // TODO: Hashformat?
    /**
     * Hash des Passworts.
     */
    public String getPassword() {
        return password;
    }
}
