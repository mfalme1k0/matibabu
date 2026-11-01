## 1. Purpose of This Document

This document explains how backend developers should test APIs while Spring Security is enabled in the application.
It is not a final or permanent specification. 
The security implementation, authentication flow, CSRF handling, authorization rules, and testing procedures may change as the project evolves. 
Developers should refer to the latest version of this document and the current security configuration when testing the application.

Need to test your API without going through the security workflow? 
See Section 23 — If You Only Want to Test Your Own API Without Going Through the Security Workflow. 
This option is intended only for developers who need to temporarily bypass the security workflow while working on their own API and should not be committed as the normal project configuration.

The backend currently uses **session-based authentication** with:

* Spring Security
* Form-based login
* HTTP sessions
* CSRF protection
* Role-based authorization
* Email and password authentication
* Session cookies (`JSESSIONID`)
* CSRF tokens sent using the `X-XSRF-TOKEN` request header

Until the frontend is integrated, developers testing APIs directly should follow this workflow when interacting with secured endpoints.

The purpose of this document is **not** to explain the internal implementation of Spring Security. 
It is a practical testing workflow that developers can follow when using tools such as IntelliJ HTTP Client, Postman, Apidog or similar API clients.


# 2. Understanding Which Endpoints Need Security

Not every endpoint requires authentication.
The application currently has three broad categories of endpoints:

1. **Unsecured/public endpoints**
2. **Authenticated endpoints**
3. **Role-protected endpoints**

The security configuration determines which category an endpoint belongs to.

## 2.1 Unsecured endpoints

Public endpoints can be accessed without logging in.

For example:

* `GET /`
* `POST /auth/register`
 
More unsecured endpoints will be added if required later.

A developer does not need an authenticated session to access these endpoints.

However, because CSRF protection is enabled, a state-changing public endpoint such as registration still requires a valid CSRF token.

Therefore:

> `permitAll()` means the endpoint does not require authentication. It does **not** mean CSRF protection is bypassed.

---

# 3. CSRF Protection

CSRF protection is enabled globally.
CSRF protection applies primarily to requests that change server-side state, including:

* `POST`
* `PUT`
* `PATCH`
* `DELETE`

A valid CSRF token must be supplied when making these requests.

A new token does **not** need to be generated for every request.

The same valid token can be reused for multiple requests while it remains valid.

The important distinction is:

> A request requires a valid/current CSRF token, not necessarily a newly generated token.

---

# 4. Requesting a CSRF Token

Before making a state-changing request, make sure you have a valid CSRF token available. 
If you do not already have one, request a CSRF token first.

The developer should first make the request that obtains the CSRF token.

```
GET http://localhost:8080/csrf
```
Check the requests.http file in the project for an example 

After doing so, the response will provide an `XSRF-TOKEN` cookie containing the CSRF token.

The API client should preserve this cookie.

For example, the client may receive:

Set-Cookie: XSRF-TOKEN=<csrf-token>

The token value contained in that cookie is the value that should be supplied in the request header.

---

# 5. Adding the CSRF Token to a Request

For state-changing requests, add the CSRF token as the following request header:

X-XSRF-TOKEN: <csrf-token>

For example:

```
POST http://localhost:8080/auth/register
Content-Type: application/json
X-XSRF-TOKEN: <current-csrf-token>

{
"email": "user@example.com",
"password": "password123"
}
```

Check the requests.http file in the project also

The value must correspond to the valid CSRF token obtained from the application.

Do not manually invent a token.

Do not copy an old token from an unrelated session.

Do not generate a random string and use it as the CSRF token.

---

# 6. Testing the Registration Endpoint

Registration is a public operation, meaning authentication is not required.

The registration workflow is:

```
Request CSRF token
        ↓
Receive XSRF-TOKEN
        ↓
Send POST /auth/register
        ↓
Include X-XSRF-TOKEN header
        ↓
Application validates CSRF token
        ↓
Registration endpoint executes
        ↓
User is saved in database
```

## Step 1 — Obtain a CSRF token

Request a CSRF token before attempting registration.

