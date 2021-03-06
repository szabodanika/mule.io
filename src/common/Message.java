package common;

public class Message {

    private byte[] id, content;
    private Message next;
    private Connection connection;
    private Node node;
    private byte[] messageType = new byte[1];
    private byte[] requestId = new byte[0];

    public Message(Node node, Connection connection, byte[] id, byte[] content, Message next) {
        this.node = node;
        this.id = id;
        this.content = content;
        this.next = next;
        this.connection = connection;
        messageType[0] = 0;
    }

    public boolean isRequest(){
        if(messageType[0] == 1) return true;
        return false;
    }

    public boolean isResult(){
        if(messageType[0] == 2) return true;
        return false;
    }

    public void markAsRequest(byte[] id){
        messageType[0] = 1;
        requestId = id;
    }

    public void markAsResult(byte[] id){
        messageType[0] = 2;
        requestId = id;
    }

    public enum encryptionType{
        RAW(0),
        AES(1),
        RSA(2);

        public int value;

        encryptionType(int i) {
            this.value = i;
        }
    }

    public void enqueueMessage(Message message){
        if(this.next == null) {
            this.next = message;
        } else {
            this.next.enqueueMessage(message);
        }
    }

    public boolean hasNext(){
        return next != null;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Message getNext() {
        return next;
    }

    public void setID(byte[] id) {
        this.id = id;
    }

    public byte[] getFinal(encryptionType encryptionType) throws Exception {

        byte[] finalMessage;

        byte[] id_encrypted = new byte[0], content_encrypted = new byte[0];

        switch (encryptionType) {
            case RAW:
                id_encrypted = id;
                content_encrypted = content;
                break;
            case AES:
                id_encrypted = connection.aes.encrypt(id);
                content_encrypted = connection.aes.encrypt(content);
                break;
            case RSA:
                id_encrypted = node.rsa.encrypt(id);
                content_encrypted = node.rsa.encrypt(content);
                break;
        }

        finalMessage = new byte[Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE + (messageType[0] == 0 ? 0 : Config.ARG_REQUEST_ID_LENGTH) + Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_ID_LENGTH_BYTES + id_encrypted.length + content_encrypted.length];

        byte[] finalMessageLength = ByteUtils.intToBytes(finalMessage.length, Config.ARG_LENGTH_BYTES);

        byte[] idlength = ByteUtils.intToBytes(id_encrypted.length, 4);

        int checkSum = ByteUtils.checkSum(id_encrypted, content_encrypted);
        byte[] checkSumBytes = ByteUtils.intToBytes(checkSum, Config.ARG_CHECKSUM_BYTES);

        finalMessage[0] = (byte) encryptionType.value;

        for (int i = 0; i < finalMessageLength.length; i++){
            finalMessage[i + Config.ARG_ENCRYPTION] = finalMessageLength[i];
        }

        for (int i = 0; i < messageType.length; i++){
            finalMessage[i + Config.ARG_ENCRYPTION + Config.ARG_LENGTH_BYTES] = messageType[i];
        }

        if(messageType[0] == 0) {

            for (int i = 0; i < checkSumBytes.length; i++) {
                finalMessage[i + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE + Config.ARG_LENGTH_BYTES] = checkSumBytes[i];
            }
            for (int i = 0; i < idlength.length; i++) {
                finalMessage[i + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE + Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES] = idlength[i];
            }
            for (int i = 0; i < id_encrypted.length; i++) {
                finalMessage[i + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE + Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_ID_LENGTH_BYTES] = id_encrypted[i];
            }
            for (int i = 0; i < content_encrypted.length; i++) {
                finalMessage[i + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE + Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_ID_LENGTH_BYTES + id_encrypted.length] = content_encrypted[i];
            }

        } else if(messageType[0] == 1 || messageType[0] == 2){

            for (int i = 0; i < requestId.length; i++) {
                finalMessage[i + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE + Config.ARG_LENGTH_BYTES] = requestId[i];
            }
            for (int i = 0; i < checkSumBytes.length; i++) {
                finalMessage[i + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE + Config.ARG_LENGTH_BYTES + Config.ARG_REQUEST_ID_LENGTH] = checkSumBytes[i];
            }
            for (int i = 0; i < idlength.length; i++) {
                finalMessage[i + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE + Config.ARG_LENGTH_BYTES + Config.ARG_REQUEST_ID_LENGTH + Config.ARG_CHECKSUM_BYTES] = idlength[i];
            }
            for (int i = 0; i < id_encrypted.length; i++) {
                finalMessage[i + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE + Config.ARG_LENGTH_BYTES + Config.ARG_REQUEST_ID_LENGTH + Config.ARG_CHECKSUM_BYTES + Config.ARG_ID_LENGTH_BYTES] = id_encrypted[i];
            }
            for (int i = 0; i < content_encrypted.length; i++) {
                finalMessage[i + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE + Config.ARG_LENGTH_BYTES + Config.ARG_REQUEST_ID_LENGTH + Config.ARG_CHECKSUM_BYTES + Config.ARG_ID_LENGTH_BYTES + id_encrypted.length] = content_encrypted[i];
            }

        }

        return finalMessage;
    }

    public byte[] getID() {
        return id;
    }

    public int IDCS(){
        return ByteUtils.checkSum(id);
    }

    public byte[] getRequestId(){
        return requestId;
    }

    public byte[] getContent() {
        return content;
    }

    public void sent(){
        node.onSent(connection, id, content);
    }

    public Message withoutNext() {
        return new Message(node,connection,id,content,null);
    }
}
