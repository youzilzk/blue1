package com.youzi.blue.network.client.work;

import com.youzi.blue.utils.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class SslContextCreator {
    private static final LoggerFactory log = LoggerFactory.getLogger();
    private static SSLContext sslContext;
    private static KeyStore keyStore;

    public static SSLContext createSSLContext() {
        return initSSLContext();
    }

    public static SSLContext getSSLContext() {
        if (sslContext == null) {
            sslContext = initSSLContext();
        }
        return sslContext;
    }

    private static SSLContext initSSLContext() {
        log.info("Checking SSL configuration properties...");
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            if (keyStore == null) {
                initKeyStore();
            }
            tmf.init(keyStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            log.info("Initializing SSL context...");

            SSLContext clientSSLContext = SSLContext.getInstance("TLS");
            clientSSLContext.init(null, trustManagers, null);
            log.info("The SSL context has been initialized successfully.");

            return clientSSLContext;
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException |
                 CertificateException | IOException ex) {
            log.info("Unable to initialize SSL context. Cause = {}, errorMessage = {}.", ex.getCause(), ex.getMessage());
            return null;
        }
    }

    private static void initKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        log.info("read {}  and Initializing KeyStore...", "test.bks");
        URL url = new URL("http://61.243.3.19:5000/file/bks");
        InputStream inputStream = url.openStream();
        final String keyStorePassword = "123456";
        final KeyStore ks = KeyStore.getInstance("BKS");
        ks.load(inputStream, keyStorePassword.toCharArray());
        inputStream.close();
        keyStore = ks;
    }

}