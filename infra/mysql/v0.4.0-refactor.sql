/**
 * v0.4.0 schema rename migration.
 * Moves the existing application tables into para_bbs without changing
 * database users, credentials, or row data.
 *
 * Run this script only after backing up the existing schema.
 */

CREATE DATABASE IF NOT EXISTS `para_bbs`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

RENAME TABLE
    `faramita_bbs`.`sys_user` TO `para_bbs`.`sys_user`,
    `faramita_bbs`.`sys_role` TO `para_bbs`.`sys_role`,
    `faramita_bbs`.`sys_perm` TO `para_bbs`.`sys_perm`,
    `faramita_bbs`.`sys_user_role` TO `para_bbs`.`sys_user_role`,
    `faramita_bbs`.`sys_role_perm` TO `para_bbs`.`sys_role_perm`,
    `faramita_bbs`.`blog` TO `para_bbs`.`blog`,
    `faramita_bbs`.`folder` TO `para_bbs`.`folder`,
    `faramita_bbs`.`tag` TO `para_bbs`.`tag`,
    `faramita_bbs`.`blog_tag` TO `para_bbs`.`blog_tag`,
    `faramita_bbs`.`like_blog` TO `para_bbs`.`like_blog`,
    `faramita_bbs`.`avatar_info` TO `para_bbs`.`avatar_info`;

DROP DATABASE `faramita_bbs`;

USE `para_bbs`;
