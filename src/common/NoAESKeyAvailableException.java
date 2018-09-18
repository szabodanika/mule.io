package common;

public class NoAESKeyAvailableException extends Exception {

    public NoAESKeyAvailableException(){

        super("There is no AES key available to encrypt the message with.");

    }

}
