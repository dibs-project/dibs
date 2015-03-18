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

[@page "Mit hochschulstart.de Verbinden"]
    <section>
        <p>
            Falls du noch kein Konto auf
            <a href="https://dosv.hochschulstart.de/" target="_blank">hochschulstart.de</a>
            hast, kannst du dich
            <a href="https://dosv.hochschulstart.de/" target="_blank">jetzt registrieren</a>.
        </p>

        <form method="POST" action="${url}">
            [@form_error {
                "form_dosv-bid_missing": "Bewerber-ID (BID) fehlt.",
                "form_dosv-ban_missing": "Bewerber-Authentifizierungsnummer (BAN) fehlt.",
                "dosv_bid_dosv_ban_bad": "Zugangsdaten sind nicht korrekt."
            }/]
            <label>
                <small>Bewerber-ID (BID)</small>
                <input name="dosv-bid" value="${(form['dosv-bid'][0])!}"/>
            </label>
            <label>
                <small>Bewerber-Authentifizierungsnummer (BAN)</small>
                <input name="dosv-ban" value="${(form['dosv-ban'][0])!}"/>
            </label>
            <p><button>Verbinden</button></p>
        </form>
    </section>
[/@page]

[/#escape]
