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
                </nav>
            </header>

            <div>
                [#nested/]
            </div>
        </body>
    </html>
[/#macro]

[/#escape]
