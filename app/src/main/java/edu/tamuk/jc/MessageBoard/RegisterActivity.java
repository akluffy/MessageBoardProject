package edu.tamuk.jc.MessageBoard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import org.apache.http.conn.ConnectTimeoutException;

import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

import edu.tamuk.jc.MessageBoard.R;
import edu.tamuk.jc.service.RegisterService;
import edu.tamuk.jc.service.ServiceRulesException;

public class RegisterActivity extends Activity implements View.OnClickListener {

    private Button btnBack;
    private Button btnSubmit;
    private EditText userEmail, password, cfmPassword, nickname;
    private static ProgressDialog dialog;
    private RegisterService registerService = new RegisterService();

    private static final int FLAG_REGISTER_SUCCESS = 1;
    private static final String MSG_REGISTER_SUCCESS = "Register Success";
    public static final String MSG_REGISTER_FAILED = "A user is already registered\n with this e-mail address";
    public static final String MSG_REGISTER_ERROR = "Register Failed: error occurs";
    public static final String MSG_SERVER_ERROR = "Request error";
    private static final String MSG_REQUEST_TIMEOUT = "Server Request Timeout";
    private static final String MSG_RESPONSE_TIMEOUT = "Server Response Timeout";
    public static final String MSG_INVALID_INPUT = "The input is invalid";



    private void init() {
        btnBack = (Button) this.findViewById(R.id.register_back);
        btnSubmit = (Button) this.findViewById(R.id.register_button);
        userEmail = (EditText)findViewById(R.id.user_email_edit);
        password = (EditText)findViewById(R.id.password_edit);
        cfmPassword = (EditText)findViewById(R.id.password_edit2);
        nickname = (EditText)findViewById(R.id.user_nickname);

        btnBack.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.init();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.register_back:
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
                break;
            case R.id.register_button:
                submitRegister();
                break;
            default:
                break;
        }
    }

    private void submitRegister() {
        /*
        ** In real application, we should to input verification first
        **
         */
        final String userEmail = this.userEmail.getText().toString();
        final String password = this.password.getText().toString();
        final String cfmPassword = this.cfmPassword.getText().toString();
        final String nickname = this.nickname.getText().toString();
        if(password.equals(cfmPassword)) {
            if(dialog == null) {
                dialog = new ProgressDialog(RegisterActivity.this);
            }
            dialog.setTitle("Please wait");
            dialog.setMessage("Submitting...");
            dialog.setCancelable(false);
            dialog.show();

            Thread submitThread = new Thread(new RegisterThread(userEmail, password, nickname));
            submitThread.start();
        } else {
            Toast.makeText(this, "Password is not same", Toast.LENGTH_SHORT).show();
        }
    }

    // Handler for RegisterActivity
    private static class RegHandler extends Handler {

        private final WeakReference<Activity> mActivity;

        public RegHandler(RegisterActivity activity) {
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
                    ((RegisterActivity)mActivity.get()).showTip(errorMsg);
                    break;
                case FLAG_REGISTER_SUCCESS:
                    ((RegisterActivity)mActivity.get()).showTip(MSG_REGISTER_SUCCESS);
                    ((RegisterActivity)mActivity.get()).finish();
                    break;
                default:
                    break;
            }
        }
    }

    public RegHandler handler = new RegHandler(this);

    private void showTip(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    // 内部类
    public class RegisterThread implements Runnable {
        private String email, password, nickname;
        public RegisterThread(String e, String p, String n) {
            this.email = e;
            this.password = p;
            this.nickname = n;
        }

        @Override
        public void run() {
            try {
                registerService.userRegister(email, password, nickname);
                handler.sendEmptyMessage(FLAG_REGISTER_SUCCESS);
            } catch (ConnectTimeoutException cte) {
                cte.printStackTrace();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("ErrorMsg", MSG_REQUEST_TIMEOUT);
                msg.setData(data);
                handler.sendMessage(msg);
            } catch (SocketTimeoutException ste) {
                ste.printStackTrace();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("ErrorMsg", MSG_RESPONSE_TIMEOUT);
                msg.setData(data);
                handler.sendMessage(msg);
            } catch (ServiceRulesException sre) {
                sre.printStackTrace();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("ErrorMsg", sre.getMessage());
                msg.setData(data);
                handler.sendMessage(msg);
            } catch(Exception e) {
                e.printStackTrace();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("ErrorMsg", MSG_REGISTER_ERROR);
                msg.setData(data);
                handler.sendMessage(msg);
            }
        }
    }

}
