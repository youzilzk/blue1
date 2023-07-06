package com.youzi.blue.net.client.work;

import android.content.res.AssetManager;

import com.youzi.blue.net.client.manager.Manager;
import com.youzi.blue.net.common.utils.LoggerFactory;
import com.youzi.blue.utils.OkHttp;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
        final String jksPath = "test.bks";
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
        URL url = new URL("http://192.168.31.208:8008/file/bks");
        InputStream inputStream = url.openStream();
        final String keyStorePassword = "123456";
        final KeyStore ks = KeyStore.getInstance("BKS");
        ks.load(inputStream, keyStorePassword.toCharArray());
        inputStream.close();
        keyStore = ks;
    }

}