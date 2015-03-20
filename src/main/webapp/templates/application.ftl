[#ftl]
[#--
 dibs
 Copyright (C) 2015 Humboldt-Universität zu Berlin
 
 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License along with this
 program.  If not, see <http://www.gnu.org/licenses/>
--]

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
            <ul>
                [#if user.role == "applicant"]
                    [#if application.status == "admitted"]
                        <li>
                            <form method="POST" action="/applications/${application.id}/accept">
                                <button>Zulassung annehmen</button>
                            </form>
                        </li>
                    [/#if]
                [/#if]
            </ul>
        </aside>
    </section>

    [#if user.role == "applicant"]
        <section>
            <h2>Benötigte Informationen</h2>
            <ul>
                [#list requiredInformationMap?keys as typeId]
                    <li>
                        [#if requiredInformationMap[typeId]??]
                            <a href="/information/${requiredInformationMap[typeId].id}/">[@information_type typeId/]</a> ✔
                        [#else]
                            [@information_type typeId/] <a class="button" href="/users/${applicant.id}/create-information?type=${typeId}">anlegen</a>
                        [/#if]
                    </li>
                [/#list]
            </ul>
        </section>
    [/#if]
[/@page]

[/#escape]
