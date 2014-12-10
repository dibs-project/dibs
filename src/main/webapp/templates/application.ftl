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

        [#if user.role == "applicant"]
            <dl>
                <dt>Benötigte Informationen</dt>
                <dd><a href="/users/${applicant.id}/create-information?type=qualification">HZB</a></dd>
                <dd><a href="/users/${applicant.id}/information/${informationSet[0].id}/">${informationSet[0].type.id}</a></dd>
            </dl>
        [/#if]
    </section>
[/@page]

[/#escape]
