# SPRING-Task-2

Spring bean scope-ları və Dependency Injection tapşırığı. Bölmələr üzrə nəticələr aşağıda saxlanılır.

## İşə salmaq

Maven layihəsi `testders` qovluğundadır. Spring Boot 3.5.16, Java target 17, Spring Web və Lombok istifadə olunur. Hazırkı yoxlamalar JDK 21-də aparılıb; ayrıca JDK 17 run-u edilməyib.

```powershell
cd testders
.\mvnw.cmd spring-boot:run
```

IntelliJ-də `testders/pom.xml` açıb Maven layihəsini yükləmək və `TestdersApplication` sinfini işə salmaq olar. Lombok üçün annotation processing aktiv olmalıdır. Standart port `8080`-dir.

```powershell
.\mvnw.cmd test
```

JUnit yoxlamaları: prototype lifecycle, session tarixçəsi, repository seçimi, Spring context və Spring-siz service testləri.
HTTP yoxlamalarının nəticələri `docs/checks` qovluğundadır. Verilənlər yalnız RAM-da saxlanır; Docker və DB lazım deyil.

8080 məşğuldursa:

```powershell
.\mvnw.cmd package
java -jar target/testders-0.0.1-SNAPSHOT.jar --server.port=8082
```

Bölmə 5-də Tapşırıq 29-a görə `mockUserRepository` @Qualifier ilə seçilmişdi.
Bonus 36-dan sonra seçim `app.repository-type` ilə edilir; default YAML dəyəri `mock`-dur. `/all` həmişə iki `Mock User` qaytarır.
Mock-da POST ilə yaradılan user öz ID-si ilə GET-də tapılır, amma sabit demo `/all` siyahısına əlavə edilmir.
Əvvəlki singleton/session mərhələlərinin tam in-memory variantı Git tarixçəsində saxlanılıb.

## Endpointlər

| Metod | Yol | Nəticə |
| --- | --- | --- |
| GET | `/api/v1/user-info/all` | Bütün istifadəçilər |
| GET | `/api/v1/user-info/{id}` | ID ilə istifadəçi |
| POST | `/api/v1/user-info/create` | Yeni istifadəçi, 201 Created |
| GET | `/api/v1/user-info/recent` | Cari session-da son beş fərqli istifadəçi |
| GET | `/api/v1/scope-demo` | Singleton, iki prototype və request ID |
| GET | `/api/v1/stats` | Application scope sayğacları |

POST body:

```json
{
  "fullName": "Vaqif",
  "email": "vaqif@example.com"
}
```

## Bölmə 1 Singleton

### Tapşırıq 1 və 2

Məlumatlar `Map<Long, UserInfoResponse>` field-ində `ConcurrentHashMap` ilə saxlanılır. ID-lər `AtomicLong.incrementAndGet()` ilə ardıcıl verilir. Restart zamanı yaddaşdakı məlumatlar itir və başlanğıc istifadəçilər yenidən yüklənir.

### Tapşırıq 3

`@PostConstruct` Ana, Murad və Ali-ni yaradır. Konsola `UserRepository hazirdir, 3 user yuklendi` yazılır.

![Başlanğıc logları](docs/screenshots/startup.png)

### Tapşırıq 4

`@PreDestroy` `users.clear()` çağırır və `UserRepository baglandi, users temizlendi` yazır. IntelliJ-də Stop zamanı mesaj görüldü. Qəfil proses/JVM dayanmasında callback zəmanətli deyil. Map-in təmizlənməsi burada lifecycle nümunəsidir.

![IntelliJ bağlanma logu](docs/screenshots/shutdown-intellij.png)

### Tapşırıq 5

İstifadəçi yoxdursa `NoSuchElementException` atılır. `GET /99999` yoxlamasında konsolda `User tapilmadi: 99999` göründü. Exception handler olmadığına görə hazırda HTTP 500 qaytarılır, 404 deyil.

![Olmayan istifadəçi](docs/screenshots/missing-user.png)

