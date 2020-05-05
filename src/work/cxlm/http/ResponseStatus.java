package work.cxlm.http;

// 常用 HTTP 响应状态码，如不够可自行扩展
public enum ResponseStatus {
    OK(200, "OK"),
    FOUND(302, "Redirect"),
    BAD_REQUEST(400, "Bad Request"), FORBIDDEN(403, "Forbidden"), NOT_FOUND(404, "Not Found"),
    INTERNAL_ERROR(500, "Internal Server Error"), BUSY(503, "Service Unavailable");

    int statusCode;
    String info;

    ResponseStatus(int statusCode, String info) {
        this.statusCode = statusCode;
        this.info = info;
    }

    public String getMsg() {
        return statusCode + " " + info;
    }
}
