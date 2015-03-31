SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `food_pantry_manager` DEFAULT CHARACTER SET latin1 ;
USE `food_pantry_manager` ;

-- -----------------------------------------------------
-- Table `food_pantry_manager`.`client`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `food_pantry_manager`.`client` (
  `client_id` INT(5) NOT NULL AUTO_INCREMENT ,
  `first_name` VARCHAR(45) NOT NULL ,
  `last_name` VARCHAR(45) NOT NULL ,
  `ssn` VARCHAR(11) NOT NULL ,
  `address` VARCHAR(45) NOT NULL ,
  `city` VARCHAR(45) NOT NULL ,
  `gender` VARCHAR(45) NOT NULL ,
  `birthday` DATE NULL DEFAULT NULL ,
  `telephone` VARCHAR(45) NULL DEFAULT NULL ,
  `notes` TINYTEXT NULL DEFAULT NULL ,
  `valid_as_of` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY (`client_id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `food_pantry_manager`.`appointment`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `food_pantry_manager`.`appointment` (
  `appointment_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `client_id` INT(11) NULL DEFAULT NULL ,
  `date` DATETIME NOT NULL ,
  `pounds` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`appointment_id`) ,
  INDEX `ClientID` (`client_id` ASC) ,
  INDEX `ClientIDCheck_fromAppointment` (`client_id` ASC) ,
  CONSTRAINT `ClientIDCheck_fromAppointment`
    FOREIGN KEY (`client_id` )
    REFERENCES `food_pantry_manager`.`client` (`client_id` )
    ON DELETE SET NULL
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `food_pantry_manager`.`household`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `food_pantry_manager`.`household` (
  `household_member_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `client_id` INT(11) NOT NULL ,
  `name` VARCHAR(45) NOT NULL ,
  `birthday` DATE NULL DEFAULT NULL ,
  `gender` VARCHAR(45) NOT NULL ,
  `relationship` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`household_member_id`) ,
  INDEX `ClientID` (`client_id` ASC) ,
  CONSTRAINT `ClientIDCheck_fromHousehold`
    FOREIGN KEY (`client_id` )
    REFERENCES `food_pantry_manager`.`client` (`client_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = latin1;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