Şəkildə əvvəlki POST-un body-si qalıb; GET axtarış metodu onu istifadə etmir. Təkrar yoxlamada GET üçün Body → none seçilməlidir.

![Exception konsolda](docs/screenshots/missing-user-console.png)

### Tapşırıq 6

Controller, service və repository konstruktorlarına `yaradildi` və `hashCode()` log-u əlavə etdim. Tətbiq başladıqda hər biri bir dəfə yaradıldı. 10 təkrar sorğudan sonra konstruktor mesajları yenidən çıxmadı. Sorğular eyni singleton bean-lərdən istifadə edir. Təkrar sorğu nəticəsi müşahidə ilə yoxlanıb; ayrıca 10 sorğulu run screenshot-u yoxdur. `hashCode()` yaddaş ünvanı deyil və unikal olmasına zəmanət yoxdur.

### Tapşırıq 7

Başlanğıc `GET /all` üç istifadəçi qaytardı. `POST /create` ilə Vaqif yaradıldı və ID 4 alındı. Sonrakı `GET /4` həmin istifadəçini qaytardı. 19 oktyabr təkrar HTTP yoxlamasında eyni email və createdAt təsdiqləndi, sonrakı `/all` siyahısında 4 istifadəçi oldu. 10 əlavə GET sorğusu da 4 istifadəçi qaytardı. Bu təkrar yoxlama müvəqqəti 8081 portunda aparılıb; layihənin standart portu dəyişdirilməyib. Ayrıca GET screenshot-u yoxdur; aşağıdakı `/all` şəkli yaradılmadan əvvəlki üç istifadəçini göstərir.

![Başlanğıc üç istifadəçi](docs/screenshots/users-all.png)

![POST create cavabı](docs/screenshots/create-user.png)

### Tapşırıq 8

Singleton bean-i eyni anda bir neçə thread istifadə edə bilər. Dəqiq say serverin imkanlarından və gələn sorğulardan asılıdır; scope thread sayını məhdudlaşdırmır. Lokal dəyişənlər və parametrlər hər çağırış üçün ayrıdır, bean-in field-ləri isə ortaqdır. Lokal istinadlar ortaq dəyişən obyektə yönəlsə, həmin obyekt yenə paylaşılır.

Adi `HashMap` paralel dəyişikliklər üçün təhlükəsiz deyil. Adi `long` üzərində `++` atomik olmadığından iki thread eyni ID ala bilər. `ConcurrentHashMap` map əməliyyatları üçün, `AtomicLong` isə ID artımı üçün istifadə olunur. Bir neçə əməliyyatdan ibarət ardıcıllıq və `UserInfoResponse` field-lərinin dəyişdirilməsi ayrıca thread-safety tələb edə bilər.

## Bölmə 2 Prototype

### Tapşırıq 9 və 10

UserAuditLog prototype bean-dir: auditId UUID, createdAt LocalDateTime, action isə setter ilə təyin olunur.
Konstruktor hər yaradılmanı və auditId-ni log-a yazır.

### Tapşırıq 11 və 12

Əvvəl UserAuditLog singleton UserInfoService konstruktoruna birbaşa inject edildi. Üç POST/create eyni auditId-ni istifadə etdi:
Spring asılılığı service yaradılarkən bir dəfə verdi. Sonra ObjectProvider istifadə edildi;
hər createUser çağırışı getObject ilə yeni audit alır və CREATE_USER action təyin edir.
[Birbaşa injection log-u](docs/checks/prototype-direct-app.log), [HTTP](docs/checks/prototype-direct-http.json).

### Tapşırıq 13

Prototype bean üçün Spring yaradılma və initialization mərhələlərini idarə edir, amma avtomatik destroy callback saxlamır.
Context bağlananda UserAuditLog-un PreDestroy log-u çıxmır. Resurs varsa onu istifadə edən kod təmizləməlidir.
PrototypeLifecycleTest bunu ayrıca yoxlayır.

### Provider yoxlaması

[Üç fərqli auditId log-u](docs/checks/prototype-provider-app.log), [HTTP cavabları](docs/checks/prototype-provider-http.json).

