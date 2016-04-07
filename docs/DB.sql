CREATE DATABASE IF NOT EXISTS `freelancer_order_management` DEFAULT CHARACTER SET utf8;
USE `freelancer_order_management`;

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `oid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `title` char(40) NOT NULL,
  `price` float(8,1) NOT NULL,
  `adiv` tinyint(1) NOT NULL,
  `bdiv` tinyint(1) NOT NULL,
  `deadline` datetime NOT NULL,
  `place` char(40) DEFAULT NULL,
  `deposit` float(6,1) DEFAULT NULL,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `orderdesc`
--

DROP TABLE IF EXISTS `orderdesc`;
CREATE TABLE `orderdesc` (
  `oid` int(10) unsigned NOT NULL,
  `desc` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `orderstatus`
--

DROP TABLE IF EXISTS `orderstatus`;
CREATE TABLE `orderstatus` (
  `oid` int(10) unsigned NOT NULL,
  `promulgator` int(10) unsigned NOT NULL,
  `applicant` int(10) unsigned NOT NULL,
  `status` tinyint(1) NOT NULL,
  `isread` tinyint(1) NOT NULL,
  PRIMARY KEY (`oid`),
  KEY `select` (`promulgator`,`applicant`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `uid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` char(16) NOT NULL,
  `pw` char(40) NOT NULL,
  `usertype` tinyint(1) NOT NULL,
  `joindate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
