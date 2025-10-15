# 🚀 Configuración de MongoDB Externo para MythicEconomy

## 📋 Pasos para Conectar a MongoDB Externo

### 1. Habilitar MongoDB en el Plugin

Edita tu `config.yml`:

```yaml
database:
  use-external-database: true
  type: "MONGODB"
  mongodb:
    connection-string: "TU_CADENA_DE_CONEXION_AQUI"
    database: "MythicEconomy"
    collection: "player_economy"
```

### 2. Configurar la Cadena de Conexión

## 🔗 Tipos de Conexiones MongoDB

### 🌐 Conexión Local (MongoDB instalado en tu servidor)

```yaml
mongodb:
  connection-string: "mongodb://localhost:27017"
  database: "MythicEconomy"
```

**Para servidor con autenticación:**
```yaml
mongodb:
  connection-string: "mongodb://usuario:password@localhost:27017/MathicEconomy"
  database: "MythicEconomy"
```

### ☁️ MongoDB Atlas (Base de datos en la nube)

1. **Crear cuenta en MongoDB Atlas**: https://www.mongodb.com/atlas
2. **Crear un cluster gratuito**
3. **Obtener la cadena de conexión**:

```yaml
mongodb:
  connection-string: "mongodb+srv://mythiceconomy:tu_password@cluster0.xxxxx.mongodb.net"
  database: "MythicEconomy"
```

**Ejemplo real de Atlas:**
```yaml
mongodb:
  connection-string: "mongodb+srv://usuario:password@cluster0.abc123.mongodb.net/?retryWrites=true&w=majority"
  database: "MythicEconomy"
```

### 🖥️ Servidor Remoto (MongoDB en otro servidor)

```yaml
mongodb:
  connection-string: "mongodb://usuario:password@192.168.1.100:27017"
  database: "MythicEconomy"
```

O usando nombre de dominio:
```yaml
mongodb:
  connection-string: "mongodb://usuario:password@db.tu-dominio.com:27017"
  database: "MythicEconomy"
```

## ⚙️ Configuración Avanzada de Conexión

### Configuración de Pool de Conexiones

```yaml
mongodb:
  connection:
    timeout: 30
    max-pool-size: 20
    min-pool-size: 5
```

**Parámetros:**
- `timeout`: Tiempo máximo de espera para conexión (segundos)
- `max-pool-size`: Número máximo de conexiones simultáneas
- `min-pool-size`: Número mínimo de conexiones mantenidas

### Configuración de Seguridad

Para conexiones seguras con MongoDB Atlas:

```yaml
mongodb:
  connection-string: "mongodb+srv://usuario:password@cluster.mongodb.net/?ssl=true"
  connection:
    timeout: 60
    max-pool-size: 15
```

## 🔧 Solución de Problemas

### Error: "Connection timeout"

**Solución:**
- Aumenta el `timeout` en la configuración
- Verifica que el servidor MongoDB esté accesible desde tu servidor Minecraft
- Revisa el firewall y reglas de seguridad

### Error: "Authentication failed"

**Solución:**
- Verifica que el usuario y contraseña sean correctos
- Asegúrate de que el usuario tenga permisos en la base de datos
- Para Atlas, verifica que la IP de tu servidor esté en la whitelist

### Error: "Connection refused"

**Solución:**
- Verifica que MongoDB esté corriendo en el puerto especificado
- Para conexiones locales: `sudo systemctl status mongod`
- Para conexiones remotas: Verifica que el puerto 27017 esté abierto

## 📊 Verificación de Conexión

Después de configurar, revisa la consola de tu servidor Minecraft:

```
[MythicEconomy] Conexión a MongoDB establecida correctamente.
[MythicEconomy] Conectado a MongoDB - Base de datos: MythicEconomy, Colección: player_economy
```

Si ves errores, el plugin automáticamente hará fallback al sistema interno de archivos.

## 🔄 Cambiar entre Sistemas

Para cambiar de MongoDB al sistema interno:

```yaml
database:
  use-external-database: false
  type: "FILE"
```

Para cambiar del sistema interno a MongoDB:

```yaml
database:
  use-external-database: true
  type: "MONGODB"
```

**¡No necesitas reiniciar el servidor!** El plugin detecta automáticamente los cambios en la configuración.

## 🚨 Recomendaciones de Seguridad

1. **Usa autenticación**: Siempre configura usuario y contraseña
2. **Whitelist de IPs**: En MongoDB Atlas, agrega solo las IPs necesarias
3. **SSL/TLS**: Usa conexiones encriptadas cuando sea posible
4. **Credenciales seguras**: Usa contraseñas fuertes y únicas
5. **Backups regulares**: Mantén respaldos de tu base de datos

## 📞 Soporte

Si tienes problemas:
1. Revisa los logs del servidor Minecraft
2. Verifica la configuración en `config.yml`
3. Prueba la conexión MongoDB con un cliente como MongoDB Compass
4. Consulta la documentación oficial de MongoDB

¡Tu plugin MythicEconomy ahora puede usar MongoDB externo para almacenamiento de datos de economía! 🎉