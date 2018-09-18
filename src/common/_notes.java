package common;

public class _notes {

    /** handshake structure
     *      1. client connects server
     *      2. client sends random aes key to server
     *      3. server sends aes encoded public key to client
     */

    /** message byte structure
     *      1. encryption info                                                 >>      1 byte
     *      2. content length (encrypted)                                      >>      4 bytes
     *      3. checksum of id and content (encrypted)                          >>      8 bytes
     *      4. id                                                              >>      16 bytes
     *      5. content                                                         >>      ? bytes
     */

}
