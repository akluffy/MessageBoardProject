package edu.tamuk.jc.service;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import edu.tamuk.jc.MessageBoard.MessageActivity;

/**
 * Created by akluffy on 3/31/2015.
 */
public class PostService {

    public void PublishService(String content, String email, String nickname) throws Exception {
        HttpParams params = new BasicHttpParams();
        // choose Character format through params
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        // set connection timeout---> ConnectionTimeoutException
        HttpConnectionParams.setConnectionTimeout(params, 5000);
        // set response timeout from the server---> SocketTimeOutException
        HttpConnectionParams.setSoTimeout(params, 5000);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", PlainSocketFactory.getSocketFactory(), 433));
        ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);

        HttpClient client = new DefaultHttpClient(connectionManager, params);
        String uri = "http://10.0.0.5:8886/Servlet/leaveamessage.do?";
        HttpPost post = new HttpPost(uri);

        // NameValuePair ---> List<NameValuePair> ---> HttpEntity ---> Post ---> HttpClient.execute
        NameValuePair paramContent = new BasicNameValuePair("Content", content);
        NameValuePair paramDate = new BasicNameValuePair("DateTime", new Timestamp(System.currentTimeMillis()).toString());
        NameValuePair paramEmail = new BasicNameValuePair("Email", email);
        NameValuePair paramNickname = new BasicNameValuePair("Nickname", nickname);
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(paramContent);
        parameters.add(paramDate);
        parameters.add(paramEmail);
        parameters.add(paramNickname);
        post.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
        HttpResponse response = client.execute(post);

        int statusCode = response.getStatusLine().getStatusCode();
        String result = null;
        if(statusCode != HttpStatus.SC_OK) {
            throw new ServiceRulesException(MessageActivity.MSG_SERVER_ERROR);
        } else {
            result = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
        }

        if(result.equals("Post Success")) {
            // it's not necessary to close this service thread;
        } else {
            throw new ServiceRulesException(MessageActivity.MSG_POST_FAILED);
        }
    }

}

