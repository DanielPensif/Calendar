package com.example.Kalendar.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Kalendar.R;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CategoryEntity;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.List;

public class CategorySpinnerAdapter extends BaseAdapter {
    private final Context context;
    private final List<CategoryEntity> categories;
    private final LayoutInflater inflater;
    private final AppDatabase db;
    private final int userId;
    private final Runnable onCategoriesChanged;

    public CategorySpinnerAdapter(Context context,
                                  List<CategoryEntity> categories,
                                  AppDatabase db,
                                  int userId,
                                  Runnable onCategoriesChanged) {
        this.context = context;
        this.categories = categories;
        this.db = db;
        this.userId = userId;
        this.onCategoriesChanged = onCategoriesChanged;
        this.inflater = LayoutInflater.from(context);
        loadCategories();
    }

    @Override public int getCount() { return categories.size(); }
    @Override public Object getItem(int position) { return categories.get(position); }
    @Override public long getItemId(int position) { return categories.get(position).id; }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, parent);
    }

    @Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, parent);
    }

    private View createView(int position, ViewGroup parent) {
        View v = inflater.inflate(R.layout.item_spinner_category, parent, false);
        CategoryEntity cat = categories.get(position);

        View flag = v.findViewById(R.id.colorFlag);
        TextView name = v.findViewById(R.id.categoryName);
        ImageButton btnEdit = v.findViewById(R.id.btnEdit);
        ImageButton btnDelete = v.findViewById(R.id.btnDelete);

        btnEdit.setFocusable(false);
        btnDelete.setFocusable(false);

        Drawable d = context.getDrawable(R.drawable.flag_circle).mutate();
        d.setTint(Color.parseColor(cat.color));
        flag.setBackground(d);

        name.setText(cat.name);

        boolean isDefault = "Без категории".equals(cat.name);
        btnEdit.setVisibility(isDefault ? View.GONE : View.VISIBLE);
        btnDelete.setVisibility(isDefault ? View.GONE : View.VISIBLE);

        btnEdit.setOnClickListener(view -> showCategoryDialog(cat));
        btnDelete.setOnClickListener(view -> confirmDelete(cat));

        return v;
    }

    public void showCategoryDialog(CategoryEntity toEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(toEdit == null ? "Новая категория" : "Редактировать категорию");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * context.getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        EditText input = new EditText(context);
        input.setHint("Название");
        if (toEdit != null) input.setText(toEdit.name);

        final String[] selectedColor = { toEdit != null ? toEdit.color : "#808080" };
        Button colorBtn = new Button(context);
        colorBtn.setText("Выбрать цвет");
        colorBtn.setBackgroundColor(Color.parseColor(selectedColor[0]));

        colorBtn.setOnClickListener(v ->
                ColorPickerDialogBuilder
                        .with(context)
                        .setTitle("Выберите цвет")
                        .initialColor(Color.parseColor(selectedColor[0]))
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setPositiveButton("OK", (dialog, color, allColors) -> {
                            selectedColor[0] = String.format("#%06X", (0xFFFFFF & color));
                            colorBtn.setBackgroundColor(color);
                        })
                        .setNegativeButton("Отмена", null)
                        .build()
                        .show()
        );

        layout.addView(input);
        layout.addView(colorBtn);
        builder.setView(layout);

        builder.setPositiveButton("Сохранить", null);
        builder.setNegativeButton("Отмена", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = input.getText().toString().trim();
            if (title.isEmpty()) {
                input.setError("Введите название");
                return;
            }
            new Thread(() -> {
                CategoryEntity existing = db.categoryDao().getByNameAndUserId(title, userId);
                if (existing != null && (toEdit == null || existing.id != toEdit.id)) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Категория уже существует", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }
                if (toEdit == null) db.categoryDao().insert(new CategoryEntity(title, selectedColor[0], userId));
                else {
                    toEdit.name = title;
                    toEdit.color = selectedColor[0];
                    db.categoryDao().update(toEdit);
                }
                loadCategories();
                new Handler(Looper.getMainLooper()).post(dialog::dismiss);
            }).start();
        });
    }

    private void confirmDelete(CategoryEntity cat) {
        new AlertDialog.Builder(context)
                .setTitle("Удалить категорию?")
                .setMessage("Все задачи и события перейдут в 'Без категории'.")
                .setPositiveButton("Удалить", (d, w) -> new Thread(() -> {
                    String oldName = cat.name;
                    db.categoryDao().delete(cat);
                    db.categoryDao().reassignToDefault(oldName, userId);
                    loadCategories();
                }).start())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void loadCategories() {
        new Thread(() -> {
            List<CategoryEntity> list = db.categoryDao().getAllForUser(userId);
            int defId = db.categoryDao().getDefaultCategoryId(userId);
            CategoryEntity defaultCat = new CategoryEntity("Без категории", "#808080", userId);
            defaultCat.id = defId;

            categories.clear();
            categories.add(defaultCat);
            categories.addAll(list);

            new Handler(Looper.getMainLooper()).post(onCategoriesChanged);
        }).start();
    }
}