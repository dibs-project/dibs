<#--
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
-->

[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page course.name]
    <section class="with-aside">
        <div class="main">
            [#if user.role == "admin"]
                <p class="object-meta">
                    ${course.published?string("Veröffentlicht", "Nicht öffentlich")}.
                    ${course.admission?string("Zulassung läuft.", "")}
                </p>
            [/#if]

            <p>Kapazität: ${course.capacity}</p>

            [#if course.dosv]
                <p>
                    Zulassung über
                    <a href="http://hochschulstart.de/" target="_blank">hochschulstart.de</a>.
                </p>
            [/#if]
        </div>

        <aside>
            <ul>
                [#if user.role == "applicant"]
                    <li>
                        <form method="POST" action="/courses/${course.id}/apply/">
                            <button>Bewerben</button>
                        </form>
                    </li>

                [#elseif user.role == "admin"]
                    <li>
                        [#if course.published]
                            <form method="POST" action="/courses/${course.id}/unpublish/">
                                <button>Veröffentl. zurückziehen</button>
                            </form>
                        [#else]
                            <form method="POST" action="/courses/${course.id}/publish/">
                                <button>Veröffentlichen</button>
                            </form>
                        [/#if]
                    </li>
                    [#if !course.admission]
                        <li>
                            <form method="POST" action="/courses/${course.id}/start-admission/">
                                <button>Zulassung starten</button>
                            </form>
                        </li>
                    [/#if]
                [/#if]
            </ul>
        </aside>
    </section>

    [#if user.role == "admin"]
        [#if ranks?size > 0]
            <section>
                <h2>Rangliste</h2>
                <ol>
                    [#list ranks as rank]
                        [#-- TODO: optimize --]
                        <li><a href="/applications/${rank.application.id}/">${rank.user.name}</a></li>
                    [/#list]
                </ol>
            </section>
        [/#if]

        <section>
            <h2>Bewerbungen</h2>
            [#if applications?size > 0]
                <ul>
                    [#list applications as application]
                        <li>
                            [#-- TODO: optimize --]
                            <a href="/applications/${application.id}/">${application.user.name}</a>
                        </li>
                    [/#list]
                </ul>
            [#else]
                <p>Noch keine Bewerbung.</p>
            [/#if]
        </section>
    [/#if]
[/@page]

[/#escape]
