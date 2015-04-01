package edu.tamuk.jc.service;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

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

import java.io.DataInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import edu.tamuk.jc.MessageBoard.LoginActivity;
import edu.tamuk.jc.MessageBoard.MessageActivity;
import edu.tamuk.jc.MessageBoard.RegisterActivity;

/**
 * Created by akluffy on 3/28/2015.
 */


public class LoginService implements UserService {

    private final WeakReference<Activity> mActivity;

    public LoginService(LoginActivity activity) {
        mActivity = new WeakReference<Activity>(activity);
    }
    //private static final String TAG = "LoginService";

    @Override
    public void userLogin(String loginName, String loginPassword) throws Exception {
        /*
        Log.d: Use this for debugging purposes. If you want to print out a bunch of messages so you can log the exact flow of your program, use this. If you want to keep a log of variable values, use this.
         */
        //Log.d(TAG, loginName);
        //Log.d(TAG, loginPassword);

        /**********************************
         *  1. Local Test: simulation on local machine *
         *  *******************************
                Thread.sleep(3000);

                if(loginName.equals("admin") && loginPassword.equals("123")) {

                } else {
                    throw new ServiceRulesException(LoginActivity.MSG_LOGIN_FAILED);
                }
        ***********************************/

        /* ****************************************
            2. Http DoGet Method
           ****************************************

            HttpClient client = new DefaultHttpClient(); // instantiate a httpclient object


            * uri : URL address: http://localhost:8886/Servlet/login.do
            * GET pass parameter : URL?parameter1=value&parameter2=value......

            String uri = "http://10.0.0.5:8886/Servlet/login.do?LoginName=" + loginName + "&LoginPassword="+loginPassword;
            HttpGet get = new HttpGet(uri); // do get method

            // response
            HttpResponse response = client.execute(get);
        */

        /**************************************************
         * 3. Http DoPost Method
         **************************************************
        */
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
        String uri = "http://10.0.0.5:8886/Servlet/login.do?";
        HttpPost post = new HttpPost(uri);

        // NameValuePair ---> List<NameValuePair> ---> HttpEntity ---> Post ---> HttpClient.execute
        NameValuePair paramLoginName = new BasicNameValuePair("LoginEmail", loginName);
        NameValuePair paramLoginPassword = new BasicNameValuePair("LoginPassword", loginPassword);
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(paramLoginName);
        parameters.add(paramLoginPassword);
        post.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
        HttpResponse response = client.execute(post);

        /* response's statusCode
             * 200: success
             * 400:cannot find the server
             * 500:error occurs
             * 406:wrong parameter
        */

        int statusCode = response.getStatusLine().getStatusCode();
        String result = null;
        if(statusCode != HttpStatus.SC_OK) {
            throw new ServiceRulesException(LoginActivity.MSG_SERVER_ERROR);
        } else {
            DataInputStream dis = new DataInputStream(response.getEntity().getContent());
            result = dis.readUTF();
            if(result.equals("success")) {
                String email = dis.readUTF();
                String nickname = dis.readUTF();
                LoginActivity.isLogin = true;
                ((LoginActivity)mActivity.get()).setUserInfo(email, nickname);
                dis.close();
            } else {
                throw new ServiceRulesException(LoginActivity.MSG_LOGIN_FAILED);
            }
        }
    }
}
