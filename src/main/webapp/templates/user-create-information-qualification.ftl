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

[@page "Hochschulreife anlegen"]
    <form method="POST" action="/users/${user.id}/create-information?type=${type}">
        [@form_error {
            "form_grade_missing": "Note fehlt.",
            "grade_nan": "Note ist keine Zahl.",
            "args_grade_out_of_range": "Note liegt außerhalb des gültigen Bereichs."
        }/]
        <label>
            <small>Note</small>
            <input name="grade" value="${(form.grade[0])!}"/>
        </label>

        <p><button>Hochschulreife anlegen</button></p>
    </form>
[/@page]

[/#escape]
