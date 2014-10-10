[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page]
    <h1>Studiengang anlegen</h1>

    [#if error??]
        <p>Fehler: ${error}</p>
    [/#if]

    <form method="POST" action="/create-course/">
        Name: <input name="name" value="${(form.name[0])!}"/> Kapazität:
        <input name="capacity" value="${(form.capacity[0])!}"/>
        <button>Studiengang anlegen</button>
    </form>
[/@page]

[/#escape]
