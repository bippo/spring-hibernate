
# Akses ke PostgreSQL menggunakan Spring Hibernate

Tutorial ini merupakan lanjutan dari tutorial [Spring JDBC](https://github.com/bippo/spring-jdbc). Pada Spring JDBC, kendala-kendala yang kita alami antara lain:
1. Harus membuat `RowMapper` untuk mapping hasil dari query **select** ke object di Java.
2. Menggunakan SQL untuk semua operasi select, insert, update dan delete, akan lebih baik jika ada level abstraksi lagi sebagai pengganti query tersebut.
3. Transaction management harus kita deklarasikan secara manual.

Pada akhir tutorial, struktur file kita sebagai berikut:
```
.
├── pom.xml
└── src
    └── main
        ├── java
        │   └── bippotraining
        │       ├── Application.java
        │       ├── HibernateXMLConf.java
        │       ├── dao
        │       │   ├── EmployeeDAO.java
        │       │   └── EmployeeDAOImpl.java
        │       ├── model
        │       │   └── Employee.java
        │       └── service
        │           ├── EmployeeService.java
        │           └── EmployeeServiceImpl.java
        └── resources
            ├── application.properties
            └── hibernate5Configuration.xml
```

## 1. Mengelompokkan File
Pada tutorial [Spring JDBC](https://github.com/bippo/spring-jdbc), kita masih menempatkan seluruh source code pada 1 package, pada tutorial ini akan dibuat 3 package:
1. `bippotraining.dao` berisi file interface dan impl dari DAO
2. `bippotraining.model` berisi file Java Object yang berasosiasi dengan struktur tabel
3. `bippotraining.service` berisi file interface dan impl intuk service

## 2. Konfigurasi file pom.xml
File pom.xml kita berisi library yang dibutuhkan untuk project ini, yaitu: hibernate, spring-boot-starter-data-jpa, driver postgresql dan tomcat-dbcp. 
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>bippo-training</groupId>
    <artifactId>boot-hibernate</artifactId>
    <version>1.0-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.4.RELEASE</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>5.4.0.Final</version>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>9.4-1200-jdbc41</version>
        </dependency>
        <dependency>
            <groupId>org.apache.tomcat</groupId>
            <artifactId>tomcat-dbcp</artifactId>
            <version>9.0.21</version>
        </dependency>

    </dependencies>

    <properties>
        <java.version>1.8</java.version>
    </properties>


    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>

```
## 3. Konfigurasi
Application properties akan kita konfigurasi agar Spring tidak menggunakan fitur web-server:
```properties
spring.main.web-application-type=NONE
```

Konfigurasi akses ke database, yang pada tutorial [Spring JDBC](https://github.com/bippo/spring-jdbc) kita letakkan di application.properties, kita pindahkan ke file hibernate5Configuration.xml. Akses ke Database tidak lagi menggunakan Spring JDBC, tetapi menggunakan Hibernate:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="sessionFactory"
          class="org.springframework.orm.hibernate5.LocalSessionFactoryBean">
        <property name="dataSource"
                  ref="dataSource"/>
        <property name="packagesToScan"
                  value="bippotraining.model"/>
        <property name="hibernateProperties">
            <props>
                <prop key="hibernate.hbm2ddl.auto">
                    none
                </prop>
                <prop key="hibernate.show_sql">true</prop>
                <prop key="hibernate.dialect">
                    org.hibernate.dialect.PostgreSQL95Dialect
                </prop>
            </props>
        </property>
    </bean>

    <bean id="dataSource"
          class="org.apache.tomcat.dbcp.dbcp2.BasicDataSource">
        <property name="driverClassName" value="org.postgresql.Driver"/>
        <property name="url" value="jdbc:postgresql://localhost:5432/training"/>
        <property name="username" value="training"/>
        <property name="password" value="training"/>
    </bean>

    <bean id="txManager"
          class="org.springframework.orm.hibernate5.HibernateTransactionManager">
        <property name="sessionFactory" ref="sessionFactory"/>
    </bean>
</beans>
``` 
Hal yang penting pada konfigurasi di atas, selain informasi database adalah lokasi dari file kelas kita yang berasosiasi dengan tabel di database. Kelas ini selanjutnya kita sebut kelas `Entity`, pada konfigurasi di atas, kita beritahukan kepada Hibernate bahwa kelas entity kit ada di package `bippotraining.model`.

Agar file xml tersebut di atas dapat dibaca oleh Spring, maka kita membuat class konfigurasi bernama `HibernateXMLConf`
```java
package bippotraining;  
  
import org.springframework.context.annotation.Configuration;  
import org.springframework.context.annotation.ImportResource;  
import org.springframework.transaction.annotation.EnableTransactionManagement;  
  
@Configuration  
@EnableTransactionManagement  
@ImportResource({"classpath:hibernate5Configuration.xml"})  
public class HibernateXMLConf {  
  
}
```
## 4. Model
Hibernate bekerja dengan melihat metada (anotasi) pada kelas Entity. Pada kelas kita, yaitu `bippotraining.model.Employee` dapat dilihat anotasi-anotasi seperti di bawah ini:
```java
package bippotraining.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "employee")
public class Employee {

    String employeeId;
    String employeeName;
    String employeeEmail;
    String employeeAddress;


    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    @Id
    @Column(name = "employeeid")
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    @Column(name = "employeename")
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeAddress() {
        return employeeAddress;
    }

    public void setEmployeeAddress(String employeeAddress) {
        this.employeeAddress = employeeAddress;
    }


    @Override
    public String toString() {
        return "[" + this.getEmployeeId() + ", " + this.getEmployeeName() + ", " + this.getEmployeeAddress() + ", " + this.getEmployeeEmail() + "]";
    }
}
```
### 1. @Entity @Table
Anotasi ini menandakan bahwa kelas tersebut adalah kelas entity dan anotasi @Table(name = "employee") menandakan bahwa Entity tersebut berasosiasi pada tabel employee.

### 2. @Id
Anotasi tersebut menandakan bahwa property tersebut identifier. Agar lebih mudah dipahami, untuk saat ini dapat dibaca bahwa properti tersebut berasosiasi dengan kolom id pada tabel di database.

### 3. @Column
`Column(name = "employeename")` menandakan bahwa property employeeName pada kelas Employee berasosiasi dengan kolom employeename pada tabel employee.

## 5. DAO
Jika pada Spring JDBC kita menggunakan library `NamedParameterJdbcTemplate`, maka di hibernate kita menggunakan library `SessionFactory`. Perhatikan method untuk memasukkan data employee dengan menggunakan Hibernate:

```java
@Override  
public void addEmployee(Employee emp) {  
    sessionFactory.getCurrentSession().save(emp);  
}
```
Jika dibandingkan dengan menggunakan JDBC:
```java
@Override  
public void addEmployee(Employee emp) {  
    final String sql = "insert into employee(employeeId, employeeName , employeeAddress, employeeEmail) values(:employeeId,:employeeName,:employeeAddress,:employeeEmail)";  
  KeyHolder holder = new GeneratedKeyHolder();  
  SqlParameterSource param = new MapSqlParameterSource()  
            .addValue("employeeId", emp.getEmployeeId())  
            .addValue("employeeName", emp.getEmployeeName())  
            .addValue("employeeEmail", emp.getEmployeeEmail())  
            .addValue("employeeAddress", emp.getEmployeeAddress());  
  jdbcTemplate.update(sql, param, holder);  
}
```
maka kode program untuk memasukkan data ke database jauh lebih sederhana jika kita menggunakan Hibernate. Perhatikan juga kode program untuk mengambil data dari database:
```java
@Override  
public List<Employee> getEmployee() {  
    return sessionFactory.getCurrentSession().createQuery("from Employee", Employee.class).list();  
}
```
Kode di atas (`from Employee`) tidak menggunakan SQL, tetapi menggunakan HQL (Hibernate Query Language). Lihat lebih lanjut HQL [di sini]([https://www.javatpoint.com/hql](https://www.javatpoint.com/hql)). Tidak diperlukan lagi `RowMapper`.

## 6. Service
Kelas Service merupakan abstraksi yang lebih tinggi dibanding kelas DAO. Jika DAO seperti disebutkan sebelumnya bertugas untuk menyediakan fungsi akses ke database, maka service akan menggunakan DAO untuk membuat abstraksi logika bisnis. 

```java
package bippotraining.service;  
  
import bippotraining.dao.EmployeeDAO;  
import bippotraining.model.Employee;  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.stereotype.Service;  
import org.springframework.transaction.annotation.Transactional;  
  
import java.util.List;  
  
@Service  
@Transactional(readOnly = true)  
public class EmployeeServiceImpl implements EmployeeService{  
  
  
    @Autowired  
  private EmployeeDAO employeeDAO;  
  
  @Override  
 @Transactional(readOnly = false)  
    public void addEmployee(Employee employee) {  
        employeeDAO.addEmployee(employee);  
  }  
  
    @Override  
 @Transactional(readOnly = false)  
    public void removeEmployee(String employeeId) {  
        employeeDAO.removeEmployee(employeeId);  
  }  
  
    @Override  
 @Transactional(readOnly = false)  
    public void updateEemployee(Employee employee) {  
        employeeDAO.updateEemployee(employee);  
  }  
  
    @Override  
  public List<Employee> getEmployee() {  
        return employeeDAO.getEmployee();  
  }  
}
```
 ### 1. @Service
 Anotasi ini menandakan bahwa kelas ini berjenis Service dan Spring akan membuat instance dari Object dari kelas yang mempunyai anotasi ini.
 
 ### 2. @Transactional(readOnly=true)
Pada bagian atas deklarasi kelas, menandakan bahwa semua method di dalam kelas tersebut default transaksi readOnly, yang berarti secara default method-method di dalam kelas tersebut tidak dapat mengubah data di database.

 ### 2. @Transactional(readOnly=false)
Pada bagian atas deklarasi method, menandakan bahwa method tersebut dapat mengubah data di database. Anotasi ini diperlukan untuk method-method yang mengubah data: `addEmployee()`, `removeEmployee()` dan `updateEmployee()`.

## 6. Aplikasi Utama
Melalui aplikasi utama, kita menguji apakah kelas Service kita sudah berfungsi sebagaimana mestinya. Kelas utama sebagai berikut:
```java
@Autowired  
private EmployeeService employeeService;  
  
public static void main(String...args){  
    SpringApplication.run(Application.class, args);  
}  
  
@Override  
public void run(String... args) throws Exception {  
    Employee emp = new Employee();  
  emp.setEmployeeId("01");  
  emp.setEmployeeAddress("San Jose");  
  emp.setEmployeeEmail("john@playground.com");  
  emp.setEmployeeName("John Doe");  
  employeeDao.addEmployee(emp);
  ....
}  
```
 
Private variable employeeService kita instantiasi dengan menggunakan Spring DI melalui anotasi `@Autowired`.