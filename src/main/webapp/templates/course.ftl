[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page course.name]
    <section class="with-aside">
        <div class="main">
            [#if user.role == "admin"]
                <p class="object-meta">
                    ${course.published?string("Veröffentlicht", "Nicht öffentlich")}.
                </p>
            [/#if]

            <p>Kapazität: ${course.capacity}</p>

            [#if course.dosv]
                <p>
                    Zulassung über
                    <a href="http://hochschulstart.de/" target="_blank">hochschulstart.de</a>.
                </p>
            [/#if]
        </div>

        <aside>
            <ul>
                [#if user.role == "applicant"]
                    <li>
                        <form method="POST" action="/courses/${course.id}/apply/">
                            <button>Bewerben</button>
                        </form>
                    </li>

                [#elseif user.role == "admin"]
                    <li>
                        [#if course.published]
                            <form method="POST" action="/courses/${course.id}/unpublish/">
                                <button>Veröffentl. zurückziehen</button>
                            </form>
                        [#else]
                            <form method="POST" action="/courses/${course.id}/publish/">
                                <button>Veröffentlichen</button>
                            </form>
                        [/#if]
                    </li>
                [/#if]
            </ul>
        </aside>
    </section>

    [#if user.role == "admin"]
        [#if ranks?size > 0]
            <section>
                <h2>Rangliste</h2>
                <ol>
                    [#list ranks as rank]
                        [#-- TODO: optimize --]
                        [#-- TODO: rank.application.id --]
                        <li><a href="/applications/${rank.id}/">${rank.user.name}</a></li>
                    [/#list]
                </ol>
            </section>
        [/#if]

        <section>
            <h2>Bewerbungen</h2>
            [#if applications?size > 0]
                <ul>
                    [#list applications as application]
                        <li>
                            [#-- TODO: optimize --]
                            <a href="/applications/${application.id}/">${application.user.name}</a>
                        </li>
                    [/#list]
                </ul>
            [#else]
                <p>Noch keine Bewerbung.</p>
            [/#if]
        </section>
    [/#if]
[/@page]

[/#escape]
