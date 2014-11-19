[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Registrieren"]
    <form method="POST" action="/register/">
        [@form_error/]
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
