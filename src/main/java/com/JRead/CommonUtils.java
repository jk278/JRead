package com.JRead;

import static java.lang.String.valueOf;
import static com.JRead.SPUtils.get;
import static com.JRead.SPUtils.put;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;

import androidx.core.widget.NestedScrollView;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;

import com.JRead.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/*
 * 通用工具包
 */
public class CommonUtils {

    public static List<String> getTitleList() {
        List<String> list = new ArrayList<>();
        int i = SPUtils.get1( SPUtils.get1(Set.SOURCE_INDEX,-1), 2); // i 已经加一了
        int j = 0, amount = SPUtils.get1(Set.SHOWED_AMOUNT,10);
        for(; i > 1; i--,j++) {
            String s = SPUtils.get1(i-1+ Set.init(), "");
            list.add( s.substring(0,s.indexOf("-split-")) );
        }
        if(!SPUtils.get1(amount, "").equals("")) { // 不加括号容易搞忘，不是好习惯！
            i += Set.MAX_AMOUNT;
            for(; j < amount; i--,j++) {
                String s = SPUtils.get1(i-1+ Set.init(), "");
                list.add( s.substring(0,s.indexOf("-split-")) );
            }
        }
        return list;
    }
    public static List<Integer> getSourceIdList() {
        List<Integer> list = new ArrayList<>();
        int x = SPUtils.get1(Set.SOURCE_AMOUNT, 2);
        int j = 0, i = Set.ID_FORM;
        for(; j < x; i--,j++) list.add(SPUtils.get1(i, -1));
        Log.i("getSourceIdList---",list.toString());
        return list;
    }
    public static List<String> getSourceNameList(List<Integer> mList) {
        List<String> list = new ArrayList<>();
        mList.stream().mapToInt(x -> x).forEach(i ->list.add(SPUtils.get1(i-50,"")));
        Log.i("getSourceNameList---",list.toString());
        return list;
    }
    public static List<Integer> getEditorIdList() {
        List<Integer> list = new ArrayList<>();
        // 列表项数
        // 储存的列表顺序
        String str = SPUtils.get("editor_order","");
        Log.d("getEditorIdList str---",str);
        String[] strings;
        if (!str.equals("")) {
            strings = str.split(",");
        } else {
            Log.e("getEditorIdList---","list 为空！");
            return null;
        }
        // 循环加入顺序列表
        for (int i=strings.length-1; i>-1; i-- ) {
            list.add(Integer.valueOf(strings[i]));
        }
        Log.e("getEditorIdList---",list.toString());
        return list;
    }
    public static List<String> getEditorNameList(List<Integer> mList) {
        List<String> list = new ArrayList<>();
        // 名字（标题）与表内容的缓存号隔 10000
        mList.stream().mapToInt(x -> x).forEach(i ->list.add(SPUtils.get1(i- Set.EDITOR_MAX,"")));
        Log.e("getEditorNameList---",list.toString());
        return list;
    }

    // 顺序存
    public static void putCacheTogether(int id, String text, String title, boolean order) {
        if (order) SPUtils.put("editor_order", SPUtils.get("editor_order","")+(id) +",");
        SPUtils.put1( id, text); // -1001 放正文
        if (!title.equals("")) {
            SPUtils.put1( id- Set.EDITOR_MAX, title);
            Log.e("putCacheTogether",title+ "    "+(id- Set.EDITOR_MAX));
        } else {
            SPUtils.put1( id- Set.EDITOR_MAX, text.substring(0,10)+"...");
        }
    }

    public static void setPopupWindow(PopupWindow popupWindow, View layout, Window window) {
        int alphaDownTime= 2, alphaUpTime= 3;
        float downUnit= 0.005f, upUnit= 0.005f;

        popupWindow.setContentView(layout);
        popupWindow.setAnimationStyle(R.style.popupWindowAnim);
        popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
        new AlphaUtils(window, true, alphaDownTime, downUnit);
        // dismiss 要在 show 后面，之前弹窗不关闭是模拟器的问题
        popupWindow.setOnDismissListener(() -> new AlphaUtils(window,false, alphaUpTime, upUnit));

    }

