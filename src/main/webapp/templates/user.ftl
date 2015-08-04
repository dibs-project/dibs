[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page user.name]
    <section class="with-aside">
        <div class="main">
            <p>E-Mail-Adresse: ${user.email}</p>
        </div>
    </section>
[/@page]

[/#escape]
