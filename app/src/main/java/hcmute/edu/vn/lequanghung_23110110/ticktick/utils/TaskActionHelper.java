package hcmute.edu.vn.lequanghung_23110110.ticktick.utils;

import android.content.Context;
import java.util.List;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;

public class TaskActionHelper {
    public static void updateTaskDateAndSync(Context context, TaskModel task, String dateTag, long dateMillis, List<String> reminders) {
        TaskDatabaseHelper dbHelper = TaskDatabaseHelper.getInstance(context);
        dbHelper.updateTaskDate(task.getId(), dateTag, dateMillis, reminders);
        
        CalendarHelper calHelper = CalendarHelper.getInstance(context);
        if (calHelper.hasCalendarPermission() && calHelper.isSyncEnabled()) {
            task.setDateTag(dateTag);
            task.setDueDateMillis(dateMillis);
            if (task.getCalendarEventId() > 0) {
                if (dateMillis > 0) {
                    calHelper.updateEvent(task.getCalendarEventId(), task);
                } else {
                    calHelper.deleteEvent(task.getCalendarEventId());
                    dbHelper.updateTaskCalendarEventId(task.getId(), -1);
                }
            } else if (dateMillis > 0) {
                long eventId = calHelper.insertEvent(task);
                if (eventId > 0) {
                    dbHelper.updateTaskCalendarEventId(task.getId(), eventId);
                }
            }
        }
    }
}
