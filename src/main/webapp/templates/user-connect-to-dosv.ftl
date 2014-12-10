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
            [@form_error/]
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
