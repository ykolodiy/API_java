/*
 * Simple example makes call to Omniture API to get a companies report suites
 * 
 * Requires the following libraries
 * 
 *  jakarta commons-lang 2.4
 *  jakarta commons-beanutils 1.7.0
 *  jakarta commons-collections 3.2
 *  jakarta commons-logging 1.1.1
 *  ezmorph 1.0.6
 *  json-lib-2.3-jdk13
 *  
 *
 *  @author Lamont Crook
 *  @email lamont@adobe.com
 * 
 */

package API;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
//https://marketing.adobe.com/developer/api-explorer list of codes
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class runAPII {
        private static String USERNAME = "";
    private static String PASSWORD = "";
    private static String ENDPOINT = "https://api2.omniture.com/admin/1.4/rest/"; //dallas endpoint, change for your company's datacenter

    private runAPII() {}

    public static String callMethod(String method, String data) throws IOException {
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

    private static String getHeader() throws UnsupportedEncodingException {
        byte[] nonceB = generateNonce();
        String nonce = base64Encode(nonceB);
        String created = generateTimestamp();
        String password64 = getBase64Digest(nonceB, created.getBytes("UTF-8"), PASSWORD.getBytes("UTF-8"));
        StringBuffer header = new StringBuffer("UsernameToken Username=\"");
        header.append(USERNAME);
        header.append("\", ");
        header.append("PasswordDigest=\"");
        header.append(password64.trim());
        header.append("\", ");
        header.append("Nonce=\"");
        header.append(nonce.trim());
        header.append("\", ");
        header.append("Created=\"");
        header.append(created);
        header.append("\"");
        return header.toString();
    }

    private static byte[] generateNonce() {
        String nonce = Long.toString(new Date().getTime());
        return nonce.getBytes();
    }

    private static String generateTimestamp() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return dateFormatter.format(new Date());
    }

    private static synchronized String getBase64Digest(byte[] nonce, byte[] created, byte[] password) {
      try {
        MessageDigest messageDigester = MessageDigest.getInstance("SHA-1");
        // SHA-1 ( nonce + created + password )
        messageDigester.reset();
        messageDigester.update(nonce);
        messageDigester.update(created);
        messageDigester.update(password);
        return base64Encode(messageDigester.digest());
      } catch (java.security.NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
    }

    private static String base64Encode(byte[] bytes) {
      return Base64Coder.encodeLines(bytes);
    }

    public static void main(String[] args) throws IOException {
        Map map = new HashMap();
       // map.put("rs_type", new String[]{"standard"});
       // map.put("sp", "");
       map.put("search", ""); 
        map.put("types", new String[]{"standard"});
       
      
       
        
        
        

        String response = runAPII.callMethod("Company.GetReportSuites", JSONObject.fromObject(map).toString());
        JSONObject jsonObj = JSONObject.fromObject(response);
        JSONArray jsonArry = JSONArray.fromObject(jsonObj.get("report_suites"));

        for(int i = 0; i < jsonArry.size(); i++) {
           System.out.println("Report Suite ID: " + JSONObject.fromObject(jsonArry.get(i)).get("rsid"));
            System.out.println("Site Title: " + JSONObject.fromObject(jsonArry.get(i)).get("site_title"));
            System.out.println();
        	
        }
    }
}
