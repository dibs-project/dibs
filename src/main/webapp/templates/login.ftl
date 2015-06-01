[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Anmelden"]
    <form method="POST" action="/login/">
        [@form_error {
            "form_email_missing": "Email-Adresse fehlt.",
            "form_password_missing": "Passwort fehlt.",
            "email_password_bad": "Zugangsdaten sind nicht korrekt."
        }/]
        <label>
            <small>E-Mail-Adresse</small>
            <input name="email" value="${(form.email[0])!}"/>
        </label>
        <label>
            <small>Passwort</small>
            <input name="password" type="password" value="${(form.password[0])!}"/>
        </label>
        <p><button>Anmelden</button></p>
    </form>

    <p><a href="/register/">Noch kein Benutzerkonto? Jetzt registrieren!</a></p>
[/@page]

[/#escape]
