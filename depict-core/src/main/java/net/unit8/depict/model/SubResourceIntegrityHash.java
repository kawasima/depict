package net.unit8.depict.model;

import org.codehaus.plexus.util.Base64;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SubResourceIntegrityHash {
    private static final int BUFFER_SIZE = 2048;
    private Base64 base64 = new Base64();

    public String sha384(InputStream is) {
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream bis = new BufferedInputStream(is);
        try {
            MessageDigest digester = MessageDigest.getInstance("sha-384");
            while(true) {
                int read = bis.read(buffer, 0, BUFFER_SIZE);
                if (read <= 0) break;
                digester.update(buffer, 0, BUFFER_SIZE);
            }
            return "sha384-" + new String(base64.encode(digester.digest()), "ISO-8859-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("This JDK does not supports SHA-384", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("InputStream is wrong", e);
        }
    }
}
