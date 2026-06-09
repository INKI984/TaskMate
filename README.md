# TaskMate — Менаџер на задачи (Android)

> Проект по **ПМП — ФИКТ**, летен семестар 2026.
> Едноставна, но целосна Android апликација за управување со лични задачи (to-do),
> со автентикација, локална база, синхронизација во облак и нотификации.

---

## 1. Тема на проектот

**TaskMate** е апликација тип „листа на задачи“ (to-do / task manager). Корисникот се најавува,
ги внесува своите задачи (наслов, опис, приоритет, рок), ги означува како завршени и ги
синхронизира меѓу уреди. Темата е едноставна, но природно ги покрива сите барани функционалности
(CRUD операции, повеќе екрани, кориснички профил, синхронизација, нотификации).

---

## 2. Технологии

| Компонента | Избор |
|---|---|
| Јазик | Kotlin |
| Архитектура | MVVM (ViewModel + LiveData + Repository) |
| UI | View Binding, Material 3, Navigation Component |
| Локална база | **Room** |
| Облак | **Firebase Firestore** |
| Автентикација | **Firebase Authentication** |
| Пораки | **Firebase Cloud Messaging (FCM)** |
| Аналитика | **Firebase Analytics** |
| Min / Target SDK | 24 / 34 |
| Gradle / AGP / Kotlin | 8.7 / 8.5.0 / 1.9.24 |

---

## 3. Како се исполнети барањата од задачата

**Повеќе Activity / Fragments**
`LoginActivity` и `MainActivity` се двете Activity. `MainActivity` хостира четири фрагменти
преку Navigation Component: листа на задачи, додавање/уредување, профил и поставки.

**Различни лејаути за телефон и таблет, портрет и лендскејп**
- `layout/` — телефон, портрет (стандарден)
- `layout-land/` — телефон, лендскејп (`activity_login`, `fragment_add_edit` во две колони)
- `layout-sw600dp/` — таблет, портрет (листата во **2 колони**, центрирана картичка за најава)
- `layout-sw600dp-land/` — таблет, лендскејп (листата во **3 колони**)

**Интернационализација (македонски + англиски)**
Сите текстови се во `res/values/strings.xml` (англиски) и `res/values-mk/strings.xml` (македонски).
Јазикот може да се менува и рачно во „Поставки“ (`LocaleHelper`). Стандарден јазик: македонски.

**Локална база — исклучиво Room**
`Task` (@Entity), `TaskDao`, `AppDatabase`. Room е единствен извор на вистина за UI-то;
Firestore само се пресликува назад во Room преку snapshot listener.

**Firebase Authentication** — поддржани се Anonymous, Email/Password и Google Sign-In.
(FB најава е оставена како опција — види секција 6.)

**Firebase Firestore** — задачите се чуваат под `users/{uid}/tasks` и се синхронизираат во реално време.

**Firebase Messaging (FCM)** — `MyFirebaseMessagingService` прима пораки и прикажува нотификации;
апликацијата се претплаќа на топик `all_users`.

**Firebase Analytics** — се логираат настани: најава, креирање/завршување/бришење задача.

---

## 4. Структура на проектот

```
app/src/main/
├── java/com/taskmate/app/
│   ├── TaskMateApp.kt                application + service locator + notif. channel
│   ├── data/local/                   Task, TaskDao, AppDatabase (Room)
│   ├── data/repository/              TaskRepository (Room ⇄ Firestore)
│   ├── ui/auth/LoginActivity.kt      најава (email / anon / Google)
│   ├── ui/main/                      MainActivity, ViewModel, Adapter, листа
│   ├── ui/detail/                    додавање/уредување задача
│   ├── ui/profile/                   профил + одјава
│   ├── ui/settings/                  јазик + темна тема
│   ├── notifications/                FCM service
│   └── util/                         Constants, LocaleHelper
└── res/                              layout, layout-land, layout-sw600dp(-land),
                                      values, values-mk, values-night, navigation, ...
```

---

## 5. Поставување на Firebase (ЗАДОЛЖИТЕЛНО пред да се компајлира)

Во проектот е вклучен **PLACEHOLDER** `app/google-services.json` за да може да се отвори.
Тој **мора** да се замени со вашиот фајл:

1. Одете на <https://console.firebase.google.com> → **Add project**.
2. Додадете Android апликација со package име **`com.taskmate.app`**.
3. Преземете го `google-services.json` и заменете го фајлот во папката `app/`.
4. Во конзолата вклучете:
   - **Authentication** → Sign-in methods: Anonymous, Email/Password, Google.
   - **Firestore Database** → Create database (test mode за почеток).
   - **Analytics** и **Cloud Messaging** се активни автоматски.
5. За **Google Sign-In**: додадете го SHA-1 отпечатокот на вашиот потпис
   (`./gradlew signingReport`) во поставките на Android апликацијата во Firebase.
   Копчето „Најава со Google“ автоматски се крие ако `default_web_client_id` не постои,
   така што апликацијата работи и без оваа конфигурација.

> Правило за Firestore (за тест):
> ```
> match /users/{uid}/tasks/{doc} {
>   allow read, write: if request.auth != null && request.auth.uid == uid;
> }
> ```

---

## 6. Изградба и стартување

1. Отворете ја папката `TaskMate/` во **Android Studio** (Hedgehog или понов).
2. Android Studio автоматски ќе го генерира `gradle-wrapper.jar` и ќе ги преземе зависностите.
   (Од терминал може и: `gradle wrapper` па потоа `./gradlew assembleDebug`.)
3. Заменете го `google-services.json` (секција 5).
4. Стартувајте на емулатор или вистински телефон (API 24+).

---

## 7. Што останува да направите Вие (за предавање)

Задачата бара дел од работата да биде ваша лична:

- [ ] Креирајте **сопствен Firebase проект** и ставете го вашиот `google-services.json`.
- [ ] Качете го проектот на **јавен GitHub репозиториум** со **повеќе commit-и**
      (не само еден „initial commit“ — правете commit по функционалност).
- [ ] Снимете **скриншоти** и **скринкаст видеа** и ставете ги во папката `screenshots/`
      (види `screenshots/README.md`).
- [ ] Подгответе се за **презентација** на проектот.

> Опционо (за повеќе поени): FB најава, Cloud Functions, работа со хардвер (камера, GPS,
> сензори), објавување на Play Store.

---

## 8. Забелешка за јазик (Kotlin vs Java)

Проектот е во **Kotlin**, што е стандарден и официјален јазик за Android. Ако курсот
експлицитно бара **Java**, кажете и ќе ја конвертирам целата логика во Java
(структурата на ресурси и лејаути останува иста).
