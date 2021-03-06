/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jp.l1j.server.packets.client;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.l1j.configure.Config;
import static jp.l1j.locale.I18N.*;
import jp.l1j.server.ClientThread;
import jp.l1j.server.codes.ActionCodes;
import jp.l1j.server.controller.timer.WarTimeController;
import jp.l1j.server.datatables.CharBuffTable;
import jp.l1j.server.datatables.CharacterTable;
import jp.l1j.server.datatables.ClanRecommendTable;
import jp.l1j.server.datatables.RestartLocationTable;
import jp.l1j.server.datatables.ReturnLocationTable;
import jp.l1j.server.datatables.SkillTable;
import jp.l1j.server.model.L1CastleLocation;
import jp.l1j.server.model.L1Clan;
import jp.l1j.server.model.L1Cube;
import jp.l1j.server.model.L1CurseParalysis;
import jp.l1j.server.model.L1PolyMorph;
import jp.l1j.server.model.L1War;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.instance.L1SummonInstance;
import jp.l1j.server.model.item.executor.L1ExtraPotion;
import jp.l1j.server.model.item.executor.L1FloraPotion;
import jp.l1j.server.model.poison.L1DamagePoison;
import jp.l1j.server.model.poison.L1ParalysisPoison;
import jp.l1j.server.model.poison.L1SilencePoison;
import jp.l1j.server.model.skill.L1BuffUtil;
import static jp.l1j.server.model.skill.L1SkillId.*;
import jp.l1j.server.model.skill.L1SkillUse;
import jp.l1j.server.model.skill.executor.L1BuffSkillExecutor;
import jp.l1j.server.packets.server.S_ActiveSpells;
import jp.l1j.server.packets.server.S_AddSkill;
import jp.l1j.server.packets.server.S_BonusStats;
import jp.l1j.server.packets.server.S_BookmarkLoad;
import jp.l1j.server.packets.server.S_Bookmarks;
import jp.l1j.server.packets.server.S_CharTitle;
import jp.l1j.server.packets.server.S_CharacterConfig;
import jp.l1j.server.packets.server.S_Dexup;
import jp.l1j.server.packets.server.S_HpUpdate;
import jp.l1j.server.packets.server.S_InitialAbilityGrowth;
import jp.l1j.server.packets.server.S_InvList;
import jp.l1j.server.packets.server.S_Karma;
import jp.l1j.server.packets.server.S_Liquor;
import jp.l1j.server.packets.server.S_LoginGame;
import jp.l1j.server.packets.server.S_Mail;
import jp.l1j.server.packets.server.S_MapID;
import jp.l1j.server.packets.server.S_MpUpdate;
import jp.l1j.server.packets.server.S_OwnCharAttrDef;
import jp.l1j.server.packets.server.S_OwnCharPack;
import jp.l1j.server.packets.server.S_OwnCharStatus;
import jp.l1j.server.packets.server.S_PacketBox;
import jp.l1j.server.packets.server.S_Paralysis;
import jp.l1j.server.packets.server.S_PlayTime;
import jp.l1j.server.packets.server.S_RuneSlot;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.packets.server.S_SkillBrave;
import jp.l1j.server.packets.server.S_SkillHaste;
import jp.l1j.server.packets.server.S_SkillIconAura;
import jp.l1j.server.packets.server.S_SkillIconBlessOfEva;
import jp.l1j.server.packets.server.S_SkillIconGFX;
import jp.l1j.server.packets.server.S_SkillIconThirdSpeed;
import jp.l1j.server.packets.server.S_SkillIconWisdomPotion;
import jp.l1j.server.packets.server.S_SpMr;
import jp.l1j.server.packets.server.S_Strup;
import jp.l1j.server.packets.server.S_SummonPack;
import jp.l1j.server.packets.server.S_War;
import jp.l1j.server.packets.server.S_Weather;
import jp.l1j.server.templates.L1BookMark;
import jp.l1j.server.templates.L1CharacterBuff;
import jp.l1j.server.templates.L1CharacterSkill;
import jp.l1j.server.templates.L1GetBackRestart;
import jp.l1j.server.templates.L1Skill;
import jp.l1j.server.utils.L1DatabaseFactory;
import jp.l1j.server.utils.SqlUtil;

public class C_LoginToServer extends ClientBasePacket {
	private static final String C_LOGIN_TO_SERVER = "[C] C_LoginToServer";

