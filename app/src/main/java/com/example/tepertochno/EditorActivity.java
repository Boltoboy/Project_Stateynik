package com.example.tepertochno;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import jp.wasabeef.richeditor.RichEditor;

public class EditorActivity extends AppCompatActivity {
    private String currentFontFamily = "sans-serif";
    private int currentHtmlSize = 3;
    private String currentHexColor = "#000000";
    private boolean isBold = false;
    private boolean isItalic = false;
    private boolean isUnderline = false;
    private boolean isStrikeThrough = false;
    private TextView tvDemoFont, tvDemoColor, tvDemoSpecial;


    @Override
    protected void onCreate(Bundle Bundle) {
        super.onCreate(Bundle);
        setContentView(R.layout.activity_editor);

        EditText etTitle = findViewById(R.id.etTitle);
        EditText etTopic = findViewById(R.id.etTopic);
        Button btnSave = findViewById(R.id.btnSave);
        ImageButton btnFont = findViewById(R.id.btnImg1);
        TableLayout layoutFontSettings = findViewById(R.id.layoutFontSettings);
        Spinner spinnerFonts = findViewById(R.id.spinnerFonts);
        RichEditor mEditor = findViewById(R.id.etContent);

        ImageButton btnSpecial = findViewById(R.id.btnImg3);
        TableLayout layoutSpecialStyles = findViewById(R.id.layoutSpecialStyles);

        CheckBox cbBold = findViewById(R.id.cbBold);
        CheckBox cbItalic = findViewById(R.id.cbItalic);
        CheckBox cbUnderline = findViewById(R.id.cbUnderline);
        tvDemoFont = findViewById(R.id.tvDemoFont);
        tvDemoColor = findViewById(R.id.tvDemoColor);
        tvDemoSpecial = findViewById(R.id.tvDemoSpecial);

        btnSpecial.setOnClickListener(v -> {
            if (layoutSpecialStyles.getVisibility() == View.GONE) {
                layoutSpecialStyles.setVisibility(View.VISIBLE);
                findViewById(R.id.layoutFontSettings).setVisibility(View.GONE);
                findViewById(R.id.layoutColorSettings).setVisibility(View.GONE);
            } else {
                layoutSpecialStyles.setVisibility(View.GONE);
            }
        });

        View.OnClickListener checkBoxListener = v -> {
            isBold = cbBold.isChecked();
            isItalic = cbItalic.isChecked();
            isUnderline = cbUnderline.isChecked();
            applyCurrentWritingMode(mEditor);
        };

        cbBold.setOnClickListener(checkBoxListener);
        cbItalic.setOnClickListener(checkBoxListener);
        cbUnderline.setOnClickListener(checkBoxListener);

        mEditor.setEditorFontSize(16);
        mEditor.setPadding(10, 10, 10, 10);
        mEditor.setPlaceholder("Начните писать статью здесь...");

        btnFont.setOnClickListener(v -> {
            if (layoutFontSettings.getVisibility() == View.GONE) {
                layoutFontSettings.setVisibility(View.VISIBLE);
                findViewById(R.id.layoutColorSettings).setVisibility(View.GONE);
                findViewById(R.id.layoutSpecialStyles).setVisibility(View.GONE);
            } else {
                layoutFontSettings.setVisibility(View.GONE);
            }
        });

        String[] fonts = {"Стандартный", "Моноширинный", "С засечками (Serif)", "Без засечек (Sans)"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fonts);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFonts.setAdapter(spinnerAdapter);

        spinnerFonts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        currentFontFamily = "sans-serif";
                        break;
                    case 1:
                        currentFontFamily = "monospace";
                        break;
                    case 2:
                        currentFontFamily = "serif";
                        break;
                    case 3:
                        currentFontFamily = "cursive";
                        break;
                }
                applyCurrentWritingMode(mEditor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner spinnerSizes = findViewById(R.id.spinnerSizes);
        String[] textSizes = {"12 (Мелкий)", "14 (Обычный)", "18 (Средний)", "24 (Крупный)", "30 (Большой)", "36 (Огромный)"};
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, textSizes);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSizes.setAdapter(sizeAdapter);
        spinnerSizes.setSelection(1);

