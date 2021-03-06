package Server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

class UserInfo extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		private Socket user_socket;
		private Vector<UserInfo> user_vc;
		private String Nickname = "";
		private Server server;

		public UserInfo(Socket soc, Vector<UserInfo> vc, Server server) // 생성자메소드
		{
			// 매개변수로 넘어온 자료 저장
			this.user_socket = soc;
			this.user_vc = vc;
			this.server = server;
			User_network();
		}
		public void User_network() {
			try {
				is = user_socket.getInputStream();//inputStream
				dis = new DataInputStream(is);
				os = user_socket.getOutputStream();//OutputStream
				dos = new DataOutputStream(os);
				Nickname = dis.readUTF(); // 사용자의 닉네임 받는부분
				
				Nickname = Nickname.trim();//blank 제
				server.getTextArea().append("ID " + Nickname + " 접속\n");
				server.getTextArea().setCaretPosition(server.getTextArea().getText().length());		
				send_Message(Nickname + "님 환영합니다."); // 연결된 사용자에게 정상접속을 알림
			} catch (Exception e) {
				server.getTextArea().append("스트림 셋팅 에러\n");
				server.getTextArea().setCaretPosition(server.getTextArea().getText().length());
			}
		}
		
		//클라이언트 메시지를 화면에 출력하고, 모든 클라이언트에게 방송한다.
		public void InMessage(String str) {//
			//server.getTextArea().append("사용자로부터 들어온 메세지 : " + str+"\n");
			server.getTextArea().append("ChatServer]" + str + "\n");
			server.getTextArea().setCaretPosition(server.getTextArea().getText().length());
			// 사용자 메세지 처리
			broad_cast(str);
		}
		
		//방송 함
		public void broad_cast(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo imsi = (UserInfo) user_vc.elementAt(i);
				imsi.send_Message(str);
			}
		}

		public void send_Message(String str) {
			try {
				dos.writeUTF(str);
			} 
			catch (IOException e) {
				server.getTextArea().append("메시지 송신 에러 발생\n");	
				server.getTextArea().setCaretPosition(server.getTextArea().getText().length());
			}
		}

		public void run() // 스레드 정의
		{

			while (true) {
				try {
					
					// 사용자에게 받는 메세지
					
					
					String msg = dis.readUTF();
					msg.trim();
					//wirte to screen and write all 한명에게 출력과 동시에 나머지 인원들에게 broadcasting
					InMessage(msg);
					
				} 
				catch (IOException e) 
				{
					
					try {
						dos.close();
						dis.close();
						user_socket.close();
						server.getChatvc().removeElement( this ); // 에러가난 현재 객체를 벡터에서 지운다
						server.getTextArea().append(server.getChatvc().size() +" : 현재 벡터에 담겨진 사용자 수\n");
						server.getTextArea().append("사용자 접속 끊어짐 자원 반납\n");
						server.getTextArea().setCaretPosition(server.getTextArea().getText().length());
						
						break;
					
					} catch (Exception ee) {
					
					}// catch문 끝
				}// 바깥 catch문끝

			}
			
			
			
		}// run메소드 끝
}