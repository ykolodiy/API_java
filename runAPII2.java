package API;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class runAPII2 {

private static String USERNAME = "";
private static String PASSWORD = "";
private static String ENDPOINT = "https://api2.omniture.com/admin/1.4/rest/"; 

public static String callMethod(String method, String data) throws IOException {
    System.setProperty("https.proxyHost", "173.213.216.20");
    System.setProperty("https.proxyPort", "80");

    URL url = new URL(ENDPOINT + "?method=" + method);
    URLConnection connection = url.openConnection();
    connection.addRequestProperty("X-WSSE", getHeader());

    connection.setDoOutput(true);
    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
    wr.write(data);
    wr.flush();

    InputStream in = connection.getInputStream();
    BufferedReader res = new BufferedReader(new InputStreamReader(in, "UTF-8"));

    StringBuffer sBuffer = new StringBuffer();
    String inputLine;
    while ((inputLine = res.readLine()) != null)
        sBuffer.append(inputLine);

    res.close();

    return sBuffer.toString();
}

public static String getHeader()  {
    StringBuffer wsseHeader = new StringBuffer();

    try {
        SecureRandom rand = new SecureRandom();
        Base64 b64 = new Base64();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        String created = dateFormatter.format(new Date());
        ByteArrayOutputStream digest = new ByteArrayOutputStream(40);
        byte[] nonce = new byte[20];
        rand.nextBytes(nonce);
        digest.write(nonce);
        digest.write(created.getBytes());
        digest.write(PASSWORD.getBytes());
        wsseHeader.append("UsernameToken Username=\"");
        wsseHeader.append(USERNAME);
        wsseHeader.append("\", PasswordDigest=\"");
        wsseHeader.append(b64.encodeAsString(toSHA1(digest.toByteArray())));
        wsseHeader.append("\", Nonce=\"");
        wsseHeader.append(b64.encodeAsString(nonce));
        wsseHeader.append("\", Created=\"");
        wsseHeader.append(created);
        wsseHeader.append("\"");
    } catch (IOException e) {
        e.printStackTrace();
    }
    return wsseHeader.toString();
}

private static byte[] toSHA1(byte[] convertme) {
    MessageDigest md = null;
    try {
        md = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
    }
    return md.digest(convertme);
}



public static void main(String[] args) throws IOException {
    Map map = new HashMap();
    map.put("rs_type", new String[] { "standard" });
    map.put("sp", "");

    String response = runAPII2.callMethod("Company.GetReportSuites", JSONObject.fromObject(map).toString());
    JSONObject jsonObj = JSONObject.fromObject(response);
    JSONArray jsonArry = JSONArray.fromObject(jsonObj.get("report_suites"));

    for (int i = 0; i < jsonArry.size(); i++) {
        System.out.println("Report Suite ID: " + JSONObject.fromObject(jsonArry.get(i)).get("rsid"));
        System.out.println("Site Title: " + JSONObject.fromObject(jsonArry.get(i)).get("site_title"));
        System.out.println();
    }
}
}