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

[@page "Studiengänge"]
    [#if user.role == "applicant"]
        <section>
            <ul>
                [#list service.getCourses({"published": true}) as course]
                    <li><a href="/courses/${course.id}/">${course.name}</a></li>
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
