[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Hochschulreife anlegen"]
    <form method="POST" action="/users/${user.id}/create-information?type=${type}">
        [@form_error/]
        <label>
            <small>Note</small>
            <input name="grade" value="${(form.grade[0])!}"/>
        </label>

        <p><button>Information anlegen</button></p>
    </form>
[/@page]

[/#escape]
