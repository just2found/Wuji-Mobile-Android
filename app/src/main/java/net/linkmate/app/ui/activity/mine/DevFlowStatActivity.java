package net.linkmate.app.ui.activity.mine;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.SimpleCallback;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.bean.DeviceBean;
import net.linkmate.app.manager.DevManager;
import net.linkmate.app.util.FormatUtils;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.util.WindowUtil;
import net.linkmate.app.view.CustomMarkerView;
import net.linkmate.app.view.DeviceFlowDetailFilterDialog;
import net.linkmate.app.view.TipsBar;
import net.linkmate.app.view.adapter.PopupCheckRVAdapter;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.loader.GetFlowIncomeStatHttpLoader;
import net.sdvn.common.internet.protocol.flow.DevFlowDetailList;
import net.sdvn.nascommon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

public class DevFlowStatActivity extends BaseActivity implements HttpLoader.HttpLoaderStateListener {
    private ImageView ivLeft;
    private TextView tvTitle;
    private LinearLayout llTitle;
    private SwipeRefreshLayout mSrl;
    private TextView tvMonthStat;
    private TextView tvDayStat;
    private LinearLayout mLlExpabd;
    private TextView mTvExpabd;
    private LineChart mChart;
    private TextView adfsTvDate;
    private TextView adfsTvFlow;
    private TextView adfsTvScore;

    private List<String> options1Items = new ArrayList<>();
    private List<List<String>> options2Items = new ArrayList<>();
    private List<List<List<String>>> options3Items = new ArrayList<>();
    private OptionsPickerView pvOptions;

    private List<DevFlowDetailList.DataBean.ListBean> totalBeans = new ArrayList<>();
    private List<DevFlowDetailList.DataBean.ListBean> trueBeans = new ArrayList<>();

    private String period = "month";//统计模式，month：每一个月份的统计；day：某个月份中每一天的统计
    private String requestDate = "";//请求日期，格式：2020-05或2020-05-05
    private String checkedDevId = "";//设备id
    private String checkedDevName = "";//设备名
    private String checkedUserId = "";
    private String yearTemp;
    private int xCount = 12;
    private DevFlowDetailList.DataBean.ListBean listMonthBean;
    private View mScoreIvLeft;

