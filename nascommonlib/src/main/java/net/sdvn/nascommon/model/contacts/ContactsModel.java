package net.sdvn.nascommon.model.contacts;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import net.sdvn.cmapi.util.ToastUtil;
import net.sdvn.nascommon.model.UiUtils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class ContactsModel extends ViewModel {
    @NonNull
    private final PinyinComparator pinyinComparator;
    private CharacterParser characterParser;
    public final MutableLiveData<List<SortModel>> mLiveData = new MutableLiveData<>();

    public ContactsModel() {
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
    }

    public void checkContactsPermissions(@NonNull final FragmentActivity context) {
        final String permission;
        permission = Manifest.permission.READ_CONTACTS;
        AndPermission.with(context)
                .runtime()
                .permission(permission)
                .rationale(new Rationale<List<String>>() {
                    @Override
                    public void showRationale(@NonNull Context context, List<String> strings, RequestExecutor requestExecutor) {
                        ToastUtil.showToast(context, context.getString(R.string.permission_denied_contact));
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> strings) {
                        showSettings(context);
                    }
                })
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> strings) {
                        if (AndPermission.hasPermissions(context, permission)) {
                            loadContacts(context);
                        } else {
                            showSettings(context);
                        }
                    }
                })
                .start();

    }

    private void showSettings(final Activity context) {
        UiUtils.showSettings(context, true);
    }

    /**
     * =====================================通讯录相关操作===========================================
     */
    private void loadContacts(@NonNull final FragmentActivity context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ContentResolver resolver = context.getContentResolver();
                    Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER, "sort_key"},
                            null, null, "sort_key COLLATE LOCALIZED ASC");
                    if (phoneCursor == null || phoneCursor.getCount() == 0) {
                        Toast.makeText(context, R.string.tip_no_permission_or_data, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int PHONES_NUMBER_INDEX = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int PHONES_DISPLAY_NAME_INDEX = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int SORT_KEY_INDEX = phoneCursor.getColumnIndex("sort_key");
                    List<SortModel> mAllContactsList = new ArrayList<>();
                    if (phoneCursor.getCount() > 0) {
                        while (phoneCursor.moveToNext()) {
                            String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                            if (TextUtils.isEmpty(phoneNumber))
                                continue;
                            String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                            String sortKey = phoneCursor.getString(SORT_KEY_INDEX);
                            //System.out.println(sortKey);
                            SortModel sortModel = new SortModel(contactName, phoneNumber, sortKey);
                            //优先使用系统sortkey取,取不到再使用工具取
                            String sortLetters = getSortLetterBySortKey(sortKey);
                            if (sortLetters == null) {
                                sortLetters = getSortLetter(contactName);
                            }
                            sortModel.sortLetters = sortLetters;

                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                                sortModel.sortToken = parseSortKey(sortKey);
                            else
                                sortModel.sortToken = parseSortKeyLollipop(sortKey);

                            mAllContactsList.add(sortModel);

                        }
                    }
                    phoneCursor.close();
//                    context.runOnUiThread(new Runnable() {
//                        public void run() {
//                            Collections.sort(mAllContactsList, pinyinComparator);
//                            adapter.updateListView(mAllContactsList);
//                        }
//                    });
                    Collections.sort(mAllContactsList, pinyinComparator);
                    mLiveData.postValue(mAllContactsList);
                } catch (Exception e) {
                    Logger.LOGE("xbc", e.getLocalizedMessage());
                }
            }
        }).start();
    }

    /**
     * 取sort_key的首字母
     *
     * @param sortKey
     * @return
     */
    @Nullable
    private String getSortLetterBySortKey(@Nullable String sortKey) {
        if (sortKey == null || "".equals(sortKey.trim())) {
            return null;
        }
        String letter = "#";
        //汉字转换成拼音
        String sortString = sortKey.trim().substring(0, 1).toUpperCase(Locale.CHINESE);
        // 正则表达式，判断首字母是否是英文字母
        if (sortString.matches("[A-Z]")) {
            letter = sortString.toUpperCase(Locale.CHINESE);
        } else { // 5.0以上需要判断汉字
            if (sortString.matches("^[\u4E00-\u9FFF]+$"))// 正则表达式，判断是否为汉字
                letter = getSortLetter(sortString.toUpperCase(Locale.CHINESE));
        }
        return letter;
    }

    /**
     * 模糊查询
     *
     * @param str
     * @return
     */
    @NonNull
    public CopyOnWriteArrayList<SortModel> search(@NonNull String str) {
        CopyOnWriteArrayList<SortModel> filterList = new CopyOnWriteArrayList<>();// 过滤后的list
        //if (str.matches("^([0-9]|[/+])*$")) {// 正则表达式 匹配号码
        List<SortModel> modelList = mLiveData.getValue();
        if (modelList != null)
            if (str.matches("^([0-9]|[/+]).*")) {// 正则表达式 匹配以数字或者加号开头的字符串(包括了带空格及-分割的号码)
                String simpleStr = str.replaceAll("-|\\s", "");

                for (SortModel contact : modelList) {
                    if (contact.number != null && contact.name != null) {
                        if (contact.simpleNumber.contains(simpleStr) || contact.name.contains(str)) {
                            if (!filterList.contains(contact)) {
                                filterList.add(contact);
                            }
                        }
                    }
                }
            } else {
                for (SortModel contact : modelList) {
                    if (contact.number != null && contact.name != null) {
                        //姓名全匹配,姓名首字母简拼匹配,姓名全字母匹配
                        boolean isNameContains = contact.name.toLowerCase(Locale.CHINESE)
                                .contains(str.toLowerCase(Locale.CHINESE));

                        boolean isSortKeyContains = contact.sortKey.toLowerCase(Locale.CHINESE).replace(" ", "")
                                .contains(str.toLowerCase(Locale.CHINESE));

                        boolean isSimpleSpellContains = contact.sortToken.simpleSpell.toLowerCase(Locale.CHINESE)
                                .contains(str.toLowerCase(Locale.CHINESE));

                        boolean isWholeSpellContains = contact.sortToken.wholeSpell.toLowerCase(Locale.CHINESE)
                                .contains(str.toLowerCase(Locale.CHINESE));

                        if (isNameContains || isSortKeyContains || isSimpleSpellContains || isWholeSpellContains) {
                            if (!filterList.contains(contact)) {
                                filterList.add(contact);
                            }
                        }
                    }
                }
            }
        return filterList;
    }

    /**
     * 名字转拼音,取首字母
     *
     * @param name
     * @return
     */
    @NonNull
    private String getSortLetter(@Nullable String name) {
        String letter = "#";
        if (name == null) {
            return letter;
        }
        //汉字转换成拼音
        String pinyin = characterParser.getSelling(name);
        String sortString = pinyin.substring(0, 1).toUpperCase(Locale.CHINESE);

        // 正则表达式，判断首字母是否是英文字母
        if (sortString.matches("[A-Z]")) {
            letter = sortString.toUpperCase(Locale.CHINESE);
        }
        return letter;
    }

    /**
     * 中文字符串匹配
     */
    @NonNull
    String chReg = "[\\u4E00-\\u9FA5]+";

    /**
     * 解析sort_key,封装简拼,全拼
     *
     * @param sortKey
     * @return
     */
    @NonNull
    public SortToken parseSortKey(@Nullable String sortKey) {
        SortToken token = new SortToken();
        if (sortKey != null && sortKey.length() > 0) {
            //其中包含的中文字符
            String[] enStrs = sortKey.replace(" ", "").split(chReg);
            for (String enStr : enStrs) {
                if (enStr.length() > 0) {
                    //拼接简拼
                    token.simpleSpell += enStr.charAt(0);
                    token.wholeSpell += enStr;
                }
            }
        }
        return token;
    }

    /**
     * 解析sort_key,封装简拼,全拼。
     * Android 5.0 以上使用
     *
     * @param sortKey
     * @return
     */
    @NonNull
    public SortToken parseSortKeyLollipop(@Nullable String sortKey) {
        SortToken token = new SortToken();
        if (sortKey != null && sortKey.length() > 0) {
            boolean isChinese = sortKey.matches(chReg);
            // 分割条件：中文不分割，英文以大写和空格分割
            String regularExpression = isChinese ? "" : "(?=[A-Z])|\\s";

            String[] enStrs = sortKey.split(regularExpression);

            for (String enStr : enStrs)
                if (enStr.length() > 0) {
                    //拼接简拼
                    token.simpleSpell += getSortLetter(String.valueOf(enStr.charAt(0)));
                    token.wholeSpell += characterParser.getSelling(enStr);
                }
        }
        return token;
    }
}
