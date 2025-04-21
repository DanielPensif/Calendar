package com.example.Kalendar.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.Kalendar.R;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.utils.EventUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class AddEventDialogFragment extends BottomSheetDialogFragment {

    private LocalDate date;
    private AppDatabase db;

    private EditText inputTitle, inputLocation, inputDescription;
    private Button btnStart, btnEnd, btnRestoreExcluded;
    private Spinner spinnerCategory, spinnerCalendar;
    private CheckBox checkAllDay;

    private LinearLayout excludedDatesLayout;
    private ChipGroup chipGroupExcluded;
    private Set<LocalDate> editableExdates = new HashSet<>();

    private String selectedStart = "09:00";
    private String selectedEnd = "10:00";

    private OnEventSavedListener listener;

    private final List<CalendarEntity> calendarEntities = new ArrayList<>();

    private String repeatRule = null;
    private String repeatDisplay = "Повтор: не повторяется";
    private Button btnRepeatRule;



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



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_event, container, false);
        db = AppDatabase.getDatabase(requireContext());
        date = LocalDate.parse(Objects.requireNonNull(requireArguments().getString("date")));

        inputTitle = view.findViewById(R.id.inputTitle);
        inputLocation = view.findViewById(R.id.inputLocation);
        inputDescription = view.findViewById(R.id.inputDescription);
        btnStart = view.findViewById(R.id.btnStartTime);
        btnEnd = view.findViewById(R.id.btnEndTime);
        checkAllDay = view.findViewById(R.id.checkAllDay);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerCalendar = view.findViewById(R.id.spinnerCalendar);
        excludedDatesLayout = view.findViewById(R.id.excludedDatesLayout);
        chipGroupExcluded = view.findViewById(R.id.chipGroupExcluded);
        btnRestoreExcluded = view.findViewById(R.id.btnRestoreExcluded);
        btnRepeatRule = view.findViewById(R.id.btnRepeatRule);
        repeatDisplay = "Повтор: не повторяется";
        btnRepeatRule.setText(repeatDisplay);
        btnRepeatRule.setOnClickListener(v -> {
            RepeatRuleDialogFragment dialog = RepeatRuleDialogFragment.newInstance(repeatRule);
            dialog.setOnRepeatSelectedListener((rule, displayText) -> {
                repeatRule = rule;
                repeatDisplay = displayText;
                btnRepeatRule.setText(displayText);
            });
            dialog.show(getParentFragmentManager(), "repeatRule");
        });


        Button btnSave = view.findViewById(R.id.btnSaveEvent);
        btnSave.setOnClickListener(v -> saveEvent());

        selectedStart = "13:00";
        selectedEnd = "14:00";
        btnStart.setText("Начало: 13:00");
        btnEnd.setText("Конец: 14:00");

        checkAllDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnStart.setEnabled(!isChecked);
            btnEnd.setEnabled(!isChecked);
        });

        setupSpinners();
        setupTimePickers();

        btnRestoreExcluded.setOnClickListener(v -> {
            editableExdates.clear();
            btnRestoreExcluded.setVisibility(View.GONE);
            renderExcludedDates();
            Toast.makeText(getContext(), "Все исключённые даты восстановлены", Toast.LENGTH_SHORT).show();
        });

        if (getArguments() != null && getArguments().containsKey("editEventId")) {
            int eventId = getArguments().getInt("editEventId");

            new Thread(() -> {
                EventEntity event = db.eventDao().getById(eventId);
                if (event != null) {
                    requireActivity().runOnUiThread(() -> {
                        spinnerCategory.post(() -> {
                            inputTitle.setText(event.title);
                            inputLocation.setText(event.location);
                            inputDescription.setText(event.description);

                            checkAllDay.setChecked(event.allDay);
                            btnStart.setText("Начало: " + event.timeStart);
                            btnEnd.setText("Конец: " + event.timeEnd);
                            selectedStart = event.timeStart;
                            selectedEnd = event.timeEnd;

                            spinnerCategory.setSelection(getIndex(spinnerCategory, event.category));
                            spinnerCalendar.setSelection(getCalendarIndex(event.calendarId));

                            if (event.repeatRule != null) {
                                repeatRule = event.repeatRule;
                                repeatDisplay = "Повтор: " + EventUtils.parseDisplayFromRule(repeatRule);
                                btnRepeatRule.setText(repeatDisplay);
                            }

                            if (event.excludedDates != null && !event.excludedDates.trim().isEmpty()) {
                                editableExdates = EventUtils.parseExcludedDates(event.excludedDates);
                                renderExcludedDates();
                                btnRestoreExcluded.setVisibility(View.VISIBLE);
                            }
                        });
                    });
                }
            }).start();
        }


        return view;
    }

    private void renderExcludedDates() {
        if (editableExdates.isEmpty()) {
            excludedDatesLayout.setVisibility(View.GONE);
            return;
        }
        excludedDatesLayout.setVisibility(View.VISIBLE);
        chipGroupExcluded.removeAllViews();
        for (LocalDate date : new TreeSet<>(editableExdates)) {
            Chip chip = new Chip(requireContext());
            chip.setText(date.toString());
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                editableExdates.remove(date);
                renderExcludedDates();
                if (editableExdates.isEmpty()) {
                    btnRestoreExcluded.setVisibility(View.GONE);
                }
            });
            chipGroupExcluded.addView(chip);
        }
    }


    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private int getCalendarIndex(int calendarId) {
        for (int i = 0; i < calendarEntities.size(); i++) {
            if (calendarEntities.get(i).id == calendarId) return i;
        }
        return 0;
    }


    public interface OnEventSavedListener {
        void onEventSaved();
    }

    public void setOnEventSavedListener(OnEventSavedListener listener) {
        this.listener = listener;
    }

    private void saveEvent() {
        if (!checkAllDay.isChecked()) {
            int startMinutes = toMinutes(selectedStart);
            int endMinutes = toMinutes(selectedEnd);
            if (endMinutes <= startMinutes) {
                Toast.makeText(getContext(), "Время окончания должно быть позже начала", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String title = inputTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Введите название", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStart = checkAllDay.isChecked() ? "00:00" : selectedStart;
        String timeEnd = checkAllDay.isChecked() ? "23:59" : selectedEnd;
        boolean allDay = checkAllDay.isChecked();
        String category = spinnerCategory.getSelectedItem().toString();
        String location = inputLocation.getText().toString();
        String description = inputDescription.getText().toString();

        int index = spinnerCalendar.getSelectedItemPosition();
        if (index < 0 || index >= calendarEntities.size()) {
            Toast.makeText(getContext(), "Не выбран календарь", Toast.LENGTH_SHORT).show();
            return;
        }

        int calendarId = calendarEntities.get(index).id;

        new Thread(() -> {
            EventEntity event = new EventEntity();
            event.title = title;
            event.timeStart = timeStart;
            event.timeEnd = timeEnd;
            event.allDay = allDay;
            event.category = category;
            event.location = location;
            event.description = description;
            event.calendarId = calendarId;
            event.repeatRule = repeatRule;

            if (getArguments() != null && getArguments().containsKey("editEventId")) {
                int eventId = getArguments().getInt("editEventId");
                EventEntity old = db.eventDao().getById(eventId);

                event.id = eventId;
                event.dayId = old.dayId;

                boolean repeatChanged = !Objects.equals(old.repeatRule, event.repeatRule);

                if (!editableExdates.isEmpty()) {
                    event.excludedDates = editableExdates.stream()
                            .map(LocalDate::toString)
                            .sorted()
                            .collect(Collectors.joining(","));
                } else {
                    event.excludedDates = null;
                }

                if (repeatChanged) {
                    event.excludedDates = null;
                } else {
                    if (!editableExdates.isEmpty()) {
                        event.excludedDates = editableExdates.stream()
                                .map(LocalDate::toString)
                                .sorted()
                                .collect(Collectors.joining(","));
                    } else {
                        event.excludedDates = null;
                    }
                }


                db.eventDao().update(event);
            } else {
                long timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                DayEntity day = db.dayDao().getByTimestampAndCalendarId(timestamp, calendarId);
                if (day == null) {
                    day = new DayEntity();
                    day.timestamp = timestamp;
                    day.calendarId = calendarId;
                    day.id = (int) db.dayDao().insert(day);
                }

                event.dayId = day.id;

                if (!editableExdates.isEmpty()) {
                    event.excludedDates = editableExdates.stream()
                            .map(LocalDate::toString)
                            .sorted()
                            .collect(Collectors.joining(","));
                } else {
                    event.excludedDates = null;
                }


                db.eventDao().insert(event);
            }



            requireActivity().runOnUiThread(() -> {
                if (listener != null) listener.onEventSaved();
                dismiss();
            });
        }).start();
    }



    private int toMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }


    private Integer preselectedCalendarId = null;
    public void setPreselectedCalendarId(Integer id) {
        this.preselectedCalendarId = id;
    }


        private void setupSpinners() {

        if (preselectedCalendarId != null) {
            for (int i = 0; i < calendarEntities.size(); i++) {
                if (calendarEntities.get(i).id == preselectedCalendarId) {
                    spinnerCalendar.setSelection(i);
                    break;
                }
            }
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Работа", "Личное", "Встреча", "Учёба"));
        spinnerCategory.setAdapter(categoryAdapter);

        new Thread(() -> {
            List<CalendarEntity> calendars = db.calendarDao().getAll();
            List<String> titles = new ArrayList<>();
            for (CalendarEntity cal : calendars) titles.add(cal.title);

            requireActivity().runOnUiThread(() -> {
                calendarEntities.clear();
                calendarEntities.addAll(calendars);

                ArrayAdapter<String> calendarAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_dropdown_item, titles);
                spinnerCalendar.setAdapter(calendarAdapter);

                if (preselectedCalendarId != null) {
                    for (int i = 0; i < calendarEntities.size(); i++) {
                        if (calendarEntities.get(i).id == preselectedCalendarId) {
                            spinnerCalendar.setSelection(i);
                            break;
                        }
                    }
                }
            });
        }).start();
    }

    private void setupTimePickers() {
        btnStart.setOnClickListener(v -> showTimePicker((time) -> {
            selectedStart = time;
            btnStart.setText("Начало: " + time);
        }));
        btnEnd.setOnClickListener(v -> showTimePicker((time) -> {
            selectedEnd = time;
            btnEnd.setText("Конец: " + time);
        }));
    }

    private void showTimePicker(Consumer<String> onPicked) {
        TimePickerDialog dialog = new TimePickerDialog(getContext(),
                (view, hour, minute) -> {
                    String time = String.format("%02d:%02d", hour, minute);
                    onPicked.accept(time);
                }, 9, 0, true);
        dialog.show();
    }
}