    private void initView() {
        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initTotal();
            }
        });
    }

    private void initTotal() {
        GetFlowIncomeStatHttpLoader loader = new GetFlowIncomeStatHttpLoader(DevFlowDetailList.class);
        loader.setHttpLoaderStateListener(this);
        loader.setParams2(checkedDevId, checkedUserId, period, requestDate, 1, 24);
        loader.executor(new MyOkHttpListener<DevFlowDetailList>() {
            @Override
            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                super.error(tag, baseProtocol);
                checkedDevName = mTvExpabd.getText().toString().trim();
            }

            @Override
            public void success(Object tag, DevFlowDetailList data) {
//                if (!TextUtils.isEmpty(checkedDevName)) {
//                    mTvExpabd.setText(checkedDevName);
//                }
                totalBeans.clear();
//                DevFlowDetailList baseProtocol = new Gson().fromJson("{\n" +
//                        "\t\"result\": 0,\n" +
//                        "\t\"data\": {\n" +
//                        "\t\t\"totalRow\": 4,\n" +
//                        "\t\t\"totalPage\": 1,\n" +
//                        "\t\t\"list\": [{\n" +
//                        "\t\t\t\"billtime\": 1608433556000,\n" +
//                        "\t\t\t\"free_flow\": \"129.637MB\",\n" +
//                        "\t\t\t\"bill_flow\": \"529.59GB\",\n" +
//                        "\t\t\t\"total_flow\": \"8.82GB\",\n" +
//                        "\t\t\t\"billdate\": \"2020-12\",\n" +
//                        "\t\t\t\"mbpoint\": 54.2946\n" +
//                        "\t\t}, {\n" +
//                        "\t\t\t\"billtime\": 1600571156000,\n" +
//                        "\t\t\t\"free_flow\": \"539.67MB\",\n" +
//                        "\t\t\t\"bill_flow\": \"8.59GB\",\n" +
//                        "\t\t\t\"total_flow\": \"8.82GB\",\n" +
//                        "\t\t\t\"billdate\": \"2020-09\",\n" +
//                        "\t\t\t\"mbpoint\": 3.2946\n" +
//                        "\t\t}, {\n" +
//                        "\t\t\t\"billtime\": 1588262400000,\n" +
//                        "\t\t\t\"free_flow\": \"139.67MB\",\n" +
//                        "\t\t\t\"bill_flow\": \"8.59GB\",\n" +
//                        "\t\t\t\"total_flow\": \"8.82GB\",\n" +
//                        "\t\t\t\"billdate\": \"2020-05\",\n" +
//                        "\t\t\t\"mbpoint\": 1.2946\n" +
//                        "\t\t}, {\n" +
//                        "\t\t\t\"billtime\": 1585670400000,\n" +
//                        "\t\t\t\"free_flow\": \"314.23KB\",\n" +
//                        "\t\t\t\"bill_flow\": \"145.99KB\",\n" +
//                        "\t\t\t\"total_flow\": \"460.21KB\",\n" +
//                        "\t\t\t\"billdate\": \"2020-04\",\n" +
//                        "\t\t\t\"mbpoint\": 0\n" +
//                        "\t\t}, {\n" +
//                        "\t\t\t\"billtime\": 1582992000000,\n" +
//                        "\t\t\t\"free_flow\": \"4.63MB\",\n" +
//                        "\t\t\t\"bill_flow\": \"2.16MB\",\n" +
//                        "\t\t\t\"total_flow\": \"6.79MB\",\n" +
//                        "\t\t\t\"billdate\": \"2020-03\",\n" +
//                        "\t\t\t\"mbpoint\": 0.001\n" +
//                        "\t\t}, {\n" +
//                        "\t\t\t\"billtime\": 1580486400000,\n" +
//                        "\t\t\t\"free_flow\": \"5.78MB\",\n" +
//                        "\t\t\t\"bill_flow\": \"2.68MB\",\n" +
//                        "\t\t\t\"total_flow\": \"8.46MB\",\n" +
//                        "\t\t\t\"billdate\": \"2020-01\",\n" +
//                        "\t\t\t\"mbpoint\": 0.0013\n" +
//                        "\t\t},{\n" +
//                        "\t\t\t\"billtime\": 1576811156000,\n" +
//                        "\t\t\t\"free_flow\": \"39.67MB\",\n" +
//                        "\t\t\t\"bill_flow\": \"8.59GB\",\n" +
//                        "\t\t\t\"total_flow\": \"8.82GB\",\n" +
//                        "\t\t\t\"billdate\": \"2019-12\",\n" +
//                        "\t\t\t\"mbpoint\": 3.2946\n" +
//                        "\t\t}, {\n" +
//                        "\t\t\t\"billtime\": 1568948756000,\n" +
//                        "\t\t\t\"free_flow\": \"539.67MB\",\n" +
//                        "\t\t\t\"bill_flow\": \"8.59GB\",\n" +
//                        "\t\t\t\"total_flow\": \"8.82GB\",\n" +
//                        "\t\t\t\"billdate\": \"2019-09\",\n" +
//                        "\t\t\t\"mbpoint\": 7.2946\n" +
//                        "\t\t}, {\n" +
//                        "\t\t\t\"billtime\": 1558321556000,\n" +
//                        "\t\t\t\"free_flow\": \"139.67MB\",\n" +
//                        "\t\t\t\"bill_flow\": \"8.59GB\",\n" +
//                        "\t\t\t\"total_flow\": \"8.82GB\",\n" +
//                        "\t\t\t\"billdate\": \"2019-05\",\n" +
//                        "\t\t\t\"mbpoint\": 5.2946\n" +
//                        "\t\t}, {\n" +
//                        "\t\t\t\"billtime\": 1555729556000,\n" +
//                        "\t\t\t\"free_flow\": \"314.23KB\",\n" +
//                        "\t\t\t\"bill_flow\": \"145.99KB\",\n" +
//                        "\t\t\t\"total_flow\": \"460.21KB\",\n" +
//                        "\t\t\t\"billdate\": \"2019-04\",\n" +
//                        "\t\t\t\"mbpoint\": 2\n" +
//                        "\t\t}, {\n" +
//                        "\t\t\t\"billtime\": 1552729556000,\n" +
//                        "\t\t\t\"free_flow\": \"4.63MB\",\n" +
//                        "\t\t\t\"bill_flow\": \"2.16MB\",\n" +
//                        "\t\t\t\"total_flow\": \"6.79MB\",\n" +
//                        "\t\t\t\"billdate\": \"2019-03\",\n" +
//                        "\t\t\t\"mbpoint\": 0.001\n" +
//                        "\t\t}, {\n" +
//                        "\t\t\t\"billtime\": 1546729556000,\n" +
//                        "\t\t\t\"free_flow\": \"5.78MB\",\n" +
//                        "\t\t\t\"bill_flow\": \"2.68MB\",\n" +
//                        "\t\t\t\"total_flow\": \"8.46MB\",\n" +
//                        "\t\t\t\"billdate\": \"2019-01\",\n" +
//                        "\t\t\t\"mbpoint\": 3.0013\n" +
//                        "\t\t}]\n" +
//                        "\t},\n" +
//                        "\t\"errmsg\": \"success\"\n" +
//                        "}", DevFlowDetailList.class);
//                totalBeans.addAll(baseProtocol.data.list);
                totalBeans.addAll(data.data.list);
                trueBeans.clear();
//                if (TextUtils.isEmpty(requestDate) || "day".equals(period)) {
                for (int i = 0; i < xCount && i < totalBeans.size(); i++) {
                    trueBeans.add(totalBeans.get(i));
                }
//                } else {
//                    boolean isStart = false;
//                    int i = 0;
//                    String[] split = requestDate.split("-");
//                    String dateTemp = requestDate;
//                    if (split.length == 3) {
//                        dateTemp = split[0] + "-" + split[1];
//                    }
//                    for (DevFlowDetailList.DataBean.ListBean bean : totalBeans) {
//                        if (!isStart && Objects.equals(bean.billdate, dateTemp)) {
//                            isStart = true;
//                        }
//                        if (i >= xCount)
//                            break;
//                        if (isStart) {
//                            trueBeans.add(bean);
//                            i++;
//                        }
//                    }
//                }
//
                if (trueBeans.size() > 0) {
                    if (TextUtils.isEmpty(requestDate)) {
                        SimpleDateFormat sf = new SimpleDateFormat(getString(
                                "month".equals(period) ?
                                        R.string.fmt_time_adapter_title :
                                        R.string.fmt_time_adapter_title2));
                        refreshText(sf.format(trueBeans.get(0).billtime), trueBeans.get(0).bill_flow,
                                BigDecimal.valueOf(trueBeans.get(0).mbpoint)
                                        .stripTrailingZeros()
                                        .toPlainString());
                        requestDate = new SimpleDateFormat("yyyy-MM").format(trueBeans.get(0).billtime);
                    } else {
                        String[] splitRequest = requestDate.split("-");
                        String dateStr = requestDate;
                        Date date;
                        try {
                            if (splitRequest.length == 2) {
                                date = new SimpleDateFormat("yyyy-MM").parse(dateStr);
                            } else {
                                date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                            }
                            SimpleDateFormat sf = new SimpleDateFormat(getString(
                                    "month".equals(period) ?
                                            R.string.fmt_time_adapter_title :
                                            R.string.fmt_time_adapter_title2));
                            dateStr = sf.format(date);
                        } catch (Exception ignore) {
                        }
                        refreshText(dateStr, "0", "0");

                        String dateTemp = requestDate;
                        if ("day".equals(period) && splitRequest.length == 2) {
                            dateTemp = splitRequest[0] + "-" + splitRequest[1] + "-01";
                        } else if ("month".equals(period) && splitRequest.length == 3) {
                            dateTemp = splitRequest[0] + "-" + splitRequest[1];
                        }

                        for (DevFlowDetailList.DataBean.ListBean bean : trueBeans) {
                            if (!TextUtils.isEmpty(bean.billdate) && dateTemp.equals(bean.billdate)) {
                                SimpleDateFormat sf = new SimpleDateFormat(getString(
                                        "month".equals(period) ?
                                                R.string.fmt_time_adapter_title :
                                                R.string.fmt_time_adapter_title2));
                                refreshText(sf.format(bean.billtime), bean.bill_flow,
                                        BigDecimal.valueOf(bean.mbpoint)
                                                .stripTrailingZeros()
                                                .toPlainString());
                                break;
                            }
                        }
                    }
                } else {
                    refreshText(getString(R.string.cur_date), "0", "0");
                }
                toInitDateOption();
            }
        });
    }

    private void refreshText(String date, String flow, String score) {
        adfsTvDate.setText(date);
        if (flow.endsWith("B") || flow.endsWith("b")) {
//            String flowFmt = flow.substring(0, flow.length() - 2);
//            adfsTvFlow.setText(flowFmt);
            SpannableString spannableString = new SpannableString(flow);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.title_text_color)), flow.length() - 2, flow.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new RelativeSizeSpan(0.5f), flow.length() - 2, flow.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            adfsTvFlow.setText(spannableString);
        } else {
            adfsTvFlow.setText(flow);
        }
        if (Float.parseFloat(score) == 0 && !"0".equals(flow)) {
            adfsTvScore.setText("...");
        } else {
            adfsTvScore.setText(score);
        }
    }

    private void toInitDateOption() {
        if ("day".equals(period)) {
            GetFlowIncomeStatHttpLoader loader = new GetFlowIncomeStatHttpLoader(DevFlowDetailList.class);
            loader.setHttpLoaderStateListener(this);
            loader.setParams2(checkedDevId, checkedUserId, "month", requestDate, 1, 24);
            loader.executor(new MyOkHttpListener<DevFlowDetailList>() {
                @Override
                public void success(Object tag, DevFlowDetailList data) {
                    if (data.data.list.size() > 0) {
                        listMonthBean = data.data.list.get(0);
                    }
                    initDateOption(data.data.list);
                    initChartData();
                }
            });
        } else {
            initDateOption(totalBeans);
            initChartData();
        }
    }

    private void initDateOption(List<DevFlowDetailList.DataBean.ListBean> beans) {
        options1Items.clear();
        options2Items.clear();
        options3Items.clear();
        for (DevFlowDetailList.DataBean.ListBean bean : beans) {
            String[] split = bean.billdate.split("-");
            String year = split[0];
            if (!options1Items.contains(year)) {
                options1Items.add(year);
            }
        }
        for (String item : options1Items) {
            ArrayList<String> e = new ArrayList<>();
            for (DevFlowDetailList.DataBean.ListBean bean : beans) {
                String[] split = bean.billdate.split("-");
                if (split.length >= 2) {
                    String year = split[0];
                    String month = split[1];
                    month = FormatUtils.monthFormatToEn(this, month);
                    if (Objects.equals(item, year) && !e.contains(month)) {
                        e.add(month);
                    }
                }
            }
            options2Items.add(e);
        }
        if ("day".equals(period)) {
            Calendar c = Calendar.getInstance();
            for (int i = 0; i < options1Items.size(); i++) {
                List<List<String>> mon = new ArrayList<>();
                for (int j = 0; j < options2Items.get(i).size(); j++) {
                    ArrayList<String> day = new ArrayList<>();
                    int year = Integer.parseInt(options1Items.get(i));
                    int month = j + 1;
                    try {
                        month = Integer.parseInt(options2Items.get(i).get(j));
                    } catch (Exception e) {
                        try {
                            SimpleDateFormat sf1 = new SimpleDateFormat("MMM");
                            Date parse = sf1.parse(options2Items.get(i).get(j));
                            SimpleDateFormat sf2 = new SimpleDateFormat("MM");
                            month = Integer.parseInt(sf2.format(parse));
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                    c.set(year, month, 0);
                    int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
                    for (int k = 0; k < dayOfMonth; k++) {
                        String dayStr = Integer.toString(k + 1);
                        if (dayStr.length() == 1) {
                            dayStr = "0" + dayStr;
                        }
                        day.add(dayStr);
                    }
                    mon.add(day);
                }
                options3Items.add(mon);
            }
        }
    }

    private void initChart() {
//        mChart.invalidate();
//        mChart.notifyDataSetChanged();

        mChart.setNoDataText(getString(R.string.no_record));//设置当 chart 为空时显示的文字
        mChart.setDrawGridBackground(false);
        mChart.setTouchEnabled(true);// 启用/禁用与图表的所有可能的触摸交互。
//        mChart.setDragEnabled(false);// 启用/禁用拖动（平移）图表。
        mChart.setScaleEnabled(false);// 启用/禁用缩放图表上的两个轴。
//        mChart.setScaleXEnabled(false);// 启用/禁用缩放在x轴上。
//        mChart.setScaleYEnabled(false);// 启用/禁用缩放在y轴。
        mChart.setPinchZoom(false);// 如果设置为true，捏缩放功能。 如果false，x轴和y轴可分别放大。
        mChart.setDoubleTapToZoomEnabled(false);// 设置为false以禁止通过在其上双击缩放图表。
        mChart.setHighlightPerDragEnabled(true);// 设置为true，允许每个图表表面拖过，当它完全缩小突出。 默认值：true
        mChart.setHighlightPerTapEnabled(true);// 设置为false，以防止值由敲击姿态被突出显示。 值仍然可以通过拖动或编程方式突出显示。 默认值：true
        mChart.setDescription("");

        Legend legend = mChart.getLegend();
        legend.setEnabled(false);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float width = metrics.widthPixels;
        float offsets1 = width / 15;
        float offsets2 = width / 21;
        mChart.setViewPortOffsets(offsets1, offsets2, offsets1, offsets1);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(true);//是否绘制轴线
        xAxis.setDrawGridLines(false);//设置x轴上每个点对应的线
        xAxis.setDrawLabels(true);//绘制标签  指x轴上的对应数值
//        xAxis.setTextColor(int color);// 设置轴标签的颜色。
//        xAxis.setTextSize(float size);// 设置轴标签的文字大小。
//        xAxis.setGridColor(int color);// 设置该轴的网格线颜色。
//        xAxis.setGridLineWidth(1f);// 设置该轴网格线的宽度。
        xAxis.setAxisLineColor(0xff999999);// 设置轴线的轴的颜色。
        xAxis.setTextSize(9f);
        xAxis.setAxisLineWidth(1.5f);// 设置该轴轴行的宽度。
        xAxis.setSpaceBetweenLabels(0);//设置标签字符间的空隙，默认characters间隔是4 。
        xAxis.setValueFormatter(new XAxisValueFormatter() {
            @Override
            public String getXValue(String original, int index, ViewPortHandler viewPortHandler) {
                try {
                    if ("month".equals(period)) {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM");
                        Date date = sf.parse(original);
                        String[] split = original.split("-");
                        String strYear = split[0];
                        String strMonth = split[1];
                        if (Objects.equals(yearTemp, strYear)) {
                            //已显示过的年份不再写入，仅写入月份
                            original = strMonth;
                            original = new SimpleDateFormat("MMM").format(date);
                        } else {
                            original = strYear + "-" + strMonth;
                            original = new SimpleDateFormat(getString(R.string.fmt_time_adapter_title)).format(date);
                        }
                    } else {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = sf.parse(original);
                        String[] split = original.split("-");
                        String strYear = split[0];
                        String strMonth = split[1];
                        String strDay = split[2];
                        original = strDay;
                        original = new SimpleDateFormat("dd").format(date);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return original;
            }
        });

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisLineColor(Color.TRANSPARENT);
        leftAxis.setTextColor(Color.TRANSPARENT);
        leftAxis.setStartAtZero(true);
        leftAxis.setAxisLineWidth(1f);
        leftAxis.setSpaceTop(50f);//设置的百分比 = 最高柱顶部间距/最高柱的值
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(0x33999999);// 设置该轴的网格线颜色。
        leftAxis.setGridLineWidth(0.5f);// 设置该轴网格线的宽度。
    }

    private void initChartData() {
        mChart.clear();
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        LineDataSet dataSet = new LineDataSet(yVals, "");
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        if (totalBeans.size() > 0 && trueBeans.size() > 0) {
            if ("month".equals(period)) {
//                DevFlowDetailList.DataBean.ListBean lastBean = totalBeans.get(0);
                DevFlowDetailList.DataBean.ListBean targetBean = trueBeans.get(0);
                for (int i = 0; i < xCount; i++) {
                    String[] split = targetBean.billdate.split("-");
                    if (i == 0) {
                        yearTemp = split[0];
                    }
                    int year = Integer.parseInt(split[0]);
//                    int month = Integer.parseInt(split[1]) + i;
                    int month = i + 1;
                    String strYear = String.valueOf(month > 12 ? year + 1 : year);
                    month = month > 12 ? month - 12 : month;
                    String strMonth = month < 10 ? "0" + month : "" + month;
                    xVals.add(strYear + "-" + strMonth);

                    String[] last = targetBean.billdate.split("-");
                    if ((Integer.parseInt(strYear) <= Integer.parseInt(last[0])
                            && Integer.parseInt(strMonth) > Integer.parseInt(last[1])) ||
                            Integer.parseInt(strYear) > Integer.parseInt(last[0])) {
                        //如果时间超过了最后一组数据的时间，则不写入Y轴数据
                        continue;
                    }
                    String date = xVals.get(i);
                    Entry entry = new Entry(0f, i); // 0 == quarter 1
                    for (DevFlowDetailList.DataBean.ListBean bean : totalBeans) {
                        if (Objects.equals(bean.billdate, date)) {
                            entry = new Entry((float) bean.mbpoint, i);
                            break;
                        }
                    }
                    yVals.add(entry);
                }
            } else {
//                DevFlowDetailList.DataBean.ListBean lastBean = totalBeans.get(0);
                DevFlowDetailList.DataBean.ListBean targetBean = trueBeans.get(0);

                String[] split = targetBean.billdate.split("-");
                int year = Integer.parseInt(split[0]);
                int month = Integer.parseInt(split[1]);

                Calendar c = Calendar.getInstance();
                c.set(year, month, 0);
                int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

                for (int i = 0; i < dayOfMonth; i++) {
                    int day = i + 1;
                    month = day > 31 ? month + 1 : month;
                    String strYear = String.valueOf(month > 12 ? year + 1 : year);
                    month = month > 12 ? month - 12 : month;
                    day = day > 31 ? day - 31 : day;
                    String strMonth = month < 10 ? "0" + month : "" + month;
                    String strDay = day < 10 ? "0" + day : "" + day;
                    xVals.add(strYear + "-" + strMonth + "-" + strDay);

                    try {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                        Date dateL = sf.parse(targetBean.billdate);
                        Date dateLM = new SimpleDateFormat("yyyy-MM").parse(listMonthBean.billdate);
                        Date dateX = sf.parse(strYear + "-" + strMonth + "-" + strDay);
                        if (dateX.getTime() > dateL.getTime() && dateX.getTime() >= dateLM.getTime()) {
                            //如果时间超过了最后一组数据的时间，则不写入Y轴数据
                            continue;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String date = xVals.get(i);
                    Entry entry = new Entry(0f, i); // 0 == quarter 1
                    for (DevFlowDetailList.DataBean.ListBean bean : totalBeans) {
                        if (Objects.equals(bean.billdate, date)) {
                            entry = new Entry((float) bean.mbpoint, i);
                            break;
                        }
                    }
                    yVals.add(entry);
                }
            }

            dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSet.setColor(getResources().getColor(R.color.primary));
            dataSet.setLineWidth(2f);
            dataSet.setCircleSize(3f);
            dataSet.setCircleColor(getResources().getColor(R.color.primary));
            dataSet.setDrawValues(false); // 是否在点上绘制Value
//            dataSet.setValueTextColor(Color.GREEN);
//            dataSet.setValueTextSize(12f);setDrawFilled
            dataSet.setHighlightEnabled(true);//是否禁用点击高亮线
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(getResources().getColor(R.color.translucent_primary));
            dataSet.setHighLightColor(getResources().getColor(R.color.primary));//设置点击交点后显示交高亮线的颜色
            dataSet.setDrawHorizontalHighlightIndicator(false);//不画横线
            dataSet.setHighlightLineWidth(1f);

            dataSets.add(dataSet);

            //点击图表坐标监听
            mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

                @Override
                public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                    String dateX = xVals.get(e.getXIndex());
                    String flow = "0";
                    String score = "0";
                    for (DevFlowDetailList.DataBean.ListBean bean : totalBeans) {
                        if (Objects.equals(dateX, bean.billdate)) {
                            flow = bean.bill_flow;
                            score = BigDecimal.valueOf(bean.mbpoint)
                                    .stripTrailingZeros()
                                    .toPlainString();
                        }
                    }
                    try {
                        if ("month".equals(period)) {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM");
                            Date date = sf.parse(dateX);
                            dateX = new SimpleDateFormat(getString(R.string.fmt_time_adapter_title)).format(date);
                        } else {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                            Date date = sf.parse(dateX);
                            dateX = new SimpleDateFormat(getString(R.string.fmt_time_adapter_title2)).format(date);
                        }
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                    refreshText(dateX, flow, score);
                }

                @Override
                public void onNothingSelected() {
                }
            });

            CustomMarkerView mv = new CustomMarkerView(this, R.layout.layout_marker_view);
            mv.setPeroid(period);
            mv.setXVals(xVals);
            mv.setBeans(totalBeans);
            mv.setChartView(mChart);
            mChart.setMarkerView(mv);
        }

        mChart.setData(new LineData(xVals, dataSets));
        mChart.animateY(1000);
        mChart.invalidate();
    }

    @Override
    protected View getTopView() {
        return llTitle;
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }
    private View mAdfsLlDate;

    private void showDatePicker() {
        if (options1Items.size() == 0 || options2Items.size() == 0 ||
                ("month".equals(period) && options3Items.size() != 0) ||
                ("day".equals(period) && options3Items.size() == 0)) {
            return;
        }
        pvOptions = new OptionsPickerBuilder(DevFlowStatActivity.this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                requestDate = options1Items.get(options1)
                        + "-"
                        + FormatUtils.monthFormatToNumber(DevFlowStatActivity.this,
                        options2Items.get(options1).get(options2));
                if (options3Items.size() > 0) {
                    String day = options3Items.get(options1).get(options2).get(options3);
                    if (day.length() == 1) {
                        day = "0" + day;
                    }
                    requestDate = requestDate + "-" + day;
                }
                initTotal();
            }
        })
                .setLayoutRes(R.layout.layout_pickerview_custom_options, new CustomListener() {
                    @Override
                    public void customLayout(View v) {
                        //自定义布局中的控件初始化及事件处理
                        Button tvSubmit = (Button) v.findViewById(R.id.btnSubmit);
                        Button ivCancel = (Button) v.findViewById(R.id.btnCancel);
                        String language = getResources().getConfiguration().locale.getLanguage();
                        if (!"zh".equals(language) && !"ja".equals(language) && !"ko".equals(language)) {
                            v.findViewById(R.id.tv_year).setVisibility(View.GONE);
                            v.findViewById(R.id.tv_month).setVisibility(View.GONE);
                        }
                        tvSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvOptions.returnData();
                                pvOptions.dismiss();
                            }
                        });
                        ivCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvOptions.dismiss();
                            }
                        });
                    }
                })
                //设置可见数量为3,避免被部分手机导航栏挡住
                .setItemVisibleCount(3)
                .setOutSideCancelable(false)
                .build();
        pvOptions.setPicker(options1Items, options2Items, options3Items.size() > 0 ? options3Items : null);
        try {
            String billdate = adfsTvDate.getText().toString().trim();
            String language = getResources().getConfiguration().locale.getLanguage();
            String pattern = "zh".equals(language) || "ja".equals(language) || "ko".equals(language) ?
                    "MM" : "MMM";
            if ("month".equals(period)) {
                SimpleDateFormat sf = new SimpleDateFormat(getString(R.string.fmt_time_adapter_title));
                Date date = sf.parse(billdate);
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.US);
                SimpleDateFormat monthFormat = new SimpleDateFormat(pattern, Locale.US);
                String year = yearFormat.format(date);
                String month = monthFormat.format(date);
                month = FormatUtils.monthFormatToEn(DevFlowStatActivity.this, month);
                int option1 = options1Items.indexOf(year);
                int option2 = option1 >= 0 ? options2Items.get(option1).indexOf(month) : 0;
                pvOptions.setSelectOptions(option1, option2);
            } else if ("day".equals(period)) {
                SimpleDateFormat sf = new SimpleDateFormat(getString(R.string.fmt_time_adapter_title2));
                Date date = sf.parse(billdate);
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.US);
                SimpleDateFormat monthFormat = new SimpleDateFormat(pattern, Locale.US);
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.US);
                String year = yearFormat.format(date);
                String month = monthFormat.format(date);
                String day = dayFormat.format(date);
                month = FormatUtils.monthFormatToEn(DevFlowStatActivity.this, month);
                int option1 = options1Items.indexOf(year);
                int option2 = option1 >= 0 ? options2Items.get(option1).indexOf(month) : 0;
                int option3 = options3Items.get(option1).get(option2).indexOf(day);
                pvOptions.setSelectOptions(option1, option2, option3);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        pvOptions.show();
    }

    private void switchPeriod() {
        if ("month".equals(period)) {
            period = "day";
            xCount = 31;
            tvMonthStat.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            tvDayStat.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tvMonthStat.setTextColor(getResources().getColor(R.color.text_light_blue));
            tvDayStat.setTextColor(getResources().getColor(R.color.text_white));
            tvMonthStat.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvDayStat.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        } else {
            period = "month";
            xCount = 12;
            tvMonthStat.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tvDayStat.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            tvMonthStat.setTextColor(getResources().getColor(R.color.text_white));
            tvDayStat.setTextColor(getResources().getColor(R.color.text_light_blue));
            tvMonthStat.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            tvDayStat.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        }
        initTotal();
    }
    private DeviceFlowDetailFilterDialog filterPopupView = null;

    /**
     * 刷新过滤弹框
     *
     * @param v
     */
    private void showFilterPopupView(View v) {
        if (filterPopupView != null && filterPopupView.isShow()) {//正在显示，关闭弹框
            filterPopupView.dismiss();
            return;
        }
        filterPopupView = null;
        filterPopupView = (DeviceFlowDetailFilterDialog) new XPopup.Builder(this)
                .atView(v)
//                .dismissOnTouchOutside(false)
                .setPopupCallback(new SimpleCallback() {
                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onDismiss() {
                    }
                })
                .asCustom(new DeviceFlowDetailFilterDialog(this));
        filterPopupView.setFilterListener(new DeviceFlowDetailFilterDialog.FilterListener() {
            @Override
            public void confirm(@NotNull String deviceId, @NotNull String deviceName, @NotNull String userId) {
                if (!checkedDevId.equals(deviceId) || !checkedUserId.equals(userId)) {
                    checkedDevId = deviceId;
                    checkedUserId = userId;
                    checkedDevName = deviceName;
//                    initFilterFontStyle();
                    initTotal();
                }
            }
        });
        filterPopupView.initData(checkedDevId, checkedUserId);
        filterPopupView.show();
    }
    private void showDevPop() {
        if (!DevManager.getInstance().isInitting()) {
            List<DeviceBean> deviceBeans = new ArrayList<>();
            for (DeviceBean bean : DevManager.getInstance().getBoundDeviceBeans()) {
                if (bean.getHardData() != null && bean.getHardData().isEN() && bean.getMnglevel() == 0) {
                    deviceBeans.add(bean);
                }
            }

            DeviceBean bean = new DeviceBean(getString(R.string.all_dev), "", -1, 0);
            bean.setId("");
            deviceBeans.add(0, bean);

            View contentView = LayoutInflater.from(this).inflate(R.layout.popup_rv_check, null, false);
            final PopupWindow window = new PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            RecyclerView rv = contentView.findViewById(R.id.popup_rv);
            PopupCheckRVAdapter adapter = new PopupCheckRVAdapter(deviceBeans, checkedDevId);
            //点击device条目
            adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    String deviceId = ((DeviceBean) adapter.getData().get(position)).getId();
                    if (!checkedDevId.equals(deviceId)) {
                        checkedDevId = deviceId;
//                        currPage = 1;
//                        lastVaildMonth = "";
                        checkedDevName = ((DeviceBean) adapter.getData().get(position)).getName();
                        initTotal();
                    }
                    window.dismiss();
                }
            });
            rv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            rv.setItemAnimator(null);
            rv.setAdapter(adapter);

            window.setOutsideTouchable(true);
            window.setTouchable(true);
            window.setAnimationStyle(R.style.PopupWindowAnim);
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    WindowUtil.hintShadow(DevFlowStatActivity.this);
                }
            });
            WindowUtil.showShadow(this);
            window.showAsDropDown(mLlExpabd, 0, 0);
        } else {
            ToastUtils.showToast(R.string.loading_data);
        }
    }

    private int loadTimes = 0;
    private View mAdfsTvMonthStat;
    private View mAdfsTvDayStat;
    private View mAdfsLlExpand;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_flow_stat);
        bindView();
        tvTitle.setText(R.string.dev_flow_stat);
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);

        String checkedDevId = getIntent().getStringExtra("checkedDevId");
        if (!TextUtils.isEmpty(checkedDevId)) {
            this.checkedDevId = checkedDevId;
        }
        String checkedUserId = getIntent().getStringExtra("checkedUserId");
        if (!TextUtils.isEmpty(checkedUserId)) {
            this.checkedUserId = checkedUserId;
        }
        String checkedDevName = getIntent().getStringExtra("checkedDevName");
        if (!TextUtils.isEmpty(checkedDevName)) {
            this.checkedDevName = checkedDevName;
//            mTvExpabd.setText(this.checkedDevName);
        }
        initView();
        initChart();
        initTotal();
    }

    private void onViewClicked(View view) {
        if (Utils.isFastClick(view)) return;
        switch (view.getId()) {
            case R.id.score_iv_left:
                onBackPressed();
                break;
            case R.id.adfs_ll_date:
                showDatePicker();
                break;
            case R.id.adfs_tv_month_stat:
                if ("day".equals(period))
                    switchPeriod();
                break;
            case R.id.adfs_tv_day_stat:
                if ("month".equals(period))
                    switchPeriod();
                break;
            case R.id.adfs_ll_expand:
                showFilterPopupView(view);
                break;
        }
    }

    @Override
    public void onLoadStart(Disposable disposable) {
        loadTimes++;
        mSrl.setRefreshing(true);
    }

    @Override
    public void onLoadComplete() {
        if (--loadTimes <= 0)
            mSrl.setRefreshing(false);
    }

    @Override
    public void onLoadError() {
        if (--loadTimes <= 0)
            mSrl.setRefreshing(false);
    }

    private void bindView() {
        ivLeft =  findViewById(R.id.score_iv_left);
        tvTitle =  findViewById(R.id.score_tv_title);
        llTitle =  findViewById(R.id.score_ll_title);
        mSrl =  findViewById(R.id.score_recode_srl);
        tvMonthStat =  findViewById(R.id.adfs_tv_month_stat);
        tvDayStat =  findViewById(R.id.adfs_tv_day_stat);
        mLlExpabd =  findViewById(R.id.adfs_ll_expand);
        mTvExpabd =  findViewById(R.id.adfs_tv_expand);
        mChart =  findViewById(R.id.adfs_chart);
        adfsTvDate =  findViewById(R.id.adfs_tv_date);
        adfsTvFlow =  findViewById(R.id.adfs_tv_flow);
        adfsTvScore =  findViewById(R.id.adfs_tv_score);
        mScoreIvLeft =  findViewById(R.id.score_iv_left);
        mAdfsLlDate =  findViewById(R.id.adfs_ll_date);
        mAdfsTvMonthStat =  findViewById(R.id.adfs_tv_month_stat);
        mAdfsTvDayStat =  findViewById(R.id.adfs_tv_day_stat);
        mAdfsLlExpand =  findViewById(R.id.adfs_ll_expand);
        mScoreIvLeft.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mAdfsLlDate.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mAdfsTvMonthStat.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mAdfsTvDayStat.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mAdfsLlExpand.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }
}
