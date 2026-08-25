# EP-IAM — servicio iam

Épica del microservicio **iam** (Java/Spring · servicio de entrada, sin dependencias). Gestiona identidad de usuarios, emisión de **JWT** (acceso 15 min + actualización 7 días), **RBAC** por característica + alcance/centro, y **publica eventos** en `iam-events`. Es el único servicio que auténtica; los demás verifican el JWT localmente.

**Alcance:** HU-IAM-001..012.  
**Fuentes:** RF-IAM-01..05, RN-IAM-01..05, `09-microservices/services/01-iam-service`(README, rbac-design, data-model, contract, events).

**Brechas de arranque:** repositorio iam-service vacío → plantilla dorada Java/Spring; iam-db sin `outbox`// ( `refresh_token`conjunto `password_reset_request`de cambios aditivo).

---

# HU-IAM-001: Inicio de sesión, bloqueo por intentos y auditoría

## 🧩 Caracterización

Micro: `iam`· Capa: `backend`· Prioridad: Must · Estimación: 5

## Descripción (INVEST)

**Como quiero** autenticarme con mis credenciales **para** que el usuario acceda según mi rol. *(RF-IAM-01, RF-IAM-05)*

## Criterios (Gherkin)

**E1 iniciar sesión OK** — credenciales válidas → `200`con `access_token`(15 min) y `refresh_token`(7 días); se registra `identity_audit.audit_login`(éxito) y se reinicia el contador de fallos.  
**E2 inválido** — credenciales incorrectas → `401 INVALID_CREDENTIALS`; `audit_login`(fallo) e incrementa el contador.  
**E3 bloqueo** — 5 fallos → cuenta bloqueada 15 min; 10 → 24 h (RN-IAM-01). Iniciar sesión estando bloqueado → `423/401 ACCOUNT_LOCKED`.  
**E4 me** — `GET /auth/me`con token válido → identidad + roles/scopes del usuario.

## Detalle técnico

`POST /auth/login`, `GET /auth/me`· BCrypt · bloqueo sobre `identity.user`· escribe `identity_audit.audit_login`. Emite `iam.session.started`(ver HU-IAM-007).

## Insecto

- iam-db (identidad, identidad_auditoría) aplicado

  Plantilla dorada Java + contrato `iam.yaml`(iniciar sesión) congelado

## Departamento de Defensa

- Pruebas E1-E4 en verde (unidad + integración)

  Desplegado y promovido hasta`main`

  Traza OTel visible (ver HU-IAM-011)

---

# HU-IAM-002: Actualizar, cerrar sesión y gestión de sesiones

## 🧩 Caracterización

Micro: `iam`· Capa: `backend`· Prioridad: Must · Estimación: 3

## Descripción (INVEST)

**Como quiero** renovar y cerrar mis sesiones **para** mantener el acceso del usuario **sin** volver a iniciar sesión y poder revocar dispositivos. *(RF-IAM-01: varias sesiones simultáneas)*

## Criterios (Gherkin)

**E1 refresco** — `POST /auth/refresh`con refresco válido → nuevo `access_token`(sin rotar el refresco).  
**Cerrar sesión en E2** : `POST /auth/logout`revocar la actualización; reusarlo → `401 TOKEN_REVOKED`.  
**E3 multisesión** — varias sesiones activas por usuario; `GET /users/{id}/sessions`lista; `DELETE /users/{id}/sessions/{sid}`revocar una.

## Detalle técnico

`refresh_token`persistido (tabla `refresh_token`, gap de iam-db) con estado/TTL · revocación en logout/deactivate.

## Insecto

- HU-IAM-001 lista

  Tabla `refresh_token`migrada (gap iam-db)

## Departamento de Defensa

- Pruebas E1-E3 en verde (unidad + integración)

  Desplegado y promovido hasta`main`

  Traza OTel visible (ver HU-IAM-011)

---

# HU-IAM-003: Contraseña temporal, cambio en primer login y reset

## 🧩 Caracterización

Micro: `iam`· Capa: `backend`· Prioridad: Debería · Estimación: 3

## Descripción (INVEST)

**Como** nuevo **quiero** activar mi cuenta con una contraseña temporal y poder recuperarla **para** acceder de forma segura. *(RF-IAM-02, RN-IAM-05)*

## Criterios (Gherkin)

