package com.JRead;

import static com.JRead.SPUtils.get;
import static com.JRead.SPUtils.put;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.JRead.ui.HomeViewModel;

public class TextUtils {
    public static void setArticleUI(TextView text, String str, boolean blankLine) {
        setFormattedText(text, str, SPUtils.get("lb_size", 7),blankLine);

        int size = SPUtils.get("font_size", 19);
        float letterSpace= SPUtils.get("letter_space", 0.2f);
        int lineSpace = SPUtils.get("line_space", 16);
        int marginH = SPUtils.get("margin_horizontal", 0);

        text.setTextSize(size);
        text.setLineSpacing(lineSpace, 1);
        text.setLetterSpacing(letterSpace);
        setMargin(text,marginH);

        // 适配仓耳字库，\u2003 100% ! 但是安卓默认字体 总是大一点！
        float y = text.getPaint().measureText("\u3000"); // \u3000 全角中文空格 7/12 ; \u2002 半角空格 7/12
        float z = text.getPaint().measureText("正"); // \u0020 标准四分之一空格 3/8 ; \u00A0 不换行空格 3/8 ; \u0009 制表符 3/8 ; \u2002 半角空格 3/8
        //Log.e(valueOf(y), valueOf(z));
        if( y == z*7/12 ) setFormattedText(text,str.replace("\u3000\u3000", "\u2003\u2003"), SPUtils.get("lb_size", 7), blankLine);

    }

    // 设置水平边距
    public static void setMargin(TextView text,int margin) {
        if (margin % 2 == 0) {
            text.setPadding(margin / 2, 0, margin / 2, 0);
        } else {
            text.setPadding((margin + 1) / 2, 0, (margin - 1) / 2, 0);
        }
    }
    // 计算水平边距
    public static int getMargin(TextView text,int x) {

        int y = text.getMeasuredWidth();
        int z = (int) text.getPaint().measureText("正");

        return y % z + z * x;
    }
    // 刷新水平边距
    public static void renewMargin(TextView text) {
        SPUtils.put("margin_horizontal", getMargin(text, SPUtils.get("margin_hx", 1)));
        setMargin(text, SPUtils.get("margin_horizontal", 0));
    }
    // 设置段间距
    private static void setFormattedText(TextView text,String formattedText, int lineBreakSize, boolean blankLine) {
        Spannable spannableString = new SpannableString(formattedText);
        shortenBlankSpace(spannableString,lineBreakSize,blankLine);
        Matcher matcher = Pattern.compile("\n{2}.{1,20}\n{3}").matcher(spannableString);
        while (matcher.find()) {
            Log.i("home string match compile ","yes");
            spannableString.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                    matcher.start()+2,matcher.end()-3,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        text.setText(spannableString);
    }
    // 缩短空行
    public static void shortenBlankSpace(Spannable spannableString, int lineBreakSize, boolean blankLine) {
        if (blankLine) {
            Matcher matcher = Pattern.compile(".\n\n.").matcher(spannableString);
            while (matcher.find()) {
                spannableString.setSpan(new AbsoluteSizeSpan(lineBreakSize, true),
                        matcher.start() + 2, matcher.end()-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    public static FragmentResultListener listener(TextView text,String key, int i, float init, float multi, int type, boolean editor) {
        return (requestKey, result) -> {
            boolean click = result.getBoolean(key+i);
            Log.i("HomeFragment receive pop ui click", key+i);
            if (click) if (type> 3){
                if(type== 4) {
                    SPUtils.put(key, (init+ i *multi));
                    text.setLetterSpacing(SPUtils.get("letter_space", 0.1f));
                    renewMargin(text);
                } else {
                    SPUtils.put(key, getMargin(text,(int) (init+ i *multi)));
                    SPUtils.put("margin_hx", i);
                    setMargin(text, SPUtils.get("margin_horizontal", 0));
                }
            } else {
                SPUtils.put(key, (int) (init+ i *multi));
                if(type== 1){
                    text.setTextSize(SPUtils.get("font_size", 19));
                    renewMargin(text);
                } else if(type== 2) text.setLineSpacing(SPUtils.get("line_space", 16), 1);
                else {
                    int initAll = - ( SPUtils.get1(Set.SOURCE_INDEX,-1) +1 )*50;
                    if (editor) setArticleUI(text, SPUtils.get("text_main", ""), SPUtils.get("blank_line",true)); // type == 3
                    else setArticleUI(text, SPUtils.get1(initAll, ""), true);
                }
            }
        };
    }

    public static FragmentResultListener listener6(Activity activity,String key, int i) {
        return (requestKey, result) -> {
            boolean click = result.getBoolean(key + i);
            if (click) {
                int resID = activity.getResources().getIdentifier("Theme.阅" + (i + 1), "style", activity.getPackageName());
                SPUtils.put(key, resID);
                activity.setTheme(SPUtils.get("theme", R.style.Theme_阅1));
                HomeViewModel model = new ViewModelProvider((ViewModelStoreOwner) activity).get(HomeViewModel.class);
                model.select(Set.POPUP_UI_DISMISS);
                new Handler().postDelayed(() -> {
                    final Intent intent = new Intent(activity, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                    activity.finish();
                }, 300);
            }
        };
    }
}
