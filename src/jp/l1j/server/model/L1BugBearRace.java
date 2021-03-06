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

package jp.l1j.server.model;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;
import static jp.l1j.locale.I18N.*;
import jp.l1j.server.GeneralThreadPool;
import jp.l1j.server.datatables.DoorTable;
import jp.l1j.server.datatables.NpcTable;
import jp.l1j.server.datatables.RaceTicketTable;
import jp.l1j.server.datatables.ShopTable;
import jp.l1j.server.model.instance.L1DoorInstance;
import jp.l1j.server.model.instance.L1MerchantInstance;
import jp.l1j.server.model.instance.L1NpcInstance;
import jp.l1j.server.model.instance.L1PcInstance;
import jp.l1j.server.model.map.L1Map;
import jp.l1j.server.model.shop.L1Shop;
import jp.l1j.server.packets.server.S_NpcChatPacket;
import jp.l1j.server.packets.server.S_NpcPack;
import jp.l1j.server.random.RandomGenerator;
import jp.l1j.server.random.RandomGeneratorFactory;
import jp.l1j.server.templates.L1RaceTicket;
import jp.l1j.server.templates.L1ShopItem;
import jp.l1j.server.utils.IdFactory;

public class L1BugBearRace {
	L1MerchantInstance pory;
	L1MerchantInstance cecile;
	L1MerchantInstance parkin;
	private static final int FIRST_ID = 0x0000000;
	private static final int STATUS_NONE = 0;
	private static final int STATUS_READY = 1;
	private static final int STATUS_PLAYING = 2;
	private static final int STATUS_END = 3;
	private static final int WAIT_TIME = 60;
	private static final int READY_TIME = 9 * 60 - 10;// test 60;//
	private static final int FIRST_NPCID = 91350;// ~20
	private final L1NpcInstance[] _runner;
	private final int[] _runnerStatus = new int[5];
	private final double[] _winning_average = new double[5];
	private final double[] _allotment_percentage = new double[5];
	private final int[] _condition = new int[5];
	private int _allBet;
	private final int[] _betCount = new int[5];
	private int _round;

	public int getRound() {
		return _round;
	}

	private void setRound(int round) {
		this._round = round;
	}

	private static RandomGenerator _random = RandomGeneratorFactory.newRandom();

	private static L1BugBearRace instance;

	public static L1BugBearRace getInstance() {
		if (instance == null) {
			instance = new L1BugBearRace();
		}
		return instance;
	}

	L1BugBearRace() {
		setRound(RaceTicketTable.getInstance().getRoundNumOfMax());
		_runner = new L1NpcInstance[5];
		for (L1Object obj : L1World.getInstance().getObject()) {
			if (obj instanceof L1MerchantInstance) {
				if (((L1MerchantInstance) obj).getNpcId() == 70041) {
					parkin = (L1MerchantInstance) obj;
				}
			}
		}
		for (L1Object obj : L1World.getInstance().getObject()) {
			if (obj instanceof L1MerchantInstance) {
				if (((L1MerchantInstance) obj).getNpcId() == 70035) {
					cecile = (L1MerchantInstance) obj;
				}
			}
		}
		for (L1Object obj : L1World.getInstance().getObject()) {
			if (obj instanceof L1MerchantInstance) {
				if (((L1MerchantInstance) obj).getNpcId() == 70042) {
					pory = (L1MerchantInstance) obj;
				}
			}
		}
		new RaceTimer(0).begin();
	}

	private void setRandomRunner() {
		for (int i = 0; i < 5; i++) {
			int npcid = FIRST_NPCID + _random.nextInt(20);
			while (checkDuplicate(npcid, i)) {
				npcid = FIRST_NPCID + _random.nextInt(20);
			}
			L1Location loc = new L1Location(33522 - (i * 2), 32861 + (i * 2), 4);
			_runner[i] = spawnOne(loc, npcid, 6);

		}
	}

