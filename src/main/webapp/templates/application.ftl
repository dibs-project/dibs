[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Bewerbung"]
    <section>
        <dl class="object-primary">
            [#if user.role == "admin"]
                <dt>Bewerber</dt>
                <dd>${applicant.name}</dd>
            [/#if]
            <dt>Studiengang</dt>
            <dd><a href="/courses/${course.id}/">${course.name}</a></dd>
        </dl>

        <p>Status: [@application_status application.status/].</p>
        <aside>
            [#if user.role == "applicant"]
                [#if application.status == "admitted"]
                    <form method="POST" action="applications/${application.id}/accept">
                        <button>Annehmen</button>
                    </form>
                [/#if]
            [/#if]
        </aside>
    </section>
[/@page]

[/#escape]
