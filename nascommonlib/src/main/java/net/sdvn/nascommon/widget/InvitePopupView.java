package net.sdvn.nascommon.widget;

import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import net.sdvn.nascommon.model.contacts.ContactsModel;
import net.sdvn.nascommon.model.contacts.ContactsSortAdapter;
import net.sdvn.nascommon.model.contacts.InviteCallBack;
import net.sdvn.nascommon.model.contacts.SideBar;
import net.sdvn.nascommon.model.contacts.SortModel;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class InvitePopupView {
    private static final String TAG = InvitePopupView.class.getSimpleName();
    private FragmentActivity context;
    private int resIdTitle;
    private int resIdAction;
    private Dialog thisDialog;

    private ImageView btnAdd;
    private EditText etSearch;
    private ImageView ivClearText;
    private ListView mListView;
    private SideBar sideBar;
    private TextView tvToast;
    private TextView tvTitle;

    private List<SortModel> mAllContactsList;
    private CopyOnWriteArrayList<SortModel> mSearchContactsList;
    @Nullable
    private ContactsSortAdapter adapter;

    //    /**
//     * 汉字转换成拼音的类
//     */
//    private CharacterParser characterParser;
//
//    /**
//     * 根据拼音来排列ListView里面的数据类
//     */
//    private PinyinComparator pinyinComparator;
    private ContactsModel mContactsModel;

    public InvitePopupView(FragmentActivity context, int resIdTitle, int resIdAction) {
        this.context = context;
        this.resIdTitle = resIdTitle;
        this.resIdAction = resIdAction;
        init();
    }


    public InvitePopupView(FragmentActivity context, int resIdTitle) {
        this.context = context;
        this.resIdTitle = resIdTitle;
        init();
    }

    public InvitePopupView(FragmentActivity context) {
        this.context = context;
        init();
    }

    private void init() {
        mAllContactsList = new ArrayList<>();
        mContactsModel = new ContactsModel();
        mContactsModel.mLiveData.observe(this.context, new Observer<List<SortModel>>() {
            @Override
            public void onChanged(@Nullable List<SortModel> sortModels) {
                if (adapter != null && sortModels != null) {
                    if (mAllContactsList != null) {
                        mAllContactsList.clear();
                        mAllContactsList.addAll(sortModels);
                        adapter.updateListView(mAllContactsList);
                    }
                }
            }
        });
        initView();
        initListener();
        mContactsModel.checkContactsPermissions(this.context);
    }

    private void initView() {
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_popup_invite, null);
        btnAdd = view.findViewById(R.id.btn_add);
        tvTitle = view.findViewById(R.id.txt_title);
        if (resIdTitle > 0)
            tvTitle.setText(resIdTitle);
        sideBar = view.findViewById(R.id.sidrbar);
        tvToast = view.findViewById(R.id.dialog);
        sideBar.setTextView(tvToast);
        ivClearText = view.findViewById(R.id.ivClearText);
        etSearch = view.findViewById(R.id.et_search);
        etSearch.clearFocus();
        mListView = view.findViewById(R.id.lv_contacts);

        btnAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inviteCallBack != null) {
                    inviteCallBack.onInviteCallBack(new SortModel());
                }
            }
        });

//        characterParser = CharacterParser.getInstance();
//        mAllContactsList = new ArrayList<>();
//        pinyinComparator = new PinyinComparator();
//        Collections.sort(mAllContactsList, pinyinComparator);// 根据a-z进行排序源数据
        adapter = new ContactsSortAdapter(context, mAllContactsList, new OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
//                String number = mAllContactsList.get((Integer) view.getTag()).number;
                SortModel sortModel = mAllContactsList.get((Integer) view.getTag());
                Logger.LOGD(TAG, "onItemClick: etSearch text = " + etSearch.getText().toString().isEmpty());
                if (!etSearch.getText().toString().trim().isEmpty()) {
                    Logger.LOGD(TAG, "onItemClick: etSearch text = " + etSearch.getText());
//                    number = mSearchContactsList.get((Integer) view.getTag()).number;
                    sortModel = mSearchContactsList.get((Integer) view.getTag());
                }
//               Logger.LOGD(TAG, "onItemClick: number = " + number);
                if (inviteCallBack != null) {
                    inviteCallBack.onInviteCallBack(sortModel);
                }
            }
        });
        if (resIdAction > 0)
            adapter.setTxtAction(resIdAction);
        mListView.setAdapter(adapter);

        thisDialog = new Dialog(context, R.style.DialogTheme);
        thisDialog.setContentView(view);
        thisDialog.setCancelable(true);
        thisDialog.show();
    }

    private void initListener() {

        /**清除输入字符**/
        ivClearText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                etSearch.setText("");
            }
        });
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable e) {

                String content = etSearch.getText().toString().trim();
                if ("".equals(content)) {
                    ivClearText.setVisibility(View.INVISIBLE);
                } else {
                    ivClearText.setVisibility(View.VISIBLE);
                }
                if (content.length() > 0) {
                    mSearchContactsList = mContactsModel.search(content);
                    adapter.updateListView(mSearchContactsList);
                    //mAdapter.updateData(mContacts);
                } else {
                    adapter.updateListView(mAllContactsList);
                }
                mListView.setSelection(0);

            }

        });

        //设置右侧[A-Z]快速导航栏触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(@NonNull String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
                adapter.toggleChecked(position);
            }
        });

    }

