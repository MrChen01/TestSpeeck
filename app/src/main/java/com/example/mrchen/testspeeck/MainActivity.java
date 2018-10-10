package com.example.mrchen.testspeeck;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;


public class MainActivity extends AppCompatActivity {
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private Button btn_speech;
    private Button btn_speech_recognizer;
    private TextView tv_show_recording;
    private EditText et_show_input_language;
    private Button btn_read;
    private SpeechSynthesizer mTts;
    private RadioGroup rg_select_language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_speech = findViewById(R.id.btn_speech);
        btn_speech_recognizer = findViewById(R.id.btn_speech_recognizer);
        tv_show_recording = findViewById(R.id.tv_show_recording);
        et_show_input_language= findViewById(R.id.et_show_input_language);
        btn_read= findViewById(R.id.btn_read);
        rg_select_language= findViewById(R.id.rg_select_language);
        //初始化SDK
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5ba25989");
        //动态授权
        applypermission();
        rg_select_language.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //1.创建 SpeechSynthesizer 对象, 第二个参数：本地合成时传 InitListener
                mTts= SpeechSynthesizer.createSynthesizer(MainActivity.this, null);
                switch (checkedId){
                    //语言中文
                    case R.id.rb_zh_cn:
                        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//                设置发音人
                        mTts.setParameter(SpeechConstant.LANGUAGE,"zh_cn");
                        break;
                        //语言美音
                    case  R.id.rb_en_us:
                        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");// 设置发音人
                        break;
                        //语言粤语
                    case R.id.rb_cantonese_yueyu:
                        mTts.setParameter(SpeechConstant.VOICE_NAME,"xiaomei");
                        mTts.setParameter(SpeechConstant.ACCENT,"cantonese");
                        break;
                }
            }
        });
        rg_select_language.check(R.id.rb_zh_cn);
        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.创建 SpeechSynthesizer 对象, 第二个参数：本地合成时传 InitListener
                mTts= SpeechSynthesizer.createSynthesizer(MainActivity.this, null);
                //2.合成参数设置，详见《MSC Reference Manual》SpeechSynthesizer 类
                //设置发音人（更多在线发音人，用户可参见 附录13.2
//                mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//                设置发音人
                mTts.setParameter(SpeechConstant.SPEED, "50");//                 设置语速
                mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围 0~100
                mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
//                String language=getSpeakerLanguage();
//                String accent=getSpeakerAccent();
                //设置语言
//                mTts.setParameter(SpeechConstant.LANGUAGE,"zh_cn");
                //设置发言，口音
//                mTts.setParameter(SpeechConstant.VOICE_NAME,"xiaomei");
//                mTts.setParameter(SpeechConstant.ACCENT,"cantonese");
                //设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm” //保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
                //仅支持保存为 pcm 和 wav 格式，如果不需要保存合成音频，注释该行代码
                Log.i("TAG",getFilesDir().getAbsolutePath()+"/Test.pcm");
                mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,getFilesDir().getAbsolutePath()+"Test.pcm");
