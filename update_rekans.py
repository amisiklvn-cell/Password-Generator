from pathlib import Path
import re
import shutil
import subprocess
import sys

src = Path("app/src/main/java/com/rekans/passwordgenerator/MainActivity.java")
backup = Path("MainActivity-working.java")

if not src.exists() or not backup.exists():
    sys.exit("ERROR: Source or backup file is missing.")

# Start from the known working version; preserve the current source separately.
shutil.copy2(src, Path.home() / "MainActivity-before-welcome.java")
s = backup.read_text()

old = "        setContentView(scroll);\n        applyTheme();"
if old not in s:
    sys.exit("ERROR: Expected onCreate ending not found; source was not changed.")

s = s.replace(old, "        applyTheme();\n        showWelcomeScreen(scroll);", 1)

# Remove watermark as the root background.
s = s.replace("root.setBackground(watermark);", "root.setBackgroundColor(backgroundColor);")

welcome = r'''
    private void showWelcomeScreen(ScrollView mainScreen) {
        LinearLayout welcome = new LinearLayout(this);
        welcome.setOrientation(LinearLayout.VERTICAL);
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(dp(28), dp(30), dp(28), dp(30));
        welcome.setBackgroundColor(backgroundColor);

        TextView lock = new TextView(this);
        lock.setText("🔐");
        lock.setTextSize(52);
        lock.setGravity(Gravity.CENTER);
        welcome.addView(lock, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView brand = new TextView(this);
        brand.setText("rEKANS");
        brand.setTextSize(36);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setTextColor(accentColor);
        brand.setGravity(Gravity.CENTER);
        welcome.addView(brand, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView heading = new TextView(this);
        heading.setText("Welcome, User!");
        heading.setTextSize(25);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setTextColor(Color.WHITE);
        heading.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hp.topMargin = dp(28);
        welcome.addView(heading, hp);

        TextView message = new TextView(this);
        message.setText("Thanks for choosing the rEKANS app. This is the future.");
        message.setTextSize(16);
        message.setTextColor(Color.LTGRAY);
        message.setGravity(Gravity.CENTER);
        message.setLineSpacing(dp(4), 1.0f);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mp.topMargin = dp(14);
        welcome.addView(message, mp);

        TextView purposeTitle = new TextView(this);
        purposeTitle.setText("Your passwords. Your protection.");
        purposeTitle.setTextSize(18);
        purposeTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        purposeTitle.setTextColor(accentColor);
        purposeTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.topMargin = dp(30);
        welcome.addView(purposeTitle, tp);

        TextView purpose = new TextView(this);
        purpose.setText("Generate strong, random passwords to help protect your online accounts. Choose your password length, generate a password, and copy it when you're ready.");
        purpose.setTextSize(15);
        purpose.setTextColor(Color.LTGRAY);
        purpose.setGravity(Gravity.CENTER);
        purpose.setLineSpacing(dp(5), 1.0f);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pp.topMargin = dp(12);
        welcome.addView(purpose, pp);

        Button start = new Button(this);
        start.setText("GET STARTED  →");
        styleButton(start, true);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56));
        bp.topMargin = dp(38);
        welcome.addView(start, bp);
        start.setOnClickListener(v -> setContentView(mainScreen));

        setContentView(welcome);
    }

'''

# Insert welcome method before the class's last closing brace.
if "private void showWelcomeScreen(ScrollView mainScreen)" in s:
    sys.exit("ERROR: Welcome method already exists in backup.")
pos = s.rfind("}")
if pos < 0:
    sys.exit("ERROR: Could not find class closing brace.")
s = s[:pos] + welcome + s[pos:]

# Disable any watermark initialization in applyTheme, if present.
s = re.sub(
    r'\s*if \(watermark == null\) \{\s*watermark = new WatermarkDrawable\([^;]*;\s*\}\s*else \{\s*watermark\.setColor\([^;]*;\s*\}\s*root\.setBackground\(watermark\);',
    '\n        root.setBackgroundColor(backgroundColor);',
    s
)

src.write_text(s)
print("Source updated. Building APK...")

result = subprocess.run(["gradle", "assembleDebug"])
if result.returncode:
    shutil.copy2(Path.home() / "MainActivity-before-welcome.java", src)
    sys.exit("BUILD FAILED. Original source restored. Send me the full error.")

apk = Path("app/build/outputs/apk/debug/app-debug.apk")
if not apk.exists():
    sys.exit("Build finished but APK was not found.")

downloads = Path.home() / "storage/downloads"
downloads.mkdir(parents=True, exist_ok=True)
shutil.copy2(apk, downloads / "rEKANS-Password-Generator.apk")
print("SUCCESS: APK copied to Downloads/rEKANS-Password-Generator.apk")
