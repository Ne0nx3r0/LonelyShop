CREATE TABLE IF NOT EXISTS `mydb`.`player_account` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `uuid` VARCHAR(36) NOT NULL,
  `username` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uuid_UNIQUE` (`uuid` ASC))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `mydb`.`items` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `player_account` INT UNSIGNED NOT NULL,
  `material` INT UNSIGNED NOT NULL,
  `data` INT UNSIGNED NOT NULL,
  `amount` INT UNSIGNED NOT NULL,
  `item_data` TEXT NOT NULL,
  `posted` DATETIME NOT NULL,
  `price` DECIMAL(13,2) UNSIGNED NOT NULL,
  `price_per_item` DECIMAL(13,2) UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_items_player_account_idx` (`player_account` ASC),
  INDEX `material` (`material` ASC),
  CONSTRAINT `fk_items_player_account`
    FOREIGN KEY (`player_account`)
    REFERENCES `mydb`.`player_account` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;