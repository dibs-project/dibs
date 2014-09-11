package dosv;

import de.huberlin.cms.hub.HubException;

@SuppressWarnings("serial")
public class DosvAuthenticationException extends HubException {
    private String message;

    public DosvAuthenticationException(String code, String message) {
        super(code);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
