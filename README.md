# rest-template-quarkus

REST API backend template menggunakan **Quarkus 3.8.3** — port dari project `rest-template` (Spring Boot 3.2.3).
Logic bisnis, format response, dan konvensi kode identik. Hanya frameworknya yang berbeda.

---

## Tech Stack

| Teknologi | Versi |
|-----------|-------|
| Java | 21 |
| Quarkus | 3.8.3 |
| Hibernate ORM + Panache | via Quarkus BOM |
| RESTEasy Reactive + Jackson | via Quarkus BOM |
| MicroProfile REST Client | via Quarkus BOM |
| SmallRye OpenAPI | via Quarkus BOM |
| Quarkus Cache (Caffeine) | via Quarkus BOM |
| OpenTelemetry | via Quarkus BOM |
| JJWT | 0.12.3 |
| jBCrypt | 0.4 |
| Lombok | 1.18.36 |

---

## Fitur

- **JWT Stateless Auth** — login, register, validasi token via `SecurityFilter` (custom `ContainerRequestFilter`)
- **Dynamic Authorization** — aturan akses disimpan di DB (`endpoint_permissions`), bisa diubah tanpa restart
- **CRUD** — Category, Product dengan relasi, pagination, dan search
- **File Upload/Download** — simpan ke disk, serve via endpoint
- **Caching** — `@CacheResult` / `@CacheInvalidateAll` (Caffeine backend)
- **REST Client** — demo konsumsi API eksternal (JSONPlaceholder) via MicroProfile REST Client
- **Tracing** — OpenTelemetry: `traceId` + `requestId` di setiap log dan response header
- **Logging Interceptor** — `@Logged` annotation untuk log entry/exit/durasi otomatis
- **Swagger UI** — dokumentasi interaktif dengan auth support
- **Multi-profile** — H2 (local), PostgreSQL (dev/prod)

---

## Menjalankan Aplikasi

### Prasyarat
- Java 21
- Maven 3.8+

### Profile Local (H2 in-memory, seed data otomatis)

```bash
mvn quarkus:dev -Dquarkus.profile=local
```

Aplikasi berjalan di `http://localhost:8080`.

### Dev Mode dengan Hot Reload

Hot reload berjalan otomatis saat file diubah. Tidak perlu restart manual.

### Swagger UI

```
http://localhost:8080/q/swagger-ui
```

Klik **Authorize**, masukkan token dari endpoint `/api/auth/login`.

---

## Data Default (Profile Local)

Dibuat otomatis saat startup:

| Akun | Password | Role |
|------|----------|------|
| `admin` | `admin123` | ROLE_ADMIN + ROLE_USER |
| `user` | `user123` | ROLE_USER |

**Category:** Electronics, Clothing, Books

**Product:** iPhone 15 Pro, MacBook Pro, Kaos Polos, Spring Boot in Action, Clean Code

**Tag:** smartphone, laptop, apple, fashion, java, programming

---

## Endpoint Utama

| Method | URL | Auth | Keterangan |
|--------|-----|------|-----------|
| `POST` | `/api/auth/login` | - | Login, dapat JWT token |
| `POST` | `/api/auth/register` | - | Daftar akun baru |
| `GET` | `/api/auth/me` | Bearer | Data user yang sedang login |
| `GET` | `/api/categories` | - | List kategori (publik) |
| `GET` | `/api/categories/stats` | - | Jumlah produk per kategori (cached) |
| `POST` | `/api/categories` | Admin | Tambah kategori |
| `GET` | `/api/products` | - | List produk (publik) |
| `GET` | `/api/products/top?limit=5` | - | Top produk by harga (native query) |
| `GET` | `/api/products/tag/{tagName}` | - | Produk by tag (native query) |
| `POST` | `/api/products` | Admin | Tambah produk |
| `POST` | `/api/files/upload` | User | Upload file (multipart) |
| `GET` | `/api/files/download/{filename}` | User | Download file |
| `GET` | `/api/external/posts` | - | Ambil posts dari JSONPlaceholder |
| `GET` | `/api/diagnostic/trace` | - | Verifikasi OTel tracing |
| `POST` | `/api/cache/cleanup` | Admin | Bersihkan semua cache |
| `GET` | `/api/permissions` | Admin | List aturan otorisasi |
| `POST` | `/api/permissions` | Admin | Tambah/ubah aturan otorisasi |

