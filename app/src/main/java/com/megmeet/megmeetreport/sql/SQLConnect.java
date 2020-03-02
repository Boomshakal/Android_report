package com.megmeet.megmeetreport.sql;

import android.util.Log;

import com.megmeet.megmeetreport.bean.SQLBean;
import com.megmeet.megmeetreport.bean.SequenceBean;
import com.megmeet.megmeetreport.bean.VoiceBean;
import com.megmeet.megmeetreport.bean.WorklineBean;
import com.megmeet.megmeetreport.util.InformationUtils;
import com.megmeet.megmeetreport.util.ResultHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class SQLConnect {
    private static String driverName = "net.sourceforge.jtds.jdbc.Driver";
//    private static String url = "jdbc:jtds:sqlserver://10.3.1.205:1433/x1_core_prod;charset=gb2312;useLOBs=false;socketTimeout=30";
//    private static String userName = "sa";
//    private static String password = "Dev_Test@2019";

    private static String url = "jdbc:jtds:sqlserver://192.168.0.126:1433/X1_CORE_PROD;charset=gb2312;useLOBs=false;socketTimeout=30";
//    private static String url = "jdbc:jtds:sqlserver://192.168.0.126:1433/x1_core_prod;charset=gb2312;useLOBs=false;socketTimeout=30";
    private static String userName = "sa";
    private static String password = "2018@Ikahe";

    private static SQLConnect mSqlConnect;

//    private SQLConnect() {
//    }

    public static SQLConnect getSQLConnet() {
        if (mSqlConnect != null) {
            mSqlConnect = new SQLConnect();
        }
        return mSqlConnect;
    }

    public static ArrayList<SequenceBean> connectSequenceSQL(String sql, Parameters parameters) {
        ArrayList<SequenceBean> sqlLists = new ArrayList<>();
//        String sql = "exec fm_get_kanban_by_mac_address ?";
        Connection connection = null;
        try {
            Class.forName(driverName);   //加载驱动
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("len", "CLASSNOTFOUND:" + e.getMessage());
        }
        try {
            connection = DriverManager.getConnection(url, userName, password);
            ResultSet resultSet;

            if (parameters != null) {
                PreparedStatement statement = connection.prepareStatement(sql);
                PrepareParameters(statement, parameters);
                resultSet = statement.executeQuery();
            } else {
                Statement statement = connection.createStatement();
                resultSet = statement.executeQuery(sql);
            }
            while (resultSet.next()) {
                SequenceBean sqlBean = new SequenceBean();
                int sequenceId = resultSet.getInt("sequence_id");
                String sequenceCode = resultSet.getString("code");
                String sequenceName = resultSet.getString("name");
                sqlBean.setSequenceId(sequenceId);
                sqlBean.setSequenceCode(sequenceCode);
                sqlBean.setSequenceName(sequenceName);
                sqlLists.add(sqlBean);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("len", "SQL:" + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return sqlLists;
    }

    public static ArrayList<WorklineBean> connectWorklineSQL(String sql, Parameters parameters) {
        ArrayList<WorklineBean> sqlLists = new ArrayList<>();
//        String sql = "exec fm_get_kanban_by_mac_address ?";
        Connection connection = null;
        try {
            Class.forName(driverName);   //加载驱动
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("len", "CLASSNOTFOUND:" + e.getMessage());
        }
        try {
            connection = DriverManager.getConnection(url, userName, password);
            ResultSet resultSet;

            if (parameters != null) {
                PreparedStatement statement = connection.prepareStatement(sql);
                PrepareParameters(statement, parameters);
                resultSet = statement.executeQuery();
            } else {
                Statement statement = connection.createStatement();
                resultSet = statement.executeQuery(sql);
            }
            while (resultSet.next()) {
                WorklineBean sqlBean = new WorklineBean();
                String worklineName = resultSet.getString("name");
                sqlBean.setName(worklineName);
                sqlLists.add(sqlBean);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("len", "SQL:" + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return sqlLists;
    }

    public static ArrayList<VoiceBean> connectVoiceSQL(String sql, Parameters parameters) {
        ArrayList<VoiceBean> sqlLists = new ArrayList<>();
//        String sql = "exec fm_get_kanban_by_mac_address ?";
        Connection connection = null;
        try {
            Class.forName(driverName);   //加载驱动
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("len", "CLASSNOTFOUND:" + e.getMessage());
        }
        try {
            connection = DriverManager.getConnection(url, userName, password);
            ResultSet resultSet;

            if (parameters != null) {
                PreparedStatement statement = connection.prepareStatement(sql);
                PrepareParameters(statement, parameters);
                resultSet = statement.executeQuery();
            } else {
                Statement statement = connection.createStatement();
                resultSet = statement.executeQuery(sql);
            }
            while (resultSet.next()) {
                VoiceBean sqlBean = new VoiceBean();
                String fullName = resultSet.getString("full_name");
                String planDate = resultSet.getString("plan_date");
                int qty = resultSet.getInt("qty");
                sqlBean.setQty(qty);
                sqlBean.setFull_name(fullName);
                sqlBean.setPlan_date(planDate);
                sqlLists.add(sqlBean);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("len", "SQL:" + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return sqlLists;
    }


    public static ArrayList<SQLBean> connectSQL(String sql, Parameters parameters) {
        ArrayList<SQLBean> sqlLists = new ArrayList<>();
//        String sql = "exec fm_get_kanban_by_mac_address ?";
        Connection connection = null;
        try {
            Class.forName(driverName);   //加载驱动
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("len", "CLASSNOTFOUND:" + e.getMessage());
        }
        try {
            connection = DriverManager.getConnection(url, userName, password);
            ResultSet resultSet;

            if (parameters != null) {
                PreparedStatement statement = connection.prepareStatement(sql);
                PrepareParameters(statement, parameters);
                resultSet = statement.executeQuery();
            } else {
                Statement statement = connection.createStatement();
                resultSet = statement.executeQuery(sql);
            }
            while (resultSet.next()) {
                SQLBean sqlBean = new SQLBean();
                String url = resultSet.getString("url");
                String location = resultSet.getString("location");
                String workshop = resultSet.getString("workshop");
                String mac_address = resultSet.getString("mac_address");
                String hasVoice = resultSet.getString("has_voice");
                String voice_procedure = resultSet.getString("voice_procedure");
                int updateTime = resultSet.getInt("update_time");
                int has_hik_version = resultSet.getInt("has_hik_version");
                Log.e("len", "URL ; " + url);
                sqlBean.setUrl(url);
                sqlBean.setLocation(location);
                sqlBean.setWork_shop(workshop);
                sqlBean.setUpdate_time(updateTime);
                sqlBean.setMac_address(mac_address);
                sqlBean.setHas_voice(hasVoice);
                sqlBean.setVoice_procedure(voice_procedure);
                sqlBean.setHas_hik_version(has_hik_version);
                sqlLists.add(sqlBean);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("len", "SQL:" + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        Log.e("len", sqlLists.size() + "**" );
        return sqlLists;
    }

    public void connectSQLAsync(final String sql, final Parameters parameters, final ResultHandler<ArrayList<SQLBean>> handler) {
        new Thread() {
            @Override
            public void run() {
                ArrayList<SQLBean> sqlBeans = connectSQL(sql, parameters);
                if (handler != null) {
                    handler.Value = sqlBeans;
                    handler.sendEmptyMessage(0);
                }
            }
        }.start();
    }

    private static void PrepareParameters(PreparedStatement statement, Parameters parameters) throws SQLException {
        Iterator<Map.Entry<Object, Object>> iterator = parameters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> entry = iterator.next();
            statement.setObject((Integer) entry.getKey(), entry.getValue());
        }
    }
}