**E1 primer inicio de sesión** — usuario con contraseña temporal → el primer inicio de sesión exige cambio antes de emitir tokens plenos.  
**E2 caducidad** — contraseña temporal caducada (72 h) → rechazo, requiere reemisión.  
**Restablecimiento de E3** : `POST /auth/password-reset/request`token de género; `/confirm`fija nueva contraseña y revoca sesiones activas.

## Detalle técnico

Flag de `must_change_password`+ vencimiento en `identity.user`· tabla `password_reset_request`(gap iam-db) · rate-limit ( `RATE_LIMIT_EXCEEDED`).

## Insecto

- HU-IAM-001 lista

  Tabla `password_reset_request`migrada (gap iam-db)

## Departamento de Defensa

- Pruebas E1-E3 en verde (unidad + integración)

  Desplegado y promovido hasta`main`

  Traza OTel visible (ver HU-IAM-011)

---

# HU-IAM-004: Catálogo RBAC (módulos/características) y roles

## 🧩 Caracterización

Micro: `iam`· Capa: `backend`· Prioridad: Debería · Estimación: 3

## Descripción (INVEST)

**Como** administrador **quiero** consultar módulos, características y roles con sus características **para** gobernar el modelo de permisos.

## Criterios (Gherkin)

**E1 catálogo** — `GET /modules`, `GET /roles`, `GET /roles/{id}/features`devuelven el catálogo.  
**E2 alcance de rol** — un rol puede ser global ( `training_center_id = null`) o restringido a un centro.  
**E3 rol-característica** : `rbac.role_feature`define qué características (con alcance) otorga cada rol.

## Detalle técnico

Lectura de `rbac_catalog.module/feature`, `rbac.role/role_feature`.

## Insecto

- iam-db (rbac, rbac_catalog) aplicado + semillas de módulos/características/roles

## Departamento de Defensa

- Pruebas E1-E3 en verde (unidad + integración)

  Desplegado y promovido hasta`main`

  Traza OTel visible (ver HU-IAM-011)

---

# HU-IAM-005: Gestión de usuarios y asignación de roles

## 🧩 Caracterización

Micro: `iam`· Capa: `backend`· Prioridad: Debería · Estimación: 5

## Descripción (INVEST)

**Como** administrador **quiero** crear/editar/desactivar usuarios y asignarles roles con alcance **para** administrar el acceso. *(RF-IAM-03)*

## Criterios (Gherkin)

**E1 crear** — `POST /users`crea el usuario con contraseña temporal.  
**E2 editar/consultar** — `GET/PUT /users/{id}`, `GET /users`(paginado).  
**E3 desactivar** — `POST /users/{id}/deactivate`(borrado lógico) revoca sesiones y emite `iam.user.deactivated`(HU-IAM-007).  
**E4 roles** — `POST/DELETE /users/{id}/roles`asigna/revoca rol (opcionalmente restringido a un centro) y emite `iam.role.assigned`.

## Detalle técnico

