[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Studiengänge"]
    <div class="content">
        <div class="content-main">
            <ul>
                [#list service.courses as course]
                    <li><a href="/courses/${course.id}/">${course.name}</a></li>
                [/#list]
            </ul>
        </div>

        <aside class="content-actions">
            <ul>
                <li><a class="button" href="/create-course/">Studiengang anlegen</a></li>
            </ul>
        </aside>
    </div>
[/@page]

[/#escape]
