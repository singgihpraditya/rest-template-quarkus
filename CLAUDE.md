# REST Template Quarkus — Panduan Developer

Kamu adalah senior Java developer. Dokumen ini adalah **sumber tunggal kebenaran** untuk project ini.
Baca seluruh dokumen sebelum membuat atau mengubah kode.

> **Catatan:** Project ini adalah port dari `ogya-rest-template` (Spring Boot 3.2.3) ke Quarkus 3.8.3.
> Konvensi kode, logic bisnis, dan format response API **identik**. Yang berbeda hanya framework/annotation-nya.

---

## 1. Tech Stack & Versi

| Teknologi | Versi | Keterangan |
|-----------|-------|------------|
| Java | 21 | |
| Quarkus | 3.8.3 | Jakarta EE, RESTEasy Reactive |
| Hibernate ORM Panache | via Quarkus BOM | Menggantikan Spring Data JPA |
| Hibernate Validator | via Quarkus BOM | Bean Validation |
| MicroProfile REST Client | via Quarkus BOM | Menggantikan Spring Cloud OpenFeign |
| SmallRye OpenAPI | via Quarkus BOM | Swagger UI di `/q/swagger-ui` |
| Quarkus Cache (Caffeine) | via Quarkus BOM | Menggantikan Spring Cache |
| Quarkus OpenTelemetry | via Quarkus BOM | Menggantikan Micrometer Tracing |
| JJWT | 0.12.3 | `Jwts.parser().verifyWith().build()` — sama persis dengan Spring versi |
| jBCrypt | 0.4 | `BCrypt.checkpw()` — menggantikan Spring Security PasswordEncoder |
| Lombok | 1.18.36 | Wajib di semua class |
| Maven | - | Build tool |

---

## 2. Perbedaan Utama vs Spring Boot (Referensi Cepat)

| Konsep | Spring Boot | Quarkus |
|--------|-------------|---------|
| Dependency injection | `@Autowired` / `@RequiredArgsConstructor` | `@Inject` |
| Bean scope | `@Service` / `@Component` | `@ApplicationScoped` |
| REST controller | `@RestController` + `@GetMapping` | `@Path` + `@GET` |
| Request param | `@RequestParam` | `@QueryParam` |
| Path variable | `@PathVariable` | `@PathParam` |
| Request body | `@RequestBody` | (langsung parameter method, pakai `@Consumes`) |
| Validation trigger | `@Valid` (sama) | `@Valid` (sama) |
| Pagination | Spring `Page<T>` | `PageResponse.of(list, page, size, total)` — custom |
| Transaction | `@Transactional` (Spring) | `@Transactional` (jakarta, beda package) |
| Config value | `@Value("${key}")` | `@ConfigProperty(name = "key")` |
| Init lifecycle | `@PostConstruct` | `@Observes StartupEvent` |
| AOP logging | `@Aspect` + `@Around` | CDI `@Interceptor` + `@AroundInvoke` |
| Filter HTTP | `OncePerRequestFilter` | `ContainerRequestFilter` + `ContainerResponseFilter` |
| Exception handler | `@RestControllerAdvice` | `@Provider ExceptionMapper<T>` |
| External HTTP client | `@FeignClient` | `@RegisterRestClient(configKey)` + `@Inject @RestClient` |
| Cache | `@Cacheable` / `@CacheEvict` | `@CacheResult` / `@CacheInvalidateAll` |
| Spring Security | `SecurityFilterChain` + `DynamicAuthorizationManager` | Custom `ContainerRequestFilter` di `Priorities.AUTHENTICATION` |
| Password hashing | `BCryptPasswordEncoder` (Spring Security) | `BCrypt.checkpw()` dari jBCrypt |

---

## 3. Struktur Package

