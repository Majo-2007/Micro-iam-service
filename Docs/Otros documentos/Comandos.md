cd design-software-iam-service
.\mvnw.cmd clean verify

.\mvnw.cmd spring-boot:run -pl iam-api

---------------------------------
### Opción rápida — Apunta la app al usuario que sí existe

En la misma terminal, antes de correr Maven:

```powershell
$env:IAM_DB_DSN="jdbc:postgresql://localhost:15432/design-software-develop"
$env:IAM_DB_USERNAME="design_software_user"
$env:IAM_DB_PASSWORD="change-me"
./mvnw spring-boot:run -pl iam-api
```

> ⚠️ Estas variables solo viven mientras esa ventana de PowerShell esté abierta. Si cierras la terminal, tienes que volver a exportarlas antes de correr `mvnw` de nuevo.
