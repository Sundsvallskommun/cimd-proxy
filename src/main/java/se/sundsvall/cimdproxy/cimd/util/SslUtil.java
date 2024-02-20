package se.sundsvall.cimdproxy.cimd.util;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import se.sundsvall.cimdproxy.cimd.exception.CIMDException;

public final class SslUtil {

    private static KeyStore keyStore;

    private SslUtil() { }

    public static PrivateKey getPrivateKey(final String alias, final byte[] key, final String password) {
        try {
            var passphrase = password.toCharArray();
            var keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new ByteArrayInputStream(key), passphrase);

            return (PrivateKey) keyStore.getKey(alias, passphrase);
        } catch (Exception e) {
            throw new CIMDException("Exception when getting private key", e);
        }
    }

    public static Certificate getCertificate(final String alias, final byte[] key, final String password) {
        try {
            var passphrase = password.toCharArray();
            var keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new ByteArrayInputStream(key), passphrase);

            return keyStore.getCertificate(alias);
        } catch (Exception e) {
            throw new CIMDException("Exception when getting certificate", e);
        }
    }
}
