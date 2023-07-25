# SpringBoot-Vue实现对k8s的管理设计方案

## 0.开发背景

应实验室需求，开发此前后端分离Web项目，实现用户在网页对Pod申请，得到Pod信息后可以使用ssh连接。管理员可以简单管理用户和Pod。

## 1.开发环境

操作系统：windows11

使用的工具：Git+IDEA+VSCode+MySQLWorkBrench+docker

使用的框架（组件）：Maven+MybatisPlus+Swagger+JWT+SpringCloudKubernetes

数据库：MySQL

## 2.后端部分

GitHub：[GitHub - Gohoy/k8s-web-backend](https://github.com/Gohoy/k8s-web-backend)

### 2.1数据库部分：

数据库名称为：k8s

只有一张表：users

```sql
-- MySQL dump 10.13  Distrib 8.0.32, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: k8s
-- ------------------------------------------------------
-- Server version    8.0.32

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `ctr_occupied` int DEFAULT '0',
  `ctr_name` text,
  `ctr_max` int DEFAULT '1',
  `vm_occupied` int DEFAULT '0',
  `vm_name` text,
  `vm_max` int DEFAULT '1',
  `is_admin` tinyint NOT NULL DEFAULT '0',
  `token` text NOT NULL,
  `last_login` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'gohoy','040424',0,NULL,1,0,NULL,1,1,'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImdvaG95IiwiaWF0IjoxNjkwMjY2NjQ4LCJleHAiOjE2OTA4NzE0NDh9.dap3Mw3L6FY24VLfstol3YD4oMs2jSppkzkVRSVGAvA','2023-07-25 14:30:49');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-07-25 16:31:59
```

#### 表user字段说明

| ColumName | id  | username | password | ctr_occupied | ctr_name | ctr_max | vm_occupied | vm_name | vm_max | is_admin | token | last_login |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| describe | 主键  | 用户名，不可重复 | 密码  | 已经申请的container数量 | 已经申请的container的名称 | 最多可以申请的container数量 | 已经申请的虚拟机的数量 | 已经申请的虚拟机的名称 | 最多可以申请的虚拟机的数量 | 是否是管理员 | 用于验证身份的token | 上次登录时间 |
| notNull | yes | yes | yes |     |     |     |     |     |     | yes | yes |     |
| default |     |     |     | 0   | null | 1   | 0   | null | 1   | 0   |     | null |
| autoIncrease | yes |     |     |     |     |     |     |     |     |     |     |     |

## 2.2后台架构说明

![](file:///C:/Users/34822/AppData/Roaming/marktext/images/2023-07-25-16-53-20-image.png?msec=1690275206303)

### 2.3具体实现

#### 2.3.1拦截器

前提（前后端统一）：在登录注册时，后台会通过JWT返回token，前端使用HttpOnly Cookie + CORS存放token和用户名，并在之后请求中携带。

有两个拦截器：

- LoginInterceptor：

    - 作用：判断用户是否已经登录
    - 原理：判断token包含的username是否和请求携带的username相同
    - 拦截范围：`/user/login`和`/user/register`之外的所有地址。
    - 开发阶段可以开启Swagger的权限
        - `"/swagger-ui.html", "/swagger-ui/**", "/webjars/swagger-ui/**", "/v3/api-docs/**"`
        - 这些都需要添加，否则Swagger无法正常访问
- AdminInterceptor

    - 作用：判断用户是否是管理员

    - 原理：使用请求携带的username从数据库中访问字段is_admin，得到结果。

    - 拦截范围：`/admin/**`