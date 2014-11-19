[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page course.name]
    <p>Kapazität: ${course.capacity}</p>
[/@page]

[/#escape]