```
GET http://localhost:8080/csrf
```

Make sure the API client preserves the returned `XSRF-TOKEN` cookie.


## Step 2 — Register the user

Send:

```
POST /auth/register
```

with the appropriate request body.

The request must include:

```
X-XSRF-TOKEN: <current-csrf-token>
```

For example:

```
POST http://localhost:8080/auth/register
Content-Type: application/json
X-XSRF-TOKEN: pE2x_SlJJXaTRZnVy1aVsC1c1fi1-FV_U05xsiJc6qGWLZRIlHyAzU8rEEC-d_zh-nuh0hw_-JnTnGNSMX1AghNqjMCgH6J_

{
"email": "user@example.com",
"password": "password123"
}
```

## Step 3 — Confirm registration

A successful registration should return the application's successful registration response. (201 CREATED, or a message depending on how it has been configured)

The newly registered user should also exist in the database.

At this point, the user is registered but is **not automatically authenticated merely because registration succeeded**.

---

# 7. Logging In

After registration, the developer can authenticate using the newly created user's credentials.
This request requires a valid CSRF token.

The login endpoint is:

```
POST /login
```

Spring Security processes this request.
The login request uses:

```
Content-Type: application/x-www-form-urlencoded
```

The credentials are sent as form parameters:

```
email=<email>&password=<password>
```

The configured username parameter is `email`, not `username`.

Therefore, use:

```
email
```

for the user's email address.

For example:

```
POST http://localhost:8080/login
Content-Type: application/x-www-form-urlencoded
X-XSRF-TOKEN: pE2x_SlJJXaTRZnVy1aVsC1c1fi1-FV_U05xsiJc6qGWLZRIlHyAzU8rEEC-d_zh-nuh0hw_-JnTnGNSMX1AghNqjMCgH6J_

email=user@example.com&password=password123
```


# 8. CSRF and Login

Login is also a state-changing operation and therefore requires a valid CSRF token.

Before login:

1. Obtain a valid CSRF token.
2. Preserve the `XSRF-TOKEN` cookie.
3. Send the login request.
4. Add the token to the `X-XSRF-TOKEN` header.
5. Send the email and password as form parameters.

The login request should therefore contain:

```
X-XSRF-TOKEN: <current-csrf-token>
```

and:

```
email=<email>&password=<password>
```

with:

```
Content-Type: application/x-www-form-urlencoded
```

Example:

```
POST http://localhost:8080/login
Content-Type: application/x-www-form-urlencoded
X-XSRF-TOKEN: pE2x_SlJJXaTRZnVy1aVsC1c1fi1-FV_U05xsiJc6qGWLZRIlHyAzU8rEEC-d_zh-nuh0hw_-JnTnGNSMX1AghNqjMCgH6J_

email=user@example.com&password=password123
```

---

# 9. Successful Login

If authentication succeeds, Spring Security creates an authenticated session.

The response will contain a session cookie similar to:

```
Set-Cookie: JSESSIONID=<session-id>
```

The API client must preserve this cookie.

This session cookie is what identifies the authenticated user on subsequent requests.

The developer does **not** manually create the `JSESSIONID`.

Spring Security creates and manages it automatically.

---

# 10. Testing Authenticated Endpoints

After successful login, the API client should preserve the `JSESSIONID` cookie.

Subsequent authenticated requests will use that session.

For example, if an endpoint requires an authenticated user:

```
GET /api/user
```

the request should be made using the same API client/session that contains the authenticated user's `JSESSIONID`.

For a simple `GET` request, CSRF is normally not required because it does not change server-side state.

The important distinction is:

```
Authentication → JSESSIONID
CSRF protection → X-XSRF-TOKEN
```

They serve different purposes.

---

# 11. Testing POST, PUT, PATCH and DELETE Endpoints

After logging in, developers will commonly test endpoints that modify data.

Examples include:

```
POST /api/...
PUT /api/...
PATCH /api/...
DELETE /api/...
```

These requests require a valid CSRF token.

The general workflow is:

```
Authenticated session
        +
Valid CSRF token
        ↓
State-changing request
```

