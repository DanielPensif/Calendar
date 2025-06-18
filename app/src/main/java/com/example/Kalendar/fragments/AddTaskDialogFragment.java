package com.example.Kalendar.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.Kalendar.R;
import com.example.Kalendar.adapters.CalendarSpinnerAdapter;
import com.example.Kalendar.adapters.CategorySpinnerAdapter;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.adapters.TaskReminderReceiver;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CategoryEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.TaskEntity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import java.util.*;

public class AddTaskDialogFragment extends BottomSheetDialogFragment {
    private static final String ARG_DATE="date", ARG_EDIT="editTaskId";
    private LocalDate date;
    private AppDatabase db;
    private int userId;
    private EditText etTitle, etComment;
    private Spinner spCat, spCal;
    private CheckBox cbRem;
    private TimePicker tpRem;
    private Button btnSave;
    private CategorySpinnerAdapter catAdapter;
    private CalendarSpinnerAdapter calAdapter;
    private List<CategoryEntity> cats = new ArrayList<>();
    private List<com.example.Kalendar.models.CalendarEntity> cals = new ArrayList<>();
    private Integer editingId=null;
    private OnTaskSavedListener listener;
    public interface OnTaskSavedListener { void onTaskSaved(); }
    public void setOnTaskSavedListener(OnTaskSavedListener l){listener=l;}
    public static AddTaskDialogFragment newInstance(LocalDate d){
        AddTaskDialogFragment f=new AddTaskDialogFragment();
        Bundle b=new Bundle(); b.putString(ARG_DATE,d.toString());
        f.setArguments(b); return f;
    }
    public static AddTaskDialogFragment editInstance(int id, LocalDate d){
        AddTaskDialogFragment f=new AddTaskDialogFragment();
        Bundle b=new Bundle();
        b.putInt(ARG_EDIT,id);
        b.putString(ARG_DATE,d.toString());
        f.setArguments(b); return f;
    }
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle bs) {
        View v = inf.inflate(R.layout.fragment_add_task,parent,false);
        db = AppDatabase.getDatabase(requireContext());
        date = LocalDate.parse(Objects.requireNonNull(requireArguments().getString(ARG_DATE)));
        userId = SessionManager.getLoggedInUserId(requireContext());
        etTitle=v.findViewById(R.id.inputTaskTitle);
        etComment=v.findViewById(R.id.inputTaskComment);
        spCat=v.findViewById(R.id.spinnerTaskCategory);
        spCal=v.findViewById(R.id.spinnerTaskCalendar);
        cbRem=v.findViewById(R.id.checkReminder);
        tpRem=v.findViewById(R.id.timePickerReminder);
        tpRem.setIs24HourView(true); tpRem.setVisibility(View.GONE);
        btnSave=v.findViewById(R.id.btnSaveTask);

        // category spinner
        catAdapter=new CategorySpinnerAdapter(requireContext(),cats,db,userId,
                ()->catAdapter.notifyDataSetChanged());
        spCat.setAdapter(catAdapter);
        // calendar spinner
        calAdapter=new CalendarSpinnerAdapter(requireContext(),cals);
        spCal.setAdapter(calAdapter);

        new Thread(() -> {
            cats.clear();
            cats.addAll(db.categoryDao().getAllForUser(userId));
            cals.clear();
            cals.addAll(db.calendarDao().getByUserId(userId));
            new Handler(Looper.getMainLooper()).post(() -> {
                catAdapter.notifyDataSetChanged();
                calAdapter.notifyDataSetChanged();
            });
        }).start();

        cbRem.setOnCheckedChangeListener((b, on)->tpRem.setVisibility(on?View.VISIBLE:View.GONE));
        btnSave.setOnClickListener(x-> saveTask());

        if (getArguments().containsKey(ARG_EDIT)) {
            editingId = getArguments().getInt(ARG_EDIT);
            new Thread(() -> {
                TaskEntity t=db.taskDao().getById(editingId);
                if(t==null)return;
                new Handler(Looper.getMainLooper()).post(() -> {
                    etTitle.setText(t.title);
                    etComment.setText(t.comment);
                    for(int i=0;i<cats.size();i++)
                        if(cats.get(i).name.equals(t.category)) spCat.setSelection(i);
                    cbRem.setChecked(t.reminderEnabled);
                    tpRem.setHour(t.reminderHour);
                    tpRem.setMinute(t.reminderMinute);
                    for(int i=0;i<cals.size();i++)
                        if(cals.get(i).id==t.calendarId) spCal.setSelection(i);
                });
            }).start();
        }

        return v;
    }

    @SuppressLint("ScheduleExactAlarm")
    private void saveTask(){
        String title=etTitle.getText().toString().trim();
        if(title.isEmpty()){ Toast.makeText(getContext(),"Введите название",Toast.LENGTH_SHORT).show();return;}
        CategoryEntity cat=(CategoryEntity)spCat.getSelectedItem();
        int calId=cals.get(spCal.getSelectedItemPosition()).id;
        boolean rem=cbRem.isChecked();
        int hr=tpRem.getHour(), mn=tpRem.getMinute();
        String comment=etComment.getText().toString();
        new Thread(() -> {
            long ts = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000;
            DayEntity day=db.dayDao().getByTimestampAndCalendarId(ts,calId);
            if(day==null){
                day=new DayEntity();
                day.timestamp=ts;
                day.calendarId=calId;
                day.id=(int)db.dayDao().insert(day);
            }
            TaskEntity task = editingId!=null
                    ? db.taskDao().getById(editingId)
                    : new TaskEntity();
            if(editingId==null)task.done=false;

            task.title=title;
            task.comment=comment;
            task.category=cat.name;
            task.dayId=day.id;
            task.calendarId=calId;
            task.reminderEnabled=rem;
            task.reminderHour=hr;
            task.reminderMinute=mn;

            if(editingId!=null) db.taskDao().update(task);
            else task.id=(int)db.taskDao().insert(task);

            requireActivity().runOnUiThread(() -> {
                if(listener!=null) listener.onTaskSaved();
                dismiss();
            });
            if(rem) scheduleTaskNotification(task.id,title,comment,hr,mn,ts);
        }).start();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleTaskNotification(int code, String title, String text,
                                          int hr, int mn, long dateMs){
        Intent i=new Intent(requireContext(), TaskReminderReceiver.class);
        i.putExtra("title",title);
        i.putExtra("text",text);
        PendingIntent pi=PendingIntent.getBroadcast(
                requireContext(),code,i,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager am=requireContext().getSystemService(AlarmManager.class);
        org.threeten.bp.ZonedDateTime z = LocalDate.ofEpochDay(0)
                .atStartOfDay(ZoneId.systemDefault())
                .withHour(hr).withMinute(mn);
        long when = z.toInstant().toEpochMilli();
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
    }
}