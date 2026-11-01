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

Mövcud avtomatik test `contextLoads`-dur. HTTP ssenariləri Postman ilə ayrıca yoxlanıb; bütün funksionallıq üçün unit-test coverage iddiası yoxdur.

## Endpointlər

| Metod | Yol | Nəticə |
| --- | --- | --- |
| GET | `/api/v1/user-info/all` | Bütün istifadəçilər |
| GET | `/api/v1/user-info/{id}` | ID ilə istifadəçi |
| POST | `/api/v1/user-info/create` | Yeni istifadəçi, 201 Created |

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

RequestContext request scope-dadır: UUID-dən8simvol requestId və millisecond startTime saxlayır.
Controller,service,repository konstruktorları eyni scoped proxy-ni qəbul edir.

### Tapşırıq 16 və 17

Bir GET/id zamanı üç qat eyni requestId-ni log-a yazır. Növbəti GET yeni requestId alır.
Controller cavab qaytarmazdan əvvəl cari vaxtdan startTime çıxıb müddəti millisecond ilə yazır;
bu, context ilk istifadə olunandan hesablanan vaxtdır, tam şəbəkə round-trip ölçüsü deyil.

### Tapşırıq 18 və 19

RequestScope əvəzinə proxy-siz Scope("request") sınağında startup ScopeNotActiveException verdi:
singleton yaradılarkən aktiv HTTP request yoxdur. Sonra RequestScope bərpa edildi.
[Real startup xətası](docs/checks/request-no-proxy-error.log).
RequestScope default olaraq TARGET_CLASS proxy istifadə edir. getClass çıxışında $$SpringCGLIB$$ görünür.
Proxy singleton-da qalır,method çağırışını cari HTTP request-in həqiqi obyektinə ötürür.

## Hesabat

Birinci bölmənin cavabları və ekran görüntüləri ayrıca [Word hesabatında](docs/Singleton-hesabat.docx) da var. README-dəki cavablar və screenshot-lar tapşırığın təhvil qaydasını saxlayır.
