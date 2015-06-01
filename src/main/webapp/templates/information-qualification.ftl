[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Hochschulreife"]
    <section>
        <dl>
            <dt>Note</dt>
            <dd>${information.grade}</dd>
        </dl>
    </section>
[/@page]

[/#escape]