    public static String getTextFromWebsite(String website,String rule, boolean renew, boolean html) {
        String[][] stringsArray = getStringsArrayFromRule(rule);
        if (stringsArray==null) return "";

        StringBuilder sff = new StringBuilder();
        String string, str1, str2 = "";
        try {
            Document doc = Jsoup.connect(website).get();
            Elements elements = doc.select("*");
            // 正文
            for (Element element : getElements(elements,stringsArray,0))
                sff.append("\u3000\u3000").append(getHtml(element,html)).append("\n\n");
            string =sff.toString();
            // 标题
            str1 = getHtml( getElements(elements,stringsArray,2), html);
            // 作者
            if (!stringsArray[4][0].equals(""))
                str2 = getHtml(getElements(elements,stringsArray,4), html);
        } catch (Exception e) {
            if(renew) {
                string = "\n\n\n\n\u3000\u3000:-) 更新暂未成功，\n\u3000\u3000请检查您的网络，\n\n\u3000" + e.getMessage() + ".";
                int i = (int)( 0+ Math.random()*6);
                str1 = Set.titleStrings[i];
            }
            else {
                string = "\n\u3000\u3000\u3000:-) 加载暂未成功，\n\u3000\u3000请检查输入是否正确。\n\n\u3000" + e.getMessage() + ".";
                str1 = "无（标题不可为空！）";
            }
            Log.e("解析异常", e.toString()); // 与 e.getMessage() 不一样？
        }
        if (!str2.equals("")) string = "\n\n" + str2 + "\n\n\n" + string;
        string = str1+"-split-"+string;
        return string;
    }
    private static Elements getElements(Elements elements, String[][] stringsArray, int order) {
        for(String s: Objects.requireNonNull(stringsArray)[order]) {
            s = s.replaceAll("-add-","");
            if (!s.equals("")) elements = elements.select(s);
            Log.v("getElements order----------",valueOf(order));
            Log.v("getElements select(s) s----------",s);
            Log.v("getElements elements----------",elements.html());
        } for (String s:stringsArray[order+1]) {
            s = s.replaceAll("-add-","");
            if (!s.equals("")) elements = elements.not(s);
            Log.v("getElements order----------",valueOf(order+1));
            Log.v("getElements not(s) s----------",s);
            Log.v("getElements elements----------",elements.html());
        }
        return elements;
    }
    private static String getHtml(Element element, boolean html) {
        if (html) return element.html();
        else return element.text();
    }
    private static String getHtml(Elements elements, boolean html) {
        if (html) return elements.html();
        else return elements.text();
    }

    public static String[][] getStringsArrayFromRule(String rule) {
        String[] strings = rule.replaceAll(";","-add-;-add-").split(";");
        if( strings.length!=6) return null;
        String[][] stringsArray = new String[][]{{},{},{},{},{},{}};
        for(int i=0; i<6; i++) {
            try{
                stringsArray[i]= strings[i].replaceAll(",","-add-,-add-").split(",");
            } catch (Exception d) {
                stringsArray[i] = new String[]{""};
            }
        } // 正文；标题；作者
        return stringsArray;
    }

