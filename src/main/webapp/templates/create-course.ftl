[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Studiengang anlegen"]
    <form method="POST" action="/create-course/">
        [#if error??]
            <p>Fehler: ${error}</p>
        [/#if]
        <label>
            <small>Name</small>
            <input name="name" value="${(form.name[0])!}"/>
        </label>
        <label>
            <small>Kapazität</small>
            <input name="capacity" value="${(form.capacity[0])!}"/>
        </label>
        <p class="buttons"><button>Studiengang anlegen</button></p>
    </form>
[/@page]

[/#escape]