The request must contain:

```
X-XSRF-TOKEN: <valid-csrf-token>
```

while the API client also preserves:

```
JSESSIONID=<authenticated-session>
```

The developer therefore needs both pieces:

```
JSESSIONID
X-XSRF-TOKEN
```

The `JSESSIONID` identifies the authenticated user.

The `X-XSRF-TOKEN` header supplies the CSRF token that Spring Security validates.

---

# 12. GET Requests

A normal `GET` request does not require a CSRF token.

For example:

```
GET /api/user
```

does not require the `X-XSRF-TOKEN` header merely because it is a secured endpoint.

However, it may still require authentication.

Therefore:

```
GET secured endpoint
        ↓
JSESSIONID required
        ↓
CSRF token normally not required
```

For example:
GET http://localhost:8080/api/user

No CSRF header is required for this GET request. 
The authenticated session is maintained automatically through the JSESSIONID cookie.
Do not manually copy the JSESSIONID cookie between requests. 
IntelliJ's HTTP Client, Postman, and Apidog maintain the session cookie automatically when their cookie handling/session persistence is enabled. 
As long as cookies are enabled/preserved, authenticated requests will automatically use the session established during login.

---

# 13. Role-Protected Endpoints

Some endpoints require particular roles.

The application currently uses roles including:

* `USER`
* `ADMIN`
* `SUPER_ADMIN`

For example, an endpoint may be restricted to administrators.

In that situation, three things must be correct:

1. The user must exist.
2. The user must be authenticated.
3. The user's role must be authorized to access the endpoint.

A valid session alone does not grant access to every endpoint.

For example:

```
USER
  ↓
Authenticated
  ↓
Attempts ADMIN endpoint
  ↓
403 Forbidden
```

This is expected behavior.

The user must have the appropriate role.

---

# 14. The Difference Between 401 and 403

Understanding these responses is important when testing.

## 401 Unauthorized

In the current application, `401 Unauthorized` indicates that the request does not have a valid authenticated user/session.

Possible causes include:

* The user has not logged in.
* The `JSESSIONID` is missing.
* The session has expired.

For example:

```
GET /api/user
```

without an authenticated session may result in an authentication failure.

---

## 403 Forbidden

A `403` response commonly means the request reached the security system but was rejected.

With this application's configuration, common causes include:

* Missing CSRF token.
* Invalid CSRF token.
* Expired/invalid CSRF token.
* Insufficient role/authority.
* Attempting to access a protected resource without the required authorization.

For example:

```
POST /api/something
```

without:

```
X-XSRF-TOKEN: <valid-token>
```

can result in:

```
403 Forbidden
```


## Accessing an Endpoint Without the Required Role

A user can be successfully authenticated and still be denied access to an endpoint if their assigned role does not have permission to access it.

For example, if an endpoint is restricted to `ADMIN` users and a user with the `USER` role attempts to access it:

```
GET /api/admin
```

Spring Security will reject the request with:

```
HTTP/1.1 403 Forbidden
```

This means:

> **The user is authenticated, but does not have sufficient authority to access this endpoint.**

This is different from a `401 Unauthorized` response.

* **`401 Unauthorized`** → the request does not have a valid authenticated user/session.
* **`403 Forbidden`** → the user is authenticated, but their role/authority does not permit access to the requested endpoint.
* **`403 Forbidden` on a state-changing request** can also mean that the CSRF token is missing, invalid, or otherwise rejected.

For example:

```
USER
  ↓
Successfully logged in
  ↓
JSESSIONID automatically maintained by the API client
  ↓
GET /api/admin
  ↓
Spring Security checks the user's role
  ↓
USER does not have ADMIN authority
  ↓
403 Forbidden
```

Therefore, if a developer receives `403 Forbidden` from a secured endpoint, they should check **both**:

1. Whether the request is a state-changing request that requires a valid CSRF token.
2. Whether the logged-in user's role is actually authorized to access that endpoint.

---

# 15. Common CSRF Error

If a developer sends:

```
POST /api/...
```

without a CSRF token, the request can be rejected before the controller executes.

