package com.example.Kalendar.fragments;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import com.example.Kalendar.R;
import com.example.Kalendar.adapters.CalendarSpinnerAdapter;
import com.example.Kalendar.adapters.CategorySpinnerAdapter;
import com.example.Kalendar.adapters.EventReminderReceiver;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.*;
import com.example.Kalendar.utils.EventUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import org.threeten.bp.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddEventDialogFragment extends BottomSheetDialogFragment {
    private static final String ARG_DATE="date", ARG_EDIT="editEventId";
    private LocalDate date;
    private AppDatabase db;
    private int userId;
    private EditText etTitle, etLoc, etDesc;
    private Button bStart, bEnd, bRepeat, bRestore, bSave;
    private Spinner spCat, spCal;
    private CheckBox cbAllDay, cbEarly, cbNotify;
    private TimePicker tpEarly;
    private ChipGroup cgEx;
    private LinearLayout llEx;
    private String selStart="09:00", selEnd="10:00", rule=null;
    private Set<LocalDate> exdates=new HashSet<>();
    private List<CalendarEntity> cals=new ArrayList<>();
    private List<CategoryEntity> cats=new ArrayList<>();
    private Integer editingId=null, preCal=null;
    private OnEventSavedListener listener;
    private CategorySpinnerAdapter ca;
    public interface OnEventSavedListener{void onEventSaved();}
    public void setOnEventSavedListener(OnEventSavedListener l){listener=l;}
    public void setPreselectedCalendarId(Integer id){preCal=id;}
    public static AddEventDialogFragment newInstance(LocalDate d){
        AddEventDialogFragment f=new AddEventDialogFragment();
        Bundle b=new Bundle();b.putString(ARG_DATE,d.toString());
        f.setArguments(b);return f;
    }
    public static AddEventDialogFragment editInstance(int id,LocalDate d){
        AddEventDialogFragment f=new AddEventDialogFragment();
        Bundle b=new Bundle();
        b.putInt(ARG_EDIT,id);
        b.putString(ARG_DATE,d.toString());
        f.setArguments(b);return f;
    }
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,@Nullable ViewGroup p,@Nullable Bundle bs){
        View v=inf.inflate(R.layout.fragment_add_event,p,false);
        db=AppDatabase.getDatabase(requireContext());
        date=LocalDate.parse(Objects.requireNonNull(requireArguments().getString(ARG_DATE)));
        userId=SessionManager.getLoggedInUserId(requireContext());

        etTitle=v.findViewById(R.id.inputTitle);
        etLoc=v.findViewById(R.id.inputLocation);
        etDesc=v.findViewById(R.id.inputDescription);
        bStart=v.findViewById(R.id.btnStartTime);
        bEnd=v.findViewById(R.id.btnEndTime);
        cbAllDay=v.findViewById(R.id.checkAllDay);
        cbEarly=v.findViewById(R.id.checkReminderEarly);
        tpEarly=v.findViewById(R.id.timePickerEarly);
        cbNotify=v.findViewById(R.id.checkNotifyEvent);
        spCat=v.findViewById(R.id.spinnerCategory);
        spCal=v.findViewById(R.id.spinnerCalendar);
        bRepeat=v.findViewById(R.id.btnRepeatRule);
        llEx=v.findViewById(R.id.excludedDatesLayout);
        cgEx=v.findViewById(R.id.chipGroupExcluded);
        bRestore=v.findViewById(R.id.btnRestoreExcluded);
        bSave=v.findViewById(R.id.btnSaveEvent);

        tpEarly.setIs24HourView(true); tpEarly.setVisibility(View.GONE);
        setupSpinners(v);
        setupPickers();

        cbEarly.setOnCheckedChangeListener((b,chk)->tpEarly.setVisibility(chk?View.VISIBLE:View.GONE));
        cbAllDay.setOnCheckedChangeListener((b,all)->{bStart.setEnabled(!all);bEnd.setEnabled(!all);});

        bRepeat.setText("Повтор: не повторяется");
        bRepeat.setOnClickListener(x->{
            RepeatRuleDialogFragment dlg=RepeatRuleDialogFragment.newInstance(rule);
            dlg.setOnRepeatSelectedListener((r,txt)->{rule=r;bRepeat.setText(txt);});
            dlg.show(getParentFragmentManager(),"rrule");
        });
        bRestore.setOnClickListener(x->{exdates.clear();bRestore.setVisibility(View.GONE);renderEx();});
        bSave.setOnClickListener(x->saveEvent());

        if(getArguments().containsKey(ARG_EDIT)) loadForEdit();
        return v;
    }

    private void setupSpinners(View v){
        ca = new CategorySpinnerAdapter(
                requireContext(), cats, db, userId,
                () -> ca.notifyDataSetChanged()
        );
        spCat.setAdapter(ca);
        CalendarSpinnerAdapter calA=new CalendarSpinnerAdapter(requireContext(),cals);
        spCal.setAdapter(calA);

        new Thread(()->{
            cats.clear();cats.addAll(db.categoryDao().getAllForUser(userId));
            cals.clear();cals.addAll(db.calendarDao().getByUserId(userId));
            new Handler(Looper.getMainLooper()).post(()->{
                ca.notifyDataSetChanged();
                calA.notifyDataSetChanged();
                if(preCal!=null){
                    for(int i=0;i<cals.size();i++)
                        if(cals.get(i).getId()==preCal)spCal.setSelection(i);
                }
            });
        }).start();
    }

    private void setupPickers(){
        bStart.setOnClickListener(x->pickTime(t->{
            selStart=t; bStart.setText("Начало: "+t);
        }));
        bEnd.setOnClickListener(x->pickTime(t->{
            selEnd=t; bEnd.setText("Конец: "+t);
        }));
    }

    private void renderEx(){
        llEx.setVisibility(exdates.isEmpty()?View.GONE:View.VISIBLE);
        cgEx.removeAllViews();
        for(LocalDate d:new TreeSet<>(exdates)){
            Chip chip=new Chip(requireContext());
            chip.setText(d.toString());
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(x->{
                exdates.remove(d);
                renderEx();
                if(exdates.isEmpty())bRestore.setVisibility(View.GONE);
            });
            cgEx.addView(chip);
        }
    }

    private void loadForEdit(){
        editingId=getArguments().getInt(ARG_EDIT);
        new Thread(()->{
            EventEntity e=db.eventDao().getById(editingId);
            if(e==null)return;
            new Handler(Looper.getMainLooper()).post(()->{
                etTitle.setText(e.title);
                etLoc.setText(e.location);
                etDesc.setText(e.description);
                cbAllDay.setChecked(e.allDay);
                selStart=e.timeStart; bStart.setText("Начало: "+selStart);
                selEnd=e.timeEnd; bEnd.setText("Конец: "+selEnd);
                for(int i=0;i<cats.size();i++) if(cats.get(i).getName().equals(e.category))spCat.setSelection(i);
                for(int i=0;i<cals.size();i++) if(cals.get(i).getId()==e.calendarId)spCal.setSelection(i);
                cbEarly.setChecked(e.earlyReminderEnabled);
                tpEarly.setHour(e.earlyReminderHour);
                tpEarly.setMinute(e.earlyReminderMinute);
                cbNotify.setChecked(e.notifyOnStart);
                if(e.repeatRule!=null){rule=e.repeatRule; bRepeat.setText("Повтор: "+EventUtils.parseDisplayFromRule(rule));}
                if(e.excludedDates!=null){
                    exdates=EventUtils.parseExcludedDates(e.excludedDates);
                    bRestore.setVisibility(View.VISIBLE);
                    renderEx();
                }
            });
        }).start();
    }

    private void saveEvent(){
        String t=etTitle.getText().toString().trim();
        if(t.isEmpty()){Toast.makeText(getContext(),"Введите название",Toast.LENGTH_SHORT).show();return;}
        boolean all=cbAllDay.isChecked();
        if(!all){
            // проверка
            int sH=Integer.parseInt(selStart.split(":")[0]),sM=Integer.parseInt(selStart.split(":")[1]);
            int eH=Integer.parseInt(selEnd.split(":")[0]),eM=Integer.parseInt(selEnd.split(":")[1]);
            if(eH*60+eM <= sH*60+sM){
                Toast.makeText(getContext(),"Конец раньше начала",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        CategoryEntity cat=(CategoryEntity)spCat.getSelectedItem();
        int calId=cals.get(spCal.getSelectedItemPosition()).getId();
        boolean early=cbEarly.isChecked();
        int eh=tpEarly.getHour(), em=tpEarly.getMinute();
        boolean notify=cbNotify.isChecked();
        String loc=etLoc.getText().toString().trim();
        String desc=etDesc.getText().toString().trim();

        new Thread(()->{
            long ts=date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000;
            DayEntity day=db.dayDao().getByTimestampAndCalendarId(ts,calId);
            if(day==null){
                day=new DayEntity();
                day.setTimestamp(ts);;
                day.setCalendarId(calId);
                day.setId((int)db.dayDao().insert(day));
            }
            boolean isEdit = editingId!=null;
            EventEntity e = isEdit
                    ? db.eventDao().getById(editingId)
                    : new EventEntity();
            if(!isEdit)e.done=false;

            e.title=t; e.location=loc; e.description=desc;
            e.timeStart=all?"00:00":selStart;
            e.timeEnd=all?"23:59":selEnd;
            e.allDay=all;
            e.category=cat.getName();
            e.calendarId=calId;
            e.repeatRule=rule;
            e.earlyReminderEnabled=early;
            e.earlyReminderHour=eh; e.earlyReminderMinute=em;
            e.notifyOnStart=notify;
            e.dayId=day.getId();
            e.userId=userId;
            if(!exdates.isEmpty()){
                e.excludedDates=exdates.stream()
                                .map(LocalDate::toString)
                                .collect(Collectors.joining(","));
            }

            if(isEdit) db.eventDao().update(e);
            else e.id=(int)db.eventDao().insert(e);

            requireActivity().runOnUiThread(()->{
                if(listener!=null)listener.onEventSaved();
                dismiss();
            });
            scheduleAlarms(e);
        }).start();
    }

    private void scheduleAlarms(EventEntity e){
        // раннее напоминание
        if(cbEarly.isChecked()){
            scheduleAlarm(e.id,"Напоминание: "+e.title,e.description,e.earlyReminderHour,e.earlyReminderMinute);
        }
        if(cbNotify.isChecked()){
            scheduleAlarm(e.id+1000,"Самое время: "+e.title,e.description,0,0);
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleAlarm(int code, String title, String text,int hr,int mn){
        Intent i=new Intent(requireContext(), EventReminderReceiver.class);
        i.putExtra("title",title);
        i.putExtra("text",text);
        PendingIntent pi=PendingIntent.getBroadcast(
                requireContext(),code,i,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager am=requireContext().getSystemService(AlarmManager.class);
        org.threeten.bp.ZonedDateTime start = date.atTime(hr,mn).atZone(ZoneId.systemDefault());
        am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                start.toInstant().toEpochMilli(),
                pi
        );
    }

    private void pickTime(Consumer<String> cb){
        new TimePickerDialog(
                requireContext(),
                (tp,h,m)->cb.accept(String.format("%02d:%02d",h,m)),
                9,0,true
        ).show();
    }
}