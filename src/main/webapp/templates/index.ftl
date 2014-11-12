[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page]
    <h1>Willkommen, ${user.name}!</h1>
[/@page]

[/#escape]
