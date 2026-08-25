# Entidades de la Base de Datos IAM

## 1. Schema `identity`

**Función:** Manejar la identidad de los usuarios.

### Tabla: `user`

**Atributos:**
- `id`
- `email`
- `password_hash`
- `first_name`
- `last_name`
- `actor_type`
- `is_active`

**¿Para qué sirve?**  
Guarda la información básica y de acceso de cada usuario.

---

## 2. Schema `rbac_catalog`

**Función:** Organizar los módulos y funcionalidades del sistema.

### Tabla: `module`

**Atributos:**
- `id`
- `code`
- `name`
- `description`
- `display_order`
- `icon_key`
- `is_active`

**¿Para qué sirve?**  
Registra los módulos que existen en el sistema.

### Tabla: `feature`

**Atributos:**
- `id`
- `module_id`
- `code`
- `name`
- `description`
- `action_level`
- `is_active`

**¿Para qué sirve?**  
Registra las funciones que existen dentro de cada módulo.

---

## 3. Schema `rbac`

**Función:** Controlar los roles y permisos de los usuarios.

### Tabla: `role`

**Atributos:**
- `id`
- `name`
- `display_name`
- `description`
- `is_system_role`

**¿Para qué sirve?**  
Define los diferentes roles del sistema, como Administrador o Instructor.

### Tabla: `role_feature`

**Atributos:**
- `role_id`
- `feature_id`
- `scope_type`

**¿Para qué sirve?**  
Relaciona los roles con las funciones que pueden utilizar.

### Tabla: `user_role`

**Atributos:**
- `user_id`
- `role_id`
- `training_center_id`
- `assigned_by`
- `assigned_at`
- `expires_at`

**¿Para qué sirve?**  
Asigna uno o varios roles a los usuarios.

### Tabla: `user_scope_override`

**Atributos:**
- `user_id`
- `feature_id`
- `scope_type`
- `is_allowed`
- `reason`
- `granted_by`
- `expires_at`

**¿Para qué sirve?**  
Permite dar o quitar permisos específicos a un usuario.

---

## 4. Schema `session`

**Función:** Controlar las sesiones y recuperación de contraseña.

### Tabla: `refresh_token`

**Atributos:**
- `user_id`
- `token_hash`
- `device_hint`
- `ip_address`
- `expires_at`
- `is_revoked`

**¿Para qué sirve?**  
Mantiene y controla las sesiones activas de los usuarios.

### Tabla: `password_reset_request`

**Atributos:**
- `user_id`
- `token_hash`
- `expires_at`
- `is_used`
- `requested_at`
- `ip_address`

**¿Para qué sirve?**  
Gestiona las solicitudes de recuperación de contraseña.

---

## 5. Schema `identity_audit`

**Función:** Registrar las actividades relacionadas con el inicio de sesión.

### Tabla: `audit_login`

**Atributos:**
- `user_id`
- `email_attempted`
- `outcome`
- `ip_address`
- `user_agent`
- `attempted_at`

**¿Para qué sirve?**  
Registra los intentos de inicio de sesión, tanto exitosos como fallidos.

---

# Resumen

| Elemento | ¿Qué es? |
|---|---|
| **Schema** | Agrupa tablas relacionadas. |
| **Tabla** | Representa algo que el sistema necesita guardar. |
| **Atributo** | Es un dato que pertenece a una tabla. |
| **Función** | Explica para qué sirve la tabla dentro del sistema. |

### Ejemplo

`rbac` → `role` → `name`, `description` → Define los roles del sistema.