	private boolean checkDuplicate(int npcid, int n) {
		for (int i = 0; i < n; i++) {
			if (_runner[i] != null) {
				if (_runner[i].getNpcId() == npcid) {
					return true;
				}
			}
		}
		return false;
	}

	private void clearRunner() {
		for (int i = 0; i < 5; i++) {
			if (_runner[i] != null) {
				_runner[i].deleteMe();
				// XXX
				if (_runner[i].getMap().isInMap(_runner[i].getX(),
						_runner[i].getY())) {
					_runner[i].getMap().setPassable(_runner[i].getX(),
							_runner[i].getY(), true);
				}
			}
			_runner[i] = null;
			_runnerStatus[i] = 0;
			_condition[i] = 0;
			_winning_average[i] = 0;
			_allotment_percentage[i] = 0.0;
			setBetCount(i, 0);
		}
		setAllBet(0);
		for (L1DoorInstance door : DoorTable.getInstance().getDoorList()) {
			if (door.getDoorId() <= 812 && door.getDoorId() >= 808) {
				door.close();
			}
		}
	}

	private boolean checkPosition(int runnerNumber) {// ?????????????????????????????????
		final int[] defaultHead = { 6, 7, 0, 1, 2, 2 };
		if (getGameStatus() != STATUS_PLAYING) {
			return false;
		}
		boolean flag = false;// ????????????????????????false?????????
		L1NpcInstance npc = _runner[runnerNumber];
		int x = npc.getX();
		int y = npc.getY();
		if (_runnerStatus[runnerNumber] == 0) {// ?????????????????????
			if (// x==33476+(runnerNumber*2)&&y==32861+(runnerNumber*2)
			(x >= 33476 && x <= 33476 + 8) && (y >= 32861 && y <= 32861 + 8)) {
				_runnerStatus[runnerNumber] = _runnerStatus[runnerNumber] + 1;
				npc.setHeading(defaultHead[_runnerStatus[runnerNumber]]);// ??????????????????
			} else {
				npc.setHeading(defaultHead[_runnerStatus[runnerNumber]]);// ??????????????????
			}
		} else if (_runnerStatus[runnerNumber] == 1) {//
			if ((x <= 33473 && x >= 33473 - 9) && y == 32858) {
				_runnerStatus[runnerNumber] = _runnerStatus[runnerNumber] + 1;
				npc.setHeading(defaultHead[_runnerStatus[runnerNumber]]);// ??????????????????
			} else {
				npc.setHeading(defaultHead[_runnerStatus[runnerNumber]]);// ??????????????????
			}
		} else if (_runnerStatus[runnerNumber] == 2) {//
			if ((x <= 33473 && x >= 33473 - 9) && y == 32852) {
				_runnerStatus[runnerNumber] = _runnerStatus[runnerNumber] + 1;
				npc.setHeading(defaultHead[_runnerStatus[runnerNumber]]);// ??????????????????
			} else {
				npc.setHeading(defaultHead[_runnerStatus[runnerNumber]]);// ??????????????????
			}
		} else if (_runnerStatus[runnerNumber] == 3) {//
			if ((x == 33478 && (y <= 32847 && y >= 32847 - 9))) {
				_runnerStatus[runnerNumber] = _runnerStatus[runnerNumber] + 1;
				npc.setHeading(defaultHead[_runnerStatus[runnerNumber]]);// ??????????????????
			} else {
				npc.setHeading(defaultHead[_runnerStatus[runnerNumber]]);// ??????????????????
			}
		} else if (_runnerStatus[runnerNumber] == 4) {//
			if (x == 33523 && (y >= 32847 - 9 && y <= 32847)) {
				_runnerStatus[runnerNumber] = _runnerStatus[runnerNumber] + 1;
				npc.setHeading(defaultHead[_runnerStatus[runnerNumber]]);// ??????????????????
				// goal
				goal(runnerNumber);
			} else {
				npc.setHeading(defaultHead[_runnerStatus[runnerNumber]]);// ??????????????????
			}
		} else if (_runnerStatus[runnerNumber] == 5) {//
			if (x == 33527 && (y >= 32847 - 8 && y <= 32847)) {
				npc.setHeading(defaultHead[_runnerStatus[runnerNumber]]);// ??????????????????
				finish();
				flag = true;
			} else {
				npc.setHeading(defaultHead[_runnerStatus[runnerNumber]]);// ??????????????????
			}
		}
		return flag;
	}

