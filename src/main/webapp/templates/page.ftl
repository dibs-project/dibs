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

[#escape x as x?html]

[#macro page title]
    <!DOCTYPE html>

    <html>
        <head>
            <meta charset="utf-8"/>
            <title>Reynholm University Bewerbung - ${title}</title>
            <link href="/static/style.css" rel="stylesheet"/>
        </head>

        <body>
            <header>
                <h1><a href="/">Reynholm University Bewerbung</a></h1>
                [#if user??]
                    <nav>
                        <ul>
                            <li><a href="/">Home</a></li>
                            <li><a href="/courses/">Studiengänge</a></li>
                            <li><a href="/">${user.name}</a></li>
                            <li>
                                <form method="POST" action="/logout/">
                                    <button>Abmelden</button>
                                </form>
                            </li>
                        </ul>
                    </nav>
                [/#if]
            </header>

            <div class="main">
                [#if notification??]
                    <p class="notification">${notification}</p>
                [/#if]

                <h1 class="title">${title}</h1>

                [#nested/]
            </div>
        </body>
    </html>
[/#macro]

[#macro form_error message_map]
    [#if formError??]
        <p>${message_map[formError.message]}</p>
    [/#if]
[/#macro]

[#macro application_status status]
    ${{
        "incomplete": "Unvollständig",
        "complete": "Vollständig (Warte auf Bearbeitung…)",
        "valid": "Gültig (Warte auf Zulassung…)",
        "withdrawn": "Zurückgezogen",
        "admitted": "Zugelassen",
        "confirmed": "Zulassung angenommen"
    }[status]}[#t/]
[/#macro]

[#macro information_type typeId]
    ${{
        "qualification": "Hochschulreife"
    }[typeId]}[#t/]
[/#macro]

[/#escape]
