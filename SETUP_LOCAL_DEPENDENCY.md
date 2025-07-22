# Configuración de Dependencia Local - MangoEconomy API

Esta guía te muestra cómo configurar MangoEconomy como dependencia local en tu proyecto.

## 📁 Estructura del Proyecto

```
TuPlugin/
├── lib/
│   └── MangoEconomy-1.1.0.jar     # JAR de MangoEconomy aquí
├── src/main/java/
│   └── com/tupackage/tuplugin/
├── src/main/resources/
│   └── plugin.yml
├── pom.xml                        # o build.gradle
└── README.md
```

## 🔧 Configuración Maven

### 1. Crear carpeta lib

```bash
mkdir lib
```

### 2. Copiar JAR de MangoEconomy

Coloca el archivo `MangoEconomy-1.1.0.jar` en la carpeta `lib/`:

```bash
cp MangoEconomy-1.1.0.jar lib/
```

### 3. Configurar pom.xml

Agrega esta dependencia a tu `pom.xml`:

```xml
<dependencies>
    <!-- Paper API -->
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.21.1-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- MangoEconomy API (dependencia local) -->
    <dependency>
        <groupId>com.spectrasonic</groupId>
        <artifactId>MangoEconomy</artifactId>
        <version>1.1.0</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/lib/MangoEconomy-1.1.0.jar</systemPath>
    </dependency>
</dependencies>
```

### 4. Configurar plugin.yml

```yaml
name: TuPlugin
version: 1.0.0
main: com.tupackage.tuplugin.TuPlugin
api-version: '1.21'
depend: [MangoEconomy]  # Dependencia obligatoria
# o
softdepend: [MangoEconomy]  # Dependencia opcional
```

## 🔧 Configuración Gradle

### 1. Configurar build.gradle

```gradle
plugins {
    id 'java'
}

repositories {
    mavenCentral()
    maven {
        name = 'papermc-repo'
        url = 'https://repo.papermc.io/repository/maven-public/'
    }
}

dependencies {
    // Paper API
    compileOnly 'io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT'
    
    // MangoEconomy API (dependencia local)
    compileOnly files('lib/MangoEconomy-1.1.0.jar')
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}
```

## 💻 Uso en el Código

### Importar la API

```java
import com.spectrasonic.MangoEconomy.api.MangoEconomyAPI;
import com.spectrasonic.MangoEconomy.api.events.MoneyAddEvent;
```

### Verificar disponibilidad

```java
public class TuPlugin extends JavaPlugin {
    
    private MangoEconomyAPI economyAPI;
    
    @Override
    public void onEnable() {
        // Verificar si MangoEconomy está disponible
        if (!MangoEconomyAPI.isAvailable()) {
            getLogger().severe("MangoEconomy no está disponible!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Obtener la API
        this.economyAPI = MangoEconomyAPI.getInstance();
        getLogger().info("MangoEconomy API cargada correctamente!");
    }
}
```

## 🚀 Compilación

### Maven

```bash
# Limpiar y compilar
mvn clean compile

# Crear JAR
mvn clean package

# El JAR estará en target/TuPlugin-1.0.0.jar
```

### Gradle

```bash
# Limpiar y compilar
./gradlew clean build

# El JAR estará en build/libs/TuPlugin-1.0.0.jar
```

## 📦 Distribución

### Para desarrollo local:

1. Compila tu plugin
2. Copia el JAR a la carpeta `plugins/` de tu servidor de pruebas
3. Asegúrate de que `MangoEconomy-1.1.0.jar` esté también en `plugins/`
4. Reinicia el servidor

### Para distribución:

1. **NO incluyas** el JAR de MangoEconomy en tu plugin final
2. En la documentación de tu plugin, especifica que requiere MangoEconomy
3. Los usuarios deben instalar MangoEconomy por separado

## ⚠️ Notas Importantes

### Scope "system" en Maven

- ✅ **Ventajas**: Fácil de configurar, no necesita repositorio
- ⚠️ **Desventajas**: No se incluye en el JAR final (esto es bueno para plugins)
- 📝 **Importante**: El JAR debe existir en la ruta especificada

### Alternativa con install local

Si prefieres instalar en tu repositorio local de Maven:

```bash
# Instalar MangoEconomy en repositorio local
mvn install:install-file \
  -Dfile=MangoEconomy-1.1.0.jar \
  -DgroupId=com.spectrasonic \
  -DartifactId=MangoEconomy \
  -Dversion=1.1.0 \
  -Dpackaging=jar

# Luego usar dependencia normal
<dependency>
    <groupId>com.spectrasonic</groupId>
    <artifactId>MangoEconomy</artifactId>
    <version>1.1.0</version>
    <scope>provided</scope>
</dependency>
```

## 🔍 Verificación

### Verificar que funciona:

```java
public void testAPI() {
    if (MangoEconomyAPI.isAvailable()) {
        MangoEconomyAPI api = MangoEconomyAPI.getInstance();
        getLogger().info("API disponible - Símbolo de moneda: " + api.getCurrencySymbol());
    } else {
        getLogger().warning("API no disponible");
    }
}
```

### En el log del servidor deberías ver:

```
[TuPlugin] MangoEconomy API cargada correctamente!
[TuPlugin] API disponible - Símbolo de moneda: $
```

## 🆘 Solución de Problemas

### Error: "Cannot resolve symbol MangoEconomyAPI"

- ✅ Verifica que el JAR esté en `lib/MangoEconomy-1.1.0.jar`
- ✅ Verifica la configuración en `pom.xml` o `build.gradle`
- ✅ Refresca/reimporta el proyecto en tu IDE

### Error: "MangoEconomy no está disponible"

- ✅ Verifica que MangoEconomy esté instalado en el servidor
- ✅ Verifica que tu plugin tenga `depend: [MangoEconomy]` en plugin.yml
- ✅ Verifica que MangoEconomy se cargue antes que tu plugin

### Error de compilación con Maven

- ✅ Verifica la ruta del systemPath
- ✅ Usa rutas relativas: `${project.basedir}/lib/MangoEconomy-1.1.0.jar`
- ✅ No uses rutas absolutas

---

¡Con esta configuración podrás usar la API de MangoEconomy fácilmente en todos tus proyectos! 🎉