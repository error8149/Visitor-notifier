package com.error.dhlvisitornotification;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

public class EmailSender {
    private static final String TAG = "EmailSender";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    private static class ByteArrayDataSource implements DataSource {
        private final byte[] data;
        private final String contentType;
        private final String name;

        ByteArrayDataSource(byte[] data, String contentType, String name) {
            this.data = data;
            this.contentType = contentType;
            this.name = name;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("OutputStream not supported");
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public static boolean sendVisitorEmail(Context context, Visitor visitor, Bitmap signatureBitmap) {
        if (!isNetworkAvailable(context)) {
            Log.e(TAG, "No network connection available");
            return false;
        }

        String serverIP = getServerIP(context);
        Log.d(TAG, "Attempting to send email via James server at: " + serverIP + ":25");

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Properties props = createEmailProperties(serverIP);
                Session session = createEmailSession(props);

                MimeMessage message = createEmailMessage(session, visitor, signatureBitmap);

                Log.d(TAG, "Sending email (attempt " + attempt + "/" + MAX_RETRIES + ")…");
                Transport.send(message);
                Log.d(TAG, "✅ Email sent successfully on attempt " + attempt);
                return true;

            } catch (Exception e) {
                Log.w(TAG, "❌ Email attempt " + attempt + " failed: " + e.getMessage());

                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    Log.e(TAG, "Failed to send email after " + MAX_RETRIES + " attempts", e);
                }
            }
        }
        return false;
    }

    private static String getServerIP(Context context) {
        // Try to detect if running on emulator
        String fingerprint = android.os.Build.FINGERPRINT;
        if (fingerprint.contains("generic") || fingerprint.contains("emulator")) {
            Log.d(TAG, "Running on emulator, using 10.0.2.2");
            return "10.0.2.2";
        } else {
            // For physical device, try to get gateway IP
            String gatewayIP = getGatewayIP(context);
            if (gatewayIP != null) {
                Log.d(TAG, "Running on physical device, using gateway IP: " + gatewayIP);
                return gatewayIP;
            } else {
                Log.d(TAG, "Running on physical device, using default IP");
                return "192.168.1.1"; // Common router IP - you may need to change this
            }
        }
    }

    private static String getGatewayIP(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                int gatewayAddr = wifiManager.getDhcpInfo().gateway;
                return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                        (gatewayAddr & 0xff),
                        (gatewayAddr >> 8 & 0xff),
                        (gatewayAddr >> 16 & 0xff),
                        (gatewayAddr >> 24 & 0xff));
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get gateway IP: " + e.getMessage());
        }
        return null;
    }

    private static Properties createEmailProperties(String serverIP) {
        Properties props = new Properties();
        props.put("mail.smtp.host", serverIP);
        props.put("mail.smtp.port", "25");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.ssl.enable", "false");
        props.put("mail.debug", "false");
        props.put("mail.smtp.connectiontimeout", "30000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.writetimeout", "30000");
        props.put("mail.smtp.localhost", "android-client");
        props.put("mail.smtp.ehlo", "false");
        return props;
    }

    private static Session createEmailSession(Properties props) {
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                Log.d(TAG, "Authenticating with user: " + BuildConfig.EMAIL_SENDER);
                return new PasswordAuthentication(BuildConfig.EMAIL_SENDER, BuildConfig.EMAIL_APP_PASSWORD);
            }
        });
    }

    private static MimeMessage createEmailMessage(Session session, Visitor visitor, Bitmap signatureBitmap)
            throws MessagingException, IOException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(BuildConfig.EMAIL_SENDER));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(BuildConfig.OWNER_EMAIL));
        message.setSubject("🚚 DHL Visitor Notification: " + visitor.getFullName());

        // Create HTML email body
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(createHtmlEmailBody(visitor), "text/html; charset=utf-8");

        // Create signature attachment
        MimeBodyPart attachmentPart = createSignatureAttachment(signatureBitmap);

        // Combine parts
        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);
        multipart.addBodyPart(attachmentPart);
        message.setContent(multipart);

        return message;
    }

    private static String createHtmlEmailBody(Visitor visitor) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        String timestamp = dateFormat.format(new Date());

        return String.format(
                "<!DOCTYPE html>" +
                        "<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f8f9fa;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>" +

                        "<!-- Header -->" +
                        "<div style='background: linear-gradient(135deg, #D40511 0%%, #FF6B00 100%%); color: white; padding: 30px; text-align: center;'>" +
                        "<h1 style='margin: 0; font-size: 28px; font-weight: bold;'>🚚 DHL Visitor Notification</h1>" +
                        "<p style='margin: 10px 0 0 0; opacity: 0.9; font-size: 16px;'>New visitor registration received</p>" +
                        "</div>" +

                        "<!-- Content -->" +
                        "<div style='padding: 40px 30px;'>" +
                        "<h2 style='color: #D40511; margin-top: 0; font-size: 24px; border-bottom: 2px solid #FFCC00; padding-bottom: 10px;'>Visitor Information</h2>" +

                        "<div style='background-color: #f8f9fa; border-radius: 8px; padding: 25px; margin: 20px 0;'>" +
                        "<table style='width: 100%%; border-collapse: collapse;'>" +
                        "<tr style='border-bottom: 1px solid #e9ecef;'><td style='padding: 12px 0; font-weight: bold; color: #495057; width: 120px;'>👤 Name:</td><td style='padding: 12px 0; color: #212529;'>%s</td></tr>" +
                        "<tr style='border-bottom: 1px solid #e9ecef;'><td style='padding: 12px 0; font-weight: bold; color: #495057;'>🏢 Company:</td><td style='padding: 12px 0; color: #212529;'>%s</td></tr>" +
                        "<tr style='border-bottom: 1px solid #e9ecef;'><td style='padding: 12px 0; font-weight: bold; color: #495057;'>📞 Phone:</td><td style='padding: 12px 0; color: #212529;'>%s</td></tr>" +
                        "<tr style='border-bottom: 1px solid #e9ecef;'><td style='padding: 12px 0; font-weight: bold; color: #495057;'>📧 Email:</td><td style='padding: 12px 0; color: #212529;'>%s</td></tr>" +
                        "<tr><td style='padding: 12px 0; font-weight: bold; color: #495057;'>📝 Purpose:</td><td style='padding: 12px 0; color: #212529;'>%s</td></tr>" +
                        "</table>" +
                        "</div>" +

                        "<div style='background-color: #e7f3ff; border-left: 4px solid #007bff; padding: 20px; margin: 25px 0; border-radius: 5px;'>" +
                        "<p style='margin: 0; color: #004085;'><strong>⏰ Registration Time:</strong> %s</p>" +
                        "<p style='margin: 10px 0 0 0; color: #004085;'><strong>✍️ Digital Signature:</strong> Please see attached image</p>" +
                        "</div>" +

                        "</div>" +

                        "<!-- Footer -->" +
                        "<div style='background-color: #f8f9fa; padding: 20px 30px; border-top: 1px solid #dee2e6; text-align: center;'>" +
                        "<p style='margin: 0; color: #6c757d; font-size: 14px;'>This notification was generated automatically by the DHL Visitor Management System</p>" +
                        "<div style='margin-top: 15px;'>" +
                        "<span style='background-color: #D40511; color: white; padding: 8px 16px; border-radius: 20px; font-size: 12px; font-weight: bold;'>DHL EXPRESS</span>" +
                        "</div>" +
                        "</div>" +

                        "</div></body></html>",
                visitor.getFullName(),
                visitor.getCompany() != null ? visitor.getCompany() : "Not specified",
                visitor.getPhone(),
                visitor.getEmail(),
                visitor.getReason(),
                timestamp
        );
    }

    private static MimeBodyPart createSignatureAttachment(Bitmap signatureBitmap) throws MessagingException {
        MimeBodyPart attachmentPart = new MimeBodyPart();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] signatureBytes = baos.toByteArray();

        DataSource source = new ByteArrayDataSource(signatureBytes, "image/png", "visitor_signature.png");
        attachmentPart.setDataHandler(new DataHandler(source));
        attachmentPart.setFileName("visitor_signature.png");
        attachmentPart.setDescription("Visitor Digital Signature");

        return attachmentPart;
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } else {
            // For older devices (API < 23)
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        }
    }
}