## Bölmə 3 Request

### Tapşırıq 14 və 15

RequestContext request scope-dadır: UUID-dən 8 simvol requestId və millisecond startTime saxlayır.
Controller, service, repository konstruktorları eyni scoped proxy-ni qəbul edir.

### Tapşırıq 16 və 17

Bir GET/id zamanı üç qat eyni requestId-ni log-a yazır. Növbəti GET yeni requestId alır.
Controller cavab qaytarmazdan əvvəl cari vaxtdan startTime çıxıb müddəti millisecond ilə yazır;
bu, context ilk istifadə olunandan hesablanan vaxtdır, tam şəbəkə round-trip ölçüsü deyil.

### Tapşırıq 18 və 19

RequestScope əvəzinə proxy-siz Scope("request") sınağında startup ScopeNotActiveException verdi:
singleton yaradılarkən aktiv HTTP request yoxdur. Sonra RequestScope bərpa edildi.
[Real startup xətası](docs/checks/request-no-proxy-error.log).
RequestScope default olaraq TARGET_CLASS proxy istifadə edir. getClass çıxışında $$SpringCGLIB$$ görünür.
Proxy singleton-da qalır, method çağırışını cari HTTP request-in həqiqi obyektinə ötürür.

## Bölmə 4 Session

### Tapşırıq 20–22

RecentlyViewedUsers session scope-dadır. Son 5 fərqli ID saxlanır, ən yeni əvvəldədir.
TəkrarID əvvəlki yerindən çıxıb əvvələ keçir. synchronized metodlar eyni session-dan paralel
sorğuları qoruyur; getUsers dəyişdirilməyən snapshot qaytarır.
Uğurlu getUserById-dən sonra ID əlavə olunur. Tapılmayan user tarixçəyə düşmür.
/recent service vasitəsilə ID-ləri tam UserInfoResponse-a çevirir.

### Tapşırıq 23

application.yaml-da server.servlet.session.timeout: 1m qoyulub.
Tomcat background yoxlaması üçün background-processor-delay: 1s istifadə olunur;
session dəqiq 60-cı saniyədə deyil, container-in növbəti expiry yoxlamasında silinə bilər.
PreDestroy zamanı tarixçə təmizlənir və bean-in ID-si log-a yazılır.
[Session HTTP yoxlamaları](docs/checks/session-http.json), [expiry nəticəsi](docs/checks/session-expiry.json),
[log](docs/checks/session-app.log).

### Tapşırıq 24

Eyni cookie ilə /1,/3,/2,/1 → /recent nəticəsi ID 1,2,3 olan tam user-lərdir.
Ayrı cookie jar-da /recent boşdur. Chrome və incognito ayrıca JSESSIONID saxlayır;
Postman da Chrome-un cookie-sini avtomatik götürmür, ona görə tarixçəsi boş ola bilər.
Cookie-ni silmək və ya session müddətinin bitməsi yeni tarixçə yaradır.

### Tapşırıq 25

Bu field request scope-da olsaydı hər yeni HTTP sorğusunda yeni siyahı yaranardı.
Əvvəlki /1 sorğusunun tarixçəsi sonrakı /recent sorğusuna qalmazdı.
Session scope bir istifadəçinin bir neçə sorğusu arasında vəziyyəti saxlayır.
## Bölmə 5 Dependency Injection

### Tapşırıq 26

UserRepository indi üç metodlu interface-dir: getUserById, getAllUsers, createUser.
Əvvəlki storage və lifecycle kodu InMemoryUserRepository-yə köçürülüb.
Service interface-dən asılıdır; hansı implementasiyanı istifadə etdiyini özü yaratmır.

### Tapşırıq 27

İkinci implementasiya MockUserRepository-dir. /all həmişə adı Mock User olan iki nümunə qaytarır.
Hər iki bean var, amma seçim yoxdursa startup dayanır: expected single matching bean but found 2.
Bu, UnsatisfiedDependencyException daxilində NoUniqueBeanDefinitionException səbəbidir;
RepositorySelectionTest də həmin exception tipini yoxlayır.
[Startup xətası](docs/checks/di-ambiguous-error.log).

