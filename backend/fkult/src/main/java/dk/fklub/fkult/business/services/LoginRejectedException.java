package dk.fklub.fkult.business.services;

/**
 * Thrown when a login attempt must not result in a session, for example because
 * the member is banned or because the admin password was wrong.
 */
public class LoginRejectedException extends RuntimeException {

    private final String reason;

    public LoginRejectedException(String reason, String message) {
        super(message);
        this.reason = reason;
    }

    /** Short machine readable reason, used in the redirect back to the frontend. */
    public String getReason() {
        return reason;
    }
}
