# KYPO2 User and group
This project represents back-end for managing users, groups and roles of KYPO project.

## How to set up the project

### Getting Masaryk University OpenID Connect credentials 

1. Go to `https://oidc.ics.muni.cz/oidc/` and log in.
1. Click on "**Self-service Client Registration**" -> "**New Client**".
1. Set Client name.
1. Add at least one custom Redirect URI and `http://localhost:8080/{context path from external properties file}/webjars/springfox-swagger-ui/oauth2-redirect.html` (IMPORTANT for Swagger UI).
1. In tab "**Access**":
    1. choose which information about user you will be getting, so called `scopes`.
    1. select just *implicit* in **Grand Types**
    1. select *token* and *code id_toke* in **Responses Types**
1. Hit **Save** button.
1. Then got to tab "**JSON**", copy the JSON file and save it to file. **IMPORTANT STEP**
1. Now create new Resource in "Self-service Protected Resource Registration".
1. Again insert client Name and save JSON to external file in "**JSON**" tab.
1. In tab "**Access**" again choose which information about user you will be getting, so called `scopes`.
1. Hit **Save** button.
