package edu.tamuk.jc.service;

import android.app.Activity;
import android.widget.Toast;

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

import java.io.DataInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tamuk.jc.MessageBoard.MessageActivity;

/**
 * Created by akluffy on 4/1/2015.
 */
public class UpdateService {

    private final WeakReference<Activity> mActivity;

    public UpdateService(MessageActivity activity) {
        mActivity = new WeakReference<Activity>(activity);
    }

    public void updateRequest() throws Exception {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpConnectionParams.setConnectionTimeout(params, 5000);
        HttpConnectionParams.setSoTimeout(params, 5000);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);

        HttpClient client = new DefaultHttpClient(connectionManager, params);
        String uri = "http://10.0.0.5:8886/Servlet/update.do?";
        HttpPost post = new HttpPost(uri);

        // NameValuePair ---> List<NameValuePair> ---> HttpEntity ---> Post ---> HttpClient.execute
        NameValuePair updateRequest = new BasicNameValuePair("Update Request", "Received");
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(updateRequest);

        post.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
        HttpResponse response = client.execute(post);

        int statusCode = response.getStatusLine().getStatusCode();
        String result = null;
        if(statusCode != HttpStatus.SC_OK) {
            throw new ServiceRulesException(MessageActivity.MSG_SERVER_ERROR);
        } else {
            DataInputStream dis = new DataInputStream(response.getEntity().getContent());
            result = dis.readUTF();
            if(result.equals("success")) {
                ((MessageActivity)mActivity.get()).listItem.clear();
                for(int i = 0; i < 10; ++i) {
                    String content = dis.readUTF();
                    String date = dis.readUTF();
                    String nickname = dis.readUTF();
                    System.out.println(content + date + nickname);
                    updateData(content, date, nickname);                }

            } else {
                throw new ServiceRulesException(MessageActivity.MSG_UPDATE_REQUEST_ERROR);
            }

            try {
                dis.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(dis != null) {
                    dis.close();
                }
            }
        }
    }

    private void updateData(String content, String date, String nickname) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("content", content);
        map.put("info", "by  " + nickname + "  on  " + date);
        ((MessageActivity)mActivity.get()).listItem.add(map);
    }

}
