[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page]
    <h1>Studiengänge</h1>
    <ul>
        [#list service.courses as course]
            <li><a href="/courses/${course.id}/">${course.name}</a></li>
        [/#list]
    </ul>
    <p><a href="/create-course/">Studiengang anlegen</a></p>
[/@page]

[/#escape]