```
com.example.template.quarkus/
├── aspect/
│   ├── Logged.java                  ← @InterceptorBinding annotation
│   └── LoggingInterceptor.java      ← CDI @Interceptor: log [ENTRY]/[EXIT]/durasi
├── config/
│   ├── CacheConfig.java             ← Konstanta nama cache
│   ├── DataInitializer.java         ← Seed data (@Observes @Priority(1) StartupEvent, local only)
│   ├── JacksonConfig.java           ← ObjectMapperCustomizer: snake_case + LocalDateTime format
│   ├── OpenApiConfig.java           ← @OpenAPIDefinition + @SecurityScheme (extends Application)
│   └── RequestIdFilter.java         ← ContainerRequestFilter+ResponseFilter: traceId+requestId ke MDC
├── controller/
│   ├── AuthController.java          ← POST /api/auth/login, register, GET /api/auth/me
│   ├── CacheController.java         ← POST /api/cache/cleanup (ADMIN)
│   ├── CategoryController.java      ← CRUD /api/categories + GET /api/categories/stats
│   ├── DiagnosticController.java    ← GET /api/diagnostic/trace
│   ├── EndpointPermissionController.java ← CRUD /api/permissions (ADMIN)
│   ├── ExternalApiController.java   ← GET /api/external/** (demo REST Client)
│   ├── FileController.java          ← POST /api/files/upload, GET /api/files/download/**
│   └── ProductController.java       ← CRUD /api/products
├── dto/
│   ├── request/                     ← Input dari client (@Getter @NoArgsConstructor)
│   │   ├── CategoryRequest.java
│   │   ├── EndpointPermissionRequest.java
│   │   ├── LoginRequest.java
│   │   ├── ProductRequest.java
│   │   └── RegisterRequest.java
│   └── response/                    ← Output ke client (@Getter @Builder + static from())
│       ├── ApiResponse.java         ← Envelope semua response
│       ├── CategoryResponse.java
│       ├── EndpointPermissionResponse.java
│       ├── ErrorMessage.java
│       ├── ErrorSchema.java
│       ├── PageResponse.java        ← Pagination custom: of(list, page, size, total)
│       ├── PostResponse.java        ← DTO untuk API eksternal JSONPlaceholder
│       ├── ProductResponse.java
│       ├── TagResponse.java
│       ├── TokenResponse.java
│       └── UserResponse.java
├── entity/
│   ├── BaseEntity.java              ← @MappedSuperclass: createdAt, updatedAt (auto)
│   ├── Category.java                ← One-to-Many → Product
│   ├── EndpointPermission.java      ← Aturan otorisasi dinamis
│   ├── Product.java                 ← Many-to-One ← Category; Many-to-Many ↔ Tag
│   ├── Role.java                    ← Many-to-Many ↔ User
│   ├── Tag.java                     ← Many-to-Many ↔ Product
│   └── User.java                    ← Many-to-Many → Role
├── exception/
│   ├── BusinessException.java       ← 409 Conflict
│   ├── BusinessExceptionMapper.java ← @Provider ExceptionMapper<BusinessException>
│   ├── GenericExceptionMapper.java  ← Catch-all 500, juga handle 401/403/WAE
│   ├── ResourceNotFoundException.java ← 404 Not Found
│   ├── ResourceNotFoundExceptionMapper.java
│   ├── UnauthorizedException.java   ← 401 Unauthorized
│   ├── UnauthorizedExceptionMapper.java
│   └── ValidationExceptionMapper.java ← ConstraintViolationException → 400
├── repository/
│   ├── projection/
│   │   ├── CategoryProductCountProjection.java ← Interface (dipertahankan untuk type-safety)
│   │   └── CategoryProductCountResult.java     ← Concrete class — mapping manual dari Object[]
│   ├── CategoryRepository.java      ← PanacheRepository<Category>
│   ├── EndpointPermissionRepository.java
│   ├── ProductRepository.java       ← Native query via EntityManager
│   ├── RoleRepository.java
│   ├── TagRepository.java
│   └── UserRepository.java
├── rest/
│   └── JsonPlaceholderClient.java   ← @RegisterRestClient(configKey = "jsonplaceholder")
├── security/
│   ├── JwtTokenProvider.java        ← Generate & validasi JWT (JJWT 0.12.x, sama persis)
│   └── SecurityFilter.java          ← ContainerRequestFilter @ Priorities.AUTHENTICATION:
│                                       validasi JWT + dynamic authorization via EndpointPermissionService
└── service/
    ├── AuthService.java             ← login() pakai BCrypt.checkpw(), bukan AuthenticationManager
    ├── CategoryService.java         ← @CacheResult / @CacheInvalidateAll
    ├── EndpointPermissionService.java ← @Observes @Priority(10) StartupEvent, antMatch() custom
    ├── ExternalApiService.java      ← Wrapper @RestClient JsonPlaceholderClient
    ├── FileStorageService.java      ← @Observes @Priority(1) StartupEvent untuk init()
    └── ProductService.java
```

