[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page course.name]
    <div class="content">
        <div class="content-main">
            <p>Kapazität: ${course.capacity}</p>
        </div>
    </div>
[/@page]

[/#escape]
