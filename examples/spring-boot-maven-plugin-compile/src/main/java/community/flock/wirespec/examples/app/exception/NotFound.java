package community.flock.wirespec.examples.app.exception;

public sealed class NotFound extends AppException {
    public NotFound(String message) {
        super(message);
    }

    public static final class User extends NotFound {

        public User() {
            super("User not found");
        }
    }
}
