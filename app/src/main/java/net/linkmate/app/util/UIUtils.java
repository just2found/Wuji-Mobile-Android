package net.linkmate.app.util;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.linkmate.app.R;
import net.linkmate.app.base.MyApplication;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.SPHelper;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

//import net.linkmate.app.activities.NewWebActivity;
//import net.linkmate.app.activities.WebActivity;

public class UIUtils {
    private static long lastClickTime;
    private static String fastClickName = "";


    public static boolean isFastClick(String name) {
        long currentTime = SystemClock.uptimeMillis();
        if (!TextUtils.isEmpty(name) && !fastClickName.equals(name)) {
            fastClickName = name;
            lastClickTime = currentTime;
            return false;
        }
        long lagTime = currentTime - lastClickTime;
        if (lagTime > 0 && lagTime < 800)
            return true;
        lastClickTime = currentTime;
        return false;
    }

    public static boolean isEn() {
        return Locale.getDefault() != null && Objects.equals(Locale.getDefault().getDisplayLanguage(), Locale.ENGLISH.getDisplayLanguage());
    }

    /**
     * 将字符串中的中文转化为拼音,其他字符不变
     *
     * @param inputString
     * @return
     */

//    public static String getPingYin(String inputString) {
//        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
//        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
//        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//        format.setVCharType(HanyuPinyinVCharType.WITH_V);
//        char[] input = inputString.trim().toCharArray();
//        String output = "";
//        try {
//            for (char curchar : input) {
//                if (Character.toString(curchar).matches(
//                        "[\\u4E00-\\u9FA5]+")) {
//                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(
//                            curchar, format);
//                    output += temp[0];
//                } else {
//                    output += Character.toString(curchar);
//                }
//            }
//        } catch (BadHanyuPinyinOutputFormatCombination e) {
//            e.printStackTrace();
//        }
//        return output;
//    }


    /**
     * 汉字转换为汉语拼音首字母，英文字符不变
     * <p>
     * param chinese 汉字
     *
     * @return 拼音
     *//*


	public static String getFirstSpell(String chinese) {
		StringBuffer pybf = new StringBuffer();
		char[] arr = chinese.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (char curchar : arr) {
			if (curchar > 128) {
				try {

					String[] temp = PinyinHelper.toHanyuPinyinStringArray(curchar, defaultFormat);
					if (temp != null) {
						pybf.append(temp[0].charAt(0));

					}

				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();

				}

			} else {
				pybf.append(curchar);

			}

		}
		return pybf.toString().replaceAll("\\W", "").trim();

	}

*/
    public static boolean contain2(String input, String regex) {
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(input);
        boolean result = m.find();
        return result;
    }

