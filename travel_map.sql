-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: travel_map
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `travel_map`
--

/*!40000 DROP DATABASE IF EXISTS `travel_map`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `travel_map` /*!40100 DEFAULT CHARACTER SET utf8mb3 */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `travel_map`;

--
-- Table structure for table `ai_chat`
--

DROP TABLE IF EXISTS `ai_chat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `user_question` text NOT NULL COMMENT '用户提问',
  `ai_answer` text NOT NULL COMMENT 'AI返回回答',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI聊天记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_chat`
--

LOCK TABLES `ai_chat` WRITE;
/*!40000 ALTER TABLE `ai_chat` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_chat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `travel_comment`
--

DROP TABLE IF EXISTS `travel_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `travel_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `strategy_id` bigint NOT NULL,
  `comment_content` varchar(500) NOT NULL COMMENT '评论内容',
  `parent_id` bigint DEFAULT '0' COMMENT '父评论ID，0=顶级评论',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='攻略评论';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `travel_comment`
--

LOCK TABLES `travel_comment` WRITE;
/*!40000 ALTER TABLE `travel_comment` DISABLE KEYS */;
INSERT INTO `travel_comment` VALUES (1,5,3,'这是一条测试评论',0,'2026-09-12 19:40:13'),(3,5,3,'这又是一条测试评论',0,'2026-09-12 19:41:15'),(4,5,3,'这又是一条测试评论',0,'2026-09-12 19:41:16'),(5,5,9,'王玉龙sb',0,'2026-09-12 21:34:27'),(6,6,9,'真的假的',0,'2026-09-13 13:42:38'),(7,6,9,'你说的对',5,'2026-09-13 13:48:44'),(8,5,11,'我真帅',0,'2026-09-18 16:02:34'),(9,7,9,'我最帅',0,'2026-09-18 16:04:54'),(10,5,3,'这人是谁啊',0,'2026-09-25 12:11:39'),(11,5,3,'一个人',10,'2026-09-25 12:11:53');
/*!40000 ALTER TABLE `travel_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `travel_like`
--

DROP TABLE IF EXISTS `travel_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `travel_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `strategy_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_like_strategy` (`user_id`,`strategy_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `travel_like`
--

LOCK TABLES `travel_like` WRITE;
/*!40000 ALTER TABLE `travel_like` DISABLE KEYS */;
INSERT INTO `travel_like` VALUES (7,5,11,'2026-09-18 16:03:22'),(8,7,9,'2026-09-18 16:04:33'),(9,7,6,'2026-09-18 16:04:36'),(10,5,3,'2026-09-25 12:11:02');
/*!40000 ALTER TABLE `travel_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `travel_strategy`
--

DROP TABLE IF EXISTS `travel_strategy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `travel_strategy` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `city_name` varchar(60) NOT NULL COMMENT '所属城市',
  `spot_name` varchar(100) NOT NULL COMMENT '景点名称',
  `title` varchar(100) DEFAULT NULL COMMENT '攻略标题',
  `content` text NOT NULL COMMENT '攻略正文',
  `img_url` varchar(500) DEFAULT NULL COMMENT '图片地址',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `tag` varchar(30) DEFAULT NULL COMMENT '标签，例如：免费、热门',
  `view_count` int DEFAULT '0' COMMENT '访问量',
  `status` tinyint DEFAULT '1' COMMENT '状态 1=上线 0=下线',
  `category` varchar(50) NOT NULL DEFAULT '自然风光' COMMENT '内容分类：历史古迹、自然风光、美食打卡',
  `author_id` bigint DEFAULT NULL COMMENT '发布者用户ID',
  `collect_count` int DEFAULT '0' COMMENT '收藏数',
  `comment_count` int DEFAULT '0' COMMENT '评论数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='旅游攻略';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `travel_strategy`
--

LOCK TABLES `travel_strategy` WRITE;
/*!40000 ALTER TABLE `travel_strategy` DISABLE KEYS */;
INSERT INTO `travel_strategy` VALUES (1,'西安','兵马俑',NULL,'兵马俑是秦始皇陵陪葬坑，世界八大奇迹之一，建议游玩3‑4小时，避开节假日高峰。',NULL,0,'2026-09-02 18:34:50','2026-09-12 21:33:36','收费',6,1,'自然风光',2,0,0),(2,'西安','兵马俑',NULL,'兵马俑是秦始皇陵陪葬坑，世界八大奇迹之一，建议游玩3‑4小时，避开节假日高峰。',NULL,0,'2026-09-02 18:55:52','2026-09-10 16:16:02','收费',5,1,'历史古迹',1,0,0),(3,'西安','大雁塔',NULL,'大雁塔游玩攻略，晚上夜景很美，灯光氛围感强，适合傍晚前往。','https://images.kcxhgf.cn/boss/f083b78110ad31345e338e8f35f42200.jpg',1,'2026-09-06 17:02:25','2026-09-25 12:11:03','收费',5,1,'人文古迹',NULL,1,1),(5,'泰安','泰山',NULL,'泰山十八盘真难爬',NULL,0,'2026-09-06 17:15:37','2026-09-12 21:10:50','收费',3,1,'自然风光',4,0,0),(6,'西安','兵马俑',NULL,'这是一篇测试攻略内容','https://images.kcxhgf.cn/boss/cang.png',1,'2026-09-10 17:10:13','2026-09-18 17:05:43','免费',8,1,'历史文化',4,0,0),(9,'西安','兵马俑',NULL,'测试攻略内容','https://images.kcxhgf.cn/boss/cang.png',1,'2026-09-10 17:35:50','2026-09-22 21:00:25','收费',14,1,'历史文化',4,1,3),(11,'天津','布布林','测试','布布林昨天兼职累不累',NULL,1,'2026-09-13 11:34:32','2026-09-18 16:03:18','收费',8,1,'历史古迹',5,1,1),(12,'天津','布布林','测试测试','emmm',NULL,0,'2026-09-25 12:09:12','2026-09-25 12:09:12','收费',0,0,'自然风光',5,0,0),(14,'天津','布布林','测试测试','emmm','images.kcxhgf.cn/4121dc59990742c5bca198e7c1a4e725.png',0,'2026-09-25 12:09:18','2026-09-25 12:14:22','收费',1,1,'自然风光',5,0,0);
/*!40000 ALTER TABLE `travel_strategy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT 'BCrypt加密密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `is_admin` tinyint DEFAULT '0' COMMENT '0普通用户 1管理员',
  `status` tinyint DEFAULT '1' COMMENT '账号状态：1正常 0禁用',
  `nick_name` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','$2a$10$7JB720yG854Vd8z3z3z3zOzYxYxYxYxYxYxYxYxYxYxYxYxYxY','admin@travel.com',1,1,'admin',NULL,'2026-09-08 22:34:37','2026-09-13 12:56:53'),(2,'test01','$2a$10$j.fEhqQj1BeqyoQlnpSEvOkmbSv/jorWppQMCQ06Y6bVhf7.5y7BS','test01@qq.com',0,1,'test01',NULL,'2026-09-08 22:45:35','2026-09-13 12:56:53'),(3,'Tset02','$2a$10$to/F2nEkTU1ixQhnym9JHeY0fZbU2pxACQXLOeUNY9P/qOAVrYWe6',NULL,0,1,'Tset02',NULL,'2026-09-10 16:05:51','2026-09-13 12:56:53'),(4,'Tset03','$2a$10$rTHDylsKizuEDjmS7zMYLeGGHwKnbPfZsDFzCFUealpKe3X8TzHvu',NULL,0,1,'Tset03',NULL,'2026-09-10 16:57:01','2026-09-13 12:56:53'),(5,'admin2','$2a$10$uQOocdwPX5Gy/n8nHypZdOSUS9Kp2CBg2Hd5DF.C1MeHCy.zmLXPq',NULL,1,1,'admin2',NULL,'2026-09-10 17:02:15','2026-09-13 12:56:53'),(6,'wyl','$2a$10$OMRWjWKJg/79KExJPfQnq.1mmaVFK0K1VL.AqiiMcLtD0TQkAA8Ua',NULL,0,1,NULL,NULL,'2026-09-13 13:42:02','2026-09-13 13:42:02'),(7,'lzy','$2a$10$BotOQpos0tnlHNdkyG0e9./138BGQLywxVoGo3yI9ic43wY/TV/za',NULL,0,1,NULL,NULL,'2026-09-18 16:04:09','2026-09-18 16:04:09'),(8,'658','$2a$10$PibQV6UsxMYznwphSj2sr.a492Jd9KseKW4L6tUg1Rx9/Tl.kFZ06',NULL,0,1,NULL,NULL,'2026-09-22 21:01:01','2026-09-22 21:01:01'),(9,'李保芳','$2a$10$qGJ0MT9AR97XuBgbTiisUe0l332JDmBysEsWimmkPJkPB5hkTxaA6',NULL,0,1,NULL,NULL,'2026-09-25 12:13:59','2026-09-25 12:13:59');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_collect`
--

DROP TABLE IF EXISTS `user_collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户id',
  `strategy_id` bigint NOT NULL COMMENT '攻略id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_strategy` (`user_id`,`strategy_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_collect`
--

LOCK TABLES `user_collect` WRITE;
/*!40000 ALTER TABLE `user_collect` DISABLE KEYS */;
INSERT INTO `user_collect` VALUES (6,5,11,'2026-09-18 16:03:20'),(7,7,9,'2026-09-18 16:04:38'),(8,5,3,'2026-09-25 12:11:03');
/*!40000 ALTER TABLE `user_collect` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'travel_map'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-30 16:59:34
