[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page]
    <h1>${course.name}</h1>
    <p>Kapazität: ${course.capacity}</p>
[/@page]

[/#escape]
