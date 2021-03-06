/**
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
package jp.l1j.server.model.instance;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import jp.l1j.server.datatables.NpcTalkDataTable;
import jp.l1j.server.datatables.TownTable;
import jp.l1j.server.model.L1Attack;
import jp.l1j.server.model.L1BugBearRace;
import jp.l1j.server.model.L1CastleLocation;
import jp.l1j.server.model.L1Clan;
import jp.l1j.server.model.L1NpcTalkData;
import jp.l1j.server.model.L1Quest;
import jp.l1j.server.model.L1TownLocation;
import jp.l1j.server.model.L1World;
import jp.l1j.server.model.gametime.L1GameTimeClock;
import jp.l1j.server.model.skill.L1SkillId;
import jp.l1j.server.model.skill.L1SkillUse;
import jp.l1j.server.packets.server.S_ChangeHeading;
import jp.l1j.server.packets.server.S_NpcTalkReturn;
import jp.l1j.server.packets.server.S_ServerMessage;
import jp.l1j.server.packets.server.S_SkillHaste;
import jp.l1j.server.packets.server.S_SkillSound;
import jp.l1j.server.templates.L1Npc;

public class L1MerchantInstance extends L1NpcInstance {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static Logger _log = Logger.getLogger(L1MerchantInstance.class
			.getName());
	
	/**
	 * @param template
	 */
	public L1MerchantInstance(L1Npc template) {
		super(template);
	}

	@Override
	public void onAction(L1PcInstance pc) {
		onAction(pc, 0);
	}

	@Override
	public void onAction(L1PcInstance pc, int skillId) {
		L1Attack attack = new L1Attack(pc, this, skillId);
		attack.calcHit();
		attack.action();
		attack.addChaserAttack();
		attack.addEvilAttack();
		attack.calcDamage();
		attack.calcStaffOfMana();
		attack.addPcPoisonAttack(pc, this);
		attack.commit();
	}

	@Override
	public void onNpcAI() {
		if (isAiRunning()) {
			return;
		}
		setActived(false);
		startAI();
	}

	@Override
	public void onTalkAction(L1PcInstance pc) {
		int objid = getId();
		L1NpcTalkData talking = NpcTalkDataTable.getInstance().getTemplate(
				getNpcTemplate().getNpcId());
		int npcid = getNpcTemplate().getNpcId();
		L1Quest quest = pc.getQuest();
		String htmlid = null;
		String[] htmldata = null;

		int pcX = pc.getX();
		int pcY = pc.getY();
		int npcX = getX();
		int npcY = getY();

		if (getNpcTemplate().getChangeHead()) {
			if (pcX == npcX && pcY < npcY) {
				setHeading(0);
			} else if (pcX > npcX && pcY < npcY) {
				setHeading(1);
			} else if (pcX > npcX && pcY == npcY) {
				setHeading(2);
			} else if (pcX > npcX && pcY > npcY) {
				setHeading(3);
			} else if (pcX == npcX && pcY > npcY) {
				setHeading(4);
			} else if (pcX < npcX && pcY > npcY) {
				setHeading(5);
			} else if (pcX < npcX && pcY == npcY) {
				setHeading(6);
			} else if (pcX < npcX && pcY < npcY) {
				setHeading(7);
			}
			broadcastPacket(new S_ChangeHeading(this));

			synchronized (this) {
				if (_monitor != null) {
					_monitor.cancel();
				}
				setRest(true);
				_monitor = new RestMonitor();
				_restTimer.schedule(_monitor, REST_MILLISEC);
			}
		}

		if (talking != null) {
			if (npcid == 70841) { // ??????????????????
				if (pc.isElf()) { // ?????????
					htmlid = "luudielE1";
				} else if (pc.isDarkelf()) { // ??????????????????
					htmlid = "luudielCE1";
				} else {
					htmlid = "luudiel1";
				}
			} else if (npcid == 70522) { // ????????????
				if (pc.isCrown()) { // ??????
					if (pc.getLevel() >= 15) {
						int lv15_step = quest.getStep(L1Quest.QUEST_LEVEL15);
						if (lv15_step == 2 || lv15_step == L1Quest.QUEST_END) { // ???????????????
							htmlid = "gunterp11";
						} else {
							htmlid = "gunterp9";
						}
					} else { // Lv15??????
						htmlid = "gunterp12";
					}
				} else if (pc.isKnight()) { // ?????????
					int lv30_step = quest.getStep(L1Quest.QUEST_LEVEL30);
					if (lv30_step == 0) { // ?????????
						htmlid = "gunterk9";
					} else if (lv30_step == 1) {
						htmlid = "gunterkE1";
					} else if (lv30_step == 2) { // ????????????????????????
						htmlid = "gunterkE2";
					} else if (lv30_step >= 3) { // ????????????????????????
						htmlid = "gunterkE3";
					}
				} else if (pc.isElf()) { // ?????????
					htmlid = "guntere1";
				} else if (pc.isWizard()) { // ???????????????
					htmlid = "gunterw1";
				} else if (pc.isDarkelf()) { // ??????????????????
					htmlid = "gunterde1";
				}
			} else if (npcid == 70653) { // ????????????
				if (pc.isCrown()) { // ??????
					if (pc.getLevel() >= 45) {
						if (quest.isEnd(L1Quest.QUEST_LEVEL30)) { // lv30???????????????
							int lv45_step = quest
									.getStep(L1Quest.QUEST_LEVEL45);
							if (lv45_step == L1Quest.QUEST_END) { // ???????????????
								htmlid = "masha4";
							} else if (lv45_step >= 1) { // ????????????
								htmlid = "masha3";
							} else { // ?????????
								htmlid = "masha1";
							}
						}
					}
				} else if (pc.isKnight()) { // ?????????
					if (pc.getLevel() >= 45) {
						if (quest.isEnd(L1Quest.QUEST_LEVEL30)) { // Lv30????????????????????????
							int lv45_step = quest
									.getStep(L1Quest.QUEST_LEVEL45);
							if (lv45_step == L1Quest.QUEST_END) { // ???????????????
								htmlid = "mashak3";
							} else if (lv45_step == 0) { // ?????????
								htmlid = "mashak1";
							} else if (lv45_step >= 1) { // ????????????
								htmlid = "mashak2";
							}
						}
					}
				} else if (pc.isElf()) { // ?????????
					if (pc.getLevel() >= 45) {
						if (quest.isEnd(L1Quest.QUEST_LEVEL30)) { // Lv30????????????????????????
							int lv45_step = quest
									.getStep(L1Quest.QUEST_LEVEL45);
							if (lv45_step == L1Quest.QUEST_END) { // ???????????????
								htmlid = "mashae3";
							} else if (lv45_step >= 1) { // ????????????
								htmlid = "mashae2";
							} else { // ?????????
								htmlid = "mashae1";
							}
						}
					}
				}
			} else if (npcid == 70554) { // ??????
				if (pc.isCrown()) { // ??????
					if (pc.getLevel() >= 15) {
						int lv15_step = quest.getStep(L1Quest.QUEST_LEVEL15);
						if (lv15_step == 1) { // ?????????????????????
							htmlid = "zero5";
						} else if (lv15_step == L1Quest.QUEST_END) { // ????????????????????????????????????
							htmlid = "zero1";// 6
						} else {
							htmlid = "zero1";
						}
					} else { // Lv15??????
						htmlid = "zero6";
					}
				}
			} else if (npcid == 70783) { // ?????????
				if (pc.isCrown()) { // ??????
					if (pc.getLevel() >= 30) {
						if (quest.isEnd(L1Quest.QUEST_LEVEL15)) { // lv15?????????????????????
							int lv30_step = quest
									.getStep(L1Quest.QUEST_LEVEL30);
							if (lv30_step == L1Quest.QUEST_END) { // ???????????????
								htmlid = "aria3";
							} else if (lv30_step == 1) { // ????????????
								htmlid = "aria2";
							} else { // ?????????
								htmlid = "aria1";
							}
						}
					}
				}
			} else if (npcid == 70782) { // ??????????????????
				if (pc.getTempCharGfx() == 1037 || pc.getTempCharGfx() == 2437) {// ?????????????????????????????????
					if (pc.isCrown()) { // ??????
						if (quest.getStep(L1Quest.QUEST_LEVEL30) == 1) {
							htmlid = "ant1";
						} else {
							htmlid = "ant3";
						}
					} else { // ????????????
						htmlid = "ant3";
					}
				}
			} else if (npcid == 70545) { // ???????????????
				if (pc.isCrown()) { // ??????
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (lv45_step >= 1 && lv45_step != L1Quest.QUEST_END) { // ?????????????????????
						if (pc.getInventory().checkItem(40586)) { // ???????????????(???)
							htmlid = "richard4";
						} else {
							htmlid = "richard1";
						}
					}
				}
			} else if (npcid == 70739) { // ??????????????????
				int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
				int lv50_step = quest.getStep(L1Quest.QUEST_LEVEL50);
				if ((pc.getLevel() >= 50) && (lv45_step == L1Quest.QUEST_END)) {
					if (pc.isCrown()) {
						if (lv50_step == 0) {
							htmlid = "dicardingp1";
						} else if (lv50_step == L1Quest.QUEST_END) {
							htmlid = "dicardingp15";
						} else if (lv50_step >= 5) {
							if (pc.getInventory().checkItem(49241, 1)) {
								htmlid = "dicardingp13";
							} else {
								htmlid = "dicardingp11";
							}
						} else if (lv50_step >= 4) {
							htmlid = "dicardingp10";
						} else if (lv50_step >= 3) {
							htmlid = "dicardingp8";
						} else if (lv50_step >= 2) {
							htmlid = "dicardingp5";
						} else if (lv50_step >= 1) {
							htmlid = "dicardingp4";
						}
					} else if (pc.isKnight()) {
						if (lv50_step == 0) {
							htmlid = "dicardingk1";
						} else if (lv50_step == L1Quest.QUEST_END) {
							htmlid = "dicardingk16";
						} else if (lv50_step >= 5) {
							if (pc.getInventory().checkItem(49241, 1)) {
								htmlid = "dicardingk14";
							} else {
								htmlid = "dicardingk12";
							}
						} else if (lv50_step >= 4) {
							htmlid = "dicardingk11";
						} else if (lv50_step >= 3) {
							if (pc.getInventory().checkItem(49161, 10)) {
								htmlid = "dicardingk9";
							} else {
								htmlid = "dicardingk8";
							}
						} else if (lv50_step >= 2) {
							htmlid = "dicardingk5";
						} else if (lv50_step >= 1) {
							htmlid = "dicardingk4";
						}
					} else if (pc.isElf()) {
						if (lv50_step == 0) {
							htmlid = "dicardinge1";
						} else if (lv50_step == L1Quest.QUEST_END) {
							htmlid = "dicardinge17";
						} else if (lv50_step >= 5) {
							if (pc.getInventory().checkItem(49241, 1)) {
								htmlid = "dicardinge15";
							} else {
								htmlid = "dicardinge13";
							}
						} else if (lv50_step >= 4) {
							htmlid = "dicardinge9";
						} else if (lv50_step >= 3) {
							htmlid = "dicardinge8";
						} else if (lv50_step >= 2) {
							htmlid = "dicardinge5";
						} else if (lv50_step >= 1) {
							htmlid = "dicardinge4";
						}
					} else if (pc.isWizard()) {
						if (lv50_step == 0) {
							htmlid = "dicardingw1";
						} else if (lv50_step == L1Quest.QUEST_END) {
							htmlid = "dicardingw15";
						} else if (lv50_step >= 5) {
							if (pc.getInventory().checkItem(49241, 1)) {
								htmlid = "dicardingw13";
							} else {
								htmlid = "dicardingw11";
							}
						} else if (lv50_step >= 4) {
							htmlid = "dicardingw10";
						} else if (lv50_step >= 3) {
							htmlid = "dicardingw6";
						} else if (lv50_step >= 2) {
							if (pc.getInventory().checkItem(49164, 1)) {
								htmlid = "dicardingw5";
							}
						} else if (lv50_step >= 1) {
							htmlid = "dicardingw4";
						}
					} 
				}
			} else if (npcid == 91307) { // ?????????????????????
				if (pc.getInventory().checkItem(49241, 1)) {
					htmlid ="50q_pout1";
				} else {
					htmlid = "50q_pout";
				}
			} else if (npcid == 91308) { // ?????????????????????
				if (pc.isCrown()) {
					htmlid = "rtf01";
				} else if (pc.isKnight()) {
					htmlid = "rtf02";
				} else if (pc.isElf()) {
					htmlid = "rtf03";
				} else if (pc.isWizard()) {
					htmlid = "rtf04";
				}
			} else if (npcid == 91299) { // ???????????????
				int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
				int lv50_step = quest.getStep(L1Quest.QUEST_LEVEL50);
				if ((pc.getLevel() >= 50) && (lv45_step == L1Quest.QUEST_END)) {
					if (pc.isCrown() && (lv50_step == 4)) {
						htmlid = "50quest_p";
					} else if (pc.isKnight() && (lv50_step == 4)) {
						htmlid = "50quest_k";
					} else if (pc.isElf() && (lv50_step == 5)) {
						htmlid = "50quest_e";
					} else if (pc.isWizard() && (lv50_step == 4)) {
						htmlid = "50quest_w";
					}
				}
			} else if (npcid == 91298) { // ?????????????????????
				int lv50_step = quest.getStep(L1Quest.QUEST_LEVEL50);
				if (pc.isCrown()) {
					if (lv50_step == 3) {
						htmlid = "kiholl1";
					} else {
						htmlid = "kiholl0";
					}
				} else {
					htmlid = "kiholl0";
				}
			} else if (npcid == 91311) { // ??????????????????????????????
				int lv50_step = quest.getStep(L1Quest.QUEST_LEVEL50);
				if (pc.isWizard()) {
					if (lv50_step == L1Quest.QUEST_END) {
						htmlid = "dspym5";
					} else if (lv50_step >= 3) {
						htmlid = "dspym4";
					} else if (lv50_step >= 2) {
						htmlid = "dspym3";
					} else if (lv50_step == 1) {
						htmlid = "dspym1";
					}
				} else {
					htmlid = "dspym5";
				}
			} else if (npcid == 70776) { // ??????
				if (pc.isCrown()) { // ??????
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (lv45_step == 1) {
						htmlid = "meg1";
					} else if (lv45_step >= 2 && lv45_step <= 3) { // ??????????????????
						htmlid = "meg2";
					} else if (lv45_step >= 4) { // ?????????????????????
						htmlid = "meg3";
					}
				}
			} else if (npcid == 71200) { // ???????????? ?????????
				if (pc.isCrown()) { // ??????
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (lv45_step == L1Quest.QUEST_END) {
						htmlid = "pieta8";
					} else if (lv45_step > 2) {
						if (pc.getInventory().checkItem(40568)) {
							htmlid = "pieta8";
						} else {
							htmlid = "pieta6";
						}
					} else if (lv45_step == 2) {
						if(pc.getInventory().checkItem(41422)) {
							htmlid = "pieta4";
						} else {
							htmlid = "pieta2";
						}
					} else {
						htmlid = "pieta1";
					}
				} else {
					htmlid = "pieta1";
				}
				// } else if (npcid == 71200) { // ???????????? ?????????
				// if (pc.isCrown()) { // ??????
				// int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
				// if (lv45_step >= 6 && lv45_step == L1Quest.QUEST_END ) {
				// //?????????????????????or??????
				// htmlid = "pieta9";
				// } else if (lv45_step == 2) { // ??????????????????????????????????????????
				// htmlid = "pieta2";
				// } else if (lv45_step == 2 ||
				// pc.getInventory().checkItem(41422) ) {//
				// ???????????????????????????
				// htmlid = "pieta4";
				// } else if (lv45_step == 3) { // ???????????????????????????
				// htmlid = "pieta6";
				// } else {//lv45??????or????????????30???
				// htmlid = "pieta8";
				// }
				// } else { // ????????????
				// htmlid = "pieta1";
				// }
				// } else if (npcid == 70751) { // ????????????
				// if (pc.isCrown()) { // ??????
				// if (pc.getLevel() >= 45) {
				// if (quest.getStep(L1Quest.QUEST_LEVEL45) == 2) { //
				// ??????????????????
				// htmlid = "brad1";
				// }
				// }
				// }
			} else if (npcid == 70798) { // ????????????
				if (pc.isKnight()) { // ?????????
					if (pc.getLevel() >= 15) {
						int lv15_step = quest.getStep(L1Quest.QUEST_LEVEL15);
						if (lv15_step >= 1) { // ???????????????????????????
							htmlid = "riky5";
						} else {
							htmlid = "riky1";
						}
					} else { // Lv15??????
						htmlid = "riky6";
					}
				}
			} else if (npcid == 70802) { // ?????????
				if (pc.isKnight()) { // ?????????
					if (pc.getLevel() >= 15) {
						int lv15_step = quest.getStep(L1Quest.QUEST_LEVEL15);
						if (lv15_step == L1Quest.QUEST_END) { // ????????????????????????
							htmlid = "aanon7";
						} else if (lv15_step == 1) { // ???????????????????????????
							htmlid = "aanon4";
						}
					}
				}
			} else if (npcid == 70775) { // ?????????
				if (pc.isKnight()) { // ?????????
					if (pc.getLevel() >= 30) {
						if (quest.isEnd(L1Quest.QUEST_LEVEL15)) { // LV15????????????????????????
							int lv30_step = quest
									.getStep(L1Quest.QUEST_LEVEL30);
							if (lv30_step == 0) { // ?????????
								htmlid = "mark1";
							} else {
								htmlid = "mark2";
							}
						}
					}
				}
			} else if (npcid == 70794) { // ?????????
				if (pc.isCrown()) { // ??????
					htmlid = "gerardp1";
				} else if (pc.isKnight()) { // ?????????
					int lv30_step = quest.getStep(L1Quest.QUEST_LEVEL30);
					if (lv30_step == L1Quest.QUEST_END) { // ?????????????????????
						htmlid = "gerardkEcg";
					} else if (lv30_step < 3) { // ?????????????????????
						htmlid = "gerardk7";
					} else if (lv30_step == 3) { // ????????????????????????
						htmlid = "gerardkE1";
					} else if (lv30_step == 4) { // ?????????????????????
						htmlid = "gerardkE2";
					} else if (lv30_step == 5) { // ??????????????? ????????????
						htmlid = "gerardkE3";
					} else if (lv30_step >= 6) { // ????????????????????????????????????
						htmlid = "gerardkE4";
					}
				} else if (pc.isElf()) { // ?????????
					htmlid = "gerarde1";
				} else if (pc.isWizard()) { // ???????????????
					htmlid = "gerardw1";
				} else if (pc.isDarkelf()) { // ??????????????????
					htmlid = "gerardde1";
				}
			} else if (npcid == 70555) { // ??????
				if (pc.getTempCharGfx() == 2374) { // ?????????????????????
					if (pc.isKnight()) { // ?????????
						if (quest.isEnd(L1Quest.QUEST_LEVEL30)) {
							htmlid = "jimcg";
						} else {
							htmlid = "jim2";
						}
					} else { // ???????????????
						htmlid = "jim4";
					}
				}
			} else if (npcid == 70715) { // ?????????
				if (pc.isKnight()) { // ?????????
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (lv45_step == 1) { // ????????????????????????
						htmlid = "jimuk1";
					} else if (lv45_step >= 2) { // ?????????????????????
						htmlid = "jimuk2";
					}
				}
			} else if (npcid == 70711) { // ?????????????????? ????????????
				if (pc.isKnight()) { // ?????????
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (lv45_step == 2) { // ?????????????????????
						if (pc.getInventory().checkItem(20026)) { // ?????????????????????
							htmlid = "giantk1";
						}
					} else if (lv45_step == 3) { // ??????????????????????????????????????????
						htmlid = "giantk2";
					} else if (lv45_step >= 4) { // ???????????????????????????
						htmlid = "giantk3";
					}
				}
			} else if (npcid == 70826) { // ??????
				if (pc.isElf()) { // ?????????
					if (pc.getLevel() >= 15) {
						if (quest.isEnd(L1Quest.QUEST_LEVEL15)) {
							htmlid = "oth5";
						} else {
							htmlid = "oth1";
						}
					} else { // ?????????????????????
						htmlid = "oth6";
					}
				}
			} else if (npcid == 70844) { // ?????????????????????
				if (pc.isElf()) { // ?????????
					if (pc.getLevel() >= 30) {
						if (quest.isEnd(L1Quest.QUEST_LEVEL15)) { // Lv15????????????
							int lv30_step = quest
									.getStep(L1Quest.QUEST_LEVEL30);
							if (lv30_step == L1Quest.QUEST_END) { // ????????????
								htmlid = "motherEE3";
							} else if (lv30_step >= 1) { // ????????????
								htmlid = "motherEE2";
							} else if (lv30_step <= 0) { // ?????????
								htmlid = "motherEE1";
							}
						} else { // Lv15?????????
							htmlid = "mothere1";
						}
					} else { // Lv30??????
						htmlid = "mothere1";
					}
				}
			} else if (npcid == 70724) { // ?????????
				if (pc.isElf()) { // ?????????
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (lv45_step >= 4) { // ?????????????????????
						htmlid = "heit5";
					} else if (lv45_step >= 3) { // ????????????????????????
						htmlid = "heit3";
					} else if (lv45_step >= 2) { // ?????????????????????
						htmlid = "heit2";
					} else if (lv45_step >= 1) { // ????????????????????????
						htmlid = "heit1";
					}
				}
			} else if (npcid == 70531) { // ??????
				if (pc.isWizard()) { // ???????????????
					if (pc.getLevel() >= 15) {
						if (quest.isEnd(L1Quest.QUEST_LEVEL15)) { // ????????????
							htmlid = "jem6";
						} else {
							htmlid = "jem1";
						}
					}
				}
			} else if (npcid == 70009) { // ?????????
				if (pc.isCrown()) { // ??????
					htmlid = "gerengp1";
				} else if (pc.isKnight()) { // ?????????
					htmlid = "gerengk1";
				} else if (pc.isElf()) { // ?????????
					htmlid = "gerenge1";
				} else if (pc.isWizard()) { // ???????????????
					if (pc.getLevel() >= 30) {
						if (quest.isEnd(L1Quest.QUEST_LEVEL15)) {
							int lv30_step = quest
									.getStep(L1Quest.QUEST_LEVEL30);
							if (lv30_step >= 4) { // ?????????????????????
								htmlid = "gerengw3";
							} else if (lv30_step >= 3) { // ????????????
								htmlid = "gerengT4";
							} else if (lv30_step >= 2) { // ?????????????????????????????????
								htmlid = "gerengT3";
							} else if (lv30_step >= 1) { // ????????????
								htmlid = "gerengT2";
							} else { // ?????????
								htmlid = "gerengT1";
							}
						} else { // Lv15?????????????????????
							htmlid = "gerengw3";
						}
					} else { // Lv30??????
						htmlid = "gerengw3";
					}
				} else if (pc.isDarkelf()) { // ??????????????????
					htmlid = "gerengde1";
				}
			} else if (npcid == 70763) { // ?????????
				if (pc.isWizard()) { // ???????????????
					int lv30_step = quest.getStep(L1Quest.QUEST_LEVEL30);
					if (lv30_step == L1Quest.QUEST_END) {
						if (pc.getLevel() >= 45) {
							int lv45_step = quest
									.getStep(L1Quest.QUEST_LEVEL45);
							if (lv45_step >= 1
									&& lv45_step != L1Quest.QUEST_END) { // ????????????
								htmlid = "talassmq2";
							} else if (lv45_step <= 0) { // ?????????
								htmlid = "talassmq1";
							}
						}
					} else if (lv30_step == 4) {
						htmlid = "talassE1";
					} else if (lv30_step == 5) {
						htmlid = "talassE2";
					}
				}
			} else if (npcid == 81105) { // ????????????
				if (pc.isWizard()) { // ???????????????
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (lv45_step >= 3) { // ????????????????????????
						htmlid = "stoenm3";
					} else if (lv45_step >= 2) { // ???????????? ????????????
						htmlid = "stoenm2";
					} else if (lv45_step >= 1) { // ????????? ????????????
						htmlid = "stoenm1";
					}
				}
			} else if (npcid == 70739) { // ?????????????????????
				if (pc.getLevel() >= 50) {
					int lv50_step = quest.getStep(L1Quest.QUEST_LEVEL50);
					if (lv50_step == L1Quest.QUEST_END) {
						if (pc.isCrown()) { // ??????
							htmlid = "dicardingp3";
						} else if (pc.isKnight()) { // ?????????
							htmlid = "dicardingk3";
						} else if (pc.isElf()) { // ?????????
							htmlid = "dicardinge3";
						} else if (pc.isWizard()) { // ???????????????
							htmlid = "dicardingw3";
						} else if (pc.isDarkelf()) { // ??????????????????
							htmlid = "dicarding";
						}
					} else if (lv50_step >= 1) { // ????????????????????? ????????????
						if (pc.isCrown()) { // ??????
							htmlid = "dicardingp2";
						} else if (pc.isKnight()) { // ?????????
							htmlid = "dicardingk2";
						} else if (pc.isElf()) { // ?????????
							htmlid = "dicardinge2";
						} else if (pc.isWizard()) { // ???????????????
							htmlid = "dicardingw2";
						} else if (pc.isDarkelf()) { // ??????????????????
							htmlid = "dicarding";
						}
					} else if (lv50_step >= 0) {
						if (pc.isCrown()) { // ??????
							htmlid = "dicardingp1";
						} else if (pc.isKnight()) { // ?????????
							htmlid = "dicardingk1";
						} else if (pc.isElf()) { // ?????????
							htmlid = "dicardinge1";
						} else if (pc.isWizard()) { // ???????????????
							htmlid = "dicardingw1";
						} else if (pc.isDarkelf()) { // ??????????????????
							htmlid = "dicarding";
						}
					} else {
						htmlid = "dicarding";
					}
				} else { // Lv50??????
					htmlid = "dicarding";
				}
			} else if (npcid == 70885) { // ?????????
				if (pc.isDarkelf()) { // ??????????????????
					if (pc.getLevel() >= 15) {
						int lv15_step = quest.getStep(L1Quest.QUEST_LEVEL15);
						if (lv15_step == L1Quest.QUEST_END) { // ????????????
							htmlid = "kanguard3";
						} else if (lv15_step >= 1) { // ????????????
							htmlid = "kanguard2";
						} else { // ?????????
							htmlid = "kanguard1";
						}
					} else { // Lv15??????
						htmlid = "kanguard5";
					}
				}
			} else if (npcid == 70892) { // ????????????
				if (pc.isDarkelf()) { // ??????????????????
					if (pc.getLevel() >= 30) {
						if (quest.isEnd(L1Quest.QUEST_LEVEL15)) {
							int lv30_step = quest
									.getStep(L1Quest.QUEST_LEVEL30);
							if (lv30_step == L1Quest.QUEST_END) { // ????????????
								htmlid = "ronde5";
							} else if (lv30_step >= 2) { // ??????????????????
								htmlid = "ronde3";
							} else if (lv30_step >= 1) { // ????????????
								htmlid = "ronde2";
							} else { // ?????????
								htmlid = "ronde1";
							}
						} else { // Lv15?????????????????????
							htmlid = "ronde7";
						}
					} else { // Lv30??????
						htmlid = "ronde7";
					}
				}
			} else if (npcid == 70895) { // ???????????????
				if (pc.isDarkelf()) { // ??????????????????
					if (pc.getLevel() >= 45) {
						if (quest.isEnd(L1Quest.QUEST_LEVEL30)) {
							int lv45_step = quest
									.getStep(L1Quest.QUEST_LEVEL45);
							if (lv45_step == L1Quest.QUEST_END) { // ????????????
								if (pc.getLevel() < 50) { // Lv50??????
									htmlid = "bluedikaq3";
								} else {
									int lv50_step = quest
											.getStep(L1Quest.QUEST_LEVEL50);
									if (lv50_step == L1Quest.QUEST_END) { // ????????????
										htmlid = "bluedikaq8";
									} else if (lv50_step >= 1) { // ????????????
										htmlid = "bluedikaq7";
									} else { // ?????????
										htmlid = "bluedikaq6";
									}
								}
							} else if (lv45_step >= 1) { // ????????????
								htmlid = "bluedikaq2";
							} else { // ?????????
								htmlid = "bluedikaq1";
							}
						} else { // Lv30?????????????????????
							htmlid = "bluedikaq5";
						}
					} else { // Lv45??????
						htmlid = "bluedikaq5";
					}
				}
			} else if (npcid == 70904) { // ??????
				if (pc.isDarkelf()) {
					if (quest.getStep(L1Quest.QUEST_LEVEL45) == 1) { // ???????????????????????????
						htmlid = "koup12";
					}
				}
			} else if (npcid == 70906) { // ??????
				if (pc.isDarkelf()) {
					if (pc.getLevel() >= 50) {
						int lv50_step = quest.getStep(L1Quest.QUEST_LEVEL50);
						if ((lv50_step == L1Quest.QUEST_END) || (lv50_step >= 4)) {
							htmlid = "kimaq4";
						} else if (lv50_step == 3) {
							htmlid = "kimaq3";
						} else {
							if (lv50_step >= 1) {
								htmlid = "kimaq1";
								quest.setStep(L1Quest.QUEST_LEVEL50, 2);
							} else {
								htmlid = "kima1";
							}
						}
					}
				}
			} else if (npcid == 70824) { // ????????????????????????????????????
				if (pc.isDarkelf()) {
					if (pc.getTempCharGfx() == 3634 || pc.getTempCharGfx() == 8783) { // ??????????????????
						int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
						if (lv45_step == 1) {
							htmlid = "assassin1";
						} else if (lv45_step == 2) {
							htmlid = "assassin2";
						} else {
							htmlid = "assassin3";
						}
					} else { // ????????????????????????
						htmlid = "assassin3";
					}
				}
			} else if (npcid == 70744) { // ?????????
				if (pc.isDarkelf()) { // ??????????????????
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (lv45_step >= 5) { // ??????????????????????????????
						htmlid = "roje14";
					} else if (lv45_step >= 4) { // ????????????????????? ????????????
						htmlid = "roje13";
					} else if (lv45_step >= 3) { // ????????? ????????????
						htmlid = "roje12";
					} else if (lv45_step >= 2) { // ???????????????????????????????????? ????????????
						htmlid = "roje11";
					} else { // ???????????????????????????????????? ?????????
						htmlid = "roje15";
					}
				}
			} else if (npcid == 70811) { // ?????????
				if (quest.getStep(L1Quest.QUEST_LYRA) >= 1) { // ????????????
					htmlid = "lyraEv3";
				} else { // ?????????
					htmlid = "lyraEv1";
				}
			} else if (npcid == 70087) { // ????????????
				if (pc.isDarkelf()) {
					htmlid = "sedia";
				}
			} else if (npcid == 70099) { // ????????????
				if (!quest.isEnd(L1Quest.QUEST_OILSKINMANT)) {
					if (pc.getLevel() > 13) {
						htmlid = "kuper1";
					}
				}
			} else if (npcid == 70796) { // ????????????
				if (!quest.isEnd(L1Quest.QUEST_OILSKINMANT)) {
					if (pc.getLevel() > 13) {
						htmlid = "dunham1";
					}
				}
			} else if (npcid == 70011) { // ?????????????????????????????????
				int time = L1GameTimeClock.getInstance().currentTime()
						.getSeconds() % 86400;
				if (time < 60 * 60 * 6 || time > 60 * 60 * 20) { // 20:00???6:00
					htmlid = "shipEvI6";
				}
			} else if (npcid == 70553) { // ???????????? ????????? ???????????????
				boolean hascastle = checkHasCastle(pc,
						L1CastleLocation.KENT_CASTLE_ID);
				if (hascastle) { // ??????????????????
					if (checkClanLeader(pc)) { // ?????????
						htmlid = "ishmael1";
						pc.setTax(true);
					} else {
						htmlid = "ishmael6";
						htmldata = new String[] { pc.getName() };
					}
				} else {
					htmlid = "ishmael7";
				}
			} else if (npcid == 70822) { // ??????????????? ????????? ????????????
				boolean hascastle = checkHasCastle(pc,
						L1CastleLocation.OT_CASTLE_ID);
				if (hascastle) { // ??????????????????
					if (checkClanLeader(pc)) { // ?????????
						htmlid = "seghem1";
						pc.setTax(true);
					} else {
						htmlid = "seghem6";
						htmldata = new String[] { pc.getName() };
					}
				} else {
					htmlid = "seghem7";
				}
			} else if (npcid == 70784) { // ???????????????????????? ????????? ???????????????
				boolean hascastle = checkHasCastle(pc,
						L1CastleLocation.WW_CASTLE_ID);
				if (hascastle) { // ??????????????????
					if (checkClanLeader(pc)) { // ?????????
						htmlid = "othmond1";
						pc.setTax(true);
					} else {
						htmlid = "othmond6";
						htmldata = new String[] { pc.getName() };
					}
				} else {
					htmlid = "othmond7";
				}
			} else if (npcid == 70623) { // ???????????? ????????? ????????????
				boolean hascastle = checkHasCastle(pc,
						L1CastleLocation.GIRAN_CASTLE_ID);
				if (hascastle) { // ??????????????????
					if (checkClanLeader(pc)) { // ?????????
						htmlid = "orville1";
						pc.setTax(true);
					} else {
						htmlid = "orville6";
						htmldata = new String[] { pc.getName() };
					}
				} else {
					htmlid = "orville7";
				}
			} else if (npcid == 70880) { // ???????????? ????????? ??????????????????
				boolean hascastle = checkHasCastle(pc,
						L1CastleLocation.HEINE_CASTLE_ID);
				if (hascastle) { // ??????????????????
					if (checkClanLeader(pc)) { // ?????????
						htmlid = "fisher1";
						pc.setTax(true);
					} else {
						htmlid = "fisher6";
						htmldata = new String[] { pc.getName() };
					}
				} else {
					htmlid = "fisher7";
				}
			} else if (npcid == 70665) { // ??????????????? ????????? ???????????????
				boolean hascastle = checkHasCastle(pc,
						L1CastleLocation.DOWA_CASTLE_ID);
				if (hascastle) { // ??????????????????
					if (checkClanLeader(pc)) { // ?????????
						htmlid = "potempin1";
						pc.setTax(true);
					} else {
						htmlid = "potempin6";
						htmldata = new String[] { pc.getName() };
					}
				} else {
					htmlid = "potempin7";
				}
			} else if (npcid == 70721) { // ???????????? ????????? ????????????
				boolean hascastle = checkHasCastle(pc,
						L1CastleLocation.ADEN_CASTLE_ID);
				if (hascastle) { // ??????????????????
					if (checkClanLeader(pc)) { // ?????????
						htmlid = "timon1";
						pc.setTax(true);
					} else {
						htmlid = "timon6";
						htmldata = new String[] { pc.getName() };
					}
				} else {
					htmlid = "timon7";
				}
			} else if (npcid == 81155) { // ?????????????????? ?????????
				boolean hascastle = checkHasCastle(pc,
						L1CastleLocation.DIAD_CASTLE_ID);
				if (hascastle) { // ??????????????????
					if (checkClanLeader(pc)) { // ?????????
						htmlid = "olle1";
						pc.setTax(true);
					} else {
						htmlid = "olle6";
						htmldata = new String[] { pc.getName() };
					}
				} else {
					htmlid = "olle7";
				}
			} else if (npcid == 80057) { // ??????????????????
				int karmaLevel = pc.getKarmaLevel();
				String[] html1 = { "alfons1", "cbk1", "cbk2", "cbk3", "cbk4",
						"cbk5", "cbk6", "cbk7", "cbk8" }; // 0 ~ 8
				String[] html2 = { "cyk1", "cyk2", "cyk3", "cyk4", "cyk5",
						"cyk6", "cyk7", "cyk8" }; // -1 ~ -8
				if (karmaLevel < 0) {
					htmlid = html2[Math.abs(karmaLevel) - 1];
				} else if (karmaLevel >= 0) {
					htmlid = html1[karmaLevel];
				} else {
					htmlid = "alfons1";
				}
			} else if (npcid == 80058) { // ????????????(??????)
				int level = pc.getLevel();
				if (level <= 44) {
					htmlid = "cpass03";
				} else if (level <= 51 && 45 <= level) {
					htmlid = "cpass02";
				} else {
					htmlid = "cpass01";
				}
			} else if (npcid == 80059) { // ????????????(???)
				if (pc.getKarmaLevel() > 0) {
					htmlid = "cpass03";
				} else if (pc.getInventory().checkItem(40921)) { // ??????????????????
					htmlid = "wpass02";
				} else if (pc.getInventory().checkItem(40917)) { // ???????????????
					htmlid = "wpass14";
				} else if (pc.getInventory().checkItem(40912) // ???????????????
						|| pc.getInventory().checkItem(40910) // ???????????????
						|| pc.getInventory().checkItem(40911)) { // ???????????????
					htmlid = "wpass04";
				} else if (pc.getInventory().checkItem(40909)) { // ???????????????
					int count = getNecessarySealCount(pc);
					if (pc.getInventory().checkItem(40913, count)) { // ????????????
						createRuler(pc, 1, count);
						htmlid = "wpass06";
					} else {
						htmlid = "wpass03";
					}
				} else if (pc.getInventory().checkItem(40913)) { // ????????????
					htmlid = "wpass08";
				} else {
					htmlid = "wpass05";
				}
			} else if (npcid == 80060) { // ????????????(???)
				if (pc.getKarmaLevel() > 0) {
					htmlid = "cpass03";
				} else if (pc.getInventory().checkItem(40921)) { // ??????????????????
					htmlid = "wpass02";
				} else if (pc.getInventory().checkItem(40920)) { // ???????????????
					htmlid = "wpass13";
				} else if (pc.getInventory().checkItem(40909) // ???????????????
						|| pc.getInventory().checkItem(40910) // ???????????????
						|| pc.getInventory().checkItem(40911)) { // ???????????????
					htmlid = "wpass04";
				} else if (pc.getInventory().checkItem(40912)) { // ???????????????
					int count = getNecessarySealCount(pc);
					if (pc.getInventory().checkItem(40916, count)) { // ????????????
						createRuler(pc, 8, count);
						htmlid = "wpass06";
					} else {
						htmlid = "wpass03";
					}
				} else if (pc.getInventory().checkItem(40916)) { // ????????????
					htmlid = "wpass08";
				} else {
					htmlid = "wpass05";
				}
			} else if (npcid == 80061) { // ????????????(???)
				if (pc.getKarmaLevel() > 0) {
					htmlid = "cpass03";
				} else if (pc.getInventory().checkItem(40921)) { // ??????????????????
					htmlid = "wpass02";
				} else if (pc.getInventory().checkItem(40918)) { // ???????????????
					htmlid = "wpass11";
				} else if (pc.getInventory().checkItem(40909) // ???????????????
						|| pc.getInventory().checkItem(40912) // ???????????????
						|| pc.getInventory().checkItem(40911)) { // ???????????????
					htmlid = "wpass04";
				} else if (pc.getInventory().checkItem(40910)) { // ???????????????
					int count = getNecessarySealCount(pc);
					if (pc.getInventory().checkItem(40914, count)) { // ????????????
						createRuler(pc, 4, count);
						htmlid = "wpass06";
					} else {
						htmlid = "wpass03";
					}
				} else if (pc.getInventory().checkItem(40914)) { // ????????????
					htmlid = "wpass08";
				} else {
					htmlid = "wpass05";
				}
			} else if (npcid == 80062) { // ????????????(???)
				if (pc.getKarmaLevel() > 0) {
					htmlid = "cpass03";
				} else if (pc.getInventory().checkItem(40921)) { // ??????????????????
					htmlid = "wpass02";
				} else if (pc.getInventory().checkItem(40919)) { // ???????????????
					htmlid = "wpass12";
				} else if (pc.getInventory().checkItem(40909) // ???????????????
						|| pc.getInventory().checkItem(40912) // ???????????????
						|| pc.getInventory().checkItem(40910)) { // ???????????????
					htmlid = "wpass04";
				} else if (pc.getInventory().checkItem(40911)) { // ???????????????
					int count = getNecessarySealCount(pc);
					if (pc.getInventory().checkItem(40915, count)) { // ????????????
						createRuler(pc, 2, count);
						htmlid = "wpass06";
					} else {
						htmlid = "wpass03";
					}
				} else if (pc.getInventory().checkItem(40915)) { // ????????????
					htmlid = "wpass08";
				} else {
					htmlid = "wpass05";
				}
			} else if (npcid == 80065) { // ?????????????????????
				if (pc.getKarmaLevel() < 3) {
					htmlid = "uturn0";
				} else {
					htmlid = "uturn1";
				}
			} else if (npcid == 80047) { // ???????????????
				if (pc.getKarmaLevel() > -3) {
					htmlid = "uhelp1";
				} else {
					htmlid = "uhelp2";
				}
			} else if (npcid == 80049) { // ????????????
				if (pc.getKarma() <= -10000000) {
					htmlid = "betray11";
				} else {
					htmlid = "betray12";
				}
			} else if (npcid == 80050) { // ??????????????????
				if (pc.getKarmaLevel() > -1) {
					htmlid = "meet103";
				} else {
					htmlid = "meet101";
				}
			} else if (npcid == 80053) { // ??????????????????
				int karmaLevel = pc.getKarmaLevel();
				if (karmaLevel == 0) {
					htmlid = "aliceyet";
				} else if (karmaLevel >= 1) {
					if (pc.getInventory().checkItem(196)
							|| pc.getInventory().checkItem(197)
							|| pc.getInventory().checkItem(198)
							|| pc.getInventory().checkItem(199)
							|| pc.getInventory().checkItem(200)
							|| pc.getInventory().checkItem(201)
							|| pc.getInventory().checkItem(202)
							|| pc.getInventory().checkItem(203)) {
						htmlid = "alice_gd";
					} else {
						htmlid = "gd";
					}
				} else if (karmaLevel <= -1) {
					if (pc.getInventory().checkItem(40991)) {
						if (karmaLevel <= -1) {
							htmlid = "Mate_1";
						}
					} else if (pc.getInventory().checkItem(196)) {
						if (karmaLevel <= -2) {
							htmlid = "Mate_2";
						} else {
							htmlid = "alice_1";
						}
					} else if (pc.getInventory().checkItem(197)) {
						if (karmaLevel <= -3) {
							htmlid = "Mate_3";
						} else {
							htmlid = "alice_2";
						}
					} else if (pc.getInventory().checkItem(198)) {
						if (karmaLevel <= -4) {
							htmlid = "Mate_4";
						} else {
							htmlid = "alice_3";
						}
					} else if (pc.getInventory().checkItem(199)) {
						if (karmaLevel <= -5) {
							htmlid = "Mate_5";
						} else {
							htmlid = "alice_4";
						}
					} else if (pc.getInventory().checkItem(200)) {
						if (karmaLevel <= -6) {
							htmlid = "Mate_6";
						} else {
							htmlid = "alice_5";
						}
					} else if (pc.getInventory().checkItem(201)) {
						if (karmaLevel <= -7) {
							htmlid = "Mate_7";
						} else {
							htmlid = "alice_6";
						}
					} else if (pc.getInventory().checkItem(202)) {
						if (karmaLevel <= -8) {
							htmlid = "Mate_8";
						} else {
							htmlid = "alice_7";
						}
					} else if (pc.getInventory().checkItem(203)) {
						htmlid = "alice_8";
					} else {
						htmlid = "alice_no";
					}
				}
			} else if (npcid == 80055) { // ??????????????????
				int amuletLevel = 0;
				if (pc.getInventory().checkItem(20358)
						|| pc.getAdditionalWarehouseInventory().checkItem(20358)) { // ???????????????????????????
					amuletLevel = 1;
				} else if (pc.getInventory().checkItem(20359)
						|| pc.getAdditionalWarehouseInventory().checkItem(20359)) { // ???????????????????????????
					amuletLevel = 2;
				} else if (pc.getInventory().checkItem(20360)
						|| pc.getAdditionalWarehouseInventory().checkItem(20360)) { // ???????????????????????????
					amuletLevel = 3;
				} else if (pc.getInventory().checkItem(20361)
						|| pc.getAdditionalWarehouseInventory().checkItem(20361)) { // ???????????????????????????
					amuletLevel = 4;
				} else if (pc.getInventory().checkItem(20362)
						|| pc.getAdditionalWarehouseInventory().checkItem(20362)) { // ???????????????????????????
					amuletLevel = 5;
				} else if (pc.getInventory().checkItem(20363)
						|| pc.getAdditionalWarehouseInventory().checkItem(20363)) { // ???????????????????????????
					amuletLevel = 6;
				} else if (pc.getInventory().checkItem(20364)
						|| pc.getAdditionalWarehouseInventory().checkItem(20364)) { // ???????????????????????????
					amuletLevel = 7;
				} else if (pc.getInventory().checkItem(20365)
						|| pc.getAdditionalWarehouseInventory().checkItem(20365)) { // ??????????????????????????????
					amuletLevel = 8;
				}
				if (pc.getKarmaLevel() == -1) {
					if (amuletLevel >= 1) {
						htmlid = "uamuletd";
					} else {
						htmlid = "uamulet1";
					}
				} else if (pc.getKarmaLevel() == -2) {
					if (amuletLevel >= 2) {
						htmlid = "uamuletd";
					} else {
						htmlid = "uamulet2";
					}
				} else if (pc.getKarmaLevel() == -3) {
					if (amuletLevel >= 3) {
						htmlid = "uamuletd";
					} else {
						htmlid = "uamulet3";
					}
				} else if (pc.getKarmaLevel() == -4) {
					if (amuletLevel >= 4) {
						htmlid = "uamuletd";
					} else {
						htmlid = "uamulet4";
					}
				} else if (pc.getKarmaLevel() == -5) {
					if (amuletLevel >= 5) {
						htmlid = "uamuletd";
					} else {
						htmlid = "uamulet5";
					}
				} else if (pc.getKarmaLevel() == -6) {
					if (amuletLevel >= 6) {
						htmlid = "uamuletd";
					} else {
						htmlid = "uamulet6";
					}
				} else if (pc.getKarmaLevel() == -7) {
					if (amuletLevel >= 7) {
						htmlid = "uamuletd";
					} else {
						htmlid = "uamulet7";
					}
				} else if (pc.getKarmaLevel() == -8) {
					if (amuletLevel >= 8) {
						htmlid = "uamuletd";
					} else {
						htmlid = "uamulet8";
					}
				} else {
					htmlid = "uamulet0";
				}
			} else if (npcid == 80056) { // ???????????????
				if (pc.getKarma() <= -10000000) {
					htmlid = "infamous11";
				} else {
					htmlid = "infamous12";
				}
			} else if (npcid == 80064) { // ????????????????????????
				if (pc.getKarmaLevel() < 1) {
					htmlid = "meet003";
				} else {
					htmlid = "meet001";
				}
			} else if (npcid == 80066) { // ???????????????
				if (pc.getKarma() >= 10000000) {
					htmlid = "betray01";
				} else {
					htmlid = "betray02";
				}
			} else if (npcid == 80071) { // ????????????????????????
				int earringLevel = 0;
				if (pc.getInventory().checkItem(21020)
						|| pc.getAdditionalWarehouseInventory().checkItem(21020)) { // ????????????????????????
					earringLevel = 1;
				} else if (pc.getInventory().checkItem(21021)
						|| pc.getAdditionalWarehouseInventory().checkItem(21021)) { // ????????????????????????
					earringLevel = 2;
				} else if (pc.getInventory().checkItem(21022)
						|| pc.getAdditionalWarehouseInventory().checkItem(21022)) { // ????????????????????????
					earringLevel = 3;
				} else if (pc.getInventory().checkItem(21023)
						|| pc.getAdditionalWarehouseInventory().checkItem(21023)) { // ????????????????????????
					earringLevel = 4;
				} else if (pc.getInventory().checkItem(21024)
						|| pc.getAdditionalWarehouseInventory().checkItem(21024)) { // ????????????????????????
					earringLevel = 5;
				} else if (pc.getInventory().checkItem(21025)
						|| pc.getAdditionalWarehouseInventory().checkItem(21025)) { // ????????????????????????
					earringLevel = 6;
				} else if (pc.getInventory().checkItem(21026)
						|| pc.getAdditionalWarehouseInventory().checkItem(21026)) { // ????????????????????????
					earringLevel = 7;
				} else if (pc.getInventory().checkItem(21027)
						|| pc.getAdditionalWarehouseInventory().checkItem(21027)) { // ????????????????????????
					earringLevel = 8;
				}
				if (pc.getKarmaLevel() == 1) {
					if (earringLevel >= 1) {
						htmlid = "lringd";
					} else {
						htmlid = "lring1";
					}
				} else if (pc.getKarmaLevel() == 2) {
					if (earringLevel >= 2) {
						htmlid = "lringd";
					} else {
						htmlid = "lring2";
					}
				} else if (pc.getKarmaLevel() == 3) {
					if (earringLevel >= 3) {
						htmlid = "lringd";
					} else {
						htmlid = "lring3";
					}
				} else if (pc.getKarmaLevel() == 4) {
					if (earringLevel >= 4) {
						htmlid = "lringd";
					} else {
						htmlid = "lring4";
					}
				} else if (pc.getKarmaLevel() == 5) {
					if (earringLevel >= 5) {
						htmlid = "lringd";
					} else {
						htmlid = "lring5";
					}
				} else if (pc.getKarmaLevel() == 6) {
					if (earringLevel >= 6) {
						htmlid = "lringd";
					} else {
						htmlid = "lring6";
					}
				} else if (pc.getKarmaLevel() == 7) {
					if (earringLevel >= 7) {
						htmlid = "lringd";
					} else {
						htmlid = "lring7";
					}
				} else if (pc.getKarmaLevel() == 8) {
					if (earringLevel >= 8) {
						htmlid = "lringd";
					} else {
						htmlid = "lring8";
					}
				} else {
					htmlid = "lring0";
				}
			} else if (npcid == 80072) { // ????????????????????????
				int karmaLevel = pc.getKarmaLevel();
				String[] html = { "lsmith0", "lsmith1", "lsmith2", "lsmith3",
						"lsmith4", "lsmith5", "lsmith7", "lsmith8" };
				if (karmaLevel <= 8) {
					htmlid = html[karmaLevel - 1];
				} else {
					htmlid = "";
				}
			} else if (npcid == 80074) { // ???????????????
				if (pc.getKarma() >= 10000000) {
					htmlid = "infamous01";
				} else {
					htmlid = "infamous02";
				}
			} else if (npcid == 80104) { // ?????????????????????
				if (!pc.isCrown()) { // ??????
					htmlid = "horseseller4";
				}
			} else if (npcid == 70528) { // ?????????????????? ?????????????????????
				htmlid = talkToTownmaster(pc,
						L1TownLocation.TOWNID_TALKING_ISLAND);
			} else if (npcid == 70546) { // ???????????? ?????????????????????
				htmlid = talkToTownmaster(pc, L1TownLocation.TOWNID_KENT);
			} else if (npcid == 70567) { // ????????????????????? ?????????????????????
				htmlid = talkToTownmaster(pc, L1TownLocation.TOWNID_GLUDIO);
			} else if (npcid == 70815) { // ????????? ?????????????????????
				htmlid = talkToTownmaster(pc,
						L1TownLocation.TOWNID_ORCISH_FOREST);
			} else if (npcid == 70774) { // ????????????????????? ?????????????????????
				htmlid = talkToTownmaster(pc, L1TownLocation.TOWNID_WINDAWOOD);
			} else if (npcid == 70799) { // ?????????????????????????????? ?????????????????????
				htmlid = talkToTownmaster(pc,
						L1TownLocation.TOWNID_SILVER_KNIGHT_TOWN);
			} else if (npcid == 70594) { // ??????????????? ?????????????????????
				htmlid = talkToTownmaster(pc, L1TownLocation.TOWNID_GIRAN);
			} else if (npcid == 70860) { // ??????????????? ?????????????????????
				htmlid = talkToTownmaster(pc, L1TownLocation.TOWNID_HEINE);
			} else if (npcid == 70654) { // ?????????????????? ?????????????????????
				htmlid = talkToTownmaster(pc, L1TownLocation.TOWNID_WERLDAN);
			} else if (npcid == 70748) { // ?????????????????? ?????????????????????
				htmlid = talkToTownmaster(pc, L1TownLocation.TOWNID_OREN);
			} else if (npcid == 70534) { // ?????????????????? ???????????????????????????
				htmlid = talkToTownadviser(pc,
						L1TownLocation.TOWNID_TALKING_ISLAND);
			} else if (npcid == 70556) { // ???????????? ???????????????????????????
				htmlid = talkToTownadviser(pc, L1TownLocation.TOWNID_KENT);
			} else if (npcid == 70572) { // ????????????????????? ???????????????????????????
				htmlid = talkToTownadviser(pc, L1TownLocation.TOWNID_GLUDIO);
			} else if (npcid == 70830) { // ????????? ???????????????????????????
				htmlid = talkToTownadviser(pc,
						L1TownLocation.TOWNID_ORCISH_FOREST);
			} else if (npcid == 70788) { // ????????????????????? ???????????????????????????
				htmlid = talkToTownadviser(pc, L1TownLocation.TOWNID_WINDAWOOD);
			} else if (npcid == 70806) { // ?????????????????????????????? ???????????????????????????
				htmlid = talkToTownadviser(pc,
						L1TownLocation.TOWNID_SILVER_KNIGHT_TOWN);
			} else if (npcid == 70631) { // ??????????????? ???????????????????????????
				htmlid = talkToTownadviser(pc, L1TownLocation.TOWNID_GIRAN);
			} else if (npcid == 70876) { // ??????????????? ???????????????????????????
				htmlid = talkToTownadviser(pc, L1TownLocation.TOWNID_HEINE);
			} else if (npcid == 70663) { // ?????????????????? ???????????????????????????
				htmlid = talkToTownadviser(pc, L1TownLocation.TOWNID_WERLDAN);
			} else if (npcid == 70761) { // ?????????????????? ???????????????????????????
				htmlid = talkToTownadviser(pc, L1TownLocation.TOWNID_OREN);
			} else if (npcid == 70997) { // ???????????????
				htmlid = talkToDoromond(pc);
			} else if (npcid == 70998) { // ?????????????????????
				htmlid = talkToSIGuide(pc);
			} else if (npcid == 70999) { // ???????????????(?????????)
				htmlid = talkToAlex(pc);
			} else if (npcid == 71000) { // ???????????????(?????????)
				htmlid = talkToAlexInTrainingRoom(pc);
			} else if (npcid == 71002) { // ??????????????????????????????
				htmlid = cancellation(pc);
			} else if (npcid == 70506) { // ?????????
				htmlid = talkToRuba(pc);
			} else if (npcid == 71005) { // ????????????
				htmlid = talkToPopirea(pc);
			} else if (npcid == 71009) { // ????????????
				if (pc.getLevel() < 13) {
					htmlid = "jpe0071";
				}
			} else if (npcid == 71011) { // ????????????
				if (pc.getLevel() < 13) {
					htmlid = "jpe0061";
				}
			} else if (npcid == 71014) { // ???????????????(???)
				if (pc.getLevel() < 13) {
					htmlid = "en0241";
				}
			} else if (npcid == 71015) { // ???????????????(???)
				if (pc.getLevel() < 13) {
					htmlid = "en0261";
				} else if (pc.getLevel() >= 13 && pc.getLevel() < 25) {
					htmlid = "en0262";
				}
			} else if (npcid == 71031) { // ??????????????????
				if (pc.getLevel() < 25) {
					htmlid = "en0081";
				}
			} else if (npcid == 71032) { // ??????????????????
				if (pc.isElf()) {
					htmlid = "en0091e";
				} else if (pc.isDarkelf()) {
					htmlid = "en0091d";
				} else if (pc.isKnight()) {
					htmlid = "en0091k";
				} else if (pc.isWizard()) {
					htmlid = "en0091w";
				} else if (pc.isCrown()) {
					htmlid = "en0091p";
				}
			} else if (npcid == 71034) { // ??????
				if (pc.getInventory().checkItem(41227)) { // ???????????????????????????
					if (pc.isElf()) {
						htmlid = "en0201e";
					} else if (pc.isDarkelf()) {
						htmlid = "en0201d";
					} else if (pc.isKnight()) {
						htmlid = "en0201k";
					} else if (pc.isWizard()) {
						htmlid = "en0201w";
					} else if (pc.isCrown()) {
						htmlid = "en0201p";
					}
				}
			} else if (npcid == 71033) { // ???????????????
				if (pc.getInventory().checkItem(41228)) { // ??????????????????
					if (pc.isElf()) {
						htmlid = "en0211e";
					} else if (pc.isDarkelf()) {
						htmlid = "en0211d";
					} else if (pc.isKnight()) {
						htmlid = "en0211k";
					} else if (pc.isWizard()) {
						htmlid = "en0211w";
					} else if (pc.isCrown()) {
						htmlid = "en0211p";
					}
				}
			} else if (npcid == 71026) { // ??????
				if (pc.getLevel() < 10) {
					htmlid = "en0113";
				} else if (pc.getLevel() >= 10 && pc.getLevel() < 25) {
					htmlid = "en0111";
				} else if (pc.getLevel() > 25) {
					htmlid = "en0112";
				}
			} else if (npcid == 71027) { // ??????
				if (pc.getLevel() < 10) {
					htmlid = "en0283";
				} else if (pc.getLevel() >= 10 && pc.getLevel() < 25) {
					htmlid = "en0281";
				} else if (pc.getLevel() > 25) {
					htmlid = "en0282";
				}
			} else if (npcid == 71021) { // ???????????????????????????
				if (pc.getLevel() < 12) {
					htmlid = "en0197";
				} else if (pc.getLevel() >= 12 && pc.getLevel() < 25) {
					htmlid = "en0191";
				}
			} else if (npcid == 71022) { // ????????????????????????
				if (pc.getLevel() < 12) {
					htmlid = "jpe0155";
				} else if (pc.getLevel() >= 12 && pc.getLevel() < 25) {
					if (pc.getInventory().checkItem(41230)
							|| pc.getInventory().checkItem(41231)
							|| pc.getInventory().checkItem(41232)
							|| pc.getInventory().checkItem(41233)
							|| pc.getInventory().checkItem(41235)
							|| pc.getInventory().checkItem(41238)
							|| pc.getInventory().checkItem(41239)
							|| pc.getInventory().checkItem(41240)) {
						htmlid = "jpe0158";
					}
				}
			} else if (npcid == 71023) { // ?????????????????????
				if (pc.getLevel() < 12) {
					htmlid = "jpe0145";
				} else if (pc.getLevel() >= 12 && pc.getLevel() < 25) {
					if (pc.getInventory().checkItem(41233)
							|| pc.getInventory().checkItem(41234)) {
						htmlid = "jpe0143";
					} else if (pc.getInventory().checkItem(41238)
							|| pc.getInventory().checkItem(41239)
							|| pc.getInventory().checkItem(41240)) {
						htmlid = "jpe0147";
					} else if (pc.getInventory().checkItem(41235)
							|| pc.getInventory().checkItem(41236)
							|| pc.getInventory().checkItem(41237)) {
						htmlid = "jpe0144";
					}
				}
			} else if (npcid == 71020) { // ?????????
				if (pc.getLevel() < 12) {
					htmlid = "jpe0125";
				} else if (pc.getLevel() >= 12 && pc.getLevel() < 25) {
					if (pc.getInventory().checkItem(41231)) {
						htmlid = "jpe0123";
					} else if (pc.getInventory().checkItem(41232)
							|| pc.getInventory().checkItem(41233)
							|| pc.getInventory().checkItem(41234)
							|| pc.getInventory().checkItem(41235)
							|| pc.getInventory().checkItem(41238)
							|| pc.getInventory().checkItem(41239)
							|| pc.getInventory().checkItem(41240)) {
						htmlid = "jpe0126";
					}
				}
			} else if (npcid == 71019) { // ??????????????????
				if (pc.getLevel() < 12) {
					htmlid = "jpe0114";
				} else if (pc.getLevel() >= 12 && pc.getLevel() < 25) {
					if (pc.getInventory().checkItem(41239)) { // ????????????????????????
						htmlid = "jpe0113";
					} else {
						htmlid = "jpe0111";
					}
				}
			} else if (npcid == 71018) { // ????????????
				if (pc.getLevel() < 12) {
					htmlid = "jpe0133";
				} else if (pc.getLevel() >= 12 && pc.getLevel() < 25) {
					if (pc.getInventory().checkItem(41240)) { // ????????????????????????
						htmlid = "jpe0132";
					} else {
						htmlid = "jpe0131";
					}
				}
			} else if (npcid == 71025) { // ????????????
				if (pc.getLevel() < 10) {
					htmlid = "jpe0086";
				} else if (pc.getLevel() >= 10 && pc.getLevel() < 25) {
					if (pc.getInventory().checkItem(41226)) { // ????????????
						htmlid = "jpe0084";
					} else if (pc.getInventory().checkItem(41225)) { // ????????????????????????
						htmlid = "jpe0083";
					} else if (pc.getInventory().checkItem(40653)
							|| pc.getInventory().checkItem(40613)) { // ?????????????????????
						htmlid = "jpe0081";
					}
				}
			} else if (npcid == 70512) { // ????????????????????? ????????????
				if (pc.getLevel() >= 25) {
					htmlid = "jpe0102";
				}
			} else if (npcid == 70514) { // ???????????????
				if (pc.getLevel() >= 25) {
					htmlid = "jpe0092";
				}
			} else if (npcid == 71038) { // ?????? ?????????
				if (pc.getInventory().checkItem(41060)) { // ?????????????????????
					if (pc.getInventory().checkItem(41090) // ????????????????????????
							|| pc.getInventory().checkItem(41091) // ?????????-?????????????????????
							|| pc.getInventory().checkItem(41092)) { // ???????????????????????????
						htmlid = "orcfnoname7";
					} else {
						htmlid = "orcfnoname8";
					}
				} else {
					htmlid = "orcfnoname1";
				}
			} else if (npcid == 71040) { // ???????????? ???????????? ??????
				if (pc.getInventory().checkItem(41060)) { // ?????????????????????
					if (pc.getInventory().checkItem(41065)) { // ??????????????????
						if (pc.getInventory().checkItem(41086) // ?????????????????????
								|| pc.getInventory().checkItem(41087) // ????????????????????????
								|| pc.getInventory().checkItem(41088) // ?????????????????????
								|| pc.getInventory().checkItem(41089)) { // ???????????????????????????
							htmlid = "orcfnoa6";
						} else {
							htmlid = "orcfnoa5";
						}
					} else {
						htmlid = "orcfnoa2";
					}
				} else {
					htmlid = "orcfnoa1";
				}
			} else if (npcid == 71041) { // ????????? ?????????
				if (pc.getInventory().checkItem(41060)) { // ?????????????????????
					if (pc.getInventory().checkItem(41064)) { // ??????????????????
						if (pc.getInventory().checkItem(41081) // ?????????????????????
								|| pc.getInventory().checkItem(41082) // ??????????????????????????????
								|| pc.getInventory().checkItem(41083) // ???????????????????????????
								|| pc.getInventory().checkItem(41084) // ?????????????????????????????????
								|| pc.getInventory().checkItem(41085)) { // ?????????????????????
							htmlid = "orcfhuwoomo2";
						} else {
							htmlid = "orcfhuwoomo8";
						}
					} else {
						htmlid = "orcfhuwoomo1";
					}
				} else {
					htmlid = "orcfhuwoomo5";
				}
			} else if (npcid == 71042) { // ????????? ?????????
				if (pc.getInventory().checkItem(41060)) { // ?????????????????????
					if (pc.getInventory().checkItem(41062)) { // ??????????????????
						if (pc.getInventory().checkItem(41071) // ????????????
								|| pc.getInventory().checkItem(41072) // ????????????
								|| pc.getInventory().checkItem(41073) // ????????????????????????
								|| pc.getInventory().checkItem(41074) // ????????????????????????
								|| pc.getInventory().checkItem(41075)) { // ??????????????????
							htmlid = "orcfbakumo2";
						} else {
							htmlid = "orcfbakumo8";
						}
					} else {
						htmlid = "orcfbakumo1";
					}
				} else {
					htmlid = "orcfbakumo5";
				}
			} else if (npcid == 71043) { // ?????????-?????? ??????
				if (pc.getInventory().checkItem(41060)) { // ?????????????????????
					if (pc.getInventory().checkItem(41063)) { // ??????????????????
						if (pc.getInventory().checkItem(41076) // ?????????????????????
								|| pc.getInventory().checkItem(41077) // ?????????????????????
								|| pc.getInventory().checkItem(41078) // ?????????????????????
								|| pc.getInventory().checkItem(41079) // ?????????????????????
								|| pc.getInventory().checkItem(41080)) { // ????????????????????????
							htmlid = "orcfbuka2";
						} else {
							htmlid = "orcfbuka8";
						}
					} else {
						htmlid = "orcfbuka1";
					}
				} else {
					htmlid = "orcfbuka5";
				}
			} else if (npcid == 71044) { // ?????????-?????? ??????
				if (pc.getInventory().checkItem(41060)) { // ?????????????????????
					if (pc.getInventory().checkItem(41061)) { // ??????????????????
						if (pc.getInventory().checkItem(41066) // ????????????
								|| pc.getInventory().checkItem(41067) // ????????????
								|| pc.getInventory().checkItem(41068) // ??????????????????
								|| pc.getInventory().checkItem(41069) // ?????????????????????
								|| pc.getInventory().checkItem(41070)) { // ?????????????????????
							htmlid = "orcfkame2";
						} else {
							htmlid = "orcfkame8";
						}
					} else {
						htmlid = "orcfkame1";
					}
				} else {
					htmlid = "orcfkame5";
				}
			} else if (npcid == 71055) { // ????????????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_RESTA) == 3) {
					htmlid = "lukein13";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) == L1Quest.QUEST_END
						&& pc.getQuest().getStep(L1Quest.QUEST_RESTA) == 2
						&& pc.getInventory().checkItem(40631)) {
					htmlid = "lukein10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) == L1Quest.QUEST_END) {
					htmlid = "lukein0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) == 11) {
					if (pc.getInventory().checkItem(40716)) {
						htmlid = "lukein9";
					}
				} else if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) >= 1
						&& pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) <= 10) {
					htmlid = "lukein8";
				}
			} else if (npcid == 71063) { // ????????????-?????????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TBOX1) == L1Quest.QUEST_END) {
				} else if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) == 1) {
					htmlid = "maptbox";
				}
			} else if (npcid == 71064) { // ????????????-2??????-?????????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) == 2) {
					htmlid = talkToSecondtbox(pc);
				}
			} else if (npcid == 71065) { // ????????????-2??????-c??????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) == 3) {
					htmlid = talkToSecondtbox(pc);
				}
			} else if (npcid == 71066) { // ????????????-2??????-d??????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) == 4) {
					htmlid = talkToSecondtbox(pc);
				}
			} else if (npcid == 71067) { // ????????????-3??????-e??????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) == 5) {
					htmlid = talkToThirdtbox(pc);
				}
			} else if (npcid == 71068) { // ????????????-3??????-f??????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) == 6) {
					htmlid = talkToThirdtbox(pc);
				}
			} else if (npcid == 71069) { // ????????????-3??????-g??????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) == 7) {
					htmlid = talkToThirdtbox(pc);
				}
			} else if (npcid == 71070) { // ????????????-3??????-h??????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) == 8) {
					htmlid = talkToThirdtbox(pc);
				}
			} else if (npcid == 71071) { // ????????????-3??????-i??????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) == 9) {
					htmlid = talkToThirdtbox(pc);
				}
			} else if (npcid == 71072) { // ????????????-3??????-j??????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_LUKEIN1) == 10) {
					htmlid = talkToThirdtbox(pc);
				}
			} else if (npcid == 71056) { // ??????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_RESTA) == 4) {
					if (pc.getInventory().checkItem(49277)) {
						htmlid = "SIMIZZ11";
					} else {
						htmlid = "SIMIZZ0";
					}
				} else if (pc.getQuest().getStep(L1Quest.QUEST_SIMIZZ) == 2) {
					htmlid = "SIMIZZ0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_SIMIZZ) == L1Quest.QUEST_END) {
					htmlid = "SIMIZZ15";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_SIMIZZ) == 1) {
					htmlid = "SIMIZZ6";
				}
			} else if (npcid == 71057) { // ????????????????????????1???
				if (pc.getQuest().getStep(L1Quest.QUEST_DOIL) == L1Quest.QUEST_END) {
					htmlid = "doil4b";
				}
			} else if (npcid == 71059) { // ??????????????????????????????2???
				if (pc.getQuest().getStep(L1Quest.QUEST_RUDIAN) == L1Quest.QUEST_END) {
					htmlid = "rudian1c";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_RUDIAN) == 1) {
					htmlid = "rudian7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_DOIL) == L1Quest.QUEST_END) {
					htmlid = "rudian1b";
				} else {
					htmlid = "rudian1a";
				}
			} else if (npcid == 71060) { // ????????????????????????3???
				if (pc.getQuest().getStep(L1Quest.QUEST_RESTA) == L1Quest.QUEST_END) {
					htmlid = "resta1e";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_SIMIZZ) == L1Quest.QUEST_END) {
					htmlid = "resta14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_RESTA) == 4) {
					htmlid = "resta13";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_RESTA) == 3) {
					htmlid = "resta11";
					pc.getQuest().setStep(L1Quest.QUEST_RESTA, 4);
				} else if (pc.getQuest().getStep(L1Quest.QUEST_RESTA) == 2) {
					htmlid = "resta16";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_SIMIZZ) == 2
						&& pc.getQuest().getStep(L1Quest.QUEST_CADMUS) == 1
						|| pc.getInventory().checkItem(40647)) {
					htmlid = "resta1a";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_CADMUS) == 1
						|| pc.getInventory().checkItem(40647)) {
					htmlid = "resta1c";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_SIMIZZ) == 2) {
					htmlid = "resta1b";
				}
			} else if (npcid == 71061) { // ???????????????????????????4???
				if (pc.getQuest().getStep(L1Quest.QUEST_CADMUS) == L1Quest.QUEST_END) {
					htmlid = "cadmus1c";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_CADMUS) == 3) {
					htmlid = "cadmus8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_CADMUS) == 2) {
					htmlid = "cadmus1a";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_DOIL) == L1Quest.QUEST_END) {
					htmlid = "cadmus1b";
				}
			} else if (npcid == 71036) { // ???????????????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_KAMYLA) == L1Quest.QUEST_END) {
					htmlid = "kamyla26";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_KAMYLA) == 4
						&& pc.getInventory().checkItem(40717)) {
					htmlid = "kamyla15";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_KAMYLA) == 4) {
					htmlid = "kamyla14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_KAMYLA) == 3
						&& pc.getInventory().checkItem(40630)) {
					htmlid = "kamyla12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_KAMYLA) == 3) {
					htmlid = "kamyla11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_KAMYLA) == 2
						&& pc.getInventory().checkItem(40644)) {
					htmlid = "kamyla9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_KAMYLA) == 1) {
					htmlid = "kamyla8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_CADMUS) == L1Quest.QUEST_END
						&& pc.getInventory().checkItem(40621)) {
					htmlid = "kamyla1";
				}
			} else if (npcid == 71089) { // ???????????????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_KAMYLA) == 2) {
					htmlid = "francu12";
				}
			} else if (npcid == 71090) { // ????????????????????????2???????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_CRYSTAL) == 1
						&& pc.getInventory().checkItem(40620)) {
					htmlid = "jcrystal2";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_CRYSTAL) == 1) {
					htmlid = "jcrystal3";
				}
			} else if (npcid == 71091) { // ????????????????????????3???????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_CRYSTAL) == 2
						&& pc.getInventory().checkItem(40654)) {
					htmlid = "jcrystall2";
				}
			} else if (npcid == 71074) { // ???????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_LIZARD) == L1Quest.QUEST_END) {
					htmlid = "lelder0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_LIZARD) == 3
						&& pc.getInventory().checkItem(40634)) {
					htmlid = "lelder12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_LIZARD) == 3) {
					htmlid = "lelder11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_LIZARD) == 2
						&& pc.getInventory().checkItem(40633)) {
					htmlid = "lelder7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_LIZARD) == 2) {
					htmlid = "lelder7b";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_LIZARD) == 1) {
					htmlid = "lelder7b";
				} else if (pc.getLevel() >= 40) {
					htmlid = "lelder1";
				}
			} else if (npcid == 71076) { // ??????????????????????????????????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_LIZARD) == L1Quest.QUEST_END) {
					htmlid = "ylizardb";
				} else {
				}
			} else if (npcid == 80079) { // ???????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_KEPLISHA) == L1Quest.QUEST_END
						&& !pc.getInventory().checkItem(41312)) {
					htmlid = "keplisha6";
				} else {
					if (pc.getInventory().checkItem(41314)) { // ????????????????????????
						htmlid = "keplisha3";
					} else if (pc.getInventory().checkItem(41313)) { // ??????????????????
						htmlid = "keplisha2";
					} else if (pc.getInventory().checkItem(41312)) { // ??????????????????
						htmlid = "keplisha4";
					}
				}
			} else if (npcid == 80102) { // ????????????
				if (pc.getInventory().checkItem(41329)) { // ????????????????????????
					htmlid = "fillis3";
				}
			} else if (npcid == 71167) { // ?????????
				if (pc.getTempCharGfx() == 3887) {// ???????????????????????????????????????
					htmlid = "frim1";
				}
			} else if (npcid == 71141) { // ???????????????1
				if (pc.getTempCharGfx() == 3887) {// ???????????????????????????????????????
					htmlid = "moumthree1";
				}
			} else if (npcid == 71142) { // ???????????????2
				if (pc.getTempCharGfx() == 3887) {// ???????????????????????????????????????
					htmlid = "moumtwo1";
				}
			} else if (npcid == 71145) { // ???????????????3
				if (pc.getTempCharGfx() == 3887) {// ???????????????????????????????????????
					htmlid = "moumone1";
				}
			} else if (npcid == 71198) { // ???????????? ????????????
				if (pc.getQuest().getStep(71198) == 1) {
					htmlid = "tion4";
				} else if (pc.getQuest().getStep(71198) == 2) {
					htmlid = "tion5";
				} else if (pc.getQuest().getStep(71198) == 3) {
					htmlid = "tion6";
				} else if (pc.getQuest().getStep(71198) == 4) {
					htmlid = "tion7";
				} else if (pc.getQuest().getStep(71198) == 5) {
					htmlid = "tion5";
				} else if (pc.getInventory().checkItem(21059, 1)) {
					htmlid = "tion19";
				}
			} else if (npcid == 71199) { // ????????????
				if (pc.getQuest().getStep(71199) == 1) {
					htmlid = "jeron3";
				} else if (pc.getInventory().checkItem(21059, 1)
						|| pc.getQuest().getStep(71199) == 255) {
					htmlid = "jeron7";
				}

			} else if (npcid == 81200) { // ???????????????????????????
				if (pc.getInventory().checkItem(21069) // ??????????????????
						|| pc.getInventory().checkItem(21074)) { // ????????????????????????
					htmlid = "c_belt";
				}
			} else if (npcid == 80076) { // ??????????????????
				if (pc.getInventory().checkItem(41058)) { // ????????????????????????
					htmlid = "voyager8";
				} else if (pc.getInventory().checkItem(49082) // ????????????????????????
						|| pc.getInventory().checkItem(49083)) {
					// ???????????????????????????????????????
					if (pc.getInventory().checkItem(41038) // ???????????? 1?????????
							|| pc.getInventory().checkItem(41039) // ????????????
							// 2?????????
							|| pc.getInventory().checkItem(41039) // ????????????
							// 3?????????
							|| pc.getInventory().checkItem(41039) // ????????????
							// 4?????????
							|| pc.getInventory().checkItem(41039) // ????????????
							// 5?????????
							|| pc.getInventory().checkItem(41039) // ????????????
							// 6?????????
							|| pc.getInventory().checkItem(41039) // ????????????
							// 7?????????
							|| pc.getInventory().checkItem(41039) // ????????????
							// 8?????????
							|| pc.getInventory().checkItem(41039) // ????????????
							// 9?????????
							|| pc.getInventory().checkItem(41039)) { // ????????????
						// 10?????????
						htmlid = "voyager9";
					} else {
						htmlid = "voyager7";
					}
				} else if (pc.getInventory().checkItem(49082) // ????????????????????????
						|| pc.getInventory().checkItem(49083)
						|| pc.getInventory().checkItem(49084)
						|| pc.getInventory().checkItem(49085)
						|| pc.getInventory().checkItem(49086)
						|| pc.getInventory().checkItem(49087)
						|| pc.getInventory().checkItem(49088)
						|| pc.getInventory().checkItem(49089)
						|| pc.getInventory().checkItem(49090)
						|| pc.getInventory().checkItem(49091)) {
					// ??????????????????????????????
					htmlid = "voyager7";
				}
			} else if (npcid == 80048) { // ???????????????
				int level = pc.getLevel();
				if (level <= 44) {
					htmlid = "entgate3";
				} else if (level >= 45 && level <= 51) {
					htmlid = "entgate2";
				} else {
					htmlid = "entgate";
				}
			} else if (npcid == 71168) { // ????????? ????????????
				if (pc.getInventory().checkItem(41028)) { // ?????????????????????
					htmlid = "dantes1";
				}
			} else if (npcid == 80067) { // ?????????(???????????????)
				if (pc.getQuest().getStep(L1Quest.QUEST_DESIRE) == L1Quest.QUEST_END) {
					htmlid = "minicod10";
				} else if (pc.getKarmaLevel() >= 1) {
					htmlid = "minicod07";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_DESIRE) == 1
						&& pc.getTempCharGfx() == 6034) { // ??????????????????????????????
					htmlid = "minicod03";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_DESIRE) == 1
						&& pc.getTempCharGfx() != 6034) {
					htmlid = "minicod05";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_SHADOWS) == L1Quest.QUEST_END // ?????????????????????????????????
						|| pc.getInventory().checkItem(41121) // ?????????????????????
						|| pc.getInventory().checkItem(41122)) { // ?????????????????????
					htmlid = "minicod01";
				} else if (pc.getInventory().checkItem(41130) // ??????????????????
						&& pc.getInventory().checkItem(41131)) { // ??????????????????
					htmlid = "minicod06";
				} else if (pc.getInventory().checkItem(41130)) { // ??????????????????
					htmlid = "minicod02";
				}
			} else if (npcid == 81202) { // ?????????(????????????)
				if (pc.getQuest().getStep(L1Quest.QUEST_SHADOWS) == L1Quest.QUEST_END) {
					htmlid = "minitos10";
				} else if (pc.getKarmaLevel() <= -1) {
					htmlid = "minitos07";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_SHADOWS) == 1
						&& pc.getTempCharGfx() == 6035) { // ??????????????????????????????
					htmlid = "minitos03";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_SHADOWS) == 1
						&& pc.getTempCharGfx() != 6035) {
					htmlid = "minitos05";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_DESIRE) == L1Quest.QUEST_END // ????????????????????????????????????
						|| pc.getInventory().checkItem(41130) // ??????????????????
						|| pc.getInventory().checkItem(41131)) { // ??????????????????
					htmlid = "minitos01";
				} else if (pc.getInventory().checkItem(41121) // ?????????????????????
						&& pc.getInventory().checkItem(41122)) { // ?????????????????????
					htmlid = "minitos06";
				} else if (pc.getInventory().checkItem(41121)) { // ?????????????????????
					htmlid = "minitos02";
				}
			} else if (npcid == 81208) { // ?????????????????????
				if (pc.getInventory().checkItem(41129) // ???????????????
						|| pc.getInventory().checkItem(41138)) { // ??????????????????
					htmlid = "minibrob04";
				} else if (pc.getInventory().checkItem(41126) // ???????????????????????????
						&& pc.getInventory().checkItem(41127) // ????????????????????????
						&& pc.getInventory().checkItem(41128) // ????????????????????????
						|| pc.getInventory().checkItem(41135) // ??????????????????????????????
						&& pc.getInventory().checkItem(41136) // ???????????????????????????
						&& pc.getInventory().checkItem(41137)) { // ???????????????????????????
					htmlid = "minibrob02";
				}
			} else if (npcid == 50113) { // ???????????? ???????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "orena14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "orena0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "orena2";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "orena3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "orena4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "orena5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "orena6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "orena7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "orena8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "orena9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "orena10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "orena11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "orena12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "orena13";
				}
			} else if (npcid == 50112) { // ??????????????? ????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "orenb14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "orenb0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "orenb2";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "orenb3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "orenb4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "orenb5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "orenb6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "orenb7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "orenb8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "orenb9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "orenb10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "orenb11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "orenb12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "orenb13";
				}
			} else if (npcid == 50111) { // ???????????? ?????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "orenc14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "orenc1";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "orenc0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "orenc3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "orenc4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "orenc5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "orenc6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "orenc7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "orenc8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "orenc9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "orenc10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "orenc11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "orenc12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "orenc13";
				}
			} else if (npcid == 50116) { // ??????????????? ?????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "orend14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "orend3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "orend1";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "orend0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "orend4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "orend5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "orend6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "orend7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "orend8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "orend9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "orend10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "orend11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "orend12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "orend13";
				}
			} else if (npcid == 50117) { // ????????? ?????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "orene14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "orene3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "orene4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "orene1";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "orene0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "orene5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "orene6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "orene7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "orene8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "orene9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "orene10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "orene11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "orene12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "orene13";
				}
			} else if (npcid == 50119) { // ?????????????????? ???????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "orenf14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "orenf3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "orenf4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "orenf5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "orenf1";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "orenf0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "orenf6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "orenf7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "orenf8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "orenf9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "orenf10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "orenf11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "orenf12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "orenf13";
				}
			} else if (npcid == 50121) { // ????????? ????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "oreng14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "oreng3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "oreng4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "oreng5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "oreng6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "oreng1";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "oreng0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "oreng7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "oreng8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "oreng9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "oreng10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "oreng11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "oreng12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "oreng13";
				}
			} else if (npcid == 50114) { // ??????????????? ??????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "orenh14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "orenh3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "orenh4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "orenh5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "orenh6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "orenh7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "orenh1";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "orenh0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "orenh8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "orenh9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "orenh10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "orenh11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "orenh12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "orenh13";
				}
			} else if (npcid == 50120) { // ?????????????????????????????? ?????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "oreni14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "oreni3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "oreni4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "oreni5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "oreni6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "oreni7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "oreni8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "oreni1";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "oreni0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "oreni9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "oreni10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "oreni11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "oreni12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "oreni13";
				}
			} else if (npcid == 50122) { // ????????? ?????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "orenj14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "orenj3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "orenj4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "orenj5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "orenj6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "orenj7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "orenj8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "orenj9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "orenj1";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "orenj0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "orenj10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "orenj11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "orenj12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "orenj13";
				}
			} else if (npcid == 50123) { // ????????? ????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "orenk14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "orenk3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "orenk4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "orenk5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "orenk6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "orenk7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "orenk8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "orenk9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "orenk10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "orenk1";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "orenk0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "orenk11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "orenk12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "orenk13";
				}
			} else if (npcid == 50125) { // ???????????? ???????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "orenl14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "orenl3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "orenl4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "orenl5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "orenl6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "orenl7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "orenl8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "orenl9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "orenl10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "orenl11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "orenl1";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "orenl0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "orenl12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "orenl13";
				}
			} else if (npcid == 50124) { // ??????????????? ???????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "orenm14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "orenm3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "orenm4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "orenm5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "orenm6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "orenm7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "orenm8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "orenm9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "orenm10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "orenm11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "orenm12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "orenm1";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "orenm0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "orenm13";
				}
			} else if (npcid == 50126) { // ????????? ???????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "orenn14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "orenn3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "orenn4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "orenn5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "orenn6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "orenn7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "orenn8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "orenn9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "orenn10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "orenn11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "orenn12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "orenn13";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "orenn1";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "orenn0";
				}
			} else if (npcid == 50115) { // ??????????????? ????????????
				if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == L1Quest.QUEST_END) {
					htmlid = "oreno0";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 1) {
					htmlid = "oreno3";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 2) {
					htmlid = "oreno4";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 3) {
					htmlid = "oreno5";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 4) {
					htmlid = "oreno6";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 5) {
					htmlid = "oreno7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 6) {
					htmlid = "oreno8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 7) {
					htmlid = "oreno9";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 8) {
					htmlid = "oreno10";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 9) {
					htmlid = "oreno11";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 10) {
					htmlid = "oreno12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 11) {
					htmlid = "oreno13";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 12) {
					htmlid = "oreno14";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_TOSCROLL) == 13) {
					htmlid = "oreno1";
				}
			} else if (npcid == 71256) { // ??????????????????
				if (!pc.isElf()) {
					htmlid = "robinhood2";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 255) {
					htmlid = "robinhood12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 8) {
					if (pc.getInventory().checkItem(40491, 30)
							&& pc.getInventory().checkItem(40495, 40)
							&& pc.getInventory().checkItem(100, 1)
							&& pc.getInventory().checkItem(40509, 12)
							&& pc.getInventory().checkItem(40052, 1)
							&& pc.getInventory().checkItem(40053, 1)
							&& pc.getInventory().checkItem(40054, 1)
							&& pc.getInventory().checkItem(40055, 1)
							&& pc.getInventory().checkItem(41347, 1)
							&& pc.getInventory().checkItem(41350, 1)) {
						htmlid = "robinhood11";
					} else if (pc.getInventory().checkItem(40491, 30)
							&& pc.getInventory().checkItem(40495, 40)
							&& pc.getInventory().checkItem(100, 1)
							&& pc.getInventory().checkItem(40509, 12)) {
						htmlid = "robinhood16";
					} else if ((!(pc.getInventory().checkItem(40491, 30)
							&& pc.getInventory().checkItem(40495, 40)
							&& pc.getInventory().checkItem(100, 1) && pc
							.getInventory().checkItem(40509, 12)))) {
						htmlid = "robinhood17";
					}
				} else if (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 7) {
					if (pc.getInventory().checkItem(41352, 4)
							&& pc.getInventory().checkItem(40618, 30)
							&& pc.getInventory().checkItem(40643, 30)
							&& pc.getInventory().checkItem(40645, 30)
							&& pc.getInventory().checkItem(40651, 30)
							&& pc.getInventory().checkItem(40676, 30)
							&& pc.getInventory().checkItem(40514, 20)
							&& pc.getInventory().checkItem(41351, 1)
							&& pc.getInventory().checkItem(41346, 1)) {
						htmlid = "robinhood9";
					} else if (pc.getInventory().checkItem(41351, 1)
							&& pc.getInventory().checkItem(41352, 4)) {
						htmlid = "robinhood14";
					} else if (pc.getInventory().checkItem(41351, 1)
							&& (!(pc.getInventory().checkItem(41352, 4)))) {
						htmlid = "robinhood15";
					} else if (pc.getInventory().checkItem(41351)) {
						htmlid = "robinhood9";
					} else {
						htmlid = "robinhood18";
					}
				} else if ((pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 2)
						|| (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 3)
						|| (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 4)
						|| (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 5)
						|| (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 6)) {
					htmlid = "robinhood13";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 1) {
					htmlid = "robinhood8";
				} else {
					htmlid = "robinhood1";
				}
			} else if (npcid == 71257) { // ????????????
				if (!pc.isElf()) {
					htmlid = "zybril16";
				} else if ((pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) >= 7)) {
					htmlid = "zybril19";
				} else if (pc.getInventory().checkItem(41349)
						&& (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 7)) {
					htmlid = "zybril19";
				} else if (pc.getInventory().checkItem(41349)
						&& (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 6)) {
					htmlid = "zybril18";
				} else if ((pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 6)
						&& (!(pc.getInventory().checkItem(41354)))) {
					htmlid = "zybril7";
				} else if ((pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 6)
						&& pc.getInventory().checkItem(41354)) {
					htmlid = "zybril17";
				} else if (pc.getInventory().checkItem(41353)
						&& pc.getInventory().checkItem(40514, 10)
						&& pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 5) {
					htmlid = "zybril8";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 5) {
					htmlid = "zybril13";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 4
						&& pc.getInventory().checkItem(40048, 10)
						&& pc.getInventory().checkItem(40049, 10)
						&& pc.getInventory().checkItem(40050, 10)
						&& pc.getInventory().checkItem(40051, 10)) {
					htmlid = "zybril7";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 4) {
					htmlid = "zybril12";
				} else if (pc.getQuest().getStep(L1Quest.QUEST_MOONOFLONGBOW) == 3) {
					htmlid = "zybril3";
				} else if ((pc.isElf())
						&& ((pc.getQuest()
								.getStep(L1Quest.QUEST_MOONOFLONGBOW) == 2) || (pc
								.getQuest().getStep(
										L1Quest.QUEST_MOONOFLONGBOW) == 1))) {
					htmlid = "zybril1";
				} else {
					htmlid = "zybril1";
				}
			} else if (npcid == 71258) { // ?????????
				if (pc.getLawful() <= -501) {
					htmlid = "marba1";
				} else if (pc.isCrown() || pc.isDarkelf() || pc.isKnight()
						|| pc.isWizard() || pc.isDragonKnight()
						|| pc.isIllusionist()) {
					htmlid = "marba2";
				} else if (pc.getInventory().checkItem(40665)
						&& (pc.getInventory().checkItem(40693)
								|| pc.getInventory().checkItem(40694)
								|| pc.getInventory().checkItem(40695)
								|| pc.getInventory().checkItem(40697)
								|| pc.getInventory().checkItem(40698) || pc
								.getInventory().checkItem(40699))) {
					htmlid = "marba8";
				} else if (pc.getInventory().checkItem(40665)) {
					htmlid = "marba17";
				} else if (pc.getInventory().checkItem(40664)) {
					htmlid = "marba19";
				} else if (pc.getInventory().checkItem(40637)) {
					htmlid = "marba18";
				} else {
					htmlid = "marba3";
				}
			} else if (npcid == 71259) { // ?????????
				if (pc.getLawful() <= -501) {
					htmlid = "aras12";
				} else if (pc.isCrown() || pc.isDarkelf() || pc.isKnight()
						|| pc.isWizard() || pc.isDragonKnight()
						|| pc.isIllusionist()) {
					htmlid = "aras11";
				} else if (pc.getInventory().checkItem(40665)
						&& (pc.getInventory().checkItem(40679)
								|| pc.getInventory().checkItem(40680)
								|| pc.getInventory().checkItem(40681)
								|| pc.getInventory().checkItem(40682)
								|| pc.getInventory().checkItem(40683) || pc
								.getInventory().checkItem(40684))) {
					htmlid = "aras3";
				} else if (pc.getInventory().checkItem(40665)) {
					htmlid = "aras8";
				} else if (pc.getInventory().checkItem(40679)
						|| pc.getInventory().checkItem(40680)
						|| pc.getInventory().checkItem(40681)
						|| pc.getInventory().checkItem(40682)
						|| pc.getInventory().checkItem(40683)
						|| pc.getInventory().checkItem(40684)
						|| pc.getInventory().checkItem(40693)
						|| pc.getInventory().checkItem(40694)
						|| pc.getInventory().checkItem(40695)
						|| pc.getInventory().checkItem(40697)
						|| pc.getInventory().checkItem(40698)
						|| pc.getInventory().checkItem(40699)) {
					htmlid = "aras3";
				} else if (pc.getInventory().checkItem(40664)) {
					htmlid = "aras6";
				} else if (pc.getInventory().checkItem(40637)) {
					htmlid = "aras1";
				} else {
					htmlid = "aras7";
				}
			} else if (npcid == 70838) { // ????????????
				if (pc.isCrown() || pc.isKnight() || pc.isWizard()
						|| pc.isDragonKnight() || pc.isIllusionist()) {
					htmlid = "nerupam1";
				} else if (pc.isDarkelf() && (pc.getLawful() <= -1)) {
					htmlid = "nerupaM2";
				} else if (pc.isDarkelf()) {
					htmlid = "nerupace1";
				} else if (pc.isElf()) {
					htmlid = "nerupae1";
				}
			} else if (npcid == 80094) { // ??????
				if (pc.isIllusionist()) {
					htmlid = "altar1";
				} else if (!pc.isIllusionist()) {
					htmlid = "altar2";
				}
			} else if (npcid == 80099) { // ????????????????????????
				if (pc.getQuest().getStep(
						L1Quest.QUEST_GENERALHAMELOFRESENTMENT) == 1) {
					if (pc.getInventory().checkItem(41325, 1)) {
						htmlid = "rarson8";
					} else {
						htmlid = "rarson10";
					}
				} else if (pc.getQuest().getStep(
						L1Quest.QUEST_GENERALHAMELOFRESENTMENT) == 2) {
					if (pc.getInventory().checkItem(41317, 1)
							&& pc.getInventory().checkItem(41315, 1)) {
						htmlid = "rarson13";
					} else {
						htmlid = "rarson19";
					}
				} else if (pc.getQuest().getStep(
						L1Quest.QUEST_GENERALHAMELOFRESENTMENT) == 3) {
					htmlid = "rarson14";
				} else if (pc.getQuest().getStep(
						L1Quest.QUEST_GENERALHAMELOFRESENTMENT) == 4) {
					if (!(pc.getInventory().checkItem(41326, 1))) {
						htmlid = "rarson18";
					} else if (pc.getInventory().checkItem(41326, 1)) {
						htmlid = "rarson11";
					} else {
						htmlid = "rarson17";
					}
				} else if (pc.getQuest().getStep(
						L1Quest.QUEST_GENERALHAMELOFRESENTMENT) >= 5) {
					htmlid = "rarson1";
				}
			} else if (npcid == 80101) { // ?????????
				if (pc.getQuest().getStep(
						L1Quest.QUEST_GENERALHAMELOFRESENTMENT) == 4) {
					if ((pc.getInventory().checkItem(41315, 1))
							&& pc.getInventory().checkItem(40494, 30)
							&& pc.getInventory().checkItem(41317, 1)) {
						htmlid = "kuen4";
					} else if (pc.getInventory().checkItem(41316, 1)) {
						htmlid = "kuen1";
					} else if (!pc.getInventory().checkItem(41316)) {
						pc.getQuest().setStep(
								L1Quest.QUEST_GENERALHAMELOFRESENTMENT, 1);
					}
				} else if ((pc.getQuest().getStep(
						L1Quest.QUEST_GENERALHAMELOFRESENTMENT) == 2)
						&& (pc.getInventory().checkItem(41317, 1))) {
					htmlid = "kuen3";
				} else {
					htmlid = "kuen1";
				}
			} else if (npcid == 80134) { // ????????????
				if (pc.isDragonKnight()) { // ?????????????????????
					int lv30_step = quest.getStep(L1Quest.QUEST_LEVEL30);
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					int lv50_step = quest.getStep(L1Quest.QUEST_LEVEL50);
					if (pc.getLevel() >= 30 && lv30_step == 2) {
						htmlid = "talrion1";
					} else if (pc.getLevel() >= 45 && lv45_step == 5) {
						htmlid = "talrion9";
					} else if ((pc.getLevel() >= 50) && (lv50_step == 4)) {
						htmlid = "talrion10";
					} else {
						htmlid = "talrion4";
					}
				}
			} else if (npcid == 80135) { // ????????????
				if (pc.isDragonKnight()) { // ?????????????????????
					int lv30_step = quest.getStep(L1Quest.QUEST_LEVEL30);
					if (lv30_step == L1Quest.QUEST_END) {
						htmlid = "elas6";
					} else if (pc.getLevel() >= 30 && lv30_step >= 1) {
						htmlid = "elas1";
					}
				}
			} else if (npcid == 80136) { // ?????? ????????????
				int lv15_step = quest.getStep(L1Quest.QUEST_LEVEL15);
				int lv30_step = quest.getStep(L1Quest.QUEST_LEVEL30);
				int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
				int lv50_step = quest.getStep(L1Quest.QUEST_LEVEL50);
				if (pc.isDragonKnight()) { // ?????????????????????
					if (pc.getLevel() >= 50 && lv45_step == L1Quest.QUEST_END) {
						if (lv50_step == 0) {
							htmlid = "prokel21";
						} else if (lv50_step > 3) { // ???????????????
							htmlid = "prokel32";
						} else if (lv50_step > 1) {
							htmlid = "prokel25";
						} else {
							htmlid = "prokel24";
						}
					} else if (pc.getLevel() >= 45
							&& lv30_step == L1Quest.QUEST_END) {
						if (lv45_step == 0) {
							htmlid = "prokel15";
						} else if (lv45_step >= 5) { // ???????????????
							htmlid = "prokel20";
						} else {
							htmlid = "prokel17";
						}
					} else if (pc.getLevel() >= 30
							&& lv15_step == L1Quest.QUEST_END) {
						if (lv30_step == 0) {
							htmlid = "prokel8";
						} else if (lv30_step >= 2) { // ???????????????
							htmlid = "prokel14";
						} else {
							htmlid = "prokel10";
						}
					} else if (pc.getLevel() >= 15) {
						if (lv15_step == 0) {
							htmlid = "prokel2";
						} else if (lv15_step == L1Quest.QUEST_END) { // ???????????????)
							htmlid = "prokel7";
						} else {
							htmlid = "prokel4";
						}
					} else { // Lv15??????
						htmlid = "prokel1";
					}
				}
			} else if (npcid == 80145) { // ?????? ???????????????
				int lv15_step = quest.getStep(L1Quest.QUEST_LEVEL15);
				int lv30_step = quest.getStep(L1Quest.QUEST_LEVEL30);
				int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
				int lv50_step = quest.getStep(L1Quest.QUEST_LEVEL50);
				if (pc.isDragonKnight()) { // ?????????????????????
					if (pc.getLevel() >= 45 && lv45_step == 1) {
						htmlid = "silrein37";
					} else if (pc.getLevel() >= 45 && lv45_step == 2) {
						htmlid = "silrein38";
					} else if (pc.getLevel() >= 45 && lv45_step == 3) {
						htmlid = "silrein40";
					} else if (pc.getLevel() >= 45 && lv45_step == 4) {
						htmlid = "silrein43";
					}
				} else if (pc.isIllusionist()) { // ???????????????????????????
					if (pc.getLevel() >= 50 && lv45_step == L1Quest.QUEST_END) {
						if (lv50_step == 0) {
							htmlid = "silrein27";
						} else if (lv50_step > 4) {
							htmlid = "silrein36";
						} else if (lv50_step > 3) {
							htmlid = "silrein35";
						} else if (lv50_step > 2) {
							if (pc.getInventory().checkItem(49206)) {
								htmlid = "silrein33";
							}
						} else if (lv50_step > 1) {
							if (pc.getInventory().checkItem(49178)
									&& pc.getInventory().checkItem(49202)) {
								htmlid = "silrein32";
							} else {
								htmlid = "silrein34";
							}
						} else if (lv50_step > 0) {
							htmlid = "silrein29";
						} else {
							htmlid = "silrein26";
						}
					} else if (pc.getLevel() >= 45
							&& lv30_step == L1Quest.QUEST_END) {
						if (lv45_step == 0) {
							htmlid = "silrein18";
						} else if (lv45_step == L1Quest.QUEST_END) {
							htmlid = "silrein26";
						} else if (lv45_step > 4) {
							if (pc.getInventory().checkItem(49202)) {
								htmlid = "silrein23";
							} else {
								htmlid = "silrein24";
							}
						} else if (lv45_step > 0) {
							if (pc.getInventory().checkItem(49194)
									&& pc.getInventory().checkItem(49195)
									&& pc.getInventory().checkItem(49196)) {
								htmlid = "silrein20";
							} else {
								htmlid = "silrein21";
							}
						} else {
							htmlid = "silrein19";
						}
					} else if (pc.getLevel() >= 30
							&& lv15_step == L1Quest.QUEST_END) {
						if (lv30_step == 0) {
							htmlid = "silrein11";
						} else if (lv30_step == L1Quest.QUEST_END) {
							htmlid = "silrein15";
						} else if (lv30_step > 0) {
							htmlid = "silrein14";
						} else {
							htmlid = "silrein10";
						}
					} else if (pc.getLevel() >= 15) {
						if (lv15_step == 0) {
							htmlid = "silrein2";
						} else if (lv15_step == L1Quest.QUEST_END) {
							htmlid = "silrein5";
						} else {
							htmlid = "silrein4";
						}
					} else {
						htmlid = "silrein1";
					}
				}
			} else if (npcid == 81247) { // ??????????????????????????????(1)
				if (pc.isIllusionist()) {
					int lv30_step = quest.getStep(L1Quest.QUEST_LEVEL30);
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (pc.getLevel() >= 45
							&& lv30_step == L1Quest.QUEST_END) {
						if (lv45_step == 1) {
							htmlid = "wcorpse2";
						} else {
							htmlid = "wcorpse1";
						}
					}
				}
			} else if (npcid == 81248) { // ??????????????????????????????(2)
				if (pc.isIllusionist()) {
					int lv30_step = quest.getStep(L1Quest.QUEST_LEVEL30);
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (pc.getLevel() >= 45
							&& lv30_step == L1Quest.QUEST_END) {
						if (lv45_step == 2) {
							htmlid = "wcorpse5";
						} else {
							htmlid = "wcorpse4";
						}
					}
				}
			} else if (npcid == 81249) { // ??????????????????????????????(3)
				if (pc.isIllusionist()) {
					int lv30_step = quest.getStep(L1Quest.QUEST_LEVEL30);
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (pc.getLevel() >= 45
							&& lv30_step == L1Quest.QUEST_END) {
						if (lv45_step == 3) {
							htmlid = "wcorpse8";
						} else {
							htmlid = "wcorpse7";
						}
					}
				}
			} else if (npcid == 81250) { // ??????????????????????????????(???)
				if (pc.isIllusionist()) {
					int lv30_step = quest.getStep(L1Quest.QUEST_LEVEL30);
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (pc.getLevel() >= 45
							&& lv30_step == L1Quest.QUEST_END) {
						if (lv45_step == 5) {
							htmlid = "wa_earth2";
						} else {
							htmlid = "wa_earth1";
						}
					}
				}
			} else if (npcid == 81251) { // ??????????????????????????????(?????????)
				if (pc.isIllusionist()) {
					int lv30_step = quest.getStep(L1Quest.QUEST_LEVEL30);
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (pc.getLevel() >= 45
							&& lv30_step == L1Quest.QUEST_END) {
						if (lv45_step == 6) {
							htmlid = "wa_acidw2";
						} else {
							htmlid = "wa_acidw1";
						}
					}
				}
			} else if (npcid == 81252) { // ??????????????????????????????(?????????)
				if (pc.isIllusionist()) {
					int lv30_step = quest.getStep(L1Quest.QUEST_LEVEL30);
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					if (pc.getLevel() >= 45
							&& lv30_step == L1Quest.QUEST_END) {
						if (lv45_step == 7) {
							htmlid = "wa_egg2";
						} else {
							htmlid = "wa_egg1";
						}
					}
				}
			} else if (npcid == 91314) { // ???????????????
				if (pc.isIllusionist()) {
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					int lv50_step = quest.getStep(L1Quest.QUEST_LEVEL50);
					if (pc.getLevel() > 49
							&& lv45_step == L1Quest.QUEST_END) {
						if (lv50_step == 3) {
							htmlid = "bluesoul_f2";
						} else {
							htmlid = "bluesoul_f1";
						}
					}
				}
			} else if (npcid == 91315) { // ???????????????
				if (pc.isDragonKnight()) {
					int lv45_step = quest.getStep(L1Quest.QUEST_LEVEL45);
					int lv50_step = quest.getStep(L1Quest.QUEST_LEVEL50);
					if (pc.getLevel() > 49 && lv45_step == L1Quest.QUEST_END) {
						if (lv50_step == 3) {
							htmlid = "redsoul_f2";
						} else {
							htmlid = "redsoul_f1";
						}
					}
				}
			} else if (npcid == 81245) { // ???????????????(HC3)
				if (pc.isDragonKnight()) {
					if (pc.getTempCharGfx() == 6984) { // ?????????????????????
						int lv30_step = pc.getQuest().getStep(
								L1Quest.QUEST_LEVEL30);
						if (lv30_step == 1) {
							htmlid = "spy_orc1";
						}
					}
				}
			} else if (npcid == 91050) { // ??????????????????
				if (pc.getInventory().checkItem(50500, 1)) {
					htmlid = "veil3";
				} else if (pc.getInventory().checkItem(50501)) {
					htmlid = "veil8";
				}
			} else if (npcid == 91327) { // ?????????
				if (pc.getQuest().getStep(L1Quest.QUEST_YURIE) == L1Quest.QUEST_END
						&& !pc.getInventory().checkItem(50006)) {
					htmlid = "j_html03";
				} else {
					if (pc.getQuest().getStep(L1Quest.QUEST_YURIE) == 0
							&& !pc.getInventory().checkItem(50006)) {
						htmlid = "j_html01";
					}
				}
			} else if (npcid == 70035 || npcid == 70041 || npcid == 70042) { // ???????????????????????????(???????????????????????????????????????)
				// STATUS_NONE = 0; STATUS_READY = 1; STATUS_PLAYING = 2;
				// STATUS_END = 3;
				if (L1BugBearRace.getInstance().getGameStatus() == 0) {
					htmlid = "maeno5";
				} else if (L1BugBearRace.getInstance().getGameStatus() == 1) {
					htmlid = "maeno1";
				} else if (L1BugBearRace.getInstance().getGameStatus() == 2) {
					htmlid = "maeno3";
				} else if (L1BugBearRace.getInstance().getGameStatus() == 3) {
					htmlid = "maeno5";
				}
			} else if (npcid == 80192) { // ?????????????????? ???????????????
				if (pc.getInventory().checkItem(49031) // ????????????????????????
						&& pc.getInventory().checkItem(21088)) { // ???????????????????????????????????????????????????
					htmlid = "gemout8";
				} else if (pc.getInventory().checkItem(49031) // ????????????????????????
						&& pc.getInventory().checkItem(21087)) { // ???????????????????????????????????????????????????
					htmlid = "gemout7";
				} else if (pc.getInventory().checkItem(49031) // ????????????????????????
						&& pc.getInventory().checkItem(21086)) { // ???????????????????????????????????????????????????
					htmlid = "gemout6";
				} else if (pc.getInventory().checkItem(49031) // ????????????????????????
						&& pc.getInventory().checkItem(21085)) { // ???????????????????????????????????????????????????
					htmlid = "gemout5";
				} else if (pc.getInventory().checkItem(49031) // ????????????????????????
						&& pc.getInventory().checkItem(21084)) { // ???????????????????????????????????????????????????
					htmlid = "gemout4";
				} else if (pc.getInventory().checkItem(49031) // ????????????????????????
						&& pc.getInventory().checkItem(21083)) { // ???????????????????????????????????????????????????
					htmlid = "gemout3";
				} else if (pc.getInventory().checkItem(49031) // ????????????????????????
						&& pc.getInventory().checkItem(21082)) { // ???????????????????????????????????????????????????
					htmlid = "gemout2";
				} else if (pc.getInventory().checkItem(49031) // ????????????????????????
						&& pc.getInventory().checkItem(21081)) { // ???????????????????????????????????????????????????
					htmlid = "gemout1";
				} else if (pc.getInventory().checkItem(49031)) { // ????????????????????????
					htmlid = "gemout17";
				} else {
					htmlid = "8event3";
				}
			} else if (npcid == 80238) { // ?????????
				if (pc.getInventory().checkItem(50537, 1) || pc.getInventory().checkItem(50538, 1)) {
					// ???????????????????????????????????????????????????
					htmlid = "hiren3";
				} else {
					htmlid = "hiren1";
				}
			} else if (npcid == 80239) { // ?????????
				if (pc.getInventory().checkItem(50538, 1)) { // ?????????????????????
					htmlid = "jeff1";
				} else if (pc.getInventory().checkItem(50537, 1)) { // ???????????????????????????
					htmlid = "jeff2";
				} else {
					htmlid = "arsia";
				}
			} else if (npcid == 80242) { // ???????????????
				if (L1GameTimeClock.getInstance().currentTime().isNight()) { // ???
					htmlid = "tearfairy2";
				} else { // ???
					htmlid = "tearfairy1";
				}
			} else if (npcid == 80171) { // ??????????????????
				if (pc.getLevel() < 13) {
					talkToBeginnersGuide(pc, npcid);
					if (pc.isDarkelf()) {
						htmlid = "tutord";
					} else if (pc.isDragonKnight()) {
						htmlid = "tutordk";
					} else if (pc.isElf()) {
						htmlid = "tutore";
					} else if (pc.isIllusionist()) {
						htmlid = "tutori";
					} else if (pc.isKnight()) {
						htmlid = "tutork";
					} else if (pc.isWizard()) {
						htmlid = "tutorm";
					} else if (pc.isCrown()) {
						htmlid = "tutorp";
					}
				} else {
					htmlid = "tutorend";
				}
			} else if (npcid == 80172) { // ??????????????????
				int level = pc.getLevel();
				if (level < 13) {
					talkToBeginnersGuide(pc, npcid);
					if (level > 4) {
						htmlid = "admin3";
					} else {
						htmlid = "admin2";
					}
				} else {
					htmlid = "admin1";
				}
			} else if (npcid == 80179) { // ?????????
				int level = pc.getLevel();
				if (level < 5) {
					if (quest.getStep(L1Quest.QUEST_NEWBIE) == 0) {
						L1ItemInstance item =
								pc.getInventory().storeItem(50546, 1); // ??????????????????
						if (item != null) {
							pc.sendPackets(new S_ServerMessage(143,
									getNpcTemplate().getName(), item.getLogName())); // \f1%0???%1?????????????????????
						}
						quest.setStep(L1Quest.QUEST_NEWBIE, 1);
						htmlid = "j_nb01";
					} else {
						htmlid = "j_nb05";
					}
				} else if (level < 31) {
					htmlid = "j_nb02";
				} else {
					if (quest.getStep(L1Quest.QUEST_NEWBIE) != L1Quest.QUEST_END) {
						if (pc.getInventory().checkItem(50546)) { // ??????????????????
							pc.getInventory().consumeItem(50546, 1);
						}
						L1ItemInstance item =
								pc.getInventory().storeItem(50548, 1); // ???????????????
						if (item != null) {
							pc.sendPackets(new S_ServerMessage(143,
									getNpcTemplate().getName(), item.getLogName())); // \f1%0???%1?????????????????????
						}
						quest.setStep(L1Quest.QUEST_NEWBIE, L1Quest.QUEST_END);
						htmlid = "j_nb98";
					} else {
						htmlid = "j_nb99";
					}
				}
			} else if (npcid == 80180) { // ???????????????
				int level = pc.getLevel();
				if (level < 13) {
					htmlid = "lowlvS1";
				} else if (level > 12 && level < 46) {
					htmlid = "lowlvS2";
				} else {
					htmlid = "lowlvno";
				}
		
				/*
				 * ????????????????????? } else if (npcid >= 81273 && npcid <= 81276) { //
				 * ???????????? switch (npcid) { case 81273: // ??????????????? if
				 * (getRBpcCount((short) 1005) < 32) {
				 * L1Teleport.teleport(pc, 32599, 32742, (short) 1005, 5,
				 * true); } else { pc.sendPackets(new
				 * S_ServerMessage(1229)); } break; case 81274: // ??????????????? if
				 * (getRBpcCount((short) 1011) < 32) {
				 * L1Teleport.teleport(pc, 32927, 32741, (short) 1011, 5,
				 * true); } else { pc.sendPackets(new
				 * S_ServerMessage(1229)); } break; case 81275: // ????????? ???Lindvior
				 * if (getRBpcCount((short) 1005) < 32) {
				 * L1Teleport.teleport(pc, 32599, 32742, (short) 1005, 5,
				 * true); } else { pc.sendPackets(new
				 * S_ServerMessage(1229)); } break; case 81276: // ????????????Valakas
				 * if (getRBpcCount((short) 1005) < 32) {
				 * L1Teleport.teleport(pc, 32599, 32742, (short) 1005, 5,
				 * true); } else { pc.sendPackets(new
				 * S_ServerMessage(1229)); } }
				 */
			} else if (npcid == 70762) {
				if (pc.getLevel() >= 50 && pc.isDarkelf()) {
					htmlid = "karif1";
				} else {
					htmlid = "karif9";
				}
			}

			// html????????????????????????
			if (htmlid != null) { // htmlid??????????????????????????????
				if (htmldata != null) { // html??????????????????????????????
					pc
							.sendPackets(new S_NpcTalkReturn(objid, htmlid,
									htmldata));
				} else {
					pc.sendPackets(new S_NpcTalkReturn(objid, htmlid));
				}
			} else {
				if (pc.getLawful() < -1000) { // ????????????????????????????????????
					pc.sendPackets(new S_NpcTalkReturn(talking, objid, 2));
				} else {
					pc.sendPackets(new S_NpcTalkReturn(talking, objid, 1));
				}
			}
		}
	}

	private static String talkToTownadviser(L1PcInstance pc, int town_id) {
		String htmlid;
		if (pc.getHomeTownId() == town_id) {
			htmlid = "artisan1";
		} else {
			htmlid = "artisan2";
		}

		return htmlid;
	}

	private static String talkToTownmaster(L1PcInstance pc, int town_id) {
		String htmlid;
		if (pc.getHomeTownId() == town_id) {
			htmlid = "hometown";
		} else {
			htmlid = "othertown";
		}
		return htmlid;
	}

	@Override
	public void onFinalAction(L1PcInstance pc, String action) {
	}

	public void doFinalAction(L1PcInstance pc) {
	}

	private boolean checkHasCastle(L1PcInstance pc, int castle_id) {
		if (pc.getClanId() != 0) { // ??????????????????
			L1Clan clan = L1World.getInstance().getClan(pc.getClanName());
			if (clan != null) {
				if (clan.getCastleId() == castle_id) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkClanLeader(L1PcInstance pc) {
		if (pc.isCrown()) { // ??????
			L1Clan clan = L1World.getInstance().getClan(pc.getClanName());
			if (clan != null) {
				if (pc.getId() == clan.getLeaderId()) {
					return true;
				}
			}
		}
		return false;
	}

	private int getNecessarySealCount(L1PcInstance pc) {
		int rulerCount = 0;
		int necessarySealCount = 10;
		if (pc.getInventory().checkItem(40917)) { // ???????????????
			rulerCount++;
		}
		if (pc.getInventory().checkItem(40920)) { // ???????????????
			rulerCount++;
		}
		if (pc.getInventory().checkItem(40918)) { // ???????????????
			rulerCount++;
		}
		if (pc.getInventory().checkItem(40919)) { // ???????????????
			rulerCount++;
		}
		if (rulerCount == 0) {
			necessarySealCount = 10;
		} else if (rulerCount == 1) {
			necessarySealCount = 100;
		} else if (rulerCount == 2) {
			necessarySealCount = 200;
		} else if (rulerCount == 3) {
			necessarySealCount = 500;
		}
		return necessarySealCount;
	}

	private void createRuler(L1PcInstance pc, int attr, int sealCount) {
		// 1.?????????,2.?????????,4.?????????,8.?????????
		int rulerId = 0;
		int protectionId = 0;
		int sealId = 0;
		if (attr == 1) {
			rulerId = 40917;
			protectionId = 40909;
			sealId = 40913;
		} else if (attr == 2) {
			rulerId = 40919;
			protectionId = 40911;
			sealId = 40915;
		} else if (attr == 4) {
			rulerId = 40918;
			protectionId = 40910;
			sealId = 40914;
		} else if (attr == 8) {
			rulerId = 40920;
			protectionId = 40912;
			sealId = 40916;
		}
		pc.getInventory().consumeItem(protectionId, 1);
		pc.getInventory().consumeItem(sealId, sealCount);
		L1ItemInstance item = pc.getInventory().storeItem(rulerId, 1);
		if (item != null) {
			pc.sendPackets(new S_ServerMessage(143,
					getNpcTemplate().getName(), item.getLogName())); // \f1%0???%1?????????????????????
		}
	}

	private String talkToDoromond(L1PcInstance pc) {
		String htmlid = "";
		if (pc.getQuest().getStep(L1Quest.QUEST_DOROMOND) == 0) {
			htmlid = "jpe0011";
		} else if (pc.getQuest().getStep(L1Quest.QUEST_DOROMOND) == 1) {
			htmlid = "jpe0015";
		}

		return htmlid;
	}

	private String talkToAlex(L1PcInstance pc) {
		String htmlid = "";
		if (pc.getLevel() < 3) {
			htmlid = "jpe0021";
		} else if (pc.getQuest().getStep(L1Quest.QUEST_DOROMOND) < 2) {
			htmlid = "jpe0022";
		} else if (pc.getQuest().getStep(L1Quest.QUEST_AREX) == L1Quest.QUEST_END) {
			htmlid = "jpe0023";
		} else if (pc.getLevel() >= 10 && pc.getLevel() < 25) {
			if (pc.getInventory().checkItem(41227)) { // ???????????????????????????
				htmlid = "jpe0023";
			} else if (pc.isCrown()) {
				htmlid = "jpe0024p";
			} else if (pc.isKnight()) {
				htmlid = "jpe0024k";
			} else if (pc.isElf()) {
				htmlid = "jpe0024e";
			} else if (pc.isWizard()) {
				htmlid = "jpe0024w";
			} else if (pc.isDarkelf()) {
				htmlid = "jpe0024d";
			}
		} else if (pc.getLevel() > 25) {
			htmlid = "jpe0023";
		} else {
			htmlid = "jpe0021";
		}
		return htmlid;
	}

	private String talkToAlexInTrainingRoom(L1PcInstance pc) {
		String htmlid = "";
		if (pc.getLevel() < 3) {
			htmlid = "jpe0031";
		} else {
			if (pc.getQuest().getStep(L1Quest.QUEST_DOROMOND) < 2) {
				htmlid = "jpe0035";
			} else {
				htmlid = "jpe0036";
			}
		}

		return htmlid;
	}

	private String cancellation(L1PcInstance pc) {
		String htmlid = "";
		if (pc.getLevel() < 13) {
			htmlid = "jpe0161";
		} else {
			htmlid = "jpe0162";
		}

		return htmlid;
	}

	private String talkToRuba(L1PcInstance pc) {
		String htmlid = "";

		if (pc.isCrown() || pc.isWizard()) {
			htmlid = "en0101";
		} else if (pc.isKnight() || pc.isElf() || pc.isDarkelf()) {
			htmlid = "en0102";
		}

		return htmlid;
	}

	private String talkToSIGuide(L1PcInstance pc) {
		String htmlid = "";
		if (pc.getLevel() < 3) {
			htmlid = "en0301";
		} else if (pc.getLevel() >= 3 && pc.getLevel() < 7) {
			htmlid = "en0302";
		} else if (pc.getLevel() >= 7 && pc.getLevel() < 9) {
			htmlid = "en0303";
		} else if (pc.getLevel() >= 9 && pc.getLevel() < 12) {
			htmlid = "en0304";
		} else if (pc.getLevel() >= 12 && pc.getLevel() < 13) {
			htmlid = "en0305";
		} else if (pc.getLevel() >= 13 && pc.getLevel() < 25) {
			htmlid = "en0306";
		} else {
			htmlid = "en0307";
		}
		return htmlid;
	}

	private String talkToPopirea(L1PcInstance pc) {
		String htmlid = "";
		if (pc.getLevel() < 25) {
			htmlid = "jpe0041";
			if (pc.getInventory().checkItem(41209)
					|| pc.getInventory().checkItem(41210)
					|| pc.getInventory().checkItem(41211)
					|| pc.getInventory().checkItem(41212)) {
				htmlid = "jpe0043";
			}
			if (pc.getInventory().checkItem(41213)) {
				htmlid = "jpe0044";
			}
		} else {
			htmlid = "jpe0045";
		}
		return htmlid;
	}

	private String talkToSecondtbox(L1PcInstance pc) {
		String htmlid = "";
		if (pc.getQuest().getStep(L1Quest.QUEST_TBOX1) == L1Quest.QUEST_END) {
			if (pc.getInventory().checkItem(40701)) {
				htmlid = "maptboxa";
			} else {
				htmlid = "maptbox0";
			}
		} else {
			htmlid = "maptbox0";
		}
		return htmlid;
	}

	private String talkToThirdtbox(L1PcInstance pc) {
		String htmlid = "";
		if (pc.getQuest().getStep(L1Quest.QUEST_TBOX2) == L1Quest.QUEST_END) {
			if (pc.getInventory().checkItem(40701)) {
				htmlid = "maptboxd";
			} else {
				htmlid = "maptbox0";
			}
		} else {
			htmlid = "maptbox0";
		}
		return htmlid;
	}

	private void talkToBeginnersGuide(L1PcInstance pc, int npcid) {
		if (npcid == 80171) { // ??????????????????
			// ????????????(1200???)???HP???MP?????????
			if (pc.getLevel() < 13) {
				pc.sendPackets(new S_ServerMessage(183)); // \f1??????????????????????????????????????????
				pc.sendPackets(new S_SkillHaste(pc.getId(), 1, 1200));
				pc.broadcastPacket(new S_SkillHaste(pc.getId(), 1, 0));
				pc.sendPackets(new S_SkillSound(pc.getId(), 755));
				pc.broadcastPacket(new S_SkillSound(pc.getId(), 755));
				pc.setMoveSpeed(1);
				pc.setSkillEffect(L1SkillId.STATUS_HASTE, 1200 * 1000);
				pc.sendPackets(new S_ServerMessage(77)); // \f1?????????????????????????????????
				pc.setCurrentHp(pc.getMaxHp());
				pc.setCurrentMp(pc.getMaxMp());
				pc.sendPackets(new S_SkillSound(pc.getId(), 830));
			}
		} else if (npcid == 80172) { // ??????????????????
			// ????????????(2400???)
			if (pc.getLevel() < 13) {
				pc.sendPackets(new S_ServerMessage(183)); // \f1??????????????????????????????????????????
				pc.sendPackets(new S_SkillHaste(pc.getId(), 1, 2400));
				pc.broadcastPacket(new S_SkillHaste(pc.getId(), 1, 0));
				pc.sendPackets(new S_SkillSound(pc.getId(), 755));
				pc.broadcastPacket(new S_SkillSound(pc.getId(), 755));
				pc.setMoveSpeed(1);
				pc.setSkillEffect(L1SkillId.STATUS_HASTE, 2400 * 1000);
			}
			// ?????????????????????
			if (pc.getLevel() < 5) {
				if (pc.getWeapon() != null) {
					for (L1ItemInstance item : pc.getInventory().getItems()) {
						if (pc.getWeapon().equals(item)) {
							L1SkillUse l1skilluse = new L1SkillUse();
							l1skilluse.handleCommands(pc, L1SkillId.BLESS_WEAPON,
									pc.getId(), pc.getX(), pc.getY(), null, 0,
									L1SkillUse.TYPE_NPCBUFF);
							break;
						}
					}
				}
			}
		}
	}
	
	/*
	 * // ????????????????????????????????????????????? private int getRBpcCount(short mapId) { int pcCount =
	 * 0; for (Object obj : L1World.getInstance().getVisibleObjects(mapId)
	 * .values()) { if (obj instanceof L1PcInstance) { L1PcInstance pc =
	 * (L1PcInstance) obj; if (pc != null) { pcCount++; } } } return pcCount; }
	 */

	private static final long REST_MILLISEC = 10000;

	private static final Timer _restTimer = new Timer(true);

	private RestMonitor _monitor;

	public class RestMonitor extends TimerTask {
		@Override
		public void run() {
			setRest(false);
		}
	}

}