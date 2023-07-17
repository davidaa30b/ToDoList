package todolist.messagesstatus;

public enum StatusMessages {
    WARNING("WARNING"),
    SUCCESS("SUCCESS"),
    ERROR("ERROR");

    private String status;
    StatusMessages(String message) {
        this.status = message;
    }

    public String getInString() {
        return status;
    }

    public  String getMessage( String  message) {
        return String.format("{\"status\": \"%s\" ,\"Message\":\"%s\"}", status, message);
    }
}
