[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page]
    <h1>Anmelden</h1>

    <form method="POST" action="/login/">
        [@form_error/]
        Email-Adresse: <input name="email" value="${(form.email[0])!}"/> Passwort:
        <input name="password" type="password" value="${(form.password[0])!}"/>
        <button>Anmelden</button>
    </form>

    <p><a href="/register/">Registrieren</a></p>
[/@page]

[/#escape]