### Tapşırıq 28 və 29

Əvvəl InMemoryUserRepository üzərinə @Primary qoyuldu: /all Ana, Murad və Ali qaytardı.
Sonra @Primary silindi, service konstruktorunda @Qualifier("mockUserRepository") seçildi.
İndi /all iki Mock User qaytarır. @Primary ümumi üstünlükdür, @Qualifier konkret injection nöqtəsində seçimi daraldır.
[Primary nəticəsi](docs/checks/di-primary-http.json), [Qualifier nəticəsi](docs/checks/di-final-http.json).

### Tapşırıq 30 və 31

NotificationService.sendWelcome(String email) optional setter ilə inject olunur:
@Autowired(required = false). Implementasiya olmadan tətbiq açıldı və POST/create uğurlu oldu
([HTTP](docs/checks/di-primary-http.json)). Bu setter Tapşırıq 30-un ayrıca tələbidir;
məcburi asılılıqlar final field və constructor ilə verilir.
Sonra @Service ConsoleNotificationService əlavə edildi. createUser uğurla saxlanandan sonra
Xoş gəldin: email log-u yazılır. Bu, həqiqi email göndərilməsi deyil.
[Final welcome log-u](docs/checks/final-http-app.log).

### Tapşırıq 32

UserInfoServiceTest Spring açmadan Mockito mock-ları və new UserInfoService ilə işləyir.
Müvəqqəti sınaqda repository field üzərinə @Autowired qoyuldu, constructor assignment silindi.
Test constructor-a mock versə də, field onu almırdı: plain new Spring injection işə salmır.
Bu səbəbdən repository null qaldı və testlər NullPointerException ilə dayandı.
Sonra final field + constructor assignment bərpa edildi, clean test package uğurlu oldu.
[Sınağın xəta log-u](docs/checks/di-field-injection-error.log), [düzəldilmiş build](docs/checks/di-final-build.log).
Constructor injection asılılığı açıq göstərir, final field istifadə etməyə və Spring-siz test yazmağa imkan verir.

### Tapşırıq 33

Service konstruktorunda List<UserRepository> iki element aldı: InMemoryUserRepository və MockUserRepository.
Tək repository parametrindəki @Qualifier siyahıya tətbiq olunmur; siyahı bütün uyğun bean-ləri alır.
Startup log-u: Repository sayi: 2. Siyahının sırasına güvənilmir.

## Şifahi müdafiə

- IoC: obyektlərin yaradılması və lifecycle idarəsi container-ə verilir. DI: asılılıqların obyektə kənardan ötürülməsidir;
  IoC-ni tətbiq etməyin bir yoludur. DI Spring olmadan da mümkündür.
- Constructor injection: asılılıqlar görünür, final field mümkündür, unit test üçün container lazım deyil.
- Prototype üçün container avtomatik PreDestroy çağırmır; lazım olan cleanup istifadə edən kodun məsuliyyətidir.
- Request bean singleton-a proxy vasitəsilə verilir. Proxy hər çağırışda cari request-in target obyektini tapır.
- Session scope bir HTTP session üçün tarixçə/səbət saxlaya bilər. Application scope ServletContext üzrə ortaqdır,
  məsələn bütün istifadəçilərin statistikasını saxlayır. Ortaq dəyişən state yenə thread-safe olmalıdır.
- Konkret @Qualifier ilə seçim daraldılır; uyğun gəlməyən @Primary bu seçimi əvəz etmir.

## Bölmə 6 Bonus

### Tapşırıq 34

GET /api/v1/scope-demo singleton service-in hashCode-unu, iki ayrı prototype auditId və cari requestId qaytarır.
HashCode yaddaş ünvanı deyil; bu təcrübədə eyni singleton-a çağırış etdiyimiz üçün dəyişmir.
Üç real HTTP cavabı aşağıdadır. Altı auditId fərqli, üç requestId fərqli oldu.

