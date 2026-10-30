package com.rekans.passwordgenerator;

import android.app.Activity;
import android.app.AlertDialog;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.security.SecureRandom;

public class MainActivity extends Activity {

    private final SecureRandom random = new SecureRandom();

    private static final int RED = Color.rgb(225, 29, 72);

    private int accent = RED;
    private int bg = Color.rgb(9, 9, 12);
    private int surface = Color.rgb(29, 17, 22);
    private ValueAnimator ringAnimator;

    private LinearLayout main;
    private TextView passwordText, lengthValue, strengthText;
    private SeekBar lengthBar;
    private CheckBox upperBox, lowerBox, numberBox, symbolBox;
    private String password = "";
    private boolean visible = true;

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        showLaunchSplash();
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout.LayoutParams params(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    private void addGap(LinearLayout parent, int height) {
        parent.addView(new View(this), params(1, dp(height)));
    }

    private TextView centered(String value, int size, int color, boolean bold) {
        TextView t = text(value, size, color, bold);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private Button button(String label, boolean primary) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(Color.WHITE);
        b.setBackground(shape(primary ? accent : Color.rgb(36, 45, 58), 14));
        b.setPadding(dp(12), dp(8), dp(12), dp(8));
        return b;
    }

    private LinearLayout card() {
        LinearLayout c = column();
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        c.setBackground(shape(surface, 18));
        return c;
    }


    private void showLaunchSplash() {
        FrameLayout splash = new FrameLayout(this);
        splash.setBackgroundColor(Color.rgb(9, 9, 12));

        LinearLayout content = column();
        content.setGravity(Gravity.CENTER);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_launcher);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        content.addView(logo, params(dp(120), dp(120)));
        TextView name = centered("rEKANS", 28, Color.WHITE, true);
        content.addView(name, params(-1, dp(48)));
        TextView subtitle = centered("PASSWORD GENERATOR", 12, RED, true);
        content.addView(subtitle, params(-1, dp(28)));

        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(
            -1, -2, Gravity.CENTER);
        splash.addView(content, cp);
        setContentView(splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) showWelcome();
        }, 1200);
    }

    private void showAppMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("About");
        menu.getMenu().add("Developer");
        menu.getMenu().add("Contact developer");
        menu.getMenu().add("Privacy");
        menu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            String message;
            if ("About".equals(title)) {
                message = "rEKANS Password Generator\\n"
                    + "Generate random passwords using selected character types.";
            } else if ("Developer".equals(title)) {
                message = "Developer: rEKANS";
            } else if ("Contact developer".equals(title)) {
                message = "Email: amisiklvn@gmail.com";
            } else {
                message = "Passwords are generated on this device. "
                    + "Avoid sharing generated passwords or storing them "
                    + "where other people can access them.";
            }
            new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
            return true;
        });
        menu.show();
    }

    private final class BrokenRing extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float rotation = 0f;

        BrokenRing(Context context) {
            super(context);
            paint.setColor(RED);
            paint.setStyle(Paint.Style.FILL);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            ringAnimator = ValueAnimator.ofFloat(0f, 360f);
            ringAnimator.setDuration(1800);
            ringAnimator.setRepeatCount(ValueAnimator.INFINITE);
            ringAnimator.setInterpolator(new LinearInterpolator());
            ringAnimator.addUpdateListener(animation -> {
                rotation = (float) animation.getAnimatedValue();
                invalidate();
            });
            ringAnimator.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float radius = Math.min(cx, cy) * 0.78f;
            int count = 12;
            for (int i = 0; i < count; i++) {
                // Leave a gap in the circle; dots rotate around the logo.
                if (i >= 8 && i <= 9) continue;
                double angle = Math.toRadians(rotation + i * 360f / count);
                float x = cx + (float)Math.cos(angle) * radius;
                float y = cy + (float)Math.sin(angle) * radius;
                float dot = (i % 3 == 0) ? dp(3) : dp(2);
                paint.setAlpha(90 + (i * 13) % 166);
                canvas.drawCircle(x, y, dot, paint);
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            if (ringAnimator != null) {
                ringAnimator.cancel();
                ringAnimator = null;
            }
            super.onDetachedFromWindow();
        }
    }

    private void showWelcome() {
        // Full-screen vertical layout.
        LinearLayout root = column();
        root.setBackgroundColor(bg);
        root.setPadding(dp(20), dp(24), dp(20), 0);

        // Menu stays at the top-right.
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

        Button menuButton = button("⋮", true);
        menuButton.setTextSize(26);
        menuButton.setTextColor(Color.WHITE);
        menuButton.setBackground(shape(Color.rgb(225, 29, 72), 12));
        menuButton.setMinWidth(dp(48));
        menuButton.setContentDescription("Open menu");

        topBar.addView(menuButton, new LinearLayout.LayoutParams(
            dp(48), dp(44)));
        menuButton.setOnClickListener(v -> showAppMenu(menuButton));

        root.addView(topBar, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));

        // This area takes all remaining height and centres the content.
        FrameLayout middle = new FrameLayout(this);

        LinearLayout content = column();
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(0, dp(8), 0, dp(8));

        FrameLayout logoFrame = new FrameLayout(this);
        BrokenRing ring = new BrokenRing(this);
        logoFrame.addView(ring, new FrameLayout.LayoutParams(
            dp(144), dp(144), Gravity.CENTER));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_launcher);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logoFrame.addView(logo, new FrameLayout.LayoutParams(
            dp(104), dp(104), Gravity.CENTER));

        content.addView(logoFrame, params(-1, dp(150)));

        addGap(content, 8);

        content.addView(centered("Password Generator", 26,
            Color.WHITE, true), params(-1, dp(44)));

        addGap(content, 6);

        content.addView(centered(
            "Welcome! Create strong passwords in seconds to help protect your online accounts.",
            14, Color.LTGRAY, false), params(-1, dp(64)));

        content.addView(centered(
            "Simple. Secure. Private.",
            15, accent, true), params(-1, dp(32)));

        addGap(content, 18);

        Button start = button("GET STARTED   →", true);
        content.addView(start, params(-1, dp(54)));
        start.setOnClickListener(v -> showGenerator());

        FrameLayout.LayoutParams contentParams =
            new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);

        middle.addView(content, contentParams);

        root.addView(middle, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // Footer stays at the physical bottom of the app content.
        TextView footer = centered(
            "PRIVATE BY rEKANS", 12, Color.GRAY, true);

        root.addView(footer, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(42)));

        setContentView(root);
    }

    private void showGenerator() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        main = column();
        main.setPadding(dp(18), dp(14), dp(18), dp(28));
        main.setBackgroundColor(bg);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.addView(main, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout header = new FrameLayout(this);

        TextView screenTitle = centered(
            "Password Generator", 20, Color.WHITE, true);
        header.addView(screenTitle, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        main.addView(header, params(-1, dp(54)));

        addGap(main, 5);
        main.addView(text("Secure your digital life, one password at a time.",
            14, Color.LTGRAY, false));

        addGap(main, 20);
        LinearLayout passwordCard = card();

        LinearLayout cardHeader = new LinearLayout(this);
        cardHeader.setGravity(Gravity.CENTER_VERTICAL);
        cardHeader.addView(text("YOUR PASSWORD", 12,
            Color.LTGRAY, true), new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        cardHeader.addView(text("● SECURE", 11,
            Color.rgb(52, 211, 153), true));
        passwordCard.addView(cardHeader);

        addGap(passwordCard, 20);
        passwordText = centered("Tap Generate to begin", 21,
            Color.WHITE, true);
        passwordText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        passwordCard.addView(passwordText, params(-1, dp(55)));

        strengthText = text("Ready to generate", 12,
            Color.LTGRAY, false);
        passwordCard.addView(strengthText);

        addGap(passwordCard, 14);
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button visibility = button("Show / Hide", false);
        Button copy = button("Copy", true);
        actions.addView(visibility, new LinearLayout.LayoutParams(0,
            dp(48), 1));
        View gap = new View(this);
        actions.addView(gap, params(dp(10), 1));
        actions.addView(copy, new LinearLayout.LayoutParams(0,
            dp(48), 1));
        passwordCard.addView(actions);

        visibility.setOnClickListener(v -> toggleVisibility());
        copy.setOnClickListener(v -> copyPassword());
        main.addView(passwordCard);

        addGap(main, 22);
        LinearLayout lengthCard = card();
        LinearLayout lengthHeader = new LinearLayout(this);
        lengthHeader.setGravity(Gravity.CENTER_VERTICAL);
        lengthHeader.addView(text("Password length", 16,
            Color.WHITE, true), new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        lengthValue = text("16", 18, accent, true);
        lengthHeader.addView(lengthValue);
        lengthCard.addView(lengthHeader);

        lengthBar = new SeekBar(this);
        lengthBar.setMax(124);
        lengthBar.setProgress(12);
        lengthCard.addView(lengthBar, params(-1, dp(42)));
        lengthBar.setOnSeekBarChangeListener(
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar,
                    int progress, boolean fromUser) {
                    lengthValue.setText(String.valueOf(progress + 4));
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });

        main.addView(lengthCard);

        addGap(main, 22);
        main.addView(text("CHARACTER TYPES", 12, Color.LTGRAY, true));
        addGap(main, 8);

        LinearLayout options = card();
        upperBox = check("Uppercase letters (A–Z)", false);
        lowerBox = check("Lowercase letters (a–z)", false);
        numberBox = check("Numbers (0–9)", false);
        symbolBox = check("Symbols (!@#$…)", false);

        options.addView(upperBox);
        options.addView(lowerBox);
        options.addView(numberBox);
        options.addView(symbolBox);
        main.addView(options);

        addGap(main, 22);
        Button generate = button("GENERATE PASSWORD", true);
        main.addView(generate, params(-1, dp(56)));
        generate.setOnClickListener(v -> generatePassword());

        addGap(main, 14);
        main.addView(centered("Keep your passwords private.",
            12, Color.GRAY, false), params(-1, dp(32)));

        setContentView(scroll);
        updateTheme();
    }

    private CheckBox check(String label, boolean checked) {
        CheckBox c = new CheckBox(this);
        c.setText(label);
        c.setTextSize(14);
        c.setTextColor(Color.WHITE);
        c.setButtonTintList(android.content.res.ColorStateList.valueOf(accent));
        c.setChecked(checked);
        return c;
    }

    private void updateTheme() {
        accent = RED;
        bg = Color.rgb(9, 9, 12);
        surface = Color.rgb(29, 17, 22);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        if (main != null) {
            main.setBackgroundColor(bg);
            applyThemeToView(main);
        }
        if (lengthValue != null) lengthValue.setTextColor(accent);
        if (lengthBar != null) {
            lengthBar.setProgressTintList(
                android.content.res.ColorStateList.valueOf(accent));
            lengthBar.setThumbTintList(
                android.content.res.ColorStateList.valueOf(accent));
        }
        if (upperBox != null) {
            upperBox.setButtonTintList(
                android.content.res.ColorStateList.valueOf(accent));
            lowerBox.setButtonTintList(
                android.content.res.ColorStateList.valueOf(accent));
            numberBox.setButtonTintList(
                android.content.res.ColorStateList.valueOf(accent));
            symbolBox.setButtonTintList(
                android.content.res.ColorStateList.valueOf(accent));
        }
    }

    private int themeSecondary() {
        return Color.rgb(51, 25, 34);
    }

    private void applyThemeToView(View view) {
        Object tag = view.getTag();

        if ("rekans-card".equals(tag)) {
            view.setBackground(shape(surface, 18));
        } else if ("rekans-primary-button".equals(tag)) {
            view.setBackground(shape(accent, 14));
        } else if ("rekans-secondary-button".equals(tag)) {
            view.setBackground(shape(themeSecondary(), 14));
        } else if ("rekans-accent-text".equals(tag)
                && view instanceof TextView) {
            ((TextView) view).setTextColor(accent);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyThemeToView(group.getChildAt(i));
            }
        }
    }

    private void generatePassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String symbols = "!@#$%^&*()-_=+[]{}?";

        StringBuilder pool = new StringBuilder();
        StringBuilder result = new StringBuilder();
        if (upperBox.isChecked()) {
            pool.append(upper);
            result.append(upper.charAt(random.nextInt(upper.length())));
        }
        if (lowerBox.isChecked()) {
            pool.append(lower);
            result.append(lower.charAt(random.nextInt(lower.length())));
        }
        if (numberBox.isChecked()) {
            pool.append(digits);
            result.append(digits.charAt(random.nextInt(digits.length())));
        }
        if (symbolBox.isChecked()) {
            pool.append(symbols);
            result.append(symbols.charAt(random.nextInt(symbols.length())));
        }

        if (pool.length() == 0) {
            Toast.makeText(this, "Select at least one character type",
                Toast.LENGTH_SHORT).show();
            return;
        }

        int target = lengthBar.getProgress() + 4;
        while (result.length() < target) {
            result.append(pool.charAt(random.nextInt(pool.length())));
        }

        char[] chars = result.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        password = new String(chars);
        visible = true;
        passwordText.setText(password);
        strengthText.setText(target >= 16
            ? "Long password • harder to guess"
            : "Shorter password • consider using 16+ characters");
    }

    private void toggleVisibility() {
        if (password.isEmpty()) {
            Toast.makeText(this, "Generate a password first",
                Toast.LENGTH_SHORT).show();
            return;
        }
        visible = !visible;
        passwordText.setText(visible ? password : "•".repeat(password.length()));
    }

    private void copyPassword() {
        if (password.isEmpty()) {
            Toast.makeText(this, "Generate a password first",
                Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard =
            (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Password", password));
        Toast.makeText(this, "Password copied",
            Toast.LENGTH_SHORT).show();
    }
}