	private void finish() {
		int cnt = 0;
		for (int i = 0; i < _runnerStatus.length; i++) {
			if (_runnerStatus[i] == 5) {
				cnt++;
			}
		}
		if (cnt == 5) {
			setGameStatus(STATUS_END);
			new RaceTimer(30).begin();
			/* SHOP???????????? */
		}
	}

	private void goal(int runnberNumber) {
		int cnt = 0;
		for (int i = 0; i < _runnerStatus.length; i++) {
			if (_runnerStatus[i] == 5) {
				cnt++;
			}
		}
		if (cnt == 1) {
			cecile.wideBroadcastPacket(new S_NpcChatPacket(cecile,
					String.format(I18N_RACE_ROUND, getRound()) + " $366 "
					+ NpcTable.getInstance().getTemplate(_runner[runnberNumber].getNpcId()).getNameId()
					+ " $367", 2));// 5>3?????????
			/* DB???????????? */
			RaceTicketTable.getInstance().updateTicket(getRound(),
					_runner[runnberNumber].getNpcId() - FIRST_NPCID + 1,
					_allotment_percentage[runnberNumber]);
		}
	}

	// TODO ????????????????????????end

	private int _status = 0;

	public void setGameStatus(int i) {
		_status = i;
	}

	public int getGameStatus() {
		return _status;
	}

	private void sendMessage(String id) {
		parkin.wideBroadcastPacket(new S_NpcChatPacket(parkin, id, 2));
		// cecile.broadcastPacket(new S_NpcChatPacket(cecile,id, 2));
		pory.wideBroadcastPacket(new S_NpcChatPacket(pory, id, 2));
	}

	private class RaceTimer extends TimerTask {
		int _startTime;

		RaceTimer(int startTime) {
			_startTime = startTime;
		}

