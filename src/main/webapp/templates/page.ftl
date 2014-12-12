[#ftl]
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

[#macro form_error]
    [#if formError??]
        <p>Fehler: ${formError}</p>
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

[/#escape]
