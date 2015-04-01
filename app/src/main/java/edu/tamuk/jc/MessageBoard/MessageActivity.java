package edu.tamuk.jc.MessageBoard;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.conn.ConnectTimeoutException;

import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.tamuk.jc.MessageBoard.R;
import edu.tamuk.jc.service.PostService;
import edu.tamuk.jc.service.ServiceRulesException;
import edu.tamuk.jc.service.UpdateService;

public class MessageActivity extends Activity implements View.OnClickListener, RefreshableView.PullToRefreshListener {

    private EditText txtMessage;
    private Button btnBack;
    private Button btnLogOut;
    private Button btnEnter;
    private RefreshableView rView;
    private ListView lView;

    private static final int FLAG_SUBMIT_SUCCESS = 1;
    private static final int FLAG_UPDATE_SUCCESS = 2;
    private static final String MSG_REQUEST_TIMEOUT = "Server Request Timeout";
    private static final String MSG_RESPONSE_TIMEOUT = "Server Response Timeout";
    public static final String MSG_POST_SUCCESS = "Post success~";
    public static final String MSG_POST_FAILED = "Post failed";
    public static final String MSG_SERVER_ERROR = "Server connection error";
    public static final String MSG_UPDATE_SUCCESS = "Refresh success@_@";
    private static final String MSG_POST_ERROR = "Unknown error";
    private static final String MSG_UPDATE_ERROR = "Server update error";
    public static final String MSG_UPDATE_REQUEST_ERROR = "Server update request error";

    private String myEmail = null;
    private String myNickname = null;

    private static ProgressDialog dialog;
    private PostService postService = null;
    private UpdateService updateService = null;
    public UpdateHandler updatehandler = null;
    public PostHandler posthandler = null;

    SimpleAdapter adapter = null;
    public List<Map<String, String>> listItem = new ArrayList<Map<String, String>>();


    private void init() {
        this.txtMessage = (EditText) this.findViewById(R.id.txtMessage);
        this.btnBack = (Button) this.findViewById(R.id.btnBack);
        this.btnLogOut = (Button) this.findViewById(R.id.btnLogOut);
        this.btnEnter = (Button) this.findViewById(R.id.btnEnter);
        this.rView = (RefreshableView)this.findViewById(R.id.refreshable_view);
        this.lView = (ListView)this.findViewById(R.id.list_view);

        txtMessage.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnLogOut.setOnClickListener(this);
        btnEnter.setOnClickListener(this);
        rView.setOnRefreshListener(this, 0);

        posthandler = new PostHandler(this);
        updatehandler = new UpdateHandler(this);
        updateService = new UpdateService(this);
        postService = new PostService();

        adapter = new SimpleAdapter(this, listItem, R.layout.message_layout,
                new String[] {"content", "info"}, new int[]{R.id.content,R.id.info});
        lView.setAdapter(adapter);

        if(LoginActivity.isLogin == false) {
            btnLogOut.setVisibility(View.GONE);
        } else {
            Bundle bundle = this.getIntent().getExtras();
            myEmail = bundle.getString("Email");
            myNickname = bundle.getString("Nickname");
            btnBack.setVisibility(View.GONE);
        }
    }





