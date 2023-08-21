package com.example.home.gohoy.k8s_backend.utils;

import com.example.home.gohoy.k8s_backend.entities.kubevirt.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClientException;
import jakarta.validation.constraints.NotNull;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

@Component
public class KubevirtUtil {


    private final String clientCertResource = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURJVENDQWdtZ0F3SUJBZ0lJU25HeEVwNXAwU2N3RFFZSktvWklodmNOQVFFTEJRQXdGVEVUTUJFR0ExVUUKQXhNS2EzVmlaWEp1WlhSbGN6QWVGdzB5TXpBNE1UVXhNVEF5TlRaYUZ3MHlOREE0TVRReE1UQXlOVGxhTURReApGekFWQmdOVkJBb1REbk41YzNSbGJUcHRZWE4wWlhKek1Sa3dGd1lEVlFRREV4QnJkV0psY201bGRHVnpMV0ZrCmJXbHVNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXk3SklzNlZ5KytLVUVXTDYKTk4va1B1c25FdjJUSTc1SVhVQkVzbHNWbzNnazRCakR0MHhZSTI1MGZJdDZ3NERKNWRiWStHc0I5cmhTd1F1KwpLcGd4MkdSd2ZKZWtnUjVtUERKdUJlSXlsUVY5QzF6NzVEUTVzblByOE9yaDdaT1RjRHUybEZCeTY1V3RjMi9SClZNaWhTSGV0MktGSWh6WnpqTWJvbThGdmpkdmtNOFZ3V1A4cUpBanhlKzF5RGF6UWgzVTlUN0hicmZZcm81ZUoKeDFzR0dFa3lvbEhVUlppS0xmR1dWMnMwaEh2dU9rRmQzb1hYbzRERkVSVFVzL2F4SnJiVDZOVTRjT1FDOFlDcwpjdjduRzZvKzNRa2ZYNnRrcTFxanN1eUZQOHNBOHpUVFg4aDlyYUpwaTcrRmdZRlA2N3IwaWNPZGdyYWZ2RDRmCmN3RXFYUUlEQVFBQm8xWXdWREFPQmdOVkhROEJBZjhFQkFNQ0JhQXdFd1lEVlIwbEJBd3dDZ1lJS3dZQkJRVUgKQXdJd0RBWURWUjBUQVFIL0JBSXdBREFmQmdOVkhTTUVHREFXZ0JRNnNQa0tKRnVHNUxVaWU2cWpDMDRodVROOQp3ekFOQmdrcWhraUc5dzBCQVFzRkFBT0NBUUVBVjVhTmtORlhIYThQUGU0TEk4WG4rR2JpR2FLcGpndlFlL0lwCmlydU1pYTJlaGpVTGoyZVRJOXRabEErWk5jTmdOWmFpZjBPZnkxZGF4SFBTdmhiUjNoWElsQnphQ0F1TUZnakMKMlM1K3lYdUFHWnNFRC9LWkZxSk9BT0FKNEZMRXBTUnB2c2tIcTYvQWJveWxyeE52bmRGRjkyR2NxNmZySDd6MwpUZmdSS0xwNWlXekdQaVNFWXF5V0lUeXFxOXBmamlzTXpCY2RaOW9Fb05kdEd5Qzgzdi84TmZ6aG92bGlLVmFJCk85WnovUGN2MWpZY25rRWx6cGliSkk5UUZQSHZwaTZlUDlWazF0RUt4QWMxamRwbFhvaXQ5WmZlODFQTzRnc1IKOXZDMmN5ZkV5b0VoN0dzSmR3V0w2QXJhd3Irak1iblFpQTFCWHZMU1BGU1N0VmlXTVE9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==";
    private String caCertResource = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUMvakNDQWVhZ0F3SUJBZ0lCQURBTkJna3Foa2lHOXcwQkFRc0ZBREFWTVJNd0VRWURWUVFERXdwcmRXSmwKY201bGRHVnpNQjRYRFRJek1EZ3hOVEV4TURJMU5sb1hEVE16TURneE1qRXhNREkxTmxvd0ZURVRNQkVHQTFVRQpBeE1LYTNWaVpYSnVaWFJsY3pDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBTFdvCnlkUWRTZThmTFU2di9OUTBDa3ZYZXhyNUhDN3dUQ21ielpiUjAyK0dONWNhbmk3bWNCbmE0bkZ4czBWTXlBRzIKL0RGZDMzU1JySXhRNnNUeUVLNlBIMHc2a0xPaHczQkZncVJCbGZMOEtYSHpXZXF4TGRlbDkrT1E4UDBQbzBiNQpSeVlJdFRDWGlxMU82bFMxM2ZxbG9wYXRSc3ppUHRmMU5aQUhUaVUyRHlzcHlUNTdzUDJRV2ZBOTdaNmg4RHUvClhFaUNMRjFKbU9uNmhrblY5eGdMT25hYm5HYkh4SkN1bXh6aHdENW1FcHRZdW9PSTZFeTBKTmNHRW82cEZHZGwKYUVnL3RLUUtPck5WVUpHUmoyVUV5czlDYlJmSFNRaHpLQ1FlZXV3OVUrVWN6TU1hNWc3YzMrWXF4TFBuSmI5cgpkZGdYMWlkNk9HalBTWXNaTXYwQ0F3RUFBYU5aTUZjd0RnWURWUjBQQVFIL0JBUURBZ0trTUE4R0ExVWRFd0VCCi93UUZNQU1CQWY4d0hRWURWUjBPQkJZRUZEcXcrUW9rVzRia3RTSjdxcU1MVGlHNU0zM0RNQlVHQTFVZEVRUU8KTUF5Q0NtdDFZbVZ5Ym1WMFpYTXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBS2xlMGhBZFR6eUNvZU8yT2tIRQp3bTZSM1FGNDh4YUd4VHk3bnlyL3JrRG0yUkFrUUltMmhLQXhnM0YxQXBsT0NlQnhxMEJPSk5ydWVYc2d4cU5NCi95NHVXdHdIV0lrTnRaMVVnTS84UFMwZTVGWmw2SUt1cWk4YmdRcjQycHJXdzdWbVgydi9mMmFUYUJvTytLV2IKNFdNckpLaThCWUlKZHV4VkoxSnZmaFhCdk10Uzc4UXdyRXpQL3NXdFM0QkRId25JOTdQV2c2WElzVmdVZENuMwpYaHpVUU1NVzBxUnBZcnI2U095OEpRTUJucjdITGN3RXd0cXJvZzRDY1ZiRFlyaWlIL0VMMWZQRE81V2dVUlIxCkJkOHVndUZ1dXd4MjBNQTlXQ0NoMjloNjY2ai8rdlBubGZaTjVuTGRTTmQ3eDEvZEVsV1F4empwMWhKL21oZVcKZS8wPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==";
private TrustManager[] trustManagers;

