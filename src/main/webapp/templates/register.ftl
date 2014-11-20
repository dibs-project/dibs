[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page]
    <h1>Registrieren</h1>

    <form method="POST" action="/register/">
        [@form_error/]
        Name: <input name="name" value="${(form.name[0])!}"/> Email-Adresse:
        <input name="email" value="${(form.email[0])!}"/> Passwort:
        <input name="password" type="password" value="${(form.password[0])!}"/>
        <button>Registrieren</button>
    </form>
[/@page]

[/#escape]
