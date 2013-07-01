-- MySQL dump 10.10
--
-- Host: localhost    Database: cyanos
-- ------------------------------------------------------
-- Server version	5.0.27-standard

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `assay`
--

DROP TABLE IF EXISTS `assay`;
CREATE TABLE `assay` (
  `plate` varchar(16) NOT NULL default '',
  `culture_id` varchar(32) default NULL,
  `location` varchar(8) NOT NULL default '',
  `activity` varchar(45) NOT NULL default '',
  `name` varchar(45) NOT NULL default '',
  `sample_id` bigint(20) unsigned default NULL,
  PRIMARY KEY  (`plate`,`location`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `assay_info`
--

DROP TABLE IF EXISTS `assay_info`;
CREATE TABLE `assay_info` (
  `id` varchar(16) NOT NULL,
  `target` varchar(32) default NULL,
  `date` date NOT NULL default '1970-01-01',
  `active_level` float default NULL,
  `unit` varchar(16) default '%s',
  `active_op` enum('eq','ne','gt','ge','lt','le') NOT NULL default 'ge',
  `name` text,
  `length` int(10) unsigned default NULL,
  `width` int(10) unsigned default NULL,
  `notes` text,
  PRIMARY KEY  (`id`),
  KEY `target` (`target`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `collection`
--

DROP TABLE IF EXISTS `collection`;
CREATE TABLE `collection` (
  `id` varchar(16) NOT NULL default '',
  `type` varchar(32) NOT NULL default '',
  `format` varchar(8) default NULL,
  `name` text,
  `length` int(10) unsigned default NULL,
  `width` int(10) unsigned default NULL,
  `notes` text,
  `parent` varchar(16) default NULL,
  PRIMARY KEY  (`id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `cryo`
--

DROP TABLE IF EXISTS `cryo`;
CREATE TABLE `cryo` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `collection` varchar(16) NOT NULL default '',
  `location` varchar(8) NOT NULL default '',
  `date` date NOT NULL default '0000-00-00',
  `source_id` bigint(20) unsigned default NULL,
  `thaw_id` bigint(20) unsigned default NULL,
  `notes` text,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `collection` (`collection`),
  KEY `source_id` (`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `data`
--

DROP TABLE IF EXISTS `data`;
CREATE TABLE `data` (
  `file` varchar(200) NOT NULL default '',
  `type` varchar(32) default NULL,
  `description` text,
  `id` varchar(32) NOT NULL default '',
  `tab` varchar(16) NOT NULL default 'species',
  PRIMARY KEY  (`file`,`tab`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `data_templates`
--

DROP TABLE IF EXISTS `data_templates`;
CREATE TABLE `data_templates` (
  `data` varchar(32) NOT NULL COMMENT 'Data type for the template, e.g. fraction_data or assay_data',
  `name` varchar(32) NOT NULL COMMENT 'Name of the template',
  `template` longblob COMMENT 'Template data.  Serialized java.util.HashMap',
  PRIMARY KEY  (`data`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Stores data templates for uploads';

--
-- Table structure for table `extract`
--

DROP TABLE IF EXISTS `extract`;
CREATE TABLE `extract` (
  `sample_id` bigint(20) unsigned NOT NULL,
  `harvest_id` bigint(20) unsigned NOT NULL,
  `solvent` varchar(32) default NULL,
  `type` varchar(32) default NULL,
  PRIMARY KEY  USING BTREE (`sample_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Extract Information';

--
-- Table structure for table `harvest`
--

DROP TABLE IF EXISTS `harvest`;
CREATE TABLE `harvest` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `date` date default NULL,
  `color` text,
  `type` varchar(8) default NULL,
  `cell_mass` float default NULL COMMENT 'Cell Mass in grams',
  `old_cell_mass` text,
  `media_volume` float default NULL COMMENT 'Media volume in liters',
  `old_media_volume` text,
  `notes` text,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `inoculation`
--

DROP TABLE IF EXISTS `inoculation`;
CREATE TABLE `inoculation` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `culture_id` varchar(32) NOT NULL default '',
  `date` date NOT NULL default '0000-00-00',
  `parent_id` bigint(20) unsigned default NULL,
  `media` text NOT NULL,
  `volume` float NOT NULL,
  `old_volume` text,
  `notes` text,
  `fate` enum('stock','cryo','harvest','dead') default NULL,
  `harvest_id` bigint(20) unsigned default NULL,
  `removed` date default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `harvest` (`harvest_id`),
  KEY `parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `username` varchar(15) NOT NULL default '',
  `role` varchar(15) NOT NULL default '',
  PRIMARY KEY  (`username`,`role`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Tomcat role mapping';

--
-- Table structure for table `sample`
--

DROP TABLE IF EXISTS `sample`;
CREATE TABLE `sample` (
  `sample_id` bigint(20) unsigned NOT NULL auto_increment,
  `collection` varchar(16) NOT NULL default '',
  `location` varchar(8) NOT NULL default '',
  `date` date NOT NULL default '1970-01-01',
  `name` text,
  `culture_id` varchar(32) default NULL,
  `notes` text,
  `unit` enum('g','L') NOT NULL default 'g',
  `vial_wt` varchar(45) default NULL,
  `scale` enum('k','','m','u') NOT NULL default 'm',
  PRIMARY KEY  USING BTREE (`sample_id`),
  KEY `collection` (`collection`),
  KEY `culture_id` (`culture_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `sample_acct`
--

DROP TABLE IF EXISTS `sample_acct`;
CREATE TABLE `sample_acct` (
  `acct_id` bigint(20) unsigned NOT NULL default '0',
  `sample_id` bigint(20) unsigned NOT NULL default '0',
  `date` date NOT NULL default '1970-01-01',
  `ref_table` varchar(45) default NULL,
  `ref_id` varchar(45) default NULL,
  `amount` float NOT NULL default '0',
  `notes` text,
  PRIMARY KEY  USING BTREE (`acct_id`,`sample_id`),
  KEY `sample_id` (`sample_id`),
  KEY `ref_table` (`ref_table`),
  KEY `ref_id` (`ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `sample_library`
--

DROP TABLE IF EXISTS `sample_library`;
CREATE TABLE `sample_library` (
  `library` varchar(16) NOT NULL,
  `collection` varchar(16) NOT NULL,
  `default_type` enum('extract','fraction','compound') NOT NULL default 'fraction',
  `name` text NOT NULL,
  `length` int(10) unsigned NOT NULL,
  `width` int(10) unsigned NOT NULL,
  `notes` text,
  PRIMARY KEY  USING BTREE (`collection`,`library`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Sample Library infomation';

--
-- Table structure for table `separation`
--

DROP TABLE IF EXISTS `separation`;
CREATE TABLE `separation` (
  `separation_id` bigint(20) unsigned NOT NULL auto_increment,
  `s_phase` text,
  `m_phase` text,
  `notes` text,
  `date` date default NULL,
  PRIMARY KEY  USING BTREE (`separation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Fractionation Infomation';

--
-- Table structure for table `separation_product`
--

DROP TABLE IF EXISTS `separation_product`;
CREATE TABLE `separation_product` (
  `separation_id` bigint(20) unsigned NOT NULL,
  `sample_id` bigint(20) unsigned NOT NULL,
  `fraction_number` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  USING BTREE (`separation_id`,`fraction_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Fractionation Result Infomation';

--
-- Table structure for table `separation_source`
--

DROP TABLE IF EXISTS `separation_source`;
CREATE TABLE `separation_source` (
  `separation_id` bigint(20) unsigned NOT NULL,
  `sample_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY  USING BTREE (`separation_id`,`sample_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Fractionation Source Infomation';

--
-- Table structure for table `species`
--

DROP TABLE IF EXISTS `species`;
CREATE TABLE `species` (
  `culture_source` text,
  `culture_id` varchar(32) NOT NULL default '',
  `tax_order` text,
  `name` text,
  `genus` varchar(45) default NULL,
  `media_name` text,
  `notes` text,
  `photo` blob,
  `date` date default NULL,
  `removed` date default NULL,
  `remove_reason` varchar(45) default NULL,
  PRIMARY KEY  (`culture_id`),
  KEY `removed` (`remove_reason`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `taxonomic`
--

DROP TABLE IF EXISTS `taxonomic`;
CREATE TABLE `taxonomic` (
  `kingdom` varchar(45) NOT NULL default '',
  `phylum` varchar(45) NOT NULL default '',
  `class` varchar(45) NOT NULL default '',
  `ord` varchar(45) NOT NULL default '',
  `family` varchar(45) NOT NULL default '',
  `genus` varchar(45) NOT NULL default '',
  `synonym` varchar(45) default NULL,
  PRIMARY KEY  (`genus`),
  KEY `family` (`family`),
  KEY `class` (`class`),
  KEY `phylum` (`phylum`),
  KEY `order` (`ord`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Taxonomic map';

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `username` varchar(15) NOT NULL default '',
  `password` varchar(48) NOT NULL default '',
  `fullname` text,
  `email` text,
  `style` text,
  PRIMARY KEY  (`username`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Cyanos Users';
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2008-03-11  6:22:35
