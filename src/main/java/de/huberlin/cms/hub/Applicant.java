/*
 * hub
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Bewerber, der mit dem Bewerbungssystem interagiert.
 */
public class Applicant extends User {

    /**
     * Status der Registrierung, bzw. des Benutzerkontos.
     */
    public enum RegistrationStatus {
        /** Konto beantragt */
        REQUESTED,
        /** registriert */
        REGISTERED,
        /** Konto zurückgezogen */
        WITHDRAWN;

        /**
         * Konvertiert einen onlbew_reg.reg_status- zu einem RegistrationStatus-Wert.
         *
         * @param value Wert aus onlbew_reg.reg_status
         * @return korrespondierenden RegistrationStatus-Wert
         */
        @Deprecated
        static RegistrationStatus fromRegStatus(String value) {
            if (value.equals("AN")) {
                return REQUESTED;
            } else if (value.equals("OK")) {
                return REGISTERED;
            } else if (value.equals("XX")) {
                return WITHDRAWN;
            } else {
                // unerreichbar
                throw new RuntimeException();
            }
        }
    }

    Date birthday;
    String phoneHome;
    String phoneMobile;
    Address address;
    String birthName;
    String birthPlace;
    String citizenship;
    Date registrationTime;
    RegistrationStatus registrationStatus;
    boolean dosv;
    String dosvBid;

    /**
     * Initialisiert den Applicant.
     */
    public Applicant(int id, String surname, String firstName, String title, String infix,
            Gender gender, String email, String huAccount, String password, Date birthday,
            String phoneHome, String phoneMobile, Address address, String birthName,
            String birthPlace, String citizenship, Date registrationTime,
            RegistrationStatus registrationStatus, boolean dosv, String dosvBid) {
        super(id, surname, firstName, title, infix, gender, email, huAccount, password);
        this.birthday = birthday;
        this.phoneHome = phoneHome;
        this.phoneMobile = phoneMobile;
        this.address = address;
        this.birthName = birthName;
        this.birthPlace = birthPlace;
        this.citizenship = citizenship;
        this.registrationTime = registrationTime;
        this.registrationStatus = registrationStatus;
        this.dosv = dosv;
        this.dosvBid = dosvBid;
    }

    /**
     * Initialisiert den Applicant via Datenbankcursor.
     *
     * @param results Datenbankcursor, der auf eine Zeile aus onlbew.onlbew_reg verweist
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    Applicant(ResultSet results) throws SQLException {
        this(
            results.getInt("reg_id"), // id
            results.getString("nachname"), // surname
            results.getString("vorname"), // firstName
            results.getString("anti"), // title
            results.getString("antizudtxt"), // infix
            Gender.fromGeschl(results.getString("geschl")), // gender
            results.getString("email"), // email
            null, // huAccount TODO
            results.getString("pwd"), // password
            results.getDate("gebdat"), // birthday
            results.getString("potel"), // phoneHome
            null, // phoneMobile TODO
            new Address(
                results.getString("postrasse"), // street
                results.getString("poplz"), // postalCode
                results.getString("poort"), // town
                results.getString("pokfz"), // country
                results.getString("pozusatz") // supplement
            ),
            results.getString("gebname"), // birthName
            results.getString("gebort"), // birthPlace
            results.getString("staat"), // citizenship
            new Date(results.getTime("reg_time").getTime() +
                results.getDate("reg_date").getTime()), // registrationTime
            RegistrationStatus.fromRegStatus(
                results.getString("reg_status")), // registrationStatus
            false, // dosv TODO
            results.getString("bid") // dosvBid
        );
    }

    /**
     * Geburtstag.
     */
    public Date getBirthday() {
        return birthday;
    }

    /**
     * Telefonnummer (Festnetz).
     */
    public String getPhoneHome() {
        return phoneHome;
    }

    /**
     * Telefonnummer (Mobil).
     */
    public String getPhoneMobile() {
        return phoneMobile;
    }

    /**
     * Postadresse.
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Geburtsname.
     */
    public String getBirthName() {
        return birthName;
    }

    /**
     * Geburtsort.
     */
    public String getBirthPlace() {
        return birthPlace;
    }

    /**
     * Staatsbürgerschaft.
     */
    public String getCitizenship() {
        return citizenship;
    }

    /**
     * Registrierungszeitpunkt.
     */
    public Date getRegistrationTime() {
        return registrationTime;
    }

    /**
     * Registrierungsstatus, bzw.&nbsp;Status des Benutzerkontos.
     */
    public RegistrationStatus getRegistrationStatus() {
        return registrationStatus;
    }

    /**
     * Hat der Benutzer ein DoSV-Konto?
     */
    public boolean isDosv() {
        return dosv;
    }

    /**
     * DoSV-BID (Benutzeridentifikation).
     */
    public String getDosvBid() {
        return dosvBid;
    }
}
