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
                <nav>
                    <ul>
                        <li><a href="/">Reynholm University Bewerbung</a></li>
                        [#-- NOTE: wandert evtl. ins Dashboard --]
                        <li><a href="/courses/">Studiengänge</a></li>
                    </ul>
                    [#if user??]
                         <form method="POST" action="/logout/">
                            ${user.name} <button>Abmelden</button>
                         </form>
                    [/#if]
                </nav>
            </header>

            <div class="main">
                <h1>${title}</h1>

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

[/#escape]
