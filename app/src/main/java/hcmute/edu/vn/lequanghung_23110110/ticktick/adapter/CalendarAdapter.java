package hcmute.edu.vn.lequanghung_23110110.ticktick.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;

public class CalendarAdapter extends BaseAdapter {

    private final Context context;
    private final List<Integer> days;        // Ngày trong tháng (0 = ô trống)
    private final int todayDay;              // Ngày hôm nay
    private final int todayMonth;
    private final int todayYear;
    private final int displayMonth;          // Tháng đang hiển thị
    private final int displayYear;
    private int selectedDay = -1;            // Ngày được chọn (-1 = chưa chọn)

    public CalendarAdapter(Context context, List<Integer> days,
                           int displayMonth, int displayYear) {
        this.context = context;
        this.days = days;
        this.displayMonth = displayMonth;
        this.displayYear = displayYear;

        Calendar today = Calendar.getInstance();
        this.todayDay = today.get(Calendar.DAY_OF_MONTH);
        this.todayMonth = today.get(Calendar.MONTH);
        this.todayYear = today.get(Calendar.YEAR);
    }

    public void setSelectedDay(int day) {
        this.selectedDay = day;
        notifyDataSetChanged();
    }

    public int getSelectedDay() {
        return selectedDay;
    }

    @Override
    public int getCount() { return days.size(); }

    @Override
    public Object getItem(int position) { return days.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_calendar_day, parent, false);
        }

        TextView dayText = convertView.findViewById(R.id.day_text);
        int day = days.get(position);

        if (day == 0) {
            // Ô trống (padding đầu tháng)
            dayText.setText("");
            dayText.setBackground(null);
            return convertView;
        }

        dayText.setText(String.valueOf(day));

        boolean isToday = (day == todayDay
                && displayMonth == todayMonth
                && displayYear == todayYear);
        boolean isSelected = (day == selectedDay);

        if (isSelected) {
            // Ngày được chọn → vòng tròn xanh, text trắng
            dayText.setBackgroundResource(R.drawable.bg_day_selected);
            dayText.setTextColor(Color.WHITE);
        } else if (isToday) {
            // Ngày hôm nay → vòng tròn viền trắng
            dayText.setBackgroundResource(R.drawable.bg_day_today);
            dayText.setTextColor(Color.WHITE);
        } else {
            // Ngày bình thường
            dayText.setBackground(null);
            dayText.setTextColor(Color.parseColor("#CCCCCC"));
        }

        return convertView;
    }
}