---

## Format Response

Semua endpoint menggunakan wrapper `ApiResponse<T>`:

**Sukses:**
```json
{
  "error_schema": {
    "error_code": "RST-000",
    "error_message": { "english": "Success", "indonesian": "Berhasil" }
  },
  "output_schema": { ... }
}
```

**Error:**
```json
{
  "error_schema": {
    "error_code": "RST-002",
    "error_message": {
      "english": "Product dengan ID 99 tidak ditemukan",
      "indonesian": "Product dengan ID 99 tidak ditemukan"
    }
  }
}
```

**Pagination:**
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

---

## Tracing & Logging

Setiap HTTP request otomatis mendapat:
- `traceId` — dari OpenTelemetry span (32 hex char, W3C standard)
- `requestId` — dari header `X-Request-Id` atau UUID yang di-generate server

Kedua nilai muncul di setiap baris log dan dikembalikan di response header `X-Request-Id`.

**Kirim requestId custom dari Postman:**
```
Header: X-Request-Id: my-request-123
```

**Verifikasi tracing:**
```
GET /api/diagnostic/trace
```

---

## Konfigurasi Profile

### Dev (`%dev.` prefix)
```properties
# application-dev.properties
%dev.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/rest_template_dev
%dev.quarkus.datasource.username=postgres
%dev.quarkus.datasource.password=postgres
```

### Prod (`%prod.` prefix) — via environment variable
```bash
export DB_URL=jdbc:postgresql://host:5432/rest_template_prod
export DB_USERNAME=appuser
export DB_PASSWORD=secret
export JWT_SECRET=base64-encoded-secret-min-256-bit
```

---

## Build

```bash
# Build JAR (skip test)
mvn package -DskipTests

# Jalankan hasil build
java -jar target/quarkus-app/quarkus-run.jar

# Jalankan test
mvn test
```

---

## Perbedaan dari Spring Boot Versi

| Aspek | Spring Boot (`rest-template`) | Quarkus (project ini) |
|-------|------------------------------------|-----------------------|
| REST | `@RestController` + `@GetMapping` | `@Path` + `@GET` |
| DI | `@Autowired` / `@RequiredArgsConstructor` | `@Inject` |
| JPA | `extends JpaRepository` | `implements PanacheRepository` |
| Pagination | Spring `Page<T>` | `PageResponse.of(list, page, size, total)` |
| Config | `@Value` | `@ConfigProperty` |
| Startup hook | `@PostConstruct` | `@Observes StartupEvent` |
| AOP | `@Aspect` + `@Around` | CDI `@Interceptor` + `@AroundInvoke` |
| Exception handler | `@RestControllerAdvice` | `@Provider ExceptionMapper<T>` |
| Caching | `@Cacheable` / `@CacheEvict` | `@CacheResult` / `@CacheInvalidateAll` |
| REST Client | `@FeignClient` | `@RegisterRestClient` + `@Inject @RestClient` |
| Security | Spring Security | Custom `ContainerRequestFilter` + jBCrypt |
| Swagger | `/swagger-ui.html` | `/q/swagger-ui` |
| Tracing | Micrometer Tracing Bridge OTel | Quarkus OpenTelemetry |

---

## Struktur Direktori

```
src/main/java/com/example/template/
├── aspect/          # CDI Interceptor untuk logging
├── config/          # Jackson, OpenAPI, Cache constants, DataInitializer, RequestIdFilter
├── controller/      # JAX-RS resource classes
├── dto/
│   ├── request/     # Input DTO
│   └── response/    # Output DTO + ApiResponse envelope
├── entity/          # JPA entities
├── exception/       # Custom exceptions + ExceptionMapper providers
├── repository/      # PanacheRepository implementations
│   └── projection/  # Interface + concrete class untuk native query projection
├── rest/            # MicroProfile REST Client interfaces
├── security/        # JWT provider + security filter
└── service/         # Business logic
```

---

## Lisensi

Internal use — Developer Team.
