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

[@page "Studiengang anlegen"]
    <form method="POST" action="/create-course/">
        [@form_error {
            "form_name_missing": "Name fehlt.",
            "form_capacity_missing": "Kapazität fehlt.",
            "capacity_nan": "Kapazität ist keine Zahl.",
            "capacity_nonpositive": "Kapazität ist nicht positiv."
        }/]
        <label>
            <small>Name</small>
            <input name="name" value="${(form.name[0])!}"/>
        </label>
        <label>
            <small>Kapazität</small>
            <input name="capacity" value="${(form.capacity[0])!}"/>
        </label>
        <label>
            <input type="checkbox" name="dosv" [#if (form.dosv[0])??]checked="checked"[/#if]/>
            <small>
                Zulassung über
                <a href="http://hochschulstart.de/" target="_blank">hochschulstart.de</a>
            </small>
        </label>
        <p><button>Studiengang anlegen</button></p>
    </form>
[/@page]

[/#escape]
