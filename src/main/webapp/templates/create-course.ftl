[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Studiengang anlegen"]
    <form method="POST" action="/create-course/">
        [@form_error {
            "form_name_missing": "Name fehlt.",
            "form_capacity_missing": "Kapazität fehlt.",
            "capacity_nan": "Kapazität ist keine Zahl.",
            "capacity_nonpositive": "Kapazität ist nicht positiv."
        }/]
        <label>
            <small>Name</small>
            <input name="name" value="${(form.name[0])!}"/>
        </label>
        <label>
            <small>Kapazität</small>
            <input name="capacity" value="${(form.capacity[0])!}"/>
        </label>
        <label>
            <input type="checkbox" name="dosv" [#if (form.dosv[0])??]checked="checked"[/#if]/>
            <small>
                Zulassung über
                <a href="http://hochschulstart.de/" target="_blank">hochschulstart.de</a>
            </small>
        </label>
        <p><button>Studiengang anlegen</button></p>
    </form>
[/@page]

[/#escape]