    public SSLContext preLoad() throws Exception {
//        System.out.println(caCertResource);
//        System.out.println(clientCertResource);
        String clientKeyResource = "LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFcFFJQkFBS0NBUUVBeTdKSXM2VnkrK0tVRVdMNk5OL2tQdXNuRXYyVEk3NUlYVUJFc2xzVm8zZ2s0QmpECnQweFlJMjUwZkl0Nnc0REo1ZGJZK0dzQjlyaFN3UXUrS3BneDJHUndmSmVrZ1I1bVBESnVCZUl5bFFWOUMxejcKNURRNXNuUHI4T3JoN1pPVGNEdTJsRkJ5NjVXdGMyL1JWTWloU0hldDJLRkloelp6ak1ib204RnZqZHZrTThWdwpXUDhxSkFqeGUrMXlEYXpRaDNVOVQ3SGJyZllybzVlSngxc0dHRWt5b2xIVVJaaUtMZkdXVjJzMGhIdnVPa0ZkCjNvWFhvNERGRVJUVXMvYXhKcmJUNk5VNGNPUUM4WUNzY3Y3bkc2byszUWtmWDZ0a3ExcWpzdXlGUDhzQTh6VFQKWDhoOXJhSnBpNytGZ1lGUDY3cjBpY09kZ3JhZnZENGZjd0VxWFFJREFRQUJBb0lCQVFERXZsckNTWmVaK3VzUgpNQXlEYlkxRHlGVmhYbEwwUHlJZUQ2bEtxL2RtZ1BEOUVtNVhhUUhvbHhNa0NJUjJ1THBKcVE3QVFuc1BGbEczCjYrT3R1SHJNaTVhcWdMMS9pU0V1RzNkeGIxMXoybTFBd0hyN25BWHVlMUQwemFtU2t1dmhoNHQrRFlZeVRjdk8KcDc0RXFoUTlybnJYdGxBYmFWaUtMTVVYUytxM2xHWUtqMEVxMmEvSWNaU1prMVBaS3llOTgvUmUzeUpuNWhBNwpWRyt0RURHcEQzc0V1VS9aTHN2aDFMNlcxdkRHVDVUMHpqK0t0ekRLN3BDTlVNa2t5T1YwUlMzSkNMWGYxOWs2CmVvZHR6WDdvQ2FxYmYwNEdCa0x4bzFWTFJ1NG9SZjAvTS9TRlJMNmZkY01LZUNNU1JsWkx2T3JyTElzNWtQanoKSFhCaHE1aHhBb0dCQVBMZ0FnRHppV3greWcxTGU0K3g1VTkzRXlQKzFSYkZHOUxHRG1iT1U4Zlg5VlBmKytmeAp5eXcxWFg2V2pWWHhtVlQxRDZRQmxZOVVzMmEwNzQveXNCYno0cWkyT01ENUdWbGhMK3BvQUR6aWpuTUVZT3FvCjZ5NVNsV0NmZlRMQkpiYXB5ZlpSc0NmVU1hdVNYSWVudHUvM3RxQTBrR1JvbFMzbko1eHRtbU1iQW9HQkFOYTAKUlEvaEVwREhPbGRubitzQzI5VzZ4cTE4TTgvbHl5Z056aElQMG5hR21IbnBTZElMd1hDNzFPRTAyVit4YU5xagp4SkFLc1MyZm04MzNFUkhmUnBmaGFuRzBPaFU5UmRBaTVVbHhTMEtGMFRuZ2l5Mm54UlBmeGlmejY5aXpxQ25kCkJ2Q1BPdnlkMTgrNEpmK1NhWVZ0UUFMMlk2V29MZlNYRnBnL3RBZm5Bb0dBQ2pQUXdidDVQVjZDSEZiY1pPZmoKS3hoa2JIR3EvOTdkMFZmU2lRMEh4ZkJUZE9lR3pIS0N4Mk9pZnN2U09oY0JDdW1VYzU1QmF4anl4RkM3YzMrTgpXQmw4bnZ1d25LS2FKd0FISkVDa0tlcVdjMHh1eWN5NHZHUnBzL3BmSnYrb0Z1bEErNkRvMW5ONmRxdWFTWFNKCkljRjFJWHV6eVJQL21FTUtqZTZvdXBzQ2dZRUFsemxCcTMyTDVnMXRPa0x5a0VUak1oYnIrU1dTR014eDJ6UmUKQUxyRU1sZDY3ZU15NjQ3OElMc3JwYlBqcDFMOTE0R2g0UkttMk11aW9kOXpWbk04OVp6L3JEYVU4b0RGdTJQWQp1M0hnNEdvQWhCTno5Q1JHMjBpeCtWSUV2QTFPVks0cFJTYWRGWERYb0tiRG5NOHpJNWRmanJhY1dRR3IzS3JPCmIwM3ZMK0VDZ1lFQWw5dUlGY1BMaVV6aGk4VmJiWlplckFBSkFhbWlqZlBhSUhCNm1WTDBOcXNWSUtzUTlSdHAKMDR5ek1qL1ZKY21FVkpidW1kL256N1Vab2lEUUJnM2NhMDc3Q1cvYlY4RFcwWk9iYnNKalkyeDdFRVlvNk9YMwpQcHozUWhwczFsOS8vVWRrb2NZc2NMR282WnduV01vOHhRdzN2bGlnRFNYTlFFZlBqRkNhUFRFPQotLS0tLUVORCBSU0EgUFJJVkFURSBLRVktLS0tLQo=";
//        System.out.println(clientKeyResource);
        InputStream caCert = createInputStreamFromBase64EncodedString(caCertResource);
        InputStream clientCert = createInputStreamFromBase64EncodedString(clientCertResource);
        InputStream clientKey = createInputStreamFromBase64EncodedString(clientKeyResource);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore trustStore = KeyStore.getInstance("pkcs12");
        char[] trustStorePassphrase = "changeit".toCharArray();
        trustStore.load(null);
        while (caCert.available() > 0) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(caCert);
            String alias = cert.getSubjectX500Principal().getName() + "_" + cert.getSerialNumber().toString(16);
            trustStore.setCertificateEntry(alias, cert);
        }
        tmf.init(trustStore);
         trustManagers = tmf.getTrustManagers();
      // clientKey clientCrt
        CertificateFactory certFactory = CertificateFactory.getInstance("X509");
        Collection<? extends Certificate> certificates = certFactory.generateCertificates(clientCert);
        PrivateKey privateKey ;
        byte[] keyBytes = decodePem(clientKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        try {
            // First let's try PKCS8
            privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (InvalidKeySpecException e) {
            // Otherwise try PKCS8
            RSAPrivateCrtKeySpec keySpec = PKCS1Util.decodePKCS1(keyBytes);
            privateKey= keyFactory.generatePrivate(keySpec);
        }
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        String alias = certificates.stream().map(cert->((X509Certificate)cert).getIssuerX500Principal().getName()).collect(Collectors.joining("_"));
        keyStore.setKeyEntry(alias, privateKey, trustStorePassphrase, certificates.toArray(new Certificate[0]));
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, trustStorePassphrase);
        KeyManager[] keyManagers = kmf.getKeyManagers();

        //sslContext
        SSLContext sslContext ;
        try {
             sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyManagers, trustManagers, new SecureRandom());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw KubernetesClientException.launderThrowable(e);
        }
        return sslContext;
    }

    public String sendHttpRequest(@NotNull String method,@NotNull String path, Headers headers, RequestBody requestBody) throws Exception {
        SSLContext sslContext = preLoad();

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0])
                .hostnameVerifier((hostname, session) -> true)
                .build();

        HttpUrl url = HttpUrl.parse("https://10.168.59.90:6443" + path);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .method(method, requestBody);
        System.out.println(url);

        if (headers != null) {
            requestBuilder.headers(headers);
        }

        Request request = requestBuilder.build();

        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String result = responseBody.string();
                System.out.println(result);
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }



    private static ByteArrayInputStream createInputStreamFromBase64EncodedString(String data) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(data);
        } catch (IllegalArgumentException illegalArgumentException) {
            bytes = data.getBytes();
        }

        return new ByteArrayInputStream(bytes);
    }

    private static byte[] decodePem(InputStream keyInputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(keyInputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("-----BEGIN ")) {
                    return readBytes(reader, line.trim().replace("BEGIN", "END"));
                }
            }
            throw new IOException("PEM is invalid: no begin marker");
        }
    }
    private static byte[] readBytes(BufferedReader reader, String endMarker) throws IOException {
        String line;
        StringBuilder buf = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            if (line.contains(endMarker)) {
                return Base64.getDecoder().decode(buf.toString());
            }
            buf.append(line.trim());
        }
        throw new IOException("PEM is invalid : No end marker");
    }



    public String getRequestBody() throws JsonProcessingException {
        HashMap<String, String> labels = new HashMap<>();
        labels.put("slt", "vm-test-1");

        ArrayList<Disk> disks = new ArrayList<>();
        disks.add(new Disk().setName("containerinit").setBootOrder(1)
                .setDisk(new DiskTarget().setBus("virtio"))
        );
        ArrayList<Interface> interfaces = new ArrayList<>();
        interfaces.add(new Interface().setName("default").setMasquerade("{}"));

        ArrayList<Network> networks = new ArrayList<>();
        networks.add(new Network().setName("default").setPod(new PodNetwork()));

        ArrayList<Volume> volumes = new ArrayList<>();
        volumes.add(new Volume()
                .setName("containerinit")
                .setPersistentVolumeClaim(
                        new PersistentVolumeClaimVolumeSource()
                                .setClaimName("euler-test-1")
                )
        );

        VirtualMachine virtualMachine = new VirtualMachine();
        virtualMachine.setApiVersion("kubevirt.io/v1")
                .setKind("VirtualMachine")
                .setMetadata(new ObjectMeta().setName("vm-test-1"))
                .setSpec(new VirtualMachineSpec().setRunning(true)
                        .setTemplate(new VirtualMachineInstanceTemplateSpec()
                                .setMetadata(new ObjectMeta()
                                        .setNamespace("default")
                                        .setLabels(labels)
                                ).setSpec(
                                        new VirtualMachineInstanceSpec()
                                                .setDomain(
                                                        new DomainSpec().setDevices(new Devices()
                                                                .setAutoattachGraphicsDevice(true)
                                                                .setDisks(disks)
                                                                .setInterfaces(interfaces)
                                                        ).setResources(new ResourceRequirements()
                                                                .setRequests(new Requests()
                                                                        .setCpu("2")
                                                                        .setMemory("1G")
                                                                )
                                                        )
                                                ).setNetworks(networks)
                                                .setVolumes(volumes)
                                )
                        )
                );
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(virtualMachine);
        System.out.println(requestBody);
        return  requestBody;
    }

    public String sendHttpRequest(String method, String path) throws Exception {
        return sendHttpRequest(method,path,null,null);
    }

}