//    private void checkContactsPermissions() {
//        final String permission;
//        permission = Manifest.permission.READ_CONTACTS;
//        Dexter.withActivity(context)
//                .withPermission(permission)
//                .withListener(new PermissionListener() {
//                    @Override
//                    public void onPermissionGranted(PermissionGrantedResponse response) {
//                        loadContacts();
//                    }
//
//                    @Override
//                    public void onPermissionDenied(PermissionDeniedResponse response) {
//                        int tip;
//                        tip = R.string.permission_denied_contact;
//
//                        MagicDialog dialog = new MagicDialog(context);
//                        dialog.title(R.string.permission_denied).confirm().content(tip).positive(R.string.settings)
//                                .negative(R.string.cancel).bold(MagicDialog.MagicDialogButton.POSITIVE).right(MagicDialog.MagicDialogButton.POSITIVE)
//                                .listener(new OnMagicDialogClickCallback() {
//                                    @Override
//                                    public void onItemClick(View view, MagicDialog.MagicDialogButton button, boolean checked) {
//                                        if (button == MagicDialog.MagicDialogButton.POSITIVE) {
//                                            Utils.gotoAppDetailsSettings(context);
//                                        }
//                                    }
//                                }).show();
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
//                        token.continuePermissionRequest();
//                    }
//
//                }).check();
//    }
//
//    /**
//     * =====================================通讯录相关操作===========================================
//     */
//    private void loadContacts() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    ContentResolver resolver = context.getContentResolver();
//                    Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                            new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
//                                    ContactsContract.CommonDataKinds.Phone.NUMBER, "sort_key"},
//                            null, null, "sort_key COLLATE LOCALIZED ASC");
//                    if (phoneCursor == null || phoneCursor.getCount() == 0) {
//                        Toast.makeText(context, R.string.tip_no_permission_or_data, Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    int PHONES_NUMBER_INDEX = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//                    int PHONES_DISPLAY_NAME_INDEX = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
//                    int SORT_KEY_INDEX = phoneCursor.getColumnIndex("sort_key");
//                    if (phoneCursor.getCount() > 0) {
//                        mAllContactsList = new ArrayList<SortModel>();
//                        while (phoneCursor.moveToNext()) {
//                            String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
//                            if (TextUtils.isEmpty(phoneNumber))
//                                continue;
//                            String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
//                            String sortKey = phoneCursor.getString(SORT_KEY_INDEX);
//                            //System.out.println(sortKey);
//                            SortModel sortModel = new SortModel(contactName, phoneNumber, sortKey);
//                            //优先使用系统sortkey取,取不到再使用工具取
//                            String sortLetters = getSortLetterBySortKey(sortKey);
//                            if (sortLetters == null) {
//                                sortLetters = getSortLetter(contactName);
//                            }
//                            sortModel.sortLetters = sortLetters;
//
//                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
//                                sortModel.sortToken = parseSortKey(sortKey);
//                            else
//                                sortModel.sortToken = parseSortKeyLollipop(sortKey);
//
//                            mAllContactsList.add(sortModel);
//                        }
//                    }
//                    phoneCursor.close();
//                    context.runOnUiThread(new Runnable() {
//                        public void run() {
//                            Collections.sort(mAllContactsList, pinyinComparator);
//                            adapter.updateListView(mAllContactsList);
//                        }
//                    });
//                } catch (Exception e) {
//                    Logger.LOGE("xbc", e.getLocalizedMessage());
//                }
//            }
//        }).start();
//    }


    public void dismiss() {
        if (thisDialog != null && thisDialog.isShowing()) {
            thisDialog.dismiss();
        }
    }

    private InviteCallBack inviteCallBack;

    public void setOnInviteCallBack(InviteCallBack callBack) {
        inviteCallBack = callBack;
    }


