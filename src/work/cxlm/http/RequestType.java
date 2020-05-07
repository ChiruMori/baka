package work.cxlm.http;

public enum RequestType {
    GET, POST, PUT, DELETE, ALL;

    public static RequestType getType(String method) {
        switch (method) {
            case "GET":
                return GET;
            case "POST":
                return POST;
            case "PUT":
                return PUT;
            case "DELETE":
                return DELETE;
            case "*":
            case "ALL":
                return ALL;
            default:
                return null;
        }
    }
}
