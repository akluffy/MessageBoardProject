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

import java.util.ArrayList;
import java.util.List;

import edu.tamuk.jc.MessageBoard.RegisterActivity;

/**
 * Created by akluffy on 3/29/2015.
 */
public class RegisterService {

    public void userRegister(String email, String password, String nickname) throws Exception {
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
        String uri = "http://10.0.0.5:8886/Servlet/register.do?";
        HttpPost post = new HttpPost(uri);

        // NameValuePair ---> List<NameValuePair> ---> HttpEntity ---> Post ---> HttpClient.execute
        NameValuePair paramEmail = new BasicNameValuePair("Email", email);
        NameValuePair paramPassword = new BasicNameValuePair("Password", password);
        NameValuePair paramNickname = new BasicNameValuePair("Nickname", nickname);
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(paramEmail);
        parameters.add(paramPassword);
        parameters.add(paramNickname);
        post.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
        HttpResponse response = client.execute(post);

        int statusCode = response.getStatusLine().getStatusCode();
        String result = null;
        if(statusCode != HttpStatus.SC_OK) {
            throw new ServiceRulesException(RegisterActivity.MSG_SERVER_ERROR);
        } else {
            result = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
        }

        if(result.equals("Register Success")) {
            // back to login page
        } else if(result.equals("Email is Registered")) {
            throw new ServiceRulesException(RegisterActivity.MSG_REGISTER_FAILED);
        } else if(result.equals("Invalid Input")) {
            throw new ServiceRulesException(RegisterActivity.MSG_INVALID_INPUT);
        } else {
            throw new ServiceRulesException(RegisterActivity.MSG_REGISTER_ERROR);
        }
    }

}