| Sorğu | Singleton hashCode | Prototype 1 auditId | Prototype 2 auditId | RequestId |
| --- | --- | --- | --- | --- |
| 1 | 859985937 | a182a047-0954-4474-b003-c05c1723525d | fb2e376a-f406-4a09-9f27-7017b6e31198 | beba6484 |
| 2 | 859985937 | cc67333b-2b0b-4a60-ad88-58236ff270ef | ab1cf847-dda4-40d3-9b01-5be87c7f3819 | 013fbcce |
| 3 | 859985937 | 834e2b3d-5fcb-4074-8060-59d0c4427014 | ecb66184-89cf-4523-82e1-901eee3065ad | d4e6085e |

### Tapşırıq 35

UserStatistics @ApplicationScope bean-dir. İki AtomicLong bütün sessions üçün ortaqdır.
createdUsers yalnız service-də uğurlu createUser-dən sonra artır; repository-nin startup seed-ləri sayılmır.
getUserByIdCalls hər service.getUserById çağırışını, o cümlədən tapılmayan ID-ni sayır.
/all, /recent və /scope-demo bu sayğacı artırmır. GET /api/v1/stats sayğacları oxuyur.
Sayğaclar yaddaşdadır, restart zamanı sıfırlanır. İki sayğacın oxunması ayrıca atomikdir, birlikdə transaction deyil.

İki ayrı cookie jar-da bir create və iki lookup etdik: createdUsers=1, getUserByIdCalls=2 hər ikisində eyni oldu.
Sonrakı tam HTTP run-dan sonra Chrome və in-app browser eyni nəticəni göstərdi: createdUsers=7, getUserByIdCalls=21.
[Brauzer nəticələri](docs/checks/bonus-browsers.json), [əsas bonus HTTP yoxlaması](docs/checks/bonus-http.json).

### Tapşırıq 36

application.yaml:

```yaml
app:
  repository-type: mock
```

InMemoryUserRepository yalnız memory dəyərində, MockUserRepository yalnız mock dəyərində qeyd olunur
(@ConditionalOnProperty). Property ümumiyyətlə yoxdursa mock default-dur. Başqa dəyərdə uyğun repository yoxdur,
tətbiq dependency xətası ilə açılmır. Seçim startup-da edilir; işləyən tətbiqdə YAML-i dəyişmək kifayət etmir.

İki rejimi ayrıca işə saldım: mock → iki Mock User; memory → Ana, Murad, Ali.
Memory rejimini faylı dəyişmədən də yoxlamaq olar:

```powershell
java -jar target/testders-0.0.1-SNAPSHOT.jar --server.port=8082 --app.repository-type=memory
```

Bu bonusda tək bean qaldığı üçün service-də əvvəlki @Qualifier çıxarılıb, List<UserRepository> indi 1 element alır.
Tapşırıq 29/33 üçün iki bean + Qualifier təcrübəsi task-5 commit-ində və yuxarıdakı log-larda qalır.
[Memory HTTP](docs/checks/bonus-memory-http.json), [memory startup](docs/checks/bonus-memory-app.log),
[mock startup](docs/checks/bonus-mock-app.log). Unit test hər iki property dəyərini ayrıca yoxlayır.

### Tapşırıq 37

InitializationLogProcessor BeanPostProcessor implement edir. ClassUtils.getUserClass ilə CGLIB subclass-dan
əsas class tapılır və yalnız az.iktex. ilə başlayan paketlər üçün before/after initialization yazılır.
PriorityOrdered bu processor-un before callback-ini @PostConstruct işlədən processor-dan əvvəl yerləşdirir.
Memory repository üçün real ardıcıllıq:

```text
UserRepository yaradildi: ...
[Bean before] inMemoryUserRepository
UserRepository hazirdir, 3 user yuklendi
[Bean after] inMemoryUserRepository
```

