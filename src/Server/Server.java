package Server;
// Java Chatting Server

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Server extends JFrame {
	private JPanel contentPane;
	private JButton Start; // 서버를 실행시킨 버튼
	JTextArea textArea; // 클라이언트 및 서버 메시지 출력

	private ServerSocket chatSocket; // 채팅용 서버 소켓 
	private ServerSocket gameSocket; // 게임용 서버 소켓
	private Socket soc; // 연결소켓 클라이언트당 하나씩 생겨야한다.
	private int Port1 = 30000; // 채팅용 포트번호
	private int Port2 = 30001; // 게임용 포트번호
	private Vector<UserInfo> chatvc = new Vector<>(); // 채팅 연결된 사용자를 저장할 벡터
	private Vector<GameUserInfo> gamevc = new Vector<>(); // 게임 사용자 저장 벡터
	private Server server = this;

	public static void main(String[] args)
	{	
			Server frame = new Server();
			frame.setVisible(true);			
	}

	public Server() {
		init();
	}

	private void init() { // GUI를 구성하는 메소드		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 280, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane js = new JScrollPane();				

		textArea = new JTextArea();
		textArea.setColumns(20);
		textArea.setRows(5);
		js.setBounds(0, 0, 264, 254);
		contentPane.add(js);
		js.setViewportView(textArea);

		Start = new JButton("서버 실행");
		
		Myaction action = new Myaction(); //엔터키 혹은 클릭액션 
		Start.addActionListener(action); // 내부클래스로 액션 리스너를 상속받은 클래스로
		Start.setBounds(0, 325, 264, 37);
		contentPane.add(Start);
	}
	
	class Myaction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		public void actionPerformed(ActionEvent e) {

			server_start();

		}
	
	}
	private void server_start() {
		try {
			chatSocket = new ServerSocket(Port1); // socket, bind, listen 부분 서버가 포트 여는부분
			gameSocket = new ServerSocket(Port2); // gameSocket과 chatSocket 생
			
			Start.setText("채팅/게임 서버 실행");
			Start.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
			
			if(chatSocket!=null) // socket 이 정상적으로 열렸을때
			{
				chatConnection();
			}
			if(gameSocket!=null) {
				gameConnection();
			}
			
			
		} catch (IOException e) {
			textArea.append("소켓이 이미 사용중입니다…\n");
		}

	}

	private void chatConnection() {//연결부분 쓰레드 사
		Thread th = new Thread(new Runnable() { // 사용자 접속을 받을 스레드
			@Override
			public void run() {
				while (true) { // 사용자 접속을 계속해서 받기 위해 while문
					try {
						textArea.append("chatting server 사용자 접속 대기중…\n");
						soc = chatSocket.accept(); // accept가 일어나기 전까지는 무한 대기중
						textArea.append("사용자 접속!!\n");
						UserInfo user = new UserInfo(soc, chatvc, server); // 연결된 소켓 정보는 금방 사라지므로, user 클래스 형태로 객체 생성 
	                                // 매개변수로 현재 연결된 소켓과, 벡터를 담아둔다
						chatvc.add(user); // 해당 벡터에 사용자 객체를 추가
						user.start(); // 만든 객체의 스레드 실행
						if(chatvc.size()==2)
							break;

					} catch (IOException e) {
						textArea.append("!!!! accept 에러 발생… !!!!\n");
					} 
				}
			}
		});
		th.start();
	}
	
	private void gameConnection() {//연결부분 쓰레드 사
		Thread th = new Thread(new Runnable() { // 사용자 접속을 받을 스레드
			@Override
			public void run() {
				while (true) { // 사용자 접속을 계속해서 받기 위해 while문
					try {
						textArea.append("game server 사용자 접속 대기중…\n");
						soc = gameSocket.accept(); // accept가 일어나기 전까지는 무한 대기중
						textArea.append("사용자 접속!!\n");
						GameUserInfo user = new GameUserInfo(soc, gamevc, server); // 연결된 소켓 정보는 금방 사라지므로, user 클래스 형태로 객체 생성 
	                                // 매개변수로 현재 연결된 소켓과, 벡터를 담아둔다
						while(true) {
						gamevc.add(user); // 해당 벡터에 사용자 객체를 추가
						System.out.println(gamevc.size());
						break;
						}
						
						user.start(); // 만든 객체의 스레드 실행
						if(gamevc.size()==2)
							break;
					} catch (IOException e) {
						textArea.append("!!!! accept 에러 발생… !!!!\n");
					} 
				}
			}
		});
		th.start();
	}

	public JTextArea getTextArea() {
		return textArea;
	}

	public void setTextArea(JTextArea textArea) {
		this.textArea = textArea;
	}

	public Vector<UserInfo> getChatvc() {
		return chatvc;
	}

	public Vector<GameUserInfo> getGamevc() {
		return gamevc;
	}

	

	

}