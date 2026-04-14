# Microservicio de Recibos de Sueldo

Microservicio encargado de listar y firmar digitalmente recibos de sueldo almacenados en Google Drive.
Forma parte de un sistema mayor que incluye un microservicio de login que maneja el flujo OAuth2 con Google.

---

## Indice

1. [Objetivo del proyecto](#objetivo-del-proyecto)
2. [Arquitectura general](#arquitectura-general)
3. [Tecnologias utilizadas](#tecnologias-utilizadas)
4. [Estructura del proyecto](#estructura-del-proyecto)
5. [Como funciona cada componente](#como-funciona-cada-componente)
6. [Configuracion de Google Cloud](#configuracion-de-google-cloud)
7. [Configuracion local](#configuracion-local)
8. [Como generar el certificado para firmar](#como-generar-el-certificado-para-firmar)
9. [Endpoints disponibles](#endpoints-disponibles)
10. [Flujo completo de una firma](#flujo-completo-de-una-firma)
11. [Errores comunes](#errores-comunes)

---

## Objetivo del proyecto

1. Conectarse a una carpeta especifica de Google Drive donde viven los recibos de sueldo en PDF.
2. Listar todos los PDFs de esa carpeta.
3. El empleado hace click en su recibo directamente desde la lista para firmarlo.
4. El microservicio aplica una **firma digital PKCS#7** al PDF y lo sube de vuelta a Drive.

El nombre de cada archivo sigue el formato: `{mes}-{año}-{legajo}` (ej: `marzo-2026-12345`).

---

## Arquitectura general

```
[ Microservicio Login ]
        |
        | OAuth2 con Google (Client ID + Client Secret)
        | → obtiene un access_token de Google con scope Drive
        | → lo devuelve al frontend
        |
        v
[ Frontend / Cliente ]
        |
        | Authorization: Bearer {access_token}
        |
        v
[ Microservicio Recibo ]  <-- este proyecto
        |
        +-- Usa el access_token para autenticarse contra Google Drive
        +-- Lista los PDFs de la carpeta configurada
        +-- El empleado elige su recibo y hace click en "Firmar"
        +-- Aplica firma digital al PDF
        +-- Sube el PDF firmado de vuelta a Drive
```

Este microservicio **no maneja OAuth2 por si mismo**. Recibe el `access_token` ya
generado por el microservicio de login en el header `Authorization: Bearer {token}`
y lo usa directamente para operar con Drive.

---

## Tecnologias utilizadas

| Tecnologia | Version | Para que se usa |
|---|---|---|
| Java | 17 | Lenguaje base |
| Spring Boot | 4.0.5 | Framework web y configuracion |
| H2 | runtime | Base de datos en memoria para desarrollo |
| Google Drive API v3 | rev20240521 | Listar, descargar y subir PDFs |
| Google Auth Library | 1.23.0 | Construir el cliente Drive desde el access token |
| Apache PDFBox | 3.0.3 | Manipulacion del PDF para agregar la firma |
| Bouncy Castle | 1.78.1 | Criptografia: genera el bloque PKCS#7 de la firma |
| Lombok | ultima | Reduce boilerplate (constructores, getters) |

---

## Estructura del proyecto

```
src/
└── main/
    ├── java/com/mobydigital/recibo/
    │   ├── ReciboApplication.java           # Entry point de Spring Boot
    │   ├── config/
    │   │   └── GoogleDriveConfig.java       # Factory que construye el cliente Drive desde el access token
    │   ├── service/
    │   │   ├── GoogleDriveService.java      # Operaciones contra Drive (listar, descargar, reemplazar)
    │   │   └── PdfSignatureService.java     # [PENDIENTE] Aplica firma digital al PDF
    │   └── controller/
    │       └── GoogleDriveController.java   # Endpoints REST: listar recibos y firmar
    └── resources/
        ├── application.properties           # Configuracion general (sin secretos)
        └── dev.properties                   # Valores locales reales (NO commitear)
```

---

## Como funciona cada componente

### GoogleDriveConfig.java

Ya no es un `@Bean` singleton. Es un `@Component` con un metodo `buildClient(accessToken)`
que construye un cliente de Drive autenticado por cada request, usando el token del usuario.

```
request entra con Authorization: Bearer {token}
    → controller extrae el token
    → llama a driveConfig.buildClient(token)
    → GoogleCredentials.create(new AccessToken(token))
    → Drive client listo para ese request
```

No se necesita ningun archivo de credenciales en el servidor. El token lo genera
Google via OAuth2 en el microservicio de login.

---

### GoogleDriveService.java

Todos los metodos reciben el `accessToken` como primer parametro y construyen
el cliente Drive en cada llamada:

| Metodo | Que hace |
|---|---|
| `listarRecibos(token)` | Lista todos los PDFs de la carpeta configurada, ordenados por nombre |
| `buscarPorId(token, fileId)` | Trae los metadatos de un archivo por su ID de Drive |
| `descargar(token, fileId)` | Descarga el binario del PDF como `byte[]` |
| `reemplazar(token, fileId, bytes)` | Sube el PDF firmado reemplazando el contenido del mismo archivo |

La carpeta que consulta se configura con `google.drive.folder-id` en `dev.properties`.

---

### GoogleDriveController.java

Extrae el token del header `Authorization: Bearer {token}` en cada endpoint
y lo pasa al servicio. Expone dos endpoints bajo `/api/recibos`:

**`GET /api/recibos`** — devuelve la lista de PDFs para que el empleado elija el suyo.

**`POST /api/recibos/{fileId}/firmar`** — el empleado hace click en su recibo,
se firma y se sube de vuelta a Drive sin navegar a otra pagina.

---

### PdfSignatureService.java — PENDIENTE

Se encargara de:
1. Cargar el keystore PKCS12 (`.p12`) con la clave privada y certificado.
2. Usar **PDFBox** para agregar el campo de firma al PDF.
3. Usar **Bouncy Castle** para generar el bloque PKCS#7 (SHA256withRSA).
4. Devolver el PDF firmado como `byte[]`.

---

## Configuracion de Google Cloud

### 1. Habilitar la Google Drive API

En el mismo proyecto de Google Cloud donde esta configurado el OAuth2 del microservicio de login:

`APIs y servicios` > `Biblioteca` > buscar `Google Drive API` > **Habilitar**

### 2. Agregar el scope de Drive al OAuth2 del login

En la configuracion del consentimiento OAuth2 del microservicio de login, agregar el scope:

```
https://www.googleapis.com/auth/drive
```

Esto hace que el `access_token` que genera el login incluya permisos para acceder a Drive.

### 3. Obtener el folder ID de la carpeta de recibos

La carpeta de recibos en Drive debe existir y estar accesible por el usuario autenticado.
El ID se obtiene de la URL:

```
https://drive.google.com/drive/folders/1aBcDeFgHiJkLmNo
                                        ^^^^^^^^^^^^^^^^^
                                             folder-id
```

Ese ID va en `dev.properties` como `google.drive.folder-id`.

> No hace falta compartir la carpeta con ninguna service account. El usuario
> autenticado via OAuth2 accede a su propio Drive con sus propios permisos.

---

## Configuracion local

### application.properties

```properties
spring.application.name=recibo
server.port=8080

spring.config.import=optional:classpath:dev.properties

# H2
spring.datasource.url=jdbc:h2:mem:recibodb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Google Drive
google.drive.folder-id=${GOOGLE_DRIVE_FOLDER_ID:}
```

### dev.properties

```properties
google.drive.folder-id=1aBcDeFgHiJkLmNo
```

Solo se necesita el folder ID. No hay credenciales ni secrets en este microservicio.

### .gitignore

```
src/main/resources/dev.properties
```

### Consola H2

Disponible en `http://localhost:8080/h2-console` mientras la app corre.

- **JDBC URL:** `jdbc:h2:mem:recibodb`
- **User:** `sa`
- **Password:** *(vacio)*

---

## Como generar el certificado para firmar

Para firmar PDFs se necesita un archivo `.p12` con clave privada y certificado.
Para pruebas se genera uno autofirmado con `keytool` (incluido en el JDK):

```bash
keytool -genkeypair \
  -alias firma-recibos \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365 \
  -storetype PKCS12 \
  -keystore src/main/resources/firma.p12 \
  -storepass tu-password-aqui \
  -dname "CN=Recibo Sueldo, O=MobyDigital, C=AR"
```

Agregar en `dev.properties`:

```properties
firma.keystore-path=firma.p12
firma.keystore-password=tu-password-aqui
firma.alias=firma-recibos
```

> Para produccion usar un certificado emitido por una CA reconocida.

---

## Endpoints disponibles

### GET /api/recibos

Lista todos los PDFs de la carpeta de Drive del usuario autenticado.

**Request:**
```
GET /api/recibos
Authorization: Bearer {access_token_de_google}
```

**Response:**
```json
[
  {
    "id": "1aBcDeFgHiJkLmNo",
    "name": "marzo-2026-12345",
    "createdTime": "2026-03-01T10:00:00.000Z",
    "webViewLink": "https://drive.google.com/file/d/1aBcDeFgHiJkLmNo/view"
  },
  {
    "id": "2xYzAbCdEfGhIjKl",
    "name": "marzo-2026-67890",
    "createdTime": "2026-03-01T10:01:00.000Z",
    "webViewLink": "https://drive.google.com/file/d/2xYzAbCdEfGhIjKl/view"
  }
]
```

### POST /api/recibos/{fileId}/firmar

El empleado hace click en su recibo de la lista. Se firma y se sube de vuelta a Drive.

**Request:**
```
POST /api/recibos/1aBcDeFgHiJkLmNo/firmar
Authorization: Bearer {access_token_de_google}
```

**Response exitosa:**
```
200 OK
"Recibo 'marzo-2026-12345' firmado correctamente."
```

**Response si el fileId no existe:**
```
404 Not Found
```

---

## Flujo completo de una firma

```
1. El microservicio de login autentica al empleado con Google OAuth2
   (Client ID + Client Secret + scope drive)
   → Google devuelve un access_token
   → El login lo reenvía al frontend

2. El frontend llama a este microservicio con el token
            |
            v
GET /api/recibos
Authorization: Bearer {access_token}
    → GoogleDriveConfig.buildClient(token)
    → GoogleCredentials.create(new AccessToken(token))
    → Drive client autenticado como ese usuario
    → lista todos los PDFs de la carpeta configurada
    → devuelve id, nombre, fecha y link de cada recibo

3. El empleado ve la lista e identifica su recibo (marzo-2026-12345)
   Hace click en "Firmar" directamente desde la lista
            |
            v
POST /api/recibos/1aBcDeFgHiJkLmNo/firmar
Authorization: Bearer {access_token}
            |
            v
GoogleDriveService.buscarPorId(token, fileId)
    → verifica que el archivo existe en Drive
            |
            v
GoogleDriveService.descargar(token, fileId)     ← cuando PdfSignatureService este listo
    → descarga el PDF como byte[]
            |
            v
PdfSignatureService.firmar(byte[] pdf)          ← PENDIENTE DE IMPLEMENTAR
    → carga keystore .p12
    → extrae clave privada + certificado
    → PDFBox agrega campo de firma al PDF
    → Bouncy Castle genera bloque PKCS#7 (SHA256withRSA)
    → devuelve PDF firmado como byte[]
            |
            v
GoogleDriveService.reemplazar(token, fileId, pdfFirmado)
    → sube el PDF firmado a Drive
    → reemplaza el contenido del mismo archivo (mismo nombre, mismo ID)
            |
            v
200 OK - Recibo firmado y guardado en Drive
```

---

## Errores comunes

### `Authorization header invalido`

El request no incluye el header `Authorization: Bearer {token}` o tiene formato incorrecto.
El token debe venir del microservicio de login luego de autenticarse con Google.

### `Could not resolve placeholder 'GOOGLE_DRIVE_FOLDER_ID'`

`dev.properties` no se esta cargando o no tiene definido `google.drive.folder-id`.
Verificar que el archivo existe en `src/main/resources/` y que `application.properties` tiene:
```properties
spring.config.import=optional:classpath:dev.properties
```

### `403 Forbidden` al llamar a Drive API

El `access_token` no tiene el scope de Drive. Verificar que el microservicio de login
solicita el scope `https://www.googleapis.com/auth/drive` al autenticar con Google.
