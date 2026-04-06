# MedVault — Full Project Prompt for Antigravity

## What you are building

Build a mobile application called **MedVault** — a local-first, privacy-first personal health records manager. The core problem it solves: patients in India (and globally) visit multiple hospitals and doctors, and no single doctor can see their full medical history — past prescriptions, drug allergies, X-rays, lab reports. MedVault fixes this by letting patients consolidate all their medical records on their own phone, with zero data sent to any cloud or server.

---

## Platform & Tech Stack

- **Platform**: Android only, minimum SDK 26 (Android 8.0), target SDK 34
- **Language**: Kotlin — do not use Java anywhere in the codebase
- **UI framework**: Jetpack Compose (no XML layouts)
- **Architecture**: MVVM — ViewModels, StateFlow, Repository pattern. One repository per domain (VisitRepository, ContactRepository, MediaRepository).
- **Database**: Room (`androidx.room`) on top of Android's built-in SQLite — no Firebase, no Supabase, no any cloud backend whatsoever
- **File storage**: `context.filesDir` for binary files (images, PDFs) — Room stores metadata and file paths only, never binary blobs
- **Data serialization**: kotlinx.serialization for JSON columns inside Room (e.g. the medicines list stored as a JSON string in the prescriptions table)
- **OCR**: Google ML Kit Text Recognition (`com.google.mlkit:text-recognition`) — on-device, free, no API key needed
- **AI summaries**: Use a lightweight external API (Gemini Flash or GPT-4o-mini) called only with explicit user consent. Default state is "AI not configured." User must go to Settings, enter their own API key, and acknowledge a privacy warning before AI features activate. All AI calls are made from a dedicated `AiSummaryRepository` that can be swapped for an on-device model later without touching any other code. If no API key is set, stub the function to return null and show "AI not available" in the UI.
- **HTTP client**: Retrofit 2 + OkHttp (used only for AI summary calls, no other network traffic)
- **Search**: Room FTS4 (Full-Text Search) — declare an `@Fts4` entity mirroring the visits table; Room generates the virtual FTS table and handles indexing automatically
- **PDF viewing**: `AndroidPdfViewer` library (`com.github.barteksc:android-pdf-viewer`)
- **PDF export**: Generate HTML string on-device, convert using `android.print.PrintDocumentAdapter` or `PdfDocument` API
- **Camera**: CameraX (`androidx.camera`)
- **Image loading**: Coil (`io.coil-kt:coil-compose`)
- **Auth**: `androidx.biometric:biometric` for fingerprint/face unlock, with a PIN fallback stored as a bcrypt hash in `EncryptedSharedPreferences`
- **Dependency injection**: Hilt
- **Build**: Gradle with Kotlin DSL (`build.gradle.kts`)

---

## Absolute constraints — do not violate these