`identity.user`, `rbac.user_role`(alcance/centro) · eliminación temporal (ver política docs#77).

## Insecto

- HU-IAM-004 lista

  HU-IAM-007 (bandeja de salida) para los eventos

## Departamento de Defensa

- Pruebas E1-E4 en verde (unidad + integración)

  Desplegado y promovido hasta`main`

  Traza OTel visible (ver HU-IAM-011)

---

# HU-IAM-006: Autorización por característica+alcance y anulaciones

## 🧩 Caracterización

Micro: `iam`· Capa: `backend`· Prioridad: Must · Estimación: 5

## Descripción (INVEST)

**Como** administrador **quiero** que cada operación se autorice por características y alcance **para** proteger los datos entre centros. *(RF-IAM-03, RN-IAM-03)*

## Criterios (Gherkin)

**E1 permitido** — usuario con el feature (vía rol) → autorizado.  
**E2 alcance** — un coordinador solo opera dentro de su centro asignado; fuera → denegado.  
**Anulación de E3** — `GET/POST/DELETE /users/{id}/scope-overrides`conceder/quita acceso excepcional individual.

## Detalle técnico

Función de evaluación+alcance combinando `user_role`, `role_feature`y `user_scope_override`. Expuesto como reclamo en el JWT (HU-IAM-010) para verificación local.

## Insecto

- Listas HU-IAM-004 y HU-IAM-005

## Departamento de Defensa

- Pruebas E1-E3 en verde (unidad + integración)

  Desplegado y promovido hasta`main`

  Traza OTel visible (ver HU-IAM-011)

---

# HU-IAM-007: Publicación de eventos de dominio (bandeja de salida → iam-events)

## 🧩 Caracterización

Micro: `iam`· Capa: `backend`· Prioridad: Debería · Estimación: 5

## Descripción (INVEST)

**Como** ecosistema **quiero** que iam publique sus eventos de dominio **para** que actores/audit reaccionen (perfil de actor, auditoría). *(eventos.md)*

## Criterios (Gherkin)

**E1 user.created** — al crear un usuario → se publica `iam.user.created`en `iam-events`.  
**E2 usuario.desactivado** — al desactivar → `iam.user.deactivated`.  
**E3 role.assigned** — al asignar un rol → `iam.role.assigned`.  
**E4 sesión.iniciada** — inicio de sesión exitoso → `iam.session.started`.  
Todos vía **outbox** (al menos una vez + idempotencia por `event_id`), sobre estándar (contratos-compartidos).

## Detalle técnico

Tabla `outbox`(gap iam-db) + relé a `iam-events`(tema) · consumidores: actor-service, audit-service.

## Insecto

- Tabla `outbox`migrada (gap iam-db)

  Agente + tema `iam-events`en docker-infra

## Departamento de Defensa

- Pruebas E1-E4 en verde (unidad + integración)

  Desplegado y promovido hasta`main`

  Traza OTel visible (ver HU-IAM-011)

---

# HU-IAM-008: Provisión coordinada usuario ↔ actor (invariante)

## 🧩 Caracterización

Micro: `iam`· Capa: `backend`· Prioridad: Debería · Estimación: 3

## Descripción (INVEST)

**Como** nuevo **quiero** activar mi cuenta con una contraseña temporal y poder recuperarla **para** acceder de forma segura. *(RF-IAM-04, RN-IAM-04)*

## Criterios (Gherkin)

**E1 emisión** — al crear un usuario con `actor_type`/ `actor_id`, `iam.user.created`viaja con esos campos para que actores-service cree el perfil.  
**E2 consistencia (lado iam)** — no se permite un estado inconsistente en iam; la creación es atómica (usuario + bandeja de salida).  
**E3 saga (diferido)** — el consumo/compensación en actores-servicio se cierra en la HU correspondiente de actores (dependencia cross-servicio).

## Detalle técnico

Extiende `iam.user.created`(HU-IAM-007) con `actor_type`/`actor_id`; contrato de coordinacion documentado. **Dependencia:** actores-servicio (aún no construido) para el E2E completo.

## Insecto

- HU-IAM-007 lista

## Departamento de Defensa

- Pruebas E1-E2 (E3 diferido a actores) en verde (unidad + integración)

  Desplegado y promovido hasta`main`

  Traza OTel visible (ver HU-IAM-011)

---

# HU-IAM-009: Informe de auditoría de inicio de sesión

## 🧩 Caracterización

Micro: `iam`· Capa: `backend`· Prioridad: Podría · Estimación: 2

## Descripción (INVEST)

**Como** rol de seguridad **quiero** consultar los intentos de iniciar sesión **para** auditar accesos.

## Criterios (Gherkin)

**E1 reporte** — `GET /reports/login-audit`devuelve `identity_audit.audit_login`paginado por cursor, solo lectura.  
**E2 autorización** — solo roles de seguridad/administración pueden consultarlo (feature+scope, HU-IAM-006).

## Detalle técnico

Lectura inmutable de `identity_audit.audit_login`· paginación por cursor.

## Insecto

- HU-IAM-001 (escribe audit_login) y HU-IAM-006 (autz) listas

## Departamento de Defensa

- Pruebas E1-E2 en verde (unidad + integración)

  Desplegado y promovido hasta`main`

  Traza OTel visible (ver HU-IAM-011)

---

# HU-IAM-010: Reclamaciones del JWT para verificación local

## 🧩 Caracterización

Micro: `iam`· Capa: `backend`· Prioridad: Must · Estimación: 3

## Descripción (INVEST)

**Como** el resto de servicios/edge **quiero** verificar el JWT localmente **para** no llamar a iam en cada solicitud. *(README: verificación local)*

## Criterios (Gherkin)

**E1 afirma** — el `access_token`lleva `sub`, `roles`, `scopes`/centro y `exp`.  
**E2 verificación local** — edge (Traefik, ADR-007) y servicios validan firma + reclamos sin llamar a iam.  
**Clave E3** — mecanismo de clave/rotación documentado (JWKS o secreto compartido gestionado).

## Detalle técnico

Firma JWT (lib Java) · publicación de clave pública/JWKS o secreto por config · alinear con verificación en el borde (ADR-007).

## Insecto

- Listas HU-IAM-001 y HU-IAM-006

## Departamento de Defensa

- Pruebas E1-E3 en verde (unidad + integración)

  Desplegado y promovido hasta`main`

  Traza OTel visible (ver HU-IAM-011)

---

# HU-IAM-011: Observabilidad OTel y salud

## 🧩 Caracterización

Micro: `iam`· Capa: `backend`· Prioridad: Debería · Estimación: 3

## Descripción (INVEST)

**Como** SRE **quiero** trazas/métricas/logs y health **para** operar iam. *(ADR-008)*

## Criterios (Gherkin)

**E1 trazas** — spans HTTP + spans de DB con `trace_id`.  
**E2 salud** — `/health`y `/ready`refleja DB (y broker si aplica).  
**Registros E3** : JSON `trace_id`embebido.

## Detalle técnico

OTel agente Java (automático) + exportador al recopilador de docker-infra (ADR-008).

## Insecto

- Collector OTel disponible en docker-infra

## Departamento de Defensa

- Pruebas E1-E3 en verde (unidad + integración)

  Desplegado y promovido hasta`main`

  Traza OTel visible (ver HU-IAM-011)

---

# HU-IAM-012: Levantado local de extremo a extremo

## 🧩 Caracterización

Micro: `iam`· Capa: `infra`· Prioridad: Must · Estimación: 5

## Descripción (INVEST)

**Como** desarrollador **quiero** levantar iam-api en local integrado en docker-infra **para** validar el flujo E2E. *(ADR-007)*

## Criterios (Gherkin)

**E1 componer** — `docker compose --profile iam up`levanta postgres + iam-api (+ broker si hay eventos) sin errores.  
**E2 edge** — salud de iam accesible vía el borde ( `/iam/health`), sin exponer el puerto interno.  
**E3 E2E** — iniciar sesión → acceder/actualizar → operación autorizada por característica+alcance → `iam.session.started`visible; traza en Grafana.

## Detalle técnico

Dockerfile iam-api + perfil `iam`en docker-infra + etiquetas Traefik ( `/iam`) + OTel endpoint. Aplicar lo aprendido en notificación (broker/DB fresco, validación E2E real).

## Insecto

- HU-IAM-001/006/007/011 al menos fusionadas

  Plantilla dorada Java desplegable (Dockerfile)

## Departamento de Defensa

- Pruebas E1-E3 en verde (unidad + integración)

  Desplegado y promovido hasta`main`

  Traza OTel visible (ver HU-IAM-011)

---

# HU-IAM-012: Levantado local de extremo a extremo

## 🧩 Caracterización

Micro: `iam`· Capa: `infra`· Prioridad: Must · Estimación: 5

## Descripción (INVEST)

**Como** desarrollador **quiero** levantar iam-api en local integrado en docker-infra **para** validar el flujo E2E. *(ADR-007)*

## Criterios (Gherkin)

**E1 componer** — `docker compose --profile iam up`levanta postgres + iam-api (+ broker si hay eventos) sin errores.  
**E2 edge** — salud de iam accesible vía el borde ( `/iam/health`), sin exponer el puerto interno.  
**E3 E2E** — iniciar sesión → acceder/actualizar → operación autorizada por característica+alcance → `iam.session.started`visible; traza en Grafana.

## Detalle técnico

Dockerfile iam-api + perfil `iam`en docker-infra + etiquetas Traefik ( `/iam`) + OTel endpoint. Aplicar lo aprendido en notificación (broker/DB fresco, validación E2E real).

## Insecto

- HU-IAM-001/006/007/011 al menos fusionadas

  Plantilla dorada Java desplegable (Dockerfile)

## Departamento de Defensa

- Pruebas E1-E3 en verde (unidad + integración)

  Desplegado y promovido hasta`main`

  Traza OTel visible (ver HU-IAM-011)