    public static Bitmap getBitmap(String path) throws IOException{
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(3000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode()==200) {
            InputStream inputStream = conn.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options(); //压缩
            options.inJustDecodeBounds = false; //
            options.inSampleSize = 2; //
            return BitmapFactory.decodeStream(inputStream, null, options);
        }
        return null;
    }
    public static String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    }
    public  static Bitmap stringToBitmap(String s) {
        byte[] decode = Base64.getDecoder().decode(s.getBytes());
        return BitmapFactory.decodeByteArray(decode,0,decode.length);
    }

    public static boolean mismatchStrings(String s, String[] strings) {
        for (String string : strings)
            if (s.equals(string))
                return false;
        return true;
    }

    public static void hideKeyboard(Activity activity) {
        View v = activity.getWindow().peekDecorView();
        if( v != null ) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE); // 直接括号是整体
            imm.hideSoftInputFromWindow(v.getWindowToken(),0);
        }
    }
    public static void showKeyboard(Activity activity,View view) {
        //View v = activity.getWindow().peekDecorView();
        if( view.requestFocus() ) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE); // 直接括号是整体
            imm.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /*
    public static int getDecorViewHeight(Activity activity) {
        Rect r= new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        return r.height();
    }

    public static int getScreenHeight(Activity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        return screenHeight;
    }
     */

    public static void openFab(FloatingActionButton fab, FloatingActionButton fab2, FloatingActionButton fab3,
                               Float currentR, Float currentY2, Float currentY3, Float currentE2, Float currentE3,
                               FloatingActionButton fabItem1, FloatingActionButton fabItem2, Float currentX1, Float currentX2) {
        int time=350, distance2=200, distance3=400;
        float animBack= 1.2f;
        ObjectAnimator.ofFloat(fab,"rotation",currentR,180).setDuration(time).start();
        ObjectAnimator.ofFloat(fab2,"translationY",currentY2, -distance2*animBack, -distance2).setDuration( (int)( time*(animBack*2-1) ) ).start();
        ObjectAnimator.ofFloat(fab3,"translationY",currentY3, -distance3*animBack, -distance3).setDuration( (int)( time*(animBack*2-1) ) ).start();
        ObjectAnimator.ofFloat(fab2,"compatElevation",currentE2,18).setDuration( (int)( time*(animBack*2-1) ) ).start();
        ObjectAnimator.ofFloat(fab3,"compatElevation",currentE3,18).setDuration( (int)( time*(animBack*2-1) ) ).start();
        ObjectAnimator.ofFloat(fabItem1,"translationX",currentX1, -distance2*animBack, -distance2).setDuration( (int)( time*(animBack*2-1) ) ).start();
        ObjectAnimator.ofFloat(fabItem2,"translationX",currentX2, -distance3*animBack, -distance3).setDuration( (int)( time*(animBack*2-1) ) ).start();
        ObjectAnimator.ofFloat(fabItem1,"compatElevation",currentE2,18).setDuration( (int)( time*(animBack*2-1) ) ).start();
        ObjectAnimator.ofFloat(fabItem2,"compatElevation",currentE3,18).setDuration( (int)( time*(animBack*2-1) ) ).start();
    }

    public static void closeFab(FloatingActionButton fab, FloatingActionButton fab2, FloatingActionButton fab3,
                                Float currentR, Float currentY2, Float currentY3, Float currentE2, Float currentE3,
                                FloatingActionButton fabItem1, FloatingActionButton fabItem2, Float currentX1, Float currentX2) {
        int time = 350;
        ObjectAnimator.ofFloat(fab,"rotation",currentR,0).setDuration(time).start();
        ObjectAnimator.ofFloat(fab2,"translationY",currentY2, 0).setDuration(time).start();
        ObjectAnimator.ofFloat(fab3,"translationY",currentY3, 0).setDuration(time).start();
        ObjectAnimator.ofFloat(fab2,"compatElevation",currentE2,0.1f).setDuration(time).start();
        ObjectAnimator.ofFloat(fab3,"compatElevation",currentE3,0).setDuration(time).start();
        ObjectAnimator.ofFloat(fabItem1,"translationX",currentX1, 0).setDuration(time).start();
        ObjectAnimator.ofFloat(fabItem2,"translationX",currentX2, 0).setDuration(time).start();
        ObjectAnimator.ofFloat(fabItem1,"compatElevation",currentE2,0.1f).setDuration(time).start();
        ObjectAnimator.ofFloat(fabItem2,"compatElevation",currentE3,0).setDuration(time).start();
    }

    public static void fabChangeImage(FloatingActionButton fab2, int resId, Float currentE2) {
        int time = 300;
        ObjectAnimator.ofFloat(fab2,"scaleX",0).setDuration(time).start();
        ObjectAnimator.ofFloat(fab2,"scaleY",0).setDuration(time).start();
        ObjectAnimator.ofFloat(fab2,"compatElevation",currentE2,0.1f).setDuration(time).start();
        new Handler().postDelayed(() -> {
            fab2.setImageResource(resId);
            ObjectAnimator.ofFloat(fab2,"scaleX",1).setDuration(time).start();
            ObjectAnimator.ofFloat(fab2,"scaleY",1).setDuration(time).start();
            ObjectAnimator.ofFloat(fab2,"compatElevation",currentE2,18).setDuration(time).start();
        }, 200);
    }

    public static void setNavDestination(NavController navController, int destinationID) {
        NavGraph navGraph = navController.getGraph();
        navGraph.setStartDestination(destinationID);
        navController.setGraph(navGraph);
    }

    public static int getCursorY(EditText editText) {
        int pos = editText.getSelectionStart();
        Layout layout = editText.getLayout();
        int line = layout.getLineForOffset(pos);
        int baseline = layout.getLineBaseline(line);
        int ascent = layout.getLineAscent(line);
        //float x = layout.getPrimaryHorizontal(pos);

        return baseline + ascent;
    }
    /*public static float[] getCursorXY(EditText editText) {
        int pos = editText.getSelectionStart();
        Layout layout = editText.getLayout();
        int line = layout.getLineForOffset(pos);
        int baseline = layout.getLineBaseline(line);
        int ascent = layout.getLineAscent(line);
        float x = layout.getPrimaryHorizontal(pos);
        int y = baseline + ascent;

        return new float[]{x,y};
    }

     */

    public static void editorAutoScroll(NestedScrollView scrollEditor, EditText editor, int keyboardHeight) {
        final int HEADER_SPACE = 483; // 滑动到顶时，editor cursorY 的最小值。editor.getTop+actionBarSize*3 actionbar是ll留的margin

        int requireCursorAwayTop = scrollEditor.getBottom() - keyboardHeight;
        int cursorAndHeader = getCursorY(editor) + HEADER_SPACE;

        int requireScrollY = cursorAndHeader - requireCursorAwayTop;
        new Handler().post(() -> scrollEditor.smoothScrollTo(0,requireScrollY));
    }

}
