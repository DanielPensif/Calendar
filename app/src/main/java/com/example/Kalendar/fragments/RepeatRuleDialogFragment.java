package com.example.Kalendar.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.Kalendar.R;

import org.threeten.bp.LocalDate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RepeatRuleDialogFragment extends DialogFragment {

    public interface OnRepeatSelectedListener {
        void onRepeatSelected(String ruleText, String displayText);
    }

    private OnRepeatSelectedListener listener;

    private EditText inputCount;

    private LocalDate[] untilDate;

    public void setOnRepeatSelectedListener(OnRepeatSelectedListener listener) {
        this.listener = listener;
    }

    public static RepeatRuleDialogFragment newInstance(String repeatRule) {
        RepeatRuleDialogFragment frag = new RepeatRuleDialogFragment();
        Bundle args = new Bundle();
        args.putString("rule", repeatRule);
        frag.setArguments(args);
        return frag;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_repeat_rule, container, false);

        Spinner freqSpinner = view.findViewById(R.id.spinnerFreq);
        EditText inputInterval = view.findViewById(R.id.inputInterval);
        RadioGroup endGroup = view.findViewById(R.id.radioEndGroup);
        inputCount = view.findViewById(R.id.inputCount);
        Button btnPickDate = view.findViewById(R.id.btnPickDate);
        Button btnDone = view.findViewById(R.id.btnDone);

        inputCount.setEnabled(false);
        btnPickDate.setEnabled(false);

        untilDate = new LocalDate[]{null};

        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Каждый день", "Каждую неделю", "Каждый месяц", "Каждый год"));
        freqSpinner.setAdapter(freqAdapter);

        endGroup.setOnCheckedChangeListener((group, checkedId) -> {
            inputCount.setEnabled(checkedId == R.id.radioCount);
            btnPickDate.setEnabled(checkedId == R.id.radioUntil);
            inputCount.setVisibility(checkedId == R.id.radioCount ? View.VISIBLE : View.GONE);
            btnPickDate.setVisibility(checkedId == R.id.radioUntil ? View.VISIBLE : View.GONE);
        });

        btnPickDate.setOnClickListener(v -> {
            LocalDate today = LocalDate.now();
            DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                    (d, y, m, dOfM) -> {
                        untilDate[0] = LocalDate.of(y, m + 1, dOfM);
                        btnPickDate.setText("До: " + untilDate[0]);
                    },
                    today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth());
            dialog.show();
        });

        btnDone.setOnClickListener(v -> {
            int freqIndex = freqSpinner.getSelectedItemPosition();
            String freq = switch (freqIndex) {
                case 1 -> "WEEKLY";
                case 2 -> "MONTHLY";
                case 3 -> "YEARLY";
                default -> "DAILY";
            };

            int interval = 1;
            try {
                interval = Integer.parseInt(inputInterval.getText().toString().trim());
            } catch (Exception ignored) {}

            StringBuilder rule = new StringBuilder("FREQ=" + freq);

            rule.append(";INTERVAL=").append(interval);

            int selectedEnd = endGroup.getCheckedRadioButtonId();
            if (selectedEnd == R.id.radioCount) {
                String count = inputCount.getText().toString().trim();
                if (!count.isEmpty()) rule.append(";COUNT=").append(count);
            } else if (selectedEnd == R.id.radioUntil && untilDate[0] != null) {
                rule.append(";UNTIL=").append(untilDate[0].toString().replace("-", ""));
            }

            String displayText = generateDisplayText(freq, interval, selectedEnd);

            if (listener != null) listener.onRepeatSelected(rule.toString(), displayText);
            dismiss();
        });


        if (getArguments() != null && getArguments().containsKey("rule")) {
            String rule = getArguments().getString("rule");

            if (rule != null && !rule.isEmpty()) {
                Map<String, String> parts = new HashMap<>();
                for (String part : rule.split(";")) {
                    String[] kv = part.split("=");
                    if (kv.length == 2) parts.put(kv[0], kv[1]);
                }

                String freq = parts.get("FREQ");
                int freqIndex = switch (freq != null ? freq : "") {
                    case "DAILY" -> 0;
                    case "WEEKLY" -> 1;
                    case "MONTHLY" -> 2;
                    case "YEARLY" -> 3;
                    default -> 0;
                };
                freqSpinner.setSelection(freqIndex);
                inputInterval.setText(parts.getOrDefault("INTERVAL", "1"));

                if (parts.containsKey("COUNT")) {
                    endGroup.check(R.id.radioCount);
                    inputCount.setText(parts.get("COUNT"));
                    inputCount.setVisibility(View.VISIBLE);
                } else if (parts.containsKey("UNTIL")) {
                    endGroup.check(R.id.radioUntil);
                    String s = parts.get("UNTIL");
                    if (s != null && s.length() == 8) {
                        LocalDate until = LocalDate.of(
                                Integer.parseInt(s.substring(0, 4)),
                                Integer.parseInt(s.substring(4, 6)),
                                Integer.parseInt(s.substring(6, 8))
                        );
                        untilDate[0] = until;
                        btnPickDate.setText("До: " + until);
                        btnPickDate.setVisibility(View.VISIBLE);
                    }
                } else {
                    endGroup.check(R.id.radioNever);
                }
            }
        }

        return view;
    }

    private String generateDisplayText(String freq, int interval, int selectedEnd) {
        String base = switch (freq) {
            case "DAILY" -> (interval == 1) ? "Каждый день" : "Каждые " + interval + " дней";
            case "WEEKLY" -> (interval == 1) ? "Каждую неделю" : "Каждые " + interval + " недель";
            case "MONTHLY" -> (interval == 1) ? "Каждый месяц" : "Каждые " + interval + " месяцев";
            case "YEARLY" -> (interval == 1) ? "Каждый год" : "Каждые " + interval + " лет";
            default -> "Повтор: не определён";
        };

        if (selectedEnd == R.id.radioCount) {
            String count = inputCount.getText().toString().trim();
            if (!count.isEmpty()) {
                base += ", " + count + " раз";
            }
        } else if (selectedEnd == R.id.radioUntil && untilDate[0] != null) {
            base += ", до " + untilDate[0];
        }

        return base;
    }

}
