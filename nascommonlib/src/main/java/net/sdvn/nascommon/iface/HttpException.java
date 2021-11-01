package net.sdvn.nascommon.iface;

public class HttpException extends RuntimeException {

    private final int code;
    private final String message;

    public HttpException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * HTTP status code.
     */
    public int code() {
        return code;
    }

    /**
     * HTTP status message.
     */
    public String message() {
        return message;
    }

}
