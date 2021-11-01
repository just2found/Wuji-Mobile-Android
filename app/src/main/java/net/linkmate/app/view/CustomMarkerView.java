package net.linkmate.app.view;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import net.linkmate.app.R;
import net.sdvn.common.internet.protocol.flow.DevFlowDetailList;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CustomMarkerView extends MarkerView {
    private final Context context;
    private TextView tvDate;
    private TextView tvFlow;
    private TextView tvScore;
    private ArrayList<String> xVals;
    private List<DevFlowDetailList.DataBean.ListBean> beans;
    private LineChart mChart;
    private String peroid;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        // this markerview only displays a textview
        this.context = context;
        tvDate = (TextView) findViewById(R.id.lmv_tv_time);
        tvFlow = (TextView) findViewById(R.id.lmv_tv_flow);
        tvScore = (TextView) findViewById(R.id.lmv_tv_score);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String dateX = xVals.get(e.getXIndex());
        tvFlow.setText("0GB");
        tvScore.setText(context.getString(R.string.relate_points) + "：0");
        for (DevFlowDetailList.DataBean.ListBean bean : beans) {
            if (Objects.equals(dateX, bean.billdate)) {
                tvFlow.setText(bean.bill_flow);
                if (bean.mbpoint == 0 && !"0".equals(bean.bill_flow)) {
                    tvScore.setText("...");
                } else {
                    tvScore.setText(context.getString(R.string.relate_points) + "：" + BigDecimal.valueOf(bean.mbpoint)
                            .stripTrailingZeros()
                            .toPlainString());
                }
            }
        }
        try {
            if ("month".equals(peroid)) {
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM");
                Date date = sf.parse(dateX);
                dateX = new SimpleDateFormat(context.getString(R.string.fmt_time_adapter_title)).format(date);
            } else {
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sf.parse(dateX);
                dateX = new SimpleDateFormat(context.getString(R.string.fmt_time_adapter_title2)).format(date);
            }
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        tvDate.setText(dateX); // set the entry-value as the display text
    }

    @Override
    public int getXOffset(float xpos) {
        // this will center the marker-view horizontally
        if (xpos < getWidth() / 2) {
            return (int) -xpos + 5;
        } else if (xpos + getWidth() / 2 > mChart.getWidth()) {
            return (int) -(getWidth() + xpos - mChart.getWidth()) - 5;
        } else {
            return -(getWidth() / 2);
        }
    }

    @Override
    public int getYOffset(float ypos) {
        // this will cause the marker-view to be above the selected value
        return (int) -ypos + 5;
    }

    public void setXVals(ArrayList<String> xVals) {
        this.xVals = xVals;
    }

    public void setBeans(List<DevFlowDetailList.DataBean.ListBean> beans) {
        this.beans = beans;
    }

    public void setChartView(LineChart mChart) {
        this.mChart = mChart;
    }

    public void setPeroid(String peroid) {
        this.peroid = peroid;
    }
}
