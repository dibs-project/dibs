[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Studiengänge"]
    [#if user.role == "applicant"]
        <section>
            <ul>
                [#list service.getCourses({"published": true}) as course]
                    <li><a href="/courses/${course.id}/">${course.name}</a>
                [/#list]
            </ul>
        </section>

    [#elseif user.role == "admin"]
        <section class="with-aside">
            <div class="main">
                <ul>
                    [#list service.courses as course]
                        <li><a href="/courses/${course.id}/">${course.name}</a></li>
                    [/#list]
                </ul>
            </div>

            <aside>
                <ul>
                    <li>
                        <a class="button" href="/create-course/">Studiengang anlegen</a>
                    </li>
                </ul>
            </aside>
        </section>
    [/#if]
[/@page]

[/#escape]
