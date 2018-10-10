package common;

public class Config {

    //These values have to match for the server and client.
    //Eventually the server will communicate these for the client at the beginning of every connection.

    public static final int ARG_ENCRYPTION = 1;
    public static final int ARG_LENGTH_BYTES = 4;
    public static final int ARG_MESSAGE_TYPE = 1;
    public static final int ARG_REQUEST_ID_LENGTH = 8;
    public static final int ARG_CHECKSUM_BYTES = 8;
    public static final int ARG_ID_LENGTH_BYTES = 4;
    public static final int REQUEST_TIMEOUT = 250; //MILLISECONDS

}
