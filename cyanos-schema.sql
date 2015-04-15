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

-- -----------------------------------------------------
-- Table `assay`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `assay` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `assay` (
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
-- Table `assay_info`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `assay_info` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `assay_info` (
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
CREATE INDEX `target` ON `assay_info` (`target` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `collection`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `collection` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `collection` (
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
-- Table `compound`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `compound` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `compound` (
  `compound_id` VARCHAR(32) NOT NULL ,
  `date_created` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
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
-- Table `cryo`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cryo` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cryo` (
  `cryo_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `collection` VARCHAR(16) NOT NULL DEFAULT '',
  `row` INT(10) NULL DEFAULT '0',
  `col` INT(10) NULL DEFAULT '0',
  `date` DATE NULL DEFAULT '1970-01-01',
  `source_id` BIGINT(20) UNSIGNED NULL DEFAULT NULL ,
  `culture_id` VARCHAR(32) NOT NULL ,
  `thaw_id` BIGINT(20) UNSIGNED NULL DEFAULT NULL ,
  `removed` DATE NULL DEFAULT NULL ,
  `notes` TEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`cryo_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SHOW WARNINGS;
CREATE INDEX `collection` ON `cryo` (`collection` ASC) ;

SHOW WARNINGS;
CREATE INDEX `source_id` ON `cryo` (`source_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `cryo_library`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cryo_library` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `cryo_library` (
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
-- Table `data`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `data` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `data` (
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
-- Table `data_templates`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `data_templates` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `data_templates` (
  `name` VARCHAR(32) NOT NULL DEFAULT '' ,
  `data` VARCHAR(128) NOT NULL DEFAULT '' ,
  `template` LONGBLOB NULL DEFAULT NULL ,
  PRIMARY KEY (`name`, `data`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `species`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `species` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `species` (
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
CREATE INDEX `removed` ON `species` (`remove_reason` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `material`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `material` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `material` (
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
    REFERENCES `species` (`culture_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 4642
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;
CREATE UNIQUE INDEX `material_id_UNIQUE` ON `material` (`material_id` ASC) ;

SHOW WARNINGS;
CREATE INDEX `material_strain_idx` ON `material` (`culture_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `harvest`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `harvest` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `harvest` (
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
-- Table `extract_info`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `extract_info` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `extract_info` (
  `material_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0' ,
  `harvest_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0' ,
  `solvent` VARCHAR(256) NULL DEFAULT NULL ,
  `type` VARCHAR(128) NULL DEFAULT NULL ,
  `method` VARCHAR(256) NULL DEFAULT NULL ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY (`material_id`) ,
  CONSTRAINT `ext_material`
    FOREIGN KEY (`material_id` )
    REFERENCES `material` (`material_id` )
    ON DELETE NO ACTION
    ON UPDATE CASCADE,
  CONSTRAINT `ext_harvest`
    FOREIGN KEY (`harvest_id` )
    REFERENCES `harvest` (`harvest_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci
COMMENT = 'Extract Information';

SHOW WARNINGS;
CREATE INDEX `ext_harvest_idx` ON `extract_info` (`harvest_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `inoculation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inoculation` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `inoculation` (
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
    REFERENCES `species` (`culture_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;
CREATE INDEX `parent` ON `inoculation` (`parent_id` ASC) ;

SHOW WARNINGS;
CREATE INDEX `inoc_strain_idx` ON `inoculation` (`culture_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `isolation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `isolation` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `isolation` (
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
    REFERENCES `collection` (`collection_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SHOW WARNINGS;
CREATE INDEX `isolation_collection_idx` ON `isolation` (`collection_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `news`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `news` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `news` (
  `subject` VARCHAR(512) NOT NULL DEFAULT '' ,
  `date_added` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `content` TEXT NULL DEFAULT NULL ,
  `expires` DATETIME NULL DEFAULT NULL ,
  PRIMARY KEY (`date_added`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `project`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `project` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `project` (
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
-- Table `queue`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `queue` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `queue` (
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
-- Table `roles`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `roles` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `roles` (
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
-- Table `sample`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sample` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `sample` (
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
    REFERENCES `material` (`material_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 6051
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;
CREATE INDEX `collection` ON `sample` (`collection` ASC) ;

SHOW WARNINGS;
CREATE INDEX `culture_id` ON `sample` (`culture_id` ASC) ;

SHOW WARNINGS;
CREATE INDEX `sample_material_idx` ON `sample` (`material_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `sample_acct`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sample_acct` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `sample_acct` (
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
CREATE INDEX `sample_id` ON `sample_acct` (`sample_id` ASC) ;

SHOW WARNINGS;
CREATE INDEX `ref_table` ON `sample_acct` (`ref_table` ASC) ;

SHOW WARNINGS;
CREATE INDEX `ref_id` ON `sample_acct` (`ref_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `sample_library`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sample_library` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `sample_library` (
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
-- Table `separation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `separation` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `separation` (
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
-- Table `separation_product`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `separation_product` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `separation_product` (
  `material_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0' ,
  `separation_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0' ,
  `fraction_number` INT(10) UNSIGNED NOT NULL DEFAULT '0' ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY USING BTREE (`separation_id`, `material_id`) ,
  CONSTRAINT `fraction_separation`
    FOREIGN KEY (`separation_id` )
    REFERENCES `separation` (`separation_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fraction_material`
    FOREIGN KEY (`material_id` )
    REFERENCES `material` (`material_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci
COMMENT = 'Fractionation Result Infomation';
SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `separation_source`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `separation_source` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `separation_source` (
  `material_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 ,
  `separation_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 ,
  `amount_value` INT(11) NOT NULL DEFAULT 0 ,
  `amount_scale` INT(11) NOT NULL DEFAULT 0 ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY USING BTREE (`separation_id`, `material_id`) ,
  CONSTRAINT `sep_src_sep`
    FOREIGN KEY (`separation_id` )
    REFERENCES `separation` (`separation_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `sep_src_material`
    FOREIGN KEY (`material_id` )
    REFERENCES `material` (`material_id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci
COMMENT = 'Fractionation Source Infomation';

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `users` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `users` (
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
-- Table `users_oauth`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `users_oauth` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `users_oauth` (
  `username` VARCHAR(15) NOT NULL DEFAULT '' ,
  `client_id` VARCHAR(256) NOT NULL DEFAULT ''
  PRIMARY KEY (`username`,`client_id`) )
ENGINE = MyISAM
DEFAULT CHARACTER SET = utf8
COMMENT = 'Cyanos Users OAuth details';

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `config`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `config` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `config` (
  `element` VARCHAR(45) NOT NULL ,
  `param` VARCHAR(64) NOT NULL DEFAULT ' ' ,
  `param_key` VARCHAR(64) NOT NULL DEFAULT ' ' ,
  `value` TEXT NULL DEFAULT NULL )
ENGINE = MyISAM;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `compound_peaks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `compound_peaks` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `compound_peaks` (
  `compound_id` VARCHAR(32) NOT NULL ,
  `material_id` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0' ,
  `retention_time` DECIMAL(10,4) NULL DEFAULT '0.0000' ,
  `separation_id` BIGINT(20) UNSIGNED NULL DEFAULT NULL ,
  `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  CONSTRAINT `peak_compound_id`
    FOREIGN KEY (`compound_id` )
    REFERENCES `compound` (`compound_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;
CREATE INDEX `peak_compound_id_idx` ON `compound_peaks` (`compound_id` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `update_host`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `update_host` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `update_host` (
  `project_id` VARCHAR(45) NOT NULL ,
  `host_id` VARCHAR(45) NOT NULL ,
  `hostname` VARCHAR(45) NULL ,
  `pub_key` TEXT NOT NULL ,
  `last_update` DATETIME NOT NULL DEFAULT '1000-01-01 00:00:00' ,
  PRIMARY KEY (`project_id`, `host_id`) )
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `queue_subscription`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `queue_subscription` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `queue_subscription` (
  `queue_name` VARCHAR(128) NOT NULL ,
  `queue_type` VARCHAR(64) NOT NULL ,
  `username` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`queue_name`, `queue_type`, `username`) )
ENGINE = InnoDB;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `taxon`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `taxon` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `taxon` (
  `name` VARCHAR(64) NOT NULL ,
  `level` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`name`) )
ENGINE = InnoDB;

SHOW WARNINGS;
CREATE INDEX `taxa_level` ON `taxon` (`level` ASC) ;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `taxon_paths`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `taxon_paths` ;

SHOW WARNINGS;
CREATE  TABLE IF NOT EXISTS `taxon_paths` (
  `parent` VARCHAR(64) NOT NULL ,
  `child` VARCHAR(64) NOT NULL ,
  `depth` INT(11) NULL DEFAULT '0' ,
  CONSTRAINT `taxon_child`
    FOREIGN KEY (`child` )
    REFERENCES `taxon` (`name` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `taxon_parent`
    FOREIGN KEY (`parent` )
    REFERENCES `taxon` (`name` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

SHOW WARNINGS;
CREATE INDEX `taxon_child_idx` ON `taxon_paths` (`child` ASC) ;

--
-- Table structure for table `compound_atoms`
--

DROP TABLE IF EXISTS `compound_atoms`;

CREATE TABLE IF NOT EXISTS `compound_atoms` (
  `compound_id` varchar(32) NOT NULL,
  `atom_number` int(10) unsigned NOT NULL DEFAULT '0',
  `element` varchar(2) DEFAULT NULL,
  `coord_x` decimal(8,4) DEFAULT '0.0000',
  `coord_y` decimal(8,4) DEFAULT '0.0000',
  `coord_z` decimal(8,4) DEFAULT '0.0000',
  `charge` tinyint(3) DEFAULT '0',
  `attached_h` tinyint(3) unsigned DEFAULT '0',
  PRIMARY KEY (`compound_id`,`atom_number`),
  CONSTRAINT `compound_atom` FOREIGN KEY (`compound_id`) REFERENCES `compound` (`compound_id`)
  	ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `compound_bonds`
--

DROP TABLE IF EXISTS `compound_bonds`;

CREATE TABLE IF NOT EXISTS `compound_bonds` (
  `compound_id` varchar(32) NOT NULL,
  `bond_id` int(10) unsigned NOT NULL,
  `bond_order` decimal(4,3) unsigned DEFAULT '0.000',
  `stereo` tinyint(3) unsigned DEFAULT '0',
  PRIMARY KEY (`compound_id`,`bond_id`),
  CONSTRAINT `compound_bond` FOREIGN KEY (`compound_id`) REFERENCES `compound` (`compound_id`)
  	ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `compound_bond_atoms`
--

DROP TABLE IF EXISTS `compound_bond_atoms`;

CREATE TABLE IF NOT EXISTS `compound_bond_atoms` (
  `compound_id` varchar(32) NOT NULL,
  `bond_id` int(10) unsigned NOT NULL,
  `atom_number` int(10) unsigned NOT NULL,
  PRIMARY KEY (`compound_id`,`bond_id`,`atom_number`),
  CONSTRAINT `graph_atom_id` FOREIGN KEY (`compound_id`, `atom_number`) 
  	REFERENCES `compound_atoms` (`compound_id`, `atom_number`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `graph_bond_id` FOREIGN KEY (`compound_id`, `bond_id`) 
  	REFERENCES `compound_bonds` (`compound_id`, `bond_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `compound_graph` FOREIGN KEY (`compound_id`) REFERENCES `compound` (`compound_id`)
  	ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP VIEW IF EXISTS `compound_diatomic`;

CREATE VIEW `compound_diatomic` AS
SELECT atom1.compound_id, atom1.atom_number as "atom1_number", atom1.element as "atom1_element", atom1.charge as "atom1_charge", atom1.attached_h as "atom1_H", bond.bond_order, atom2.atom_number as "atom2_number", atom2.element as "atom2_element", atom2.charge as "atom2_charge", atom2.attached_h as "atom2_H"
FROM compound_atoms atom1
JOIN compound_bond_atoms cab1 ON (atom1.compound_id = cab1.compound_id AND atom1.atom_number = cab1.atom_number)
JOIN compound_bonds bond ON ( atom1.compound_id = bond.compound_id AND cab1.bond_id = bond.bond_id)
JOIN compound_bond_atoms cab2 ON (atom1.compound_id = cab2.compound_id AND cab2.bond_id = cab1.bond_id AND cab2.atom_number != atom1.atom_number)
JOIN compound_atoms atom2 ON (atom1.compound_id = atom2.compound_id AND cab2.atom_number = atom2.atom_number);

DROP VIEW IF EXISTS `compound_triatomic`;

CREATE VIEW `compound_triatomic` AS
SELECT atom1.compound_id, atom1.atom_number as "atom1_number", atom1.element as "atom1_element", atom1.charge as "atom1_charge", atom1.attached_h as "atom1_H", 
bond1.bond_order as "bond1_order", 
atom2.atom_number as "atom2_number", atom2.element as "atom2_element", atom2.charge as "atom2_charge", atom2.attached_h as "atom2_H",
bond2.bond_order as "bond2_order",
atom3.atom_number as "atom3_number", atom3.element as "atom3_element", atom3.charge as "atom3_charge", atom3.attached_h as "atom3_H"
FROM compound_atoms atom1
JOIN compound_bond_atoms cab1 ON (atom1.compound_id = cab1.compound_id AND atom1.atom_number = cab1.atom_number)
JOIN compound_bonds bond1 ON ( atom1.compound_id = bond1.compound_id AND cab1.bond_id = bond1.bond_id)
JOIN compound_bond_atoms cab2 ON (atom1.compound_id = cab2.compound_id AND cab2.bond_id = cab1.bond_id AND cab2.atom_number != atom1.atom_number)
JOIN compound_atoms atom2 ON (atom1.compound_id = atom2.compound_id AND cab2.atom_number = atom2.atom_number)
JOIN compound_bond_atoms cab3 ON (atom1.compound_id = cab3.compound_id AND cab3.atom_number = atom2.atom_number AND cab3.bond_id != bond1.bond_id)
JOIN compound_bonds bond2 ON (atom1.compound_id = bond2.compound_id AND bond2.bond_id = cab3.bond_id)
JOIN compound_bond_atoms cab4 ON (atom1.compound_id = cab4.compound_id AND cab4.bond_id = bond2.bond_id AND cab4.atom_number != atom2.atom_number)
JOIN compound_atoms atom3 ON (atom1.compound_id = atom3.compound_id AND cab4.atom_number = atom3.atom_number);

DROP VIEW IF EXISTS `compound_bond_links`;

CREATE VIEW `compound_bond_links` AS
 select `bond`.`compound_id` AS `compound_id`,`bond`.`bond_id` AS `bond_id`,`bond`.`bond_order` AS `bond_order`,`bond`.`stereo` AS `stereo`,`cab1`.`atom_number` AS `atom1_number`,`cab2`.`atom_number` AS `atom2_number` 
 from ((`compound_bonds` `bond` 
 join `compound_bond_atoms` `cab1` on(((`bond`.`compound_id` = `cab1`.`compound_id`) and (`bond`.`bond_id` = `cab1`.`bond_id`)))) 
 join `compound_bond_atoms` `cab2` on(((`bond`.`compound_id` = `cab2`.`compound_id`) and (`bond`.`bond_id` = `cab2`.`bond_id`) and (`cab1`.`atom_number` <> `cab2`.`atom_number`))));

DROP VIEW IF EXISTS `compound_aromatic_h`;

CREATE VIEW `compound_aromatic_h` AS
 select `atom1`.`compound_id` AS `compound_id`,sum((((((`atom1`.`attached_h` + `atom2`.`attached_h`) + `atom3`.`attached_h`) + `atom4`.`attached_h`) + `atom5`.`attached_h`) + `atom6`.`attached_h`)) AS `aromatic_h` 
 from (((((((((((`compound_atoms` `atom1` 
 join `compound_bond_links` `bond1` on(((`bond1`.`compound_id` = `atom1`.`compound_id`) and (`atom1`.`atom_number` = `bond1`.`atom1_number`)))) 
 join `compound_atoms` `atom2` on(((`atom2`.`compound_id` = `atom1`.`compound_id`) and (`atom2`.`atom_number` = `bond1`.`atom2_number`)))) 
 join `compound_bond_links` `bond2` on(((`bond2`.`compound_id` = `atom1`.`compound_id`) and (`atom2`.`atom_number` = `bond2`.`atom1_number`) and (`bond2`.`atom2_number` <> `atom1`.`atom_number`)))) 
 join `compound_atoms` `atom3` on(((`atom3`.`compound_id` = `atom1`.`compound_id`) and (`atom3`.`atom_number` = `bond2`.`atom2_number`)))) 
 join `compound_bond_links` `bond3` on(((`bond3`.`compound_id` = `atom1`.`compound_id`) and (`atom3`.`atom_number` = `bond3`.`atom1_number`) and (`bond3`.`atom2_number` <> `atom2`.`atom_number`)))) 
 join `compound_atoms` `atom4` on(((`atom4`.`compound_id` = `atom1`.`compound_id`) and (`atom4`.`atom_number` = `bond3`.`atom2_number`)))) 
 join `compound_bond_links` `bond4` on(((`bond4`.`compound_id` = `atom1`.`compound_id`) and (`atom4`.`atom_number` = `bond4`.`atom1_number`) and (`bond4`.`atom2_number` <> `atom3`.`atom_number`)))) 
 join `compound_atoms` `atom5` on(((`atom5`.`compound_id` = `atom1`.`compound_id`) and (`atom5`.`atom_number` = `bond4`.`atom2_number`)))) 
 join `compound_bond_links` `bond5` on(((`bond5`.`compound_id` = `atom1`.`compound_id`) and (`atom5`.`atom_number` = `bond5`.`atom1_number`) and (`bond5`.`atom2_number` <> `atom4`.`atom_number`)))) 
 join `compound_atoms` `atom6` on(((`atom6`.`compound_id` = `atom1`.`compound_id`) and (`atom6`.`atom_number` = `bond5`.`atom2_number`)))) 
 join `compound_bond_links` `bond6` on(((`bond6`.`compound_id` = `atom1`.`compound_id`) and (`atom6`.`atom_number` = `bond6`.`atom1_number`) and (`bond6`.`atom2_number` = `atom1`.`atom_number`)))) 
 where ((`atom1`.`atom_number` < `atom6`.`atom_number`) and (`atom1`.`atom_number` < `atom2`.`atom_number`) and (`atom2`.`atom_number` < `atom6`.`atom_number`) and ((((((`bond1`.`bond_order` + `bond2`.`bond_order`) + `bond3`.`bond_order`) + `bond4`.`bond_order`) + `bond5`.`bond_order`) + `bond6`.`bond_order`) = 9)) 
 group by `atom1`.`compound_id`;
 
DROP VIEW IF EXISTS `compound_aromatic_ring`;

CREATE VIEW `compound_aromatic_ring` AS 
 select `atom1`.`compound_id` AS `compound_id`,`atom1`.`atom_number` AS `atom1_number`,`atom1`.`element` AS `atom1_element`,`atom1`.`attached_h` AS `atom1_H`,`atom2`.`atom_number` AS `atom2_number`,`atom2`.`element` AS `atom2_element`,`atom2`.`attached_h` AS `atom2_H`,`atom3`.`atom_number` AS `atom3_number`,`atom3`.`element` AS `atom3_element`,`atom3`.`attached_h` AS `atom3_H`,`atom4`.`atom_number` AS `atom4_number`,`atom4`.`element` AS `atom4_element`,`atom4`.`attached_h` AS `atom4_H`,`atom5`.`atom_number` AS `atom5_number`,`atom5`.`element` AS `atom5_element`,`atom5`.`attached_h` AS `atom5_H`,`atom6`.`atom_number` AS `atom6_number`,`atom6`.`element` AS `atom6_element`,`atom6`.`attached_h` AS `atom6_H` 
 from (((((((((((`compound_atoms` `atom1` 
 join `compound_bond_links` `bond1` on(((`bond1`.`compound_id` = `atom1`.`compound_id`) and (`atom1`.`atom_number` = `bond1`.`atom1_number`)))) 
 join `compound_atoms` `atom2` on(((`atom2`.`compound_id` = `atom1`.`compound_id`) and (`atom2`.`atom_number` = `bond1`.`atom2_number`)))) 
 join `compound_bond_links` `bond2` on(((`bond2`.`compound_id` = `atom1`.`compound_id`) and (`atom2`.`atom_number` = `bond2`.`atom1_number`) and (`bond2`.`atom2_number` <> `atom1`.`atom_number`)))) 
 join `compound_atoms` `atom3` on(((`atom3`.`compound_id` = `atom1`.`compound_id`) and (`atom3`.`atom_number` = `bond2`.`atom2_number`)))) join `compound_bond_links` `bond3` on(((`bond3`.`compound_id` = `atom1`.`compound_id`) and (`atom3`.`atom_number` = `bond3`.`atom1_number`) and (`bond3`.`atom2_number` <> `atom2`.`atom_number`)))) 
 join `compound_atoms` `atom4` on(((`atom4`.`compound_id` = `atom1`.`compound_id`) and (`atom4`.`atom_number` = `bond3`.`atom2_number`)))) join `compound_bond_links` `bond4` on(((`bond4`.`compound_id` = `atom1`.`compound_id`) and (`atom4`.`atom_number` = `bond4`.`atom1_number`) and (`bond4`.`atom2_number` <> `atom3`.`atom_number`)))) 
 join `compound_atoms` `atom5` on(((`atom5`.`compound_id` = `atom1`.`compound_id`) and (`atom5`.`atom_number` = `bond4`.`atom2_number`)))) join `compound_bond_links` `bond5` on(((`bond5`.`compound_id` = `atom1`.`compound_id`) and (`atom5`.`atom_number` = `bond5`.`atom1_number`) and (`bond5`.`atom2_number` <> `atom4`.`atom_number`)))) 
 join `compound_atoms` `atom6` on(((`atom6`.`compound_id` = `atom1`.`compound_id`) and (`atom6`.`atom_number` = `bond5`.`atom2_number`)))) join `compound_bond_links` `bond6` on(((`bond6`.`compound_id` = `atom1`.`compound_id`) and (`atom6`.`atom_number` = `bond6`.`atom1_number`) and (`bond6`.`atom2_number` = `atom1`.`atom_number`)))) 
 where ((((((`bond1`.`bond_order` + `bond2`.`bond_order`) + `bond3`.`bond_order`) + `bond4`.`bond_order`) + `bond5`.`bond_order`) + `bond6`.`bond_order`) = 9);


DROP TABLE IF EXISTS `elements`;

CREATE TABLE `elements` (
	`element` VARCHAR(2) NOT NULL,
	`atomic_number` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
	`valence` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0'
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

INSERT INTO `elements`(`element`,`atomic_number`,`valence`) VALUES('C','6','4'),('N','7','5'),('O','8','6');

DROP VIEW IF EXISTS `compound_atom_valence`;

CREATE VIEW `compound_atom_valence` AS
SELECT atom.compound_id, atom.atom_number, atom.element, atom.charge, atom.attached_h, elements.valence, count(bond.bond_id) AS "bonds", SUM(bond.bond_order) AS "sum_order"
FROM compound_atoms atom 
JOIN elements ON (atom.element = elements.element)
JOIN compound_bond_atoms cab ON ( atom.compound_id = cab.compound_id AND atom.atom_number = cab.atom_number)
JOIN compound_bonds bond ON ( atom.compound_id = bond.compound_id AND bond.bond_id = cab.bond_id )
GROUP BY atom.compound_id, atom.atom_number;

DROP TABLE IF EXISTS `jobs`;

CREATE TABLE IF NOT EXISTS `jobs` (
  `job_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `owner` VARCHAR(32) NULL DEFAULT NULL,
  `job_type` VARCHAR(45) NULL DEFAULT NULL,
  `messages` LONGTEXT NULL DEFAULT NULL,
  `output` LONGTEXT NULL DEFAULT NULL,
  `output_type` VARCHAR(32) NULL DEFAULT NULL, 
  `progress` DECIMAL(6,5) NULL DEFAULT 0.00000,
  `startDate` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `endDate` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`job_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci

-- -----------------------------------------------------
-- function degreeSign
-- -----------------------------------------------------

DROP function IF EXISTS `degreeSign`;
SHOW WARNINGS;

DELIMITER $$

CREATE FUNCTION `degreeSign`() RETURNS CHAR(1)
RETURN CHAR(0xB0 USING utf8)$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function parseActivity
-- -----------------------------------------------------

DROP function IF EXISTS `parseActivity`;
SHOW WARNINGS;

DELIMITER $$

CREATE FUNCTION `parseActivity`(val VARCHAR(45)) RETURNS float
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

DROP function IF EXISTS `lonDMS`;
SHOW WARNINGS;

DELIMITER $$


CREATE FUNCTION `lonDMS`(c_value FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN CONCAT( IF( c_value < 0, 'W ', 'E '), DMS(c_value, prec))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function lonDM
-- -----------------------------------------------------

DROP function IF EXISTS `lonDM`;
SHOW WARNINGS;

DELIMITER $$


CREATE FUNCTION `lonDM`(c_value FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN CONCAT( IF( c_value < 0, 'W ', 'E '), DM(c_value, prec))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function lonD
-- -----------------------------------------------------

DROP function IF EXISTS `lonD`;
SHOW WARNINGS;

DELIMITER $$

CREATE FUNCTION `lonD`(lon FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN IF ( lon < 0, CONCAT('W ', ABS(ROUND(lon,prec)), degreeSign()), CONCAT('E ', ROUND(lon,prec), degreeSign()))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function locationAlpha
-- -----------------------------------------------------

DROP function IF EXISTS `locationAlpha`;
SHOW WARNINGS;

DELIMITER $$

CREATE FUNCTION `locationAlpha`(x_val INT(10), y_val INT(10)) RETURNS varchar(3) CHARSET latin1
RETURN CONCAT(CHAR(64 + x_val), y_val)$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function latDMS
-- -----------------------------------------------------

DROP function IF EXISTS `latDMS`;
SHOW WARNINGS;

USE `cyanos`$$


CREATE FUNCTION `latDMS`(c_value FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN CONCAT( IF ( c_value < 0, 'S ', 'N '), DMS(c_value, prec))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function latDM
-- -----------------------------------------------------

DROP function IF EXISTS `latDM`;
SHOW WARNINGS;

DELIMITER $$


CREATE FUNCTION `latDM`(c_value FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN CONCAT( IF ( c_value < 0, 'S ', 'N '), DM(c_value, prec))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function latD
-- -----------------------------------------------------

DROP function IF EXISTS `latD`;
SHOW WARNINGS;

DELIMITER $$


CREATE FUNCTION `latD`(lat FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN IF ( lat < 0, CONCAT('S ', ABS(ROUND(lat,prec)), degreeSign()), CONCAT('N ', ROUND(lat,prec), degreeSign()))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function isActive
-- -----------------------------------------------------

DROP function IF EXISTS `isActive`;
SHOW WARNINGS;

DELIMITER $$


CREATE FUNCTION `isActive`(val VARCHAR(45), act_level FLOAT, act_op VARCHAR(2)) RETURNS tinyint(1)
RETURN IF( CHAR_LENGTH(val) < 1, 0, active( parseActivity(val), act_level, act_op ))$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function active
-- -----------------------------------------------------

DROP function IF EXISTS `active`;
SHOW WARNINGS;

DELIMITER $$


CREATE FUNCTION `active`(val FLOAT, act_level FLOAT, act_op VARCHAR(2)) RETURNS tinyint(1)
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

DROP function IF EXISTS `DMS`;
SHOW WARNINGS;

DELIMITER $$

CREATE FUNCTION `DMS`(c_value FLOAT) RETURNS varchar(32) CHARSET utf8
RETURN CONCAT( ABS(TRUNCATE(c_value,0)), degreeSign(), ' ', 
	ABS(TRUNCATE((c_value * 60) % 60,0)), "\' ", 
	ABS(ROUND((c_value * 3600) % 3600,0)), "\"")$$

DELIMITER ;
SHOW WARNINGS;

-- -----------------------------------------------------
-- function DM
-- -----------------------------------------------------

DROP function IF EXISTS `DM`;
SHOW WARNINGS;

DELIMITER $$

CREATE FUNCTION `DM`(c_value FLOAT, prec INT(10)) RETURNS varchar(32) CHARSET utf8
RETURN CONCAT( ABS(TRUNCATE(c_value,0)), degreeSign(), ' ', 
	ABS(ROUND((c_value * 60) % 60, ABS(ROUND(LOG10(1825 / prec))) )), "\'")$$

DELIMITER ;
SHOW WARNINGS;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `config`
-- -----------------------------------------------------
START TRANSACTION;

INSERT INTO `config` (`element`, `param`, `param_key`, `value`) VALUES ('database', 'version', 'protected', '2');

COMMIT;
