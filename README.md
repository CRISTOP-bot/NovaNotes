# NovaNotes ✦

Una app Android pequeña para guardar notas localmente.

## Características
- Crear notas.
- Verlas en una lista.
- Eliminar notas.
- Persistencia local con SharedPreferences.
- Sin internet ni cuentas.

## Compilar
Abre el proyecto con Android Studio y ejecuta:

```bash
./gradlew assembleDebug
```

Si tu entorno no tiene Gradle Wrapper, importa el proyecto en Android Studio para que use el Gradle configurado.

## GitHub Actions

El proyecto incluye workflows para:
- CI y compilación del APK debug.
- Tests automáticos.
- Android Lint.
- Dependency Review en pull requests.
- Releases automáticos al crear tags `v*.*.*`.
- Actualización semanal de dependencias con Dependabot.

Ejemplo de release:

```bash
git tag v1.0.0
git push origin v1.0.0
```
