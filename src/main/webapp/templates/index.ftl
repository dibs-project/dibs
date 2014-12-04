[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Willkommen, ${user.name}!"]
    [#if user.role == "applicant"]
        <section>
            <h2>Bewerbungen</h2>

            [#if applications?size > 0]
                <ul>
                    [#list applications as application]
                        <li>
                            [#-- TODO: optimize --]
                            <a href="/applications/${application.id}/">${application.course.name}</a>
                        </li>
                    [/#list]
                </ul>
            [#else]
                <p>Du hast noch keine Bewerbung.</p>
            [/#if]

            <p class="dash-apply"><a href="/courses/">Jetzt bewerben!</a></p>
        </section>
    [/#if]

    <section>
        <h2>Navigation</h2>
        <ul>
            <li><a href="/courses/">Studiengänge</a></li>
        </ul>
    </section>
[/@page]

[/#escape]
