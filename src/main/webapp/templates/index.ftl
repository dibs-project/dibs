[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Willkommen, ${user.name}!"]
    <section>
        <h2>Navigation</h2>
        <ul>
            <li><a href="/courses/">Studiengänge</a></li>
        </ul>
    </section>
[/@page]

[/#escape]