	private static Logger _log = Logger.getLogger(C_LoginToServer.class.getName());

	public C_LoginToServer(byte abyte0[], ClientThread client)
			throws FileNotFoundException, Exception {
		super(abyte0);
		String login = client.getAccountName();
		String charName = readS();
		if (client.getActiveChar() != null) {
			_log.info(String.format(I18N_MULTIPLE_LOGINS_DETECTED, client.getHostname()));
			// ??????????????????????????????????????????%s ??????????????????????????????
			client.close();
			return;
		}
		L1PcInstance pc = L1PcInstance.load(charName);
		if (pc == null || !login.equals(pc.getAccountName())) {
			_log.info(String.format(I18N_LOGIN_REQUEST_IS_INVALID,
					charName, login, client.getHostname()));
			// ?????????????????????????????????????????????char=%s account=%s host=%s
			client.close();
			return;
		}
		if (Config.LEVEL_DOWN_RANGE != 0) {
			if (pc.getHighLevel() - pc.getLevel() >= Config.LEVEL_DOWN_RANGE) {
				_log.info(String.format(I18N_BEYOND_THE_TOLERANCE_OF_THE_LEVEL_DOWN,
						charName, login, client.getHostname()));
				// ?????????????????????????????????????????????????????????char=%s account=% host=%
				client.kick();
				return;
			}
		}
		_log.info(String.format(I18N_LOGGED_CHARACTER, charName, login, client.getHostname()));
		// ??????????????????????????????: char=%s account=% host=%
		int currentHpAtLoad = pc.getCurrentHp();
		int currentMpAtLoad = pc.getCurrentMp();
		pc.clearSkillMastery();
		pc.setOnlineStatus(1);
		CharacterTable.updateOnlineStatus(pc);
		L1World.getInstance().storeObject(pc);
		pc.setNetConnection(client);
		pc.setPacketOutput(client);
		client.setActiveChar(pc);
		/** ????????????????????????????????? */
		S_InitialAbilityGrowth AbilityGrowth = new S_InitialAbilityGrowth(pc);
		pc.sendPackets(AbilityGrowth);
		// 3.0c
		/*
		 * S_Unknown1 s_unknown1 = new S_Unknown1(); pc.sendPackets(s_unknown1);
		 * // // S_Unknown2 s_unknown2 = new S_Unknown2();
		 * pc.sendPackets(s_unknown2);
		 */
		pc.sendPackets(new S_LoginGame()); // 3.3c
		pc.sendPackets(new S_Karma(pc)); // ?????????????????????
		// ??????????????????????????????
		if (pc.getMapId() == 5143) {
			pc.setMiniGamePlaying(1);
		} else {
			pc.setMiniGamePlaying(0);
		}
		// ?????????????????????getback_restart??????????????????????????????????????????????????????
		RestartLocationTable gbrTable = RestartLocationTable.getInstance();
		L1GetBackRestart[] gbrList = gbrTable.getGetBackRestartTableList();
		for (L1GetBackRestart gbr : gbrList) {
			if (pc.getMapId() == gbr.getArea()) {
				pc.setX(gbr.getLocX());
				pc.setY(gbr.getLocY());
				pc.setMap(gbr.getMapId());
				break;
			}
		}
		// altsettings.properties???GetBack???true???????????????????????????
		if (Config.GET_BACK) {
			int[] loc = ReturnLocationTable.getReturnLocation(pc, true);
			pc.setX(loc[0]);
			pc.setY(loc[1]);
			pc.setMap((short) loc[2]);
		}
		// ????????????????????????????????????????????????????????????????????????????????????
		int castle_id = L1CastleLocation.getCastleIdByArea(pc);
		if (0 < castle_id) {
			if (WarTimeController.getInstance().isNowWar(castle_id)) {
				L1Clan clan = L1World.getInstance().getClan(pc.getClanName());
				if (clan != null) {
					if (clan.getCastleId() != castle_id) {
						// ???????????????????????????
						int[] loc = new int[3];
						loc = L1CastleLocation.getGetBackLoc(castle_id);
						pc.setX(loc[0]);
						pc.setY(loc[1]);
						pc.setMap((short) loc[2]);
					}
				} else {
					// ????????????????????????????????????????????????
					int[] loc = new int[3];
					loc = L1CastleLocation.getGetBackLoc(castle_id);
					pc.setX(loc[0]);
					pc.setY(loc[1]);
					pc.setMap((short) loc[2]);
				}
			}
		}
		L1World.getInstance().addVisibleObject(pc);
		pc.sendPackets(new S_Mail(pc , 0));
		pc.sendPackets(new S_Mail(pc , 1));
		pc.sendPackets(new S_Mail(pc , 2));
		pc.beginGameTimeCarrier();
		pc.sendPackets(new S_RuneSlot(S_RuneSlot.RUNE_CLOSE_SLOT, 3));
		pc.sendPackets(new S_RuneSlot(S_RuneSlot.RUNE_OPEN_SLOT, 1));
		pc.sendPackets(new S_OwnCharStatus(pc));
		pc.sendPackets(new S_MapID(pc.getMap().getBaseMapId(), pc.getMap().isUnderwater()));
		pc.sendPackets(new S_OwnCharPack(pc));
		pc.sendPackets(new S_SpMr(pc));
		// XXX ?????????????????????S_OwnCharPack?????????????????????????????????
		S_CharTitle s_charTitle = new S_CharTitle(pc.getId(), pc.getTitle());
		pc.sendPackets(s_charTitle);
		pc.broadcastPacket(s_charTitle);
		pc.sendVisualEffectAtLogin(); // ??????????????????????????????????????????????????????
		pc.sendPackets(new S_Weather(L1World.getInstance().getWeather()));
		items(pc);
		pc.setEquipped(pc, true);//TODO 3.63????????????
		pc.sendPackets(new S_BookmarkLoad(pc));
		skills(pc);
		buff(client, pc);
		buffBlessOfAin(pc); // ??????????????????????????????
		pc.setServivalScream(); // TODO ???????????????
		pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_PLUS, pc.getDodge())); // ?????????????????? ???
		pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_MINUS, pc.getNdodge())); // ?????????????????? ???
		checkPledgeRecommendation(pc);
		pc.sendPackets(new S_ActiveSpells(pc));
		if (pc.getCurrentHp() > 0) {
			pc.setDead(false);
			pc.setStatus(0);
		} else {
			pc.setDead(true);
			pc.setStatus(ActionCodes.ACTION_Die);
		}
		if (pc.getLevel() >= 51 && pc.getLevel() - 50 > pc.getBonusStats()) {
			if ((pc.getBaseStr() + pc.getBaseDex() + pc.getBaseCon()
					+ pc.getBaseInt() + pc.getBaseWis() + pc.getBaseCha()) < 210) {
				pc.sendPackets(new S_BonusStats(pc.getId(), 1));
			}
		}
		if (Config.CHARACTER_CONFIG_IN_SERVER_SIDE) {
			pc.sendPackets(new S_CharacterConfig(pc.getId()));
		}
		serchSummon(pc);
		WarTimeController.getInstance().checkCastleWar(pc);
		if (pc.getClanId() != 0) { // ??????????????????
			L1Clan clan = L1World.getInstance().getClan(pc.getClanName());
			if (clan != null) {
				if (pc.getClanId() == clan.getClanId() && // ????????????????????????????????????????????????????????????????????????????????????
						pc.getClanName().toLowerCase().equals(clan.getClanName().toLowerCase())) {
					L1PcInstance[] clanMembers = clan.getOnlineClanMember();
					for (L1PcInstance clanMember : clanMembers) {
						if (clanMember.getId() != pc.getId()) {
							clanMember.sendPackets(new S_ServerMessage(843, pc.getName()));
							// ?????????????????????%0%s????????????????????????????????????
						}
					}
					// ???????????????????????????
					for (L1War war : L1World.getInstance().getWarList()) {
						boolean ret = war.CheckClanInWar(pc.getClanName());
						if (ret) { // ??????????????????
							String enemy_clan_name = war.GetEnemyClanName(pc.getClanName());
							if (enemy_clan_name != null) {
								// ???????????????????????????_???????????????????????????
								pc.sendPackets(new S_War(8, pc.getClanName(), enemy_clan_name));
							}
							break;
						}
					}
				} else {
					pc.setClanid(0);
					pc.setClanname("");
					pc.setClanRank(0);
					pc.save(); // DB??????????????????????????????????????????
				}
			}
		}
		if (pc.getPartnerId() != 0) { // ?????????
			L1PcInstance partner = (L1PcInstance) L1World.getInstance().findObject(pc.getPartnerId());
			if (partner != null && partner.getPartnerId() != 0) {
				if (pc.getPartnerId() == partner.getId()
						&& partner.getPartnerId() == pc.getId()) {
					pc.sendPackets(new S_ServerMessage(548)); // ??????????????????????????????????????????????????????
					partner.sendPackets(new S_ServerMessage(549)); // ?????????????????????????????????????????????????????????????????????
				}
			}
		}
		if (currentHpAtLoad > pc.getCurrentHp()) {
			pc.setCurrentHp(currentHpAtLoad);
		}
		if (currentMpAtLoad > pc.getCurrentMp()) {
			pc.setCurrentMp(currentMpAtLoad);
		}
		pc.startHpRegeneration();
		pc.startMpRegeneration();
		pc.startObjectAutoUpdate();
		client.CharReStart(false);
		pc.beginExpMonitor();
		pc.save(); // DB??????????????????????????????????????????
		pc.sendPackets(new S_OwnCharStatus(pc));
		if (pc.getHellTime() > 0) {
			pc.beginHell(false);
		}
		pc.startExpirationTimer(); // ??????????????????????????????????????????????????????
		pc.startMapLimiter(); // ?????????????????????????????????
		pc.sendPackets(new S_PlayTime()); // ???????????????????????????
	}

	private void items(L1PcInstance pc) {
		// DB???????????????????????????????????????????????????????????????
		CharacterTable.getInstance().restoreInventory(pc);

		pc.sendPackets(new S_InvList(pc.getInventory().getItems()));
	}

	private void bookmarks(L1PcInstance pc) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM character_bookmarks WHERE char_id=? ORDER BY name ASC");
			pstm.setInt(1, pc.getId());
			rs = pstm.executeQuery();
			while (rs.next()) {
				L1BookMark bookmark = new L1BookMark();
				bookmark.setId(rs.getInt("id"));
				bookmark.setCharId(rs.getInt("char_id"));
				bookmark.setName(rs.getString("name"));
				bookmark.setLocX(rs.getInt("loc_x"));
				bookmark.setLocY(rs.getInt("loc_y"));
				bookmark.setMapId(rs.getShort("map_id"));
				S_Bookmarks s_bookmarks = new S_Bookmarks(bookmark.getName(),
						bookmark.getMapId(), bookmark.getId(),
						bookmark.getLocX(), bookmark.getLocY());
				pc.addBookMark(bookmark);
				pc.sendPackets(s_bookmarks);
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SqlUtil.close(rs);
			SqlUtil.close(pstm);
			SqlUtil.close(con);
		}
	}

	private void skills(L1PcInstance pc) {
		List<L1CharacterSkill> skills = L1CharacterSkill.findByCharcterId(pc.getId());
		for (L1CharacterSkill skill : skills) {
			pc.setSkillMastery(skill.getSkillId());
		}
		pc.sendPackets(new S_AddSkill(skills));
	}

	private void serchSummon(L1PcInstance pc) {
		for (L1SummonInstance summon : L1World.getInstance().getAllSummons()) {
			if (summon.getMaster().getId() == pc.getId()) {
				summon.setMaster(pc);
				pc.addPet(summon);
				for (L1PcInstance visiblePc : L1World.getInstance().getVisiblePlayer(summon)) {
					visiblePc.sendPackets(new S_SummonPack(summon, visiblePc));
				}
			}
		}
	}

	private boolean buffByExecutor(L1PcInstance pc, L1CharacterBuff buff) {
		L1Skill skill = SkillTable.getInstance().findBySkillId(buff.getSkillId());
		if (skill == null) {
			return false;
		}
		L1BuffSkillExecutor exe = skill.newBuffSkillExecutor();
		if (exe == null) {
			return false;
		}
		pc.setSkillEffect(buff.getSkillId(), buff.getRemainingTime() * 1000);
		exe.restoreEffect(pc, buff);
		return true;
	}

	private void buff(ClientThread clientthread, L1PcInstance pc) {
		for (L1CharacterBuff buff : CharBuffTable.findByCharacterId(pc.getId())) {
			int skillId = buff.getSkillId();
			int remainingTime = buff.getRemainingTime();
			long logoutTime = pc.getLogoutTime().getTime();
			long loginTime = new Timestamp(System.currentTimeMillis()).getTime();
			int differenceTime = (int)(loginTime - logoutTime) / 1000;
			if (buffByExecutor(pc, buff)) {
				continue;
			}
			if (skillId == SHAPE_CHANGE) { // ??????
				if (pc.getMiniGamePlaying() == 0) {
					L1PolyMorph.doPoly(pc, buff.getPolyId(), remainingTime,
							L1PolyMorph.MORPH_BY_LOGIN);
				}
			} else if (skillId == STATUS_BRAVE) { // ???????????? ??????????????????
				if (pc.getMiniGamePlaying() == 0) {
					pc.sendPackets(new S_SkillBrave(pc.getId(), 1, remainingTime));
					pc.broadcastPacket(new S_SkillBrave(pc.getId(), 1, 0));
					pc.setBraveSpeed(1);
					pc.setSkillEffect(skillId, remainingTime * 1000);
				}
			} else if (skillId == STATUS_HASTE) { // ???????????? ???????????????
				if (pc.getMiniGamePlaying() == 0) {
					pc.sendPackets(new S_SkillHaste(pc.getId(), 1, remainingTime));
					pc.broadcastPacket(new S_SkillHaste(pc.getId(), 1, 0));
					pc.setMoveSpeed(1);
					pc.setSkillEffect(skillId, remainingTime * 1000);
				} else if (pc.getMiniGamePlaying() == 1) {
					pc.setMiniGamePlaying(0);
				}
			} else if (skillId == STATUS_BLUE_POTION) { // ????????????????????????
				pc.sendPackets(new S_SkillIconGFX(34, remainingTime));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_UNDERWATER_BREATH) { // ??????????????????
				pc.sendPackets(new S_SkillIconBlessOfEva(pc.getId(), remainingTime));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_WISDOM_POTION) { // ??????????????????????????????
				pc.sendPackets(new S_SkillIconWisdomPotion(remainingTime / 4));
				pc.addSp(2);
				pc.addMpr(2);
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_CHAT_PROHIBITED) { // ??????????????????
				pc.sendPackets(new S_SkillIconGFX(36, remainingTime));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_POISON) { // ???
				L1DamagePoison.doInfection(pc, pc, 3000, 5); // 3????????????5????????????
			} else if (skillId == STATUS_POISON_SILENCE) { // ?????????
				L1SilencePoison.doInfection(pc);
			} else if (skillId == STATUS_POISON_PARALYZING) { // ?????????(?????????)
				L1ParalysisPoison.doInfection(pc, remainingTime * 1000, 16000);
			} else if (skillId == STATUS_POISON_PARALYZED) { // ?????????(?????????)
				L1ParalysisPoison.doInfection(pc, 0, remainingTime * 1000);
			} else if (skillId == STATUS_CURSE_PARALYZING) { // ????????????(?????????)
				L1CurseParalysis.curse(pc, remainingTime * 1000, 16000);
			} else if (skillId == STATUS_CURSE_PARALYZED) { // ????????????(?????????)
				L1CurseParalysis.curse(pc, 0, remainingTime * 1000);
			} else if (skillId == STATUS_FLOATING_EYE) { // ??????????????????????????????
				pc.setSkillEffect(skillId, 0);
			} else if (skillId == STATUS_HOLY_WATER) { // ??????
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_HOLY_MITHRIL_POWDER) { // ?????????????????????????????????
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_HOLY_WATER_OF_EVA) { // ????????????????????????
				pc.setSkillEffect(skillId, remainingTime * 1000);
				pc.sendPackets(new S_SkillIconAura(221, remainingTime, 5));
			} else if (skillId == STATUS_ELFBRAVE) { // ????????????????????????
				pc.sendPackets(new S_SkillBrave(pc.getId(), 3, remainingTime));
				pc.broadcastPacket(new S_SkillBrave(pc.getId(), 3, 0));
				pc.setBraveSpeed(1);
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_RIBRAVE) { // ??????????????????
				pc.setSkillEffect(skillId, remainingTime * 1000);
				pc.setBraveSpeed(1);
			} else if (skillId == STATUS_CUBE_IGNITION_TO_ALLY) { // ????????????[??????????????????]
				pc.addFire(30);
				pc.sendPackets(new S_OwnCharAttrDef(pc));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_CUBE_QUAKE_TO_ALLY) { // ????????????[????????????]
				pc.addEarth(30);
				pc.sendPackets(new S_OwnCharAttrDef(pc));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_CUBE_SHOCK_TO_ALLY) { // ????????????[????????????]
				pc.addWind(30);
				pc.sendPackets(new S_OwnCharAttrDef(pc));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_CUBE_BALANCE) { //	????????????[????????????]
				pc.setSkillEffect(skillId, remainingTime * 1000);
				L1Cube cube = new L1Cube(pc, pc, skillId);
				cube.begin();
			} else if (skillId == STATUS_THIRD_SPEED) { // ????????????
				pc.sendPackets(new S_Liquor(pc.getId(), 8));
				pc.broadcastPacket(new S_Liquor(pc.getId(), 8));
				pc.sendPackets(new S_SkillIconThirdSpeed(remainingTime / 4));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == MIRROR_IMAGE || skillId == UNCANNY_DODGE) { // ???????????????????????????????????????????????????
				pc.addDodge((byte) 5); // ?????????????????? + 50%
				pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_PLUS, pc.getDodge()));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == RESIST_FEAR) { // ????????????
				pc.addNdodge((byte) 5); // ?????????????????? - 50%
				pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_MINUS, pc.getNdodge()));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_FLORA_POTION_STR) { // ????????????????????????????????????
				L1FloraPotion potion = L1FloraPotion.get(40922);
				int str = potion.getEffect(pc).getStr();
				pc.addStr(str);
				pc.sendPackets(new S_Strup(pc, str, remainingTime));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_FLORA_POTION_DEX) { // ????????????????????????????????????
				L1FloraPotion potion = L1FloraPotion.get(40923);
				int dex = potion.getEffect(pc).getDex();
				pc.addDex(dex);
				pc.sendPackets(new S_Dexup(pc, dex, remainingTime));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_FREEZE) { // ??????
				pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_BIND, true));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_CURSE_BARLOG) {
				pc.sendPackets(new S_SkillIconAura(221, remainingTime, 2));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_CURSE_YAHEE) {
				pc.sendPackets(new S_SkillIconAura(221, remainingTime, 1));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_WEAKNESS_EXPOSURE_LV1) { // ????????????Lv1
				pc.sendPackets(new S_SkillIconGFX(75, 1));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_WEAKNESS_EXPOSURE_LV2) { // ????????????Lv2
				pc.sendPackets(new S_SkillIconGFX(75, 2));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_WEAKNESS_EXPOSURE_LV3) { // ????????????Lv3
				pc.sendPackets(new S_SkillIconGFX(75, 3));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_DESTRUCTION_NOSTRUM) { // ???????????????
				pc.sendPackets(new S_SkillIconAura(221, remainingTime, 6));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_EXP_UP) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50616);
				pc.addExpBonusPct(potion.getEffect().getExp());
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STATUS_EXP_UP_II) { // ????????????????????????II
				L1ExtraPotion potion = L1ExtraPotion.get(50617);
				pc.addExpBonusPct(potion.getEffect().getExp());
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_SWORDMAN) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50618);
				pc.addMaxHp(potion.getEffect().getHp());
				pc.addHpr(potion.getEffect().getHpr());
				pc.sendPackets(new S_HpUpdate(pc.getCurrentHp(), pc.getMaxHp()));
				pc.startHpRegeneration();
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_MAGICIAN) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50619);
				pc.addMaxMp(potion.getEffect().getMp());
				pc.addMpr(potion.getEffect().getMpr());
				pc.sendPackets(new S_MpUpdate(pc.getCurrentMp(), pc.getMaxMp()));
				pc.startMpRegeneration();
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_RECOVERY) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50620);
				pc.addHpr(potion.getEffect().getHpr());
				pc.startHpRegeneration();
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_MEDITATION) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50621);
				pc.addMpr(potion.getEffect().getMpr());
				pc.startMpRegeneration();
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_LIFE) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50622);
				pc.addMaxHp(potion.getEffect().getHp());
				pc.sendPackets(new S_HpUpdate(pc.getCurrentHp(), pc.getMaxHp()));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_MAGIC) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50623);
				pc.addMaxMp(potion.getEffect().getMp());
				pc.sendPackets(new S_MpUpdate(pc.getCurrentMp(), pc.getMaxMp()));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_MAGIC_RESIST) { // ??????????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50624);
				pc.addMr(potion.getEffect().getMr());
				pc.sendPackets(new S_SpMr(pc));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_STR) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50625);
				pc.addStr(potion.getEffect().getStr());
				pc.sendPackets(new S_OwnCharStatus(pc));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_DEX) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50626);
				pc.addDex(potion.getEffect().getDex());
				pc.sendPackets(new S_OwnCharStatus(pc));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_CON) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50627);
				pc.addCon(potion.getEffect().getCon());
				pc.sendPackets(new S_OwnCharStatus(pc));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_INT) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50628);
				pc.addInt(potion.getEffect().getInt());
				pc.sendPackets(new S_OwnCharStatus(pc));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_WIS) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50629);
				pc.addWis(potion.getEffect().getWis());
				pc.sendPackets(new S_OwnCharStatus(pc));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_RAGE) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50630);
				pc.addHitup(potion.getEffect().getHit());
				pc.addDmgup(potion.getEffect().getDmg());
				pc.addBowHitup(potion.getEffect().getBowHit());
				pc.addBowDmgup(potion.getEffect().getBowDmg());
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == POTION_OF_CONCENTRATION) { // ????????????????????????
				L1ExtraPotion potion = L1ExtraPotion.get(50631);
				pc.addSp(potion.getEffect().getSp());
				pc.sendPackets(new S_SpMr(pc));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId >= COOKING_BEGIN && skillId <= COOKING_END) { // ??????
				L1Skill skill = SkillTable.getInstance().findBySkillId(skillId);
				L1BuffSkillExecutor exe = skill.newBuffSkillExecutor();
				exe.addEffect(null, pc, remainingTime);
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == BLOODSTAIN_OF_ANTHARAS) { // ????????????????????????
				if (remainingTime - differenceTime > 0) {
					L1BuffUtil.bloodstain(pc, (byte) 0,
							(remainingTime - differenceTime) / 60, false);
				}
			} else if (skillId == BLOODSTAIN_OF_FAFURION) { // ????????????????????????
				if (remainingTime - differenceTime > 0) {
					L1BuffUtil.bloodstain(pc, (byte) 1,
							(remainingTime - differenceTime) / 60, false);
				}
			} else if (skillId == BLOODSTAIN_OF_LINDVIOR) { // ???????????????????????????
				if (remainingTime - differenceTime > 0) {
					L1BuffUtil.bloodstain(pc, (byte) 2,
							(remainingTime - differenceTime) / 60, false);
				}
				//} else if (skillId == BLOODSTAIN_OF_VALAKAS) { // ????????????????????????(?????????)
				//	if (remainingTime - differenceTime > 0) {
				//		L1BuffUtil.bloodstain(pc, (byte) 3,
				//				(remainingTime - differenceTime) / 60, false);
				//	}
			} else if (skillId == BLESS_OF_CRAY) { // ??????????????????
				L1BuffUtil.effectBlessOfDragonSlayer(pc, skillId, 2400, 7681);
			} else if (skillId == BLESS_OF_SAEL) { // ??????????????????
				L1BuffUtil.effectBlessOfDragonSlayer(pc, skillId, 2400, 7680);
			} else if (skillId == BLESS_OF_GUNTER) { // ?????????????????????
				L1BuffUtil.effectBlessOfDragonSlayer(pc, skillId, 2400, 7683);
			} else if (skillId == MAGIC_EYE_OF_ANTHARAS) { // ???????????????
				pc.addResistHold(5);
				pc.addDodge((byte) 1); // ????????? + 10%
				pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_PLUS, pc.getDodge()));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == MAGIC_EYE_OF_FAFURION) { // ???????????????
				pc.addResistFreeze(5);
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == MAGIC_EYE_OF_LINDVIOR) { // ???????????????
				pc.addResistSleep(5);
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == MAGIC_EYE_OF_VALAKAS) { // ???????????????
				pc.addResistStun(5);
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == MAGIC_EYE_OF_BIRTH) { // ???????????????
				pc.addResistHold(5);
				pc.addResistFreeze(5);
				pc.addDodge((byte) 1); // ????????? + 10%
				pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_PLUS, pc.getDodge()));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == MAGIC_EYE_OF_SHAPE) { // ???????????????
				pc.addResistHold(5);
				pc.addResistFreeze(5);
				pc.addResistSleep(5);
				pc.addDodge((byte) 1); // ????????? + 10%
				pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_PLUS, pc.getDodge()));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == MAGIC_EYE_OF_LIFE) { // ???????????????
				pc.addResistHold(5);
				pc.addResistFreeze(5);
				pc.addResistSleep(5);
				pc.addResistStun(5);
				pc.addDodge((byte) 1); // ????????? + 10%
				pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_PLUS, pc.getDodge()));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == STONE_OF_DRAGON) { // ??????????????????
				L1FloraPotion potion = L1FloraPotion.get(50555);
				pc.addHitup(potion.getEffect(pc).getHit());
				pc.addDmgup(potion.getEffect(pc).getDmg());
				pc.addBowHitup(potion.getEffect(pc).getBowHit());
				pc.addBowDmgup(potion.getEffect(pc).getBowDmg());
				pc.addSp(potion.getEffect(pc).getSp());
				pc.sendPackets(new S_SpMr(pc));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == BLESS_OF_COMA1) { // ??????????????????
				pc.setSkillEffect(skillId, remainingTime * 1000);
				pc.addStr(5);
				pc.addDex(5);
				pc.addCon(1);
				pc.addHitup(3);
				pc.addAc(-3);
			} else if (skillId == BLESS_OF_COMA2) { // ??????????????????
				pc.addStr(5);
				pc.addDex(5);
				pc.addCon(3);
				pc.addHitup(5);
				pc.addAc(-8);
				pc.addSp(1);
				pc.addExpBonusPct(20);
				pc.sendPackets(new S_SpMr(pc));
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else if (skillId == BLESS_OF_SAMURAI) { // ???????????????
				pc.addExpBonusPct(10);
				pc.setSkillEffect(skillId, remainingTime * 1000);
			} else {
				L1SkillUse l1skilluse = new L1SkillUse();
				l1skilluse.handleCommands(clientthread.getActiveChar(),
						skillId, pc.getId(), pc.getX(), pc.getY(), null,
						remainingTime, L1SkillUse.TYPE_LOGIN);
			}
		}
	}

	private void buffBlessOfAin(L1PcInstance pc) {
		if(pc.getBlessOfAin() >= 2000000){
			pc.setBlessOfAin(2000000);
			pc.sendPackets(new S_PacketBox(S_PacketBox.BLESS_OF_AIN, pc.getBlessOfAin()));
			return;
		}
		Timestamp logoutTime = pc.getLogoutTime();
		if (logoutTime == null) {
			logoutTime = new Timestamp(System.currentTimeMillis());
		}
		int tmp = (int)((System.currentTimeMillis() - logoutTime.getTime()) / 900000);
		int sum = pc.getBlessOfAin() + (tmp * 10000);
		if(sum >= 2000000) {
			pc.setBlessOfAin(2000000);
		} else {
			pc.setBlessOfAin(sum);
		}
		pc.sendPackets(new S_PacketBox(S_PacketBox.BLESS_OF_AIN, pc.getBlessOfAin()));
	}
	
	private void checkPledgeRecommendation(L1PcInstance pc){
		if(pc.getClanId() > 0){
			//pc.sendPackets(new S_ClanAttention());
			//pc.sendPackets(new S_PacketBox(S_PacketBox.PLEDGE_EMBLEM_STATUS, pc.getClan().getEmblemStatus()));
			if(pc.getClanRank() == L1Clan.CLAN_RANK_LEADER
							|| pc.getClanRank() == L1Clan.CLAN_RANK_SUBLEADER
							|| pc.getClanRank() == L1Clan.CLAN_RANK_GUARDIAN){
				if(ClanRecommendTable.getInstance().isRecorded(pc.getClanId())){
					if(ClanRecommendTable.getInstance().isClanApplyByPlayer(pc.getClanId())){
						pc.sendPackets(new S_ServerMessage(3248)); // ????????????????????????????????????
					}
				} else {
					//pc.sendPackets(new S_PacketBox(S_PacketBox.PLEDGE_EMBLEM_STATUS, pc.getClan().getEmblemStatus()));
					//pc.sendPackets(new S_ClanAttention());
					pc.sendPackets(new S_ServerMessage(3246)); // ???????????????????????????????????????????????????
				}
			}
		} else {
			if(pc.isCrown()){
				pc.sendPackets(new S_ServerMessage(3247)); // ???????????????????????????????????????????????????????????????
			} else {
				if(ClanRecommendTable.getInstance().isApplied(pc.getName())){
				} else {
					pc.sendPackets(new S_ServerMessage(3245)); // ???????????????????????????????????????????????????
				}
			}
		}
	}
		
	@Override
	public String getType() {
		return C_LOGIN_TO_SERVER;
	}
}
