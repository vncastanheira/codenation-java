package com.vnc;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class Main {

    static String GET_URL = "https://api.codenation.dev/v1/challenge/dev-ps/generate-data?token=56b38829c35932d8c39c8b212288a0a43752ccc9";
    static String POST_URL = "https://api.codenation.dev/v1/challenge/dev-ps/submit-solution?token=56b38829c35932d8c39c8b212288a0a43752ccc9";

    static String answerFilePath = "D:\\Java Projects\\Criptografia-Julio-Cesar\\answer.json";

    public static void main(String[] args) throws IOException {
        try {
            JSONObject jsonObject = GetJSON();

            // read json contents
            String cifrado = (String) jsonObject.get("cifrado");
            long shift = (long) jsonObject.get("numero_casas");

            String decifrado = Decipher(cifrado.toLowerCase(), shift);

            jsonObject.replace("decifrado", decifrado);
            jsonObject.replace("resumo_criptografico", Hash(decifrado));


            WriteFile(jsonObject);
            Send();
        }
        catch (ParseException ex) {}
    }


    public static String Hash(String message) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(message.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject GetJSON()
            throws IOException, FileNotFoundException, ParseException{
        //URL url = new URL(request);
        //InputStream stream = url.openStream();
        //BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
        File file = new File("D:\\Java Projects\\Criptografia-Julio-Cesar\\question.json");
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String json = readAll(reader);
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        reader.close();

        return jsonObject;
    }

    public static void Send() throws MalformedURLException, IOException {
        FileBody bin = new FileBody(new File(answerFilePath));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try{
            HttpPost httpPost = new HttpPost(POST_URL);

            StringBody comment = new StringBody("A binary file of some kind", ContentType.MULTIPART_FORM_DATA);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("answer", bin)
                    .build();

            httpPost.setEntity(reqEntity);
            System.out.println("executing request " + httpPost.getRequestLine());
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    System.out.println("RESPONSE:");
                    System.out.println("-- Length: " + resEntity.getContentLength());
                    System.out.println("-- Content: " + resEntity.getContent());
                    System.out.println("-- Type: " + resEntity.getContentType());
                }
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }

        //List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        //params.add(new BasicNameValuePair("Content-Type", "multipart/form-data; charset=UTF-8"));
        //params.add(new BasicNameValuePair("User-Agent", "Java client"));
        //httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));



/*        URL url = new URL(POST_URL);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("POST");
        httpConnection.setDoOutput(true);
        httpConnection.setRequestProperty("User-Agent", "Java client");
        httpConnection.setRequestProperty("Content-Type", "multipart/form-data; charset=UTF-8");

        try(DataOutputStream os = new DataOutputStream(httpConnection.getOutputStream())) {
            os.write(fileInputStream.readAllBytes());
        }



        System.out.println(httpConnection.getResponseCode());
        System.out.println(httpConnection.getResponseMessage());

        httpConnection.disconnect();

 */
    }

    public static void WriteFile(JSONObject jsonObject) throws IOException {
        FileWriter fileWriter = new FileWriter(answerFilePath);
        fileWriter.write(jsonObject.toJSONString());
        fileWriter.close();
    }

    public static String Decipher(String content, long shift){
        String result = "";
        for (int i = 0; i < content.length(); i++) {
            char character = content.charAt(i);
            if (Character.toString(character).matches("^[A-Za-z]+$"))
            {
                result += Julius((long) character, shift);
            }
            else {
                result += character;
            }
        }

        return result;
    }

    public static char Julius(long charCode, long shift) {
        long finalCode = charCode - shift;
        if(finalCode < 'a')
        {
            long diff = 'a' - finalCode;
            long newCode = 'z' - diff + 1;
            return (char) newCode;
        }
        else
            return (char) finalCode;
    }

    private static String readAll(BufferedReader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        String cp = new String();
        while((cp=rd.readLine())!=null){
            sb.append(cp);
        }
        return sb.toString();
    }
}