    @Override
    public void onRefresh() {
        try {
            refresh();
            Thread.sleep(60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        rView.finishRefreshing();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        init();
        refresh();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack: {
                LoginActivity.isLogin = false;
                Intent i = new Intent(MessageActivity.this, LoginActivity.class);
                startActivity(i);
                break;
            }
            case R.id.btnLogOut: {
                LoginActivity.isLogin = false;
                myEmail = null;
                myNickname = null;
                Intent i = new Intent(MessageActivity.this, LoginActivity.class);
                startActivity(i);
                break;
            }
            case R.id.btnEnter: {
                if(LoginActivity.isLogin == false) {
                    Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                } else {
                    postMessage();
                    // update and refresh
                    refresh();
                }
                break;
            }
            default:
                break;
        }
    }

    private void refresh() {
        Thread updateThread = new Thread(new UpdateThread());
        updateThread.start();
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    private void postMessage() {
        final String content = this.txtMessage.getText().toString();
        final String email = this.myEmail;
        final String nickname = this.myNickname;
        if(content.equals("") || content == null) {
            Toast.makeText(this, "Cannot post empty message", Toast.LENGTH_SHORT).show();
        } else {
            if(dialog == null) {
                dialog = new ProgressDialog(MessageActivity.this);
            }
            dialog.setTitle("Please wait");
            dialog.setMessage("Submitting...");
            dialog.setCancelable(false);
            dialog.show();

            Thread submitThread = new Thread(new PostThread(content, email, nickname));
            submitThread.start();
        }
    }

    private void showTip(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    // Post Handler for MessageActivity
    private class PostHandler extends Handler {

        private final WeakReference<Activity> mActivity;

        public PostHandler(MessageActivity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {

            if(dialog != null) {
                dialog.dismiss();
            }
            int flag = msg.what;
            switch (flag) {
                case 0:
                    String errorMsg = msg.getData().getSerializable("ErrorMsg").toString();
                    ((MessageActivity)mActivity.get()).showTip(errorMsg);
                    break;
                case FLAG_SUBMIT_SUCCESS:
                    ((MessageActivity)mActivity.get()).showTip(MSG_POST_SUCCESS);
                    break;
                default:
                    break;
            }
        }
    }

    // Post Thread
    public class PostThread implements Runnable {
        private String content, email, nickname;
        public PostThread(String c, String e, String n) {
            this.content = c;
            this.email = e;
            this.nickname = n;
        }

        @Override
        public void run() {
            try {
                postService.PublishService(content, email, nickname);
                posthandler.sendEmptyMessage(FLAG_SUBMIT_SUCCESS);
            } catch (ConnectTimeoutException cte) {
                cte.printStackTrace();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("ErrorMsg", MSG_REQUEST_TIMEOUT);
                msg.setData(data);
                posthandler.sendMessage(msg);
            } catch (SocketTimeoutException ste) {
                ste.printStackTrace();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("ErrorMsg", MSG_RESPONSE_TIMEOUT);
                msg.setData(data);
                posthandler.sendMessage(msg);
            } catch (ServiceRulesException sre) {
                sre.printStackTrace();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("ErrorMsg", sre.getMessage());
                msg.setData(data);
                posthandler.sendMessage(msg);
            } catch(Exception e) {
                e.printStackTrace();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("ErrorMsg", MSG_POST_ERROR);
                msg.setData(data);
                posthandler.sendMessage(msg);
            }
        }
    }


    /************************************************************************************************/
    private class UpdateHandler extends Handler {

        private final WeakReference<Activity> mActivity;

        public UpdateHandler(MessageActivity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {

            if(dialog != null) {
                dialog.dismiss();
            }
            int flag = msg.what;
            switch (flag) {
                case 0:
                    String errorMsg = msg.getData().getSerializable("ErrorMsg").toString();
                    ((MessageActivity)mActivity.get()).showTip(errorMsg);
                    break;
                case FLAG_UPDATE_SUCCESS:
                    ((MessageActivity)mActivity.get()).showTip(MSG_UPDATE_SUCCESS);
                    ((MessageActivity)mActivity.get()).adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    }


    public class UpdateThread implements Runnable {

        @Override
        public void run() {
            try {
                updateService.updateRequest();
                updatehandler.sendEmptyMessage(FLAG_UPDATE_SUCCESS);
            } catch (ConnectTimeoutException cte) {
                cte.printStackTrace();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("ErrorMsg", MSG_REQUEST_TIMEOUT);
                msg.setData(data);
                updatehandler.sendMessage(msg);
            } catch (SocketTimeoutException ste) {
                ste.printStackTrace();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("ErrorMsg", MSG_RESPONSE_TIMEOUT);
                msg.setData(data);
                updatehandler.sendMessage(msg);
            } catch (ServiceRulesException sre) {
                sre.printStackTrace();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("ErrorMsg", sre.getMessage());
                msg.setData(data);
                updatehandler.sendMessage(msg);
            } catch(Exception e) {
                e.printStackTrace();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("ErrorMsg", MSG_UPDATE_ERROR);
                msg.setData(data);
                updatehandler.sendMessage(msg);
            }
        }
    }


}
