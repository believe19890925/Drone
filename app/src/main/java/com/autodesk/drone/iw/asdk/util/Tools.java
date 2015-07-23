package com.autodesk.drone.iw.asdk.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {

    /**
     * 关闭虚拟键盘
     */
    public static void CloseMethod(Context context, EditText myEditText) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
    }

    public static void closeVirtual(Context context) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive())
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                    InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 对邮箱格式的验证
     */
    public static boolean isEmail(String strEmail) {// ^[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$
        strEmail = strEmail.trim();// 去除前后空格
        //String strPattern = "^[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?$";
        //String strPattern = "[A-Z0-9a-z._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";

        //andy.zhao modify here not allowed "%" 20140116
        String strPattern = "[A-Z0-9a-z._-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";

        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

    // 判断是否为中文
    public static boolean isCN(String str) {
        try {
            byte[] bytes = str.getBytes("UTF-8");
            if (bytes.length == str.length()) {
                return false;
            } else {
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    // 判断是否为空格
    public static boolean isBlank(String str) {
        boolean mat = str.matches("^(\\s|.*\\s+.*)$");
        if (mat) {
            return true;
        } else {
            return false;
        }
    }

    // 判断是否为十六进制数据
    public static boolean isHexString(String str) {
        String strPattern = "[A-F0-9a-f]*";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 对Repeater SSID的验证
     */
    public static boolean RepeaterSsidCheck(String str) {
        String strPattern = "[A-Z0-9a-z_]*";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 对Repeater Password的验证
     */
    public static boolean RepeaterPasswordCheck(String str) {
        String strPattern = "[A-Z0-9a-z]*";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * @param passwordString
     * @return false表示不合格 主要检查不能全部为一样的数字或字母 长度不能小于6
     */
    public static boolean PasswordStrengthCheck(String passwordString) {
        // boolean isStreng = true;
        if (passwordString.length() < 6) {
            return false;
        }

        if (passwordString.contains(" ") || passwordString.contains("&") || passwordString.contains("+")) {
            return false;
        }

        // // 包含字母，数字和特殊字符， 不能是纯字母或是纯字符
        // String strPattern = "^.*\\d+.*[a-zA-z]+.*$";
        // Pattern p = Pattern.compile(strPattern);
        // Matcher m = p.matcher(passwordString);
        // return m.matches();
        return true;

        // for (int i = 0; i < passwordString.length(); i++) {// 不能连续出现相同的字母、数字
        // String item = passwordString.substring(0, 1);
        // String itemSecond = passwordString.substring(1, 2);
        // if (item.equals(itemSecond)) {
        // return false;
        // }
        // }
        // return isStreng;
    }

    /**
     * 查询是否打开网络
     *
     * @param contex
     * @return true打开/false关闭
     */
    public static boolean QueryOpenNet(Context contex) {
        ConnectivityManager manager = (ConnectivityManager) contex
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        boolean isWifiConnected = false;
        int ConnectNetCnt = 0;
        NetworkInfo[] info;

        info = manager.getAllNetworkInfo();

        if (info != null) {

            for (int i = 0; i < info.length; i++) {

                if (info[i].isConnected()) {
                    Log.d("Tools", "info[i].getTypeName() = " + info[i].getTypeName());
                    ConnectNetCnt++;
                    if (info[i].getTypeName().equals("WIFI"))
                        isWifiConnected = true;
                }
            }
        } else {
            return false;
        }

        //Log.d("Tools","ConnectNetCnt = "+ ConnectNetCnt + ",isWifiConnected ="+isWifiConnected);
        if (ConnectNetCnt == 0) {
            return false;
        } else if (isWifiConnected == true) {
            String SSID = "";
            WifiManager mWifiManager = (WifiManager) contex.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if (wifiInfo == null) {
                //Log.d("Tools","wifiInfo == null");
                return false;
            }

            SSID = wifiInfo.getSSID();
            if (SSID == null) {
                //Log.d("Tools","SSID == null");
                return false;
            } else {
                if (SSID.toLowerCase().startsWith("phantom") || SSID.toLowerCase().startsWith("fc200")) {
                    Log.d("Tools", "Phantom wifi not connect network!!!");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 对文件名的验证 return:true存在该文件/false不存在该文件
     */
    public boolean checkFileExit(String filePath) {
        File fileDir = new File(filePath);
        return fileDir.exists();
    }

    /**
     * 获取当前系统的时间（按照格式：yyyy/MM/dd HH:mm:ss）
     */
    @SuppressLint("SimpleDateFormat")
    public String getSystemData() {
        String time = "";
        Date date = new Date(); // 这个是最后修改时间
        SimpleDateFormat bartDateFormat = new SimpleDateFormat(
                "yyyy/MM/dd HH:mm:ss");

        time = bartDateFormat.format(date);
        return time;
    }

    /**
     * 限制输入框最多能输入多少行
     *
     * @param edit   要限制的输入框
     * @param number 限制的行数
     */
    public static void editTextInoutLimitLine(final EditText edit,
                                              final int number) {
        edit.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
                int lines = edit.getLineCount();
                // 限制最大输入行数
                if (lines > number) {// 最多输入2行
                    String str = s.toString();
                    int cursorStart = edit.getSelectionStart();
                    int cursorEnd = edit.getSelectionEnd();
                    if (cursorStart == cursorEnd && cursorStart < str.length()
                            && cursorStart >= 1) {
                        str = str.substring(0, cursorStart - 1)
                                + str.substring(cursorStart);
                    } else {
                        str = str.substring(0, s.length() - 1);
                    }
                    // setText会触发afterTextChanged的递归
                    edit.setText(str);
                    // setSelection用的索引不能使用str.length()否则会越界
                    edit.setSelection(edit.getText().length());
                }
            }
        });
    }

    /**
     * 判断是否可以输入空格、中文
     *
     * @param edit
     * @param isCN    为true限制输入中文，为false可以输入中文
     * @param isBlank 为true限制输入空格，为false可以输入空格
     */
    public static void EditTextInputLimit(final EditText edit,
                                          final boolean isCN, final boolean isBlank) {

        edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(256),
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start,
                                               int end, Spanned dest, int dstart, int dend) {
                        if (isCN && Tools.isCN(source.toString())) {// 判断是否为中文
                            return "";
                        } else if (isBlank && Tools.isBlank(source.toString())) {// 判断是否为空格
                            return "";
                        } else {
                            return source;
                        }
                    }
                }});
    }

    /**
     * 设置智能输入十六进制
     *
     * @param edit
     */
    public static void EditTextInputHexLimit(final EditText edit, int length) {

        edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(length),
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start,
                                               int end, Spanned dest, int dstart, int dend) {

                        if (isHexString(source.toString())) {
                            return source;
                        } else {
                            return "";
                        }
                    }
                }});
    }

    public static String getFormatDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        return format.format(new Date());
    }
}
