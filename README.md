# Kai Finance - Personal Finance & Wealth Manager

Aplikasi manajemen keuangan pribadi modern berbasis Android Jetpack Compose dengan Room Database lokal, keamanan PIN Salted SHA-256 & Biometrik, ekspor/impor 9 entitas, dan penjadwalan transaksi otomatis.

---

## 📱 Cara Mengunduh & Memasang File `.apk`

### Opsi 1: Unduh Langsung File APK yang Sudah Siap
File APK sudah berhasil di-build dan siap diunduh langsung dari proyek ini:
- **Lokasi File:** `apk/KaiFinance.apk` atau `KaiFinance.apk` di folder utama.
- **Cara Unduh:**
  1. Pada panel File Explorer di sebelah kiri editor AI Studio, cari file `KaiFinance.apk` atau folder `apk/KaiFinance.apk`.
  2. Klik kanan pada file tersebut dan pilih **Download** (atau gunakan menu export AI Studio).
  3. Kirim file `.apk` ke ponsel Android Anda (via WhatsApp, Telegram, Google Drive, atau kabel data USB).
  4. Buka file `.apk` di ponsel Android Anda dan pilih **Install** (izinkan "Install from unknown sources" jika diminta).

---

### Opsi 2: Otomatis Build di GitHub (GitHub Actions CI/CD)
Jika Anda push kode ini ke repository **GitHub**, kami sudah menyiapkan pipeline otomatis:
- **Workflow file:** `.github/workflows/build-apk.yml`
- **Cara Kerja:**
  1. Push repository ke GitHub (`git push origin main`).
  2. Buka tab **Actions** di repository GitHub Anda.
  3. Pilih workflow **Build Android APK** (berjalan otomatis setiap kali ada push atau dapat dijalankan manual lewat tombol *Run workflow*).
  4. Setelah selesai (centang hijau), klik nama workflow run tersebut, lalu unduh file `.apk` di bagian **Artifacts** (`KaiFinance-debug-apk`).

---

## ✨ Fitur Utama Aplikasi
1. **Keamanan Maksimal:**
   - Proteksi PIN dengan Salted SHA-256 Cryptographic Hash.
   - Autentikasi Biometrik (Fingerprint & Face Unlock) dengan transisi mulus ke PIN.
2. **Manajemen Akun Fleksibel:**
   - Multi-akun: Tunai (Cash), Bank, dan Dompet Digital (E-Wallet).
   - Transfer instan antar akun tanpa merusak riwayat transaksi.
3. **Pencatatan Keuangan Lengkap:**
   - Transaksi Pengeluaran, Pemasukan, dan Transfer.
   - Hutang & Piutang (Debts) dengan pelacakan cicilan pelunasan bertahap.
   - Transaksi Berulang (Recurring) dengan event-driven Exact AlarmManager.
   - Anggaran Bulanan (Budget) & Notifikasi Ambang Batas.
   - Portofolio Investasi & Target Tabungan (Financial Goals).
4. **Keandalan & Audit Data:**
   - **Data Integrity Checker:** Pemeriksaan otomatis selisih saldo vs mutasi riil + Rekonsiliasi satu klik.
   - **Safe CSV Importer:** Tampilan preview baris valid/rusak sebelum dimasukkan ke database.
   - **Full Backup & Restore 9 Entitas:** Cadangan seluruh data format JSON.