Constructor əvvəl işləyir, dependency injection tamamlanır, before callback-lər → PostConstruct/init → after callback-lər gəlir.
Bu, hər HTTP sorğusunun deyil, bean yaradılmasının lifecycle-ıdır. Request/session/application target-ləri ilk istifadədə
yarandığına görə scopedTarget.* mesajları ilk uyğun HTTP sorğusunda görünür. Scoped proxy FactoryBean məhsulu üçün
tək after mesajı da görünə bilər; bu, target-in initialization log-u ilə eyni şey deyil.
Processor bean-i dəyişdirmədən qaytarır. Spring/Tomcat-ın adi INFO log-larını söndürmür, yalnız öz mesajlarını filtr edir.
Unit test tətbiq bean-inin log-a düşdüyünü, java.lang.Object-un isə düşmədiyini yoxlayır.

### Bonusların yekun yoxlaması

clean test package:13 JUnit testi, 0 failure/error. Yenilənmiş kolleksiya:36 HTTP sorğusu, 51 assertion, 0 failure.
[Build](docs/checks/bonus-build.log), [HTTP log-u](docs/checks/bonus-postman-run.log),
[JSON nəticəsi](docs/checks/bonus-postman-run.json).
Postman-da 8080 seçsəniz kolleksiyada həm baseUrl, həm bonusUrl dəyişdirilməlidir.

![Bonusların hesabatı](docs/screenshots/bonus-report.png)

## Əhatə

1–37 tapşırıqları, o cümlədən bütün bonuslar yerinə yetirilib. Bonus 36-dan sonra finalda conditional repository seçimi işləyir.
Tapşırıq üzrə gözlənən NoSuchElementException üçün ayrıca handler əlavə olunmayıb; olmayan ID HTTP500 verir.

## Son yoxlamalar

Bonusdan əvvəlki Bölmə 1–5 yoxlaması (2 noyabr2026): clean test package —10JUnit,0failure/error. Bonusdan sonrakı nəticə yuxarıda:13JUnit/51HTTPassertion.
Postman v2.1 kolleksiyasının Newman6.2.3 run-u: 29 HTTP sorğusu, 40 assertion, 0 failure.
10 təkrar GET zamanı singleton constructor log-ları yenidən çıxmadı.
Beş create üçün beş fərqli auditId və beş welcome mesajı oldu.
[Kolleksiya](docs/postman/Task17.postman_collection.json), [run log-u](docs/checks/postman-run.log),
[JSON nəticə](docs/checks/postman-run.json), [constructor/audit nəticəsi](docs/checks/final-log-summary.json).

Postman-da kolleksiyanı import edib Run ilə işə salmaq olar; baseUrl standart olaraq8082-dir.
Yeni cookie jar istifadə edin; uzun fasilədə1dəqiqəlik session bitə bilər.
Nəticələr konkret ssenariləri yoxlayır, bütün mümkün hallara zəmanət deyil.

![Chrome session tarixçəsi](docs/screenshots/session-chrome.png)

Incognito-da ilk /recent ayrıca session yaratdı və boş siyahı qaytardı.

![Incognito ayrı session](docs/screenshots/session-incognito.png)

## Şəkillər və əlavə hesabat

Aşağıdakı dörd şəkil real log-lardan hazırlanmış HTML hesabat səhifələridir;
terminal və Postman interfeysinin şəkli kimi təqdim olunmur. İlkin mənbələr `docs/checks`-dədir.

![Prototype müqayisəsi](docs/screenshots/prototype-report.png)

![Request və proxy](docs/screenshots/request-report.png)

![Session və timeout](docs/screenshots/session-report.png)

![DI yoxlamaları](docs/screenshots/di-report.png)

Aşağıdakı şəkil isə tətbiqin özündə icra olunmuş Postman runner-dir:40 pass,0 fail.

![Postman runner](docs/screenshots/postman-runner.png)

## Hesabat

Birinci bölmənin cavabları və ekran görüntüləri ayrıca [Word hesabatında](docs/Singleton-hesabat.docx) da var. README-dəki cavablar və screenshot-lar tapşırığın təhvil qaydasını saxlayır.
