package work.cxlm.http;

public enum RequestType {
    GET, POST, PUT, DELETE;

    public static RequestType getType(String method) {
        return switch (method) {
            case "GET" -> GET;
            case "POST" -> POST;
            case "PUT" -> PUT;
            case "DELETE" -> DELETE;
            default -> null;
        };
    }
}
