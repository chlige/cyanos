-- -----------------------------------------------------
-- SQL Schema for CYANOS database
--
-- This file has the necessary SQL commands to create the CYANOS schema. 
-- The command to create the schema can be executed as follows. 
-- Please refer to MySQL documentation for more details on using the mysql command-line tool.
--
-- 	# mysql --tee cyanos.log cyanos < cyanos_mysql.sql
--
-- or if a user and password must be specified.
-- 
-- 	# mysql -u <username> -p <password> --tee cyanos.log cyanos < cyanos_mysql.sql
--
-- or the SQL file can be executed from within the MySQL command interface. 
-- In this case, the tee command can be used to log any messages to a file.
-- 
-- 	mysql> tee cyanos.log
-- 	mysql> source cyanos_mysql.sql
--
-- -----------------------------------------------------
-- CUSTOMIZING inoculation fates & harvest material types.
--
-- These columns use SQL data types that restrict the possible values
-- that any record may have.  For both instances, the CYANOS web application
-- uses the information of the current database schema to create web forms.
-- Thus, one can change the possible values and the CYANOS web application 
-- will reflect the change.  
--
-- NOTE: If you desire to customize the possible values for these columns, 
--	it is recommended that you do so when the schema is created.
--
-- Currently the "fate" for inoculation records is defined as an ENUM.
--
--  	`fate` ENUM('stock','cryo','harvest','dead') NULL DEFAULT NULL
--
-- The possible values defined in this schema are 'stock', 'cryo', 'harvest', 'dead', and NULL.
-- The `fate` for any inoculation record can be ONE of these values.
-- The CYANOS web application automatically reads the possible values for this field
-- and will present on the web form as a pull down field.
-- 
-- If different possible values are desired, one can change the definition for the `fate` column
-- either during schema creation (edit this file before creating the schema) or after the schema is 
-- created (via the ALTER TABLE statement) and the CYANOS web application will automatically change 
-- to reflect the defined column.
--
-- NOTE: If the column is changed after inoculation data has been entered with 'fate' values that
-- are NOT included in the new ENUM definition an SQL error may occur and the column may not 
-- be changed to the new definition.
--
-- The 'type' for harvest records is defined as a SET.
--
-- 		`type` SET('filamentous','encrusting','planktonic') NULL DEFAULT NULL ,
--
-- A SET is similar to an ENUM however the value can be ANY of the values allowed, including multiple values.
--
-- As with the 'fate' of inoculation records, the CYANOS web application reads the possible values for this 
-- field and will present on the web form as a series of checkboxes.
-- This field can also be customized before or after the schema is created.
-- 
-- -----------------------------------------------------

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `cyanos` ;
CREATE SCHEMA IF NOT EXISTS `cyanos` DEFAULT CHARACTER SET utf8 ;
SHOW WARNINGS;
USE `cyanos` ;

