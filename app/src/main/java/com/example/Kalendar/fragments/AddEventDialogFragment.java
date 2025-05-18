package com.example.Kalendar.fragments;

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
import com.example.Kalendar.adapters.EventReminderReceiver;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CategoryEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class AddEventDialogFragment extends BottomSheetDialogFragment {

    private LocalDate date;
    private AppDatabase db;
    private int currentUserId;

    private EditText inputTitle, inputLocation, inputDescription;
    private Button btnStart, btnEnd, btnRepeat;
    private CheckBox chkAllDay, chkEarly, chkNotify;
    private TimePicker tpEarly;
    private Spinner spinnerCategory, spinnerCalendar;
    private ImageButton btnAddCategory;
    private Button btnSave;

    private CategorySpinnerAdapter categoryAdapter;
    private final List<CategoryEntity> categories = new ArrayList<>();
    private CalendarSpinnerAdapter calendarAdapter;
    private final List<com.example.Kalendar.models.CalendarEntity> calendars = new ArrayList<>();

    private Integer editingEventId = null;
    private Integer preselectedCalendarId = null;
    private OnEventSavedListener listener;

    public interface OnEventSavedListener { void onEventSaved(); }
    public void setOnEventSavedListener(OnEventSavedListener l) { this.listener = l; }
    public void setPreselectedCalendarId(Integer id) { this.preselectedCalendarId = id; }

    public static AddEventDialogFragment newInstance(LocalDate date) {
        AddEventDialogFragment f = new AddEventDialogFragment();
        Bundle args = new Bundle();
        args.putString("date", date.toString());
        f.setArguments(args);
        return f;
    }

    public static AddEventDialogFragment editInstance(int eventId, LocalDate date) {
        AddEventDialogFragment f = new AddEventDialogFragment();
        Bundle args = new Bundle();
        args.putInt("editEventId", eventId);
        args.putString("date", date.toString());
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.fragment_add_event, container, false);
        date = LocalDate.parse(getArguments().getString("date"));
        db = AppDatabase.getDatabase(requireContext());
        currentUserId = SessionManager.getLoggedInUserId(requireContext());

        inputTitle = v.findViewById(R.id.inputTitle);
        inputLocation = v.findViewById(R.id.inputLocation);
        inputDescription = v.findViewById(R.id.inputDescription);
        btnStart = v.findViewById(R.id.btnStartTime);
        btnEnd = v.findViewById(R.id.btnEndTime);
        chkAllDay = v.findViewById(R.id.checkAllDay);
        chkEarly = v.findViewById(R.id.checkReminderEarly);
        tpEarly = v.findViewById(R.id.timePickerEarly);
        chkNotify = v.findViewById(R.id.checkNotifyEvent);
        spinnerCategory = v.findViewById(R.id.spinnerCategory);
        btnAddCategory = v.findViewById(R.id.btnAddCategory);
        spinnerCalendar = v.findViewById(R.id.spinnerCalendar);
        btnRepeat = v.findViewById(R.id.btnRepeatRule);
        btnSave = v.findViewById(R.id.btnSaveEvent);

        tpEarly.setIs24HourView(true);
        tpEarly.setVisibility(View.GONE);

        setupCategorySpinner();
        setupCalendarSpinner();

        btnAddCategory.setOnClickListener(x -> categoryAdapter.showCategoryDialog(null));
        chkEarly.setOnCheckedChangeListener((b, c) -> tpEarly.setVisibility(c ? View.VISIBLE : View.GONE));
        chkAllDay.setOnCheckedChangeListener((b, a) -> {
            btnStart.setEnabled(!a);
            btnEnd.setEnabled(!a);
        });
        // TODO: implement time pickers and repeat dialog
        btnSave.setOnClickListener(x -> saveEvent());

        if (getArguments().containsKey("editEventId")) {
            editingEventId = getArguments().getInt("editEventId");
            loadEventForEdit();
        }

        return v;
    }

    private void setupCategorySpinner() {
        categoryAdapter = new CategorySpinnerAdapter(
                requireContext(), categories, db, currentUserId,
                () -> categoryAdapter.notifyDataSetChanged()
        );
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupCalendarSpinner() {
        calendarAdapter = new CalendarSpinnerAdapter(requireContext(), calendars);
        spinnerCalendar.setAdapter(calendarAdapter);
        new Thread(() -> {
            List<com.example.Kalendar.models.CalendarEntity> list =
                    db.calendarDao().getByUserId(currentUserId);
            new Handler(Looper.getMainLooper()).post(() -> {
                calendars.clear();
                calendars.addAll(list);
                calendarAdapter.notifyDataSetChanged();
                if (preselectedCalendarId != null) {
                    for (int i = 0; i < calendars.size(); i++) {
                        if (calendars.get(i).id == preselectedCalendarId) {
                            spinnerCalendar.setSelection(i);
                            break;
                        }
                    }
                }
            });
        }).start();
    }

    private void loadEventForEdit() {
        new Thread(() -> {
            EventEntity ev = db.eventDao().getById(editingEventId);
            if (ev == null) return;
            new Handler(Looper.getMainLooper()).post(() -> {
                inputTitle.setText(ev.title);
                inputLocation.setText(ev.location);
                inputDescription.setText(ev.description);
                chkAllDay.setChecked(ev.allDay);
                btnStart.setText("Начало: " + ev.timeStart);
                btnEnd.setText("Конец: " + ev.timeEnd);
                chkEarly.setChecked(ev.earlyReminderEnabled);
                tpEarly.setHour(ev.earlyReminderHour);
                tpEarly.setMinute(ev.earlyReminderMinute);
                tpEarly.setVisibility(ev.earlyReminderEnabled ? View.VISIBLE : View.GONE);
                chkNotify.setChecked(ev.notifyOnStart);
                // category selection
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).name.equals(ev.category)) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
                // calendar selection
                for (int i = 0; i < calendars.size(); i++) {
                    if (calendars.get(i).id == ev.calendarId) {
                        spinnerCalendar.setSelection(i);
                        break;
                    }
                }
                // repeat and exclusions omitted for brevity
            });
        }).start();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void saveEvent() {
        String title = inputTitle.getText().toString().trim();
        if (title.isEmpty()) { inputTitle.setError("Введите название"); return; }
        CategoryEntity selCat = (CategoryEntity) spinnerCategory.getSelectedItem();
        int calId = calendars.get(spinnerCalendar.getSelectedItemPosition()).id;
        boolean allDay = chkAllDay.isChecked();
        boolean early = chkEarly.isChecked();
        int eh = tpEarly.getHour(), em = tpEarly.getMinute();
        boolean notify = chkNotify.isChecked();
        String start = allDay ? "00:00" : btnStart.getText().toString().replace("Начало: ", "");
        String end   = allDay ? "23:59" : btnEnd.getText().toString().replace("Конец: ", "");

        new Thread(() -> {
            long ts = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            DayEntity day = db.dayDao().getByTimestampAndCalendarId(ts, calId);
            if (day == null) {
                day = new DayEntity(); day.timestamp = ts; day.calendarId = calId;
                day.id = (int) db.dayDao().insert(day);
            }
            EventEntity ev = editingEventId != null
                    ? db.eventDao().getById(editingEventId)
                    : new EventEntity();
            ev.title = title;
            ev.location = inputLocation.getText().toString().trim();
            ev.description = inputDescription.getText().toString().trim();
            ev.timeStart = start; ev.timeEnd = end;
            ev.allDay = allDay;
            ev.category = selCat.name;
            ev.calendarId = calId;
            ev.dayId = day.id;
            ev.earlyReminderEnabled = early;
            ev.earlyReminderHour = eh; ev.earlyReminderMinute = em;
            ev.notifyOnStart = notify;
            // repeatRule/exclusions omitted

            if (editingEventId != null) db.eventDao().update(ev);
            else ev.id = (int) db.eventDao().insert(ev);

            requireActivity().runOnUiThread(() -> {
                if (listener != null) listener.onEventSaved();
                dismiss();
            });

            // schedule notifications if needed
            if (early || notify) {
                scheduleEventNotification(
                        requireContext(), ev.id, "Напоминание: " + title,
                        ev.description, eh, em,
                        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), early
                );
            }
        }).start();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleEventNotification(Context ctx,
                                           int requestCode,
                                           String title,
                                           String text,
                                           int hour,
                                           int minute,
                                           long eventStartMs,
                                           boolean isEarly) {
        Intent intent = new Intent(ctx, EventReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(eventStartMs);
        if (isEarly) {
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
        }
        c.set(Calendar.SECOND, 0);
        AlarmManager am = ctx.getSystemService(AlarmManager.class);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
    }

    public void refresh() { loadEventForEdit(); }
}