//                mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
                //合成监听器
                SynthesizerListener mSynListener = new SynthesizerListener(){
                    //会话结束回调接口，没有错误时，error为null
                    public void onCompleted(SpeechError error) {}
                    //缓冲进度回调
                    //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
                    public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
                        Log.i("TAG","onBufferProgress percent"+percent);
                        Log.i("TAG","onBufferProgress beginPos"+beginPos);
                        Log.i("TAG","onBufferProgress endPos"+endPos);
                        Log.i("TAG","onBufferProgress info"+info);

                    }
                    //开始播放
                    public void onSpeakBegin() {
                        Log.i("TAG","onSpeakBegin");
                    }
                    //暂停播放
                    public void onSpeakPaused() {
                        Log.i("TAG","onSpeakPaused");
                    }
                    //播放进度回调
                    //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
                    public void onSpeakProgress(int percent, int beginPos, int endPos) {
                        Log.i("TAG","onSpeakProgress percent"+percent);
                        Log.i("TAG","onSpeakProgress beginPos"+beginPos);
                        Log.i("TAG","onSpeakProgress endPos"+endPos);
                    }
                    //恢复播放回调接口
                    public void onSpeakResumed() {}
                    //会话事件回调接口
                    public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {

                    }
                };
                //3.开始合成
                mTts.startSpeaking(et_show_input_language.getText().toString(), mSynListener);

            }
        });
        //识别通过Recognizer
        btn_speech_recognizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.创建SpeechRecognizer对象，第二个参数：本地识别时传InitListener
                SpeechRecognizer mIat = SpeechRecognizer.createRecognizer(MainActivity.this, null);
                //2.设置听写参数，详见《MSC Reference Manual》SpeechConstant类
                mIat.setParameter(SpeechConstant.DOMAIN, "iat");
                mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
                //3.开始听写
                //听写监听器
                RecognizerListener mRecoListener = new RecognizerListener() {
                    //听写结果回调接口(返回Json格式结果，用户可参见附录13.1)；
                    //一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
                    //关于解析Json的代码可参见Demo中JsonParser类； //isLast等于true时会话结束。
                    public void onResult(RecognizerResult results, boolean isLast) {
                        Log.d("TAG", "result:" + results.getResultString());
                        String text = JsonParser.parseIatResult(results.getResultString());
//                        Log.i("TAG","recognizerResult 解析结果是："+text.toString());
                        String sn = null;
                        // 读取json结果中的sn字段
                        try {
                            JSONObject resultJson = new JSONObject(results.getResultString());
                            sn = resultJson.optString("sn");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        mIatResults.put(sn, text);

                        StringBuffer resultBuffer = new StringBuffer();
                        for (String key : mIatResults.keySet()) {
                            resultBuffer.append(mIatResults.get(key));
                        }
                        et_show_input_language.setText(resultBuffer.toString());
                        et_show_input_language.setSelection(resultBuffer.length());
                        Log.i("TAG", "" + resultBuffer.toString());

                    }

                    //会话发生错误回调接口
                    public void onError(SpeechError error) {
                        //打印错误码描
                        Log.d("TAG", "error:" + error.getPlainDescription(true));
                    }

                    //开始录音
                    public void onBeginOfSpeech() {
                        Log.i("TAG", "onBeginOfSpeech");
                    }

                    //volume音量值0~30，data音频数据
                    public void onVolumeChanged(int volume, byte[] data) {
                        tv_show_recording.setVisibility(View.VISIBLE);

                    }

                    //结束录音
                    public void onEndOfSpeech() {
                        Log.i("TAG", "onEndOfSpeech");
                        tv_show_recording.setVisibility(View.GONE);
                    }

                    //扩展用接口
                    public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

                    }
                };
                mIat.startListening(mRecoListener);
            }
        });
        //识别通过RecognizerDialog
        btn_speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.创建RecognizerDialog对象
                RecognizerDialog mDialog = new RecognizerDialog(MainActivity.this, new InitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i != ErrorCode.SUCCESS) {
                            Log.i("TAG", "创建RecognizerDialog对象失败");
                        }
                    }
                });
                //2.设置accent、language等参数
                mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
                //若要将UI控件用于语义理解，必须添加以下参数设置，设置之后onResult回调返回将是语义理解
                //结果
                // mDialog.setParameter("asr_sch", "1");
                // mDialog.setParameter("nlp_version", "2.0");

                //3.设置回调接口
                mDialog.setListener(new RecognizerDialogListener() {
                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean b) {
                        Log.i("TAG", recognizerResult.getResultString());
                        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
//                        Log.i("TAG","recognizerResult 解析结果是："+text.toString());
                        String sn = null;
                        // 读取json结果中的sn字段
                        try {
                            JSONObject resultJson = new JSONObject(recognizerResult.getResultString());
                            sn = resultJson.optString("sn");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        mIatResults.put(sn, text);

                        StringBuffer resultBuffer = new StringBuffer();
                        for (String key : mIatResults.keySet()) {
                            resultBuffer.append(mIatResults.get(key));
                        }
                        et_show_input_language.setText(resultBuffer.toString());
                        et_show_input_language.setSelection(resultBuffer.length());
                        Log.i("TAG", "" + resultBuffer.toString());
                    }

                    @Override
                    public void onError(SpeechError speechError) {
                        Log.i("TAG", speechError.getPlainDescription(true));
                    }
                });
                //4.显示dialog，接收语音输入
                mDialog.show();

            }
        });
    }

    private String getSpeakerAccent() {
        return null;
    }

    private String getSpeakerLanguage() {
        String language=null;


        return language;
    }

    public void applypermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //检查是否已经给了权限
            int checkpermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (checkpermission != PackageManager.PERMISSION_GRANTED) {//没有给权限
                Log.e("permission", "动态申请");
                //参数分别是当前活动，权限字符串数组，requestcode
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
                        1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "录音已授权", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "录音拒绝授权", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
