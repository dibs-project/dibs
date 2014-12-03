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
        </div>

        <aside>
            <ul>
                [#if user.role == "applicant"]
                    [#-- TODO: apply --]
                    <li>
                        <form method="POST" action="/users/${user.id}/connect-to-dosv/">
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
[/@page]

[/#escape]