1. **No network calls for any medical data, ever**, except the AI summary call which is strictly opt-in (see tech stack). No analytics (no Firebase Analytics, no Crashlytics, no Mixpanel, no Sentry, nothing). The app works 100% offline if the user does not configure an AI API key.
2. AI summary calls must only fire after the user has explicitly entered an API key in Settings and acknowledged the warning: "Your prescription text will be sent to an external AI service. Do not proceed if you are uncomfortable with this." This warning must be shown once on setup and accessible again in Settings.
3. All data files live in `context.filesDir` (the app's private internal storage) — not in external storage, not in the public media gallery, not in Downloads. Other apps cannot read them.
4. No ads, no in-app purchases, no tracking of any kind.
5. Kotlin only — no Java files anywhere in the project.

---

## Data Architecture — Room + SQLite

The database is a single Room database (`MedVaultDatabase`) with three tables and one FTS virtual table. Binary files (images, PDFs) are stored on disk in `context.filesDir`; the database stores only their paths.

---

### Table 1: `visits`

```kotlin
@Entity(tableName = "visits")
data class VisitEntity(
    @PrimaryKey val visitId: String,           // UUID v4
    val date: String,                           // "YYYY-MM-DD" — indexed for sort
    val doctorName: String,
    val doctorPhone: String?,
    val hospitalName: String?,
    val doctorSpecialty: String?,
    val diagnosis: String?,
    val notes: String?,
    val nextAppointmentDate: String?,
    val tags: String,                           // JSON array stored as string e.g. '["antibiotics","infection"]'
    val createdAt: Long,                        // epoch millis
    val updatedAt: Long
)
```

---

### Table 2: `prescriptions`

One-to-one with visits. A visit always has at most one prescription row.

```kotlin
@Entity(
    tableName = "prescriptions",
    foreignKeys = [ForeignKey(
        entity = VisitEntity::class,
        parentColumns = ["visitId"],
        childColumns = ["visitId"],
        onDelete = ForeignKey.CASCADE       // deleting a visit deletes its prescription
    )]
)
data class PrescriptionEntity(
    @PrimaryKey val prescriptionId: String, // UUID v4
    val visitId: String,                    // FK → visits.visitId
    val rawText: String?,                   // OCR output or manually typed text
    val photoUri: String?,                  // relative path e.g. "prescriptions/{visitId}.jpg"
    val medicines: String,                  // JSON array of Medicine objects (see below)
    val aiSummary: String?,                 // JSON of AiSummary object, null until generated
    val aiSummaryGeneratedAt: Long?         // epoch millis, null until generated
)
```

The `medicines` column stores a JSON array serialized with kotlinx.serialization:
```kotlin
@Serializable
data class Medicine(
    val name: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val instructions: String?
)
```

Use a Room `TypeConverter` that calls `Json.encodeToString(medicines)` on write and `Json.decodeFromString(raw)` on read.

---

### Table 3: `attachments`

One-to-many with visits. A visit can have any number of attachments.

```kotlin
@Entity(
    tableName = "attachments",
    foreignKeys = [ForeignKey(
        entity = VisitEntity::class,
        parentColumns = ["visitId"],
        childColumns = ["visitId"],
        onDelete = ForeignKey.CASCADE       // deleting a visit deletes all its attachments
    )]
)
data class AttachmentEntity(
    @PrimaryKey val mediaId: String,        // UUID v4
    val visitId: String,                    // FK → visits.visitId
    val type: String,                       // "xray" | "report" | "before" | "after" | "other"
    val localUri: String,                   // relative path e.g. "media/{visitId}/{mediaId}.jpg"
    val caption: String?,
    val capturedAt: Long                    // epoch millis
)
```

---

### Table 4: `visit_fts` (Full-Text Search virtual table)

```kotlin
@Fts4(contentEntity = VisitEntity::class)
@Entity(tableName = "visit_fts")
data class VisitFts(
    val doctorName: String,
    val hospitalName: String,
    val diagnosis: String,
    val notes: String,
    val tags: String
)
```

Room keeps this in sync automatically. Query it with:
```kotlin
@Query("SELECT * FROM visits JOIN visit_fts ON visits.rowid = visit_fts.rowid WHERE visit_fts MATCH :query")
fun search(query: String): Flow<List<VisitEntity>>
```

---

### Table 5: `contacts`

Separate table, not joined to visits. Used only for autocomplete in the Add Visit form.

```kotlin
@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val doctorName: String,
    val doctorPhone: String?,
    val hospitalName: String?,
    val specialty: String?,
    val lastVisited: String           // "YYYY-MM-DD"
)
```

Upsert on every visit save: if a doctor name already exists, update `lastVisited` and any changed fields.

---

### DAOs

```kotlin
@Dao
interface VisitDao {
    @Query("SELECT * FROM visits ORDER BY date DESC")
    fun getAllVisits(): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE visitId = :id")
    suspend fun getVisitById(id: String): VisitEntity?

    @Transaction
    @Query("SELECT * FROM visits WHERE visitId = :id")
    suspend fun getVisitWithDetails(id: String): VisitWithDetails?

    @Upsert
    suspend fun upsertVisit(visit: VisitEntity)

    @Query("DELETE FROM visits WHERE visitId = :id")
    suspend fun deleteVisit(id: String)

    @Query("SELECT * FROM visits JOIN visit_fts ON visits.rowid = visit_fts.rowid WHERE visit_fts MATCH :query ORDER BY date DESC")
    fun search(query: String): Flow<List<VisitEntity>>
}

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions WHERE visitId = :visitId")
    suspend fun getForVisit(visitId: String): PrescriptionEntity?

    @Upsert
    suspend fun upsertPrescription(prescription: PrescriptionEntity)
}

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE visitId = :visitId ORDER BY capturedAt ASC")
    fun getForVisit(visitId: String): Flow<List<AttachmentEntity>>

    @Upsert
    suspend fun upsertAttachment(attachment: AttachmentEntity)

    @Query("DELETE FROM attachments WHERE mediaId = :mediaId")
    suspend fun deleteAttachment(mediaId: String)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts WHERE doctorName LIKE '%' || :query || '%' ORDER BY lastVisited DESC LIMIT 10")
    suspend fun search(query: String): List<ContactEntity>

    @Upsert
    suspend fun upsertContact(contact: ContactEntity)
}
```

---

### Relation — VisitWithDetails

Use a Room `@Relation` data class to load a full visit in one query:

```kotlin
data class VisitWithDetails(
    @Embedded val visit: VisitEntity,
    @Relation(parentColumn = "visitId", entityColumn = "visitId")
    val prescription: PrescriptionEntity?,
    @Relation(parentColumn = "visitId", entityColumn = "visitId")
    val attachments: List<AttachmentEntity>
)
```

---

### File storage layout (unchanged — Room stores paths, not files)

```
context.filesDir/
  medvault/
    prescriptions/
      {visitId}.jpg          ← prescription photos
    media/
      {visitId}/
        {mediaId}.jpg        ← X-rays, before/after photos
        {mediaId}.pdf        ← lab reports, other PDFs
```

Store all file paths in Room as **relative paths** (e.g. `medvault/prescriptions/abc123.jpg`). Resolve to absolute by prepending `context.filesDir.absolutePath` at read time. This prevents breakage on backup/restore.

---

### Database migrations

Define a Room migration for every schema change — never use `fallbackToDestructiveMigration()` in production. Even in v1, set up the migration infrastructure from day one:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // example: db.execSQL("ALTER TABLE visits ADD COLUMN newField TEXT")
    }
}
```

---

### Repository rules

- All three repositories (`VisitRepository`, `ContactRepository`, `MediaRepository`) are Hilt-injected singletons.
- `VisitRepository` coordinates across `VisitDao`, `PrescriptionDao`, and `AttachmentDao` — callers never talk to DAOs directly.
- When deleting a visit: Room `CASCADE` handles database rows automatically. `MediaRepository` must additionally delete the physical files from `context.filesDir`.
- All `suspend` functions run on `Dispatchers.IO` (enforce via `withContext(Dispatchers.IO)` inside the repository, not the ViewModel).
- DAOs that return `Flow` are collected in ViewModels — never in the repository layer.

---

## Screens & Navigation

Use Jetpack Compose Navigation (`androidx.navigation:navigation-compose`) with a `NavHost`. Bottom navigation bar for the two main tabs: Timeline and Settings. All other screens are pushed onto the back stack.

### Screen 1: Timeline (Home tab)

- Shows all visits as a scrollable list of cards, sorted newest first by default
- Each card displays: date, doctor name, hospital, the first sentence of the AI summary (or "Summary pending..." if not yet generated), and an icon row showing count of media attachments
- Top bar has: sort toggle (newest/oldest), filter button, search icon
- Filter panel (slide-up sheet): filter by date range, by doctor name (multi-select from contacts), by tag, by media type present (has X-ray, has report, has image)
- Search: real-time full-text search across prescription text, doctor names, diagnosis, AI summaries, tags — powered by MiniSearch
- FAB (floating action button) at bottom right: "Add visit"
- Empty state: friendly illustration with text "No visits yet. Tap + to add your first prescription."

### Screen 2: Add / Edit Visit (modal stack)

A multi-step form. Steps are shown as a progress indicator at the top.

**Step 1 — Doctor info**
- Doctor name (text, with autocomplete from contacts.json — show matching doctors as a dropdown as the user types)
- Doctor phone / hospital phone (numeric)
- Hospital name (text, autocomplete)
- Doctor specialty (text)
- Visit date (date picker, defaults to today)
- Diagnosis (text, optional)
- Notes (multi-line text, optional)
- Next appointment date (date picker, optional)
- Tags (free-text chips, user types and presses enter to add)

**Step 2 — Prescription**
Two sub-options presented as two buttons: "Take a photo" and "Type manually". User picks one (or both).

*Photo flow:*
- Opens CameraX viewfinder in a full-screen Composable
- After capture, shows preview with "Retake" and "Use this" buttons
- On "Use this": run ML Kit Text Recognition on the captured image
- Show a loading indicator while OCR runs
- Best-effort parse the OCR output into medicine name / dosage / frequency / duration rows
- User reviews and corrects the extracted medicines

*Manual flow:*
- Shows the medicine entry form directly

*Medicine entry form (used in both flows):*
- Repeatable rows: medicine name, dosage, frequency, duration, instructions
- "Add another medicine" button
- "Generate AI Summary" button — runs inference and shows result below
- AI summary display: three labelled sections — "What you were prescribed", "Why (if diagnosis provided)", "What to watch for"
- If AI is unavailable: show a placeholder card with a "Generate when AI is ready" state

**Step 3 — Attach media**
- Grid of attachment slots
- Each slot: tap to add — shows action sheet with options: "Take photo", "Choose from gallery", "Scan document / PDF"
- After adding, user sets: type (X-ray / Lab report / Before photo / After photo / Other) and caption (optional)
- Existing attachments shown as thumbnails with a remove button

**Step 4 — Review & Save**
- Summary card of everything entered
- "Save visit" button
- On save: upsert visit + prescription rows in a single Room `@Transaction`, upsert contact row, copy any new media files to `context.filesDir`, insert attachment rows

### Screen 3: Visit Detail

Reached by tapping any visit card on the Timeline.

Sections (scrollable):
1. **Header**: date, doctor name, hospital, specialty, phone (tappable — opens dialer)
2. **Diagnosis & notes**: shown if present
3. **AI Summary card**: collapsible. Shows the three-section summary. Bottom row: "Regenerate" button + "Generated on {date}" timestamp
4. **Prescription**: list of medicines as formatted cards (name, dosage, frequency, duration, instructions). If a prescription photo exists, show a thumbnail that opens the full image on tap.
5. **Attached media**: horizontal scrollable row of thumbnails. Tap opens full-screen viewer. Each thumbnail shows its type badge (X-ray, Report, etc.)
6. **Tags**: chip row
7. **Actions row**: Edit visit, Export as PDF, Delete visit

### Screen 4: Full-screen Media Viewer

- Swipe left/right between attachments for that visit
- Pinch to zoom on images
- PDF viewer for report files
- Bottom bar: caption text, type badge, capture date
- Top bar: close button, share button (opens system share sheet)

### Screen 5: Export Preview

Reached from "Export as PDF" on the Visit Detail screen.

- Checklist of what to include: AI summary (checked by default), prescription photo (checked), medicine list (checked), each media attachment individually (unchecked by default)
- "Generate PDF" button — renders an HTML template on-device and converts via react-native-html-to-pdf
- After generation: preview screen with "Share" button (opens system share sheet) and "Save to Files" button

### Screen 6: Settings tab

- **App lock**: toggle biometric on/off, set PIN, set auto-lock timeout (immediately / 1 min / 5 min / never)
- **AI model**: shows current model status (loaded / not found). Option to configure a custom API endpoint (with the privacy warning described above).
- **Data**: "Export all data as ZIP" — exports the Room database file (`medvault.db`) + all files from `context.filesDir/medvault/`, user shares via system share sheet. "Import data from ZIP" — replaces the current database and media files after a confirmation dialog. "Delete all data" — drops and recreates all Room tables, deletes all files from `context.filesDir/medvault/`, requires typing "DELETE" to confirm.
- **About**: app version, open source licenses

### Screen 7: Lock screen

- Shown on app open if lock is enabled, and after auto-lock timeout
- Biometric prompt auto-triggers
- PIN entry fallback below
- No data is visible behind the lock screen

---

## AI Summary — detailed spec

AI summaries are optional and off by default. They only activate after the user configures an API key in Settings.

The `AiSummaryRepository` exposes a single suspend function:

```kotlin
suspend fun generateSummary(
    medicines: List<Medicine>,
    diagnosis: String?,
    notes: String?
): AiSummary?   // returns null if no API key is configured or call fails
```

`AiSummary` is a data class:
```kotlin
data class AiSummary(
    val whatYouWerePrescribed: String,
    val why: String?,           // null if no diagnosis provided
    val whatToWatchFor: String
)
```

The prompt sent to the AI API must be:
> "You are a helpful medical assistant. Summarise the following prescription in plain language that a non-medical person can understand. Be concise. Do not add any information not present in the input. Output a JSON object with three keys: whatYouWerePrescribed, why (null if no diagnosis), whatToWatchFor. Prescription data: {serialized input}"

Use `response_format: json_object` if the API supports it (OpenAI / Gemini both do) to get a clean parseable response.

The `AiSummaryRepository` must be the only class that knows which API is being used. The ViewModel calls `generateSummary()` and does not know or care whether it hits Gemini, OpenAI, or a future on-device model. This makes swapping the AI backend a one-file change.

Cache the result by updating the `aiSummary` and `aiSummaryGeneratedAt` columns in the `prescriptions` table. Only regenerate if the user explicitly taps "Regenerate" or if the medicines list changes.

---



## UI & Design guidelines

- Use Material 3 (`androidx.compose.material3`) throughout — no Material 2 components
- Theme: light mode only for v1. Clean white surfaces, `surfaceVariant` for cards, one teal accent (`primary` color token set to a calm medical teal)
- All text in sentence case — never ALL CAPS on labels (override any Material 3 defaults that uppercase button text)
- Error states: always show a human-readable `Snackbar` message, never a raw exception message
- Loading states: `CircularProgressIndicator` with a descriptive label during OCR ("Reading prescription...") and AI inference ("Generating summary...")
- Skeleton loaders on the Timeline while visits load from disk
- Confirmation dialogs (`AlertDialog` composable) for all destructive actions: delete visit, delete all data
- The app must be fully usable without AI configured — hide the "Generate summary" button entirely if no API key is set, show a small tappable note "Set up AI in Settings" instead
- All Composables must support dark mode even though it's not a v1 requirement — use `MaterialTheme.colorScheme` tokens, never hardcoded colors

---

## Deliverables

1. Working Android Studio project (Kotlin + Jetpack Compose + Hilt + Room) — all screens navigable end to end, builds and runs on an Android emulator or device with no extra setup
2. Room database with `visits`, `prescriptions`, `attachments`, `contacts`, and `visit_fts` tables, all DAOs, TypeConverters, and migration infrastructure
3. Three Hilt-injected repositories (`VisitRepository`, `ContactRepository`, `MediaRepository`) — callers never touch DAOs directly
4. CameraX + ML Kit OCR prescription capture flow with manual override and correction
5. `AiSummaryRepository` with Retrofit integration — returns null when no API key is set, fully functional when configured
6. Timeline screen with sort, filter, and Room FTS search
7. Visit detail screen with AI summary card and media gallery, loading via `VisitWithDetails` relation
8. Full-screen media viewer with swipe navigation and pinch-to-zoom
9. PDF export flow (checklist → generate with `PdfDocument` API → share via Intent)
10. Biometric/PIN lock screen using `androidx.biometric`, with PIN hash stored in `EncryptedSharedPreferences`
11. Doctor contacts autocomplete in the Add Visit form, queried from the `contacts` table
12. Settings screen: AI API key config (with privacy warning), biometric toggle, database + media ZIP export/import, delete all data
13. A `README.md` covering: project setup, Room schema explanation, file layout, how to configure an AI API key, how to swap in a different AI provider, how to add a Room migration, how to do a manual data backup and restore

---

## What success looks like

A patient goes to the doctor, gets a prescription. They open MedVault, tap +, take a photo of the prescription with CameraX, correct the ML Kit OCR-extracted medicines if needed, add the doctor's details, attach the lab report PDF they received, and save. If they've set up an AI API key, the app generates a plain-language summary of what they were prescribed. Six months later, they visit a different hospital. In the waiting room they open MedVault, scroll their timeline, find the visit, and can show the new doctor the exact medicines they were on, any drug allergies noted, and the attached X-ray — all on their Android phone, with no internet connection required for anything except the optional AI summary.