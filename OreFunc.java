public class OreFunc {
	public static void main(String[] args) {
		int need = 10000; //whatever we want
		int beavercost = 100;
		int beaverincome = 2;
		int beaverdelay = 20;
		int income = 10;
		int ore = 500;
		int beaversneeded = 0;
		int turnsneeded = perbeaver(income, ore, need, 0);
		for (int beavernum = 1; beavernum < need/beavercost; beavernum++) {
			int thisincome = income + beavernum*beaverincome;
			int thisore = ore - beavernum*beavercost;
			int thisneed = need - thisore;
			int thisresult = perbeaver(thisincome, thisore, thisneed, beavernum);
			if (thisresult < turnsneeded) {
				turnsneeded = thisresult;
				beaversneeded = beavernum;
			}
		}
		System.out.print("we need " + turnsneeded + " turns to get " + need + " ore, making " + beaversneeded + " beaver(s).");
	}
	public static int perbeaver(int income, int ore, int need, int beavernum) {
		int turnsneeded = 0;
		int count = 0;
		for (int turn = 1; turn < need/5; turn += 2) {
			ore += income;
			if (ore >= need) {
				turnsneeded = turn;
				break;
			}
			if (beavernum > 0) {
				if (count == 0) { //can make a beaver immediately (turn 1)
					count = 20;
					beavernum--;
					ore -= 100;
					income += 2;
				}
				else count--; //takes twenty turns to make a beaver
			}
		}
		return turnsneeded;
	}
}
