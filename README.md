<img src="https://github.com/user-attachments/assets/2f5335e3-b095-4e2f-a9de-e3ac46fbaf45" alt="MP Logo" width="100" height="100" align="right" />
<br><br><br>

# TopSpin - Training Planner for Table Tennis 🏓

A modern, cross-platform desktop client for easily creating, managing, and exporting table tennis training plans.

---

## ✨ Features

* **📝 Plan & Structure:** Easily create complete training plans. Organize them into clear training units (e.g., per weekday) and add specific exercises with details like duration, sets, and notes.
* **💾 Save & Load:** Never lose your work! Plans are automatically saved locally as easy-to-read JSON files in your application data folder and reloaded when you start the app.
* **🔍 Overview & Quick Access:** Stay organized! A clean startup view shows all your plans, which you can instantly filter and find using the integrated search bar.
* **⬆️ Integrated Updater:** Stay up-to-date! Topspin checks for new versions on GitHub Releases (on startup or via indicator click) and helps you through the update process (download & installer launch).
* **📄 PDF Export:** Take your plan to the training! Generate a printable PDF version of your training plan.
* **💻 Cross-Platform & Simple:** Use Topspin on Windows, macOS (Intel & Apple Silicon), or Linux. **No separate Java installation needed** – the required Java runtime is bundled right inside the installer!

## 📸 Screenshot (Current Version)

![Current TopSpin UI](https://github.com/user-attachments/assets/041029b4-cf95-4a61-bcbd-e2d351741c32)

*Current view shows the plan overview with search bar and "+ New Plan" button.*

---

## 🚀 Technology Stack

* **Language:** Java 21
* **UI Framework:** JavaFX 21
* **Build Tool:** Apache Maven
* **Dependency Injection:** Google Guice
* **JSON Processing:** Jackson Databind
* **Logging:** Logback (via custom LogFacade)
* **Packaging:** `jpackage` (JDK Tool)
* **CI/CD & Hosting:** GitHub Actions & GitHub Releases

---

## 🏁 Getting Started / Installation

**No separate Java installation is necessary!** The required Java runtime is bundled directly within the installation package.

1.  Go to the **[Releases Page](https://github.com/Metaphoriker/topspin/releases)**.
2.  Download the appropriate package for your operating system:
    * Windows: `.exe` file
    * macOS: `.dmg` file (choose x64 or aarch64/Apple Silicon)
    * Linux: `.deb` file (amd64)
3.  Run the downloaded file to install the application.
4.  Launch TopSpin via the Start Menu or the created shortcut.

---

## 💬 Feedback & Contributing

This project is under active development. Feedback is extremely valuable!

* **Bugs & Feature Requests:** Please report everything via **[GitHub Issues](https://github.com/Metaphoriker/topspin/issues)**.
* **Support:** If you like TopSpin or see its potential, please show your support by starring ⭐ the repository!

---

## 📄 License

TopSpin is released under the **[MIT License](LICENSE)**.

---

## 📧 Contact

For feedback, questions, and bug reports, please primarily use **[GitHub Issues](https://github.com/Metaphoriker/topspin/issues)**.
