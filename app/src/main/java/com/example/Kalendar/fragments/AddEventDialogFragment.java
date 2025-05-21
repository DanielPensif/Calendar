package com.example.Kalendar.fragments;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
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
import com.example.Kalendar.adapters.CategorySpinnerAdapter;
import com.example.Kalendar.adapters.EventReminderReceiver;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.adapters.CalendarSpinnerAdapter;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.CategoryEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.utils.EventUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AddEventDialogFragment extends BottomSheetDialogFragment {

    private LocalDate date;
    private AppDatabase db;
    private int currentUserId;

    private EditText inputTitle, inputLocation, inputDescription;
    private Button btnStart, btnEnd, btnRepeatRule, btnRestoreExcluded, btnSave;
    private Spinner spinnerCategory, spinnerCalendar;
    private CategorySpinnerAdapter categoryAdapter;
    private ImageButton btnAddCategory;
    private CheckBox checkAllDay, checkReminderEarly, checkNotifyEvent;
    private TimePicker timePickerEarly;
    private LinearLayout excludedDatesLayout;
    private ChipGroup chipGroupExcluded;

    private String selectedStart = "09:00", selectedEnd = "10:00";
    private String repeatRule = null;
    private final List<CalendarEntity> calendarEntities = new ArrayList<>();
    private final List<CategoryEntity> categories = new ArrayList<>();
    private Set<LocalDate> editableExdates = new HashSet<>();
    private Integer editEventId = null;
    private Integer preselectedCalendarId = null;
    private OnEventSavedListener listener;

    public interface OnEventSavedListener { void onEventSaved(); }
    public void setOnEventSavedListener(OnEventSavedListener l) { listener = l; }
    public void setPreselectedCalendarId(Integer id) { this.preselectedCalendarId = id; }
    private int toMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bs) {
        View view = inflater.inflate(R.layout.fragment_add_event, container, false);
        db = AppDatabase.getDatabase(requireContext());
        date = LocalDate.parse(requireArguments().getString("date"));
        currentUserId = SessionManager.getLoggedInUserId(requireContext());

        inputTitle = view.findViewById(R.id.inputTitle);
        inputLocation = view.findViewById(R.id.inputLocation);
        inputDescription = view.findViewById(R.id.inputDescription);
        btnStart = view.findViewById(R.id.btnStartTime);
        btnEnd = view.findViewById(R.id.btnEndTime);
        checkAllDay = view.findViewById(R.id.checkAllDay);
        checkReminderEarly = view.findViewById(R.id.checkReminderEarly);
        timePickerEarly = view.findViewById(R.id.timePickerEarly);
        checkNotifyEvent = view.findViewById(R.id.checkNotifyEvent);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnAddCategory = view.findViewById(R.id.btnAddCategory);
        spinnerCalendar = view.findViewById(R.id.spinnerCalendar);
        btnRepeatRule = view.findViewById(R.id.btnRepeatRule);
        excludedDatesLayout = view.findViewById(R.id.excludedDatesLayout);
        chipGroupExcluded = view.findViewById(R.id.chipGroupExcluded);
        btnRestoreExcluded = view.findViewById(R.id.btnRestoreExcluded);
        btnSave = view.findViewById(R.id.btnSaveEvent);

        timePickerEarly.setIs24HourView(true);
        timePickerEarly.setVisibility(View.GONE);

        setupSpinners();
        setupTimePickers();

        checkReminderEarly.setOnCheckedChangeListener((b, c) ->
                timePickerEarly.setVisibility(c ? View.VISIBLE : View.GONE)
        );
        btnRepeatRule.setText("Повтор: не повторяется");
        btnRepeatRule.setOnClickListener(v -> {
            RepeatRuleDialogFragment dlg = RepeatRuleDialogFragment.newInstance(repeatRule);
            dlg.setOnRepeatSelectedListener((rule, txt) -> {
                repeatRule = rule;
                btnRepeatRule.setText(txt);
            });
            dlg.show(getParentFragmentManager(), "repeatRule");
        });
        checkAllDay.setOnCheckedChangeListener((b, all) -> {
            btnStart.setEnabled(!all);
            btnEnd.setEnabled(!all);
        });
        btnRestoreExcluded.setOnClickListener(v -> {
            editableExdates.clear();
            btnRestoreExcluded.setVisibility(View.GONE);
            renderExcludedDates();
        });
        btnSave.setOnClickListener(v -> saveEvent());

        if (getArguments() != null && getArguments().containsKey("editEventId")) {
            editEventId = getArguments().getInt("editEventId");
            loadEventForEdit();
        }

        return view;
    }

    private void setupSpinners() {
        categoryAdapter = new CategorySpinnerAdapter(
                requireContext(), categories, db, currentUserId,
                () -> categoryAdapter.notifyDataSetChanged()
        );
        spinnerCategory.setAdapter(categoryAdapter);
        btnAddCategory.setOnClickListener(v -> categoryAdapter.showCategoryDialog(null));

        CalendarSpinnerAdapter calAdapter = new CalendarSpinnerAdapter(
                requireContext(), calendarEntities
        );
        spinnerCalendar.setAdapter(calAdapter);
        new Thread(() -> {
            List<CalendarEntity> cals = db.calendarDao().getByUserId(currentUserId);
            requireActivity().runOnUiThread(() -> {
                calendarEntities.clear();
                calendarEntities.addAll(cals);
                calAdapter.notifyDataSetChanged();
                if (preselectedCalendarId != null) {
                    spinnerCalendar.setSelection(getCalendarIndex(preselectedCalendarId));
                }
            });
        }).start();
    }

    private void setupTimePickers() {
        btnStart.setOnClickListener(v -> showTimePicker(t -> {
            selectedStart = t;
            btnStart.setText("Начало: " + t);
        }));

        btnEnd.setOnClickListener(v -> showTimePicker(t -> {
            selectedEnd = t;
            btnEnd.setText("Конец: " + t);
        }));
    }

    private void renderExcludedDates() {
        if (editableExdates.isEmpty()) {
            excludedDatesLayout.setVisibility(View.GONE);
            return;
        }
        excludedDatesLayout.setVisibility(View.VISIBLE);
        chipGroupExcluded.removeAllViews();
        for (LocalDate d : new TreeSet<>(editableExdates)) {
            Chip chip = new Chip(requireContext());
            chip.setText(d.toString());
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                editableExdates.remove(d);
                renderExcludedDates();
                if (editableExdates.isEmpty()) btnRestoreExcluded.setVisibility(View.GONE);
            });
            chipGroupExcluded.addView(chip);
        }
    }

    private void loadEventForEdit() {
        new Thread(() -> {
            EventEntity ev = db.eventDao().getById(editEventId);
            if (ev == null) return;
            new Handler(Looper.getMainLooper()).post(() -> {
                inputTitle.setText(ev.title);
                inputLocation.setText(ev.location);
                inputDescription.setText(ev.description);
                checkAllDay.setChecked(ev.allDay);
                btnStart.setText("Начало: " + ev.timeStart);
                btnEnd.setText("Конец: " + ev.timeEnd);
                selectedStart = ev.timeStart;
                selectedEnd = ev.timeEnd;
                spinnerCategory.setSelection(getIndex(spinnerCategory, ev.category));
                spinnerCalendar.setSelection(getCalendarIndex(ev.calendarId));
                checkReminderEarly.setChecked(ev.earlyReminderEnabled);
                timePickerEarly.setHour(ev.earlyReminderHour);
                timePickerEarly.setMinute(ev.earlyReminderMinute);
                timePickerEarly.setVisibility(ev.earlyReminderEnabled?View.VISIBLE:View.GONE);
                checkNotifyEvent.setChecked(ev.notifyOnStart);

                if (ev.repeatRule != null) {
                    repeatRule = ev.repeatRule;
                    btnRepeatRule.setText("Повтор: " + EventUtils.parseDisplayFromRule(ev.repeatRule));
                }
                if (ev.excludedDates != null) {
                    editableExdates = EventUtils.parseExcludedDates(ev.excludedDates);
                    renderExcludedDates();
                    btnRestoreExcluded.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    private int getIndex(Spinner sp, String val) {
        for (int i = 0; i < sp.getCount(); i++) {
            if (sp.getItemAtPosition(i).toString().equalsIgnoreCase(val)) return i;
        }
        return 0;
    }

    private int getCalendarIndex(int id) {
        for (int i = 0; i < calendarEntities.size(); i++) {
            if (calendarEntities.get(i).id == id) return i;
        }
        return 0;
    }

    private void saveEvent() {
        String title = inputTitle.getText().toString().trim();
        String location = inputLocation.getText().toString().trim();
        String description = inputDescription.getText().toString().trim();
        boolean allDay = checkAllDay.isChecked();
        boolean earlyRem = checkReminderEarly.isChecked();
        int earlyHour = timePickerEarly.getHour();
        int earlyMinute = timePickerEarly.getMinute();
        boolean notifyOnStart = checkNotifyEvent.isChecked();

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Введите название", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!allDay) {
            int startMin = toMinutes(selectedStart);
            int endMin = toMinutes(selectedEnd);
            if (endMin <= startMin) {
                Toast.makeText(getContext(), "Время окончания должно быть позже начала", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int calendarId = calendarEntities.get(spinnerCalendar.getSelectedItemPosition()).id;
        String timeStart = allDay ? "00:00" : selectedStart;
        String timeEnd = allDay ? "23:59" : selectedEnd;
        CategoryEntity cat = (CategoryEntity) spinnerCategory.getSelectedItem();

        new Thread(() -> {
            long ts = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            DayEntity day = db.dayDao().getByTimestampAndCalendarId(ts, calendarId);
            if (day == null) {
                day = new DayEntity();
                day.timestamp = ts;
                day.calendarId = calendarId;
                day.id = (int) db.dayDao().insert(day);
            }

            boolean isEdit = getArguments()!=null && getArguments().containsKey("editEventId");
            EventEntity event = isEdit
                    ? db.eventDao().getById(editEventId)
                    : new EventEntity();
            if (!isEdit) event.done = false;

            event.title = title;
            event.location = location;
            event.description = description;
            event.timeStart = timeStart;
            event.timeEnd = timeEnd;
            event.allDay = allDay;
            event.category = cat.name;
            event.calendarId = calendarId;
            event.repeatRule = repeatRule;
            event.earlyReminderEnabled = earlyRem;
            event.earlyReminderHour = earlyHour;
            event.earlyReminderMinute = earlyMinute;
            event.notifyOnStart = notifyOnStart;
            event.dayId = day.id;
            event.userId = currentUserId;
            if (!editableExdates.isEmpty()) {
                event.excludedDates = editableExdates.stream()
                        .map(LocalDate::toString)
                        .sorted()
                        .collect(Collectors.joining(","));
            }

            if (isEdit) db.eventDao().update(event);
            else event.id = (int) db.eventDao().insert(event);

            requireActivity().runOnUiThread(() -> {
                if (listener != null) listener.onEventSaved();
                dismiss();
            });

            scheduleNotifications(event.id, title, description, earlyHour, earlyMinute, timeStart, notifyOnStart);
        }).start();
    }

    private void scheduleNotifications(int id, String title, String text, int eh, int em, String timeStart, boolean notifyOnStart) {
        try {
            String[] parts = timeStart.split(":");
            LocalDateTime ldt = date.atTime(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1])
            );
            long eventStartMs = ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            if (checkReminderEarly.isChecked()) {
                scheduleAlarm(id, "Напоминание: " + title, text, eh, em, eventStartMs, true);
            }
            if (notifyOnStart) {
                scheduleAlarm(id + 1000, "Самое время: " + title, text, 0, 0, eventStartMs, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleAlarm(int requestCode, String title, String text,
                               int hour, int minute, long eventStartMs, boolean isEarly) {
        Intent intent = new Intent(requireContext(), EventReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        PendingIntent pi = PendingIntent.getBroadcast(
                requireContext(), requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(eventStartMs);
        if (isEarly) {
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
        }
        c.set(Calendar.SECOND, 0);
        AlarmManager am = requireContext().getSystemService(AlarmManager.class);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
    }

    private void showTimePicker(Consumer<String> onPicked) {
        new TimePickerDialog(getContext(), (tp, h, m) -> {
            String t = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            onPicked.accept(t);
        }, 9, 0, true).show();
    }
}