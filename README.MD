# Spring Security Configuration for a Process Application

## Goal

Use default Spring security with OAuth2 and provide authenticated access to Camunda WebApps and
Camunda REST.

## Principle

Uses `ContainerBasedAuthenticationProvider` and default Spring Security OAuth2 configuration.

## Features

Uses standard OAuth2 Authentication Token for extracting the entire Spring- and Camunda-related
user information including the `OAuthIdentityServiceProvider`.


## Required configuration

You need to set some environment variables to make this work, pointing to your SSO. I used Keycloak
for testing.

    KEYCLOAK_BASE_URL=https://<hostname-of-keycloak-installation>/auth
    KEYCLOAK_REALM=<realm>
    KEYCLOAK_CLIENT_ID=<client-id-of-your-app>
    KEYCLOAK_CLIENT_SECRET=<client-secret-of-your-app>
    WEBAPP_REQUIRED_ROLE=<client-role-to-access-the-webapp> # not a realm role, check application.yml
 
## Paths and expected responses.

* http://localhost:8082/public/index.html will retrieve an unprotected web page, since it is on the unprotected path.
* http://localhost:8082/public/rest/info will retrieve protected information and will fail (401) if no token is provided in the request.
* http://localhost:8082/ will redirect to http://localhost:8082/app/
* http://localhost:8082/app/* will enforce the OAuth login and redirect back to the webapps (http://localhost:8082/app/welcome/).
* http://localhost:8082/rest/engine will retrieve the name of the camunda engines initialized ("default") and will fail (401) if no token is provided in the request.
 