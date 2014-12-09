[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Bewerbung"]
    <section class="with-aside">
        <div class="main">
            <dl class="object-primary">
                [#if user.role == "admin"]
                    <dt>Bewerber</dt>
                    <dd>${applicant.name}</dd>
                [/#if]
                <dt>Studiengang</dt>
                <dd><a href="/courses/${course.id}/">${course.name}</a></dd>
            </dl>
            <p>Status: [@application_status application.status/].</p>
        </div>
        <aside>
            [#if user.role == "applicant"]
                [#if application.status == "admitted"]
                    <form method="POST" action="/applications/${application.id}/accept">
                        <button>Zulassung annehmen</button>
                    </form>
                [/#if]
            [/#if]
        </aside>
    </section>
[/@page]

[/#escape]
