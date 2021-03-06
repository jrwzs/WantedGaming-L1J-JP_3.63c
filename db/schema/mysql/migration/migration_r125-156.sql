ALTER TABLE `armor` CHANGE COLUMN `invgfx` `inv_gfx` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `armor` CHANGE COLUMN `grdgfx` `grd_gfx` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `armor` CHANGE COLUMN `itemdesc_id` `item_desc_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `character_teleport` CHANGE COLUMN `locx` `loc_x` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `character_teleport` CHANGE COLUMN `locy` `loc_y` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `character_teleport` CHANGE COLUMN `mapid` `map_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `characters` CHANGE COLUMN `locx` `loc_x` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `characters` CHANGE COLUMN `locy` `loc_y` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `etcitem` CHANGE COLUMN `invgfx` `inv_gfx` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `etcitem` CHANGE COLUMN `grdgfx` `grd_gfx` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `etcitem` CHANGE COLUMN `itemdesc_id` `item_desc_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `etcitem` CHANGE COLUMN `locx` `loc_x` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `etcitem` CHANGE COLUMN `locy` `loc_y` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `getback_restart` CHANGE COLUMN `locx` `loc_x` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `getback_restart` CHANGE COLUMN `locy` `loc_y` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `inn` CHANGE COLUMN `npcid` `npc_id` int(10) unsigned NOT NULL;
ALTER TABLE `log_accelerator` CHANGE COLUMN `locx` `loc_x` int(10) NOT NULL;
ALTER TABLE `log_accelerator` CHANGE COLUMN `locy` `loc_y` int(10) NOT NULL;
ALTER TABLE `log_accelerator` CHANGE COLUMN `mapid` `map_id` int(10) NOT NULL;
ALTER TABLE `log_chat` CHANGE COLUMN `locx` `loc_x` int(10) NOT NULL;
ALTER TABLE `log_chat` CHANGE COLUMN `locy` `loc_y` int(10) NOT NULL;
ALTER TABLE `log_chat` CHANGE COLUMN `mapid` `map_id` int(10) NOT NULL;
ALTER TABLE `log_chat` CHANGE COLUMN `target_locx` `target_loc_x` int(10) NOT NULL;
ALTER TABLE `log_chat` CHANGE COLUMN `target_locy` `target_loc_y` int(10) NOT NULL;
ALTER TABLE `log_chat` CHANGE COLUMN `target_mapid` `target_map_id` int(10) NOT NULL;
ALTER TABLE `log_enchant` CHANGE COLUMN `old_enchantlvl` `old_enchant_level` int(3) NOT NULL DEFAULT '0';
ALTER TABLE `log_enchant` CHANGE COLUMN `new_enchantlvl` `new_enchant_level` int(3) DEFAULT '0';
ALTER TABLE `mobskill` CHANGE COLUMN `gfxid` `gfx_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `mobskill` CHANGE COLUMN `actid` `act_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `npcid` `npc_id` int(10) unsigned NOT NULL AUTO_INCREMENT;
ALTER TABLE `npc` CHANGE COLUMN `nameid` `name_id` varchar(45) NOT NULL DEFAULT '';
ALTER TABLE `npc` CHANGE COLUMN `gfxid` `gfx_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `lvl` `level` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `intel` `int` int(10) NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `weakAttr` `weak_attr` int(10) NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `passispeed` `move_speed` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `atkspeed` `atk_speed` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `agrososc` `agro_sosc` tinyint(1) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `agrocoi` `agro_coi` tinyint(1) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `agrofamily` `agro_family` int(1) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `agrogfxid1` `agro_gfx_id1` int(10) NOT NULL DEFAULT '-1';
ALTER TABLE `npc` CHANGE COLUMN `agrogfxid2` `agro_gfx_id2` int(10) NOT NULL DEFAULT '-1';
ALTER TABLE `npc` CHANGE COLUMN `picupitem` `pickup_item` tinyint(1) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `digestitem` `digest_item` int(1) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `bravespeed` `brave_speed` tinyint(1) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `hprinterval` `hpr_interval` int(6) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `mprinterval` `mpr_interval` int(6) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `randomlevel` `random_level` int(3) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `randomhp` `random_hp` int(5) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `randommp` `random_mp` int(5) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `randomac` `random_ac` int(3) NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `randomexp` `random_exp` int(5) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `randomlawful` `random_lawful` int(5) NOT NULL DEFAULT '0';
ALTER TABLE `npc` MODIFY COLUMN `damage_reduction` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `enableTU` `enable_tu` tinyint(1) NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `enableErase` `enable_erase` tinyint(1) NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `bow_actid` `bow_act_id` int(5) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npc` CHANGE COLUMN `transform_gfxid` `transform_gfx_id` int(10) NOT NULL DEFAULT '0';
ALTER TABLE `npcaction` CHANGE COLUMN `npcid` `npc_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `npcaction` CHANGE COLUMN `caotic_action` `chaotic_action` varchar(45) NOT NULL DEFAULT '';
ALTER TABLE `petitem` CHANGE COLUMN `hitmodifier` `hit_modifier` int(3) NOT NULL DEFAULT '0';
ALTER TABLE `petitem` CHANGE COLUMN `dmgmodifier` `dmg_modifier` int(3) NOT NULL DEFAULT '0';
ALTER TABLE `petitem` CHANGE COLUMN `add_str` `str` int(2) NOT NULL DEFAULT '0';
ALTER TABLE `petitem` CHANGE COLUMN `add_con` `con` int(2) NOT NULL DEFAULT '0';
ALTER TABLE `petitem` CHANGE COLUMN `add_dex` `dex` int(2) NOT NULL DEFAULT '0';
ALTER TABLE `petitem` CHANGE COLUMN `add_int` `int` int(2) NOT NULL DEFAULT '0';
ALTER TABLE `petitem` CHANGE COLUMN `add_wis` `wis` int(2) NOT NULL DEFAULT '0';
ALTER TABLE `petitem` CHANGE COLUMN `add_hp` `hp` int(10) NOT NULL DEFAULT '0';
ALTER TABLE `petitem` CHANGE COLUMN `add_mp` `mp` int(10) NOT NULL DEFAULT '0';
ALTER TABLE `petitem` CHANGE COLUMN `add_sp` `sp` int(10) NOT NULL DEFAULT '0';
ALTER TABLE `petitem` CHANGE COLUMN `m_def` `mr` int(2) NOT NULL DEFAULT '0';
ALTER TABLE `petitem` MODIFY COLUMN `use_type` int(2) NOT NULL DEFAULT '0';
ALTER TABLE `pets` CHANGE COLUMN `objid` `obj_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `pets` CHANGE COLUMN `npcid` `npc_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `pets` CHANGE COLUMN `lvl` `level` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `pets` MODIFY COLUMN `food` int(2) NOT NULL DEFAULT '0';
ALTER TABLE `pettypes` CHANGE COLUMN `base_npcid` `base_npc_id` int(10) NOT NULL;
ALTER TABLE `pettypes` CHANGE COLUMN `tame_itemid` `tame_item_id` int(10) NOT NULL;
ALTER TABLE `pettypes` CHANGE COLUMN `transform_itemid` `transform_item_id` int(10) NOT NULL;
ALTER TABLE `pettypes` CHANGE COLUMN `transform_npcid` `transform_npc_id` int(10) NOT NULL;
ALTER TABLE `skills` CHANGE COLUMN `consume_itemid` `consume_item_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `skills` CHANGE COLUMN `nameid` `name_id` varchar(45) NOT NULL DEFAULT '';
ALTER TABLE `skills` CHANGE COLUMN `castgfx` `cast_gfx` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `skills` CHANGE COLUMN `castgfx2` `cast_gfx2` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `skills` CHANGE COLUMN `sysmsgid_happen` `sys_msg_id_happen` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `skills` CHANGE COLUMN `sysmsgid_stop` `sys_msg_id_stop` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `skills` CHANGE COLUMN `sysmsgid_fail` `sys_msg_id_fail` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist` CHANGE COLUMN `npc_templateid` `npc_template_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist` CHANGE COLUMN `locx` `loc_x` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist` CHANGE COLUMN `locy` `loc_y` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist` CHANGE COLUMN `randomx` `random_x` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist` CHANGE COLUMN `randomy` `random_y` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist` CHANGE COLUMN `locx1` `loc_x1` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist` CHANGE COLUMN `locy1` `loc_y1` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist` CHANGE COLUMN `locx2` `loc_x2` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist` CHANGE COLUMN `locy2` `loc_y2` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist` CHANGE COLUMN `mapid` `map_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_boss` CHANGE COLUMN `locx` `loc_x` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_boss` CHANGE COLUMN `locy` `loc_y` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_boss` CHANGE COLUMN `randomx` `random_x` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_boss` CHANGE COLUMN `randomy` `random_y` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_boss` CHANGE COLUMN `locx1` `loc_x1` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_boss` CHANGE COLUMN `locy1` `loc_y1` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_boss` CHANGE COLUMN `locx2` `loc_x2` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_boss` CHANGE COLUMN `locy2` `loc_y2` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_boss` CHANGE COLUMN `mapid` `map_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_door` CHANGE COLUMN `gfxid` `gfx_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_door` CHANGE COLUMN `locx` `loc_x` int(10) NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_door` CHANGE COLUMN `locy` `loc_y` int(10) NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_door` CHANGE COLUMN `mapid` `map_id` int(10) NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_door` CHANGE COLUMN `DoorOpen` `is_open` tinyint(1) NOT NULL DEFAULT '0' COMMENT '????????????????????????\r\n1????????????\r\n0????????????';
ALTER TABLE `spawnlist_furniture` CHANGE COLUMN `npcid` `npc_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_furniture` CHANGE COLUMN `locx` `loc_x` int(10) NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_furniture` CHANGE COLUMN `locy` `loc_y` int(10) NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_furniture` CHANGE COLUMN `mapid` `map_id` int(10) NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_light` CHANGE COLUMN `npcid` `npc_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_light` CHANGE COLUMN `locx` `loc_x` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_light` CHANGE COLUMN `locy` `loc_y` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_light` CHANGE COLUMN `mapid` `map_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_npc` CHANGE COLUMN `npc_templateid` `npc_template_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_npc` CHANGE COLUMN `locx` `loc_x` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_npc` CHANGE COLUMN `locy` `loc_y` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_npc` CHANGE COLUMN `randomx` `random_x` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_npc` CHANGE COLUMN `randomy` `random_y` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_npc` CHANGE COLUMN `mapid` `map_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spawnlist_trap` CHANGE COLUMN `mapid` `map_id` int(10) NOT NULL;
ALTER TABLE `spawnlist_trap` CHANGE COLUMN `locx` `loc_x` int(10) NOT NULL;
ALTER TABLE `spawnlist_trap` CHANGE COLUMN `locy` `loc_y` int(10) NOT NULL;
ALTER TABLE `spawnlist_ub` CHANGE COLUMN `npc_templateid` `npc_template_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spr_action` CHANGE COLUMN `framecount` `frame_count` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `spr_action` CHANGE COLUMN `framerate` `frame_rate` int(10) unsigned NOT NULL DEFAULT '24';
ALTER TABLE `trap` CHANGE COLUMN `gfxid` `gfx_id` int(10) NOT NULL;
ALTER TABLE `ub_settings` CHANGE COLUMN `ub_mapid` `ub_map_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `ub_settings` CHANGE COLUMN `min_lvl` `min_level` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `ub_settings` CHANGE COLUMN `max_lvl` `max_level` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `weapon` CHANGE COLUMN `invgfx` `inv_gfx` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `weapon` CHANGE COLUMN `grdgfx` `grd_gfx` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `weapon` CHANGE COLUMN `itemdesc_id` `item_desc_id` int(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `weapon_skill` MODIFY COLUMN `arrow_type` tinyint(1) unsigned NOT NULL DEFAULT '0';
ALTER TABLE `weapon_skill` CHANGE COLUMN `isMr` `enable_mr` tinyint(1) unsigned NOT NULL DEFAULT '1';
ALTER TABLE `weapon_skill` CHANGE COLUMN `isAttrMr` `enable_attr_mr` tinyint(1) unsigned NOT NULL DEFAULT '1';
