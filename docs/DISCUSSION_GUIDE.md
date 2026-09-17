# دليل المناقشة — Discussion Guide

## المؤلفون — Authors

- المهندس محمد أبوهادي — Engineer Mohammed Abu Hadi
- المهندس حازم البقلي — Engineer Hazem Al-Baqli

---

## 1. ما هو النظام؟ — What is the system?

**بالعربية:**
نظام إدارة عيادة أكاديمي مبني بلغة Java 21. يحتوي على نماذج المرضى والأطباء والمواعيد والسجلات الطبية، مع طبقة خدمات وواجهات مستودعات وتطبيق كونسول تفاعلي.

**In English:**
An academic clinic management system built with Java 21. It contains Patient, Doctor, Appointment, and MedicalRecord models, with a service layer, repository interfaces, and an interactive console runtime.

---

## 2. لماذا كانت الصيانة مطلوبة؟ — Why was maintenance needed?

**بالعربية:**
النظام الأساسي لم يكن يتحقق من تعارض المواعيد. كان يسمح بحجز موعدين لنفس الطبيب في نفس الوقت. طلب التعديل SMR-001 يضيف كشف التعارض لمنع الحجز المزدوج.

**In English:**
The baseline system did not validate appointment conflicts. It allowed booking two appointments for the same doctor at the same time. SMR-001 adds conflict detection to prevent double-booking.

---

## 3. ما هو SMR-001؟ — What is SMR-001?

**بالعربية:**
طلب تعديل برمجي لإضافة كشف تعارض المواعيد. يتضمن: دعم مدة الموعد، فحص التعارض حسب الطبيب، حماية الجدولة والتحديث، وقواعد تصفية الحالات.

**In English:**
A Software Modification Request to add appointment conflict detection. It includes: duration support, doctor-scoped conflict checking, schedule/update protection, and status-aware filtering rules.

---

## 4. لماذا صيانة كمالية (Perfective)؟ — Why Perfective Maintenance?

**بالعربية:**
لأن التغيير يضيف وظيفة جديدة (كشف التعارض) لم تكن موجودة أصلاً. النظام كان يعمل قبل التغيير لكنه كان يفتقر إلى حماية سلامة الجدولة. لم يكن هناك خطأ يُصلح (ليست تصحيحية) ولم يكن هناك تغيير في المنصة الخارجية (ليست تكيفية). الخصائص الثانوية: وقائية (Preventive) لمنع مشاكل تكامل البيانات مستقبلاً.

**In English:**
The change adds new functionality (conflict detection) that did not previously exist. The system worked before but lacked scheduling integrity. There was no bug to fix (not Corrective) and no external platform change (not Adaptive). Secondary characteristics: Preventive — guarding against future data integrity issues.

---

## 5. ما هو تحليل الأثر (Impact Analysis)؟ — What is Impact Analysis?

**بالعربية:**
تحديد الملفات المتأثرة بالتغيير قبل البدء بالتنفيذ:
- تغييرات مباشرة: `Appointment.java`، `AppointmentService.java`، `schema.sql`
- مكونات جديدة: `AppointmentConflictException`، `InMemoryAppointmentRepository`، `AppointmentServiceTest`
- مكونات غير متأثرة: `Patient`، `Doctor`، `MedicalRecord`، وخدماتها

**In English:**
Identifying affected files before implementation:
- Direct changes: `Appointment.java`, `AppointmentService.java`, `schema.sql`
- New components: `AppointmentConflictException`, `InMemoryAppointmentRepository`, `AppointmentServiceTest`
- Unaffected: `Patient`, `Doctor`, `MedicalRecord`, and their services

---

## 6. ما دور AppointmentService؟ — What does AppointmentService do?

**بالعربية:**
هو المكون الرئيسي للصيانة. يحتوي على منطق كشف التعارض: قبل حفظ أو تحديث أي موعد، يفحص جدول الطبيب ويرمي `AppointmentConflictException` إذا وُجد تداخل.

**In English:**
It is the principal maintenance component. It contains conflict detection logic: before saving or updating any appointment, it checks the doctor's calendar and throws `AppointmentConflictException` if a blocking overlap is found.

---

## 7. ما هي صيغة التداخل؟ — What is the overlap formula?

**بالعربية:**
```
newStart < existingEnd && newEnd > existingStart
```
نستخدم فترة نصف مفتوحة: `[start, start + durationMinutes)`.

**In English:**
Half-open interval model: `[start, start + durationMinutes)`. Overlap occurs when the new interval starts before the existing one ends AND the new interval ends after the existing one starts.

