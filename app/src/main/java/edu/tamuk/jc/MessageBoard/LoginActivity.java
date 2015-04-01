package edu.tamuk.jc.MessageBoard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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

import edu.tamuk.jc.service.LoginService;
import edu.tamuk.jc.service.ServiceRulesException;
import edu.tamuk.jc.service.UserService;


public class LoginActivity extends Activity {

    private EditText txtLoginName;
    private EditText txtLoginPassword;
    private Button btnCancel;
    private Button btnRegister;
    private Button btnLogin;

    private UserService userService = new LoginService(this);

    private static final int FLAG_LOGIN_SUCCESS = 1;
    private static final String MSG_LOGIN_SUCCESS = "Login success";
    private static final String MSG_LOGIN_ERROR = "Login Failed";
    public static final String MSG_LOGIN_FAILED = "Username or Password is incorrect";
    public static final String MSG_SERVER_ERROR = "Request error";
    public static final String MSG_REQUEST_TIMEOUT = "Server Request Timeout";
    public static final String MSG_RESPONSE_TIMEOUT = "Server Response Timeout";

    private static ProgressDialog dialog;
    /*
     volatile 变量可以被看作是一种 “程度较轻的 synchronized”；与 synchronized 块相比，volatile 变量所需的编码较少，并且运行时开销也较少，但是它所能实现的功能也仅是 synchronized 的一部分
     */
    public volatile static boolean isLogin = false;
    private String myEmail = null;
    private String myNickname = null;

    public void setUserInfo(String e, String n) {
        if(isLogin == true) {
            this.myEmail = e;
            this.myNickname = n;
        }
    }

    private void init() {
        this.txtLoginName = (EditText) this.findViewById(R.id.LoginName);
        this.txtLoginPassword = (EditText) this.findViewById(R.id.loginPassword);
        this.btnCancel = (Button) this.findViewById(R.id.btnCancel);
        this.btnRegister = (Button) this.findViewById(R.id.btnRegister);
        this.btnLogin = (Button) this.findViewById(R.id.BtnMenulogin);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        this.init(); // initiate all components

        // Login Button
        this.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String loginName = txtLoginName.getText().toString(); // have to use final here
                final String loginPassword = txtLoginPassword.getText().toString();

                //Toast.makeText(v.getContext(),"登陆名： " + loginName, Toast.LENGTH_SHORT).show();
                //Toast.makeText(v.getContext(),"登陆密码： " + loginPassword, Toast.LENGTH_SHORT).show();

                /*
                validate the input loginName and loginPassword
                 */

                /*
                Loading period
                 */
                if(dialog == null) {
                    dialog = new ProgressDialog(LoginActivity.this);
                }
                dialog.setTitle("Please wait");
                dialog.setMessage("Logging...");
                dialog.setCancelable(false);
                dialog.show();

                /*
                Vice thread: connect to the server
                 */
                Thread loginThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            userService.userLogin(loginName, loginPassword);
                            handler.sendEmptyMessage(FLAG_LOGIN_SUCCESS);
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
                            data.putSerializable("ErrorMsg", MSG_LOGIN_ERROR);
                            msg.setData(data);
                            handler.sendMessage(msg);
                        }
                    }
                });
                loginThread.start();
            }
        });

        // Cancel Button
        this.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iM = new Intent(LoginActivity.this, MessageActivity.class);
                startActivity(iM);
            }
        });

        // Register Button
        this.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iR = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(iR);
            }
        });

    }

//    private static Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//        }
//    };
    private class ActHandler extends Handler {

        private final WeakReference<Activity> mActivity;

        public ActHandler(LoginActivity activity) {
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
                   ((LoginActivity)mActivity.get()).showTip(errorMsg);
                   break;
               case FLAG_LOGIN_SUCCESS:
                   ((LoginActivity)mActivity.get()).showTip(MSG_LOGIN_SUCCESS);
                   Intent i = new Intent((LoginActivity)mActivity.get(), MessageActivity.class);
                   Bundle bundle = new Bundle();
                   bundle.putString("Email", ((LoginActivity)mActivity.get()).myEmail);
                   bundle.putString("Nickname", ((LoginActivity)mActivity.get()).myNickname);
                   i.putExtras(bundle);
                   startActivity(i);
                   break;
               default:
                   break;
           }
        }
    }

    public void showTip(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    public ActHandler handler = new ActHandler(this);



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
