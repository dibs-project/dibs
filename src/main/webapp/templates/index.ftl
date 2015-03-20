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
[/@page]

[/#escape]