    /**
     * 设置搜索关键字高亮
     *
     * @param content 原文本内容
     * @param keyword 关键字
     */
    public static SpannableString setKeyWordColor(String content, String keyword) {
        SpannableString s = new SpannableString(content);
        Pattern p = Pattern.compile(keyword);
        Matcher m = p.matcher(s);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            s.setSpan(new ForegroundColorSpan(MyApplication.getContext().getResources()
                            .getColor(R.color.color_green_dark)),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return s;
    }

    /**
     * 搜索关键字标红
     *
     * @param content
     * @param keyword
     * @return
     */
    public static SpannableString setKeyWordColorI(String content, String keyword) {
        SpannableString s = new SpannableString(content);
        String wordReg = "(?i)" + keyword;//用(?i)来忽略大小写
        StringBuffer sb = new StringBuffer();
        Matcher matcher = Pattern.compile(wordReg).matcher(content);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            s.setSpan(new ForegroundColorSpan(MyApplication.getContext().getResources()
                            .getColor(R.color.color_green_dark)),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        matcher.appendTail(sb);
        content = sb.toString();
//		LogUtils.i( "Utils", content );
        //如果匹配和替换都忽略大小写,则可以用以下方法
        //content = content.replaceAll(wordReg,"<font color=\"#ff0014\">"+keyword+"</font>");
//		LogUtils.i( "Utils", content );
        return s;
    }


    /**
     * 搜索关键字标红
     *
     * @param title
     * @param keyword
     * @return
     */
    public static String matcherSearchTitle(String title, String keyword) {
        String content = title;
        String wordReg = "(?i)" + keyword;//用(?i)来忽略大小写
        StringBuffer sb = new StringBuffer();
        Matcher matcher = Pattern.compile(wordReg).matcher(content);
        while (matcher.find()) {
            //这样保证了原文的大小写没有发生变化
            matcher.appendReplacement(sb, "<font color=\"#ff179b16\">" + matcher.group() + "</font>");
        }
        matcher.appendTail(sb);
        content = sb.toString();
//		LogUtils.i("UIUtils",  content );
        //如果匹配和替换都忽略大小写,则可以用以下方法
        //content = content.replaceAll(wordReg,"<font color=\"#ff0014\">"+keyword+"</font>");
//		LogUtils.i("UIUtils",  content );
        return content;
    }

    public static int getSreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

//
//    public static void jumpToBrows(Activity context, String title, String url, boolean isConnected, int enableScript, boolean hasFullTitle) {
//        Intent intent = new Intent(context, WebActivity.class);
//        intent.putExtra("url", url);
//        intent.putExtra("title", title);
//        intent.putExtra("ConnectionState", isConnected);
//        intent.putExtra("enableScript", enableScript);
//        intent.putExtra("hasFullTitle",hasFullTitle);
//        intent.putExtra("sllType","app");
//        context.startActivity(intent);
//        context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
//    }
//
//
//    public static void jumpToBrowsByPost(Activity context, String title, String url, boolean isConnected, int enableScript, boolean hasFullTitle) {
//        Intent intent = new Intent(context, NewWebActivity.class);
//        intent.putExtra("url", url);
//        intent.putExtra("title", title);
//        intent.putExtra("ConnectionState", isConnected);
//        intent.putExtra("enableScript", enableScript);
//        intent.putExtra("hasFullTitle",hasFullTitle);
//        intent.putExtra("sllType","app");
//        intent.putExtra("post",true);
//        context.startActivity(intent);
//        context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
//    }
//
//    public static void jumpToBrows(Activity context, String title, String url) {
//        Intent intent = new Intent(context, WebActivity.class);
//        intent.putExtra("url", url);
//        intent.putExtra("title", title);
//        intent.putExtra("ConnectionState", true);
//        intent.putExtra("enableScript", 0);
//        context.startActivity(intent);
//        context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
//    }
//
//    public static void jumpToBrows(Activity context, String title, String url, boolean enableScript) {
//        Intent intent = new Intent(context, WebActivity.class);
//        intent.putExtra("url", url);
//        intent.putExtra("title", title);
//        intent.putExtra("ConnectionState", false);
//        intent.putExtra("enableScript", enableScript ? 1 : 0);
//        intent.putExtra("sllType","home");
//        context.startActivity(intent);
//        context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
//    }


    public static void doAnimationDown(View view) {
        final RotateAnimation animation = new RotateAnimation(-180f, 0f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setDuration(200);//设置动画持续时间
        animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        view.startAnimation(animation);
    }

    public static void doAnimationUp(View view) {
        final RotateAnimation animation = new RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setDuration(200);//设置动画持续时间
        animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        view.startAnimation(animation);
    }

    public static void doAnimationDown(View view, float fromDegrees, float toDegrees) {
        final RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(300);//设置动画持续时间
        animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        view.startAnimation(animation);
    }

    public static void doAnimationUp(View view, float fromDegrees, float toDegrees) {
        final RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(300);//设置动画持续时间
        animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        view.startAnimation(animation);
    }

    public static void doAlphaAnimation(View view, float fromAlpha, float toAlpha) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        alphaAnimation.setInterpolator(new AccelerateInterpolator());
        alphaAnimation.setDuration(300);
        alphaAnimation.setFillAfter(true);
        view.startAnimation(alphaAnimation);
    }


    public static void swapActivity_rightIn_leftOut(Activity context, Class cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }

    public static void swapActivity_rightIn_leftOut(Activity context, Intent intent) {
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }

    public static void swapActivity_rightIn_leftOut(Activity context, Class cls, int requestCode) {
        Intent intent = new Intent(context, cls);
        context.startActivityForResult(intent, requestCode);
        context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }

    public static void doAnimation(View view, float fromArg, float toArg, int duration) {
        final RotateAnimation animation = new RotateAnimation(fromArg, toArg, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);//设置动画持续时间
        animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        view.startAnimation(animation);
    }


    public static Context getContext() {
        return MyApplication.getContext();
    }

    public static String getStrByResId(int resId) {
        return getContext().getResources().getString(resId);
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    /**
     * 动态设置Activity背景透明度
     *
     * @param isopen
     */
    public static void setWindowAlpa(Activity activity, final boolean isopen) {
        if (Build.VERSION.SDK_INT < 11) {
            return;
        }
        final Window window = activity.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        ValueAnimator animator;
        if (isopen) {
            animator = ValueAnimator.ofFloat(1.0f, 0.6f);
        } else {
            animator = ValueAnimator.ofFloat(0.6f, 1.0f);
        }
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                lp.alpha = alpha;
                window.setAttributes(lp);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isopen) {
                    WindowManager.LayoutParams lp = window.getAttributes();
                    window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    window.setAttributes(lp);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    public static int getStatueBarHeight(Context activity) {
        int identifier = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) {
            return (int) activity.getResources().getDimension(identifier);
        }
        return 0;
    }

    public static @Nullable
    Activity getActivity(@NonNull View view) {
        return getActivity(view, Activity.class);
    }

    public static @Nullable
    <A extends Activity> A getActivity(@NonNull View view, @NotNull Class<A> type) {
        for (Context context = view.getContext(); ; ) {
            if (type.isInstance(context)) {
                return type.cast(context);
            } else if (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
            } else {
                Timber.e("Activity not found for: %s", view);
                return null;
            }
        }
    }

    public static boolean isNotCareFlow() {
        boolean wifi = NetworkUtils.isWifi(UIUtils.getContext());
        if (wifi) {
            return true;
        }
        boolean b = SPHelper.get(AppConstants.SP_FIELD_ONLY_WIFI_CARE, true);
        return !b;
    }

    public static String getProcessNameApi(@NotNull Application app) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return app.getProcessName();
        } else {
            BufferedReader reader = null;
            try {
                File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
                reader = new BufferedReader(new FileReader(file));
                String processName = reader.readLine().trim();
                reader.close();
                return processName;
            } catch (Exception e) {
                Timber.e(e);
                return null;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }
            }
        }
    }

    /**
     * @param view      visible Icon
     * @param editTexts special of first
     */
    public static void togglePasswordStatus(@NonNull View view, @NonNull EditText... editTexts) {
        boolean isPasswordTransformationMethod = PasswordTransformationMethod.getInstance().equals(editTexts[0].getTransformationMethod());
        TransformationMethod newType = isPasswordTransformationMethod ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance();
        for (EditText text : editTexts) {
            text.setTransformationMethod(newType);
            if (text.isFocused()) {
                String pwd = text.getText().toString().trim();
                text.setSelection(pwd.length());
            }
        }
        view.setSelected(isPasswordTransformationMethod);
    }
}
