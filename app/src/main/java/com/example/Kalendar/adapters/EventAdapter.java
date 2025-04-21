package com.example.Kalendar.adapters;
import android.app.AlertDialog;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.R;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.fragments.AddEventDialogFragment;
import com.example.Kalendar.fragments.EventsFragment;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.utils.EventUtils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.*;
import java.util.stream.Collectors;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private List<EventEntity> events;

    public EventAdapter(List<EventEntity> events) {
        this.events = events;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, time;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.eventTitle);
            time = itemView.findViewById(R.id.eventTime);
        }

        public void bind(EventEntity event) {
            TextView title = itemView.findViewById(R.id.eventTitle);
            TextView time = itemView.findViewById(R.id.eventTime);
            TextView category = itemView.findViewById(R.id.eventCategory);
            TextView description = itemView.findViewById(R.id.eventDescription);

            title.setText(event.title);
            category.setText("Категория: " + event.category);

            if (event.allDay) {
                time.setText("Весь день");
            } else {
                time.setText(event.timeStart + " – " + event.timeEnd);
            }

            if (event.description != null && !event.description.trim().isEmpty()) {
                description.setText(event.description);
                description.setVisibility(View.VISIBLE);
            } else {
                description.setVisibility(View.GONE);
            }


            itemView.setOnClickListener(v -> new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(v.getContext());
                DayEntity day = db.dayDao().getById(event.dayId);

                if (day != null) {
                    if (event.repeatRule != null && !event.repeatRule.isEmpty()) {
                        ((AppCompatActivity) v.getContext()).runOnUiThread(() -> {
                            new AlertDialog.Builder(v.getContext())
                                    .setTitle("Это повторяющееся событие")
                                    .setItems(new CharSequence[]{"Редактировать только это", "Редактировать всю серию", "Отмена"}, (dialog, which) -> {
                                        switch (which) {
                                            case 0:
                                                new Thread(() -> {
                                                    EventEntity single = new EventEntity();
                                                    single.title = event.title;
                                                    single.timeStart = event.timeStart;
                                                    single.timeEnd = event.timeEnd;
                                                    single.allDay = event.allDay;
                                                    single.category = event.category;
                                                    single.location = event.location;
                                                    single.description = event.description;
                                                    single.done = event.done;
                                                    single.repeatRule = null;
                                                    single.calendarId = event.calendarId;
                                                    single.excludedDates = null;

                                                    LocalDate date = ((EventsFragment) ((AppCompatActivity) v.getContext())
                                                            .getSupportFragmentManager()
                                                            .findFragmentByTag("f1")).getDate();

                                                    long timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                                                    DayEntity dayLocal = db.dayDao().getByTimestampAndCalendarId(timestamp, event.calendarId);

                                                    if (dayLocal == null) {
                                                        dayLocal = new DayEntity();
                                                        dayLocal.timestamp = timestamp;
                                                        dayLocal.calendarId = event.calendarId;
                                                        dayLocal.id = (int) db.dayDao().insert(dayLocal);
                                                    }

                                                    single.dayId = dayLocal.id;

                                                    EventEntity original = db.eventDao().getById(event.id);
                                                    if (original != null) {
                                                        Set<LocalDate> exdates = EventUtils.parseExcludedDates(original.excludedDates);
                                                        exdates.add(date);

                                                        StringBuilder result = new StringBuilder();
                                                        for (LocalDate d : exdates) {
                                                            if (result.length() > 0) result.append(",");
                                                            result.append(d);
                                                        }

                                                        original.excludedDates = result.toString();
                                                        db.eventDao().update(original);
                                                    }

                                                    int newId = (int) db.eventDao().insert(single);

                                                    ((AppCompatActivity) v.getContext()).runOnUiThread(() -> {
                                                        AddEventDialogFragment newDialog = AddEventDialogFragment.editInstance(newId, date);
                                                        newDialog.setOnEventSavedListener(() -> {
                                                            Fragment f = ((AppCompatActivity) v.getContext())
                                                                    .getSupportFragmentManager()
                                                                    .findFragmentByTag("f1");
                                                            if (f instanceof EventsFragment) ((EventsFragment) f).refresh();
                                                        });
                                                        newDialog.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "editSingle");
                                                    });
                                                }).start();
                                                break;

                                            case 1:
                                                openSeriesEditor(v, event);
                                                break;

                                            default:
                                                dialog.dismiss();
                                        }
                                    })
                                    .show();
                        });
                        return;
                    }

                    // Если это не повторяющееся событие
                    LocalDate date = Instant.ofEpochMilli(day.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    ((AppCompatActivity) v.getContext()).runOnUiThread(() -> {
                        AddEventDialogFragment dialog = AddEventDialogFragment.editInstance(event.id, date);
                        dialog.setOnEventSavedListener(() -> {
                            Fragment f = ((AppCompatActivity) v.getContext())
                                    .getSupportFragmentManager()
                                    .findFragmentByTag("f1");
                            if (f instanceof EventsFragment) ((EventsFragment) f).refresh();
                        });
                        dialog.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "editEvent");
                    });
                }
            }).start());

            itemView.setOnLongClickListener(v -> {
                if (event.repeatRule != null && !event.repeatRule.isEmpty()) {
                    // Повторяющееся событие — предложим варианты
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Удалить событие")
                            .setItems(new CharSequence[]{
                                    "Удалить только это",
                                    "Удалить всю серию",
                                    "Удалить все будущие"
                            }, (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        new Thread(() -> {
                                            AppDatabase db = AppDatabase.getDatabase(v.getContext());

                                            // Получаем оригинал
                                            EventEntity original = (event.id != 0)
                                                    ? db.eventDao().getById(event.id)
                                                    : db.eventDao().getFirstByRepeatRule(event.repeatRule);

                                            if (original != null) {
                                                // Получаем дату, которую нужно исключить
                                                String dateStr = event.date;
                                                if (dateStr == null || dateStr.isEmpty()) {
                                                    Fragment fragment = ((AppCompatActivity) v.getContext())
                                                            .getSupportFragmentManager().findFragmentByTag("f1");
                                                    if (fragment instanceof EventsFragment) {
                                                        dateStr = ((EventsFragment) fragment).getDate().toString();
                                                    }
                                                }

                                                if (dateStr != null) {
                                                    Set<LocalDate> exdates = EventUtils.parseExcludedDates(original.excludedDates);
                                                    exdates.add(LocalDate.parse(dateStr));

                                                    original.excludedDates = exdates.stream()
                                                            .map(LocalDate::toString)
                                                            .sorted()
                                                            .collect(Collectors.joining(","));

                                                    db.eventDao().update(original);
                                                }

                                                postRefresh(v);
                                            }
                                        }).start();

                                        break;

                                    case 1: // Вся серия
                                        deleteSeries(v, event);
                                        break;

                                    case 2: // Будущие
                                        deleteFutureOccurrences(v, event);
                                        break;
                                }
                            })
                            .show();
                } else {
                    // Обычное событие — удалить сразу
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Удалить событие?")
                            .setPositiveButton("Удалить", (d, w) -> deleteSingle(v, event))
                            .setNegativeButton("Отмена", null)
                            .show();
                }
                return true;
            });

            TextView seriesInfo = itemView.findViewById(R.id.eventSeriesInfo);

            if (event.repeatRule != null && !event.repeatRule.isEmpty()) {
                seriesInfo.setVisibility(View.VISIBLE);
                seriesInfo.setText(event.id == 0
                        ? "Экземпляр повторяющегося события"
                        : "Повторяющееся событие");
            } else {
                seriesInfo.setVisibility(View.GONE);
            }

        }

        private void deleteSingle(View v, EventEntity event) {
            new Thread(() -> {
                AppDatabase.getDatabase(v.getContext()).eventDao().delete(event);
                postRefresh(v);
            }).start();
        }


        private void deleteSeries(View v, EventEntity event) {
            new Thread(() -> {
                AppDatabase.getDatabase(v.getContext()).eventDao()
                        .deleteAllByRepeatRule(event.repeatRule);
                postRefresh(v);
            }).start();
        }


        private void deleteFutureOccurrences(View v, EventEntity event) {
            LocalDate targetDate = ((EventsFragment) Objects.requireNonNull(((AppCompatActivity) v.getContext())
                    .getSupportFragmentManager().findFragmentByTag("f1"))).getDate();

            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(v.getContext());
                EventEntity original = (event.id != 0)
                        ? db.eventDao().getById(event.id)
                        : db.eventDao().getFirstByRepeatRule(event.repeatRule);

                if (original == null || original.repeatRule == null) return;

                StringBuilder newRule = getStringBuilder(original, targetDate);

                original.repeatRule = newRule.toString();
                db.eventDao().update(original);

                postRefresh(v);
            }).start();
        }

        @NonNull
        private static StringBuilder getStringBuilder(EventEntity original, LocalDate targetDate) {
            Map<String, String> parts = new HashMap<>();
            for (String part : original.repeatRule.split(";")) {
                String[] kv = part.split("=");
                if (kv.length == 2) parts.put(kv[0], kv[1]);
            }

            parts.remove("COUNT");
            parts.remove("UNTIL");

            String until = targetDate.minusDays(1).toString().replace("-", "");
            parts.put("UNTIL", until);

            StringBuilder newRule = new StringBuilder();
            for (Map.Entry<String, String> e : parts.entrySet()) {
                if (newRule.length() > 0) newRule.append(";");
                newRule.append(e.getKey()).append("=").append(e.getValue());
            }
            return newRule;
        }


        private void postRefresh(View v) {
            ((AppCompatActivity) v.getContext()).runOnUiThread(() -> {
                Fragment f = ((AppCompatActivity) v.getContext())
                        .getSupportFragmentManager()
                        .findFragmentByTag("f1");
                if (f instanceof EventsFragment) ((EventsFragment) f).refresh();
            });
        }


        private void openSeriesEditor(View v, EventEntity event) {
            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(v.getContext());
                EventEntity original = event.id != 0
                        ? db.eventDao().getById(event.id)
                        : db.eventDao().getFirstByRepeatRule(event.repeatRule);

                if (original != null) {
                    DayEntity day = db.dayDao().getById(original.dayId);


                    if (day != null) {
                        LocalDate date = Instant.ofEpochMilli(day.timestamp)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();

                        ((AppCompatActivity) v.getContext()).runOnUiThread(() -> {
                            AddEventDialogFragment dialog = AddEventDialogFragment.editInstance(original.id, date);
                            dialog.setOnEventSavedListener(() -> {
                                Fragment f = ((AppCompatActivity) v.getContext())
                                        .getSupportFragmentManager()
                                        .findFragmentByTag("f1");
                                if (f instanceof EventsFragment) ((EventsFragment) f).refresh();
                            });
                            dialog.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "editEvent");
                        });
                    }
                }
            }).start();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<EventEntity> newEvents) {
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return events.size();
            }

            @Override
            public int getNewListSize() {
                return newEvents.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                EventEntity oldEvent = events.get(oldItemPosition);
                EventEntity newEvent = newEvents.get(newItemPosition);
                return oldEvent.id == newEvent.id && newEvent.repeatRule == null;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                EventEntity oldEvent = events.get(oldItemPosition);
                EventEntity newEvent = newEvents.get(newItemPosition);
                return oldEvent.title.equals(newEvent.title)
                        && oldEvent.timeStart.equals(newEvent.timeStart)
                        && oldEvent.timeEnd.equals(newEvent.timeEnd)
                        && oldEvent.allDay == newEvent.allDay
                        && oldEvent.category.equals(newEvent.category);
            }
        });

        events.clear();
        events.addAll(newEvents);
        diff.dispatchUpdatesTo(this);
    }

}


