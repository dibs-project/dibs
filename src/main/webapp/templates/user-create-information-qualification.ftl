[#ftl]
[#include "page.ftl"/]
[#escape x as x?html]

[@page "Hochschulreife anlegen"]
    <form method="POST" action="/users/${user.id}/create-information?type=${type}">
        [@form_error {
            "form_grade_missing": "Note fehlt.",
            "grade_nan": "Note ist keine Zahl.",
            "args_grade_out_of_range": "Note liegt außerhalb des gültigen Bereichs."
        }/]
        <label>
            <small>Note</small>
            <input name="grade" value="${(form.grade[0])!}"/>
        </label>

        <p><button>Hochschulreife anlegen</button></p>
    </form>
[/@page]

[/#escape]
