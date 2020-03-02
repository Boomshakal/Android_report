package com.megmeet.megmeetreport;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.megmeet.megmeetreport.bean.SQLBean;
import com.megmeet.megmeetreport.bean.SequenceBean;
import com.megmeet.megmeetreport.bean.WorklineBean;
import com.megmeet.megmeetreport.sql.Parameters;
import com.megmeet.megmeetreport.sql.SQLConnect;
import com.megmeet.megmeetreport.util.InformationUtils;
import com.megmeet.megmeetreport.util.Result;
import com.megmeet.megmeetreport.util.ResultHandler;

import java.util.ArrayList;

public class ReportLocationActivity extends AppCompatActivity {
    private static final int CHOOSESEQUENCE = 1001;
    private static final int CHOOSEWORKLINE = 1002;
    private static final int NOSEQUENCE = 1023;
    private static final int NOWORKLINE = 1024;
    private static final int INTENTTOREPORT = 1033;
    private LinearLayout linearlayoutAdd;
    private ImageButton imageButtonTask;
    private ImageButton imageSeqId;
    private ImageButton imageButtonWorkLine;
    private ImageButton imageButtonExit;
    private ImageButton imageButtonLogin;
    EditText editText;
    EditText editTextSequence;
    EditText editTextWorkline;
    private int sequenceId;
    private String workLine;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor edit;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHOOSESEQUENCE:
                    final ArrayList<SequenceBean> sqlBeans = (ArrayList<SequenceBean>) msg.obj;
                    ArrayList<String> names = new ArrayList<String>();
                    for (SequenceBean row : sqlBeans) {
                        StringBuffer name = new StringBuffer();
                        name.append(row.getSequenceCode() + "-" + row.getSequenceName());
                        names.add(name.toString());
                    }

                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which >= 0) {
                                sequenceId = sqlBeans.get(which).getSequenceId();
                                String sequenceCode = sqlBeans.get(which).getSequenceCode();
                                String sequenceName = sqlBeans.get(which).getSequenceName();
                                editTextSequence.setText(sequenceCode + "-" + sequenceName);
                            }
                            dialog.dismiss();
                        }
                    };
                    new AlertDialog.Builder(ReportLocationActivity.this).setTitle("请选择")
                            .setSingleChoiceItems(names.toArray(new String[0]), names.indexOf
                                    (editTextSequence.getText().toString()), listener)
                            .setNegativeButton("取消", null).show();
                    break;
                case CHOOSEWORKLINE:
                    final ArrayList<WorklineBean> worklineBeans = (ArrayList<WorklineBean>) msg.obj;
                    ArrayList<String> worklines = new ArrayList<>();
                    for (WorklineBean row : worklineBeans) {
                        StringBuffer name = new StringBuffer();
                        name.append(row.getName());
                        worklines.add(name.toString());
                    }

                    DialogInterface.OnClickListener worklistener = new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which >= 0) {
                                workLine = worklineBeans.get(which).getName();
                                editTextWorkline.setText(workLine);
                            }
                            dialog.dismiss();
                        }
                    };
                    new AlertDialog.Builder(ReportLocationActivity.this).setTitle("请选择")
                            .setSingleChoiceItems(worklines.toArray(new String[0]), worklines.indexOf
                                    (editTextWorkline.getText().toString()), worklistener)
                            .setNegativeButton("取消", null).show();
                    break;
                case NOSEQUENCE:
                    Toast.makeText(ReportLocationActivity.this, "没有工序", Toast.LENGTH_SHORT).show();
                    break;
                case NOWORKLINE:
                    Toast.makeText(ReportLocationActivity.this, "输入的工单有误。", Toast.LENGTH_SHORT).show();
                    break;
                case INTENTTOREPORT:
                    Intent intent = new Intent(ReportLocationActivity.this, ReportActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportlocation);
        linearlayoutAdd = findViewById(R.id.linearlayout_add);
        imageButtonTask = findViewById(R.id.btn_task_order_code);
        imageSeqId = findViewById(R.id.btn_seq_id);
        imageButtonWorkLine = findViewById(R.id.btn_work_line);
        editText = findViewById(R.id.txtServer);
        editTextSequence = findViewById(R.id.txt_sequence);
        editTextWorkline = findViewById(R.id.txt_workline);
        imageButtonExit = findViewById(R.id.btnExit);
        imageButtonLogin = findViewById(R.id.btnLogin);
        sharedPreferences = getSharedPreferences("report", MODE_PRIVATE);
        edit = sharedPreferences.edit();
        final int seq_id = sharedPreferences.getInt("seq_id", 0);
        String seq_name = sharedPreferences.getString("seq_name", "");
        String first_task_order_code = sharedPreferences.getString("first_task_order_code", "");
        String work_line = sharedPreferences.getString("work_line", "");
        if (seq_id > 0) {
            sequenceId = seq_id;
        }
        if (!TextUtils.isEmpty(seq_name)) {
            editTextSequence.setText(seq_name);
        }
        if (!TextUtils.isEmpty(first_task_order_code)) {
            editText.setText(first_task_order_code);
        }
        if (!TextUtils.isEmpty(work_line)) {
            editTextWorkline.setText(work_line);
        }
        imageButtonTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View layout = getLayoutInflater().inflate(R.layout.reportlocation_item, null);
                linearlayoutAdd.addView(layout);
            }
        });
        imageSeqId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSequence();
            }
        });
        imageButtonWorkLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadWorkline();
            }
        });
        imageButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (sequenceId < 1) {
                    Toast.makeText(ReportLocationActivity.this, "请先选择管控工序", Toast.LENGTH_SHORT).show();
                } if (TextUtils.isEmpty(editTextWorkline.getText().toString())) {
                    Toast.makeText(ReportLocationActivity.this, "请先选择线体", Toast.LENGTH_SHORT).show();
                }else {
                    new Thread(){
                        @Override
                        public void run() {
                            StringBuffer stringBuffer = new StringBuffer();
                            String s = editText.getText().toString();
                            if (TextUtils.isEmpty(s)) {
                                Toast.makeText(ReportLocationActivity.this, "请先输入工单", Toast.LENGTH_SHORT).show();
                            } else {
                                stringBuffer.append(s);
                                for (int i = 0; i < linearlayoutAdd.getChildCount(); i++) {
                                    EditText editText = linearlayoutAdd.getChildAt(i).findViewById(R.id.txtServer);
                                    String s1 = editText.getText().toString();
                                    if (!TextUtils.isEmpty(s1)) {
                                        stringBuffer.append(",");
                                        stringBuffer.append(s1);
                                    }
                                }
                                edit.putInt("seq_id", sequenceId);
                                edit.putString("seq_name", editTextSequence.getText().toString());
                                edit.putString("first_task_order_code", editText.getText().toString());
                                edit.putString("work_line", editTextWorkline.getText().toString());
                                edit.commit();
                                //把填写的数据插入数据库
                                String macAddress = InformationUtils.getInformationUtils(ReportLocationActivity.this).getMacAddress();
                                String sql = "exec fm_update_finereport_datas_macaddress_workline ?,?,?,?";
                                Parameters p = new Parameters().add(1, macAddress).add(2, stringBuffer.toString()).add(3, sequenceId).add(4, editTextWorkline.getText().toString());
                                ArrayList<SQLBean> sqlBeans = SQLConnect.connectSQL(sql, p);
                                Message message = new Message();
                                message.what = INTENTTOREPORT;
                                message.obj = sqlBeans;
                                handler.sendMessage(message);
                            }
                        }
                    }.start();

                }
            }
        });
        imageButtonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linearlayoutAdd.getChildCount() > 0) {
                    linearlayoutAdd.removeViewAt(linearlayoutAdd.getChildCount() - 1);
                } else {
                    new AlertDialog.Builder(ReportLocationActivity.this)
                            .setTitle("提示")
                            .setMessage("确定要退出程序吗？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();
                }
            }
        });
    }

    private void loadWorkline() {
        new Thread() {
            @Override
            public void run() {
                String sql = "exec p_qm_sop_work_line_items ?,?";
                Parameters p = new Parameters().add(1, 1).add(2, editText.getText().toString());
                ArrayList<WorklineBean> worklineBeans = SQLConnect.connectWorklineSQL(sql, p);
                Message message = new Message();
                if (worklineBeans.size() > 0) {
                    message.obj = worklineBeans;
                    message.what = CHOOSEWORKLINE;
                } else {
                    message.what = NOWORKLINE;
                }
                handler.sendMessage(message);
            }
        }.start();
    }

    private void loadSequence() {
        new Thread() {
            @Override
            public void run() {
                String sql = "SELECT  * FROM  dbo.fm_eng_sequence";
                ArrayList<SequenceBean> sqlBeans = SQLConnect.connectSequenceSQL(sql, null);
                Message message = new Message();
                if (sqlBeans.size() > 0) {
                    message.obj = sqlBeans;
                    message.what = CHOOSESEQUENCE;
                } else {
                    message.what = NOSEQUENCE;
                }
                handler.sendMessage(message);
            }
        }.start();
    }
}
