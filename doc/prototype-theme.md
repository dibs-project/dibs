HUB Prototype Theme
===================

Layout
------

--------------------------------
| header                       |
|------------------------------|
| title                        |
|------------------------------|
| section main | section aside |
|------------------------------|
| ...                          |
--------------------------------

Colors
------

 * Text / Primary: #333
 * Background:     #fff

Spacing
-------

General:

-----------
|         |
|  Lorem  | < line height: 1.5rem
|  ipsum  |
|         | < vertical margin: 1.5rem
|  Lorem  |
|  ipsum  |
|         |
-----------
 ^
 horizontal padding: 1.5rem

Button / Input:

/------\ < border radius: 0.25rem
|      | < padding: 0.25rem
| Text |
|      |
\------/

Forms
-----

```html
<form method="POST" action="/login/">
    [@form_error/]
    <label>
        <small>Email address</small>
        <input name="email" value="${(form.email[0])!}"/>
    </label>
    ...
    <p><button>Login</button></p>
</form>
```
