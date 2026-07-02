# Invoice Flow - Bill Mind Pro

Native Kotlin Android billing, GST invoice, stock, and customer credit management app built with Jetpack Compose and Material 3.

## Included Features

- Login screen and modern Home dashboard
- Inventory and stock management
- GST invoice creation
- Customer and item dropdown selection while creating invoices
- PDF invoice generation
- Android share sheet for invoice PDFs
- Sales analytics dashboard
- Clickable monthly sales analytics amount breakdown
- Payment due reminders with Android notifications
- Customer credit tracking
- Monthly profit/loss report
- QR-style invoice verification image
- Room database with Kotlin Flow
- Hilt dependency injection
- WorkManager payment reminders
- Material 3 dark mode and dynamic color support

## Build

Open this folder in Android Studio, or run:

```powershell
.\gradlew.bat assembleDebug
```

## Notes

- Data is stored locally with Room.
- PDF generation uses Android `PdfDocument`.
- Invoice sharing uses Android `FileProvider`.
- Reminder notifications use WorkManager and Android notifications.
- QR codes are generated locally from invoice data with ZXing.
