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

[@page "Anmelden"]
    <form method="POST" action="/login/">
        [@form_error {
            "form_email_missing": "Email-Adresse fehlt.",
            "form_password_missing": "Passwort fehlt.",
            "email_password_bad": "Zugangsdaten sind nicht korrekt."
        }/]
        <label>
            <small>E-Mail-Adresse</small>
            <input name="email" value="${(form.email[0])!}"/>
        </label>
        <label>
            <small>Passwort</small>
            <input name="password" type="password" value="${(form.password[0])!}"/>
        </label>
        <p><button>Anmelden</button></p>
    </form>

    <p><a href="/register/">Noch kein Benutzerkonto? Jetzt registrieren!</a></p>
[/@page]

[/#escape]