---

## 4. Konvensi Kode

### 4.1 Entity

Sama persis dengan Spring Boot versi:

```java
@Entity
@Table(name = "nama_tabel")
@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class NamaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nama_kolom", nullable = false, length = 100)
    private String namaField;

    @Builder.Default
    private boolean active = true;
}
```

- **Selalu extend `BaseEntity`**
- **Nama tabel** selalu plural snake_case
- **`@Builder.Default`** wajib jika field punya nilai default

---

### 4.2 Repository (Panache)

```java
@ApplicationScoped
public class NamaRepository implements PanacheRepository<Nama> {

    // Panache query (JPQL-style)
    public PanacheQuery<Nama> findByNameContainingIgnoreCase(String keyword) {
        return find("LOWER(name) LIKE LOWER(?1)", "%" + keyword + "%");
    }

    public boolean existsByName(String name) {
        return count("name", name) > 0;
    }
}
```

**Native query dengan Projection (WAJIB):**
```java
// 1. Tetap gunakan interface projection di repository/projection/
public interface NamaProjection { Long getId(); String getName(); Long getCount(); }

// 2. Buat concrete class yang implements projection (Hibernate tidak auto-proxy)
public class NamaResult implements NamaProjection { ... }

// 3. Repository: mapping manual dari Object[]
@Inject EntityManager em;

public List<NamaProjection> findWithCount() {
    List<Object[]> rows = em.createNativeQuery("SELECT id, name, COUNT(*) ...").getResultList();
    return rows.stream().map(row ->
        (NamaProjection) new NamaResult(
            ((Number) row[0]).longValue(),
            (String) row[1],
            ((Number) row[2]).longValue()
        )
    ).toList();
}
```

> **Mengapa concrete class?** Spring Data otomatis membuat proxy untuk interface projection.
> Hibernate (digunakan Quarkus) tidak. Solusi: buat class yang implements interface-nya.

---

### 4.3 DTO

**Sama persis dengan Spring Boot versi** — tidak ada `@JsonNaming`, karena `JacksonConfig` mengatur
`SNAKE_CASE` secara global via `ObjectMapperCustomizer`.

**Request DTO:**
```java
@Getter
@NoArgsConstructor
public class NamaRequest {
    @NotBlank(message = "Nama tidak boleh kosong")
    private String name;

    @NotNull
    private Long namaId;
}
```

**Response DTO:**
```java
@Getter
@Builder
public class NamaResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;

    public static NamaResponse from(Nama entity) {
        return NamaResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
```

---

### 4.4 Service

```java
@ApplicationScoped
@Slf4j
public class NamaService {

    @Inject
    NamaRepository namaRepository;

    @Transactional                              // jakarta.transaction.Transactional, bukan Spring
    public NamaResponse create(NamaRequest request) {
        if (namaRepository.existsByName(request.getName())) {
            throw new BusinessException("Nama sudah ada");
        }
        Nama entity = Nama.builder().name(request.getName()).build();
        namaRepository.persist(entity);
        log.info("Nama dibuat: {}", entity.getName());
        return NamaResponse.from(entity);
    }

    @Transactional                              // Panache tidak punya readOnly, pakai @Transactional biasa
    public PageResponse<NamaResponse> findAll(int page, int size, String search) {
        var query = search != null && !search.isBlank()
                ? namaRepository.findByNameContainingIgnoreCase(search)
                : namaRepository.findAll(Sort.by("createdAt").descending());
        long total = query.count();
        List<Nama> items = query.page(Page.of(page, size)).list();
        return PageResponse.of(items.stream().map(NamaResponse::from).toList(), page, size, total);
    }
}
```

**Exception yang digunakan (sama dengan Spring versi):**

| Exception | Error Code | HTTP | Kapan dipakai |
|-----------|-----------|------|--------------|
| `ResourceNotFoundException("Entity", id)` | RST-002 | 404 | Data tidak ditemukan |
| `BusinessException("pesan")` | RST-003 | 409 | Duplikat, tidak bisa dihapus, dll. |
| `UnauthorizedException("pesan")` | RST-401 | 401 | Token invalid / login gagal |
| `ConstraintViolationException` | RST-001 | 400 | Otomatis dari `@Valid` |

Semua exception ditangani oleh `ExceptionMapper` di package `exception/` — tidak perlu try-catch di service.

---