        spinnerSizes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        currentHtmlSize = 1;
                        break;
                    case 1:
                        currentHtmlSize = 3;
                        break;
                    case 2:
                        currentHtmlSize = 4;
                        break;
                    case 3:
                        currentHtmlSize = 5;
                        break;
                    case 4:
                        currentHtmlSize = 6;
                        break;
                    case 5:
                        currentHtmlSize = 7;
                        break;
                }
                applyCurrentWritingMode(mEditor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ImageButton btnColor = findViewById(R.id.btnImg2);
        LinearLayout layoutColorSettings = findViewById(R.id.layoutColorSettings);
        Spinner spinnerColors = findViewById(R.id.spinnerColors);

        btnColor.setOnClickListener(v -> {
            if (layoutColorSettings.getVisibility() == View.GONE) {
                layoutColorSettings.setVisibility(View.VISIBLE);
                findViewById(R.id.layoutFontSettings).setVisibility(View.GONE);
                findViewById(R.id.layoutSpecialStyles).setVisibility(View.GONE);
            } else {
                layoutColorSettings.setVisibility(View.GONE);
            }
        });

        String[] colorNames = {"Черный", "Красный", "Синий", "Зеленый", "Оранжевый", "Фиолетовый"};
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colorNames);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColors.setAdapter(colorAdapter);

        spinnerColors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] colorHexCodes = {"#000000", "#FF0000", "#0000FF", "#008000", "#FFA500", "#800080"};
                currentHexColor = colorHexCodes[position];
                applyCurrentWritingMode(mEditor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final boolean[] isStyleApplying = {false};
        mEditor.setOnTextChangeListener(text -> {
            if (isStyleApplying[0]) return;
            isStyleApplying[0] = true;
            mEditor.postDelayed(() -> {
                if (isFinishing()) return;
                mEditor.requestFocus();
                mEditor.requestFocusFromTouch();

                String jsCommand = "javascript:RE.focus(); " +
                        "document.execCommand('fontName', false, '" + currentFontFamily + "'); " +
                        "document.execCommand('fontSize', false, '" + currentHtmlSize + "'); " +
                        "document.execCommand('foreColor', false, '" + currentHexColor + "');";

                mEditor.evaluateJavascript(jsCommand, null);
                isStyleApplying[0] = false;
            }, 50);
        });

        btnSave.setOnClickListener(v -> {
            Log.d("MY_APP", "Начинаю сохранение...");
            String title = etTitle.getText().toString().trim();
            String topic = etTopic.getText().toString().trim();
            String content = mEditor.getHtml();

            if (title.isEmpty()) {
                etTitle.setError("Введите заголовок");
                return;
            }
            if (topic.isEmpty()) {
                etTopic.setError("Введите раздел");
                return;
            }
            if (content == null || content.trim().isEmpty()) {
                Toast.makeText(this, "Статья не может быть пустой", Toast.LENGTH_SHORT).show();
                return;
            }

            String databaseUrl = "https://tepertochno-82a9f-default-rtdb.europe-west1.firebasedatabase.app/";
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance(databaseUrl).getReference("users").child(uid);

            userRef.get().addOnSuccessListener(snapshot -> {
                User user = snapshot.getValue(User.class);
                String authorName = (user != null) ? user.name : "Аноним";
                String authorId = uid;

                DatabaseReference dbArticles = FirebaseDatabase.getInstance(databaseUrl).getReference("articles");
                String articleId = dbArticles.push().getKey();

                if (articleId != null) {
                    Article article = new Article(articleId, title, authorName, content, topic, authorId);

                    dbArticles.child(articleId).setValue(article);

                    DatabaseReference userArticlesRef = FirebaseDatabase.getInstance(databaseUrl)
                            .getReference("users")
                            .child(uid)
                            .child("myArticles");

                    userArticlesRef.child(articleId).setValue(article)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("MY_APP", "Успех сохранения везде!");
                                Toast.makeText(this, "Статья сохранена успешно!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("MY_APP", "Ошибка Firebase: " + e.getMessage());
                                Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                }
            });
        });
    }
    // Метод отправляет комплексную JavaScript-команду для одновременного применения всех выбранных стилей к редактору
    private void applyCurrentWritingMode(RichEditor editor) {
        editor.requestFocus();
        editor.requestFocusFromTouch();

        StringBuilder js = new StringBuilder("javascript:RE.focus(); " +
                "document.execCommand('fontName', false, '" + currentFontFamily + "'); " +
                "document.execCommand('fontSize', false, '" + currentHtmlSize + "'); " +
                "document.execCommand('foreColor', false, '" + currentHexColor + "'); ");

        js.append("if (document.queryCommandState('bold') !== ").append(isBold).append(") document.execCommand('bold', false, null); ");
        js.append("if (document.queryCommandState('italic') !== ").append(isItalic).append(") document.execCommand('italic', false, null); ");
        js.append("if (document.queryCommandState('underline') !== ").append(isUnderline).append(") document.execCommand('underline', false, null); ");

        if (isStrikeThrough) {
            js.append("document.execCommand('styleWithCSS', false, true); ");
            js.append("document.execCommand('strikeThrough', false, null); ");
            js.append("document.execCommand('styleWithCSS', false, false); ");
        } else {
            js.append("if (document.queryCommandState('strikeThrough')) document.execCommand('strikeThrough', false, null); ");
        }

        editor.evaluateJavascript(js.toString(), null);
        updateDemoViews();
    }
    private void updateDemoViews() {
        TextView[] demoViews = {tvDemoFont, tvDemoColor, tvDemoSpecial};

        for (TextView tv : demoViews) {
            if (tv == null) continue;

            tv.setTextColor(android.graphics.Color.parseColor(currentHexColor));

            float realSize = 14f;
            switch (currentHtmlSize) {
                case 1: realSize = 12f; break;
                case 3: realSize = 14f; break;
                case 4: realSize = 18f; break;
                case 5: realSize = 24f; break;
                case 6: realSize = 30f; break;
                case 7: realSize = 36f; break;
            }
            tv.setTextSize(realSize);

            Typeface baseTypeface = Typeface.DEFAULT;
            if ("monospace".equals(currentFontFamily)) baseTypeface = Typeface.MONOSPACE;
            else if ("serif".equals(currentFontFamily)) baseTypeface = Typeface.SERIF;
            else if ("cursive".equals(currentFontFamily)) baseTypeface = Typeface.create("cursive", Typeface.NORMAL);

            int styleFlags = Typeface.NORMAL;
            if (isBold && isItalic) styleFlags = Typeface.BOLD_ITALIC;
            else if (isBold) styleFlags = Typeface.BOLD;
            else if (isItalic) styleFlags = Typeface.ITALIC;

            tv.setTypeface(baseTypeface, styleFlags);

            int paintFlags = tv.getPaintFlags();
            paintFlags &= ~android.graphics.Paint.UNDERLINE_TEXT_FLAG;
            paintFlags &= ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;

            if (isUnderline) paintFlags |= android.graphics.Paint.UNDERLINE_TEXT_FLAG;
            if (isStrikeThrough) paintFlags |= android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;

            tv.setPaintFlags(paintFlags);
        }
    }



}
