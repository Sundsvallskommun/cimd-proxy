package se.sundsvall.cimdproxy.cimd.util;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import se.sundsvall.cimdproxy.cimd.exception.CIMDException;

public final class SslUtil {

	private SslUtil() {}

	public static PrivateKey getPrivateKey(final String keyStoreType, final String keyStoreAlias, final byte[] keyStoreData, final String keyStorePassword) {
		try {
			var passphrase = keyStorePassword.toCharArray();
			var keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(new ByteArrayInputStream(keyStoreData), passphrase);

			return (PrivateKey) keyStore.getKey(keyStoreAlias, passphrase);
		} catch (Exception e) {
			throw new CIMDException("Exception when obtaining private key", e);
		}
	}

	public static Certificate getCertificate(final String keyStoreType, final String keyStoreAlias, final byte[] keyStoreData, final String keyStorePassword) {
		try {
			var passphrase = keyStorePassword.toCharArray();
			var keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(new ByteArrayInputStream(keyStoreData), passphrase);

			return keyStore.getCertificate(keyStoreAlias);
		} catch (Exception e) {
			throw new CIMDException("Exception when obtaining certificate", e);
		}
	}
}