### 4.5 Controller

```java
@Path("/api/nama")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Nama", description = "Deskripsi endpoint")
@SecurityRequirement(name = "bearerAuth")   // tambahkan jika butuh login
@Logged                                      // aktifkan LoggingInterceptor
@Slf4j
public class NamaController {

    @Inject
    NamaService namaService;

    @GET
    @Operation(summary = "List Nama")
    public Response getAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return Response.ok(ApiResponse.success(namaService.findAll(page, size, null))).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Detail Nama")
    public Response getById(@PathParam("id") Long id) {
        return Response.ok(ApiResponse.success(namaService.findById(id))).build();
    }

    @POST
    @Operation(summary = "Tambah Nama")
    public Response create(@Valid NamaRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success(namaService.create(request))).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update Nama")
    public Response update(@PathParam("id") Long id, @Valid NamaRequest request) {
        return Response.ok(ApiResponse.success(namaService.update(id, request))).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Hapus Nama")
    public Response delete(@PathParam("id") Long id) {
        namaService.delete(id);
        return Response.ok(ApiResponse.success(null)).build();
    }
}
```

**Pola return value (sama dengan Spring versi, beda syntax):**
- **Sukses:** `Response.ok(ApiResponse.success(data)).build()`
- **Create:** `Response.status(Response.Status.CREATED).entity(ApiResponse.success(data)).build()`
- **Delete:** `Response.ok(ApiResponse.success(null)).build()`

> **Catatan khusus FileController:** `java.nio.file.Path` konflik dengan `jakarta.ws.rs.@Path`.
> Jangan pakai `import jakarta.ws.rs.*;` — gunakan explicit import tiap annotation JAX-RS,
> dan gunakan `java.nio.file.Path` fully-qualified di dalam method body.

---

## 5. Format Response API

Sama persis dengan Spring Boot versi:

### Response Sukses
```json
{
  "error_schema": {
    "error_code": "RST-000",
    "error_message": { "english": "Success", "indonesian": "Berhasil" }
  },
  "output_schema": { "id": 1, "name": "Contoh", "created_at": "2024-01-15 10:30:00" }
}
```

### Response Pagination
```json
{
  "error_schema": { "error_code": "RST-000", "error_message": { ... } },
  "output_schema": {
    "content": [ ... ],
    "page_number": 0,
    "page_size": 10,
    "total_elements": 25,
    "total_pages": 3,
    "first": true,
    "last": false
  }
}
```

### Response Error
```json
{
  "error_schema": {
    "error_code": "RST-002",
    "error_message": { "english": "Product dengan ID 99 tidak ditemukan", "indonesian": "..." }
  }
}
```

---

## 6. Sistem Otorisasi Dinamis

Sama persis dengan Spring versi: aturan disimpan di tabel `endpoint_permissions`, dibaca saat startup,
di-cache di memori.

### Cara Kerja (Quarkus)
```
Request → RequestIdFilter (isi traceId + requestId ke MDC)
        → SecurityFilter @ Priorities.AUTHENTICATION
            → EndpointPermissionService.findMatchingPermission(method, path)
            → antMatch() dengan Ant-style regex (/** → (/.*)?  ,  * → [^/]*)
            → requiredRole null/kosong → PUBLIK
            → requiredRole ada → validasi JWT, cek roles dari claims
        → Controller
```

### Rule Default (seeded oleh DataInitializer)
| sort_order | Method | Pattern | Role | Keterangan |
|-----------|--------|---------|------|-----------|
| 1 | `*` | `/error` | - | Error handler |
| 2 | `POST` | `/api/auth/login` | - | Login publik |
| 3 | `POST` | `/api/auth/register` | - | Register publik |
| 4-8 | `*` | Swagger, OpenAPI, Health, Metrics, H2 | - | Dev tools |
| 10 | `GET` | `/api/products/**` | - | Publik |
| 11 | `GET` | `/api/categories/**` | - | Publik |
| 12 | `GET` | `/api/external/**` | - | Publik |
| 13 | `GET` | `/api/diagnostic/**` | - | Publik |
| 20-25 | `POST/PUT/DELETE` | `/api/categories/**`, `/api/products/**` | ROLE_ADMIN | Admin only |
| 26 | `*` | `/api/permissions/**` | ROLE_ADMIN | Kelola permission |
| 27 | `*` | `/api/cache/**` | ROLE_ADMIN | Cache management |
| 30 | `*` | `/api/files/**` | ROLE_USER | Upload/download file |
| 999 | `*` | `/api/**` | ROLE_USER | Default fallback |

