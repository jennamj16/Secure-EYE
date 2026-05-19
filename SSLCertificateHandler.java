package com.example.motioneyeapp.utils;

import android.annotation.SuppressLint;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Utility class for handling SSL certificates
 * This class provides methods to trust self-signed certificates for local development
 */
public class SSLCertificateHandler {

    /**
     * Creates a trust manager that trusts all certificates
     * WARNING: Use only for development with self-signed certificates
     */
    public static TrustManager[] getTrustAllCertificates() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @SuppressLint("TrustAllX509TrustManager")
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                        // Trust all client certificates
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                        // Trust all server certificates
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }

    /**
     * Creates a hostname verifier that accepts all hostnames
     * WARNING: Use only for development with self-signed certificates
     */
    public static HostnameVerifier getTrustAllHostnameVerifier() {
        return new HostnameVerifier() {
            @SuppressLint("BadHostnameVerifier")
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }

    /**
     * Configures SSL context to trust all certificates
     * @return SSLContext configured to trust all certificates
     */
    public static SSLContext getTrustAllSSLContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, getTrustAllCertificates(), new java.security.SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }

    /**
     * Checks if a hostname is a local/private address
     * @param hostname The hostname to check
     * @return true if the hostname is local, false otherwise
     */
    public static boolean isLocalHost(String hostname) {
        if (hostname == null) return false;
        
        return hostname.equals("localhost") ||
                hostname.equals("rpi.local") ||
                hostname.startsWith("192.168.") ||
                hostname.startsWith("10.") ||
                hostname.startsWith("172.16.") ||
                hostname.equals("127.0.0.1");
    }
}