---

## 8. ما هي الحالات المانعة (Blocking Statuses)؟

**بالعربية:**
- **تمنع الحجز:** `SCHEDULED` (مجدول) و `CONFIRMED` (مؤكد)
- **لا تمنع الحجز:** `COMPLETED` (مكتمل)، `CANCELLED` (ملغي)، `NO_SHOW` (لم يحضر)

**In English:**
- **Blocking:** `SCHEDULED` and `CONFIRMED`
- **Non-blocking:** `COMPLETED`, `CANCELLED`, `NO_SHOW`

---

## 9. ما هي قاعدة الحدود (Boundary Rule)؟

**بالعربية:**
المواعيد التي تتلامس حدودها (نهاية موعد = بداية موعد آخر) مسموحة ولا تُعتبر تعارضاً. هذا بسبب استخدام مقارنات صارمة (`<` و `>` بدلاً من `<=` و `>=`).

**In English:**
Boundary-touching appointments (one ends exactly when another starts) are allowed and do not conflict. This is because strict inequalities (`isBefore`/`isAfter`) evaluate to `false` when endpoints are equal.

---

## 10. ما هو إعادة الهيكلة (Refactoring) الذي تم؟

**بالعربية:**
تم تفكيك منطق التعارض المتكتل في `AppointmentService` إلى دوال مساعدة متماسكة:
- `calculateEndTime` — حساب نهاية الموعد
- `isBlockingStatus` — تصنيف الحالة
- `appointmentsOverlap` — فحص التداخل
- `conflictsWith` — تصفية الحالة + التداخل
- `isCurrentAppointment` — استبعاد النفس أثناء التعديل

42/42 اختبار نجحت قبل وبعد إعادة الهيكلة دون أي تغيير في السلوك.

**In English:**
Decomposed monolithic conflict logic into cohesive helper methods: `calculateEndTime`, `isBlockingStatus`, `appointmentsOverlap`, `conflictsWith`, `isCurrentAppointment`. 42/42 tests pass before and after. No behavioral change.

---

## 11. ما هو تقطيع البرنامج (Program Slicing)؟

**بالعربية:**
تحليل ثابت (static) لتتبع التبعيات:
- **شريحة خلفية** من `AppointmentConflictException`: ما المتغيرات التي تؤثر على رمي الاستثناء؟ (الموعد، الطبيب، الحالة، المدة، الأوقات)
- **شريحة أمامية** من `durationMinutes`: كيف تتدفق المدة إلى `calculateEndTime` ثم إلى قرار التداخل؟

لم يتم إجراء تقطيع ديناميكي.

**In English:**
Static analysis tracing dependencies:
- **Backward slice** from `AppointmentConflictException`: Which variables influence the exception? (appointment, doctor, status, duration, times)
- **Forward slice** from `durationMinutes`: How does duration flow through `calculateEndTime` into the overlap decision?

Dynamic slicing was NOT performed.

---

## 12. ما هي الهندسة العكسية (Reverse Engineering)؟

**بالعربية:**
استرجاع تصميم النظام من الكود المصدري:
- الهيكلة الطبقية (خدمات → واجهات مستودعات)
- نموذج المجال وعلاقاته
- 10 قواعد عمل (BR-01 إلى BR-10)
- توثيق الفجوات المعمارية (غياب تنفيذات JDBC)

**In English:**
Recovering system design from current source code:
- Layered architecture (services → repository interfaces)
- Domain model and relationships
- 10 business rules (BR-01 through BR-10)
- Documented architectural gaps (no JDBC implementations)

---

## 13. ما هو دليل الاختبارات؟ — Test Evidence