The developer may see:

HTTP/1.1 403

In this situation, first check:

1. Did you request a CSRF token?
2. Did the server return an `XSRF-TOKEN` cookie?
3. Did your API client preserve that cookie?
4. Did you copy the correct token value?
5. Did you send it using the exact header:

```
POST http://localhost:8080/auth/register
Content-Type: application/json
X-XSRF-TOKEN: <valid-csrf-token>
{
    "email": "user@example.com",
    "password": "password123"
}
```

6. Is the token still valid?
7. Are you using the token belonging to the current session?

---

# 16. Common Authentication Error

If a developer attempts to access a secured endpoint without logging in, authentication has not been established.

The workflow should be:

```
Register
   ↓
Login
   ↓
Receive JSESSIONID
   ↓
Call authenticated endpoint
```

Do not expect:

```
Register
   ↓
Immediately call secured endpoint
```

to work unless the application explicitly authenticates the user during registration.

Registration and authentication are separate operations.

---

# 17. Common Login Error

If the login fails, verify all the following:

### Credentials

Make sure the email and password belong to a registered user.

### Content type

Login uses:

```
application/x-www-form-urlencoded
```

e.g 

```
POST http://localhost:8080/login
Content-Type: application/x-www-form-urlencoded
X-XSRF-TOKEN: <current-csrf-token>

email=user@example.com&password=password123
```

not JSON.

### Parameter names

Use:

```
email
password
```

not:

```
username
password
```

### CSRF

Include:

```
X-XSRF-TOKEN: <valid-csrf-token>

e.g
X-XSRF-TOKEN: pE2x_SlJJXaTRZnVy1aVsC1c1fi1-FV_U05xsiJc6qGWLZRIlHyAzU8rEEC-d_zh-nuh0hw_-JnTnGNSMX1AghNqjMCgH6J_
```

### Session

Make sure the API client preserves the returned `JSESSIONID` after successful authentication.

---

# 18. Do Not Confuse the CSRF Token With the Session

The application uses two different cookies/tokens for two different purposes.

## `XSRF-TOKEN`

This is related to CSRF protection.

It is used to supply:

```
X-XSRF-TOKEN
```

for state-changing requests.

## `JSESSIONID`

Identifies the HTTP session. 
The session contains the authenticated security context when authentication has been established.
It is used so Spring Security knows which user is making the request.

Therefore, a developer should not treat them as interchangeable.

Developers do not need to manually copy or add the JSESSIONID cookie to requests. 
API testing clients such as IntelliJ HTTP Client, Postman, and Apidog automatically preserve cookies received from the server and send the appropriate JSESSIONID with subsequent requests to the same application.

A request to a secured `POST` endpoint may require both:

```
JSESSIONID
X-XSRF-TOKEN
```

---

# 19. Reusing a CSRF Token

A new CSRF token does not need to be generated before every `POST`, `PUT`, `PATCH`, or `DELETE`.

For example:

```
Obtain CSRF token
       ↓
POST request
       ↓
PUT request
       ↓
PATCH request
       ↓
DELETE request
```

The same valid token can be used for multiple requests.

Reuse the current CSRF token for subsequent state-changing requests. 
If the token is rejected, obtain the current token again from `GET http://localhost:8080/csrf` and retry.
Therefore, developers should not unnecessarily generate a new token before every API request.

---

# 20. Recommended Testing Workflow

For a developer testing an API from scratch, use this sequence:

```
1. Start the backend
        ↓
2. Request a CSRF token
        ↓
3. Preserve the XSRF-TOKEN cookie
        ↓
4. Test public GET endpoints
        ↓
5. Register a test user
        ↓
6. Include X-XSRF-TOKEN in registration request
        ↓
7. Confirm the user exists in the database
        ↓
8. Use the current CSRF token for login
        ↓
9. POST /login using form-urlencoded data
        ↓
10. Preserve JSESSIONID
        ↓
11. Test authenticated GET endpoints
        ↓
12. Test role-protected endpoints
        ↓
13. Include X-XSRF-TOKEN on POST/PUT/PATCH/DELETE requests
        ↓
14. Verify authorization and endpoint behavior
```