		@Override
		public void run() {
			try {
				// ??????????????????????????????NONE??????10?????????
				setGameStatus(STATUS_NONE);
				sendMessage("$376 10 $377");
				for (int loop = 0; loop < WAIT_TIME; loop++) {
					Thread.sleep(1000);
				}
				clearRunner();
				setRound(getRound() + 1);
				/* ???????????????-???????????? */
				L1RaceTicket ticket = new L1RaceTicket();
				ticket.setItemObjId(FIRST_ID);// ????????????
				ticket.setAllotmentPercentage(0);
				ticket.setRound(getRound());
				ticket.setRunnerNum(0);
				ticket.setVictory(0);
				RaceTicketTable.getInstance().storeNewTiket(ticket);// ?????????
				RaceTicketTable.getInstance().oldTicketDelete(getRound());// ?????????????????????
				/**/
				setRandomRunner();// ??????????????????
				setRandomCondition();
				/* SHOP BuyList???????????? */
				L1Shop shop1 = ShopTable.getInstance().get(70035);
				L1Shop shop2 = ShopTable.getInstance().get(70041);
				L1Shop shop3 = ShopTable.getInstance().get(70042);
				for (int i = 0; i < 5; i++) {
					L1ShopItem shopItem1 = new L1ShopItem(40309, 500, 1);
					shopItem1.setName(i);
					L1ShopItem shopItem2 = new L1ShopItem(40309, 500, 1);
					shopItem2.setName(i);
					L1ShopItem shopItem3 = new L1ShopItem(40309, 500, 1);
					shopItem3.setName(i);
					shop1.getSellingItems().add(shopItem1);
					shop2.getSellingItems().add(shopItem2);
					shop3.getSellingItems().add(shopItem3);
				}
				/**/
				setWinnigAverage();
				setGameStatus(STATUS_READY);
				for (int loop = 0; loop < READY_TIME - 1; loop++) {
					if (loop % 60 == 0) {
						sendMessage("$376 " + (1 + (READY_TIME - loop) / 60) + " $377");
					}
					Thread.sleep(1000);
				}
				sendMessage("$363");// 363 ???????????????
				Thread.sleep(1000);
				for (int loop = 10; loop > 0; loop--) {
					sendMessage("" + loop);
					Thread.sleep(1000);
				}
				sendMessage("$364");// 364 ?????????
				setGameStatus(STATUS_PLAYING);
				/* SHOP BuyList???????????? */
				shop1.getSellingItems().clear();
				shop2.getSellingItems().clear();
				shop3.getSellingItems().clear();
				/**/
				for (L1DoorInstance door : DoorTable.getInstance().getDoorList()) {
					if (door.getDoorId() <= 812 && door.getDoorId() >= 808) {
						door.open();
					}
				}
				for (int i = 0; i < _runner.length; i++) {
					new BugBearRunning(i).begin(0);
				}
				new StartBuffTimer().begin();
				for (int i = 0; i < _runner.length; i++) {
					if (getBetCount(i) > 0) {
						// TODO ?????????????????????????????????0.2?????????
						BigDecimal p = new BigDecimal(String.valueOf((double) (getAllBet()
								/ (getBetCount(i)) / 500D) + 0.2));
						// ????????????3??????????????????
						_allotment_percentage[i] = p.setScale(2,BigDecimal.ROUND_DOWN).doubleValue();;
					} else {
						_allotment_percentage[i] = 0.0;
					}
				}
				for (int i = 0; i < _runner.length; i++) {
					Thread.sleep(1000);
					sendMessage(NpcTable.getInstance().getTemplate(
							_runner[i].getNpcId()).getNameId()
							+ " $402 "// ?????????3???????????????????????????????????????ID???????????????????????????
							+ String.valueOf(_allotment_percentage[i]));// 402
					// ???????????????
				}
				this.cancel();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void begin() {
			Timer timer = new Timer();
			timer.schedule(this, _startTime * 1000);
		}
	}

	private class BugBearRunning implements Runnable {
		final L1NpcInstance _bugBear;
		final int _runnerNumber;

		BugBearRunning(int runnerNumber) {
			_bugBear = _runner[runnerNumber];
			_runnerNumber = runnerNumber;
		}

		@Override
		public void run() {
			if (getGameStatus() != STATUS_PLAYING) {
				return;
			}
			_bugBear.setDirectionMove(calcNewHeading());// ???????????????
			if (checkPosition(_runnerNumber)) {
				return;
			}
			int sleepTime = calcSleepTime(_bugBear);
			GeneralThreadPool.getInstance().schedule(this, sleepTime);
		}

		private int calcSleepTime(L1NpcInstance runner) {
			int sleepTime = runner.getPassiSpeed();
			if (runner.getBraveSpeed() == 1) {
				sleepTime -= (sleepTime * 0.25);
			}
			return sleepTime;
		}

		private int calcNewHeading() {
			int x = _bugBear.getX();
			int y = _bugBear.getY();
			int heading = _bugBear.getHeading();
			L1Map map = _bugBear.getMap();
			while (!map.isPassable(x, y, heading)) {
				if (map.isPassable(x, y, Heading.rotateRight(heading))) {
					heading = Heading.rotateRight(heading);
					break;
				}
				heading = Heading.rotateLeft(heading);
			}
			return heading;
		}

		public void begin(int startTime) {
			GeneralThreadPool.getInstance().schedule(this, startTime);
		}
	}

	/**
	 * ?????????????????????????????????????????????Npc????????????????????????
	 * 
	 * @param loc
	 *            ????????????
	 * @param npcid
	 *            ?????????NpcId
	 * @param heading
	 *            ??????
	 * @return L1NpcInstance ????????? : ??????=?????????????????????????????? ??????=null
	 */
	private L1NpcInstance spawnOne(L1Location loc, int npcid, int heading) {
		final L1NpcInstance mob = new L1NpcInstance(NpcTable.getInstance().getTemplate(npcid));
		if (mob == null) {
			return mob;
		}
		mob.setNameId("#" + (mob.getNpcId() - FIRST_NPCID + 1) + " " + mob.getNameId());
		mob.setId(IdFactory.getInstance().nextId());
		mob.setHeading(heading);
		mob.setX(loc.getX());
		mob.setHomeX(loc.getX());
		mob.setY(loc.getY());
		mob.setHomeY(loc.getY());
		mob.setMap((short) loc.getMapId());
		mob.setMoveSpeed(mob.getPassiSpeed() * 2);
		L1World.getInstance().storeObject(mob);
		L1World.getInstance().addVisibleObject(mob);
		final S_NpcPack S_NpcPack = new S_NpcPack(mob);
		for (final L1PcInstance pc : L1World.getInstance().getRecognizePlayer(mob)) {
			pc.addKnownObject(mob);
			mob.addKnownObject(pc);
			pc.sendPackets(S_NpcPack);
		}
		// ?????????????????????????????????
		mob.onNpcAI();
		mob.updateLight();
		mob.startChat(L1NpcInstance.CHAT_TIMING_APPEARANCE); // ??????????????????
		return mob;
	}

	public void setAllBet(int allBet) {// allbet???3????????????
		this._allBet = (int) (allBet * 0.9);// 1????????????????????????
	}

	public int getAllBet() {
		return _allBet;
	}

	public int getBetCount(int i) {
		return _betCount[i];
	}

	public void setBetCount(int i, int count) {
		_betCount[i] = count;
	}

	private class StartBuffTimer extends TimerTask {
		StartBuffTimer() {
		}

		@Override
		public void run() {
			if (getGameStatus() == STATUS_PLAYING) {
				for (int i = 0; i < _runner.length; i++) {
					if (getRandomProbability() <= _winning_average[i]
							* (1 + (0.2 * getCondition(i)))) {
						_runner[i].setBraveSpeed(1);
					} else {
						_runner[i].setBraveSpeed(0);
					}
				}
			} else {
				this.cancel();
			}
		}

		public void begin() {
			Timer _timer = new Timer();
			_timer.scheduleAtFixedRate(this, 1000, 1000);
		}
	}

	private double getRandomProbability() {
		return (_random.nextInt(10000) + 1) / 100D;
	}
	
	private void setWinnigAverage() {
		for (int i = 0; i < _winning_average.length - 1; i++) {
			double winningAverage = getRandomProbability();
			while (checkDuplicateAverage(winningAverage, i)) {
				winningAverage = getRandomProbability();
			}
			_winning_average[i] = winningAverage;
		}
	}

	private boolean checkDuplicateAverage(double winning_average, int n) {
		for (int i = 0; i < n; i++) {
			if (_winning_average[i] == winning_average && _condition[i] == _condition[n]) {
				return true;
			}
		}
		return false;
	}

	/*
	 * private void setCondition(int num, int condition) { this._condition[num]
	 * = condition; }
	 */

	public int getCondition(int num) {
		return _condition[num];
	}

	private void setRandomCondition() {
		for (int i = 0; i < _condition.length; i++) {
			_condition[i] = -1 + _random.nextInt(3);
		}
	}

	public L1NpcInstance getRunner(int num) {
		return _runner[num];
	}

	public double getWinningAverage(int num) {
		return _winning_average[num];
	}
}