**بالعربية:**
```
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

30 اختبار نموذج + 12 اختبار خدمة = 42 اختبار ناجح. كل معايير القبول (AC-01 إلى AC-08) تم التحقق منها كـ PASS.

**In English:**
30 model tests + 12 service tests = 42 total, all passing. All acceptance criteria (AC-01 through AC-08) verified as PASS.

---

## 14. ما هي القيود المعمارية المعروفة؟ — Known Architecture Limitations

**بالعربية:**
1. **لا توجد تنفيذات JDBC:** الكود الإنتاجي لا يتضمن مستودعات متصلة بقاعدة بيانات MySQL؛ واجهات المستودعات في `com.clinic.repository` يتم تطبيقها لتشغيل الكونسول عبر مستودعات داخل الذاكرة (`com.clinic.repository.memory`).
2. **بيانات الكونسول مؤقتة:** جميع البيانات المدخلة في الكونسول تُخزن في الذاكرة وتُفقد عند إغلاق البرنامج.
3. **عدم ربط قاعدة البيانات حالياً:** ملف `schema.sql` وأداة `DatabaseConfig` جاهزان ومكتملان نحوياً، ولكنهما غير متصلين ببيئة تشغيل الكونسول الحالية.
4. **عزل بيئة الاختبار:** مستودع `InMemoryAppointmentRepository` في مجلد `src/test/java` مخصص لاختبارات الوحدة المعزولة.

**In English:**
1. **No JDBC persistence:** Production code does not implement JDBC database repositories; repository interfaces are satisfied at runtime by in-memory repositories (`com.clinic.repository.memory`).
2. **Volatile console data:** Console runtime data is stored in memory and lost upon program exit.
3. **Database disconnected from current runtime:** `schema.sql` and `DatabaseConfig` are prepared and valid, but not connected to the current console runner.
4. **Test double isolation:** `InMemoryAppointmentRepository` under `src/test/java` serves exclusively for isolated automated testing.

---

## 15. كيف نستعرض النظام بشكل حي؟ — How do we demonstrate the system live?

**بالعربية:**
يتم الاستعراض الحي عبر تطبيق الكونسول بالخطوات التالية:
1. تشغيل التطبيق عبر: `mvn compile` ثم `java -cp target\classes com.clinic.App`.
2. استخدام الطبيب (د. خالد - ID 1) والمريض (أحمد علي - ID 1) الموجودين في البيانات التجريبية.
3. حجز موعد صالح (د. خالد، الساعة 10:00، مدة 30 دقيقة، مجدول) $\rightarrow$ **يُقبل الحجز**.
4. محاولة حجز موعد متداخل لنفس الطبيب (د. خالد، الساعة 10:15، مدة 30 دقيقة) $\rightarrow$ **يُرفض الحجز** وتظهر رسالة التعارض الناتجة عن `AppointmentConflictException`.
5. محاولة حجز نفس الوقت (10:15) مع طبيب آخر (د. أمل - ID 2) $\rightarrow$ **يُقبل الحجز** لأن الفحص محدد بجدول الطبيب فقط (Doctor-Scoped).
6. تجربة موعد ملامس للحدود (د. خالد، الساعة 10:30، مدة 30 دقيقة) $\rightarrow$ **يُقبل الحجز** لأن تلامس الحدود مسموح في الفترات نصف المفتوحة.
7. توضيح أن `AppointmentService` هو من ينفذ هذا الفحص بالكامل، بينما `App.java` يمثل واجهة مستخدم فقط.

**In English:**
Live demonstration follows these interactive steps:
1. Run the console: `mvn compile` then `java -cp target\classes com.clinic.App`.
2. Use the seeded patient (Ahmed Ali - ID 1) and doctor (Dr. Khaled - ID 1).
3. Schedule a valid appointment: Dr. Khaled, 10:00, 30 min, SCHEDULED $\rightarrow$ **Accepted**.
4. Attempt an overlapping appointment for the same doctor: Dr. Khaled, 10:15, 30 min $\rightarrow$ **Rejected** with conflict message from `AppointmentConflictException`.
5. Attempt the same overlapping time with a different doctor: Dr. Amal, 10:15, 30 min $\rightarrow$ **Accepted** (doctor-scoped check).
6. Attempt boundary-touching booking: Dr. Khaled, 10:30, 30 min $\rightarrow$ **Accepted** (boundary rule).
7. Explain that `AppointmentService` performs the entire validation; `App.java` is strictly the presentation layer.

---

## 16. هل النظام متصل بقاعدة بيانات MySQL؟ — Is the system connected to MySQL?

**بالعربية:**
**لا.** تطبيق الكونسول التفاعلي الحالي يعمل باستخدام مستودعات داخل الذاكرة (In-Memory Repositories) في الحزمة `com.clinic.repository.memory`. مخطط قاعدة البيانات `schema.sql` وإعدادات الاتصال في `DatabaseConfig` و `db.properties` موجودة ومصممة وفق أفضل الممارسات، لكن ربط المستودعات الفعلية عبر JDBC يمثل خطوة تطويرية مستقبلية.

**In English:**
**No.** The current runnable console uses in-memory repository implementations in `com.clinic.repository.memory`. The MySQL schema (`schema.sql`) and connection configuration (`DatabaseConfig`, `db.properties`) exist and are structurally verified, but concrete JDBC persistence has not yet been connected to the runtime.
