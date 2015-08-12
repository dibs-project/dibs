[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page userObject.name]
    <section class="with-aside">
        <div class="main">
            <p>E-Mail-Adresse: ${userObject.email}</p>
        </div>
    </section>
[/@page]

[/#escape]
