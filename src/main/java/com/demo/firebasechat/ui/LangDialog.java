package com.demo.firebasechat.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.firebasechat.R;

import java.util.HashMap;
import java.util.Map;

public abstract class LangDialog extends Dialog {

    private Context mContext;
    private Spinner spinner;

    private Map<String, String> langMap = new HashMap<>();

    public LangDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public LangDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    public LangDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_lang, null);
        setContentView(view);

        DisplayMetrics d = mContext.getResources().getDisplayMetrics();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int) (d.widthPixels * 0.8);
        lp.gravity = Gravity.CENTER;
        getWindow().setAttributes(lp);

        spinner = view.findViewById(R.id.sp_lang);

        langMap.put("English", "en");
        langMap.put("Chinese (Simplified)", "zh-CN");
        langMap.put("French", "fr");
        langMap.put("German", "de");
        langMap.put("Greek", "el");
        langMap.put("Japanese", "ja");
        langMap.put("Russian", "ru");
        langMap.put("Thai", "th");

        String[] keyArray = langMap.keySet().toArray(new String[langMap.size()]);
        ArrayAdapter<String> starAdapter = new ArrayAdapter<String>(mContext, R.layout.layout_lang_item_select, keyArray);
        starAdapter.setDropDownViewResource(R.layout.layout_lang_item_dropdown);
        spinner.setPrompt("please target languages");
        spinner.setAdapter(starAdapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                OnSelectLang(langMap.get(keyArray[i]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        view.findViewById(R.id.confirm).setOnClickListener(view1 -> {
            dismiss();
        });
    }

    public abstract void OnSelectLang(String lang);
}
