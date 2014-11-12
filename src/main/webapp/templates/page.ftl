[#ftl]
[#escape x as x?html]

[#macro page]
    <!DOCTYPE html>

    <html>
        <head>
            <meta charset="utf-8"/>
            <title>Reynholm University Bewerbung</title>
        </head>

        <body>
            <header>
                <nav>
                    <a href="/">Reynholm University Bewerbung</a>
                    <a href="/courses/">Studiengänge</a>
                    [#if user??]
                         <form method="POST" action="/logout/">
                            ${user.name} <button>Abmelden</button>
                         </form>
                    [/#if]
                </nav>
            </header>

            <div>
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