### Menambah Rule untuk Endpoint Baru
```bash
POST /api/permissions
Authorization: Bearer <admin-token>

{ "http_method": "GET", "url_pattern": "/api/orders/**", "required_role": "ROLE_USER", "sort_order": 40, "description": "Baca order" }
```

---

## 7. Langkah Membuat Fitur CRUD Baru

Contoh: membuat fitur **Order Management**.

### Step 1 — Entity
```java
@Entity @Table(name = "orders")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Order extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
```

### Step 2 — Repository
```java
@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {
    public PanacheQuery<Order> findByUserId(Long userId) {
        return find("user.id", userId);
    }
}
```

### Step 3 — Request DTO
```java
@Getter @NoArgsConstructor
public class OrderRequest {
    @NotBlank private String orderNumber;
    @NotNull private Long userId;
}
```

### Step 4 — Response DTO
```java
@Getter @Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private LocalDateTime createdAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
```

### Step 5 — Service
Ikuti pola di section 4.4. Gunakan `@Transactional` untuk semua operasi tulis.
Lempar `ResourceNotFoundException` atau `BusinessException`.

### Step 6 — Controller
Ikuti pola di section 4.5. Tambahkan `@Tag`, `@Operation`, `@SecurityRequirement`, `@Logged`.

### Step 7 — Daftarkan Permission di DataInitializer (atau via API)
```java
// Tambahkan di seedPermissions() dalam DataInitializer.java (profile local):
perm(40, "GET",    "/api/orders/**", "ROLE_USER",  "Baca order"),
perm(41, "POST",   "/api/orders",    "ROLE_USER",  "Buat order"),
perm(42, "PUT",    "/api/orders/**", "ROLE_ADMIN", "Update order"),
perm(43, "DELETE", "/api/orders/**", "ROLE_ADMIN", "Hapus order"),
```

---

## 8. Pemanggilan API Eksternal (MicroProfile REST Client)

```java
// rest/NamaApiClient.java
@RegisterRestClient(configKey = "nama-api")
@Path("/endpoint")
public interface NamaApiClient {
    @GET
    List<NamaResponse> getData();
}

// Inject di service:
@Inject @RestClient NamaApiClient namaApiClient;
```

Tambahkan URL di `application.properties`:
```properties
quarkus.rest-client.nama-api.url=https://api.example.com
```

DTO untuk external API: gunakan `@JsonProperty` eksplisit karena tidak ikut konvensi snake_case internal.

---

## 9. Konfigurasi per Profile

| Property | local | dev | prod |
|----------|-------|-----|------|
| Database | H2 in-memory | PostgreSQL | PostgreSQL |
| DB Name | `logitrackdb` | `rest_template_dev` | via env var `DB_URL` |
| `schema-generation` | `drop-and-create` | `update` | `validate` |
| SQL logging | enabled | enabled | disabled |
| Log level app | DEBUG | DEBUG | INFO |

**Cara menjalankan:**
```bash
# Profile local (H2, seed data otomatis)
mvn quarkus:dev -Dquarkus.profile=local

# Swagger UI
http://localhost:8080/q/swagger-ui

# Health check
http://localhost:8080/q/health
```

**Credential prod via environment variable:**
```bash
DB_URL=jdbc:postgresql://...
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET=...
```

**JWT:** secret Base64, expiration 10 menit (600000ms), claims: `sub` (username) + `roles` (list).

---

## 10. Logging & Tracing

- **`LoggingInterceptor`**: log otomatis `[ENTRY]`, `[EXIT]`, durasi untuk semua class yang pakai `@Logged`
- **`RequestIdFilter`**: isi `traceId` (dari OTel Span aktif) dan `requestId` (dari header atau UUID baru) ke MDC setiap HTTP request
- **Log pattern:** `timestamp | level | thread | logger | traceId=... | requestId=... | pesan`
- Nilai requestId dikembalikan di response header `X-Request-Id`

### traceId vs requestId

| | `traceId` | `requestId` |
|---|---|---|
| Dibuat oleh | OpenTelemetry (server, otomatis) | Client via header `X-Request-Id`, atau UUID otomatis |
| Dikontrol client? | Tidak | Ya |
| Tujuan | Debug distributed tracing (Jaeger) | Korelasi log antara client dan server |
| Format | Hex 32 karakter (W3C TraceContext) | Bebas — UUID, sequence number, dll. |

