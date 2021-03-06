package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

import Map.Ground;
import Map.Land;

public class GameUserInfo extends Thread {
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private Socket user_socket;
	private Vector<GameUserInfo> user_vc = new Vector<GameUserInfo>();
	private String user_id = "";
	private Server server;
	private int order; // 유저가 들어온 순서
	private int ready; // 레디 여부
	private ArrayList<Land> ownCity = new ArrayList<Land>();
	private String nickname;
	private int balance; // 초기비용
	private boolean hasEscapePrison = false; // 무인도 탈출권
	private boolean hasAngelCard = false; // 천사카드 유무
	private int x = 0, y = 0;
	private int index; // 현재 위치
	private int hasTourSpot = 0; // 관광지 소유 개수
	private int turn = 0;
	private GameUserInfo pc1;
	private GameUserInfo pc2;
	private Ground ground = Ground.getSingleGround();

	public GameUserInfo(Socket soc, Vector<GameUserInfo> vc, Server server) // 생성자메소드
	{
		// 매개변수로 넘어온 자료 저장
		this.user_socket = soc;
		this.user_vc = vc;
		this.server = server;
		balance = 5000000;
		index = 0;
		ready = 0;
		User_network();
	}

	public void User_network() {
		try {
			is = user_socket.getInputStream();// inputStream
			dis = new DataInputStream(is);
			os = user_socket.getOutputStream();// OutputStream
			dos = new DataOutputStream(os);
			/*user_id = dis.readUTF(); // 사용자의 닉네임 받는부분
			user_id = user_id.trim();// blank 제
			server.getTextArea().append("Game Server ID " + user_id + " 접속\n");
			server.getTextArea().setCaretPosition(server.getTextArea().getText().length());*/
			//send_Message(user_id + "님 환영합니다."); // 연결된 사용자에게 정상접속을 알림
		} catch (Exception e) {
			server.getTextArea().append("스트림 셋팅 에러\n");
			server.getTextArea().setCaretPosition(server.getTextArea().getText().length());
		}
	}

	// 클라이언트 메시지를 화면에 출력하고, 모든 클라이언트에게 방송한다.
	public void InMessage(String str) {//
		// server.getTextArea().append("사용자로부터 들어온 메세지 : " + str+"\n");
		server.getTextArea().append(str + "\n");
		//System.out.println(str);
		server.getTextArea().setCaretPosition(server.getTextArea().getText().length());
		// 사용자 메세지 처리

		// broad_cast(str);
	}

	// 방송 함
	public void broad_cast(String str) {
		for (int i = 0; i < user_vc.size(); i++) {
			GameUserInfo imsi = (GameUserInfo) user_vc.get(i);
			imsi.send_Message(str);
		}
	}

	public void send_Message(String str) {
		try {
			// dos.writeUTF(str);
			dos.writeUTF(str);
		} catch (IOException e) {
			server.getTextArea().append("메시지 송신 에러 발생\n");
			server.getTextArea().setCaretPosition(server.getTextArea().getText().length());
		}
	}

