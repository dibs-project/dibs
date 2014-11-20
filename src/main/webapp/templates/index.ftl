[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page]
    <h1>Willkommen, ${user.name}!</h1>

    [#if user.role == "applicant"]
        <p>Bewirb dich!</p>
    [#elseif user.role == "admin"]
        <p>Administriere!</p>
    [/#if]
[/@page]

[/#escape]
