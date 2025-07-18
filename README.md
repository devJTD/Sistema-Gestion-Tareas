<<<<<<< HEAD
# Sistema-Gestion-Tareas
=======
<div align="center">

# 📋 Sistema de Gestión de Tareas
### *Solución completa para la gestión eficiente de proyectos y equipos*

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

</div>

---

## 🎯 **Descripción del Proyecto**

> Sistema de gestión de tareas desarrollado con **Spring Boot**, **MySQL** y tecnologías web modernas. Diseñado para optimizar la productividad de equipos mediante la creación, asignación y seguimiento efectivo de tareas.

## 📋 **Prerrequisitos del Sistema**

> Antes de comenzar, asegúrate de tener instalados los siguientes componentes:

### 🔧 **Requisitos Obligatorios**

| Componente | Versión Mínima | Comando de Verificación |
|------------|----------------|-------------------------|
| **Java JDK** | 17+ | `java -version` |
| **Maven** | 3.8+ | `mvn -version` |
| **MySQL** | 8.0+ | `mysql --version` |
| **Git** | 2.0+ | `git --version` |

### 📦 **Instalación de Dependencias**

<details>
<summary><b>🖥️ Windows</b></summary>

```powershell
# Instalar Java JDK 17
# Descargar desde: https://adoptium.net/

# Instalar Maven
# Descargar desde: https://maven.apache.org/download.cgi

# Instalar MySQL
# Descargar desde: https://dev.mysql.com/downloads/mysql/
```

</details>

<details>
<summary><b>🐧 Linux (Ubuntu/Debian)</b></summary>

```bash
# Actualizar repositorios
sudo apt update

# Instalar Java JDK 17
sudo apt install openjdk-17-jdk

# Instalar Maven
sudo apt install maven

# Instalar MySQL
sudo apt install mysql-server
```

</details>

<details>
<summary><b>🍎 macOS</b></summary>

```bash
# Instalar Homebrew (si no está instalado)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Instalar Java JDK 17
brew install openjdk@17

# Instalar Maven
brew install maven

# Instalar MySQL
brew install mysql
```

</details>

---

## 🚀 **Guía de Instalación**

### **Paso 1: Clonar el Repositorio**

```bash
git clone https://github.com/devJTD/Sistema-Gestion-Tareas.git
cd Sistema-Gestion-Tareas
```

### **Paso 2: Configurar Base de Datos**

> **💡 Tip:** JPA creará automáticamente todas las tablas basándose en tus entidades y la base de datos `@Entity` recuerda verificar que tu usuario y contraseña en `src/main/resources/application.properties`coincida con tu base de datos 

### **Paso 3: Configurar Aplicación**

Editar en caso sea necesario `src/main/resources/application.properties`:

```properties
# ===================================
# CONFIGURACIÓN DE BASE DE DATOS
# ===================================
spring.datasource.url=jdbc:mysql://localhost:3306/freedombd?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

---

## ⚡ **Ejecución del Proyecto**

### **Opción 1: Usando Maven** *(Recomendado)*

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn spring-boot:run
```

### **Opción 2: Desde tu IDE Favorito**

1. **IntelliJ IDEA:** Abrir proyecto → Run `TaskManagementApplication.java`
2. **Eclipse:** Import project → Run as Java Application
3. **VS Code:** Open folder → Run with Spring Boot Extension

### **Opción 3: Ejecutar JAR**

```bash
# Generar JAR ejecutable
mvn clean package

# Ejecutar JAR
java -jar target/Sistema-Gestion-Tareas-1.0.0.jar
```

---

## ✅ **Verificación de Instalación**

### 🌐 **Acceso a la Aplicación**

Una vez ejecutada la aplicación, accede a:

- **🏠 Login:** http://localhost:8080
---

## 🤝 **Contribución al Proyecto**

### **Proceso de Contribución**

1. *** Fork** el repositorio
2. *** Crear rama** para tu feature:
   ```bash
   git checkout -b feature/nueva-funcionalidad
   ```
3. **💻 Desarrollar** tu funcionalidad
4. **✅ Commit** tus cambios:
   ```bash
   git commit -m "Añadir nueva funcionalidad: descripción"
   ```
5. **📤 Push** a tu rama:
   ```bash
   git push origin feature/nueva-funcionalidad
   ```
6. **🔄 Crear Pull Request**


---
**¡Proyecto listo para usar! 🚀**

*Si encuentras algún problema durante la instalación, no dudes en crear un issue en el repositorio.*

</div>
>>>>>>> 7b7dbe4cb3319fb50a4423318bfa6a897eb9b6a2