//    /**
//     * 取sort_key的首字母
//     *
//     * @param sortKey
//     * @return
//     */
//    private String getSortLetterBySortKey(String sortKey) {
//        if (sortKey == null || "".equals(sortKey.trim())) {
//            return null;
//        }
//        String letter = "#";
//        //汉字转换成拼音
//        String sortString = sortKey.trim().substring(0, 1).toUpperCase(Locale.CHINESE);
//        // 正则表达式，判断首字母是否是英文字母
//        if (sortString.matches("[A-Z]")) {
//            letter = sortString.toUpperCase(Locale.CHINESE);
//        } else { // 5.0以上需要判断汉字
//            if (sortString.matches("^[\u4E00-\u9FFF]+$"))// 正则表达式，判断是否为汉字
//                letter = getSortLetter(sortString.toUpperCase(Locale.CHINESE));
//        }
//        return letter;
//    }
//
//    /**
//     * 模糊查询
//     *
//     * @param str
//     * @return
//     */
//    private CopyOnWriteArrayList<SortModel> search(String str) {
//        CopyOnWriteArrayList<SortModel> filterList = new CopyOnWriteArrayList<>();// 过滤后的list
//        //if (str.matches("^([0-9]|[/+])*$")) {// 正则表达式 匹配号码
//        if (str.matches("^([0-9]|[/+]).*")) {// 正则表达式 匹配以数字或者加号开头的字符串(包括了带空格及-分割的号码)
//            String simpleStr = str.replaceAll("-|\\s", "");
//            for (SortModel contact : mAllContactsList) {
//                if (contact.number != null && contact.name != null) {
//                    if (contact.simpleNumber.contains(simpleStr) || contact.name.contains(str)) {
//                        if (!filterList.contains(contact)) {
//                            filterList.add(contact);
//                        }
//                    }
//                }
//            }
//        } else {
//            for (SortModel contact : mAllContactsList) {
//                if (contact.number != null && contact.name != null) {
//                    //姓名全匹配,姓名首字母简拼匹配,姓名全字母匹配
//                    boolean isNameContains = contact.name.toLowerCase(Locale.CHINESE)
//                            .contains(str.toLowerCase(Locale.CHINESE));
//
//                    boolean isSortKeyContains = contact.sortKey.toLowerCase(Locale.CHINESE).replace(" ", "")
//                            .contains(str.toLowerCase(Locale.CHINESE));
//
//                    boolean isSimpleSpellContains = contact.sortToken.simpleSpell.toLowerCase(Locale.CHINESE)
//                            .contains(str.toLowerCase(Locale.CHINESE));
//
//                    boolean isWholeSpellContains = contact.sortToken.wholeSpell.toLowerCase(Locale.CHINESE)
//                            .contains(str.toLowerCase(Locale.CHINESE));
//
//                    if (isNameContains || isSortKeyContains || isSimpleSpellContains || isWholeSpellContains) {
//                        if (!filterList.contains(contact)) {
//                            filterList.add(contact);
//                        }
//                    }
//                }
//            }
//        }
//        return filterList;
//    }
//
//    /**
//     * 名字转拼音,取首字母
//     *
//     * @param name
//     * @return
//     */
//    private String getSortLetter(String name) {
//        String letter = "#";
//        if (name == null) {
//            return letter;
//        }
//        //汉字转换成拼音
//        String pinyin = characterParser.getSelling(name);
//        String sortString = pinyin.substring(0, 1).toUpperCase(Locale.CHINESE);
//
//        // 正则表达式，判断首字母是否是英文字母
//        if (sortString.matches("[A-Z]")) {
//            letter = sortString.toUpperCase(Locale.CHINESE);
//        }
//        return letter;
//    }
//
//    /**
//     * 中文字符串匹配
//     */
//    String chReg = "[\\u4E00-\\u9FA5]+";
//
//    /**
//     * 解析sort_key,封装简拼,全拼
//     *
//     * @param sortKey
//     * @return
//     */
//    public SortToken parseSortKey(String sortKey) {
//        SortToken token = new SortToken();
//        if (sortKey != null && sortKey.length() > 0) {
//            //其中包含的中文字符
//            String[] enStrs = sortKey.replace(" ", "").split(chReg);
//            for (String enStr : enStrs) {
//                if (enStr.length() > 0) {
//                    //拼接简拼
//                    token.simpleSpell += enStr.charAt(0);
//                    token.wholeSpell += enStr;
//                }
//            }
//        }
//        return token;
//    }
//
//    /**
//     * 解析sort_key,封装简拼,全拼。
//     * Android 5.0 以上使用
//     *
//     * @param sortKey
//     * @return
//     */
//    public SortToken parseSortKeyLollipop(String sortKey) {
//        SortToken token = new SortToken();
//        if (sortKey != null && sortKey.length() > 0) {
//            boolean isChinese = sortKey.matches(chReg);
//            // 分割条件：中文不分割，英文以大写和空格分割
//            String regularExpression = isChinese ? "" : "(?=[A-Z])|\\s";
//
//            String[] enStrs = sortKey.split(regularExpression);
//
//            for (String enStr : enStrs)
//                if (enStr.length() > 0) {
//                    //拼接简拼
//                    token.simpleSpell += getSortLetter(String.valueOf(enStr.charAt(0)));
//                    token.wholeSpell += characterParser.getSelling(enStr);
//                }
//        }
//        return token;
//    }
}