---

# 21. Quick Reference

| Request                   | Authentication | CSRF        |
| ------------------------- |----------------| ----------- |
| `GET /`                   | No             | No          |
| `POST /auth/register`     | No             | Yes         |
| `POST /login`             | No             | Yes         |
| `GET` secured endpoint    | Yes            | Normally no |
| `POST` secured endpoint   | Yes            | Yes         |
| `PUT` secured endpoint    | Yes            | Yes         |
| `PATCH` secured endpoint  | Yes            | Yes         |
| `DELETE` secured endpoint | Yes            | Yes         |

The exact authorization requirements depend on the endpoint's security configuration.

---

# 22. Complete Example Flow

A developer joining the project should think about testing as follows:

```
                    ┌─────────────────┐
                    │ Start Backend   │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │ Request CSRF    │
                    │ Token           │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │ Receive         │
                    │ XSRF-TOKEN      │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │ Register User   │
                    │ POST /auth/...  │
                    │ + CSRF Header   │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │ User saved      │
                    │ in database     │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │ Login           │
                    │ POST /login     │
                    │ + CSRF Header   │
                    └────────┬────────┘
                             ↓
                    ┌─────────────────┐
                    │ JSESSIONID      │
                    │ created         │
                    └────────┬────────┘
                             ↓
                 ┌───────────┴───────────┐
                 ↓                       ↓
          GET secured API         POST/PUT/PATCH/
          + JSESSIONID            DELETE secured API
                                  + JSESSIONID
                                  + X-XSRF-TOKEN
```

---

# ## 23. If You Only Want to Test Your Own API Without Going Through the Security Workflow

Spring Security should normally remain enabled when testing APIs. 
However, if you temporarily want to focus only on testing your own endpoint without going through the registration, login, CSRF, and authorization workflow, you can disable the application's security configuration for development.

In `application-local.properties`, set:

```
app.security.enabled=false
```

Restart the application after making the change. This allows you to test your endpoint without going through the normal Spring Security workflow.

Once you have finished testing your endpoint, change the property back to:

```
app.security.enabled=true
```

Restart the application and test your endpoint again with the normal security workflow before considering the work complete.

This is intended only as a temporary local-development convenience and `app.security.enabled=false` should not be committed as the normal project configuration.

---

# 24. Summary

When Spring Security is enabled, a developer should remember the following:

```
Public endpoint
    ↓
May not require authentication
    ↓
But CSRF can still apply to state-changing requests

Login
    ↓
Requires valid CSRF
    ↓
Creates authenticated JSESSIONID

Authenticated endpoint
    ↓
Requires JSESSIONID

State-changing authenticated endpoint
    ↓
Requires JSESSIONID
    +
Valid X-XSRF-TOKEN
```

If an endpoint suddenly returns `403 Forbidden`, **do not immediately assume the endpoint implementation is broken**.

First, check whether the request contains the required CSRF token and whether the authenticated user has the required role to perform the requested action.

If an endpoint returns `401 Unauthorized`, first check whether the request has a valid authenticated session and whether login succeeded.

Following this workflow allows backend developers to test their APIs normally while keeping the application's Spring Security protections enabled.

---

## Swagger UI and CSRF (developer workflow)

Swagger UI is available at `/swagger-ui.html` without authentication.

Interactive **Try it out** against protected APIs still requires:

1. `GET /csrf` — establishes the `XSRF-TOKEN` cookie (Swagger helper does this on load).
2. `POST /login` — form fields `email`, `password` (session cookie `JSESSIONID`).
3. Mutating calls must send header `X-XSRF-TOKEN` matching the cookie value.

springdoc is configured with:

```properties
springdoc.swagger-ui.csrf.enabled=true
springdoc.swagger-ui.csrf.cookie-name=XSRF-TOKEN
springdoc.swagger-ui.csrf.header-name=X-XSRF-TOKEN
springdoc.swagger-ui.custom-js=/swagger-csrf-helper.js
```

See also [docs/api/README.md](../api/README.md).