	public void protocol(String str) {
		
		InMessage(str);
		String[] msg = str.split("><");

		switch (msg[0]) {
		case "CONN":
			user_id = msg[1];
			order = user_vc.size();
			if (order == 1) { // 첫번째 유저 접속시 순서 인원수를 추가하여 돌려줌 CONN/1/pc1
				pc1 = (GameUserInfo) user_vc.elementAt(0);
				pc1.setNickname(user_id);
				send_Message("CONN><" + user_vc.size() + "><" + user_id);
			}
			if (order == 2) { // 두번째 유저 접속시 인원수와 모든 아이디를 브로드캐스트 CONN/2/pc1/pc2
				pc1 = (GameUserInfo) user_vc.elementAt(0);
				pc2 = (GameUserInfo) user_vc.elementAt(1);
				pc2.setNickname(user_id);
				broad_cast("CONN><" + user_vc.size() + "><" + pc1.user_id + "><" + user_id);
			}
			break;
			
		case "READY":
			user_id = msg[1];
			//ready = Integer.parseInt(msg[2]);
			pc1 = (GameUserInfo) user_vc.elementAt(0);
			pc2 = (GameUserInfo) user_vc.elementAt(1);
			 /*GameUserInfo pc1 = (GameUserInfo) user_vc.elementAt(0);
			 GameUserInfo pc2 = (GameUserInfo) user_vc.elementAt(1);*/

			
			//System.out.println(user_id + "," + pc1.getNickname() + "," + pc2.getNickname());
			if (pc1.getNickname().equals(user_id)) {
				pc1.setReady(1);
			}else if(pc2.getNickname().equals(user_id)) {
				pc2.setReady(1);
			}
			//broad_cast(str);
			// 모든 유저가 ready 상태면 게임 시작
			
			if (pc1.getReady() == 1 && pc2.getReady() == 1) {
				System.out.println(pc1.getReady() + "," + pc2.getReady());
				broad_cast("START><" + pc1.getIndex() + "><" + pc2.getIndex());
				broad_cast("TURN><" + pc1.getNickname());
			}
			break;
			
		case "DICE":
			user_id = msg[1];
			
			int rand1 = Integer.parseInt(msg[2]);//(int) (Math.random() * 6 + 1);
			int rand2 = Integer.parseInt(msg[3]); //(int) (Math.random() * 6 + 1);
			if (pc1.getNickname().equals(user_id)) {
				pc1.setIndex((pc1.getIndex() + rand1 + rand2) % ground.getGround().length);
				/*pc1.setX(ground.getGround()[pc1.getIndex()].getX1());
				pc1.setY(ground.getGround()[pc1.getIndex()].getY1());*/
				broad_cast("MOVE><" + user_id + "><" + rand1 + "><" + rand2 + "><" + pc1.getIndex());
			} else if (pc2.getNickname().equals(user_id)) {
				pc2.setIndex((pc2.getIndex() + rand1 + rand2) % ground.getGround().length);
			/*	pc2.setX(ground.getGround()[pc2.getIndex()].getX2());
				pc2.setY(ground.getGround()[pc2.getIndex()].getY2());*/
				broad_cast("MOVE><" + user_id + "><" + rand1 + "><" + rand2 + "><" + pc2.getIndex());
			}
			break;
			
		case "TURNEND":
			user_id = msg[1];
			if (pc1.getNickname().equals(user_id)) {
				broad_cast("TURN><" + pc2.getNickname());

			} else if (pc2.getNickname().equals(user_id)) {
				broad_cast("TURN><" + pc1.getNickname());
			}
			break;
			
		case "PAYING":	
			broad_cast(str);
			break;
			
		case "UPDATE":	
			broad_cast(str);
			break;
			
		case "INTERCEPT":	
			broad_cast(str);
			break;
			
		case "RESULT":	
			broad_cast(str);
			break;
			
		case "PURCHASE":
			broad_cast(str);
			break;
			//PURCHASE><name><지불액><TOURSPOT><TRUE
			//PURCHASE><name><지불액><CITY><TRUE><별장><빌딩><호텔
			
		case "EXIT":
			user_id = msg[1];
			if (pc1.getName().equals(user_id)) {
				broad_cast("EXIT><" + pc1.getNickname());

			} else if (pc2.getName().equals(user_id)) {
				broad_cast("EXIT><" + pc2.getNickname());
			}
			break;
		}
		
		
	}

	public int getReady() {
		// TODO Auto-generated method stub
		return this.ready;
	}

	public void setReady(int ready) {
		this.ready = ready;
	}

	public void run() // 스레드 정의
	{

		while (true) {
			try {

				// 사용자에게 받는 메세지
				String msg = dis.readUTF();
				msg.trim();
				System.out.println(msg);
				protocol(msg);
				// wirte to screen and write all 한명에게 출력과 동시에 나머지 인원들에게 broadcasting

			} catch (IOException e) {

				try {
					e.printStackTrace();
					dos.close();
					dis.close();
					user_socket.close();
					server.getGamevc().removeElement(this); // 에러가난 현재 객체를 벡터에서 지운다
					server.getTextArea().append(server.getGamevc().size() + " : 현재 벡터에 담겨진 사용자 수\n");
					server.getTextArea().append("사용자 접속 끊어짐 자원 반납\n");
					server.getTextArea().setCaretPosition(server.getTextArea().getText().length());
					
					break;

				} catch (Exception ee) {
					
				} // catch문 끝
			} // 바깥 catch문끝

		}

	}// run메소드 끝

	public String getuserid() {
		return user_id;
	}

	public void setuserid(String user_id) {
		this.user_id = user_id;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public ArrayList<Land> getOwnLandList() {
		return ownCity;
	}

	public int getOwnTotalBalance() {
		int totalBalance = 0;

		for (Land land : ownCity)
			totalBalance += land.getValuePrice();

		return totalBalance;
	}

	public int getAllSellingBalance() {
		int totalBalance = 0;

		for (Land land : ownCity)
			totalBalance += land.sellingPrice();

		return totalBalance;
	}

	

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public boolean hasEscapePrison() {
		return hasEscapePrison;
	}

	public void usingEscapePrison() {
		hasEscapePrison = !hasEscapePrison;
	}

	public boolean hasAngelCard() {
		return hasAngelCard;
	}

	public void usingAngelCard() {
		hasAngelCard = !hasAngelCard;
	}

	public int getTurn() {
		return this.turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}

	public void addLand(Land land) {
		ownCity.add(land);
		land.setOwnerPlayerName(user_id);
	}

	public void sellingLand(Land land) {
		ownCity.remove(land);
		land.setOwnerPlayerName(null);
	}

	public int getHasTourSpot() {
		return hasTourSpot;
	}

	public void setHasTourSpot(int hasTourSpot) {
		this.hasTourSpot = hasTourSpot;
	}

	public ArrayList<Land> getLands() {
		return ownCity;
	}
}