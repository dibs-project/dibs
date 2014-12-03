[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Mit hochschulstart.de Verbinden"]
    <section>
        <p>
            Du benötigst ein Konto auf
            <a href="https://dosv.hochschulstart.de/">hochschulstart.de</a>.
        </p>

        <form method="POST" action="/users/${user.id}/connect-to-dosv/">
            [@form_error/]
            <label>
                <small>BID</small>
                <input name="dosv_bid" value="${(form.dosv_bid[0])!}"/>
            </label>
            <label>
                <small>BAN</small>
                <input name="dosv_ban" value="${(form.dosv_ban[0])!}"/>
            </label>
            <p><button>Verbinden</button></p>
        </form>
    </section>
[/@page]

[/#escape]
