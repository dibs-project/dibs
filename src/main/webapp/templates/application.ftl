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
            <section>
                <h2>Benötigte Informationen</h2>
                <ul>
                    [#list requiredTypesToInformationMap?keys as typeId]
                        <li>
                            [#if requiredTypesToInformationMap[typeId]??]
                                <a href="/users/${applicant.id}/information/${requiredTypesToInformationMap[typeId].id}/">[@information_type typeId/]</a> ✔
                            [#else]
                                [@information_type typeId/] <a class="button" href="/users/${applicant.id}/create-information?type=${typeId}">anlegen</a>
                            [/#if]
                        </li>
                    [/#list]
                </ul>
            </section>
        [/#if]
    </section>
[/@page]

[/#escape]
