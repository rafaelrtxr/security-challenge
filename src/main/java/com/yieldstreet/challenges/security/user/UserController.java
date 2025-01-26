package com.yieldstreet.challenges.security.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import com.yieldstreet.challenges.security.session.SessionDao;
import com.yieldstreet.challenges.security.session.SessionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserDao userDao;
    private final SessionDao sessionDao;
    private final SessionHelper sessionHelper;
    private final MessageDigest md;

    public UserController(UserDao userDao, SessionDao sessionDao, SessionHelper sessionHelper)
            throws NoSuchAlgorithmException {
        this.userDao = userDao;
        this.sessionDao = sessionDao;
        this.sessionHelper = sessionHelper;
        this.md = MessageDigest.getInstance("MD5");
    }

    @RequestMapping(method = RequestMethod.POST, value = "/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createUser(@RequestBody CreateUserForm form) {
        var id = UUID.randomUUID();
        var user = new User(id, form.email, form.name, hashedPassword(form.password));
        logger.info("Creating new user {}", user);

        userDao.insert(user);
        return id;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/users/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UUID> login(@RequestParam("email") String email, @RequestParam("password") String password) {
        //FIXME: Password on query string parameters (https://cwe.mitre.org/data/definitions/598)
        logger.info("Authenticating user {} with hashed password {}", email, password);
        var userId = userDao.authenticate(email, hashedPassword(password));

        if (userId == null) {
            logger.warn("failed to authenticate user {}", email);
            //FIXME: Exposure of stack trace in error message (https://cwe.mitre.org/data/definitions/209.html)
            throw new AuthenticationFailedException();
        }

        UUID sessionId = UUID.randomUUID();
        sessionDao.insert(sessionId.toString(), userId.toString());

        //FIXME: Sensitive cookie without HtppOnly flag (https://cwe.mitre.org/data/definitions/1004.html)
        //FIXME: Sensitive cookie without Secure flag (https://cwe.mitre.org/data/definitions/614.html)
        var cookie = ResponseCookie.from("session_id", sessionId.toString())
                .path("/")
                .maxAge(60 * 60)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                //TODO: evaluate the exposure of a database primary key
                .body(userId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/users/changepwd")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(@CookieValue("session_id") String sessionId, @RequestParam String password) {
        //FIXME: Insecure password change method, it should require the old password and a confirmation code
        var userId = sessionHelper.authenticate(sessionId);
        userDao.changePassword(userId.toString(), hashedPassword(password));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class AuthenticationFailedException extends RuntimeException {
        public AuthenticationFailedException() {
        }
    }

    private String hashedPassword(String password) {
        //FIXME: Weak hash (https://cwe.mitre.org/data/definitions/328.html)
        return Base64.getEncoder().encodeToString(md.digest(password.getBytes()));
    }

    public static class CreateUserForm {
        public String email;
        public String name;
        public String password;
    }
}
