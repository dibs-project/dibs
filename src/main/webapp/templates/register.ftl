[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Registrieren"]
    <form method="POST" action="/register/">
        [@form_error {
            "form_name_missing": "Name fehlt.",
            "form_email_missing": "Email-Adresse fehlt.",
            "form_password_missing": "Passwort fehlt."
        }/]
        <label>
            <small>Name</small>
            <input name="name" value="${(form.name[0])!}"/>
        </label>
        <label>
            <small>Email-Adresse</small>
            <input name="email" value="${(form.email[0])!}"/>
        </label>
        <label>
            <small>Passwort</small>
            <input name="password" type="password" value="${(form.password[0])!}"/>
        </label>
        <p><button>Registrieren</button></p>
    </form>
[/@page]

[/#escape]