**Kirim requestId dari Postman:**
```
Header: X-Request-Id: checkout-retry-3
```

### Verifikasi Tracing
```bash
GET /api/diagnostic/trace
→ { "status": "OK", "trace_id": "4bf92f3...", "mdc_trace_id": "4bf92f3...", "trace_active": true }
```

---

## 11. Data Default (Profile Local)

Dibuat otomatis oleh `DataInitializer.java` saat startup (hanya jika `roles` tabel masih kosong):

| Type | Data |
|------|------|
| User | `admin / admin123` → ROLE_ADMIN + ROLE_USER |
| User | `user / user123` → ROLE_USER |
| Category | Electronics, Clothing, Books |
| Product | iPhone 15 Pro, MacBook Pro, Kaos Polos, Spring Boot in Action, Clean Code |
| Tag | smartphone, laptop, apple, fashion, java, programming |
| Permission | 21 rules (lihat section 6) |

---

## 12. File Penting

| File | Fungsi |
|------|--------|
| `pom.xml` | Dependencies Maven (Quarkus BOM 3.8.3) |
| `src/main/resources/application.properties` | Base config |
| `src/main/resources/application-local.properties` | H2, drop-and-create, SQL logging |
| `src/main/resources/application-dev.properties` | PostgreSQL dev |
| `src/main/resources/application-prod.properties` | PostgreSQL prod via env var |

---

## 13. Hal yang Harus Diikuti

1. **Selalu gunakan `ApiResponse<T>`** sebagai wrapper response
2. **Selalu extend `BaseEntity`** — jangan tulis `createdAt`/`updatedAt` manual
3. **Jangan pakai `@JsonNaming`** di DTO — sudah global via `JacksonConfig` (`ObjectMapperCustomizer`)
4. **Jangan hardcode rule otorisasi** — tambahkan via `DataInitializer` (local) atau `/api/permissions`
5. **`@Transactional`** wajib untuk semua operasi tulis (Panache tidak punya `readOnly`)
6. **Lempar exception yang benar:** `ResourceNotFoundException` → 404, `BusinessException` → 409, `UnauthorizedException` → 401
7. **Format timestamp** input/output: `"yyyy-MM-dd HH:mm:ss"` — bukan ISO-8601
8. **Native query WAJIB pakai Projection interface + concrete class** — bukan `List<Object[]>` langsung
9. **Nama cache WAJIB pakai konstanta** dari `CacheConfig` — bukan string literal
10. **FileController:** jangan pakai `import jakarta.ws.rs.*;` — konflik dengan `java.nio.file.Path`
11. **Lombok version:** gunakan versi yang tersedia di local Maven repo — project ini pakai `1.18.36`
12. **`@Logged` wajib** di semua class controller agar `LoggingInterceptor` aktif

---

## 14. Perbedaan Teknis yang Perlu Diingat

### antMatch() — Custom Ant Path Matcher
Spring's `AntPathMatcher` tidak tersedia di Quarkus. Implementasi custom ada di `EndpointPermissionService.antMatch()`:
- `/**` → `(/.*)?` (matches base path DAN apapun di bawahnya)
- `**` → `.*`
- `*` → `[^/]*`

### PageResponse — Custom Pagination
Spring's `Page<T>` tidak tersedia. Gunakan:
```java
PageResponse.of(contentList, page, size, totalCount)
```

### Startup Order (Priority)
| Priority | Bean | Aksi |
|----------|------|------|
| 1 | `DataInitializer` | Seed data ke DB |
| 1 | `FileStorageService` | Buat direktori upload |
| 10 | `EndpointPermissionService` | Load permission rules ke cache |

DataInitializer harus selesai dulu sebelum EndpointPermissionService membaca tabel `endpoint_permissions`.

### Login Flow (tanpa AuthenticationManager)
```java
// Spring Boot: authenticationManager.authenticate(...)
// Quarkus: manual
User user = userRepository.findByUsername(username).orElseThrow();
if (!BCrypt.checkpw(rawPassword, user.getPassword())) throw new UnauthorizedException(...);
String token = jwtTokenProvider.generateToken(user);  // menerima User entity, bukan UserDetails
```
