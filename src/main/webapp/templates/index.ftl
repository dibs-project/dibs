[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Willkommen, ${user.name}!"]
    <section>
        [#if user.role == "applicant"]
            <p>Bewirb dich!</p>
        [#elseif user.role == "admin"]
            <p>Administriere!</p>
        [/#if]
    </section>

    <section>
        <h2>Navigation</h2>
        <ul>
            <li><a href="/courses/">Studiengänge</a></li>
        </ul>
    </section>
[/@page]

[/#escape]