-- -----------------------------------------------------
-- Table `cyanos`.`assay`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`assay` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`assay` (
  `assay_id` VARCHAR(32) NOT NULL ,
  `culture_id` VARCHAR(32) NOT NULL ,
  `material_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0' ,
  `sample_id` BIGINT(20) UNSIGNED NULL DEFAULT NULL ,
  `name` VARCHAR(128) NOT NULL DEFAULT '' ,
  `row` INT(10) NOT NULL DEFAULT '0' ,
  `col` INT(10) NOT NULL DEFAULT '0' ,
  `sign` INT(1) NOT NULL DEFAULT '0' ,
  `activity` DECIMAL(30,15) NULL DEFAULT NULL ,
  `std_dev` DECIMAL(30,15) NULL DEFAULT NULL ,
  `concentration` DECIMAL(30,15) NOT NULL DEFAULT '0' ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY USING BTREE (`assay_id`, `row`, `col`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`assay_info`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`assay_info` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`assay_info` (
  `assay_id` VARCHAR(32) NOT NULL ,
  `name` VARCHAR(64) NOT NULL DEFAULT 'NONAME' ,
  `target` VARCHAR(32) NOT NULL DEFAULT 'UNASSIGNED' ,
  `date` DATE NOT NULL DEFAULT '1970-01-01' ,
  `length` INT(10) UNSIGNED NOT NULL DEFAULT '8' ,
  `width` INT(10) UNSIGNED NOT NULL DEFAULT '12' ,
  `sig_figs` INT(10) NOT NULL DEFAULT '5' ,
  `unit` VARCHAR(16) NULL DEFAULT '' ,
  `active_level` DECIMAL(30,15) NULL DEFAULT NULL ,
  `active_op` ENUM('eq','ne','gt','ge','lt','le') NOT NULL DEFAULT 'ge' ,
  `notes` TEXT NULL DEFAULT NULL ,
  `project_id` VARCHAR(32) NULL DEFAULT NULL ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  `remote_host` VARCHAR(36) NULL DEFAULT NULL ,
  PRIMARY KEY (`assay_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;
CREATE INDEX `target` ON `cyanos`.`assay_info` (`target` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`collection`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`collection` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`collection` (
  `collection_id` VARCHAR(32) NOT NULL ,
  `date` DATE NULL DEFAULT '1970-01-01' ,
  `location` VARCHAR(256) NULL DEFAULT NULL ,
  `latitude` DECIMAL(7,5) NULL DEFAULT NULL ,
  `longitude` DECIMAL(8,5) NULL DEFAULT NULL ,
  `geo_precision` INT(10) UNSIGNED NULL DEFAULT '182' ,
  `collector` VARCHAR(256) NOT NULL DEFAULT 'UNKNOWN' ,
  `notes` TEXT NULL DEFAULT NULL ,
  `project_id` VARCHAR(32) NULL DEFAULT NULL ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  `remote_host` VARCHAR(36) NULL DEFAULT NULL ,
  PRIMARY KEY USING BTREE (`collection_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`compound`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`compound` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`compound` (
  `compound_id` VARCHAR(32) NOT NULL ,
  `name` VARCHAR(128) NULL DEFAULT NULL ,
  `formula` VARCHAR(64) NULL DEFAULT NULL ,
  `smiles` VARCHAR(256) NULL DEFAULT NULL ,
  `average_wt` DECIMAL(10,4) NULL DEFAULT NULL ,
  `isotopic_wt` DECIMAL(11,5) NULL DEFAULT NULL ,
  `notes` TEXT NULL DEFAULT NULL ,
  `cml_data` LONGTEXT NULL DEFAULT NULL ,
  `mdl_data` LONGTEXT NULL DEFAULT NULL ,
  `inchi_string` TEXT NULL DEFAULT NULL ,
  `inchi_key` VARCHAR(256) NULL DEFAULT NULL ,
  `project_id` VARCHAR(45) NULL DEFAULT NULL ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  `remote_host` VARCHAR(26) NULL DEFAULT NULL ,
  PRIMARY KEY USING BTREE (`compound_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci
COMMENT = 'Compound Information';

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`cryo`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`cryo` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`cryo` (
  `cryo_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `collection` VARCHAR(16) NOT NULL DEFAULT '' ,
  `location` VARCHAR(8) NOT NULL DEFAULT '' ,
  `row` INT(10) NULL DEFAULT '0' ,
  `col` INT(10) NULL DEFAULT '0' ,
  `date` DATE NULL DEFAULT '1970-01-01' ,
  `source_id` BIGINT(20) UNSIGNED NULL DEFAULT NULL ,
  `culture_id` VARCHAR(32) NOT NULL ,
  `thaw_id` BIGINT(20) UNSIGNED NULL DEFAULT NULL ,
  `notes` TEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`cryo_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SHOW WARNINGS;
CREATE UNIQUE INDEX `id` ON `cyanos`.`cryo` (`cryo_id` ASC) ;

SHOW WARNINGS;
CREATE UNIQUE INDEX `cryo_id` ON `cyanos`.`cryo` (`cryo_id` ASC) ;

SHOW WARNINGS;
CREATE INDEX `collection` ON `cyanos`.`cryo` (`collection` ASC) ;

SHOW WARNINGS;
CREATE INDEX `source_id` ON `cyanos`.`cryo` (`source_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`cryo_library`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`cryo_library` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`cryo_library` (
  `collection` VARCHAR(32) NOT NULL ,
  `format` VARCHAR(32) NULL DEFAULT NULL ,
  `name` VARCHAR(64) NOT NULL DEFAULT 'NONAME' ,
  `length` INT(10) UNSIGNED NULL DEFAULT NULL ,
  `width` INT(10) UNSIGNED NULL DEFAULT NULL ,
  `notes` TEXT NULL DEFAULT NULL ,
  `date` DATE NULL DEFAULT '1970-01-01' ,
  `parent` VARCHAR(32) NULL DEFAULT NULL ,
  PRIMARY KEY (`collection`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`data`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`data` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`data` (
  `file` VARCHAR(200) NOT NULL DEFAULT '' ,
  `type` VARCHAR(32) NULL DEFAULT NULL ,
  `description` TEXT NULL DEFAULT NULL ,
  `id` VARCHAR(32) NOT NULL DEFAULT '' ,
  `tab` VARCHAR(16) NOT NULL DEFAULT 'species' ,
  `mime_type` VARCHAR(64) NULL DEFAULT NULL ,
  PRIMARY KEY (`file`, `tab`, `id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`data_templates`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`data_templates` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`data_templates` (
  `name` VARCHAR(32) NOT NULL DEFAULT '' ,
  `data` VARCHAR(128) NOT NULL DEFAULT '' ,
  `template` LONGBLOB NULL DEFAULT NULL ,
  PRIMARY KEY (`name`, `data`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`species`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`species` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`species` (
  `culture_source` VARCHAR(32) NULL DEFAULT NULL ,
  `culture_id` VARCHAR(32) NOT NULL ,
  `collection_id` VARCHAR(32) NULL DEFAULT NULL ,
  `isolation_id` VARCHAR(32) NULL DEFAULT NULL ,
  `name` VARCHAR(256) NULL DEFAULT 'NONAME' ,
  `genus` VARCHAR(45) NULL DEFAULT NULL ,
  `media_name` VARCHAR(256) NULL DEFAULT NULL ,
  `notes` TEXT NULL DEFAULT NULL ,
  `date` DATE NOT NULL DEFAULT '1970-01-01' ,
  `culture_status` VARCHAR(64) NULL DEFAULT 'good' ,
  `removed` DATE NULL DEFAULT NULL ,
  `remove_reason` VARCHAR(45) NULL DEFAULT NULL ,
  `project_id` VARCHAR(32) NULL DEFAULT NULL ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  `remote_host` VARCHAR(36) NULL DEFAULT NULL ,
  PRIMARY KEY (`culture_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;
CREATE INDEX `removed` ON `cyanos`.`species` (`remove_reason` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`material`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`material` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`material` (
  `material_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `label` VARCHAR(45) NULL DEFAULT NULL ,
  `culture_id` VARCHAR(32) NOT NULL DEFAULT 'UNASSIGNED' ,
  `date` DATE NOT NULL DEFAULT '1900-01-01' ,
  `notes` TEXT NULL DEFAULT NULL ,
  `project_id` VARCHAR(32) NULL DEFAULT NULL ,
  `amount_value` INT(11) NOT NULL DEFAULT '0' COMMENT 'Initial amount of material in grams' ,
  `amount_scale` INT(11) NOT NULL DEFAULT '0' ,
  `sample_id` BIGINT(20) UNSIGNED NULL DEFAULT NULL ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  `remote_host` VARCHAR(36) NULL DEFAULT NULL ,
  `remote_id` VARCHAR(36) NOT NULL ,
  PRIMARY KEY (`material_id`) ,
  CONSTRAINT `material_strain`
    FOREIGN KEY (`culture_id` )
    REFERENCES `cyanos`.`species` (`culture_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 4642
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;
CREATE UNIQUE INDEX `material_id_UNIQUE` ON `cyanos`.`material` (`material_id` ASC) ;

SHOW WARNINGS;
CREATE INDEX `material_strain_idx` ON `cyanos`.`material` (`culture_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`harvest`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`harvest` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`harvest` (
  `harvest_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `culture_id` VARCHAR(32) NOT NULL DEFAULT '' ,
  `date` DATE NOT NULL DEFAULT '1970-01-10' ,
  `color` VARCHAR(32) NULL DEFAULT NULL ,
  `type` SET('filamentous','encrusting','planktonic') NULL DEFAULT NULL ,
  `cell_mass_value` INT(11) NULL DEFAULT NULL ,
  `cell_mass_scale` INT(11) NULL DEFAULT NULL ,
  `prep_date` DATE NULL DEFAULT NULL ,
  `media_volume_value` INT(11) NULL DEFAULT NULL ,
  `media_volume_scale` INT(11) NULL DEFAULT NULL ,
  `notes` TEXT NULL DEFAULT NULL ,
  `collection_id` VARCHAR(32) NULL DEFAULT NULL ,
  `project_id` VARCHAR(32) NULL DEFAULT NULL ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  `remote_host` VARCHAR(36) NULL DEFAULT NULL ,
  `remote_id` VARCHAR(36) NOT NULL ,
  PRIMARY KEY (`harvest_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`extract_info`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`extract_info` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`extract_info` (
  `material_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0' ,
  `harvest_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0' ,
  `solvent` VARCHAR(256) NULL DEFAULT NULL ,
  `type` VARCHAR(128) NULL DEFAULT NULL ,
  `method` VARCHAR(256) NULL DEFAULT NULL ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY (`material_id`) ,
  CONSTRAINT `ext_material`
    FOREIGN KEY (`material_id` )
    REFERENCES `cyanos`.`material` (`material_id` )
    ON DELETE NO ACTION
    ON UPDATE CASCADE,
  CONSTRAINT `ext_harvest`
    FOREIGN KEY (`harvest_id` )
    REFERENCES `cyanos`.`harvest` (`harvest_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci
COMMENT = 'Extract Information';

SHOW WARNINGS;
CREATE INDEX `ext_harvest_idx` ON `cyanos`.`extract_info` (`harvest_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`inoculation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`inoculation` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`inoculation` (
  `inoculation_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `culture_id` VARCHAR(32) NULL DEFAULT NULL ,
  `date` DATE NOT NULL DEFAULT '1970-01-01' ,
  `parent_id` BIGINT(20) UNSIGNED NULL DEFAULT NULL ,
  `media` VARCHAR(256) NULL DEFAULT '' ,
  `volume_value` INT(11) NOT NULL DEFAULT '0' ,
  `volume_scale` INT(11) NOT NULL DEFAULT '0' ,
  `notes` TEXT NULL DEFAULT NULL ,
  `fate` ENUM('stock','cryo','harvest','dead') NULL DEFAULT NULL ,
  `harvest_id` BIGINT(20) UNSIGNED NULL DEFAULT NULL ,
  `removed` DATE NULL DEFAULT NULL ,
  `project_id` VARCHAR(32) NULL DEFAULT NULL ,
  PRIMARY KEY (`inoculation_id`) ,
  CONSTRAINT `inoc_strain`
    FOREIGN KEY (`culture_id` )
    REFERENCES `cyanos`.`species` (`culture_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;
CREATE INDEX `parent` ON `cyanos`.`inoculation` (`parent_id` ASC) ;

SHOW WARNINGS;
CREATE INDEX `inoc_strain_idx` ON `cyanos`.`inoculation` (`culture_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`isolation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`isolation` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`isolation` (
  `isolation_id` VARCHAR(32) NOT NULL DEFAULT '' ,
  `collection_id` VARCHAR(32) NOT NULL DEFAULT '' ,
  `date` DATE NOT NULL DEFAULT '1970-01-01' ,
  `type` VARCHAR(64) NOT NULL DEFAULT 'plate' ,
  `media` VARCHAR(256) NOT NULL DEFAULT 'Z' ,
  `parent` VARCHAR(64) NULL DEFAULT NULL ,
  `notes` TEXT NULL DEFAULT NULL ,
  `project_id` VARCHAR(32) NULL DEFAULT NULL ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY USING BTREE (`isolation_id`) ,
  CONSTRAINT `isolation_collection`
    FOREIGN KEY (`collection_id` )
    REFERENCES `cyanos`.`collection` (`collection_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SHOW WARNINGS;
CREATE INDEX `isolation_collection_idx` ON `cyanos`.`isolation` (`collection_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`news`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`news` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`news` (
  `subject` VARCHAR(512) NOT NULL DEFAULT '' ,
  `date_added` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `content` TEXT NULL DEFAULT NULL ,
  `expires` DATETIME NULL DEFAULT NULL ,
  PRIMARY KEY (`date_added`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`project`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`project` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`project` (
  `project_id` VARCHAR(32) NOT NULL DEFAULT '' ,
  `name` VARCHAR(128) NULL DEFAULT NULL ,
  `notes` TEXT NULL DEFAULT NULL ,
  `url` VARCHAR(128) NULL DEFAULT NULL ,
  `master_key` TEXT NULL DEFAULT NULL ,
  `update_prefs` VARCHAR(128) NULL DEFAULT NULL ,
  `last_update_sent` DATETIME NULL DEFAULT '1000-01-01 00:00:00' ,
  `last_update_message` TEXT NULL ,
  PRIMARY KEY USING BTREE (`project_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci
COMMENT = 'Project Code Information';

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`queue`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`queue` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`queue` (
  `queue_name` VARCHAR(128) NOT NULL DEFAULT '' ,
  `queue_type` VARCHAR(64) NOT NULL DEFAULT '' ,
  `item_type` VARCHAR(64) NOT NULL DEFAULT '' ,
  `item_id` VARCHAR(128) NOT NULL DEFAULT '' ,
  `added` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `completed` TIMESTAMP NULL DEFAULT NULL ,
  `req_details` VARCHAR(512) NULL DEFAULT NULL ,
  `complete_details` VARCHAR(512) NULL DEFAULT NULL ,
  `added_by` VARCHAR(15) NULL DEFAULT 'UNKNOWN' ,
  `completed_by` VARCHAR(15) NULL DEFAULT NULL ,
  PRIMARY KEY USING BTREE (`queue_name`, `queue_type`, `item_type`, `item_id`, `added`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`roles`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`roles` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`roles` (
  `username` VARCHAR(15) NOT NULL DEFAULT '' ,
  `role` VARCHAR(15) NOT NULL DEFAULT '' ,
  `project_id` VARCHAR(32) NOT NULL DEFAULT '' ,
  `perm` INT(10) UNSIGNED NOT NULL DEFAULT '0' ,
  PRIMARY KEY (`username`, `role`, `project_id`) )
ENGINE = MyISAM
DEFAULT CHARACTER SET = utf8
COMMENT = 'Tomcat role mapping';

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`sample`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`sample` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`sample` (
  `sample_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `material_id` BIGINT(20) UNSIGNED NOT NULL ,
  `collection` VARCHAR(32) NOT NULL DEFAULT 'UNASSIGNED' ,
  `row` INT(10) NULL DEFAULT NULL ,
  `col` INT(10) NULL DEFAULT NULL ,
  `name` TEXT NULL DEFAULT NULL ,
  `date` DATE NOT NULL DEFAULT '1970-01-01' ,
  `culture_id` VARCHAR(32) NOT NULL DEFAULT 'UNASSIGNED' ,
  `notes` TEXT NULL DEFAULT NULL ,
  `unit` VARCHAR(16) NULL DEFAULT NULL ,
  `concentration` FLOAT(11) NULL DEFAULT '0' ,
  `removed_date` DATE NULL DEFAULT NULL ,
  `removed_by` VARCHAR(15) NULL DEFAULT NULL ,
  `vial_wt` VARCHAR(45) NULL DEFAULT NULL ,
  `source_id` BIGINT(20) UNSIGNED NULL DEFAULT NULL ,
  `project_id` VARCHAR(32) NULL DEFAULT NULL ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01' ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY USING BTREE (`sample_id`) ,
  CONSTRAINT `sample_material`
    FOREIGN KEY (`material_id` )
    REFERENCES `cyanos`.`material` (`material_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 6051
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;
CREATE INDEX `collection` ON `cyanos`.`sample` (`collection` ASC) ;

SHOW WARNINGS;
CREATE INDEX `culture_id` ON `cyanos`.`sample` (`culture_id` ASC) ;

SHOW WARNINGS;
CREATE INDEX `sample_material_idx` ON `cyanos`.`sample` (`material_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`sample_acct`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`sample_acct` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`sample_acct` (
  `sample_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0' ,
  `acct_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0' ,
  `date` DATE NOT NULL DEFAULT '1970-01-01' ,
  `ref_table` VARCHAR(32) NULL DEFAULT NULL ,
  `ref_id` VARCHAR(32) NULL DEFAULT NULL ,
  `void_date` DATE NULL DEFAULT NULL ,
  `void_user` VARCHAR(15) NULL DEFAULT NULL ,
  `amount` FLOAT NOT NULL DEFAULT '0' ,
  `notes` VARCHAR(256) NULL DEFAULT '' ,
  `amount_value` INT NOT NULL DEFAULT '0' ,
  `amount_scale` INT NOT NULL DEFAULT '0' ,
  PRIMARY KEY USING BTREE (`acct_id`, `sample_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SHOW WARNINGS;
CREATE INDEX `sample_id` ON `cyanos`.`sample_acct` (`sample_id` ASC) ;

SHOW WARNINGS;
CREATE INDEX `ref_table` ON `cyanos`.`sample_acct` (`ref_table` ASC) ;

SHOW WARNINGS;
CREATE INDEX `ref_id` ON `cyanos`.`sample_acct` (`ref_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`sample_library`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`sample_library` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`sample_library` (
  `library` VARCHAR(32) NOT NULL DEFAULT 'UNASSIGNED' ,
  `collection` VARCHAR(32) NOT NULL ,
  `default_type` ENUM('extract','fraction','compound') NOT NULL DEFAULT 'fraction' ,
  `name` VARCHAR(256) NOT NULL DEFAULT '' ,
  `length` INT(10) UNSIGNED NOT NULL DEFAULT '0' ,
  `width` INT(10) UNSIGNED NOT NULL DEFAULT '0' ,
  `notes` TEXT NULL DEFAULT NULL ,
  PRIMARY KEY USING BTREE (`collection`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'Sample Library infomation';

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`separation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`separation` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`separation` (
  `separation_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `tag` VARCHAR(128) NULL DEFAULT NULL ,
  `date` DATE NOT NULL DEFAULT '1970-01-01' ,
  `s_phase` VARCHAR(256) NULL DEFAULT '' ,
  `m_phase` VARCHAR(256) NULL DEFAULT '' ,
  `method` VARCHAR(256) NULL DEFAULT '' ,
  `removed_date` DATE NULL DEFAULT NULL ,
  `removed_by` VARCHAR(15) NULL DEFAULT NULL ,
  `notes` TEXT NULL DEFAULT NULL ,
  `project_id` VARCHAR(32) NULL DEFAULT NULL ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  `remote_host` VARCHAR(36) NULL DEFAULT NULL ,
  `remote_id` VARCHAR(36) NOT NULL ,
  PRIMARY KEY USING BTREE (`separation_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'Fractionation Infomation';

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`separation_product`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`separation_product` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`separation_product` (
  `material_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0' ,
  `separation_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0' ,
  `fraction_number` INT(10) UNSIGNED NOT NULL DEFAULT '0' ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY USING BTREE (`separation_id`, `material_id`) ,
  CONSTRAINT `fraction_separation`
    FOREIGN KEY (`separation_id` )
    REFERENCES `cyanos`.`separation` (`separation_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fraction_material`
    FOREIGN KEY (`material_id` )
    REFERENCES `cyanos`.`material` (`material_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci
COMMENT = 'Fractionation Result Infomation';

SHOW WARNINGS;
CREATE INDEX `fraction_separation_idx` ON `cyanos`.`separation_product` (`separation_id` ASC) ;

SHOW WARNINGS;
CREATE INDEX `fraction_material_idx` ON `cyanos`.`separation_product` (`material_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`separation_source`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`separation_source` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`separation_source` (
  `material_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 ,
  `separation_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 ,
  `amount_value` INT(11) NOT NULL DEFAULT 0 ,
  `amount_scale` INT(11) NOT NULL DEFAULT 0 ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY USING BTREE (`separation_id`, `material_id`) ,
  CONSTRAINT `sep_src_sep`
    FOREIGN KEY (`separation_id` )
    REFERENCES `cyanos`.`separation` (`separation_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `sep_src_material`
    FOREIGN KEY (`material_id` )
    REFERENCES `cyanos`.`material` (`material_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci
COMMENT = 'Fractionation Source Infomation';

SHOW WARNINGS;
CREATE INDEX `sep_src_sep_idx` ON `cyanos`.`separation_source` (`separation_id` ASC) ;

SHOW WARNINGS;
CREATE INDEX `sep_src_material_idx` ON `cyanos`.`separation_source` (`material_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`users` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`users` (
  `username` VARCHAR(15) NOT NULL DEFAULT '' ,
  `password` VARCHAR(48) NOT NULL DEFAULT '' ,
  `fullname` VARCHAR(256) NULL DEFAULT '' ,
  `email` TEXT NULL ,
  `style` TEXT NULL ,
  PRIMARY KEY (`username`) )
ENGINE = MyISAM
DEFAULT CHARACTER SET = utf8
COMMENT = 'Cyanos Users';

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`config`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`config` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`config` (
  `element` VARCHAR(45) NOT NULL ,
  `param` VARCHAR(64) NOT NULL DEFAULT ' ' ,
  `param_key` VARCHAR(64) NOT NULL DEFAULT ' ' ,
  `value` TEXT NULL DEFAULT NULL )
ENGINE = MyISAM;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`compound_peaks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`compound_peaks` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`compound_peaks` (
  `compound_id` VARCHAR(32) NOT NULL ,
  `material_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0' ,
  `retention_time` DECIMAL(10,4) NULL DEFAULT '0.0000' ,
  `separation_id` BIGINT(20) UNSIGNED NULL DEFAULT NULL ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  CONSTRAINT `peak_compound_id`
    FOREIGN KEY (`compound_id` )
    REFERENCES `cyanos`.`compound` (`compound_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;
CREATE INDEX `peak_compound_id_idx` ON `cyanos`.`compound_peaks` (`compound_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`update_host`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`update_host` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`update_host` (
  `project_id` VARCHAR(45) NOT NULL ,
  `host_id` VARCHAR(45) NOT NULL ,
  `hostname` VARCHAR(45) NULL ,
  `pub_key` TEXT NOT NULL ,
  `last_update` DATETIME NOT NULL DEFAULT '1000-01-01 00:00:00' ,
  PRIMARY KEY (`project_id`, `host_id`) )
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`queue_subscription`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`queue_subscription` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`queue_subscription` (
  `queue_name` VARCHAR(128) NOT NULL ,
  `queue_type` VARCHAR(64) NOT NULL ,
  `username` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`queue_name`, `queue_type`, `username`) )
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`taxon`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`taxon` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`taxon` (
  `name` VARCHAR(64) NOT NULL ,
  `level` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`name`) )
ENGINE = InnoDB;

SHOW WARNINGS;
CREATE INDEX `taxa_level` ON `cyanos`.`taxon` (`level` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cyanos`.`taxon_paths`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cyanos`.`taxon_paths` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cyanos`.`taxon_paths` (
  `parent` VARCHAR(64) NOT NULL ,
  `child` VARCHAR(64) NOT NULL ,
  `depth` INT(11) NULL DEFAULT '0' ,
  CONSTRAINT `taxon_child`
    FOREIGN KEY (`child` )
    REFERENCES `cyanos`.`taxon` (`name` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `taxon_parent`
    FOREIGN KEY (`parent` )
    REFERENCES `cyanos`.`taxon` (`name` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;
CREATE INDEX `taxon_child_idx` ON `cyanos`.`taxon_paths` (`child` ASC) ;

-- -----------------------------------------------------
-- function parseActivity
-- -----------------------------------------------------

USE `cyanos`;
DROP function IF EXISTS `cyanos`.`parseActivity`;
SHOW WARNINGS;

DELIMITER $$
USE `cyanos`$$


CREATE DEFINER=`root`@`localhost` FUNCTION `parseActivity`(val VARCHAR(45)) RETURNS float
RETURN CASE 
		WHEN val LIKE ">%" THEN CAST(TRIM(">" FROM val) AS DECIMAL) + 1 
		WHEN val LIKE "<%" THEN CAST(TRIM("<" FROM val) AS DECIMAL) - 1 
		ELSE CAST(val AS DECIMAL)
	END$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function lonDMS
-- -----------------------------------------------------

USE `cyanos`;
DROP function IF EXISTS `cyanos`.`lonDMS`;
SHOW WARNINGS;

DELIMITER $$
USE `cyanos`$$


CREATE DEFINER=`root`@`localhost` FUNCTION `lonDMS`(c_value FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN CONCAT( IF( c_value < 0, 'W ', 'E '), DMS(c_value, prec))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function lonDM
-- -----------------------------------------------------

USE `cyanos`;
DROP function IF EXISTS `cyanos`.`lonDM`;
SHOW WARNINGS;

DELIMITER $$
USE `cyanos`$$


CREATE DEFINER=`root`@`localhost` FUNCTION `lonDM`(c_value FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN CONCAT( IF( c_value < 0, 'W ', 'E '), DM(c_value, prec))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function lonD
-- -----------------------------------------------------

USE `cyanos`;
DROP function IF EXISTS `cyanos`.`lonD`;
SHOW WARNINGS;

DELIMITER $$
USE `cyanos`$$


CREATE DEFINER=`root`@`localhost` FUNCTION `lonD`(lon FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN IF ( lon < 0, CONCAT('W ', ABS(ROUND(lon,prec)), 'Â°'), CONCAT('E ', ROUND(lon,prec), 'Â°'))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function locationAlpha
-- -----------------------------------------------------

USE `cyanos`;
DROP function IF EXISTS `cyanos`.`locationAlpha`;
SHOW WARNINGS;

DELIMITER $$
USE `cyanos`$$


CREATE DEFINER=`root`@`localhost` FUNCTION `locationAlpha`(x_val INT(10), y_val INT(10)) RETURNS varchar(3) CHARSET latin1
RETURN CONCAT(CHAR(64 + x_val), y_val)$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function latDMS
-- -----------------------------------------------------

USE `cyanos`;
DROP function IF EXISTS `cyanos`.`latDMS`;
SHOW WARNINGS;

DELIMITER $$
USE `cyanos`$$


CREATE DEFINER=`root`@`localhost` FUNCTION `latDMS`(c_value FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN CONCAT( IF ( c_value < 0, 'S ', 'N '), DMS(c_value, prec))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function latDM
-- -----------------------------------------------------

USE `cyanos`;
DROP function IF EXISTS `cyanos`.`latDM`;
SHOW WARNINGS;

DELIMITER $$
USE `cyanos`$$


CREATE DEFINER=`root`@`localhost` FUNCTION `latDM`(c_value FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN CONCAT( IF ( c_value < 0, 'S ', 'N '), DM(c_value, prec))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function latD
-- -----------------------------------------------------

USE `cyanos`;
DROP function IF EXISTS `cyanos`.`latD`;
SHOW WARNINGS;

DELIMITER $$
USE `cyanos`$$


CREATE DEFINER=`root`@`localhost` FUNCTION `latD`(lat FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN IF ( lat < 0, CONCAT('S ', ABS(ROUND(lat,prec)), 'Â°'), CONCAT('N ', ROUND(lat,prec), 'Â°'))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function isActive
-- -----------------------------------------------------

USE `cyanos`;
DROP function IF EXISTS `cyanos`.`isActive`;
SHOW WARNINGS;

DELIMITER $$
USE `cyanos`$$


CREATE DEFINER=`root`@`localhost` FUNCTION `isActive`(val VARCHAR(45), act_level FLOAT, act_op VARCHAR(2)) RETURNS tinyint(1)
RETURN IF( CHAR_LENGTH(val) < 1, 0, active( parseActivity(val), act_level, act_op ))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function active
-- -----------------------------------------------------

USE `cyanos`;
DROP function IF EXISTS `cyanos`.`active`;
SHOW WARNINGS;

DELIMITER $$
USE `cyanos`$$


CREATE DEFINER=`root`@`localhost` FUNCTION `active`(val FLOAT, act_level FLOAT, act_op VARCHAR(2)) RETURNS tinyint(1)
RETURN CASE act_op 
	WHEN 'eq' THEN IF( val = act_level, 1, 0)
	WHEN 'ne' THEN IF( val <> act_level, 1, 0)
	WHEN 'gt' THEN IF( val > act_level, 1, 0)
	WHEN 'ge' THEN IF( val >= act_level, 1, 0)
	WHEN 'lt' THEN IF( val < act_level, 1, 0)
	WHEN 'le' THEN IF( val <= act_level, 1, 0) END$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function DMS
-- -----------------------------------------------------

USE `cyanos`;
DROP function IF EXISTS `cyanos`.`DMS`;
SHOW WARNINGS;

DELIMITER $$
USE `cyanos`$$


CREATE DEFINER=`root`@`localhost` FUNCTION `DMS`(c_value FLOAT) RETURNS varchar(32) CHARSET utf8
RETURN CONCAT( ABS(TRUNCATE(c_value,0)), 'Â° ', 
	ABS(TRUNCATE((c_value * 60) % 60,0)), "\' ", 
	ABS(ROUND((c_value * 3600) % 3600,0)), "\"")$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function DM
-- -----------------------------------------------------

USE `cyanos`;
DROP function IF EXISTS `cyanos`.`DM`;
SHOW WARNINGS;

DELIMITER $$
USE `cyanos`$$


CREATE DEFINER=`root`@`localhost` FUNCTION `DM`(c_value FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN CONCAT( ABS(TRUNCATE(c_value,0)), 'Â° ', 
	ABS(ROUND((c_value * 60) % 60, ABS(ROUND(LOG10(1825 / prec))) )), "\'")$$

DELIMITER ;
SHOW WARNINGS;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `cyanos`.`config`
-- -----------------------------------------------------
START TRANSACTION;
USE `cyanos`;
INSERT INTO `cyanos`.`config` (`element`, `param`, `param_key`, `value`) VALUES ('database', 'version', 'protected', '2');

COMMIT;
